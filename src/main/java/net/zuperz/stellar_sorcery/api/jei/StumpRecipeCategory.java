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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.component.EssenceBottleData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.item.ModItems;
import net.zuperz.stellar_sorcery.recipes.StumpRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class StumpRecipeCategory implements IRecipeCategory<StumpRecipe> {
    public final static ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "stump");
    public static final RecipeType<StumpRecipe> RECIPE_TYPE = RecipeType.create(StellarSorcery.MOD_ID, "stump", StumpRecipe.class);

    public final static ResourceLocation SLOT_TEX =
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/magic_slot.png");

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated progress;
    private final IDrawableStatic slotDrawable;

    private final IDrawable dayIcon;
    private final IDrawable nightIcon;
    private final IDrawable bothIcon;

    private final IDrawable checkIcon;
    private final IDrawable arrowIcon;

    private final int width = 115;
    private final int height = 55;

    int centerX = 20;
    int centerY = height / 2 - 7;

    private static final int[][] SLOT_POSITIONS = new int[][]{
            {-19, 0},   // venstre
            {0, -19},   // op
            {19, 0},    // højre
            {0, 19},    // ned
            {-19, -19}, // øverste venstre hjørne
            {19, -19},  // øverste højre hjørne
            {19, 19},   // nederste højre hjørne
            {-19, 19}   // nederste venstre hjørne
    };

    public StumpRecipeCategory(IGuiHelper helper) {
        ResourceLocation ARROW = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/arrow.png");

        this.background = helper.createBlankDrawable(width, height);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.VITAL_STUMP.get()));

        IDrawableStatic progressDrawable = helper.drawableBuilder(ARROW, 0, 0, 23, 15)
                .setTextureSize(23, 15)
                .build();

        this.progress = helper.createAnimatedDrawable(progressDrawable, 200,
                IDrawableAnimated.StartDirection.LEFT, false);

        this.slotDrawable = helper.drawableBuilder(SLOT_TEX, 0, 0, 18, 18)
                .setTextureSize(18, 18).build();


        this.dayIcon = helper.drawableBuilder(
                ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/icon_day.png"),
                0, 0, 16, 16
        ).setTextureSize(16, 16).build();

        this.nightIcon = helper.drawableBuilder(
                ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/icon_night.png"),
                0, 0, 16, 16
        ).setTextureSize(16, 16).build();

        this.bothIcon = helper.drawableBuilder(
                ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/icon_both.png"),
                0, 0, 16, 16
        ).setTextureSize(16, 16).build();

        ResourceLocation CHECK_ICON = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/check_icon.png");
        this.checkIcon = helper.drawableBuilder(CHECK_ICON, 0, 0, 18, 18)
                .setTextureSize(18, 18).build();

        ResourceLocation ARROW_ICON = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/arrow_icon.png");
        this.arrowIcon = helper.drawableBuilder(ARROW_ICON, 0, 0, 18, 18)
                .setTextureSize(18, 18).build();
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
    public RecipeType<StumpRecipe> getRecipeType() {
        return JEIPlugin.STUMP_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.stellar_sorcery.astral_altar");
    }

    @Override
    public void draw(StumpRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {

        slotDrawable.draw(guiGraphics, centerX - 1, centerY - 1);

        for (int i = 1; i < recipe.getIngredients().size(); i++) {
            if (i > SLOT_POSITIONS.length) break;
            int offsetX = SLOT_POSITIONS[i - 1][0];
            int offsetY = SLOT_POSITIONS[i - 1][1];
            slotDrawable.draw(guiGraphics, centerX + offsetX - 1, centerY + offsetY - 1);
        }

        slotDrawable.draw(guiGraphics, 97 - 1, centerY - 1);

        this.progress.draw(guiGraphics, 63, centerY);

        // Time of Day
        if (recipe.fakeTimeOfDay.isPresent()) {

            int iconX = 97;
            int iconY = centerY - 20;

            switch (recipe.fakeTimeOfDay.get()) {
                case DAY -> dayIcon.draw(guiGraphics, iconX, iconY);
                case NIGHT -> nightIcon.draw(guiGraphics, iconX, iconY);
                case BOTH -> bothIcon.draw(guiGraphics, iconX, iconY);
            }

            if (mouseX >= iconX && mouseX <= iconX + 16 && mouseY >= iconY && mouseY <= iconY + 16) {
                guiGraphics.renderTooltip(Minecraft.getInstance().font,
                        Component.literal("Works at: " + recipe.fakeTimeOfDay.get().name()),
                        (int) mouseX, (int) mouseY);
            }
        }

        // Needs Block
        recipe.needsBlock.ifPresent(needs -> {
            if (needs) {
                int iconX = 76 - 19;
                int iconY = centerY + 20;
                checkIcon.draw(guiGraphics, iconX, iconY);

                if (mouseX >= iconX && mouseX <= iconX + 16 && mouseY >= iconY && mouseY <= iconY + 16) {
                    guiGraphics.renderTooltip(
                            Minecraft.getInstance().font,
                            Component.literal("Needs Block"),
                            (int) mouseX, (int) mouseY
                    );
                }
            }
        });

        // Arrow Block
        recipe.additionalBlock.ifPresent(needs -> {
            int iconX = 76 + 5;
            int iconY = centerY + 20;
            arrowIcon.draw(guiGraphics, iconX, iconY);
        });

        // recipeTime
        if (mouseX >= 63 && mouseX <= 63 + 23 && mouseY >= centerY && mouseY <= centerY + 15) {
            guiGraphics.renderTooltip(
                    Minecraft.getInstance().font,
                    Component.literal("Time: " + recipe.recipeTime / 20 + "s"),
                    (int) mouseX, (int) mouseY
            );
        }
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder,
                          StumpRecipe recipe,
                          @NotNull IFocusGroup focuses) {

        // Input center
        builder.addSlot(RecipeIngredientRole.INPUT, centerX, centerY)
                .addIngredients(recipe.getIngredients().get(0));

        // De andre ingredienser
        for (int i = 1; i < recipe.getIngredients().size(); i++) {
            if (i > SLOT_POSITIONS.length) break;

            var ingredient = recipe.getIngredients().get(i);
            int offsetX = SLOT_POSITIONS[i - 1][0];
            int offsetY = SLOT_POSITIONS[i - 1][1];

            boolean isEssenceBottle = Arrays.stream(ingredient.getItems())
                    .anyMatch(stack -> stack.getItem() == ModItems.ESSENCE_BOTTLE.get());

            if (isEssenceBottle) {
                ItemStack bottle = new ItemStack(ModItems.ESSENCE_BOTTLE.get());

                recipe.requiredEssenceType.ifPresent(str -> {
                    List<ResourceLocation> ids = Arrays.stream(str.split(","))
                            .map(ResourceLocation::tryParse)
                            .filter(Objects::nonNull)
                            .sorted(Comparator.comparing(ResourceLocation::toString))
                            .toList();

                    if (ids.size() == 3) {
                        ItemStack base = new ItemStack(BuiltInRegistries.ITEM.get(ids.get(0)));
                        ItemStack infusion1 = new ItemStack(BuiltInRegistries.ITEM.get(ids.get(1)));
                        ItemStack infusion2 = new ItemStack(BuiltInRegistries.ITEM.get(ids.get(2)));

                        EssenceBottleData data = new EssenceBottleData(base, infusion1, infusion2);
                        bottle.set(ModDataComponentTypes.ESSENCE_BOTTLE, data);
                    }
                });

                builder.addSlot(RecipeIngredientRole.INPUT, centerX + offsetX, centerY + offsetY)
                        .addItemStack(bottle);
            } else {
                builder.addSlot(RecipeIngredientRole.INPUT, centerX + offsetX, centerY + offsetY)
                        .addIngredients(ingredient);
            }
        }

        // Output
        builder.addSlot(RecipeIngredientRole.OUTPUT, 97, centerY)
                .addItemStack(recipe.output);


        // Additional block
        recipe.additionalBlock.ifPresent(block -> {
            builder.addSlot(RecipeIngredientRole.INPUT, 76 - 2, centerY + 20)
                    .addItemStack(new ItemStack(block));
        });

        // BlockOutput
        recipe.blockOutput.ifPresent(block -> {
            builder.addSlot(RecipeIngredientRole.OUTPUT, 97, centerY + 20)
                    .addItemStack(new ItemStack(block));
        });
    }
}