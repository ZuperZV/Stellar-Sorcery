package net.zuperz.stellar_sorcery.datagen;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.datagen.custom.AstralAltarRecipeBuilder;
import net.zuperz.stellar_sorcery.datagen.custom.StumpRecipeBuilder;
import net.zuperz.stellar_sorcery.item.ModItems;
import net.zuperz.stellar_sorcery.recipes.TimeOfDay;
import net.zuperz.stellar_sorcery.util.ModTags;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(pOutput, pRegistries);
    }

    @Override
    protected void buildRecipes(RecipeOutput pWriter) {
        System.out.println("pWriter: " + pWriter);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.STUMP)
                .requires(Items.OAK_LOG)
                .requires(ModTags.Items.STELLAR_SORCERY_FLOWER_ITEMS)
                .requires(Items.DIRT)
                .group("stellar_sorcery_stumps")
                .unlockedBy("has_stellar_sorcery_flower_items", has(ModTags.Items.STELLAR_SORCERY_FLOWER_ITEMS))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.VITAL_STUMP)
                .pattern("DCD")
                .pattern("BAB")
                .define('A', ModBlocks.STUMP.asItem())
                .define('B', ModTags.Items.STELLAR_SORCERY_FLOWER_ITEMS)
                .define('C', ModBlocks.RED_CAMPION.asItem())
                .define('D', Items.GOLD_INGOT)
                .group("stellar_sorcery_stumps")
                .unlockedBy("has_red_campion", has(ModBlocks.RED_CAMPION.asItem()))
                .save(pWriter);

        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.FRITILLARIA_MELEAGRIS_SEEDS.get()),
                        Ingredient.of(Items.WHEAT_SEEDS),
                        Ingredient.of(ModBlocks.RED_CAMPION.asItem()),
                        Ingredient.of(Items.WHEAT),
                        Ingredient.of(ModItems.ESSENCE_BOTTLE)
                )
                .withEssenceType("minecraft:apple,minecraft:stick,minecraft:stone")
                .withBlock(Blocks.WHEAT)
                .withBlockState(Map.of("age", "7"))
                .needsBlock(false)
                .blockOutput(ModBlocks.FRITILLARIA_MELEAGRIS_CROP.get())
                .timeOfDay(TimeOfDay.DAY)
                .recipeTime(80)
                .unlockedBy("has_wheat_seeds", has(Items.WHEAT_SEEDS))
                .save(pWriter);

        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModBlocks.RED_CAMPION.asItem()),
                        Ingredient.of(Blocks.POPPY),
                        Ingredient.of(ModBlocks.RED_CAMPION.asItem()),
                        Ingredient.of(ModItems.FRITILLARIA_MELEAGRIS)
                )
                .withBlock(Blocks.POPPY)
                .needsBlock(true)
                .blockOutput(ModBlocks.RED_CAMPION.get())
                .timeOfDay(TimeOfDay.DAY)
                .recipeTime(80)
                .unlockedBy("has_poppy", has(Blocks.POPPY))
                .save(pWriter);

        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModBlocks.CALENDULA.asItem()),
                        Ingredient.of(Blocks.OXEYE_DAISY),
                        Ingredient.of(ModBlocks.CALENDULA.asItem()),
                        Ingredient.of(ModItems.FRITILLARIA_MELEAGRIS)
                )
                .withBlock(Blocks.OXEYE_DAISY)
                .needsBlock(true)
                .blockOutput(ModBlocks.CALENDULA.get())
                .timeOfDay(TimeOfDay.DAY)
                .recipeTime(90)
                .unlockedBy("has_oxeye_daisy", has(Blocks.OXEYE_DAISY))
                .save(pWriter);

        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModBlocks.NIGELLA_DAMASCENA.asItem()),
                        Ingredient.of(Blocks.WITHER_ROSE),
                        Ingredient.of(ModBlocks.NIGELLA_DAMASCENA.asItem()),
                        Ingredient.of(ModItems.FRITILLARIA_MELEAGRIS)
                )
                .withBlock(Blocks.WITHER_ROSE)
                .needsBlock(true)
                .blockOutput(ModBlocks.NIGELLA_DAMASCENA.get())
                .timeOfDay(TimeOfDay.DAY)
                .recipeTime(100)
                .unlockedBy("has_wither_rose", has(Blocks.WITHER_ROSE))
                .save(pWriter);


        AstralAltarRecipeBuilder.astralAltar(RecipeCategory.MISC, new ItemStack(Items.NETHERITE_INGOT),
                        Ingredient.of(Items.DIAMOND),
                        Ingredient.of(Items.GOLD_INGOT),
                        Ingredient.of(Items.EMERALD),
                        Ingredient.of(ModItems.ESSENCE_BOTTLE)
                )
                .withEssenceType("minecraft:apple,minecraft:stick,minecraft:stone")
                .withBlock(Blocks.WHEAT)
                .withBlockState(Map.of("age", "7"))
                .needsBlock(false)
                .blockOutput(ModBlocks.FRITILLARIA_MELEAGRIS_CROP.get())
                .timeOfDay(TimeOfDay.BOTH)
                .recipeTime(120)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(pWriter);


        /*fourBlockStorageRecipes(pWriter, RecipeCategory.BUILDING_BLOCKS, ModItems.CHROMIUM_INGOT.get(), RecipeCategory.MISC,
                ModBlocks.CHROMIUM_BLOCK.get());

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModBlocks.TERRAMIX.get()), RecipeCategory.MISC, ModBlocks.HARDENED_BRICKS.get(), 5, 60)
                .unlockedBy("has_" + getItemName(ModBlocks.TERRAMIX.get()), inventoryTrigger(ItemPredicate.Builder.item().of(ModBlocks.TERRAMIX.get()).build()))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, getItemName(ModBlocks.HARDENED_BRICKS.get()) + "_from_smelting"));

        rawToIngot(ModItems.RAW_COBALT.get(), RecipeCategory.MISC, "cobalt_ingot", 0.2f, 300, pWriter);
         */
    }

    protected static void oreSmelting(RecipeOutput pRecipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult,
                                      float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(pRecipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, pIngredients, pCategory, pResult,
                pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(RecipeOutput pRecipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult,
                                      float pExperience, int pCookingTime, String pGroup) {
        oreCooking(pRecipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, pIngredients, pCategory, pResult,
                pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    public static void smithingRecipe(RecipeOutput pRecipeOutput, Item ingredient1, Item ingredient2, Item ingredient3, RecipeCategory category, Item result) {
        SmithingTransformRecipeBuilder.smithing(Ingredient.of(ingredient1.getDefaultInstance()), Ingredient.of(ingredient2.getDefaultInstance()), Ingredient.of(ingredient3.getDefaultInstance()), category, result)
                .unlocks("has_brush_ingot", has(Items.BRUSH))
                .save(pRecipeOutput, "_smithing");
    }


    protected static void netheriteSmithing(RecipeOutput pRecipeOutput, Item ingredient1, Item ingredient2, Item ingredient3, RecipeCategory p_248986_, Item p_250389_) {
        SmithingTransformRecipeBuilder.smithing(
                        Ingredient.of(ingredient1), Ingredient.of(ingredient2), Ingredient.of(ingredient3), p_248986_, p_250389_
                )
                .unlocks("has_" + ingredient2.getDescriptionId(), has(ingredient2))
                .save(pRecipeOutput, getItemName(p_250389_) + "_smithing");
    }

    protected static <T extends AbstractCookingRecipe> void oreCooking(RecipeOutput pRecipeOutput, RecipeSerializer<T> pCookingSerializer, AbstractCookingRecipe.Factory<T> factory,
                                                                       List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult, pExperience, pCookingTime, pCookingSerializer, factory).group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(pRecipeOutput, StellarSorcery.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }

    protected static void fourBlockStorageRecipes(
            RecipeOutput p_301057_, RecipeCategory p_251203_, ItemLike p_251689_, RecipeCategory p_251376_, ItemLike p_248771_
    ) {
        fourBlockStorageRecipes(
                p_301057_, p_251203_, p_251689_, p_251376_, p_248771_, getSimpleRecipeName(p_248771_), null, getSimpleRecipeName(p_251689_), null
        );
    }

    protected static void fourBlockStorageRecipes(
            RecipeOutput p_301222_,
            RecipeCategory p_250083_,
            ItemLike p_250042_,
            RecipeCategory p_248977_,
            ItemLike p_251911_,
            String p_250475_,
            @Nullable String p_248641_,
            String p_252237_,
            @Nullable String p_250414_
    ) {
        ShapelessRecipeBuilder.shapeless(p_250083_, p_250042_, 4)
                .requires(p_251911_)
                .group(p_250414_)
                .unlockedBy(getHasName(p_251911_), has(p_251911_))
                .save(p_301222_, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, p_252237_));
        ShapedRecipeBuilder.shaped(p_248977_, p_251911_)
                .define('#', p_250042_)
                .pattern("##")
                .pattern("##")
                .group(p_248641_)
                .unlockedBy(getHasName(p_250042_), has(p_250042_))
                .save(p_301222_, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, p_250475_));
    }

    public static void rawToIngot(ItemLike rawItem, RecipeCategory category, String ingot, float experience, int cookingTime, RecipeOutput pWriter) {

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(rawItem), category, (BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("metal_morph", ingot))), experience, cookingTime)
                .unlockedBy("has_" + getItemName(rawItem), inventoryTrigger(ItemPredicate.Builder.item().of(rawItem).build()))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, getItemName((BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("metal_morph", ingot)))) + "_from_smelting"));

        float blastingExperience = experience - 0.10f;
        int blastingTime = cookingTime - 100 >= 0 ? cookingTime - 100 : cookingTime;

        SimpleCookingRecipeBuilder.blasting(Ingredient.of(rawItem), category, (BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("metal_morph", ingot))), blastingExperience, blastingTime)
                .unlockedBy("has_" + getItemName(rawItem), inventoryTrigger(ItemPredicate.Builder.item().of(rawItem).build()))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, getItemName((BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("metal_morph", ingot)))) + "_from_blasting"));
    }
}