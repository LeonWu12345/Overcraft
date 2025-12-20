package com.jam8ee.overcraft;

import com.jam8ee.overcraft.core.registry.ModEntities;
import com.jam8ee.overcraft.core.registry.ModItems;
import com.jam8ee.overcraft.core.network.ModNetwork;
import com.jam8ee.overcraft.server.tick.ServerGunHandler;
import com.jam8ee.overcraft.core.registry.ModSounds;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(OvercraftMod.MODID)
public class OvercraftMod {
    public static final String MODID = "overcraft";

    @SuppressWarnings("removal")
    public OvercraftMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModNetwork.register();
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModSounds.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(ServerGunHandler.class);
    }
}
