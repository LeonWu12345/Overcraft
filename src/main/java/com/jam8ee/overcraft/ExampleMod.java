package com.jam8ee.overcraft;

import com.jam8ee.overcraft.entity.ModEntities;
import com.jam8ee.overcraft.item.ModItems;
import com.jam8ee.overcraft.network.ModNetwork;
import com.jam8ee.overcraft.server.ServerGunHandler;
import com.jam8ee.overcraft.sound.ModSounds;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(ExampleMod.MODID)
public class ExampleMod {
    public static final String MODID = "overcraft";

    @SuppressWarnings("removal")
    public ExampleMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModNetwork.register();
        ModItems.register(modEventBus);
        ModEntities.register(modEventBus);
        ModSounds.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(ServerGunHandler.class);
    }
}
