package com.jam8ee.overcraft.server;

import com.jam8ee.overcraft.ExampleMod;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID)
public final class Soldier76UltimateChargeEvents {
    private Soldier76UltimateChargeEvents() {}

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        // 充能逻辑已迁移到 GunItem.shoot()（命中并造成伤害时充能）
        // 这样可以确保“只有枪伤害计入 76 大招”，也避免事件法导致重复/误判。
    }
}
