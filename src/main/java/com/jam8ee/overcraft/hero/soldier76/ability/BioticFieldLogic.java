package com.jam8ee.overcraft.hero.soldier76.ability;

import com.jam8ee.overcraft.core.gameplay.cooldown.CooldownManager;
import com.jam8ee.overcraft.core.gameplay.weapon.GunItem;
import com.jam8ee.overcraft.core.registry.ModEntities;
import com.jam8ee.overcraft.entity.effect.BioticFieldEntity;
import com.jam8ee.overcraft.hero.soldier76.data.Soldier76Tuning;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

public final class BioticFieldLogic {
    private BioticFieldLogic() {}

    /**
     * @return true = 成功施放并进入冷却；false = 未施放（CD中/没拿枪/环境不对）
     */
    public static boolean tryCast(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (!(main.getItem() instanceof GunItem)) return false;
        if (!(player.level() instanceof ServerLevel level)) return false;

        int cd = CooldownManager.get(player, Soldier76Tuning.NBT_E_CD);
        if (cd > 0) {
            level.playSound(null, player.blockPosition(),
                    SoundEvents.COMPARATOR_CLICK, SoundSource.PLAYERS, 0.6f, 0.8f);
            return false;
        }

        BioticFieldEntity field = ModEntities.BIOTIC_FIELD.get().create(level);
        if (field == null) return false;

        field.setOwner(player);
        field.moveTo(player.getX(), player.getY(), player.getZ(), 0.0f, 0.0f);
        level.addFreshEntity(field);

        CooldownManager.set(player, Soldier76Tuning.NBT_E_CD, Soldier76Tuning.E_COOLDOWN_TICKS);

        level.playSound(null, player.blockPosition(),
                SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.8f, 1.8f);

        return true;
    }
}
