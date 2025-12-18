package com.jam8ee.overcraft.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

public class BioticFieldEntity extends Entity {

    // ===== Tunables (Soldier:76 E) =====
    public static final float RADIUS = 5.0f;
    public static final int DURATION_TICKS = 100; // 5s
    private static final int HEAL_INTERVAL_TICKS = 10; // 每 0.5s 治疗一次
    private static final float HEAL_AMOUNT = 1.0f;     // 0.5 heart

    private int lifeTicks = 0;
    private int healTick = 0;

    private UUID ownerUuid;

    public BioticFieldEntity(EntityType<? extends BioticFieldEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public void setOwner(Player owner) {
        this.ownerUuid = owner.getUUID();
    }

    @Override
    protected void defineSynchedData() {
        // no synced data needed for now
    }

    @Override
    public void tick() {
        super.tick();

        // 保持贴地一点（视觉更舒服）
        if (!level().isClientSide) {
            setPos(getX(), getY(), getZ());
        }

        lifeTicks++;
        if (lifeTicks >= DURATION_TICKS) {
            discard();
            return;
        }

        if (level() instanceof ServerLevel serverLevel) {
            // 粒子（服务器发给客户端）
            serverLevel.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    getX(), getY() + 0.1, getZ(),
                    6,
                    0.4, 0.05, 0.4,
                    0.0
            );

            // 周期性治疗
            healTick++;
            if (healTick >= HEAL_INTERVAL_TICKS) {
                healTick = 0;
                healPlayersInRadius(serverLevel);
            }
        }
    }

    private void healPlayersInRadius(ServerLevel level) {
        AABB box = new AABB(
                getX() - RADIUS, getY() - 2.0, getZ() - RADIUS,
                getX() + RADIUS, getY() + 2.0, getZ() + RADIUS
        );

        List<Player> players = level.getEntitiesOfClass(Player.class, box, p -> p.isAlive());

        for (Player p : players) {
            // 圆形半径过滤（更像“立场”）
            double dx = p.getX() - getX();
            double dz = p.getZ() - getZ();
            if ((dx * dx + dz * dz) > (RADIUS * RADIUS)) continue;

            float hp = p.getHealth();
            float max = p.getMaxHealth();
            if (hp >= max) continue;

            p.setHealth(Mth.clamp(hp + HEAL_AMOUNT, 0.0f, max));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        lifeTicks = tag.getInt("LifeTicks");
        healTick = tag.getInt("HealTick");
        if (tag.hasUUID("Owner")) ownerUuid = tag.getUUID("Owner");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("LifeTicks", lifeTicks);
        tag.putInt("HealTick", healTick);
        if (ownerUuid != null) tag.putUUID("Owner", ownerUuid);
    }
}
