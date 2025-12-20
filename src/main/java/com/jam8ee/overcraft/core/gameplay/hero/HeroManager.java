package com.jam8ee.overcraft.core.gameplay.hero;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

/**
 * HeroManager：统一入口（get/set/校验）
 */
public final class HeroManager {
    private HeroManager() {}

    public static HeroId get(ServerPlayer player) {
        return HeroState.getHero(player);
    }

    public static void set(ServerPlayer player, HeroId hero) {
        HeroState.setHero(player, hero);
    }

    public static boolean is(ServerPlayer player, HeroId hero) {
        return get(player) == hero;
    }

    /**
     * 如果玩家不是指定英雄，则返回 false，并播放一个“不可用”提示音。
     */
    public static boolean requireHero(ServerPlayer player, HeroId hero) {
        if (is(player, hero)) return true;

        if (player.level() != null) {
            player.level().playSound(
                    null,
                    player.blockPosition(),
                    SoundEvents.COMPARATOR_CLICK,
                    SoundSource.PLAYERS,
                    0.6f,
                    0.8f
            );
        }
        return false;
    }
}
