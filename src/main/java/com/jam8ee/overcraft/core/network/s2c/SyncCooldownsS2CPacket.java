package com.jam8ee.overcraft.core.network.s2c;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.lang.reflect.Method;
import java.util.function.Supplier;

/**
 * 通用冷却同步包（S2C）
 * 目前先覆盖 Soldier76 的两条冷却：
 *  - E 生物力场
 *  - 右键螺旋飞弹
 *
 * 注意：
 * - 这里不能直接引用 client-only 类（例如 ClientCooldownCache），
 *   否则 dedicated server 可能会因为类加载失败崩溃。
 * - 因此使用反射在客户端侧调用 ClientCooldownCache.applyFromServer(...)
 */
public class SyncCooldownsS2CPacket {

    private final int soldier76ECooldownTicks;
    private final int soldier76HelixCooldownTicks;

    public SyncCooldownsS2CPacket(int soldier76ECooldownTicks, int soldier76HelixCooldownTicks) {
        this.soldier76ECooldownTicks = Math.max(0, soldier76ECooldownTicks);
        this.soldier76HelixCooldownTicks = Math.max(0, soldier76HelixCooldownTicks);
    }

    public static void encode(SyncCooldownsS2CPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.soldier76ECooldownTicks);
        buf.writeVarInt(msg.soldier76HelixCooldownTicks);
    }

    public static SyncCooldownsS2CPacket decode(FriendlyByteBuf buf) {
        int e = buf.readVarInt();
        int helix = buf.readVarInt();
        return new SyncCooldownsS2CPacket(e, helix);
    }

    public static void handle(SyncCooldownsS2CPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            // 只会在客户端收到 S2C 包；这里用反射避免服务端类加载 client-only 类
            try {
                Class<?> cacheCls = Class.forName("com.jam8ee.overcraft.client.hud.cache.ClientCooldownCache");
                Method m = cacheCls.getMethod("applyFromServer", int.class, int.class);
                m.invoke(null, msg.soldier76ECooldownTicks, msg.soldier76HelixCooldownTicks);
            } catch (Throwable ignored) {
                // 如果客户端类名/方法名被改了，这里会静默失败（方便你重构时先跑起来）
            }
        });
        ctx.setPacketHandled(true);
    }
}
