package com.jam8ee.overcraft.network;

import com.jam8ee.overcraft.client.ClientCooldownCache;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncSoldier76ECooldownS2CPacket {
    private final int cooldownTicks;

    public SyncSoldier76ECooldownS2CPacket(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    public static void encode(SyncSoldier76ECooldownS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.cooldownTicks);
    }

    public static SyncSoldier76ECooldownS2CPacket decode(FriendlyByteBuf buf) {
        return new SyncSoldier76ECooldownS2CPacket(buf.readInt());
    }

    public static void handle(SyncSoldier76ECooldownS2CPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                ClientCooldownCache.setSoldier76ECooldownTicks(msg.cooldownTicks);
            }
        }); ctx.setPacketHandled(true);
    }
}
