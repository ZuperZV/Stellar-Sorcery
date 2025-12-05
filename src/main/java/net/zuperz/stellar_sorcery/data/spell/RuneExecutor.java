package net.zuperz.stellar_sorcery.data.spell;

import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public class RuneExecutor {

    public static void executeRune(Player caster, List<Entity> targets, RuneFile rune) {

        switch (rune.effect_type) {

            case "damage" -> {
                for (Entity e : targets) {
                    if (e instanceof LivingEntity living) {
                        living.hurt(caster.damageSources().magic(), rune.amount);
                    }
                }
            }

            case "status_effect" -> {

                Holder<MobEffect> effect = StatusEffectHelper.mapEffect(rune.status);

                if (effect == null) {
                    System.err.println("[SpellSystem] Rune has unknown effect: " + rune.status);
                    return;
                }

                for (Entity e : targets) {
                    if (e instanceof LivingEntity le) {
                        le.addEffect(new MobEffectInstance(
                                effect,
                                rune.duration,
                                Math.max(0, rune.tick_damage)
                        ));
                    }
                }
            }

            case "heal" -> {
                for (Entity e : targets) {
                    if (e instanceof LivingEntity living) {
                        living.heal(rune.amount);
                    }
                }
            }
        }
    }
}
