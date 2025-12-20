package com.jam8ee.overcraft.core.network.c2s;

import com.jam8ee.overcraft.core.gameplay.weapon.GunItem;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ReloadGunC2SPacket {

    public ReloadGunC2SPacket() {}

    public static void encode(ReloadGunC2SPacket msg, FriendlyByteBuf buf) {
        // no fields
    }

    public static ReloadGunC2SPacket decode(FriendlyByteBuf buf) {
        return new ReloadGunC2SPacket();
    }

    public static void handle(ReloadGunC2SPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ServerPlayer player = ctx.getSender();
        if (player == null) return;

        ctx.enqueueWork(() -> GunItem.serverReloadIfHoldingGun(player));
        ctx.setPacketHandled(true);
    }
}
