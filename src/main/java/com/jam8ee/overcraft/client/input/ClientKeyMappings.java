package com.jam8ee.overcraft.client.input;

import com.jam8ee.overcraft.core.registry.ModKeyMappings;
import net.minecraft.client.KeyMapping;

/**
 * 兼容层：保留旧字段名，实际 KeyMapping 由 ModKeyMappings 统一定义与注册。
 *
 * 注意：
 * - 这个类不再负责 RegisterKeyMappingsEvent
 * - ClientGunInput 继续引用这里即可（无需大改）
 */
public final class ClientKeyMappings {

    private ClientKeyMappings() {}

    // 旧字段名保留，指向新的集中管理
    public static final KeyMapping RELOAD = ModKeyMappings.RELOAD;
    public static final KeyMapping ABILITY_E = ModKeyMappings.ABILITY_E;

    // 你以前叫 ULTIMATE_Q，这里继续保留不改名，避免你其他代码要动
    public static final KeyMapping ULTIMATE_Q = ModKeyMappings.ULTIMATE;
}
