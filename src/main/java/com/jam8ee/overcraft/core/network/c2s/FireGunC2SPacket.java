package com.jam8ee.overcraft.core.network.c2s;

import com.jam8ee.overcraft.core.gameplay.weapon.GunItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class FireGunC2SPacket {

    public FireGunC2SPacket() {}

    public static void encode(FireGunC2SPacket msg, FriendlyByteBuf buf) {
        // no fields
    }

    public static FireGunC2SPacket decode(FriendlyByteBuf buf) {
        return new FireGunC2SPacket();
    }

    public static void handle(FireGunC2SPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ServerPlayer player = ctx.getSender();
        if (player == null) return;

        ctx.enqueueWork(() -> GunItem.serverShootIfHoldingGun(player));
        ctx.setPacketHandled(true);
    }
}
