package com.jam8ee.overcraft.core.gameplay.hero;

import com.jam8ee.overcraft.OvercraftMod;
import com.jam8ee.overcraft.core.registry.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * 规则 A（你选的）：
 * - 切英雄时清空所有 Overcraft 物品（namespace == "overcraft"）
 * - 强制发放该英雄默认武器到 hotbar 1号槽（index 0），并切到该槽位
 */
public final class HeroLoadoutApplier {
    private HeroLoadoutApplier() {}

    public static void switchHero(ServerPlayer player, HeroId hero) {
        // 1) 写入 HeroState
        HeroManager.set(player, hero);

        // 2) 清空所有 Overcraft 物品
        clearAllOvercraftItems(player);

        // 3) 发放默认武器到 1号槽并切换到该槽
        ItemStack weapon = defaultWeaponStack(hero);
        giveToHotbarSlot1(player, weapon);
    }

    private static ItemStack defaultWeaponStack(HeroId hero) {
        return switch (hero) {
            case SOLDIER76 -> new ItemStack(ModItems.TRAINING_RIFLE.get());
            case REINHARDT -> new ItemStack(ModItems.REINHARDT_HAMMER.get());
        };
    }

    private static void giveToHotbarSlot1(ServerPlayer player, ItemStack stack) {
        // hotbar 1号槽 index 0
        player.getInventory().setItem(0, stack);
        player.getInventory().selected = 0;

        // 强制同步到客户端（多人/单人都稳）
        player.inventoryMenu.broadcastChanges();
    }

    private static void clearAllOvercraftItems(ServerPlayer player) {
        // 主背包 + hotbar（同一个容器）
        int size = player.getInventory().getContainerSize();
        for (int i = 0; i < size; i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (!s.isEmpty() && isOvercraftItem(s.getItem())) {
                player.getInventory().setItem(i, ItemStack.EMPTY);
            }
        }

        // 护甲
        for (int i = 0; i < player.getInventory().armor.size(); i++) {
            ItemStack s = player.getInventory().armor.get(i);
            if (!s.isEmpty() && isOvercraftItem(s.getItem())) {
                player.getInventory().armor.set(i, ItemStack.EMPTY);
            }
        }

        // 副手
        for (int i = 0; i < player.getInventory().offhand.size(); i++) {
            ItemStack s = player.getInventory().offhand.get(i);
            if (!s.isEmpty() && isOvercraftItem(s.getItem())) {
                player.getInventory().offhand.set(i, ItemStack.EMPTY);
            }
        }
    }

    private static boolean isOvercraftItem(Item item) {
        var key = BuiltInRegistries.ITEM.getKey(item);
        if (key == null) return false;
        return Objects.equals(key.getNamespace(), OvercraftMod.MODID);
    }
}
