package com.jam8ee.overcraft.hero.soldier76;

import com.jam8ee.overcraft.hero.soldier76.ability.TacticalVisorLogic;
import net.minecraft.server.level.ServerPlayer;

/**
 * 兼容层：保留你原来对外暴露的方法名，内部转发给 TacticalVisorLogic。
 * 这样你不用全项目到处改调用点。
 */
public final class Soldier76Ultimate {
    private Soldier76Ultimate() {}

    public static void tryActivate(ServerPlayer player) {
        TacticalVisorLogic.start(player);
    }

    /** 每 tick 调用：处理大招计时、被动充能、锁定粒子提示 */
    public static void serverTick(ServerPlayer player) {
        TacticalVisorLogic.tick(player);
    }

    /** 伤害充能入口（Helix 也走这里） */
    public static void addChargeFromDamage(ServerPlayer player, float damage) {
        TacticalVisorLogic.addDamageCharge(player, damage);
    }

    public static boolean isUltActive(ServerPlayer player) {
        return TacticalVisorLogic.isUltActive(player);
    }

    public static int getUltActive(ServerPlayer player) {
        return TacticalVisorLogic.getUltActive(player);
    }

    public static int getUltChargePercent(ServerPlayer player) {
        return TacticalVisorLogic.getUltChargePercent(player);
    }

    public static void syncToClient(ServerPlayer player) {
        TacticalVisorLogic.syncToClient(player);
    }
}
