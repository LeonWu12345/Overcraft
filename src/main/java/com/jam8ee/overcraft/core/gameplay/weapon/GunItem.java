package com.jam8ee.overcraft.core.gameplay.weapon;

import com.jam8ee.overcraft.hero.soldier76.Soldier76Ultimate;
import com.jam8ee.overcraft.hero.soldier76.Soldier76VisorTargeting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class GunItem extends Item {

    // ====== Weapon Tunables ======
    public static final int MAG_SIZE = 30;
    public static final int FIRE_INTERVAL_TICKS = 2;
    public static final int RELOAD_TICKS = 30;        // 1.5s
    public static final int ULT_RELOAD_TICKS = 10;    // 0.5s
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
        stack.getOrCreateTag().putInt(
                NBT_AMMO,
                Math.max(0, Math.min(MAG_SIZE, ammo))
        );
    }

    public boolean isReloading(ItemStack stack) {
        return stack.getOrCreateTag().getInt(NBT_RELOAD_REMAIN) > 0;
    }

    public int getReloadRemain(ItemStack stack) {
        return stack.getOrCreateTag().getInt(NBT_RELOAD_REMAIN);
    }

    /** 原有接口：普通换弹 */
    public void startReload(ItemStack stack) {
        startReload(stack, RELOAD_TICKS);
    }

    /** ✅ 新增接口：可指定换弹时间（供大招使用） */
    public void startReload(ItemStack stack, int ticks) {
        stack.getOrCreateTag().putInt(
                NBT_RELOAD_REMAIN,
                Math.max(0, ticks)
        );
    }

    public void tickReload(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        int remain = tag.getInt(NBT_RELOAD_REMAIN);
        if (remain <= 0) return;

        remain--;
        tag.putInt(NBT_RELOAD_REMAIN, remain);

        if (remain == 0) {
            tag.putInt(NBT_AMMO, MAG_SIZE);
        }
    }

    // ---------------- Server entry points ----------------

    public static void serverShootIfHoldingGun(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem gun)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        // 换弹中不能开火
        if (gun.isReloading(stack)) return;

        int ammo = gun.getAmmo(stack);

        // ✅ 空弹：自动换弹
        if (ammo <= 0) {
            gun.startReload(stack, getReloadTicksForPlayer(player));
            level.playSound(null, player.blockPosition(),
                    SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.5f, 1.8f);
            return;
        }

        gun.setAmmo(stack, ammo - 1);
        gun.shoot(level, player);

        // ✅ 打空弹匣后立刻自动换弹
        if (gun.getAmmo(stack) <= 0 && !gun.isReloading(stack)) {
            gun.startReload(stack, getReloadTicksForPlayer(player));
            level.playSound(null, player.blockPosition(),
                    SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.5f, 1.8f);
        }
    }

    public static void serverReloadIfHoldingGun(Player player) {
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem gun)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        if (gun.isReloading(stack)) return;
        if (gun.getAmmo(stack) >= MAG_SIZE) return;

        gun.startReload(stack, getReloadTicksForPlayer(player));
        level.playSound(null, player.blockPosition(),
                SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.5f, 1.8f);
    }

    private static int getReloadTicksForPlayer(Player player) {
        if (player instanceof ServerPlayer sp && Soldier76Ultimate.isUltActive(sp)) {
            return ULT_RELOAD_TICKS;
        }
        return RELOAD_TICKS;
    }

    // ---------------- Shooting logic (hitscan) ----------------

    private void shoot(ServerLevel level, Player player) {
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();

        boolean visor = (player instanceof ServerPlayer sp)
                && Soldier76Ultimate.isUltActive(sp);

        Vec3 finalDir = look;
        boolean noSpread = false;

        if (visor && player instanceof ServerPlayer sp) {
            LivingEntity target = Soldier76VisorTargeting.findBestTarget(sp);
            if (target != null) {
                Vec3 aim = target.position()
                        .add(0.0, target.getBbHeight() * 0.6, 0.0);
                finalDir = aim.subtract(eye).normalize();
                noSpread = true;
            }
        }

        Vec3 shootDir = noSpread ? finalDir : applySpread(player, finalDir);

        LivingEntity hit = raycastLivingEntity(player, eye, shootDir, RANGE);
        if (hit != null && hit != player) {
            boolean didHurt = hit.hurt(
                    level.damageSources().playerAttack(player),
                    DAMAGE
            );

//            if (didHurt && player instanceof ServerPlayer sp) {
//                Soldier76Ultimate.addChargeFromDamage(sp, DAMAGE);
//            }
        }

        level.playSound(null, player.blockPosition(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0f, 1.0f);
    }

    private Vec3 applySpread(Player player, Vec3 dir) {
        return dir.add(
                (player.getRandom().nextDouble() - 0.5) * SPREAD,
                (player.getRandom().nextDouble() - 0.5) * SPREAD,
                (player.getRandom().nextDouble() - 0.5) * SPREAD
        ).normalize();
    }

    private LivingEntity raycastLivingEntity(Player player, Vec3 eye, Vec3 look, double range) {
        Vec3 end = eye.add(look.scale(range));

        HitResult blockHit = player.level().clip(new ClipContext(
                eye, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));
        Vec3 actualEnd = blockHit.getType() == HitResult.Type.MISS
                ? end
                : blockHit.getLocation();

        AABB box = player.getBoundingBox()
                .expandTowards(look.scale(range))
                .inflate(1.0);

        List<Entity> entities = player.level().getEntities(
                player, box,
                e -> e instanceof LivingEntity && e.isPickable()
        );

        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;

        for (Entity e : entities) {
            AABB eb = e.getBoundingBox().inflate(0.3);
            var opt = eb.clip(eye, actualEnd);
            if (opt.isPresent()) {
                double d = eye.distanceTo(opt.get());
                if (d < bestDist) {
                    bestDist = d;
                    best = (LivingEntity) e;
                }
            }
        }
        return best;
    }
}
