package com.jam8ee.overcraft.core.registry;

import com.jam8ee.overcraft.OvercraftMod;
import com.jam8ee.overcraft.core.gameplay.weapon.GunItem;
import com.jam8ee.overcraft.core.gameplay.weapon.HammerItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, OvercraftMod.MODID);

    public static final RegistryObject<Item> TRAINING_RIFLE =
            ITEMS.register("training_rifle", () -> new GunItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> REINHARDT_HAMMER = ITEMS.register(
            "reinhardt_hammer",
            () -> new HammerItem(new Item.Properties().stacksTo(1))
    );

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
