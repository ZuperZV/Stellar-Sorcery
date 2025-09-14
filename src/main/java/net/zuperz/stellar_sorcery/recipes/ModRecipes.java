package net.zuperz.stellar_sorcery.recipes;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zuperz.stellar_sorcery.StellarSorcery;

import java.util.function.Supplier;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(BuiltInRegistries.RECIPE_SERIALIZER, StellarSorcery.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(BuiltInRegistries.RECIPE_TYPE, StellarSorcery.MOD_ID);

    public static void register(IEventBus eventBus){
        RECIPE_TYPES.register(eventBus);
        SERIALIZERS.register(eventBus);
    }

    public static final Supplier<RecipeType<AstralAltarRecipe>> ASTRAL_ALTAR_RECIPE_TYPE =
            RECIPE_TYPES.register("astral_altar", () -> AstralAltarRecipe.Type.INSTANCE);

    public static final Supplier<RecipeType<StumpRecipe>> STUMP_RECIPE_TYPE =
            RECIPE_TYPES.register("stump", () -> StumpRecipe.Type.INSTANCE);

    public static final Supplier<RecipeType<EssenceRecipe>> ESSENCE_RECIPE_TYPE =
            RECIPE_TYPES.register("essence_recipe", () -> EssenceRecipe.Type.INSTANCE);

    public static final Supplier<RecipeType<StarLightLunarInfuserRecipe>> STAR_LIGHT_LUNAR_INFUSER_RECIPE_TYPE =
            RECIPE_TYPES.register("star_light_lunar_infuser", () -> StarLightLunarInfuserRecipe.Type.INSTANCE);



    public static final Supplier<RecipeSerializer<AstralAltarRecipe>> ASTRAL_ALTAR_SERIALIZER =
            SERIALIZERS.register("astral_altar", () -> AstralAltarRecipe.Serializer.INSTANCE);

    public static final Supplier<RecipeSerializer<StumpRecipe>> STUMP_SERIALIZER =
            SERIALIZERS.register("stump", () -> StumpRecipe.Serializer.INSTANCE);

    public static final Supplier<RecipeSerializer<EssenceRecipe>> ESSENCE_SERIALIZER =
            SERIALIZERS.register("essence_recipe", () -> EssenceRecipe.Serializer.INSTANCE);

    public static final Supplier<RecipeSerializer<StarLightLunarInfuserRecipe>> STAR_LIGHT_LUNAR_INFUSER_SERIALIZER =
            SERIALIZERS.register("star_light_lunar_infuser", () -> StarLightLunarInfuserRecipe.Serializer.INSTANCE);
}