package com.jam8ee.overcraft.hero.soldier76;

import com.jam8ee.overcraft.core.gameplay.cooldown.CooldownManager;
import com.jam8ee.overcraft.core.gameplay.hero.HeroId;
import com.jam8ee.overcraft.core.gameplay.hero.HeroManager;
import com.jam8ee.overcraft.core.network.ModNetwork;
import com.jam8ee.overcraft.core.network.s2c.SyncCooldownsS2CPacket;
import com.jam8ee.overcraft.hero.soldier76.ability.BioticFieldLogic;
import com.jam8ee.overcraft.hero.soldier76.ability.HelixRocketsLogic;
import com.jam8ee.overcraft.hero.soldier76.data.Soldier76Tuning;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.PacketDistributor;

public final class Soldier76Abilities {
    private Soldier76Abilities() {}

    // ✅ 兼容：如果其他类引用了 Soldier76Abilities.E_COOLDOWN_TICKS 也不会炸
    public static final int E_COOLDOWN_TICKS = Soldier76Tuning.E_COOLDOWN_TICKS;
    public static final int HELIX_COOLDOWN_TICKS = Soldier76Tuning.HELIX_COOLDOWN_TICKS;

    public static void tryCastBioticField(ServerPlayer player) {
        if (!HeroManager.requireHero(player, HeroId.SOLDIER76)) return;

        if (BioticFieldLogic.tryCast(player)) {
            syncCooldownsToClient(player);
        }
    }

    public static void tryCastHelixRockets(ServerPlayer player) {
        if (!HeroManager.requireHero(player, HeroId.SOLDIER76)) return;

        if (HelixRocketsLogic.tryCast(player)) {
            syncCooldownsToClient(player);
        }
    }

    public static void tickCooldowns(ServerPlayer player) {
        final boolean[] shouldSync = {false};

        CooldownManager.tickDownAndMaybeNotify(
                player, Soldier76Tuning.NBT_E_CD, 5, v -> shouldSync[0] = true
        );

        CooldownManager.tickDownAndMaybeNotify(
                player, Soldier76Tuning.NBT_HELIX_CD, 5, v -> shouldSync[0] = true
        );

        if (shouldSync[0]) {
            syncCooldownsToClient(player);
        }
    }

    private static void syncCooldownsToClient(ServerPlayer player) {
        int e = CooldownManager.get(player, Soldier76Tuning.NBT_E_CD);
        int h = CooldownManager.get(player, Soldier76Tuning.NBT_HELIX_CD);

        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncCooldownsS2CPacket(e, h)
        );
    }
}
