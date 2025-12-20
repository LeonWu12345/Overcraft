package com.jam8ee.overcraft.hero.soldier76.data;

/**
 * Soldier:76 数值集中管理（冷却 / 大招 / NBT keys）
 */
public final class Soldier76Tuning {
    private Soldier76Tuning() {}

    // ===== Abilities cooldowns =====
    public static final int E_COOLDOWN_TICKS = 300;        // 15s
    public static final int HELIX_COOLDOWN_TICKS = 120;    // 6s

    // ===== Ability NBT keys (player persistent) =====
    public static final String NBT_E_CD = "overcraft_s76_e_cd";
    public static final String NBT_HELIX_CD = "overcraft_s76_helix_cd";

    // ===== Tactical Visor (Ultimate) =====
    public static final int ULT_DURATION_TICKS = 160; // 8s

    // 220 点伤害 = 100%
    // 被动每秒 +0.1%
    public static final int FULL_CHARGE_UNITS = 220_000;
    public static final int UNITS_PER_DAMAGE = 1_000;            // 1 damage -> +1000 units
    public static final int PASSIVE_UNITS_PER_SECOND = 220;      // 0.1%/sec -> 220 units/sec
    public static final int UNITS_PER_PERCENT = 2_200;           // 220000 -> 100%

    // ===== Ultimate NBT keys =====
    public static final String NBT_ULT_UNITS  = "overcraft_s76_ult_units";   // int 0..220000
    public static final String NBT_ULT_ACTIVE = "overcraft_s76_ult_active";  // ticks remaining
    public static final String NBT_PASSIVE_T  = "overcraft_ult_passive_t";   // 0..19
    public static final String NBT_LOCK_FX_T  = "overcraft_ult_lock_fx_t";   // throttle particles
}
