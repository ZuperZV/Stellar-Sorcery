package net.zuperz.stellar_sorcery.datagen.custom;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.recipes.AstralAltarRecipe;
import net.zuperz.stellar_sorcery.recipes.TimeOfDay;

import javax.annotation.Nullable;
import java.util.*;

public class AstralAltarRecipeBuilder implements RecipeBuilder {

    private final RecipeCategory category;
    private final ItemStack output;
    private final Ingredient moldIngredient;
    private final List<Optional<Ingredient>> additionalIngredients;
    private Optional<EntityType<?>> entityType = Optional.empty();
    private Optional<String> essenceType = Optional.empty();
    private Optional<Block> block = Optional.empty();
    private Optional<Map<String, String>> blockState = Optional.empty();
    private Optional<Boolean> needsBlock = Optional.of(false);
    private Optional<Block> blockOutput = Optional.empty();
    private Optional<TimeOfDay> timeOfDay = Optional.empty();
    private int time = 100;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;

    private AstralAltarRecipeBuilder(RecipeCategory category, ItemStack output, Ingredient moldIngredient, List<Optional<Ingredient>> additionalIngredients) {
        this.category = category;
        this.output = output;
        this.moldIngredient = moldIngredient;
        this.additionalIngredients = new ArrayList<>(additionalIngredients);

        while (this.additionalIngredients.size() < 8) {
            this.additionalIngredients.add(Optional.empty());
        }
    }

    public static AstralAltarRecipeBuilder astralAltar(RecipeCategory category, ItemStack output, Ingredient moldIngredient, Ingredient... additional) {
        List<Optional<Ingredient>> ingredientList = new ArrayList<>();
        for (Ingredient ing : additional) {
            ingredientList.add(Optional.ofNullable(ing));
        }
        return new AstralAltarRecipeBuilder(category, output, moldIngredient, ingredientList);
    }

    public AstralAltarRecipeBuilder withEntityType(EntityType<?> entityType) {
        this.entityType = Optional.of(entityType);
        return this;
    }


    public AstralAltarRecipeBuilder withEssenceType(String essenceType) {
        this.essenceType = Optional.of(essenceType);
        return this;
    }

    public AstralAltarRecipeBuilder withBlock(Block block) {
        this.block = Optional.of(block);
        return this;
    }

    public AstralAltarRecipeBuilder withBlockState(Map<String, String> state) {
        this.blockState = Optional.of(state);
        return this;
    }

    public AstralAltarRecipeBuilder needsBlock(boolean needsBlock) {
        this.needsBlock = Optional.of(needsBlock);
        return this;
    }

    public AstralAltarRecipeBuilder blockOutput(Block block) {
        this.blockOutput = Optional.of(block);
        return this;
    }

    public AstralAltarRecipeBuilder timeOfDay(TimeOfDay timeOfDay) {
        this.timeOfDay = Optional.of(timeOfDay);
        return this;
    }

    public AstralAltarRecipeBuilder recipeTime(int time) {
        this.time = time;
        return this;
    }

    public AstralAltarRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public AstralAltarRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public Item getResult() {
        return output.getItem();
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        ensureValid(id);

        String newPath = "astral_altar/" + id.getPath();

        id = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, newPath);

        Advancement.Builder advancementBuilder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        this.criteria.forEach(advancementBuilder::addCriterion);

        AstralAltarRecipe recipe = new AstralAltarRecipe(
                output,
                moldIngredient,
                additionalIngredients,
                entityType,
                essenceType,
                block,
                blockState,
                needsBlock,
                blockOutput,
                timeOfDay,
                time
        );

        recipeOutput.accept(
                id,
                recipe,

                advancementBuilder.build(
                        id.withPrefix("recipes/" + this.category.getFolderName() + "/")
                )
        );
    }

    private void ensureValid(ResourceLocation id) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }
}