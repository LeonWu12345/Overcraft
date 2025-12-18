package com.jam8ee.overcraft.client;

import com.jam8ee.overcraft.ExampleMod;
import com.jam8ee.overcraft.item.GunItem;
import com.jam8ee.overcraft.server.Soldier76Abilities;
import com.jam8ee.overcraft.server.Soldier76Ultimate;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = ExampleMod.MODID, value = Dist.CLIENT)
public final class HudOverlay {

    private HudOverlay() {}

    @SubscribeEvent
    public static void onRenderOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) return;
        if (mc.options.renderDebug) return;

        // ===== 1) 准星旁边显示大招剩余时间（你要的：准心偏右）=====
        if (event.getOverlay() == VanillaGuiOverlay.CROSSHAIR.type()) {
            renderUltNearCrosshair(event.getGuiGraphics(), mc);
        }

        // ===== 2) 原来的右下角枪 HUD（热键栏后绘制）=====
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem gun)) return;

        int ammo = gun.getAmmo(stack);
        boolean reloading = gun.isReloading(stack);
        int reloadRemain = gun.getReloadRemain(stack);

        int eCd = ClientCooldownCache.getSoldier76ECooldownTicks();
        int helixCd = ClientCooldownCache.getSoldier76HelixCooldownTicks();

        GuiGraphics gg = event.getGuiGraphics();
        Font font = mc.font;

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        int xRight = sw - 8;
        int yBase  = sh - 52;

        // Ammo
        String ammoText = "Ammo: " + ammo + "/" + GunItem.MAG_SIZE;
        int ammoW = font.width(ammoText);
        gg.drawString(font, ammoText, xRight - ammoW, yBase, 0xFFFFFF, true);

        // Reload
        if (reloading) {
            float sec = reloadRemain / 20.0f;
            String reloadText = String.format("Reloading: %.1fs", sec);
            int rw = font.width(reloadText);
            gg.drawString(font, reloadText, xRight - rw, yBase - 10, 0xFFFFFF, true);

            int barW = 80, barH = 6;
            int barX = xRight - barW;
            int barY = yBase - 22;

            float progress = 1.0f - (reloadRemain / (float) GunItem.RELOAD_TICKS);
            progress = Mth.clamp(progress, 0.0f, 1.0f);

            drawBar(gg, barX, barY, barW, barH, progress);
        }

        // E cooldown
        int eTextY = yBase - 34;
        drawCooldownLine(gg, font, xRight, eTextY, "E", eCd, Soldier76Abilities.E_COOLDOWN_TICKS);

        // RMB cooldown
        int hTextY = eTextY - 24;
        drawCooldownLine(gg, font, xRight, hTextY, "RMB", helixCd, Soldier76Abilities.HELIX_COOLDOWN_TICKS);

        // Ultimate charge（可复用）
        int uTextY = hTextY - 24;
        drawUltCharge(gg, font, xRight, uTextY);
    }

    private static void renderUltNearCrosshair(GuiGraphics gg, Minecraft mc) {
        int active = ClientUltimateCache.getUltActiveTicks();
        if (active <= 0) return;

        int sw = mc.getWindow().getGuiScaledWidth();
        int sh = mc.getWindow().getGuiScaledHeight();

        int cx = sw / 2;
        int cy = sh / 2;

        String t = String.format("%.1fs", active / 20.0f);
        // “准心偏右”
        gg.drawString(mc.font, t, cx + 10, cy - 4, 0xFFFFFF, true);
    }

    private static void drawUltCharge(GuiGraphics gg, Font font, int xRight, int textY) {
        int percent = ClientUltimateCache.getUltChargePercent();
        boolean active = ClientUltimateCache.isUltActive();

        String label = active ? "ULT: ACTIVE" : ("ULT: " + percent + "%");
        int w = font.width(label);
        gg.drawString(font, label, xRight - w, textY, 0xFFFFFF, true);

        // 充能条：只在非 ACTIVE 时显示更直观
        if (!active) {
            int barW = 80, barH = 6;
            int barX = xRight - barW;
            int barY = textY - 12;

            float progress = Mth.clamp(percent / 100.0f, 0.0f, 1.0f);
            drawBar(gg, barX, barY, barW, barH, progress);
        }
    }

    private static void drawCooldownLine(GuiGraphics gg, Font font, int xRight, int textY,
                                         String label, int cdTicks, int totalTicks) {
        String text = (cdTicks <= 0) ? (label + ": Ready") : String.format("%s: %.1fs", label, cdTicks / 20.0f);
        int w = font.width(text);
        gg.drawString(font, text, xRight - w, textY, 0xFFFFFF, true);

        if (cdTicks > 0) {
            int barW = 80, barH = 6;
            int barX = xRight - barW;
            int barY = textY - 12;

            float progress = 1.0f - (cdTicks / (float) totalTicks);
            progress = Mth.clamp(progress, 0.0f, 1.0f);

            drawBar(gg, barX, barY, barW, barH, progress);
        }
    }

    private static void drawBar(GuiGraphics gg, int x, int y, int w, int h, float progress) {
        gg.fill(x, y, x + w, y + h, 0xAA000000);
        gg.fill(x, y, x + (int) (w * progress), y + h, 0xAAFFFFFF);
        gg.fill(x, y, x + w, y + 1, 0xCCFFFFFF);
        gg.fill(x, y + h - 1, x + w, y + h, 0xCCFFFFFF);
        gg.fill(x, y, x + 1, y + h, 0xCCFFFFFF);
        gg.fill(x + w - 1, y, x + w, y + h, 0xCCFFFFFF);
    }
}
