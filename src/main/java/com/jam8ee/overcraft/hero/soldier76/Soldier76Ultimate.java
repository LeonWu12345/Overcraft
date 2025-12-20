package com.jam8ee.overcraft.hero.soldier76;

import com.jam8ee.overcraft.core.gameplay.weapon.GunItem;
import com.jam8ee.overcraft.core.network.ModNetwork;
import com.jam8ee.overcraft.core.network.s2c.SyncUltimateS2CPacket;
import com.jam8ee.overcraft.core.registry.ModSounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;

public final class Soldier76Ultimate {
    private Soldier76Ultimate() {}

    // ===== Tactical Visor =====
    public static final int ULT_DURATION_TICKS = 160; // 8s

    // ===== Charge rules (你要的规则) =====
    // 220 点伤害 = 100%
    // 被动每秒 +0.1%
    public static final int FULL_CHARGE_UNITS = 220_000;
    public static final int UNITS_PER_DAMAGE = 1_000; // 1 damage -> +1000 units
    public static final int PASSIVE_UNITS_PER_SECOND = 220; // 0.1%/sec -> 220 units/sec

    private static final String NBT_ULT_UNITS  = "overcraft_s76_ult_units";   // int 0..220000
    private static final String NBT_ULT_ACTIVE = "overcraft_s76_ult_active";  // ticks remaining
    private static final String NBT_PASSIVE_T  = "overcraft_ult_passive_t";   // 0..19
    private static final String NBT_LOCK_FX_T  = "overcraft_ult_lock_fx_t";   // throttle particles

    public static void tryActivate(ServerPlayer player) {
        // 目前：手持枪才算 76（后面有 Hero 系统再替换）
        if (!(player.getMainHandItem().getItem() instanceof GunItem)) return;

        if (getUltActive(player) > 0) return;

        if (getUltChargePercent(player) < 100) {
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.COMPARATOR_CLICK, SoundSource.PLAYERS, 0.6f, 0.7f);
            return;
        }

        // 开大：清空充能，进入 ACTIVE
        setUltUnits(player, 0);
        setUltActive(player, ULT_DURATION_TICKS);
        syncToClient(player);

        player.level().playSound(
                null,
                player.blockPosition(),
                ModSounds.TACTICAL_VISOR_ACTIVATE.get(),
                SoundSource.PLAYERS,
                1.0f,
                1.0f
        );
    }

    /**
     * 每 tick 调用：处理大招计时、被动充能、锁定粒子提示
     */
    public static void serverTick(ServerPlayer player) {
        // ===== 大招激活期间：倒计时 + 锁定粒子 =====
        int active = getUltActive(player);
        if (active > 0) {
            int next = active - 1;
            setUltActive(player, next);

            // 每 5 tick 同步 UI（省网络）+ 结束时同步
            if (next == 0 || (next % 5 == 0)) syncToClient(player);

            // C：目标身上粒子提示（只发给该玩家自己）
            tickLockParticles(player);

            return;
        }

        // ===== 非激活：被动充能（每秒 +0.1%）=====
        CompoundTag tag = player.getPersistentData();
        int t = tag.getInt(NBT_PASSIVE_T) + 1;

        if (t >= 20) { // 1 秒
            t = 0;
            addUnits(player, PASSIVE_UNITS_PER_SECOND);

            // 每 1% 同步一次，或者到 100% 时同步
            int percent = getUltChargePercent(player);
            if (percent == 100 || percent % 1 == 0) {
                // 注意：%1 永远为0，但这里的意图是“每秒都同步也行”
                // 为了更省网络：改成每 2% 或 5% 同步你随时说
                syncToClient(player);
            }
        }

        tag.putInt(NBT_PASSIVE_T, t);
    }

    private static void tickLockParticles(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;

        CompoundTag tag = player.getPersistentData();
        int fxT = tag.getInt(NBT_LOCK_FX_T) + 1;

        // 每 2 tick 发一次（别太密）
        if (fxT >= 2) {
            fxT = 0;

            LivingEntity target = Soldier76VisorTargeting.findBestTarget(player);
            if (target != null) {
                double x = target.getX();
                double y = target.getY() + target.getBbHeight() * 0.75;
                double z = target.getZ();

                // 只发送给该玩家（不会让全服务器都看到）
                level.sendParticles(
                        player,
                        ParticleTypes.ELECTRIC_SPARK,
                        true,
                        x, y, z,
                        6,
                        0.18, 0.22, 0.18,
                        0.0
                );
            }
        }

        tag.putInt(NBT_LOCK_FX_T, fxT);
    }

    // ====== 伤害充能入口 ======
    public static void addChargeFromDamage(ServerPlayer player, float damage) {
        if (damage <= 0.0f) return;
        if (getUltActive(player) > 0) return; // 开大期间不充能（你要开大也充能也行）

        int add = Math.round(damage * UNITS_PER_DAMAGE);
        if (add <= 0) return;

        int before = getUltUnits(player);
        addUnits(player, add);

        int after = getUltUnits(player);

        // 到 100% 或者跨过整百分点就同步一次（避免每次伤害都发包）
        int beforePct = before / 2200;
        int afterPct  = after / 2200;

        if (afterPct != beforePct || after == FULL_CHARGE_UNITS) {
            syncToClient(player);
        }
    }

    // ====== 查询/状态 ======
    public static boolean isUltActive(ServerPlayer player) {
        return getUltActive(player) > 0;
    }

    public static int getUltActive(ServerPlayer player) {
        return player.getPersistentData().getInt(NBT_ULT_ACTIVE);
    }

    public static int getUltChargePercent(ServerPlayer player) {
        int units = getUltUnits(player);
        // 220000 units -> 100%
        int pct = units / 2200;
        if (pct < 0) pct = 0;
        if (pct > 100) pct = 100;
        return pct;
    }

    private static int getUltUnits(ServerPlayer player) {
        int u = player.getPersistentData().getInt(NBT_ULT_UNITS);
        if (u < 0) u = 0;
        if (u > FULL_CHARGE_UNITS) u = FULL_CHARGE_UNITS;
        return u;
    }

    private static void setUltUnits(ServerPlayer player, int units) {
        int u = Math.max(0, Math.min(FULL_CHARGE_UNITS, units));
        player.getPersistentData().putInt(NBT_ULT_UNITS, u);
    }

    private static void addUnits(ServerPlayer player, int add) {
        int u = getUltUnits(player);
        int next = u + add;
        if (next > FULL_CHARGE_UNITS) next = FULL_CHARGE_UNITS;
        if (next < 0) next = 0;
        setUltUnits(player, next);
    }

    private static void setUltActive(ServerPlayer player, int ticks) {
        player.getPersistentData().putInt(NBT_ULT_ACTIVE, Math.max(0, ticks));
    }

    // ====== 同步到客户端 HUD ======
    public static void syncToClient(ServerPlayer player) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncUltimateS2CPacket(getUltChargePercent(player), getUltActive(player))
        );
    }
}
