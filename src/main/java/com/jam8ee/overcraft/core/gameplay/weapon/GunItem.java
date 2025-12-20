package com.jam8ee.overcraft.core.gameplay.weapon;

import com.jam8ee.overcraft.core.util.RaycastUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public class GunItem extends Item {

    // ====== Weapon Tunables ======
    public static final int MAG_SIZE = 25;
    public static final int FIRE_INTERVAL_TICKS = 2;  // 5 发/秒
    public static final int RELOAD_TICKS = 30;        // 1.5 秒（20 ticks = 1s）
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

    public void startReload(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putInt(NBT_RELOAD_REMAIN, RELOAD_TICKS);
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
     */
    public static void serverShootIfHoldingGun(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem gun)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        // 换弹中不能开火
        if (gun.isReloading(stack)) return;

        int ammo = gun.getAmmo(stack);
        if (ammo <= 0) {
            // 空弹提示音（可选）
            serverLevel.playSound(null, player.blockPosition(),
                    SoundEvents.COMPARATOR_CLICK, SoundSource.PLAYERS, 0.6f, 1.2f);
            return;
        }

        // 扣子弹
        gun.setAmmo(stack, ammo - 1);

        // 真正射击
        gun.shoot(serverLevel, player);
    }

    /**
     * 由服务器调用：如果玩家主手拿着枪，就开始换弹（有计时）
     */
    public static void serverReloadIfHoldingGun(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem gun)) return;
        if (!(player.level() instanceof ServerLevel serverLevel)) return;

        // 已在换弹就不重复触发
        if (gun.isReloading(stack)) return;

        int ammo = gun.getAmmo(stack);
        if (ammo >= MAG_SIZE) return; // 满弹不换

        gun.startReload(stack);

        serverLevel.playSound(null, player.blockPosition(),
                SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.5f, 1.8f);
    }

    // ---------------- Shooting logic (hitscan) ----------------

    private void shoot(ServerLevel level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle();

        Vec3 spreadLook = look.add(
                (player.getRandom().nextDouble() - 0.5) * SPREAD,
                (player.getRandom().nextDouble() - 0.5) * SPREAD,
                (player.getRandom().nextDouble() - 0.5) * SPREAD
        ).normalize();

        LivingEntity hit = RaycastUtil.raycastLivingEntity(player, eye, spreadLook, RANGE);
        if (hit != null && hit != player) {
            hit.hurt(level.damageSources().playerAttack(player), DAMAGE);
        }

        level.playSound(null, player.blockPosition(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
