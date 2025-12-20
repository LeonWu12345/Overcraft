package com.jam8ee.overcraft.hero.soldier76;

import com.jam8ee.overcraft.OvercraftMod;
import com.jam8ee.overcraft.hero.soldier76.ability.TacticalVisorLogic;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OvercraftMod.MODID)
public final class Soldier76UltimateChargeEvents {
    private Soldier76UltimateChargeEvents() {}

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;

        float dmg = event.getAmount();
        if (dmg <= 0.0f) return;

        // 开大期间不充能（逻辑在 TacticalVisorLogic 内部）
        TacticalVisorLogic.addDamageCharge(player, dmg);
    }
}
