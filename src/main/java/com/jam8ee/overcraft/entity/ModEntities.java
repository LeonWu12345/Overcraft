package com.jam8ee.overcraft.entity;

import com.jam8ee.overcraft.ExampleMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModEntities {
    private ModEntities() {}

    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, ExampleMod.MODID);

    public static final RegistryObject<EntityType<BioticFieldEntity>> BIOTIC_FIELD =
            ENTITIES.register("biotic_field", () ->
                    EntityType.Builder.<BioticFieldEntity>of(BioticFieldEntity::new, MobCategory.MISC)
                            .sized(1.0f, 0.25f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("biotic_field")
            );

    public static final RegistryObject<EntityType<HelixRocketEntity>> HELIX_ROCKET =
            ENTITIES.register("helix_rocket", () ->
                    EntityType.Builder.<HelixRocketEntity>of(HelixRocketEntity::new, MobCategory.MISC)
                            .sized(0.25f, 0.25f)
                            .clientTrackingRange(64)
                            .updateInterval(1)
                            .build("helix_rocket")
            );

    public static void register(IEventBus bus) {
        ENTITIES.register(bus);
    }
}
