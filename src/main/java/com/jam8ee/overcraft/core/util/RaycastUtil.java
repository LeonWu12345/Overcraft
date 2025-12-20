package com.jam8ee.overcraft.core.util;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public final class RaycastUtil {
    private RaycastUtil() {}

    /**
     * 从玩家视角发射一条射线，优先命中最近的 LivingEntity（同时会考虑方块遮挡）。
     *
     * @param player          玩家
     * @param eye             眼睛位置
     * @param look            方向（应当是 normalized）
     * @param range           射程
     * @param boxInflate      用于扩大“可能命中实体”的搜索盒
     * @param entityInflate   用于扩大实体 hitbox（让命中更手感）
     */
    public static LivingEntity raycastLivingEntity(Player player,
                                                   Vec3 eye,
                                                   Vec3 look,
                                                   double range,
                                                   double boxInflate,
                                                   double entityInflate) {

        Vec3 end = eye.add(look.scale(range));

        // 先做方块裁剪（保证实体在方块后面时不会被打到）
        HitResult blockHit = player.level().clip(new ClipContext(
                eye, end,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                player
        ));
        Vec3 actualEnd = (blockHit.getType() == HitResult.Type.MISS) ? end : blockHit.getLocation();

        // 在射线路径附近找实体
        AABB searchBox = player.getBoundingBox().expandTowards(look.scale(range)).inflate(boxInflate);
        List<Entity> entities = player.level().getEntities(player, searchBox,
                e -> e instanceof LivingEntity && e.isPickable());

        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;

        for (Entity e : entities) {
            AABB eb = e.getBoundingBox().inflate(entityInflate);
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

    /**
     * 常用默认参数版本：boxInflate=1.0, entityInflate=0.3
     */
    public static LivingEntity raycastLivingEntity(Player player, Vec3 eye, Vec3 look, double range) {
        return raycastLivingEntity(player, eye, look, range, 1.0, 0.3);
    }
}
