package net.zuperz.stellar_sorcery.gaze;

import net.minecraft.world.entity.player.Player;

public final class GazeCostHandler {

    private GazeCostHandler() {}

    public enum CostResult {
        OK,
        NOT_ENOUGH_MANA,
        NOT_ENOUGH_XP,
        NOT_ENOUGH_HEALTH
    }

    public static CostResult check(Player player, GazeMutableStats stats) {
        if (stats.manaCost > 0 && player.experienceLevel < stats.manaCost) return CostResult.NOT_ENOUGH_MANA;
        if (stats.xpLevels > 0 && player.experienceLevel < stats.xpLevels) return CostResult.NOT_ENOUGH_XP;
        if (stats.healthCost > 0 && player.getHealth() <= stats.healthCost) return CostResult.NOT_ENOUGH_HEALTH;
        return CostResult.OK;
    }

    public static boolean canPay(Player player, GazeMutableStats stats) {
        return check(player, stats) == CostResult.OK;
    }

    public static void pay(Player player, GazeMutableStats stats) {
        if (stats.manaCost > 0) {
            player.giveExperienceLevels(-stats.manaCost);
        }
        if (stats.xpLevels > 0) {
            player.giveExperienceLevels(-stats.xpLevels);
        }
        if (stats.healthCost > 0) {
            player.hurt(player.damageSources().magic(), stats.healthCost);
        }
        if (stats.exhaustion > 0.0f) {
            player.getFoodData().addExhaustion(stats.exhaustion);
        }
    }
}
