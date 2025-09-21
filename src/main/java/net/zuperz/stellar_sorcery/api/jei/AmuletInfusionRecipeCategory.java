package net.zuperz.stellar_sorcery.api.jei;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.item.ModItems;
import net.zuperz.stellar_sorcery.recipes.AmuletEssenceRecipe;
import net.zuperz.stellar_sorcery.recipes.StumpRecipe;
import org.jetbrains.annotations.NotNull;

public class AmuletInfusionRecipeCategory implements IRecipeCategory<AmuletEssenceRecipe> {

    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "amulet_essence_recipe");
    public static final RecipeType<AmuletEssenceRecipe> RECIPE_TYPE =
            new RecipeType<>(UID, AmuletEssenceRecipe.class);

    public final static ResourceLocation SLOT_TEX =
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/magic_slot.png");

    private final IDrawable background;
    private final IDrawable icon;

    private final IDrawableStatic slotDrawable;
    private final IDrawableAnimated arrow;


    private final int width = 115;
    private final int height = 55;

    public AmuletInfusionRecipeCategory(IGuiHelper helper) {
        this.background = helper.createBlankDrawable(width, height);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.ESSENCE_BOILER.get()));

        this.slotDrawable = helper.drawableBuilder(SLOT_TEX, 0, 0, 18, 18)
                .setTextureSize(18, 18).build();

        ResourceLocation ARROW = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/arrow.png");

        IDrawableStatic arrowDrawable = helper.drawableBuilder(ARROW, 0, 0, 23, 15)
                .setTextureSize(23, 15)
                .build();

        this.arrow = helper.createAnimatedDrawable(arrowDrawable, 200,
                IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void draw(AmuletEssenceRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     GuiGraphics graphics, double mouseX, double mouseY) {

        for (int i = 0; i < recipe.getIngredients().size(); i++) {
            int x = 10 - 1;
            int y = height / 2 - 16 / 2 + i * 18 - 18 - 1;
            slotDrawable.draw(graphics, x, y);
        }

        slotDrawable.draw(graphics, 40 - 1, height / 2 - 16 / 2 - 1);

        slotDrawable.draw(graphics, 90 - 1, height / 2 - 16 / 2 - 1);

        arrow.draw(graphics, 61, height / 2 - 7);
    }


    @Override
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public RecipeType<AmuletEssenceRecipe> getRecipeType() {
        return JEIPlugin.AMULET_ESSENCE_RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.stellar_sorcery.essence_infusion_boiler");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder,
                          AmuletEssenceRecipe recipe,
                          @NotNull IFocusGroup focuses) {

        builder.addSlot(RecipeIngredientRole.INPUT, 10, height / 2 - 16 / 2 - 18)
                .addIngredients(Ingredient.of(recipe.getInput1()));

        builder.addSlot(RecipeIngredientRole.INPUT, 10, height / 2 - 16 / 2)
                .addIngredients(recipe.getInput2());

        builder.addSlot(RecipeIngredientRole.INPUT, 40, height / 2 - 16 / 2)
                .addItemStack(new ItemStack(ModItems.EMPTY_ESSENCE_AMULET.get()));

        builder.addSlot(RecipeIngredientRole.OUTPUT, 90, height / 2 - 16 / 2)
                .addItemStack(recipe.getOutput());
    }
}