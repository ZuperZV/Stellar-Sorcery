package net.zuperz.stellar_sorcery.effect;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zuperz.stellar_sorcery.StellarSorcery;

public class ModEffects {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS =
            DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, StellarSorcery.MOD_ID);

    public static final Holder<MobEffect> VULNERABILITY = MOB_EFFECTS.register("vulnerability",
            () -> new BasicEffect(MobEffectCategory.HARMFUL, 0x406278));

    public static final Holder<MobEffect> OMNIVISION = MOB_EFFECTS.register("omnivision",
            () -> new BasicEffect(MobEffectCategory.BENEFICIAL, 0x557672));

    public static void register(IEventBus eventBus) {
        MOB_EFFECTS.register(eventBus);
    }
}
