package net.zuperz.stellar_sorcery.api.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.api.jei.subtypeInterpreter.EssenceBottleSubtypeInterpreter;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.item.ModItems;
import net.zuperz.stellar_sorcery.recipes.AstralAltarRecipe;
import net.zuperz.stellar_sorcery.recipes.ModRecipes;
import net.zuperz.stellar_sorcery.recipes.StumpRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

@JeiPlugin
public class JEIPlugin implements IModPlugin {

    public static mezz.jei.api.recipe.RecipeType<AstralAltarRecipe> ASTRAL_ALTAR_TYPE =
            new mezz.jei.api.recipe.RecipeType<>(AstralAltarRecipeCategory.UID, AstralAltarRecipe.class);

    public static mezz.jei.api.recipe.RecipeType<StumpRecipe> STUMP_TYPE =
            new mezz.jei.api.recipe.RecipeType<>(StumpRecipeCategory.UID, StumpRecipe.class);

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        var jeiHelpers = registration.getJeiHelpers();

        registration.addRecipeCategories(new AstralAltarRecipeCategory(jeiHelpers.getGuiHelper()));
        registration.addRecipeCategories(new StumpRecipeCategory(jeiHelpers.getGuiHelper()));

        registration.addRecipeCategories(new EssenceBoilerRecipeCategory(jeiHelpers.getGuiHelper()));
    }


    @Override
    public void registerRecipes(@NotNull IRecipeRegistration registration) {
        var world = Minecraft.getInstance().level;
        if (world != null) {

            var astralAltar = world.getRecipeManager();
            registration.addRecipes(AstralAltarRecipeCategory.RECIPE_TYPE,
                    getRecipe(astralAltar, ModRecipes.ASTRAL_ALTAR_RECIPE_TYPE.get()));

            var vitalStump = world.getRecipeManager();
            registration.addRecipes(StumpRecipeCategory.RECIPE_TYPE,
                    getRecipe(vitalStump, ModRecipes.STUMP_RECIPE_TYPE.get()));

            var essenceBoiler = world.getRecipeManager();
            registration.addRecipes(EssenceBoilerRecipeCategory.RECIPE_TYPE,
                    getRecipe(essenceBoiler, ModRecipes.ESSENCE_RECIPE_TYPE.get()));
        }
    }

    public <C extends RecipeInput, T extends Recipe<C>> List<T> getRecipe(RecipeManager manager, RecipeType<T> recipeType){
        List<T> list = new ArrayList<>();
        manager.getAllRecipesFor(recipeType).forEach(tRecipeHolder -> {
            list.add(tRecipeHolder.value());
        });
        return list;
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration){

        var astralAltar = new ItemStack(ModBlocks.ASTRAL_ALTAR.get());
        registration.addRecipeCatalyst(astralAltar, AstralAltarRecipeCategory.RECIPE_TYPE);

        var astralNexus = new ItemStack(ModBlocks.ASTRAL_NEXUS.get());
        registration.addRecipeCatalyst(astralNexus, AstralAltarRecipeCategory.RECIPE_TYPE);

        var vitalStump = new ItemStack(ModBlocks.VITAL_STUMP.get());
        registration.addRecipeCatalyst(vitalStump, StumpRecipeCategory.RECIPE_TYPE);

        var stump = new ItemStack(ModBlocks.STUMP.get());
        registration.addRecipeCatalyst(stump, StumpRecipeCategory.RECIPE_TYPE);

        var essenceBoiler = new ItemStack(ModBlocks.ESSENCE_BOILER.get());
        registration.addRecipeCatalyst(essenceBoiler, EssenceBoilerRecipeCategory.RECIPE_TYPE);
    }

    /*
    @Override
    public void registerGuiHandlers(IGuiHandlerRegistration registration)
    {
        registration.addRecipeClickArea(TestScreen.class, 80, 37, 24, 17, JEIPlugin.TEST_TYPE);
    }
     */

    @Override
    public void registerItemSubtypes(mezz.jei.api.registration.ISubtypeRegistration registration) {
        registration.registerSubtypeInterpreter(
                ModItems.ESSENCE_BOTTLE.get(),
                new EssenceBottleSubtypeInterpreter()
        );
    }

}