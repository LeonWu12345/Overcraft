package com.jam8ee.overcraft.hero.soldier76;

import com.jam8ee.overcraft.core.gameplay.cooldown.CooldownManager;
import com.jam8ee.overcraft.core.gameplay.hero.HeroId;
import com.jam8ee.overcraft.core.gameplay.hero.HeroManager;
import com.jam8ee.overcraft.core.gameplay.weapon.GunItem;
import com.jam8ee.overcraft.core.network.ModNetwork;
import com.jam8ee.overcraft.core.network.s2c.SyncCooldownsS2CPacket;
import com.jam8ee.overcraft.core.registry.ModEntities;
import com.jam8ee.overcraft.entity.effect.BioticFieldEntity;
import com.jam8ee.overcraft.entity.projectile.HelixRocketEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.PacketDistributor;

public final class Soldier76Abilities {
    private Soldier76Abilities() {}

    // ===== E (Biotic Field) =====
    public static final int E_COOLDOWN_TICKS = 300; // 15s
    private static final String NBT_E_CD = "overcraft_s76_e_cd";

    // ===== Right Click (Helix Rocket) =====
    public static final int HELIX_COOLDOWN_TICKS = 120; // 6s（可按版本微调）
    private static final String NBT_HELIX_CD = "overcraft_s76_helix_cd";

    public static void tryCastBioticField(ServerPlayer player) {
        if (!HeroManager.requireHero(player, HeroId.SOLDIER76)) return;

        ItemStack main = player.getMainHandItem();
        if (!(main.getItem() instanceof GunItem)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        int cd = CooldownManager.get(player, NBT_E_CD);
        if (cd > 0) {
            level.playSound(null, player.blockPosition(),
                    SoundEvents.COMPARATOR_CLICK, SoundSource.PLAYERS, 0.6f, 0.8f);
            return;
        }

        BioticFieldEntity field = ModEntities.BIOTIC_FIELD.get().create(level);
        if (field == null) return;

        field.setOwner(player);
        field.moveTo(player.getX(), player.getY(), player.getZ(), 0.0f, 0.0f);
        level.addFreshEntity(field);

        CooldownManager.set(player, NBT_E_CD, E_COOLDOWN_TICKS);
        syncCooldownsToClient(player);

        level.playSound(null, player.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.8f);
    }

    public static void tryCastHelixRockets(ServerPlayer player) {
        if (!HeroManager.requireHero(player, HeroId.SOLDIER76)) return;

        ItemStack main = player.getMainHandItem();
        if (!(main.getItem() instanceof GunItem)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        int cd = CooldownManager.get(player, NBT_HELIX_CD);
        if (cd > 0) {
            level.playSound(null, player.blockPosition(),
                    SoundEvents.COMPARATOR_CLICK, SoundSource.PLAYERS, 0.6f, 0.9f);
            return;
        }

        var eye = player.getEyePosition();
        var look = player.getLookAngle().normalize();

        // 起点往前推一点，避免贴脸自爆
        var start = eye.add(look.scale(1.0));

        // ✅ 现在只发射 1 发
        HelixRocketEntity.spawn(level, player, start, look);

        CooldownManager.set(player, NBT_HELIX_CD, HELIX_COOLDOWN_TICKS);
        syncCooldownsToClient(player);

        level.playSound(null, player.blockPosition(),
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 0.9f, 1.2f);
    }

    public static void tickCooldowns(ServerPlayer player) {
        final boolean[] shouldSync = {false};

        // E CD（每 5 tick 同步一次，归零也同步）
        CooldownManager.tickDownAndMaybeNotify(player, NBT_E_CD, 5, v -> shouldSync[0] = true);

        // Helix CD（每 5 tick 同步一次，归零也同步）
        CooldownManager.tickDownAndMaybeNotify(player, NBT_HELIX_CD, 5, v -> shouldSync[0] = true);

        if (shouldSync[0]) {
            syncCooldownsToClient(player);
        }
    }

    private static void syncCooldownsToClient(ServerPlayer player) {
        int e = CooldownManager.get(player, NBT_E_CD);
        int h = CooldownManager.get(player, NBT_HELIX_CD);

        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncCooldownsS2CPacket(e, h)
        );
    }
}
