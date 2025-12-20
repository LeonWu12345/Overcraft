package com.jam8ee.overcraft.core.network.c2s;

import com.jam8ee.overcraft.hero.soldier76.Soldier76Abilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class HelixRocketsC2SPacket {

    public HelixRocketsC2SPacket() {}

    public static void encode(HelixRocketsC2SPacket msg, FriendlyByteBuf buf) {
        // no fields
    }

    public static HelixRocketsC2SPacket decode(FriendlyByteBuf buf) {
        return new HelixRocketsC2SPacket();
    }

    public static void handle(HelixRocketsC2SPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ServerPlayer player = ctx.getSender();
        if (player == null) return;

        ctx.enqueueWork(() -> Soldier76Abilities.tryCastHelixRockets(player));
        ctx.setPacketHandled(true);
    }
}
