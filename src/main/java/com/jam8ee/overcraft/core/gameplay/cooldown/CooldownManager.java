package com.jam8ee.overcraft.core.gameplay.cooldown;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

import java.util.function.IntConsumer;

/**
 * 通用冷却管理器（存储在 player.getPersistentData()）
 *
 * 设计目标：
 * - 不绑任何英雄/技能
 * - 不负责发网络包（发包由调用方决定）
 * - 提供 tickDownAndMaybeNotify 帮你实现 “每 N tick 同步一次 + 归零同步”
 */
public final class CooldownManager {
    private CooldownManager() {}

    public static int get(ServerPlayer player, String key) {
        CompoundTag tag = player.getPersistentData();
        return Math.max(0, tag.getInt(key));
    }

    public static void set(ServerPlayer player, String key, int ticks) {
        player.getPersistentData().putInt(key, Math.max(0, ticks));
    }

    public static boolean isReady(ServerPlayer player, String key) {
        return get(player, key) <= 0;
    }

    /**
     * 如果冷却为 0，则设置为 cooldownTicks 并返回 true；否则不做事返回 false。
     */
    public static boolean tryStart(ServerPlayer player, String key, int cooldownTicks) {
        if (!isReady(player, key)) return false;
        set(player, key, cooldownTicks);
        return true;
    }

    /**
     * 递减冷却 1 tick（如果 >0）；返回新的冷却值。
     */
    public static int tickDown(ServerPlayer player, String key) {
        int cur = get(player, key);
        if (cur <= 0) return 0;
        int next = cur - 1;
        set(player, key, next);
        return next;
    }

    /**
     * tickDown + “每 syncEveryTicks tick 同步一次，归零也同步”
     *
     * @param onNotify 当需要同步时回调（传入最新冷却值）
     */
    public static int tickDownAndMaybeNotify(ServerPlayer player, String key, int syncEveryTicks, IntConsumer onNotify) {
        int cur = get(player, key);
        if (cur <= 0) return 0;

        int next = cur - 1;
        set(player, key, next);

        if (onNotify != null) {
            if (next == 0) {
                onNotify.accept(0);
            } else if (syncEveryTicks > 0 && (next % syncEveryTicks == 0)) {
                onNotify.accept(next);
            }
        }

        return next;
    }
}
