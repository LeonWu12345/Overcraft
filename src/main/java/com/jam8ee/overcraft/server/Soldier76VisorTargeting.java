package com.jam8ee.overcraft.server;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class Soldier76VisorTargeting {
    private Soldier76VisorTargeting() {}

    // 你之后想调手感就改这些
    public static final double RANGE = 40.0;
    public static final double FOV_DEG = 50.0; // 视野锥角度（越大越“吸”）
    private static final double COS_HALF_FOV = Math.cos(Math.toRadians(FOV_DEG * 0.5));

    public static LivingEntity findBestTarget(ServerPlayer player) {
        if (!(player.level() instanceof ServerLevel level)) return null;

        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();

        AABB box = new AABB(
                player.getX() - RANGE, player.getY() - RANGE, player.getZ() - RANGE,
                player.getX() + RANGE, player.getY() + RANGE, player.getZ() + RANGE
        );

        List<LivingEntity> candidates = level.getEntitiesOfClass(LivingEntity.class, box, e ->
                e.isAlive()
                        && e != player
                        && !e.isRemoved()
                        && !e.isInvulnerable()
        );

        LivingEntity best = null;
        double bestScore = -1e18;

        for (LivingEntity e : candidates) {
            // 目标瞄点：胸口偏上（更像 OW）
            Vec3 aimPos = e.position().add(0.0, e.getBbHeight() * 0.6, 0.0);

            Vec3 to = aimPos.subtract(eye);
            double dist = to.length();
            if (dist < 0.001 || dist > RANGE) continue;

            Vec3 dir = to.scale(1.0 / dist);

            // 视野锥过滤
            double dot = look.dot(dir);
            if (dot < COS_HALF_FOV) continue;

            // 视线可见性（不穿墙）
            if (!player.hasLineOfSight(e)) continue;

            // 评分：越接近准星（dot 越大）越优先；越近越优先
            // 你之后要更“锁头”可以把 dot 权重再加大
            double score = (dot * 2.0) - (dist / RANGE);

            if (score > bestScore) {
                bestScore = score;
                best = e;
            }
        }

        return best;
    }

    public static float yawTo(Vec3 from, Vec3 to) {
        Vec3 d = to.subtract(from);
        // Minecraft yaw: +Z 朝南，atan2(x, z)
        return (float) (Mth.atan2(d.x, d.z) * (180.0 / Math.PI));
    }

    public static float pitchTo(Vec3 from, Vec3 to) {
        Vec3 d = to.subtract(from);
        double xz = Math.sqrt(d.x * d.x + d.z * d.z);
        return (float) (-(Mth.atan2(d.y, xz) * (180.0 / Math.PI)));
    }
}
