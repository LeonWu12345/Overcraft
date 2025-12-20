package com.jam8ee.overcraft.core.gameplay.weapon;

import com.jam8ee.overcraft.core.util.RaycastUtil;
import com.jam8ee.overcraft.hero.soldier76.Soldier76Ultimate;
import com.jam8ee.overcraft.hero.soldier76.Soldier76VisorTargeting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class GunItem extends Item {

    // ====== Weapon Tunables ======
    public static final int MAG_SIZE = 30;
    public static final int FIRE_INTERVAL_TICKS = 2;    // 5 发/秒
    public static final int RELOAD_TICKS = 30;          // 1.5 秒（默认）
    private static final int RELOAD_TICKS_ULT = 10;     // ✅ 0.5 秒（大招期间）
    private static final double RANGE = 50.0;
    private static final float DAMAGE = 6.0f;
    private static final double SPREAD = 0.015;

    // ====== NBT keys ======
    private static final String NBT_AMMO = "Ammo";
    private static final String NBT_RELOAD_REMAIN = "ReloadRemain";

    public GunItem(Properties props) {
        super(props);
    }

    // ---------------- Ammo / Reload state helpers ----------------

    public int getAmmo(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(NBT_AMMO)) tag.putInt(NBT_AMMO, MAG_SIZE);
        return tag.getInt(NBT_AMMO);
    }

    public void setAmmo(ItemStack stack, int ammo) {
        stack.getOrCreateTag().putInt(NBT_AMMO, Math.max(0, Math.min(MAG_SIZE, ammo)));
    }

    public boolean isReloading(ItemStack stack) {
        return stack.getOrCreateTag().getInt(NBT_RELOAD_REMAIN) > 0;
    }

    public int getReloadRemain(ItemStack stack) {
        return stack.getOrCreateTag().getInt(NBT_RELOAD_REMAIN);
    }

    /**
     * ✅ 根据玩家状态决定本次换弹时间
     * - 普通：RELOAD_TICKS
     * - 大招期间：RELOAD_TICKS_ULT（0.5s）
     */
    private int getReloadTicksFor(Player player) {
        if (player instanceof ServerPlayer sp && Soldier76Ultimate.isUltActive(sp)) {
            return RELOAD_TICKS_ULT;
        }
        return RELOAD_TICKS;
    }

    /**
     * ✅ 改为带 player：才能判断大招状态
     */
    public void startReload(Player player, ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_RELOAD_REMAIN, getReloadTicksFor(player));
    }

    public void tickReload(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int remain = tag.getInt(NBT_RELOAD_REMAIN);
        if (remain <= 0) return;

        remain--;
        tag.putInt(NBT_RELOAD_REMAIN, remain);

        if (remain == 0) {
            // 无限备弹版本：结束直接补满
            tag.putInt(NBT_AMMO, MAG_SIZE);
        }
    }

    // ---------------- Server entry points ----------------

    /**
     * 由服务器调用：如果玩家主手拿着枪，就尝试开火（消耗弹匣）
     * - 弹匣清空后自动换弹
     * - ✅ 大招期间自动换弹为 0.5s
     */
    public static void serverShootIfHoldingGun(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem gun)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        // 换弹中不能开火
        if (gun.isReloading(stack)) return;

        int ammo = gun.getAmmo(stack);

        // 空弹：自动开始换弹（✅ 这里会根据大招状态选择 10/30 ticks）
        if (ammo <= 0) {
            gun.startReload(player, stack);
            serverLevel.playSound(null, player.blockPosition(),
                    SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.5f, 1.8f);
            return;
        }

        // 扣子弹
        int newAmmo = ammo - 1;
        gun.setAmmo(stack, newAmmo);

        // 真正射击（✅ 自瞄在 shoot 内部）
        gun.shoot(serverLevel, player);

        // 打空后立刻自动换弹（✅ 大招期间 0.5s）
        if (newAmmo <= 0 && !gun.isReloading(stack)) {
            gun.startReload(player, stack);
            serverLevel.playSound(null, player.blockPosition(),
                    SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.5f, 1.8f);
        }
    }

    /**
     * 由服务器调用：如果玩家主手拿着枪，就开始换弹（有计时）
     * ✅ 大招期间换弹为 0.5s
     */
    public static void serverReloadIfHoldingGun(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem gun)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        // 已在换弹就不重复触发
        if (gun.isReloading(stack)) return;

        int ammo = gun.getAmmo(stack);
        if (ammo >= MAG_SIZE) return; // 满弹不换

        gun.startReload(player, stack);

        serverLevel.playSound(null, player.blockPosition(),
                SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.5f, 1.8f);
    }

    // ---------------- Shooting logic (hitscan) ----------------

    /**
     * ✅ 大招自瞄：必须介入“射击方向”的确定
     */
    private void shoot(ServerLevel level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        // 默认：有散布
        Vec3 finalLook = look.add(
                (player.getRandom().nextDouble() - 0.5) * SPREAD,
                (player.getRandom().nextDouble() - 0.5) * SPREAD,
                (player.getRandom().nextDouble() - 0.5) * SPREAD
        ).normalize();

        // Tactical Visor：改写射击方向指向锁定目标
        if (player instanceof ServerPlayer sp && Soldier76Ultimate.isUltActive(sp)) {
            LivingEntity target = Soldier76VisorTargeting.findBestTarget(sp);
            if (target != null && target.isAlive()) {
                Vec3 aimPos = target.position().add(0.0, target.getBbHeight() * 0.6, 0.0);
                Vec3 to = aimPos.subtract(eye);
                if (to.lengthSqr() > 1.0e-6) {
                    finalLook = to.normalize();
                }
            }
        }

        LivingEntity hit = RaycastUtil.raycastLivingEntity(player, eye, finalLook, RANGE);
        if (hit != null && hit != player) {
            hit.hurt(level.damageSources().playerAttack(player), DAMAGE);
        }

        level.playSound(null, player.blockPosition(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
