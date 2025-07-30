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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zuperz.stellar_sorcery.block.entity.custom.VitalStumpBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.StumpBlockEntity;
import net.zuperz.stellar_sorcery.recipes.ModRecipes;
import net.zuperz.stellar_sorcery.recipes.StumpRecipe;

import java.util.*;

public class StumpBlockEntityRenderer implements BlockEntityRenderer<StumpBlockEntity> {

    public StumpBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public AABB getRenderBoundingBox(StumpBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();

        Vec3 min = new Vec3(
                pos.getX() - 3,
                pos.getY() - 3,
                pos.getZ() - 3
        );

        Vec3 max = new Vec3(
                pos.getX() + 4,
                pos.getY() + 4,
                pos.getZ() + 4
        );

        return new AABB(min, max);
    }

    @Override
    public void render(StumpBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack,
                       MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        BlockPos nexusPos = pBlockEntity.getBlockPos();
        BlockPos linkedAltarPos = pBlockEntity.getSavedPos();
        Level level = pBlockEntity.getLevel();

        ItemStack stack = pBlockEntity.inventory.getStackInSlot(0);

        if (linkedAltarPos == null || level == null) {
            renderIdleItem(pPoseStack, pBufferSource, itemRenderer, stack, level, nexusPos, pBlockEntity.getRenderingRotation());
            return;
        }

        BlockEntity linkedEntity = level.getBlockEntity(linkedAltarPos);

        if (!(linkedEntity instanceof VitalStumpBlockEntity linkedAltar)) {
            renderIdleItem(pPoseStack, pBufferSource, itemRenderer, stack, level, nexusPos, pBlockEntity.getRenderingRotation());
            return;
        }

        SimpleContainer altarInventory = new SimpleContainer(linkedAltar.inventory.getSlots());
        for (int i = 0; i < linkedAltar.inventory.getSlots(); i++) {
            altarInventory.setItem(i, linkedAltar.inventory.getStackInSlot(i));
        }

        Optional<RecipeHolder<StumpRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(ModRecipes.STUMP_RECIPE_TYPE.get(),
                        new VitalStumpBlockEntity.BlockRecipeInput(linkedAltar.inventory.getStackInSlot(0), linkedAltar.getBlockPos()),
                        level);

        if (recipe.isEmpty()) {
            renderIdleItem(pPoseStack, pBufferSource, itemRenderer, stack, level, nexusPos, pBlockEntity.getRenderingRotation());
            return;
        }

        StumpRecipe altarRecipe = recipe.get().value();
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

        List<Vec3> waypoints = getWaypoints(start, end);

        Vec3 currentPos = interpolateWaypoints(waypoints, smoothProgress);

        poseStack.pushPose();
        poseStack.translate(currentPos.x - start.getX(), currentPos.y - start.getY(), currentPos.z - start.getZ());
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                getLightLevel(level, start), OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, level, 1);

        poseStack.popPose();
    }

    private boolean isIngredientUsedInRecipeForThisNexus(StumpBlockEntity nexus, StumpRecipe recipe) {
        BlockPos thisPos = nexus.getBlockPos();
        Level level = nexus.getLevel();
        if (level == null) return false;

        List<Ingredient> ingredientsToMatch = recipe.getAdditionalIngredients();

        Set<Ingredient> matchedIngredients = new HashSet<>();

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos checkPos = nexus.getSavedPos().offset(dx, 0, dz);
                BlockEntity be = level.getBlockEntity(checkPos);

                if (!(be instanceof StumpBlockEntity nearbyNexus)) continue;

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

    private Vec3 interpolateWaypoints(List<Vec3> points, float t) {
        if (points.size() < 2) return points.get(0);

        int segmentCount = points.size() - 1;
        float totalT = t * segmentCount;
        int index = Mth.floor(totalT);
        float localT = totalT - index;

        index = Mth.clamp(index, 0, segmentCount - 1);
        Vec3 start = points.get(index);
        Vec3 end = points.get(index + 1);

        float smooth = easeInOut(localT);
        double x = Mth.lerp(smooth, start.x, end.x);
        double y = Mth.lerp(smooth, start.y, end.y);
        double z = Mth.lerp(smooth, start.z, end.z);

        return new Vec3(x, y, z);
    }

    private Vec3 interpolateBetween(Vec3 start, Vec3 end, double t) {
        return new Vec3(
                Mth.lerp(t, start.x, end.x),
                Mth.lerp(t, start.y, end.y),
                Mth.lerp(t, start.z, end.z)
        );
    }

    private List<Vec3> getWaypoints(BlockPos start, BlockPos end) {
        List<Vec3> points = new ArrayList<>();

        Vec3 startPos = new Vec3(start.getX() + 0.5, start.getY() + 1.15, start.getZ() + 0.5);
        Vec3 endPos = new Vec3(end.getX() + 0.5, end.getY() + 1.15, end.getZ() + 0.5);


        points.add(startPos);

        //Vec3 at70 = interpolateBetween(startPos, endPos, 0.9);
        //at70 = at70.add(0, 0.05, 0);
        //points.add(at70);

        points.add(endPos);

        return points;
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