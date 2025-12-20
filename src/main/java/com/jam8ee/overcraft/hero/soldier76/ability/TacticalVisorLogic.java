package com.jam8ee.overcraft.hero.soldier76.ability;

import com.jam8ee.overcraft.core.gameplay.weapon.GunItem;
import com.jam8ee.overcraft.core.network.ModNetwork;
import com.jam8ee.overcraft.core.network.s2c.SyncUltimateS2CPacket;
import com.jam8ee.overcraft.core.registry.ModSounds;
import com.jam8ee.overcraft.hero.soldier76.Soldier76VisorTargeting;
import com.jam8ee.overcraft.hero.soldier76.data.Soldier76Tuning;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.PacketDistributor;

/**
 * Tactical Visor（大招）核心逻辑（从 Soldier76Ultimate 迁移过来）
 */
public final class TacticalVisorLogic {
    private TacticalVisorLogic() {}

    /** 触发大招（开始计时、设置 active、播放音效等） */
    public static void start(ServerPlayer player) {
        // 目前：手持枪才算 76（后面接 Hero 系统再替换）
        if (!(player.getMainHandItem().getItem() instanceof GunItem)) return;

        if (getUltActive(player) > 0) return;

        if (getUltChargePercent(player) < 100) {
            player.level().playSound(null, player.blockPosition(),
                    SoundEvents.COMPARATOR_CLICK, SoundSource.PLAYERS, 0.6f, 0.7f);
            return;
        }

        // 开大：清空充能，进入 ACTIVE
        setUltUnits(player, 0);
        setUltActive(player, Soldier76Tuning.ULT_DURATION_TICKS);
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

    /** 每 tick 更新：大招倒计时 / 非激活被动充能 / 锁定粒子提示 */
    public static void tick(ServerPlayer player) {
        // ===== 大招激活期间：倒计时 + 锁定粒子 =====
        int active = getUltActive(player);
        if (active > 0) {
            int next = active - 1;
            setUltActive(player, next);

            // 每 5 tick 同步 UI（省网络）+ 结束时同步
            if (next == 0 || (next % 5 == 0)) syncToClient(player);

            // 目标身上粒子提示（只发给该玩家自己）
            tickLockParticles(player);
            return;
        }

        // ===== 非激活：被动充能（每秒 +0.1%）=====
        addPassiveCharge(player);
    }

    /** 被动充能：按秒增长（每秒 +0.1%） */
    public static void addPassiveCharge(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        int t = tag.getInt(Soldier76Tuning.NBT_PASSIVE_T) + 1;

        if (t >= 20) { // 1 秒
            t = 0;
            addUnits(player, Soldier76Tuning.PASSIVE_UNITS_PER_SECOND);

            // 你原来写的是 “%1”，等于每秒都同步一次；我保持行为不变
            int percent = getUltChargePercent(player);
            if (percent == 100 || percent % 1 == 0) {
                syncToClient(player);
            }
        }

        tag.putInt(Soldier76Tuning.NBT_PASSIVE_T, t);
    }

    /** 通过造成伤害充能（Helix 也应该走这里） */
    public static void addDamageCharge(ServerPlayer player, float damageDealt) {
        if (damageDealt <= 0.0f) return;
        if (getUltActive(player) > 0) return; // 开大期间不充能（保持你当前规则）

        int add = Math.round(damageDealt * Soldier76Tuning.UNITS_PER_DAMAGE);
        if (add <= 0) return;

        int before = getUltUnits(player);
        addUnits(player, add);
        int after = getUltUnits(player);

        // 到 100% 或者跨过整百分点就同步一次（避免每次伤害都发包）
        int beforePct = before / Soldier76Tuning.UNITS_PER_PERCENT;
        int afterPct  = after  / Soldier76Tuning.UNITS_PER_PERCENT;

        if (afterPct != beforePct || after == Soldier76Tuning.FULL_CHARGE_UNITS) {
            syncToClient(player);
        }
    }

    public static boolean isUltActive(ServerPlayer player) {
        return getUltActive(player) > 0;
    }

    public static int getUltActive(ServerPlayer player) {
        return player.getPersistentData().getInt(Soldier76Tuning.NBT_ULT_ACTIVE);
    }

    public static int getUltChargePercent(ServerPlayer player) {
        int units = getUltUnits(player);
        int pct = units / Soldier76Tuning.UNITS_PER_PERCENT; // 220000 -> 100
        if (pct < 0) pct = 0;
        if (pct > 100) pct = 100;
        return pct;
    }

    /** 同步到客户端 HUD */
    public static void syncToClient(ServerPlayer player) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncUltimateS2CPacket(getUltChargePercent(player), getUltActive(player))
        );
    }

    // ===== 内部：粒子提示 =====
    private static void tickLockParticles(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return;

        CompoundTag tag = player.getPersistentData();
        int fxT = tag.getInt(Soldier76Tuning.NBT_LOCK_FX_T) + 1;

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

        tag.putInt(Soldier76Tuning.NBT_LOCK_FX_T, fxT);
    }

    // ===== 内部：units/active =====
    private static int getUltUnits(ServerPlayer player) {
        int u = player.getPersistentData().getInt(Soldier76Tuning.NBT_ULT_UNITS);
        if (u < 0) u = 0;
        if (u > Soldier76Tuning.FULL_CHARGE_UNITS) u = Soldier76Tuning.FULL_CHARGE_UNITS;
        return u;
    }

    private static void setUltUnits(ServerPlayer player, int units) {
        int u = Math.max(0, Math.min(Soldier76Tuning.FULL_CHARGE_UNITS, units));
        player.getPersistentData().putInt(Soldier76Tuning.NBT_ULT_UNITS, u);
    }

    private static void addUnits(ServerPlayer player, int add) {
        int u = getUltUnits(player);
        int next = u + add;
        if (next > Soldier76Tuning.FULL_CHARGE_UNITS) next = Soldier76Tuning.FULL_CHARGE_UNITS;
        if (next < 0) next = 0;
        setUltUnits(player, next);
    }

    private static void setUltActive(ServerPlayer player, int ticks) {
        player.getPersistentData().putInt(Soldier76Tuning.NBT_ULT_ACTIVE, Math.max(0, ticks));
    }
}
