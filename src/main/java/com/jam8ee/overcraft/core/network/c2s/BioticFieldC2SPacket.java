package com.jam8ee.overcraft.core.network.c2s;

import com.jam8ee.overcraft.hero.soldier76.Soldier76Abilities;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class BioticFieldC2SPacket {

    public BioticFieldC2SPacket() {}

    public static void encode(BioticFieldC2SPacket msg, FriendlyByteBuf buf) {
        // no fields
    }

    public static BioticFieldC2SPacket decode(FriendlyByteBuf buf) {
        return new BioticFieldC2SPacket();
    }

    public static void handle(BioticFieldC2SPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ServerPlayer player = ctx.getSender();
        if (player == null) return;

        ctx.enqueueWork(() -> Soldier76Abilities.tryCastBioticField(player));
        ctx.setPacketHandled(true);
    }
}
