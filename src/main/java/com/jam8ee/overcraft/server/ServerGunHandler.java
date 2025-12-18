package com.jam8ee.overcraft.server;

import com.jam8ee.overcraft.item.GunItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public final class ServerGunHandler {
    private ServerGunHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        if (!(event.player instanceof ServerPlayer player)) return;

        Soldier76Abilities.tickCooldowns(player);
        Soldier76Ultimate.serverTick(player);

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem gun)) return;

        if (gun.isReloading(stack)) {
            gun.tickReload(stack);
        }
    }
}
