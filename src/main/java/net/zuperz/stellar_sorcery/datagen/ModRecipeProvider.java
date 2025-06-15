package net.zuperz.stellar_sorcery.datagen;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
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

        //fourBlockStorageRecipes(pWriter, RecipeCategory.BUILDING_BLOCKS, ModItems.CHROMIUM_INGOT.get(), RecipeCategory.MISC,
        //        ModBlocks.CHROMIUM_BLOCK.get());

        /*ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.KILN.get())
                .pattern("C C")
                .pattern("ABA")
                .pattern("AAA")
                .define('A', ModBlocks.HARDENED_BRICKS)
                .define('B', Blocks.FURNACE)
                .define('C', Items.IRON_INGOT)
                .unlockedBy("has_hardened_bricks", inventoryTrigger(ItemPredicate.Builder.item().
                        of(ModBlocks.HARDENED_BRICKS).build()))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.TERRAMIX.get())
                .pattern("AB")
                .pattern("BA")
                .define('A', Blocks.PACKED_MUD)
                .define('B', Items.CLAY_BALL)
                .unlockedBy("has_clay", inventoryTrigger(ItemPredicate.Builder.item().
                        of(Items.CLAY_BALL).build()))
                .save(pWriter);

        SimpleCookingRecipeBuilder.smelting(Ingredient.of(ModBlocks.TERRAMIX.get()), RecipeCategory.MISC, ModBlocks.HARDENED_BRICKS.get(), 5, 60)
                .unlockedBy("has_" + getItemName(ModBlocks.TERRAMIX.get()), inventoryTrigger(ItemPredicate.Builder.item().of(ModBlocks.TERRAMIX.get()).build()))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, getItemName(ModBlocks.HARDENED_BRICKS.get()) + "_from_smelting"));

        rawToIngot(ModItems.RAW_COBALT.get(), RecipeCategory.MISC, "cobalt_ingot", 0.2f, 300, pWriter);
        
         */

        //for (var scute : ArmadilloScuteRegistry.getInstance().getArmadilloScuteTypes()) {
        //    if (scute == ModArmadilloScutes.NONE) continue;
//
        //    if (!scute.getName().equals("none")) {
        //        String scuteName = scute.getName() + "_scute";
        //        ItemStack scuteItem = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, scuteName)), 1);
//
        //        String essenceName = scute.getName() + "_essence";
        //        ItemStack essenceItem = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, essenceName)), 1);
//
//
        //        String armorName = scute.getName() + "_armor";
        //        ItemStack armorItem = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, armorName)), 1);
//
        //        if (scuteItem != null && essenceItem != null && essenceItem != Items.AIR.getDefaultInstance()) {
        //            CentrifugeRecipeBuilder.centrifuge(Ingredient.of(scuteItem), RecipeCategory.MISC, essenceItem)
        //                    .group("centrifuge_" + scute.getName())
        //                    .unlockedBy("has_" + scuteName, inventoryTrigger(ItemPredicate.Builder.item().of(scuteItem.getItem()).build()))
        //                    .save(pWriter, ResourceLocation.fromNamespaceAndPath("resource_armadillo", scute.getName() + "_crafted_from_centrifuge"));
        //        }
//
        //        if (scuteItem != null && armorItem != null && armorItem != Items.AIR.getDefaultInstance()) {
        //            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, armorItem)
        //                    .pattern("A  ")
        //                    .pattern("AAA")
        //                    .pattern("A A")
        //                    .define('A', scuteItem.getItem())
        //                    .unlockedBy("has_" + armorName, inventoryTrigger(ItemPredicate.Builder.item().of(scuteItem.getItem()).build()))
        //                    .save(pWriter, ResourceLocation.fromNamespaceAndPath("resource_armadillo", armorName + "_crafted_from_" + scuteName));
        //        }
//
        //        if (scuteItem != null) {
        //            //hiveScuteBreeding( String.valueOf(scuteItem), String.valueOf(scuteItem), String.valueOf(scuteItem), pWriter);
        //            nestRecipe(String.valueOf(scuteItem), String.valueOf(scuteItem), pWriter);
        //        }
        //    }
        //}
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

    private static void essenceShapedRecipe(Object output, int count, String[] pattern, Map<Character, String> ingredients, RecipeOutput pWriter, String recipeName) {
        ItemStack outputStack;
        String outputName;

        if (output instanceof Item item) {
            outputStack = new ItemStack(item, count);
            outputName = BuiltInRegistries.ITEM.getKey(item).getPath();
        } else if (output instanceof String itemName) {
            outputStack = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(itemName)), count);
            outputName = ResourceLocation.parse(itemName).getPath();
        } else {
            throw new IllegalArgumentException("Output must be either an Item or a String representing an item name.");
        }

        ShapedRecipeBuilder builder = ShapedRecipeBuilder.shaped(RecipeCategory.MISC, outputStack);
        for (String line : pattern) {
            builder.pattern(line);
        }

        for (Map.Entry<Character, String> entry : ingredients.entrySet()) {
            builder.define(entry.getKey(), BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, entry.getValue())));
        }

        for (String ingredient : ingredients.values()) {
            builder.unlockedBy("has_" + ingredient, inventoryTrigger(
                    ItemPredicate.Builder.item()
                            .of(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, ingredient)))
                            .build()
            ));
        }

        if (recipeName != null && !recipeName.isEmpty()) {
            outputName += "_" + recipeName;
        }

        builder.save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, outputName + "_essence_recipe"));
    }
}