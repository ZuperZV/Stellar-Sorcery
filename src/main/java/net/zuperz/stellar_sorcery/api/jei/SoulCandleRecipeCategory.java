package net.zuperz.stellar_sorcery.api.jei;

import com.mojang.blaze3d.systems.RenderSystem;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.api.jei.custom.EntityDrawable;
import net.zuperz.stellar_sorcery.api.jei.custom.PlanetDrawable;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.capability.RecipesHelper.SoulCandleCommand;
import net.zuperz.stellar_sorcery.recipes.SoulCandleRecipe;
import net.zuperz.stellar_sorcery.util.ModTags;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class SoulCandleRecipeCategory implements IRecipeCategory<SoulCandleRecipe> {
    public static final ResourceLocation UID = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "altar");
    public static final RecipeType<SoulCandleRecipe> RECIPE_TYPE =
            RecipeType.create(StellarSorcery.MOD_ID, "altar", SoulCandleRecipe.class);

    private final IDrawable background;
    private final IDrawable icon;
    private final IDrawableAnimated progress;
    private final IDrawableStatic slotDrawable;

    private final IDrawable dayIcon;
    private final IDrawable nightIcon;
    private final IDrawable bothIcon;

    private int width = 155;
    private int height = 75;

    private final Map<String, Character> chalkVariants = new HashMap<>();

    public SoulCandleRecipeCategory(IGuiHelper helper) {
        ResourceLocation ARROW = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/arrow.png");

        this.background = helper.createBlankDrawable(width, height);
        this.icon = helper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModBlocks.SOUL_CANDLE.get()));

        IDrawableStatic progressDrawable = helper.drawableBuilder(ARROW, 0, 0, 23, 15)
                .setTextureSize(23, 15)
                .build();

        this.progress = helper.createAnimatedDrawable(progressDrawable, 200,
                IDrawableAnimated.StartDirection.LEFT, false);

        this.slotDrawable = helper.drawableBuilder(
                ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/magic_slot.png"),
                0, 0, 18, 18
        ).setTextureSize(18, 18).build();

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
    }

    @Override
    public RecipeType<SoulCandleRecipe> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("recipe_mods.stellar_sorcery.soul_candle");
    }

    @Override
    public void draw(SoulCandleRecipe recipe, IRecipeSlotsView recipeSlotsView,
                     GuiGraphics guiGraphics, double mouseX, double mouseY) {

        int LeftX = 35;
        int centerY = height / 2;

        int slotSize = 18;
        int margin = 2;
        int slotX = width - 60 - 21;

        var validIngredients = recipe.additionalIngredients.stream()
                .filter(opt -> opt.isPresent() && !opt.get().isEmpty())
                .toList();

        int count = validIngredients.size();
        if (count > 0) {
            int rows = 4;
            int cols = (count <= rows) ? 1 : 2;

            int slotsPerCol = (int) Math.ceil(count / (float) cols);
            int totalHeight = slotsPerCol * (slotSize + margin);

            int startY = centerY + 1 - totalHeight / 2;
            int yOffset = 0;

            for (int i = 0; i < count; i++) {
                int col = i / slotsPerCol;
                int row = i % slotsPerCol;

                int x = slotX + col * (slotSize + margin);
                int y = startY + row * (slotSize + margin) + yOffset;

                if (x == slotX + 21 - 1 && y - yOffset == height / 2 - 8 - 1 && recipe.entityType.isPresent()) {
                    yOffset += 20;
                    y += 20;
                }

                slotDrawable.draw(guiGraphics, x, y);
            }
        }

        slotDrawable.draw(guiGraphics, width - 1 - 17, centerY - 1 - 8);

        int rows = recipe.pattern.size();
        int cols = recipe.pattern.stream().mapToInt(String::length).max().orElse(1);

        int boxWidth = 75 - 5;
        int boxHeight = 75 - 5;

        float scaleX = (float) boxWidth / (cols * 16f);
        float scaleY = (float) boxHeight / (rows * 16f);
        float scale = Math.min(scaleX, scaleY);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(LeftX, centerY, 0);
        guiGraphics.pose().scale(scale, scale, 1);

        // Mønsteret
        float offsetX = -(cols * 16) / 2f;
        float offsetY = -(rows * 16) / 2f;

        for (int row = 0; row < rows; row++) {
            String line = recipe.pattern.get(row);
            for (int col = 0; col < line.length(); col++) {
                char symbol = line.charAt(col);
                if (symbol == '_') continue;

                Block block = recipe.blockMapping.getOrDefault(String.valueOf(symbol), null);
                if (block != null) {
                    if (block.defaultBlockState().is(ModTags.Blocks.CHALK_BLOCKS)) {
                        String key = row + "_" + col;

                        char letter = chalkVariants.computeIfAbsent(key, k ->
                                (char) ('a' + Minecraft.getInstance().level.random.nextInt(26))
                        );

                        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);

                        String blockName = id.getPath();

                        ResourceLocation tex = ResourceLocation.fromNamespaceAndPath(
                                StellarSorcery.MOD_ID,
                                "textures/block/" + blockName + "_" + letter + ".png"
                        );

                        int drawX = (int) (offsetX + col * 16);
                        int drawY = (int) (offsetY + row * 16);

                        RenderSystem.enableBlend();
                        guiGraphics.setColor(0f, 0f, 0f, 0.4f);
                        guiGraphics.blit(tex,
                                drawX + 1, drawY + 1,
                                0, 0,
                                16, 16,
                                16, 16
                        );
                        RenderSystem.disableBlend();

                        guiGraphics.setColor(1f, 1f, 1f, 1f);
                        guiGraphics.blit(tex,
                                drawX, drawY,
                                0, 0,
                                16, 16,
                                16, 16
                        );
                    }
                    else {
                        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);

                        String blockName = id.getPath();

                        ResourceLocation stackTex = ResourceLocation.fromNamespaceAndPath(
                                StellarSorcery.MOD_ID,
                                "textures/item/" + blockName + ".png"
                        );

                        guiGraphics.blit(stackTex,
                                (int) (offsetX + col * 16),
                                (int) (offsetY + row * 16),
                                0, 0,
                                16, 16,
                                16, 16);
                    }
                }
            }
        }

        guiGraphics.pose().popPose();

        // Progress pil (samme som før)
        this.progress.draw(guiGraphics, width - 42, centerY - 8);

        // Tooltip for tid
        if (mouseX >= width - 40 && mouseX <= width - 17 &&
                mouseY >= centerY - 8 && mouseY <= centerY + 7) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font,
                    Component.literal("Time: " + recipe.recipeTime / 20 + "s"),
                    (int) mouseX, (int) mouseY);
        }

        // Time of Day
        if (recipe.fakeTimeOfDay.isPresent()) {

            int iconX = width - 17;
            int iconY = 0;

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

        // Entity Type
        recipe.entityType.ifPresent(needs -> {
            int iconX = slotX + 21;
            int iconY = height / 2 - 8;

            slotDrawable.draw(guiGraphics, iconX- 1, iconY - 1);

            if (mouseX >= iconX && mouseX <= iconX + 16 && mouseY >= iconY && mouseY <= iconY + 16) {
                guiGraphics.renderTooltip(
                        Minecraft.getInstance().font,
                        Component.translatable(recipe.entityType.get().toString()),
                        (int) mouseX, (int) mouseY
                );
            }
        });

        for (SoulCandleCommand cmd : recipe.commands) {
            String executedCommand = cmd.getCommand();

            int commandSlotX = width - 17;
            int commandSlotY = height - slotSize;

            // Entity
            if (executedCommand.startsWith("/summon ")) {
                String[] parts = executedCommand.split(" ");
                if (parts.length >= 2) {
                    ResourceLocation entityId = ResourceLocation.tryParse(parts[1]);
                    if (entityId != null && BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
                        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);

                        slotDrawable.draw(guiGraphics, commandSlotX- 1, commandSlotY + 1);

                        if (mouseX >= commandSlotX && mouseX <= commandSlotX + 16 && mouseY >= commandSlotY && mouseY <= commandSlotY + 16) {
                            guiGraphics.renderTooltip(
                                    Minecraft.getInstance().font,
                                    Component.translatable(entityType.toString()),
                                    (int) mouseX, (int) mouseY
                            );
                        }
                    }
                }
            } else if (executedCommand.startsWith("/setblock ")) {
                String[] parts = executedCommand.split(" ");
                if (parts.length >= 4) {
                    String blockId = parts[4];
                    ResourceLocation blockRL = ResourceLocation.tryParse(blockId);
                    if (blockRL != null && BuiltInRegistries.BLOCK.containsKey(blockRL)) {
                        Block block = BuiltInRegistries.BLOCK.get(blockRL);
                        if (block != null) {
                            slotDrawable.draw(guiGraphics, commandSlotX- 1, commandSlotY + 1);

                            if (mouseX >= commandSlotX && mouseX <= commandSlotX + 16 && mouseY >= commandSlotY && mouseY <= commandSlotY + 16) {
                                guiGraphics.renderTooltip(
                                        Minecraft.getInstance().font,
                                        Component.translatable(block.getName().getString()),
                                        (int) mouseX, (int) mouseY
                                );
                            }
                        }
                    }
                }
            } else if (executedCommand.startsWith("spawn ")) {
                String[] parts = executedCommand.split(" ");
                if (parts.length >= 2) {
                    slotDrawable.draw(guiGraphics, commandSlotX- 1, commandSlotY + 1);

                    if (mouseX >= commandSlotX && mouseX <= commandSlotX + 16 && mouseY >= commandSlotY && mouseY <= commandSlotY + 16) {
                        guiGraphics.renderTooltip(
                                Minecraft.getInstance().font,
                                Component.literal("Planet: " + parts[1]),
                                (int) mouseX, (int) mouseY
                        );
                    }
                }
            }
        }
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
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
    public void setRecipe(@NotNull IRecipeLayoutBuilder builder,
                          SoulCandleRecipe recipe,
                          @NotNull IFocusGroup focuses) {

        builder.addSlot(RecipeIngredientRole.OUTPUT, width - 17, height / 2 - 8)
                .addItemStack(recipe.output);

        int centerY = height / 2 + 1;
        int entitySlotSize = 16;
        int slotSize = 18;
        int margin = 2;
        int slotX = width - 60 - 21;

        var validIngredients = recipe.additionalIngredients.stream()
                .filter(opt -> opt.isPresent() && !opt.get().isEmpty())
                .toList();

        int count = validIngredients.size();
        if (count > 0) {
            int rows = 4;
            int cols = (count <= rows) ? 1 : 2;

            int slotsPerCol = (int) Math.ceil(count / (float) cols);
            int totalHeight = slotsPerCol * (slotSize + margin);

            int startY = centerY - totalHeight / 2;
            int yOffset = 0;

            for (int i = 0; i < count; i++) {
                int col = i / slotsPerCol;
                int row = i % slotsPerCol;

                int x = slotX + col * (slotSize + margin);
                int y = startY + row * (slotSize + margin) + yOffset;

                if (x == slotX + 21 - 1 && y - yOffset == height / 2 - 8 - 1 && recipe.entityType.isPresent()) {
                    yOffset += 20;
                    y += 20;
                }

                builder.addSlot(RecipeIngredientRole.INPUT, x + 1, y + 1)
                        .addIngredients(validIngredients.get(i).get());
            }
        }


        // Input Entity
        recipe.entityType.ifPresent(entityType -> {
            Entity entity = entityType.create(Minecraft.getInstance().level);
            if (entity instanceof LivingEntity living) {

                float entityWidth = living.getBbWidth();
                float entityHeight = living.getBbHeight();

                float maxSize = Math.max(entityWidth, entityHeight);

                float scaleFloat = (entitySlotSize - 2) / maxSize;

                builder.addSlot(RecipeIngredientRole.INPUT, slotX + 21, height / 2 - 8)
                        .setOverlay(
                                new EntityDrawable(entitySlotSize, entitySlotSize,
                                        (EntityType<? extends LivingEntity>) entityType,
                                        (int) scaleFloat),
                                entitySlotSize / 2, entitySlotSize);
            }
        });

        // Output
        for (SoulCandleCommand cmd : recipe.commands) {
            String executedCommand = cmd.getCommand();

            int commandSlotX = width - 17;
            int commandSlotY = height - entitySlotSize;

            if (executedCommand.startsWith("/summon ")) {
                String[] parts = executedCommand.split(" ");
                if (parts.length >= 2) {
                    ResourceLocation entityId = ResourceLocation.tryParse(parts[1]);
                    if (entityId != null && BuiltInRegistries.ENTITY_TYPE.containsKey(entityId)) {
                        EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(entityId);
                        Entity entity = entityType.create(Minecraft.getInstance().level);
                        if (entity instanceof LivingEntity living) {

                            float width = living.getBbWidth();
                            float height = living.getBbHeight();

                            float maxSize = Math.max(width, height);

                            float scaleFloat = (entitySlotSize - 2) / maxSize;

                            builder.addSlot(RecipeIngredientRole.OUTPUT, commandSlotX, commandSlotY)
                                    .setOverlay(
                                            new EntityDrawable(entitySlotSize, entitySlotSize,
                                                    (EntityType<? extends LivingEntity>) entityType,
                                                    (int) scaleFloat),
                                            entitySlotSize / 2, entitySlotSize);
                        }
                    }
                }
            }

            else if (executedCommand.startsWith("/setblock ")) {
                String[] parts = executedCommand.split(" ");
                if (parts.length >= 4) {
                    String blockId = parts[4];
                    ResourceLocation blockRL = ResourceLocation.tryParse(blockId);
                    if (blockRL != null && BuiltInRegistries.BLOCK.containsKey(blockRL)) {
                        Block block = BuiltInRegistries.BLOCK.get(blockRL);
                        if (block != null) {
                            builder.addSlot(RecipeIngredientRole.OUTPUT, commandSlotX, commandSlotY)
                                    .addItemStack(new ItemStack(block));
                        }
                    }
                }
            }

            else if (executedCommand.startsWith("spawn ")) {
                String[] parts = executedCommand.split(" ");
                if (parts.length >= 2) {
                    System.out.println("Test fromNamespaceAndPath: " + "textures/sky/" + parts[1] + ".png");
                            builder.addSlot(RecipeIngredientRole.OUTPUT, commandSlotX, commandSlotY)
                                    .setOverlay(
                                            new PlanetDrawable(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/sky/" + parts[1] + ".png"),
                                                    14, 32, 8),
                                            1, 1);
                }
            }
        }
    }
}
