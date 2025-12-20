package com.jam8ee.overcraft.client.hud.cache;

public final class ClientCooldownCache {
    private ClientCooldownCache() {}

    // Soldier:76 E (Biotic Field)
    private static int soldier76ECooldownTicks = 0;

    // Soldier:76 Right Click (Helix Rockets)
    private static int soldier76HelixCooldownTicks = 0;

    // ===== Existing API (keep for compatibility) =====

    public static void setSoldier76ECooldownTicks(int ticks) {
        soldier76ECooldownTicks = Math.max(0, ticks);
    }

    public static int getSoldier76ECooldownTicks() {
        return soldier76ECooldownTicks;
    }

    public static void setSoldier76HelixCooldownTicks(int ticks) {
        soldier76HelixCooldownTicks = Math.max(0, ticks);
    }

    public static int getSoldier76HelixCooldownTicks() {
        return soldier76HelixCooldownTicks;
    }

    // ===== New API for SyncCooldownsS2CPacket =====

    /**
     * 由服务器同步包调用：一次性刷新所有冷却（以服务器为准）。
     * eTicks = 生物力场冷却；helixTicks = 螺旋飞弹冷却
     */
    public static void applyFromServer(int eTicks, int helixTicks) {
        soldier76ECooldownTicks = Math.max(0, eTicks);
        soldier76HelixCooldownTicks = Math.max(0, helixTicks);
    }

    /**
     * 可选：客户端每 tick 自己递减，让 HUD 数字/进度更平滑。
     * 仍然以服务器同步为准（服务器发来时会覆盖这里的值）。
     */
    public static void clientTick() {
        if (soldier76ECooldownTicks > 0) soldier76ECooldownTicks--;
        if (soldier76HelixCooldownTicks > 0) soldier76HelixCooldownTicks--;
    }
}
