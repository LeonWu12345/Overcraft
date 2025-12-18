package com.jam8ee.overcraft.network;

import com.jam8ee.overcraft.client.ClientCooldownCache;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class SyncSoldier76HelixCooldownS2CPacket {

    private final int cooldownTicks;

    public SyncSoldier76HelixCooldownS2CPacket(int cooldownTicks) {
        this.cooldownTicks = cooldownTicks;
    }

    public static void encode(SyncSoldier76HelixCooldownS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.cooldownTicks);
    }

    public static SyncSoldier76HelixCooldownS2CPacket decode(FriendlyByteBuf buf) {
        return new SyncSoldier76HelixCooldownS2CPacket(buf.readInt());
    }

    public static void handle(SyncSoldier76HelixCooldownS2CPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().player != null) {
                ClientCooldownCache.setSoldier76HelixCooldownTicks(msg.cooldownTicks);
            }
        });
        ctx.setPacketHandled(true);
    }
}
