package net.zuperz.stellar_sorcery.gaze;

import net.minecraft.resources.ResourceLocation;
import net.zuperz.stellar_sorcery.data.gaze.GazeModifierDefinition;
import net.zuperz.stellar_sorcery.data.gaze.GazeRegistry;

import java.util.List;
import java.util.Locale;

public final class GazeModifierEngine {

    private GazeModifierEngine() {}

    public static void apply(GazeMutableStats stats, List<ResourceLocation> modifiers) {
        if (modifiers == null || modifiers.isEmpty()) return;

        for (ResourceLocation rl : modifiers) {
            GazeModifierDefinition def = GazeRegistry.getModifier(rl);
            if (def == null) continue;

            String effect = def.effect().toLowerCase(Locale.ROOT);
            double amount = def.amount();

            switch (effect) {
                case "increase_radius" -> stats.actionRadius += amount;
                case "increase_range", "range_boost" -> stats.targetRange += amount;
                case "increase_particles" -> stats.particleCount += (int) Math.round(amount);
                case "mana_efficiency", "mana_multiplier" -> stats.manaCost = (int) Math.round(stats.manaCost * amount);
                case "cooldown_multiplier", "cooldown_reduction" -> stats.cooldown = (int) Math.round(stats.cooldown * amount);
                case "particle_multiplier" -> stats.particleCount = (int) Math.round(stats.particleCount * amount);
                case "health_efficiency", "health_multiplier" -> stats.healthCost = (int) Math.round(stats.healthCost * amount);
            }
        }

        stats.clampNonNegative();
    }
}
