package com.jam8ee.overcraft.client;

import com.jam8ee.overcraft.ExampleMod;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientKeyMappings {

    public static final String CATEGORY = "key.categories.overcraft";

    public static KeyMapping RELOAD;
    public static KeyMapping ABILITY_E;
    public static KeyMapping ULTIMATE_Q;

    @SubscribeEvent
    public static void registerKeys(RegisterKeyMappingsEvent event) {
        RELOAD = new KeyMapping("key.overcraft.reload", GLFW.GLFW_KEY_R, CATEGORY);
        event.register(RELOAD);

        ABILITY_E = new KeyMapping("key.overcraft.ability_e", GLFW.GLFW_KEY_E, CATEGORY);
        event.register(ABILITY_E);

        // 默认 Q（会跟“丢物品”冲突，你可以在 Controls 里改键）
        ULTIMATE_Q = new KeyMapping("key.overcraft.ultimate", GLFW.GLFW_KEY_Q, CATEGORY);
        event.register(ULTIMATE_Q);
    }
}
