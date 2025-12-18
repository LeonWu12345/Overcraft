package com.jam8ee.overcraft.server;

import com.jam8ee.overcraft.entity.BioticFieldEntity;
import com.jam8ee.overcraft.entity.HelixRocketEntity;
import com.jam8ee.overcraft.entity.ModEntities;
import com.jam8ee.overcraft.item.GunItem;
import com.jam8ee.overcraft.network.ModNetwork;
import com.jam8ee.overcraft.network.SyncSoldier76ECooldownS2CPacket;
import com.jam8ee.overcraft.network.SyncSoldier76HelixCooldownS2CPacket;
import net.minecraft.nbt.CompoundTag;
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
        ItemStack main = player.getMainHandItem();
        if (!(main.getItem() instanceof GunItem)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        int cd = getCooldown(player, NBT_E_CD);
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

        setCooldown(player, NBT_E_CD, E_COOLDOWN_TICKS);
        syncECooldownToClient(player, E_COOLDOWN_TICKS);

        level.playSound(null, player.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.8f);
    }

    public static void tryCastHelixRockets(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (!(main.getItem() instanceof GunItem)) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        int cd = getCooldown(player, NBT_HELIX_CD);
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

        setCooldown(player, NBT_HELIX_CD, HELIX_COOLDOWN_TICKS);
        syncHelixCooldownToClient(player, HELIX_COOLDOWN_TICKS);

        level.playSound(null, player.blockPosition(),
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 0.9f, 1.2f);
    }

    public static void tickCooldowns(ServerPlayer player) {
        // E CD（并同步 HUD）
        int e = getCooldown(player, NBT_E_CD);
        if (e > 0) {
            int next = e - 1;
            setCooldown(player, NBT_E_CD, next);
            if (next == 0 || (next % 5 == 0)) syncECooldownToClient(player, next);
        }

        // Helix CD（并同步 HUD）
        int h = getCooldown(player, NBT_HELIX_CD);
        if (h > 0) {
            int next = h - 1;
            setCooldown(player, NBT_HELIX_CD, next);
            if (next == 0 || (next % 5 == 0)) syncHelixCooldownToClient(player, next);
        }
    }

    private static int getCooldown(ServerPlayer player, String key) {
        CompoundTag tag = player.getPersistentData();
        return tag.getInt(key);
    }

    private static void setCooldown(ServerPlayer player, String key, int value) {
        player.getPersistentData().putInt(key, Math.max(0, value));
    }

    private static void syncECooldownToClient(ServerPlayer player, int cdTicks) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncSoldier76ECooldownS2CPacket(cdTicks)
        );
    }

    private static void syncHelixCooldownToClient(ServerPlayer player, int cdTicks) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new SyncSoldier76HelixCooldownS2CPacket(cdTicks)
        );
    }
}
