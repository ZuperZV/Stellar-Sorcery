package net.zuperz.stellar_sorcery.datagen.custom;

import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementRewards;
import net.minecraft.advancements.AdvancementRequirements;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.RecipeUnlockedTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.recipes.RecipeBuilder;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.zuperz.stellar_sorcery.recipes.StarLightLunarInfuserRecipe;
import net.zuperz.stellar_sorcery.capability.RecipesHelper.TimeOfDay;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

public class StarLightLunarInfuserRecipeBuilder implements RecipeBuilder {

    private final RecipeCategory category;
    private final FluidStack output;
    public final Ingredient Ingredient;
    private Optional<TimeOfDay> timeOfDay = Optional.empty();
    private int time = 100;

    private final Map<String, Criterion<?>> criteria = new LinkedHashMap<>();
    @Nullable
    private String group;

    private StarLightLunarInfuserRecipeBuilder(RecipeCategory category, FluidStack output, net.minecraft.world.item.crafting.Ingredient ingredient) {
        this.category = category;
        this.output = output;
        this.Ingredient = ingredient;
    }

    public static StarLightLunarInfuserRecipeBuilder lunarInfuser(RecipeCategory category, FluidStack output, Ingredient Ingredient) {
        return new StarLightLunarInfuserRecipeBuilder(category, output, Ingredient);
    }

    public StarLightLunarInfuserRecipeBuilder timeOfDay(TimeOfDay timeOfDay) {
        this.timeOfDay = Optional.of(timeOfDay);
        return this;
    }

    public StarLightLunarInfuserRecipeBuilder recipeTime(int time) {
        this.time = time;
        return this;
    }

    public StarLightLunarInfuserRecipeBuilder group(@Nullable String group) {
        this.group = group;
        return this;
    }

    @Override
    public RecipeBuilder unlockedBy(String p_176496_, Criterion<?> p_301065_) {
        this.criteria.put("has_lunar_infuser", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.LIGHT_INFUSER.get()));
        return this;
    }

    @Override
    public Item getResult() {
        return Items.DIAMOND; // Placeholder (since the result is fluid)
    }

    @Override
    public void save(RecipeOutput recipeOutput, ResourceLocation id) {
        this.criteria.put("has_lunar_infuser", InventoryChangeTrigger.TriggerInstance.hasItems(ModBlocks.LIGHT_INFUSER.get()));

        ensureValid(id);

        String newPath = "lunar_infuser/" + Arrays.stream(Ingredient.getItems()).findFirst().map(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath()).orElse("");
        id = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, newPath);

        Advancement.Builder advancementBuilder = recipeOutput.advancement()
                .addCriterion("has_the_recipe", RecipeUnlockedTrigger.unlocked(id))
                .rewards(AdvancementRewards.Builder.recipe(id))
                .requirements(AdvancementRequirements.Strategy.OR);

        this.criteria.forEach(advancementBuilder::addCriterion);

        StarLightLunarInfuserRecipe recipe = new StarLightLunarInfuserRecipe(
                output,
                Ingredient,
                timeOfDay,
                time
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
}
