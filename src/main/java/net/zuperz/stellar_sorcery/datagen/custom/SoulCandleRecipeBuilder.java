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
import net.zuperz.stellar_sorcery.capability.RecipesHelper.TimeOfDay;
import net.zuperz.stellar_sorcery.recipes.SoulCandleRecipe;

import javax.annotation.Nullable;
import java.util.*;

public class SoulCandleRecipeBuilder implements RecipeBuilder {

    private final RecipeCategory category;
    private final ItemStack output;

    private final List<String> pattern = new ArrayList<>();
    private final Map<String, Block> blockMapping = new HashMap<>();
    private final List<Optional<Ingredient>> additionalIngredients;

    private Optional<EntityType<?>> entityType = Optional.empty();
    private Optional<String> essenceType = Optional.empty();
    private Optional<Block> additionalBlock = Optional.empty();
    private Optional<Map<String, String>> blockState = Optional.empty();
    private Optional<Boolean> needsBlock = Optional.of(false);
    private Optional<Block> blockOutput = Optional.empty();
    private Optional<TimeOfDay> timeOfDay = Optional.empty();
    private Optional<TimeOfDay> fakeTimeOfDay = Optional.empty();
    private int recipeTime = 100;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;

    private SoulCandleRecipeBuilder(RecipeCategory category, ItemStack output, List<Optional<Ingredient>> additionalIngredients) {
        this.category = category;
        this.output = output;
        this.additionalIngredients = new ArrayList<>(additionalIngredients);

        while (this.additionalIngredients.size() < 8) {
            this.additionalIngredients.add(Optional.empty());
        }
    }

    public static SoulCandleRecipeBuilder soulCandle(RecipeCategory category, ItemStack output, Ingredient... additional) {
        List<Optional<Ingredient>> ingredientList = new ArrayList<>();
        for (Ingredient ing : additional) {
            ingredientList.add(Optional.ofNullable(ing));
        }
        return new SoulCandleRecipeBuilder(category, output, ingredientList);
    }

    public SoulCandleRecipeBuilder patternLine(String line) {
        this.pattern.add(line);
        return this;
    }

    public SoulCandleRecipeBuilder block(char symbol, Block block) {
        this.blockMapping.put(String.valueOf(symbol), block);
        return this;
    }

    public SoulCandleRecipeBuilder withEntityType(EntityType<?> entityType) {
        this.entityType = Optional.of(entityType);
        return this;
    }

    public SoulCandleRecipeBuilder withEssenceType(String essenceType) {
        this.essenceType = Optional.of(essenceType);
        return this;
    }

    public SoulCandleRecipeBuilder withAdditionalBlock(Block block) {
        this.additionalBlock = Optional.of(block);
        return this;
    }

    public SoulCandleRecipeBuilder withBlockState(Map<String, String> state) {
        this.blockState = Optional.of(state);
        return this;
    }

    public SoulCandleRecipeBuilder needsBlock(boolean needsBlock) {
        this.needsBlock = Optional.of(needsBlock);
        return this;
    }

    public SoulCandleRecipeBuilder blockOutput(Block block) {
        this.blockOutput = Optional.of(block);
        return this;
    }

    public SoulCandleRecipeBuilder timeOfDay(TimeOfDay timeOfDay) {
        this.timeOfDay = Optional.of(timeOfDay);
        this.fakeTimeOfDay = Optional.of(timeOfDay);
        return this;
    }

    public SoulCandleRecipeBuilder recipeTime(int time) {
        this.recipeTime = time;
        return this;
    }

    public SoulCandleRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public SoulCandleRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
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

        String newPath = "soul_candle/" + id.getPath();
        id = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, newPath);

        Advancement.Builder advancementBuilder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        this.criteria.forEach(advancementBuilder::addCriterion);

        SoulCandleRecipe recipe = new SoulCandleRecipe(
                output,
                pattern,
                blockMapping,
                additionalIngredients,
                entityType,
                essenceType,
                additionalBlock,
                blockState,
                needsBlock,
                blockOutput,
                timeOfDay,
                fakeTimeOfDay,
                recipeTime
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
        if (this.pattern.isEmpty()) {
            throw new IllegalStateException("No pattern defined for recipe " + id);
        }
    }
}
