package com.jam8ee.overcraft.core.gameplay.hero;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/**
 * 把玩家当前英雄存到 persistent NBT。
 * 默认：SOLDIER76（因为你现在只有 76 完整可玩）
 */
public final class HeroState {
    private HeroState() {}

    private static final String NBT_HERO_ID = "overcraft_current_hero";

    public static HeroId getHero(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.contains(NBT_HERO_ID)) {
            tag.putString(NBT_HERO_ID, HeroId.SOLDIER76.id());
            return HeroId.SOLDIER76;
        }
        return HeroId.fromString(tag.getString(NBT_HERO_ID));
    }

    public static void setHero(ServerPlayer player, HeroId hero) {
        player.getPersistentData().putString(NBT_HERO_ID, hero.id());
    }
}
