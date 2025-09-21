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
import net.zuperz.stellar_sorcery.recipes.AmuletEssenceRecipe;

import javax.annotation.Nullable;
import java.util.*;

public class AmuletEssenceRecipeBuilder implements RecipeBuilder {

    private final RecipeCategory category;
    private final Ingredient input2;
    private final Ingredient amuletEssence1;
    private final Ingredient amuletEssence2;
    private final Ingredient amuletEssence3;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;

    private AmuletEssenceRecipeBuilder(RecipeCategory category, Ingredient input2, Ingredient amuletEssence1, Ingredient amuletEssence2, Ingredient amuletEssence3) {
        this.category = category;
        this.input2 = input2;
        this.amuletEssence1 = amuletEssence1;
        this.amuletEssence2 = amuletEssence2;
        this.amuletEssence3 = amuletEssence3;
    }

    public static AmuletEssenceRecipeBuilder essence(RecipeCategory category,
                                                     Ingredient input2, Ingredient amuletEssence1, Ingredient amuletEssence2, Ingredient amuletEssence3) {
        return new AmuletEssenceRecipeBuilder(category, input2, amuletEssence1, amuletEssence2, amuletEssence3);
    }

    public AmuletEssenceRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    public AmuletEssenceRecipeBuilder unlockedBy(String name, Criterion<?> criterion) {
        this.criteria.put(name, criterion);
        return this;
    }

    @Override
    public Item getResult() {
        return ModItems.ESSENCE_BOTTLE.get();
    }

    public ItemStack getResultStack() {
        List<ResourceLocation> sortedIds = new ArrayList<>();
        sortedIds.add(getIdFrom(amuletEssence1));
        sortedIds.add(getIdFrom(amuletEssence2));
        sortedIds.add(getIdFrom(amuletEssence3));

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

        String newPath = "essence_amulet/" + id.getPath();
        id = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, newPath);

        Advancement.Builder advancementBuilder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        this.criteria.forEach(advancementBuilder::addCriterion);

        List<ResourceLocation> sortedIds = new ArrayList<>();
        sortedIds.add(getIdFrom(amuletEssence1));
        sortedIds.add(getIdFrom(amuletEssence2));
        sortedIds.add(getIdFrom(amuletEssence3));
        sortedIds.sort(Comparator.comparing(ResourceLocation::toString));

        ItemStack essenceBottle = new ItemStack(ModItems.ESSENCE_BOTTLE.get());
        essenceBottle.set(ModDataComponentTypes.ESSENCE_BOTTLE,
                EssenceBottleData.fromIds(sortedIds.stream().map(ResourceLocation::toString).toArray(String[]::new)));

        ItemStack essenceAmulet = new ItemStack(ModItems.ESSENCE_AMULET.get());
        essenceAmulet.set(ModDataComponentTypes.ESSENCE_BOTTLE,
                EssenceBottleData.fromIds(sortedIds.stream().map(ResourceLocation::toString).toArray(String[]::new)));

        AmuletEssenceRecipe recipe = new AmuletEssenceRecipe(
                essenceBottle,
                input2,
                amuletEssence1,
                amuletEssence2,
                amuletEssence3,
                essenceAmulet
        );

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

    private static ResourceLocation getIdFrom(Ingredient ingredient) {
        var stacks = ingredient.getItems();
        if (stacks.length == 0) {
            throw new IllegalArgumentException("Ingredient har ingen items!");
        }
        Item item = stacks[0].getItem();
        return Objects.requireNonNull(item.builtInRegistryHolder().key().location());
    }
}