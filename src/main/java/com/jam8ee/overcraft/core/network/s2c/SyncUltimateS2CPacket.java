package com.jam8ee.overcraft.core.network.s2c;

import com.jam8ee.overcraft.client.hud.cache.ClientUltimateCache;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncUltimateS2CPacket {

    private final int charge;          // 0..100
    private final int activeTicks;      // 0..duration

    public SyncUltimateS2CPacket(int charge, int activeTicks) {
        this.charge = charge;
        this.activeTicks = activeTicks;
    }

    public static void encode(SyncUltimateS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.charge);
        buf.writeInt(msg.activeTicks);
    }

    public static SyncUltimateS2CPacket decode(FriendlyByteBuf buf) {
        return new SyncUltimateS2CPacket(buf.readInt(), buf.readInt());
    }

    public static void handle(SyncUltimateS2CPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().player == null) return;
            ClientUltimateCache.setUltChargePercent(msg.charge);
            ClientUltimateCache.setUltActiveTicks(msg.activeTicks);
        });
        ctx.setPacketHandled(true);
    }
}

