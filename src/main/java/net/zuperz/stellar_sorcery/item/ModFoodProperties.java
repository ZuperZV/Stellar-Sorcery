package net.zuperz.stellar_sorcery.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;

public class ModFoodProperties {
    public static final FoodProperties FRITILLARIA_MELEAGRIS = new FoodProperties.Builder().nutrition(1).saturationModifier(0.25f)
            .effect(() -> new MobEffectInstance(MobEffects.POISON, 100), 0.60f).build();

}