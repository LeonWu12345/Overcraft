package com.jam8ee.overcraft.hero.soldier76.ability;

import com.jam8ee.overcraft.core.gameplay.cooldown.CooldownManager;
import com.jam8ee.overcraft.core.gameplay.weapon.GunItem;
import com.jam8ee.overcraft.entity.projectile.HelixRocketEntity;
import com.jam8ee.overcraft.hero.soldier76.data.Soldier76Tuning;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

public final class HelixRocketsLogic {
    private HelixRocketsLogic() {}

    /**
     * @return true = 成功施放并进入冷却；false = 未施放（CD中/没拿枪/环境不对）
     */
    public static boolean tryCast(ServerPlayer player) {
        ItemStack main = player.getMainHandItem();
        if (!(main.getItem() instanceof GunItem)) return false;
        if (!(player.level() instanceof ServerLevel level)) return false;

        int cd = CooldownManager.get(player, Soldier76Tuning.NBT_HELIX_CD);
        if (cd > 0) {
            level.playSound(null, player.blockPosition(),
                    SoundEvents.COMPARATOR_CLICK, SoundSource.PLAYERS, 0.6f, 0.9f);
            return false;
        }

        var eye = player.getEyePosition();
        var look = player.getLookAngle().normalize();

        // 起点往前推一点，避免贴脸自爆
        var start = eye.add(look.scale(1.0));

        // 现在只发射 1 发（伤害你已经在火箭实体里调过等效）
        HelixRocketEntity.spawn(level, player, start, look);

        CooldownManager.set(player, Soldier76Tuning.NBT_HELIX_CD, Soldier76Tuning.HELIX_COOLDOWN_TICKS);

        level.playSound(null, player.blockPosition(),
                SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 0.9f, 1.2f);

        return true;
    }
}
