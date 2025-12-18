package com.jam8ee.overcraft.network;

import com.jam8ee.overcraft.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class ModNetwork {
    private ModNetwork() {}

    private static final String PROTOCOL_VERSION = "1";

    @SuppressWarnings("removal")
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(ExampleMod.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    private static int id = 0;

    public static void register() {
        CHANNEL.messageBuilder(FireGunC2SPacket.class, id++)
                .encoder(FireGunC2SPacket::encode)
                .decoder(FireGunC2SPacket::decode)
                .consumerMainThread(FireGunC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(ReloadGunC2SPacket.class, id++)
                .encoder(ReloadGunC2SPacket::encode)
                .decoder(ReloadGunC2SPacket::decode)
                .consumerMainThread(ReloadGunC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(BioticFieldC2SPacket.class, id++)
                .encoder(BioticFieldC2SPacket::encode)
                .decoder(BioticFieldC2SPacket::decode)
                .consumerMainThread(BioticFieldC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(SyncSoldier76ECooldownS2CPacket.class, id++)
                .encoder(SyncSoldier76ECooldownS2CPacket::encode)
                .decoder(SyncSoldier76ECooldownS2CPacket::decode)
                .consumerMainThread(SyncSoldier76ECooldownS2CPacket::handle)
                .add();

        CHANNEL.messageBuilder(HelixRocketsC2SPacket.class, id++)
                .encoder(HelixRocketsC2SPacket::encode)
                .decoder(HelixRocketsC2SPacket::decode)
                .consumerMainThread(HelixRocketsC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(SyncSoldier76HelixCooldownS2CPacket.class, id++)
                .encoder(SyncSoldier76HelixCooldownS2CPacket::encode)
                .decoder(SyncSoldier76HelixCooldownS2CPacket::decode)
                .consumerMainThread(SyncSoldier76HelixCooldownS2CPacket::handle)
                .add();

        // Ultimate: Q activate + sync UI
        CHANNEL.messageBuilder(TacticalVisorC2SPacket.class, id++)
                .encoder(TacticalVisorC2SPacket::encode)
                .decoder(TacticalVisorC2SPacket::decode)
                .consumerMainThread(TacticalVisorC2SPacket::handle)
                .add();

        CHANNEL.messageBuilder(SyncUltimateS2CPacket.class, id++)
                .encoder(SyncUltimateS2CPacket::encode)
                .decoder(SyncUltimateS2CPacket::decode)
                .consumerMainThread(SyncUltimateS2CPacket::handle)
                .add();
    }
}
