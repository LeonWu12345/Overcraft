package com.jam8ee.overcraft.core.gameplay.weapon;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.phys.Vec3;

/**
 * Reinhardt 的基础锤子（第一版：近战武器）
 * - 先用 SwordItem 做最小可玩
 * - 后面再加入：范围横扫、击退更强、右键举盾等
 */
public class HammerItem extends SwordItem {

    public HammerItem(Properties props) {
        // Tiers.IRON 只是临时；你后面想改强度再调
        // damage bonus=7, attack speed=-3.2 会更像“重锤”
        super(Tiers.IRON, 7, -3.2f, props);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        boolean ok = super.hurtEnemy(stack, target, attacker);

        // 额外一点击退，让它更像锤子
        Vec3 pushDir = target.position().subtract(attacker.position());
        if (pushDir.lengthSqr() > 0.0001) {
            pushDir = pushDir.normalize();
            double knock = 0.7; // 后续可调
            target.push(pushDir.x * knock, 0.1, pushDir.z * knock);
        }

        // 消耗耐久：先不消耗（更像 OW 武器），所以不调用 stack.hurtAndBreak
        // 若你想要耐久系统，告诉我，我们再做开关。
        if (attacker instanceof Player) {
            // no-op
        }

        return ok;
    }
}
