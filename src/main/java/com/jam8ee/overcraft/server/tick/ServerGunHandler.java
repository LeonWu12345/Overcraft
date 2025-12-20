package com.jam8ee.overcraft.server.tick;

import com.jam8ee.overcraft.hero.soldier76.Soldier76Abilities;
import com.jam8ee.overcraft.hero.soldier76.Soldier76Ultimate;
import com.jam8ee.overcraft.core.gameplay.weapon.GunItem;
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
