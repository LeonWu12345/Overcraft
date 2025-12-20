package com.jam8ee.overcraft.client.hud.cache;

/**
 * 可复用的“当前英雄的大招 UI 缓存”。
 * 以后其他英雄也用同一套 HUD：只要服务器同步 charge% + activeTicks 即可。
 */
public final class ClientUltimateCache {
    private ClientUltimateCache() {}

    private static int ultChargePercent = 0; // 0..100
    private static int ultActiveTicks = 0;

    public static void setUltChargePercent(int percent) {
        ultChargePercent = Math.max(0, Math.min(100, percent));
    }

    public static int getUltChargePercent() {
        return ultChargePercent;
    }

    public static void setUltActiveTicks(int ticks) {
        ultActiveTicks = Math.max(0, ticks);
    }

    public static int getUltActiveTicks() {
        return ultActiveTicks;
    }

    public static boolean isUltActive() {
        return ultActiveTicks > 0;
    }

    public static boolean isUltReady() {
        return ultChargePercent >= 100 && ultActiveTicks <= 0;
    }
}
