package net.zuperz.stellar_sorcery.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralAltarBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralNexusBlockEntity;
import net.zuperz.stellar_sorcery.recipes.AstralAltarRecipe;
import net.zuperz.stellar_sorcery.recipes.ModRecipes;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class AstralNexusBlockEntityRenderer implements BlockEntityRenderer<AstralNexusBlockEntity> {

    public AstralNexusBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(AstralNexusBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack,
                       MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {

        BlockPos nexusPos = pBlockEntity.getBlockPos();
        BlockPos linkedAltarPos = pBlockEntity.getSavedPos();
        Level level = pBlockEntity.getLevel();
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        ItemStack stack = pBlockEntity.inventory.getStackInSlot(0);
        if (stack.isEmpty() || level == null || linkedAltarPos == null) {
            renderIdleItem(pPoseStack, pBufferSource, itemRenderer, stack, level, nexusPos, pBlockEntity.getRenderingRotation());
            return;
        }

        BlockEntity linkedEntity = level.getBlockEntity(linkedAltarPos);
        if (!(linkedEntity instanceof AstralAltarBlockEntity linkedAltar)) {
            renderIdleItem(pPoseStack, pBufferSource, itemRenderer, stack, level, nexusPos, pBlockEntity.getRenderingRotation());
            return;
        }

        SimpleContainer altarInventory = new SimpleContainer(linkedAltar.inventory.getSlots());
        for (int i = 0; i < linkedAltar.inventory.getSlots(); i++) {
            altarInventory.setItem(i, linkedAltar.inventory.getStackInSlot(i));
        }

        Optional<RecipeHolder<AstralAltarRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(ModRecipes.ASTRAL_ALTAR_RECIPE_TYPE.get(),
                        new AstralAltarBlockEntity.BlockRecipeInput(linkedAltar.inventory.getStackInSlot(0), linkedAltar.getBlockPos()),
                        level);

        if (recipe.isEmpty()) {
            renderIdleItem(pPoseStack, pBufferSource, itemRenderer, stack, level, nexusPos, pBlockEntity.getRenderingRotation());
            return;
        }

        AstralAltarRecipe altarRecipe = recipe.get().value();
        boolean isMatchedIngredient = isIngredientUsedInRecipeForThisNexus(pBlockEntity, altarRecipe);

        float interpolatedProgress = Mth.lerp(pPartialTick, pBlockEntity.clientProgress, pBlockEntity.progress);
        float progress = pBlockEntity.maxProgress == 0 ? 0f : Mth.clamp(interpolatedProgress / pBlockEntity.maxProgress, 0f, 1f);

        if (progress > 0f && isMatchedIngredient) {
            renderFlyingItem(pPoseStack, pBufferSource, itemRenderer, stack, level,
                    nexusPos, linkedAltar.getBlockPos(), progress, pBlockEntity.getRenderingRotation());
        } else {
            renderIdleItem(pPoseStack, pBufferSource, itemRenderer, stack, level, nexusPos, pBlockEntity.getRenderingRotation());
        }
    }

    private void renderIdleItem(PoseStack poseStack, MultiBufferSource bufferSource, ItemRenderer itemRenderer,
                                ItemStack stack, Level level, BlockPos pos, float rotation) {

        poseStack.pushPose();
        poseStack.translate(0.5f, 1.15f, 0.5f);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                getLightLevel(level, pos),
                OverlayTexture.NO_OVERLAY, poseStack, bufferSource, level, 1);

        poseStack.popPose();
    }

    private void renderFlyingItem(PoseStack poseStack, MultiBufferSource bufferSource, ItemRenderer itemRenderer,
                                  ItemStack stack, Level level, BlockPos start, BlockPos end,
                                  float progress, float rotation) {
        float smoothProgress = easeInOut(progress);

        double startX = start.getX() + 0.5;
        double startY = start.getY() + 1.15;
        double startZ = start.getZ() + 0.5;

        double endX = end.getX() + 0.5;
        double endY = end.getY() + 1.15;
        double endZ = end.getZ() + 0.5;

        double x = Mth.lerp(smoothProgress, startX, endX);
        double y = Mth.lerp(smoothProgress, startY, endY);
        double z = Mth.lerp(smoothProgress, startZ, endZ);

        poseStack.pushPose();
        poseStack.translate(x - start.getX(), y - start.getY(), z - start.getZ());
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                getLightLevel(level, start),
                OverlayTexture.NO_OVERLAY, poseStack, bufferSource, level, 1);

        poseStack.popPose();
    }

    private boolean isIngredientUsedInRecipeForThisNexus(AstralNexusBlockEntity nexus, AstralAltarRecipe recipe) {
        BlockPos thisPos = nexus.getBlockPos();
        Level level = nexus.getLevel();
        if (level == null) return false;

        List<Ingredient> ingredientsToMatch = recipe.additionalIngredients.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Set<Ingredient> matchedIngredients = new HashSet<>();

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos checkPos = nexus.getSavedPos().offset(dx, 0, dz);
                BlockEntity be = level.getBlockEntity(checkPos);

                if (!(be instanceof AstralNexusBlockEntity nearbyNexus)) continue;

                for (int slot = 0; slot < nearbyNexus.inventory.getSlots(); slot++) {
                    ItemStack stack = nearbyNexus.inventory.getStackInSlot(slot);
                    if (stack.isEmpty()) continue;

                    for (Ingredient ingredient : ingredientsToMatch) {
                        if (matchedIngredients.contains(ingredient)) continue;

                        if (ingredient.test(stack)) {
                            matchedIngredients.add(ingredient);

                            if (nearbyNexus.getBlockPos().equals(thisPos)) {
                                return true;
                            }

                            break;
                        }
                    }
                }
            }
        }
        return false;
    }

    private float easeInOut(float tp) {
        tp = Mth.clamp(tp, 0f, 1f);
        return tp * tp * (3f - 2f * tp);
    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}