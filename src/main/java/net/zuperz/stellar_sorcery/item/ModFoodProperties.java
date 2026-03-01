package net.zuperz.stellar_sorcery.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.zuperz.stellar_sorcery.effect.ModEffects;

public class ModFoodProperties {
    public static final FoodProperties FRITILLARIA_MELEAGRIS = new FoodProperties.Builder().nutrition(1).saturationModifier(0.25f)
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 100), 0.60f).build();

    public static final FoodProperties SOUL_BLOOMS = new FoodProperties.Builder().nutrition(3).saturationModifier(0.75f)
            .effect(() -> new MobEffectInstance(MobEffects.ABSORPTION, 50), 0.60f)
            .effect(() -> new MobEffectInstance(ModEffects.OMNIVISION, 100), 0.90f).alwaysEdible().build();

}