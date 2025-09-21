package net.zuperz.stellar_sorcery.datagen;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.neoforged.neoforge.fluids.FluidStack;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.datagen.custom.*;
import net.zuperz.stellar_sorcery.fluid.ModFluids;
import net.zuperz.stellar_sorcery.item.ModItems;
import net.zuperz.stellar_sorcery.capability.RecipesHelper.TimeOfDay;
import net.zuperz.stellar_sorcery.util.ModTags;
import net.zuperz.stellar_sorcery.datagen.custom.*;

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
        // Crafting Shaped

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SOFT_CLAY_JAR.get())
                .pattern(" A ")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', Items.CLAY_BALL)
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.RITUAL_DAGGER.get())
                .pattern("A")
                .pattern("B")
                .pattern("C")
                .define('A', Items.IRON_INGOT)
                .define('B', ModItems.BLOOD_VIAL)
                .define('C', Items.STICK)
                .unlockedBy("has_clay_ball", has(Items.CLAY_BALL))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.LIGHT_JAR.get())
                .pattern(" A ")
                .pattern("CBC")
                .pattern("ADA")
                .define('A', ItemTags.LOGS)
                .define('B', Blocks.GLASS)
                .define('C', ModTags.Items.STELLAR_SORCERY_FLOWER_ITEMS)
                .define('D', Items.SPIDER_EYE)
                .unlockedBy("has_stellar_sorcery_flower_items", has(ModTags.Items.STELLAR_SORCERY_FLOWER_ITEMS))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.LIGHT_INFUSER.get())
                .pattern(" B ")
                .pattern("CEC")
                .pattern("ADA")
                .define('A', ItemTags.LOGS)
                .define('B', ModItems.WIND_CLAY_JAR)
                .define('C', ModTags.Items.STELLAR_SORCERY_FLOWER_ITEMS)
                .define('D', ModBlocks.NIGELLA_DAMASCENA.asItem())
                .define('E', Items.SPIDER_EYE)
                .unlockedBy("has_stellar_sorcery_flower_items", has(ModTags.Items.STELLAR_SORCERY_FLOWER_ITEMS))
                .save(pWriter);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.LIGHT_BEAM_EMITTER.get())
                .pattern(" A ")
                .pattern("BCB")
                .define('A', Items.SPIDER_EYE)
                .define('B', Items.AMETHYST_SHARD)
                .define('C', ItemTags.LOGS)
                .unlockedBy("has_spider_eye", has(Items.SPIDER_EYE))
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

        // Crafting Shapeless

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModBlocks.STUMP)
                .requires(ItemTags.LOGS)
                .requires(ModTags.Items.STELLAR_SORCERY_FLOWER_ITEMS)
                .requires(Items.DIRT)
                .group("stellar_sorcery_stumps")
                .unlockedBy("has_stellar_sorcery_flower_items", has(ModTags.Items.STELLAR_SORCERY_FLOWER_ITEMS))
                .save(pWriter);

        // Furnaces

        SimpleCookingRecipeBuilder.blasting (Ingredient.of(ModItems.SOFT_CLAY_JAR.get()), RecipeCategory.MISC , ModItems.CLAY_JAR.get(), 0.20f , 100)
                .unlockedBy("has_soft_clay_jar",inventoryTrigger(ItemPredicate.Builder.item().of(ModItems.SOFT_CLAY_JAR.get()).build()))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "clay_jar_from_blasting"));

        SimpleCookingRecipeBuilder.smelting (Ingredient.of(ModItems.SOFT_CLAY_JAR.get()), RecipeCategory.MISC , ModItems.CLAY_JAR.get(), 0.15f , 200)
                .unlockedBy("has_soft_clay_jar",inventoryTrigger(ItemPredicate.Builder.item().of(ModItems.SOFT_CLAY_JAR.get()).build()));

        // Stump

        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.FRITILLARIA_MELEAGRIS_SEEDS.get()),
                        Ingredient.of(Items.WHEAT_SEEDS),
                        Ingredient.of(ModBlocks.RED_CAMPION.asItem()),
                        Ingredient.of(Items.WHEAT)
                )
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

        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.EMPTY_ESSENCE_BOTTLE.get()),
                        Ingredient.of(Items.GLASS_BOTTLE),
                        Ingredient.of(ModTags.Items.STELLAR_SORCERY_FLOWER_ITEMS)
                )
                .timeOfDay(TimeOfDay.BOTH)
                .recipeTime(80)
                .unlockedBy("has_glass_bottle", has(Items.GLASS_BOTTLE))
                .save(pWriter);

        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.ROOT.get()),
                        Ingredient.of(Items.STICK),
                        Ingredient.of(ModItems.ESSENCE_BOTTLE),
                        Ingredient.of(ModBlocks.RED_CAMPION.asItem())
                )
                .withEssenceType("stellar_sorcery:fritillaria_meleagris,minecraft:stick,stellar_sorcery:calendula")
                .timeOfDay(TimeOfDay.DAY)
                .recipeTime(80)
                .unlockedBy("has_wheat_seeds", has(Items.WHEAT_SEEDS))
                .save(pWriter);

        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.MOONSHINE_CATALYST.get()),
                        Ingredient.of(Items.GUNPOWDER),
                        Ingredient.of(ModItems.ESSENCE_BOTTLE),
                        Ingredient.of(Items.AMETHYST_SHARD),
                        Ingredient.of(Items.HEART_OF_THE_SEA),
                        Ingredient.of(ModItems.ROOT)
                )
                .withEssenceType("minecraft:fermented_spider_eye,minecraft:glow_berries,stellar_sorcery:nigella_damascena")
                .timeOfDay(TimeOfDay.NIGHT)
                .recipeTime(180)
                .unlockedBy("has_heart_of_the_sea", has(Items.HEART_OF_THE_SEA))
                .save(pWriter);

        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.CELESTIAL_BLADE.get()),
                        Ingredient.of(Items.GUNPOWDER),
                        Ingredient.of(ModItems.ESSENCE_BOTTLE),
                        Ingredient.of(Items.AMETHYST_SHARD),
                        Ingredient.of(Items.DIAMOND),
                        Ingredient.of(ModItems.ROOT)
                )
                .withEssenceType("minecraft:fermented_spider_eye,minecraft:glow_berries,stellar_sorcery:nigella_damascena")
                .timeOfDay(TimeOfDay.NIGHT)
                .recipeTime(180)
                .unlockedBy("test", has(Items.HEART_OF_THE_SEA))
                .save(pWriter);

        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.WHISPERING_FRAGMENT.get()),
                        Ingredient.of(ModItems.MOONSHINE_SHARD),
                        Ingredient.of(ModItems.ESSENCE_BOTTLE),
                        Ingredient.of(Items.ECHO_SHARD),
                        Ingredient.of(ModBlocks.NIGELLA_DAMASCENA),
                        Ingredient.of(Items.PHANTOM_MEMBRANE)
                )
                .withBlock(ModBlocks.BUDDING_MOONSHINE.get())
                .needsBlock(true)
                .blockOutput(Blocks.BUDDING_AMETHYST)
                .withEssenceType("minecraft:glow_berries,minecraft:phantom_membrane,stellar_sorcery:red_campion")
                .timeOfDay(TimeOfDay.NIGHT)
                .recipeTime(300)
                .unlockedBy("has_moonshine_shard", has(ModItems.MOONSHINE_SHARD))
                .save(pWriter);


        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.FIRE_CLAY_JAR.get()),
                        Ingredient.of(ModItems.CLAY_JAR),
                        Ingredient.of(Items.FLINT_AND_STEEL),
                        Ingredient.of(Items.GUNPOWDER)
                )
                .timeOfDay(TimeOfDay.BOTH)
                .recipeTime(300)
                .unlockedBy("has_clay_jar", has(ModItems.CLAY_JAR))
                .save(pWriter);


        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.TWIG_CLAY_JAR.get()),
                        Ingredient.of(ModItems.CLAY_JAR),
                        Ingredient.of(ModItems.ROOT),
                        Ingredient.of(Items.STICK)
                )
                .timeOfDay(TimeOfDay.BOTH)
                .recipeTime(300)
                .unlockedBy("has_clay_jar", has(ModItems.CLAY_JAR))
                .save(pWriter);


        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.WIND_CLAY_JAR.get()),
                        Ingredient.of(ModItems.CLAY_JAR),
                        Ingredient.of(ModItems.MOONSHINE_SHARD),
                        Ingredient.of(Items.LAPIS_LAZULI)
                )
                .timeOfDay(TimeOfDay.BOTH)
                .recipeTime(300)
                .unlockedBy("has_clay_jar", has(ModItems.CLAY_JAR))
                .save(pWriter);


        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.WATER_CLAY_JAR.get()),
                        Ingredient.of(ModItems.CLAY_JAR),
                        Ingredient.of(Items.WATER_BUCKET),
                        Ingredient.of(Items.PRISMARINE_CRYSTALS)
                )
                .timeOfDay(TimeOfDay.BOTH)
                .recipeTime(300)
                .unlockedBy("has_clay_jar", has(ModItems.CLAY_JAR))
                .save(pWriter);


        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.SHADOW_CLAY_JAR.get()),
                        Ingredient.of(ModItems.CLAY_JAR),
                        Ingredient.of(Items.ENDER_PEARL),
                        Ingredient.of(Items.BLACK_DYE)
                )
                .timeOfDay(TimeOfDay.NIGHT)
                .recipeTime(300)
                .unlockedBy("has_clay_jar", has(ModItems.CLAY_JAR))
                .save(pWriter);


        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.STONE_CLAY_JAR.get()),
                        Ingredient.of(ModItems.CLAY_JAR),
                        Ingredient.of(Items.COBBLESTONE),
                        Ingredient.of(Items.IRON_NUGGET)
                )
                .timeOfDay(TimeOfDay.BOTH)
                .recipeTime(300)
                .unlockedBy("has_clay_jar", has(ModItems.CLAY_JAR))
                .save(pWriter);


        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.SUN_CLAY_JAR.get()),
                        Ingredient.of(ModItems.CLAY_JAR),
                        Ingredient.of(Items.GLOWSTONE_DUST),
                        Ingredient.of(Items.BLAZE_POWDER)
                )
                .timeOfDay(TimeOfDay.DAY)
                .recipeTime(300)
                .unlockedBy("has_clay_jar", has(ModItems.CLAY_JAR))
                .save(pWriter);


        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.FROST_CLAY_JAR.get()),
                        Ingredient.of(ModItems.CLAY_JAR),
                        Ingredient.of(Items.SNOWBALL),
                        Ingredient.of(Items.PACKED_ICE)
                )
                .timeOfDay(TimeOfDay.BOTH)
                .recipeTime(300)
                .unlockedBy("has_clay_jar", has(ModItems.CLAY_JAR))
                .save(pWriter);


        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.STORM_CLAY_JAR.get()),
                        Ingredient.of(ModItems.CLAY_JAR),
                        Ingredient.of(Items.PRISMARINE_SHARD),
                        Ingredient.of(Items.GUNPOWDER)
                )
                .timeOfDay(TimeOfDay.BOTH)
                .recipeTime(300)
                .unlockedBy("has_clay_jar", has(ModItems.CLAY_JAR))
                .save(pWriter);


        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.EXTRACTER_CLAY_JAR.get()),
                        Ingredient.of(ModItems.CLAY_JAR),
                        Ingredient.of(ModItems.FIRE_CLAY_JAR),
                        Ingredient.of(ModItems.TWIG_CLAY_JAR),
                        Ingredient.of(ModItems.WIND_CLAY_JAR),
                        Ingredient.of(ModItems.ESSENCE_BOTTLE)
                )
                .withEssenceType("minecraft:gunpowder,minecraft:redstone,minecraft:tnt")
                .timeOfDay(TimeOfDay.BOTH)
                .recipeTime(300)
                .unlockedBy("has_clay_jar", has(ModItems.CLAY_JAR))
                .save(pWriter);

        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.FRITILLARIA_MELEAGRIS.get()),
                        Ingredient.of(ModItems.FRITILLARIA_MELEAGRIS_SEEDS),
                        Ingredient.of(ModBlocks.RED_CAMPION.asItem())
                )
                .fluidInput(new FluidStack(ModFluids.SOURCE_NOCTILUME.get(), 1000))
                .timeOfDay(TimeOfDay.BOTH)
                .recipeTime(80)
                .unlockedBy("has_stick", has(Items.STICK))
                .save(pWriter);


        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.BLUESTONE_DUST.get()),
                        Ingredient.of(Items.GLOWSTONE_DUST),
                        Ingredient.of(Items.LAPIS_LAZULI),
                        Ingredient.of(ModItems.FROST_CLAY_JAR),
                        Ingredient.of(ModTags.Items.STELLAR_SORCERY_FLOWER_ITEMS),
                        Ingredient.of(ModItems.ESSENCE_BOTTLE)
                )
                .withEssenceType("minecraft:fermented_spider_eye,minecraft:glow_berries,stellar_sorcery:nigella_damascena")
                .fluidInput(new FluidStack(ModFluids.SOURCE_NOCTILUME.get(), 5000))
                .timeOfDay(TimeOfDay.NIGHT)
                .recipeTime(100)
                .unlockedBy("has_glowstone_dust", has(Items.GLOWSTONE_DUST))
                .save(pWriter);


        StumpRecipeBuilder.stump(RecipeCategory.MISC, new ItemStack(ModItems.EMPTY_ESSENCE_AMULET.get()),
                        Ingredient.of(Items.GOLD_INGOT),
                        Ingredient.of(Items.LAPIS_LAZULI),
                        Ingredient.of(ModTags.Items.STELLAR_SORCERY_FLOWER_ITEMS),
                        Ingredient.of(ModItems.ESSENCE_BOTTLE),
                        Ingredient.of(Items.HEART_OF_THE_SEA)
                )
                .withEssenceType("minecraft:book,minecraft:experience_bottle,minecraft:lapis_lazuli")
                .fluidInput(new FluidStack(ModFluids.SOURCE_NOCTILUME.get(), 3000))
                .timeOfDay(TimeOfDay.BOTH)
                .recipeTime(150)
                .unlockedBy("has_gold_ingot", has(Items.GOLD_INGOT))
                .save(pWriter);

        // Astral Altar

        AstralAltarRecipeBuilder.astralAltar(RecipeCategory.MISC, new ItemStack(Items.NETHERITE_INGOT),
                        Ingredient.of(Items.DIAMOND),
                        Ingredient.of(Items.GOLD_INGOT),
                        Ingredient.of(Items.EMERALD),
                        Ingredient.of(ModItems.ESSENCE_BOTTLE)
                )
                .withEssenceType("stellar_sorcery:fritillaria_meleagris,minecraft:stick,stellar_sorcery:calendula")
                .withEntityType(EntityType.ARMADILLO)
                .withBlock(Blocks.WHEAT)
                .withBlockState(Map.of("age", "7"))
                .needsBlock(false)
                .blockOutput(ModBlocks.FRITILLARIA_MELEAGRIS_CROP.get())
                .timeOfDay(TimeOfDay.BOTH)
                .recipeTime(120)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(pWriter);

        AstralAltarRecipeBuilder.astralAltar(RecipeCategory.MISC, new ItemStack(Items.DIRT),
                        Ingredient.of(Items.DIAMOND),
                        Ingredient.of(Items.GOLD_INGOT),
                        Ingredient.of(Items.EMERALD)
                )
                .withEntityType(EntityType.ENDERMAN)
                .withBlock(Blocks.DIAMOND_BLOCK)
                .needsBlock(true)
                .blockOutput(ModBlocks.ASTRAL_NEXUS.get())
                .timeOfDay(TimeOfDay.NIGHT)
                .recipeTime(120)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(pWriter);

        AstralAltarRecipeBuilder.astralAltar(RecipeCategory.MISC, new ItemStack(Items.RAW_COPPER),
                        Ingredient.of(Items.DIAMOND),
                        Ingredient.of(Items.GOLD_INGOT),
                        Ingredient.of(Items.EMERALD),
                        Ingredient.of(Items.HEART_OF_THE_SEA),
                        Ingredient.of(Items.STICK),
                        Ingredient.of(Items.ACACIA_SAPLING),
                        Ingredient.of(Items.ACACIA_SLAB),
                        Ingredient.of(Items.CHERRY_WOOD),
                        Ingredient.of(ModItems.ESSENCE_BOTTLE)
                )
                .withEntityType(EntityType.WARDEN)
                .withEssenceType("stellar_sorcery:fritillaria_meleagris,minecraft:stick,stellar_sorcery:calendula")
                .timeOfDay(TimeOfDay.DAY)
                .recipeTime(120)
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(pWriter);

        //Soul Candle

        SoulCandleRecipeBuilder.soulCandle(RecipeCategory.MISC, new ItemStack(ModItems.BLOOD_VIAL.get()),
                        Ingredient.of(Items.DIAMOND),
                        Ingredient.of(Items.GOLD_INGOT),
                        Ingredient.of(Items.EMERALD)
                )
                .patternLine("_OOO_")
                .patternLine("O___O")
                .patternLine("O___O")
                .patternLine("O___O")
                .patternLine("_OOO_")

                .block('O', ModBlocks.WHITE_CHALK.get())

                .timeOfDay(TimeOfDay.BOTH)
                .recipeTime(200)
                .unlockedBy("has_obsidian", has(Blocks.OBSIDIAN))
                .save(pWriter);

        // Essence

        EssenceRecipeBuilder.essence(RecipeCategory.MISC,
                        Ingredient.of(ModItems.FRITILLARIA_MELEAGRIS.get()),
                        Ingredient.of(Items.STICK),
                        Ingredient.of(ModBlocks.CALENDULA.get()))
                .unlockedBy("has_fritillaria_meleagris", has(ModItems.FRITILLARIA_MELEAGRIS.get()))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "bottle_essence_of_nature"));

        EssenceRecipeBuilder.essence(RecipeCategory.MISC,
                        Ingredient.of(Items.FERMENTED_SPIDER_EYE),
                        Ingredient.of(Items.GLOW_BERRIES),
                        Ingredient.of(ModBlocks.NIGELLA_DAMASCENA.get()))
                .unlockedBy("has_fermented_spider_eye", has(Items.FERMENTED_SPIDER_EYE))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "bottle_essence_of_night_bloom"));

        EssenceRecipeBuilder.essence(RecipeCategory.MISC,
                        Ingredient.of(Items.GUNPOWDER),
                        Ingredient.of(Items.REDSTONE),
                        Ingredient.of(Items.TNT))
                .unlockedBy("has_tnt", has(Items.TNT))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "bottle_essence_of_chaos"));

        EssenceRecipeBuilder.essence(RecipeCategory.MISC,
                        Ingredient.of(Items.LAPIS_LAZULI),
                        Ingredient.of(Items.BOOK),
                        Ingredient.of(Items.EXPERIENCE_BOTTLE))
                .unlockedBy("has_experience_bottle", has(Items.EXPERIENCE_BOTTLE))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "bottle_essence_of_knowledge"));

        EssenceRecipeBuilder.essence(RecipeCategory.MISC,
                        Ingredient.of(Items.BLAZE_ROD),
                        Ingredient.of(Items.DIAMOND),
                        Ingredient.of(Items.ENDER_PEARL))
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "bottle_essence_diamond_power"));

        EssenceRecipeBuilder.essence(RecipeCategory.MISC,
                        Ingredient.of(Items.AMETHYST_SHARD),
                        Ingredient.of(Items.PHANTOM_MEMBRANE),
                        Ingredient.of(Items.POTION))
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "bottle_essence_of_lingering_myst"));

        EssenceRecipeBuilder.essence(RecipeCategory.MISC,
                        Ingredient.of(Items.BLAZE_POWDER),
                        Ingredient.of(Items.GHAST_TEAR),
                        Ingredient.of(Items.NETHER_STAR))
                .unlockedBy("has_nether_star", has(Items.NETHER_STAR))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "bottle_essence_of_wrath"));

        EssenceRecipeBuilder.essence(RecipeCategory.MISC,
                        Ingredient.of(Items.DRAGON_BREATH),
                        Ingredient.of(Items.GLASS_BOTTLE),
                        Ingredient.of(Items.GLOWSTONE_DUST))
                .unlockedBy("has_dragon_breath", has(Items.DRAGON_BREATH))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "bottle_essence_of_radiance"));

        // Amulet Essence

        AmuletEssenceRecipeBuilder.essence(RecipeCategory.MISC,
                        Ingredient.of(Items.GHAST_TEAR),
                        Ingredient.of(ModItems.FRITILLARIA_MELEAGRIS.get()),
                        Ingredient.of(Items.STICK),
                        Ingredient.of(ModBlocks.CALENDULA.get()))
                .unlockedBy("has_fritillaria_meleagris", has(ModItems.FRITILLARIA_MELEAGRIS.get()))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "bottle_essence_of_nature"));

        AmuletEssenceRecipeBuilder.essence(RecipeCategory.MISC,
                        Ingredient.of(Items.GHAST_TEAR),
                        Ingredient.of(Items.FERMENTED_SPIDER_EYE),
                        Ingredient.of(Items.GLOW_BERRIES),
                        Ingredient.of(ModBlocks.NIGELLA_DAMASCENA.get()))
                .unlockedBy("has_fermented_spider_eye", has(Items.FERMENTED_SPIDER_EYE))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "bottle_essence_of_night_bloom"));

        AmuletEssenceRecipeBuilder.essence(RecipeCategory.MISC,
                        Ingredient.of(Items.GHAST_TEAR),
                        Ingredient.of(Items.GUNPOWDER),
                        Ingredient.of(Items.REDSTONE),
                        Ingredient.of(Items.TNT))
                .unlockedBy("has_tnt", has(Items.TNT))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "bottle_essence_of_chaos"));

        AmuletEssenceRecipeBuilder.essence(RecipeCategory.MISC,
                        Ingredient.of(Items.GHAST_TEAR),
                        Ingredient.of(Items.LAPIS_LAZULI),
                        Ingredient.of(Items.BOOK),
                        Ingredient.of(Items.EXPERIENCE_BOTTLE))
                .unlockedBy("has_experience_bottle", has(Items.EXPERIENCE_BOTTLE))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "bottle_essence_of_knowledge"));

        AmuletEssenceRecipeBuilder.essence(RecipeCategory.MISC,
                        Ingredient.of(Items.GHAST_TEAR),
                        Ingredient.of(Items.BLAZE_ROD),
                        Ingredient.of(Items.DIAMOND),
                        Ingredient.of(Items.ENDER_PEARL))
                .unlockedBy("has_diamond", has(Items.DIAMOND))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "bottle_essence_diamond_power"));

        AmuletEssenceRecipeBuilder.essence(RecipeCategory.MISC,
                        Ingredient.of(Items.GHAST_TEAR),
                        Ingredient.of(Items.AMETHYST_SHARD),
                        Ingredient.of(Items.PHANTOM_MEMBRANE),
                        Ingredient.of(Items.POTION))
                .unlockedBy("has_amethyst_shard", has(Items.AMETHYST_SHARD))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "bottle_essence_of_lingering_myst"));

        AmuletEssenceRecipeBuilder.essence(RecipeCategory.MISC,
                        Ingredient.of(Items.GHAST_TEAR),
                        Ingredient.of(Items.BLAZE_POWDER),
                        Ingredient.of(Items.GHAST_TEAR),
                        Ingredient.of(Items.NETHER_STAR))
                .unlockedBy("has_nether_star", has(Items.NETHER_STAR))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "bottle_essence_of_wrath"));

        AmuletEssenceRecipeBuilder.essence(RecipeCategory.MISC,
                        Ingredient.of(Items.GHAST_TEAR),
                        Ingredient.of(Items.DRAGON_BREATH),
                        Ingredient.of(Items.GLASS_BOTTLE),
                        Ingredient.of(Items.GLOWSTONE_DUST))
                .unlockedBy("has_dragon_breath", has(Items.DRAGON_BREATH))
                .save(pWriter, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "bottle_essence_of_radiance"));

        // Starlight Lunar

        StarLightLunarInfuserRecipeBuilder.lunarInfuser(RecipeCategory.MISC, new FluidStack(ModFluids.SOURCE_NOCTILUME.get(), 1),
                        Ingredient.of(ModItems.MOONSHINE_SHARD)
                )
                .timeOfDay(TimeOfDay.NIGHT)
                .recipeTime(100)
                .save(pWriter);


        StarLightLunarInfuserRecipeBuilder.lunarInfuser(RecipeCategory.MISC, new FluidStack(ModFluids.SOURCE_NOCTILUME.get(), 3),
                        Ingredient.of(ModItems.BLUESTONE_DUST.get())
                )
                .timeOfDay(TimeOfDay.NIGHT)
                .recipeTime(100)
                .save(pWriter);


        /*fourBlockStorageRecipes(pWriter, RecipeCategory.BUILDING_BLOCKS, ModItems.CHROMIUM_INGOT.get(), RecipeCategory.MISC,
                ModBlocks.CHROMIUM_BLOCK.get());

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