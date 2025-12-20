package com.jam8ee.overcraft.core.registry;

import com.jam8ee.overcraft.OvercraftMod;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

/**
 * 统一管理所有 KeyMapping（客户端专用）。
 *
 * 注意：
 * - KeyMapping 的注册必须发生在客户端（Dist.CLIENT）
 * - 这里只负责“声明 + 注册”，实际按键触发逻辑仍由 client/input 处理
 */
@Mod.EventBusSubscriber(modid = OvercraftMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ModKeyMappings {
    private ModKeyMappings() {}

    public static final String CATEGORY = "key.categories.overcraft";

    // 这些是“技能键/动作键”，左键开火一般是输入事件处理（不是 KeyMapping）
    public static final KeyMapping RELOAD = new KeyMapping(
            "key.overcraft.reload",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
    );

    public static final KeyMapping ABILITY_E = new KeyMapping(
            "key.overcraft.ability_e",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
            CATEGORY
    );

    public static final KeyMapping ABILITY_RMB = new KeyMapping(
            "key.overcraft.ability_rmb",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_RIGHT,
            CATEGORY
    );

    public static final KeyMapping ULTIMATE = new KeyMapping(
            "key.overcraft.ultimate",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Q,
            CATEGORY
    );

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(RELOAD);
        event.register(ABILITY_E);
        event.register(ABILITY_RMB);
        event.register(ULTIMATE);
    }
}
