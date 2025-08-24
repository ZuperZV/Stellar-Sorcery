package net.zuperz.stellar_sorcery.potion;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.alchemy.Potion;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS =
            DeferredRegister.create(BuiltInRegistries.POTION, "stellar_sorcery");

    public static final DeferredHolder<Potion, Potion> JAR_FIRE_RESISTANCE = POTIONS.register("jar_fire_resistance",
            () -> new Potion(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 4800)));

    public static final DeferredHolder<Potion, Potion> JAR_TWIG = POTIONS.register("jar_twig",
            () -> new Potion(new MobEffectInstance(MobEffects.REGENERATION, 2400)));

    public static final DeferredHolder<Potion, Potion> JAR_WIND = POTIONS.register("jar_wind",
            () -> new Potion(new MobEffectInstance(MobEffects.LEVITATION, 1200)));

    public static final DeferredHolder<Potion, Potion> JAR_WATER = POTIONS.register("jar_water",
            () -> new Potion(new MobEffectInstance(MobEffects.WATER_BREATHING, 4800)));

    public static final DeferredHolder<Potion, Potion> JAR_SHADOW = POTIONS.register("jar_shadow",
            () -> new Potion(new MobEffectInstance(MobEffects.INVISIBILITY, 2400)));

    public static final DeferredHolder<Potion, Potion> JAR_STONE = POTIONS.register("jar_stone",
            () -> new Potion(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 2400)));

    public static final DeferredHolder<Potion, Potion> JAR_SUN = POTIONS.register("jar_sun",
            () -> new Potion(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2400), new MobEffectInstance(MobEffects.NIGHT_VISION, 2400)));

    public static final DeferredHolder<Potion, Potion> JAR_FROST = POTIONS.register("jar_frost",
            () -> new Potion(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 2400)));

    public static final DeferredHolder<Potion, Potion> JAR_STORM = POTIONS.register("jar_storm",
            () -> new Potion(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2400)));

    public static final DeferredHolder<Potion, Potion> JAR_EXTRACTER = POTIONS.register("jar_extracter",
            () -> new Potion(new MobEffectInstance(MobEffects.POISON, 2400)));

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}
