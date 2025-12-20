package com.jam8ee.overcraft.entity.projectile;

import com.jam8ee.overcraft.core.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.entity.projectile.ProjectileUtil;

import java.util.UUID;

public class HelixRocketEntity extends Projectile {

    /**
     * 现在只发射 1 发，但希望“总体伤害≈之前 3 发的效果”
     * Minecraft 爆炸伤害不是线性，所以这里用更高的 power 做近似。
     */
    public static final float EXPLOSION_POWER = 2.6f;

    public static final int MAX_LIFE_TICKS = 60;
    public static final double SPEED = 1.6;

    private int life;
    private UUID ownerUuid;

    public HelixRocketEntity(EntityType<? extends HelixRocketEntity> type, Level level) {
        super(type, level);
        this.noPhysics = false;
    }

    public void setOwnerEntity(LivingEntity owner) {
        this.setOwner(owner);
        this.ownerUuid = owner.getUUID();
    }

    @Override
    protected void defineSynchedData() {
        // no synced data
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide) {
            level().addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0, 0, 0);
            return;
        }

        life++;
        if (life > MAX_LIFE_TICKS) {
            discard();
            return;
        }

        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS) {
            onHit(hit);
        }

        Vec3 v = getDeltaMovement();
        setPos(getX() + v.x, getY() + v.y, getZ() + v.z);
        setDeltaMovement(v.scale(0.99));
    }

    @Override
    protected void onHit(HitResult result) {
        if (!(level() instanceof ServerLevel serverLevel)) return;

        Entity owner = getOwner();
        serverLevel.explode(
                owner,
                getX(), getY(), getZ(),
                EXPLOSION_POWER,
                false,
                ExplosionInteraction.NONE
        );

        serverLevel.sendParticles(ParticleTypes.EXPLOSION, getX(), getY(), getZ(),
                1, 0.0, 0.0, 0.0, 0.0);

        discard();
    }

    @Override
    protected boolean canHitEntity(Entity entity) {
        Entity owner = getOwner();
        if (entity == owner) return false;
        return super.canHitEntity(entity);
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Life", life);
        if (ownerUuid != null) tag.putUUID("Owner", ownerUuid);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        life = tag.getInt("Life");
        if (tag.hasUUID("Owner")) ownerUuid = tag.getUUID("Owner");
    }

    public static HelixRocketEntity spawn(ServerLevel level, LivingEntity owner, Vec3 startPos, Vec3 direction) {
        HelixRocketEntity rocket = ModEntities.HELIX_ROCKET.get().create(level);
        if (rocket == null) return null;

        rocket.setOwnerEntity(owner);
        rocket.setPos(startPos.x, startPos.y, startPos.z);

        Vec3 vel = direction.normalize().scale(SPEED);
        rocket.setDeltaMovement(vel);

        rocket.setYRot((float) (Math.atan2(vel.z, vel.x) * (180.0 / Math.PI)) - 90.0f);
        rocket.setXRot((float) (-(Math.atan2(vel.y, Math.sqrt(vel.x * vel.x + vel.z * vel.z)) * (180.0 / Math.PI))));

        level.addFreshEntity(rocket);
        return rocket;
    }
}
