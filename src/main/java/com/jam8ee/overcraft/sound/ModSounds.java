package com.jam8ee.overcraft.sound;

import com.jam8ee.overcraft.ExampleMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModSounds {
    private ModSounds() {}

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ExampleMod.MODID);

    // overcraft:tactical_visor_activate
    public static final RegistryObject<SoundEvent> TACTICAL_VISOR_ACTIVATE =
            SOUND_EVENTS.register("tactical_visor_activate",
                    () -> SoundEvent.createVariableRangeEvent(
                            new ResourceLocation(ExampleMod.MODID, "tactical_visor_activate")
                    ));

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
