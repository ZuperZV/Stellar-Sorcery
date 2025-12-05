package net.zuperz.stellar_sorcery.data.spell;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;

public class StatusEffectHelper {

    public static Holder<MobEffect> mapEffect(String id) {

        if (id == null) return null;

        String key = id.toLowerCase().trim();

        return switch (key) {
            case "fire", "ignite" -> MobEffects.FIRE_RESISTANCE;
            case "poison"         -> MobEffects.POISON;
            case "wither"         -> MobEffects.WITHER;
            case "slow", "slowness", "freeze" -> MobEffects.MOVEMENT_SLOWDOWN;
            case "speed"          -> MobEffects.MOVEMENT_SPEED;
            case "strength"       -> MobEffects.DAMAGE_BOOST;
            case "regen", "regeneration" -> MobEffects.REGENERATION;

            default -> {
                System.err.println("[SpellSystem] Unknown status effect: " + id);
                yield null;
            }
        };
    }
}