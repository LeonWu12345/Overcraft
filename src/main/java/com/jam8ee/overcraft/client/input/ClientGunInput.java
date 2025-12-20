package com.jam8ee.overcraft.client.input;

import com.jam8ee.overcraft.OvercraftMod;
import com.jam8ee.overcraft.client.hud.cache.ClientUltimateCache;
import com.jam8ee.overcraft.client.hud.cache.ClientCooldownCache;
import com.jam8ee.overcraft.core.network.ModNetwork;
import com.jam8ee.overcraft.core.network.c2s.*;
import com.jam8ee.overcraft.core.gameplay.weapon.GunItem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = OvercraftMod.MODID, value = Dist.CLIENT)
public class ClientGunInput {

    private static int fireCooldownTicks = 0;
    private static int helixLocalThrottleTicks = 0;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.screen != null) return;

        Player player = mc.player;
        ItemStack stack = player.getMainHandItem();

        if (fireCooldownTicks > 0) fireCooldownTicks--;
        if (helixLocalThrottleTicks > 0) helixLocalThrottleTicks--;

        // R 换弹
        if (ClientKeyMappings.RELOAD != null && ClientKeyMappings.RELOAD.consumeClick()) {
            if (stack.getItem() instanceof GunItem) {
                ModNetwork.CHANNEL.sendToServer(new ReloadGunC2SPacket());
            }
        }

        // E 生物力场
        if (ClientKeyMappings.ABILITY_E != null && ClientKeyMappings.ABILITY_E.consumeClick()) {
            if (stack.getItem() instanceof GunItem) {
                ModNetwork.CHANNEL.sendToServer(new BioticFieldC2SPacket());
            }
        }

        // Q 大招（充满才能发包）
        if (ClientKeyMappings.ULTIMATE_Q != null && ClientKeyMappings.ULTIMATE_Q.consumeClick()) {
            if (stack.getItem() instanceof GunItem) {
                if (ClientUltimateCache.isUltReady()) {
                    ModNetwork.CHANNEL.sendToServer(new TacticalVisorC2SPacket());
                }
            }
        }

        // 右键螺旋飞弹（不变）
        boolean useDown = mc.options.keyUse.isDown();
        if (useDown && helixLocalThrottleTicks <= 0) {
            if (stack.getItem() instanceof GunItem) {
                if (ClientCooldownCache.getSoldier76HelixCooldownTicks() <= 0) {
                    ModNetwork.CHANNEL.sendToServer(new HelixRocketsC2SPacket());
                }
                helixLocalThrottleTicks = 8;
            }
        }

        // 左键自动射击
        boolean attackDown = mc.options.keyAttack.isDown();
        if (!attackDown) return;
        if (fireCooldownTicks > 0) return;

        if (!(stack.getItem() instanceof GunItem gun)) return;

        int ammo = gun.getAmmo(stack);
        if (gun.isReloading(stack)) return;
        if (ammo <= 0) return;

        ModNetwork.CHANNEL.sendToServer(new FireGunC2SPacket());
        fireCooldownTicks = GunItem.FIRE_INTERVAL_TICKS;

        applyRecoil(mc);
    }

    private static void applyRecoil(Minecraft mc) {
        if (mc.player == null) return;

        float baseKick = 0.6f;
        float rand = (mc.player.getRandom().nextFloat() - 0.5f) * 0.3f;

        float newPitch = mc.player.getXRot() - (baseKick + rand);
        mc.player.setXRot(Mth.clamp(newPitch, -90f, 90f));
    }
}
