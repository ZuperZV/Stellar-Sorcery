package net.zuperz.stellar_sorcery.gaze;

import com.google.gson.JsonObject;
import net.zuperz.stellar_sorcery.data.gaze.GazeDefinition;
import net.zuperz.stellar_sorcery.data.gaze.GazeJsonHelper;

public final class GazeMutableStats {

    public double targetRange;
    public double actionRadius;
    public int manaCost;
    public int xpLevels;
    public int healthCost;
    public float exhaustion;
    public int cooldown;
    public int particleCount;
    public double particleSpread;
    public double particleSpeed;

    private GazeMutableStats() {}

    public static GazeMutableStats fromDefinition(GazeDefinition def) {
        GazeMutableStats stats = new GazeMutableStats();

        if (def.target() != null) {
            stats.targetRange = def.target().range();
        }

        if (def.action() != null) {
            JsonObject data = def.action().data();
            stats.actionRadius = GazeJsonHelper.getDouble(data, "radius", 0.0);
        }

        if (stats.actionRadius <= 0.0 && def.target() != null) {
            stats.actionRadius = def.target().radius();
        }

        if (def.cost() != null) {
            stats.manaCost = def.cost().mana();
            stats.xpLevels = def.cost().xpLevels();
            stats.healthCost = def.cost().health();
            stats.exhaustion = def.cost().exhaustion();
        }

        stats.cooldown = def.cooldown();

        if (def.particles() != null) {
            stats.particleCount = def.particles().count();
            stats.particleSpread = def.particles().spread();
            stats.particleSpeed = def.particles().speed();
        }

        return stats;
    }

    public void clampNonNegative() {
        targetRange = Math.max(0.0, targetRange);
        actionRadius = Math.max(0.0, actionRadius);
        manaCost = Math.max(0, manaCost);
        xpLevels = Math.max(0, xpLevels);
        healthCost = Math.max(0, healthCost);
        exhaustion = Math.max(0.0f, exhaustion);
        cooldown = Math.max(0, cooldown);
        particleCount = Math.max(0, particleCount);
        particleSpread = Math.max(0.0, particleSpread);
        particleSpeed = Math.max(0.0, particleSpeed);
    }
}
