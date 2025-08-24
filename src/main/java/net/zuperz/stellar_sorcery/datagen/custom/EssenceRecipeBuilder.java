package net.zuperz.stellar_sorcery.datagen.custom;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.component.EssenceBottleData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.item.ModItems;
import net.zuperz.stellar_sorcery.recipes.EssenceRecipe;

import javax.annotation.Nullable;
import java.util.*;

public class EssenceRecipeBuilder implements RecipeBuilder {

    private final RecipeCategory category;
    private final Ingredient input1;
    private final Ingredient input2;
    private final Ingredient input3;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;

    private EssenceRecipeBuilder(RecipeCategory category,
                                 Ingredient input1, Ingredient input2, Ingredient input3) {
        this.category = category;
        this.input1 = input1;
        this.input2 = input2;
        this.input3 = input3;
    }

    public static EssenceRecipeBuilder essence(RecipeCategory category,
                                               Ingredient input1, Ingredient input2, Ingredient input3) {
        return new EssenceRecipeBuilder(category, input1, input2, input3);
    }

    public EssenceRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public EssenceRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public Item getResult() {
        return ModItems.ESSENCE_BOTTLE.get();
    }

    public ItemStack getResultStack() {
        List<ResourceLocation> sortedIds = new ArrayList<>();
        sortedIds.add(getIdFromIngredient(input1));
        sortedIds.add(getIdFromIngredient(input2));
        sortedIds.add(getIdFromIngredient(input3));

        sortedIds.sort(Comparator.comparing(ResourceLocation::toString));

        ItemStack essenceBottle = new ItemStack(ModItems.ESSENCE_BOTTLE.get());

        essenceBottle.set(
                ModDataComponentTypes.ESSENCE_BOTTLE,
                EssenceBottleData.fromIds(sortedIds.stream()
                        .map(ResourceLocation::toString)
                        .toArray(String[]::new))
        );

        return essenceBottle;
    }


    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        ensureValid(id);

        String newPath = "essence/" + id.getPath();
        id = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, newPath);

        Advancement.Builder advancementBuilder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        this.criteria.forEach(advancementBuilder::addCriterion);

        List<ResourceLocation> sortedIds = new ArrayList<>();
        sortedIds.add(getIdFromIngredient(input1));
        sortedIds.add(getIdFromIngredient(input2));
        sortedIds.add(getIdFromIngredient(input3));
        sortedIds.sort(Comparator.comparing(ResourceLocation::toString));

        ItemStack essenceBottle = new ItemStack(ModItems.ESSENCE_BOTTLE.get());
        essenceBottle.set(ModDataComponentTypes.ESSENCE_BOTTLE,
                EssenceBottleData.fromIds(sortedIds.stream().map(ResourceLocation::toString).toArray(String[]::new)));

        EssenceRecipe recipe = new EssenceRecipe(input1, input2, input3, essenceBottle);

        recipeOutput.accept(
                id,
                recipe,
                advancementBuilder.build(id.withPrefix("recipes/" + this.category.getFolderName() + "/"))
        );
    }

    private void ensureValid(ResourceLocation id) {
        if (this.criteria.isEmpty()) {
            throw new IllegalStateException("No way of obtaining recipe " + id);
        }
    }

    private static ResourceLocation getIdFromIngredient(Ingredient ingredient) {
        var stacks = ingredient.getItems();
        if (stacks.length == 0) {
            throw new IllegalArgumentException("Ingredient har ingen items!");
        }
        Item item = stacks[0].getItem();
        return Objects.requireNonNull(item.builtInRegistryHolder().key().location());
    }
}

