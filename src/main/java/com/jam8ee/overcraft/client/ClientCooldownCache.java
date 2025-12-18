package com.jam8ee.overcraft.client;

public final class ClientCooldownCache {
    private ClientCooldownCache() {}

    // Soldier:76 E (Biotic Field)
    private static int soldier76ECooldownTicks = 0;

    // Soldier:76 Right Click (Helix Rockets)
    private static int soldier76HelixCooldownTicks = 0;

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
}
