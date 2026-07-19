package net.zuperz.stellar_sorcery.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zuperz.stellar_sorcery.block.entity.custom.EssenceBoilerBlockEntity;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.fluid.ModFluidTypes;

// Credits to TurtyWurty
// Under MIT-License: https://github.com/DaRealTurtyWurty/1.20-Tutorial-Mod?tab=MIT-1-ov-file#readme
//
// And credits to Kaupenjoe
// Under MIT-License: https://github.com/Tutorials-By-Kaupenjoe/NeoForge-Course-121-Module-7/blob/main/src/main/java/net/kaupenjoe/mccourse/block/entity/renderer/TankBlockEntityRenderer.java

public class EssenceBoilerBlockEntityRenderer implements BlockEntityRenderer<EssenceBoilerBlockEntity> {
    private final ItemRenderer itemRenderer;
    float plusY = 0.1f;

    public EssenceBoilerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(EssenceBoilerBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack,
                       MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        Level level = pBlockEntity.getLevel();
        if (level == null) return;
        BlockPos pos = pBlockEntity.getBlockPos();

        pPoseStack.pushPose();

        RenderFluid(pBlockEntity, pPartialTick, pPoseStack, pBufferSource, level, pos);
        RenderItem(pBlockEntity, pPartialTick, pPoseStack, pBufferSource, pPackedOverlay, level, pos);

        pPoseStack.popPose();
    }

    private void RenderItem(EssenceBoilerBlockEntity pBlockEntity, float pPartialTick,
                            PoseStack pPoseStack, MultiBufferSource pBufferSource,
                            int pPackedOverlay, Level level, BlockPos pos) {

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0.79 + plusY, 0.5);
        pPoseStack.scale(0.6f, 0.6f, 0.6f);

        ItemStackHandler itemHandler = pBlockEntity.inventory;

        float rotationTime = level.getGameTime() + pPartialTick;
        float rotation = (rotationTime * 4.0f) % 360;

        float radius = 0.35f;
        int itemCount = Math.min(itemHandler.getSlots(), 3);

        if (itemCount == 0) {
            pPoseStack.popPose();
            return;
        }

        float fluidDisplayLevel = pBlockEntity.fluidDisplayLevel;

        float transitionFactor = Math.min(fluidDisplayLevel / 0.3f, 1.0f);

        for (int i = 0; i < itemCount; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);

            if (stack.isEmpty())
                continue;

            pPoseStack.pushPose();

            float angle = (rotation + (360f / itemCount) * i) * ((float) Math.PI / 180f);
            float dryAngle = (351.51562f + (360f / itemCount) * i) * ((float) Math.PI / 180f);

            float baseX = radius * (float) Math.cos(angle);
            float baseZ = radius * (float) Math.sin(angle);
            float dryBaseX = radius * (float) Math.cos(dryAngle);
            float dryBaseZ = radius * (float) Math.sin(dryAngle);

            float slotSeed = i * 31.7f;
            float time = (level.getGameTime() + pPartialTick + slotSeed) * 0.1f;

            float bobbingY = (float) Math.sin(time) * 0.05f;
            float jitterX = (float) Math.sin(time * 0.7f) * 0.05f;
            float jitterZ = (float) Math.cos(time * 0.7f) * 0.05f;

            // !FLUID
            float dryX = dryBaseX;
            float dryY = 0f;
            float dryZ = dryBaseZ;

            float dryRotY = 0f;
            float dryRotX = -90f;
            float dryYOffset = -0.13f;

            // FLUID
            float wetX = baseX + jitterX;
            float wetY = 0.15f + bobbingY;
            float wetZ = baseZ + jitterZ;

            float wetRotY = rotation * 0.35f;
            float wetRotX = 0f;
            float wetYOffset = 0f;

            // TRANSITION
            float x = Mth.lerp(transitionFactor, dryX, wetX);
            float y = Mth.lerp(transitionFactor, dryY, wetY);
            float z = Mth.lerp(transitionFactor, dryZ, wetZ);

            float rotY = Mth.lerp(transitionFactor, dryRotY, wetRotY);
            float rotX = Mth.lerp(transitionFactor, dryRotX, wetRotX);

            float yOffset = Mth.lerp(transitionFactor, dryYOffset, wetYOffset);

            // Apply position translation first
            pPoseStack.translate(x, y + yOffset, z);

            // Then apply rotations to the item itself
            pPoseStack.mulPose(Axis.YP.rotationDegrees(rotY));
            pPoseStack.mulPose(Axis.XP.rotationDegrees(rotX));


             itemRenderer.renderStatic(
                     stack,
                     ItemDisplayContext.GROUND,
                     getLightLevel(level, pos),
                     pPackedOverlay,
                     pPoseStack,
                     pBufferSource,
                     level,
                     1
             );
             pPoseStack.popPose();
         }
         pPoseStack.popPose();
     }

    private void RenderFluid(EssenceBoilerBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBufferSource, Level level, BlockPos pos) {
        pPoseStack.pushPose();

        EssenceBoilerBlockEntity.WobbleStyle wobbleStyle = pBlockEntity.lastWobbleStyle;
        if (wobbleStyle != null && pBlockEntity.getLevel() != null) {
            float wobbleProgress = ((float)(pBlockEntity.getLevel().getGameTime() - pBlockEntity.wobbleStartedAtTick) + pPartialTick) / wobbleStyle.duration;
            if (wobbleProgress >= 0.0F && wobbleProgress <= 1.0F) {
                if (wobbleStyle == EssenceBoilerBlockEntity.WobbleStyle.POSITIVE) {
                    float angle = -1.5F * (Mth.cos(wobbleProgress * (float)Math.PI * 2) + 0.5F) * Mth.sin(wobbleProgress * (float)Math.PI);
                    pPoseStack.rotateAround(Axis.XP.rotation(angle * 0.015625F), 0.5F, 0.0F, 0.5F);
                    pPoseStack.rotateAround(Axis.ZP.rotation(Mth.sin(wobbleProgress * (float)Math.PI * 2) * 0.015625F), 0.5F, 0.0F, 0.5F);
                } else {
                    float rotY = Mth.sin(-wobbleProgress * 3.0F * (float)Math.PI) * 0.125F;
                    float f2 = 1.0F - wobbleProgress;
                    pPoseStack.rotateAround(Axis.YP.rotation(rotY * f2), 0.5F, 0.0F, 0.5F);
                }
            }
        }

        FluidStack fluidStack = pBlockEntity.getFluidTank();

        if (!fluidStack.isEmpty()) {
            pBlockEntity.cachedFluid = fluidStack.copy();
            pBlockEntity.renderCachedFluid = true;
        }

        FluidStack renderFluid = pBlockEntity.renderCachedFluid ? pBlockEntity.cachedFluid : FluidStack.EMPTY;

        float targetFluidLevel = fluidStack.isEmpty()
                ? 0f
                : Mth.clamp(fluidStack.getAmount() / 1000f - 0.2f, 0f, 1f);

        float fluidIncrement = 0.01f;

        if (pBlockEntity.fluidDisplayLevel < targetFluidLevel) {
            pBlockEntity.fluidDisplayLevel = Math.min(pBlockEntity.fluidDisplayLevel + fluidIncrement, targetFluidLevel);
        } else if (pBlockEntity.fluidDisplayLevel > targetFluidLevel) {
            pBlockEntity.fluidDisplayLevel = Math.max(pBlockEntity.fluidDisplayLevel - fluidIncrement, targetFluidLevel);
        }

        if (pBlockEntity.fluidDisplayLevel <= 0.0001f) {
            pBlockEntity.renderCachedFluid = false;
            pBlockEntity.cachedFluid = FluidStack.EMPTY;
        }

        plusY = -0.2f + pBlockEntity.fluidDisplayLevel * 0.5f;

        if (!renderFluid.isEmpty()) {

            IClientFluidTypeExtensions fluidType = IClientFluidTypeExtensions.of(renderFluid.getFluid());

            ResourceLocation stillTexture = fluidType.getStillTexture(renderFluid);

            TextureAtlasSprite sprite = Minecraft.getInstance()
                    .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                    .apply(stillTexture);

            int color = fluidType.getTintColor(renderFluid.getFluid().defaultFluidState(), level, pos);

            if (renderFluid.getFluid().getFluidType() == ModFluidTypes.POTION_FLUID_TYPE.get()) {
                PotionContents contents =
                        renderFluid.getOrDefault(ModDataComponentTypes.POTION_CONTENTS, PotionContents.EMPTY);

                if (contents != PotionContents.EMPTY) {
                    color = contents.getColor();
                }
            }

            RenderType renderType = RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS);
            VertexConsumer builder = pBufferSource.getBuffer(renderType);

            float xMin = 0.10f, xMax = 0.90f;
            float zMin = 0.10f, zMax = 0.90f;
            float y = 0.79f + plusY;

            drawQuad(
                    builder,
                    pPoseStack,
                    xMin, y, zMin,
                    xMax, y, zMax,
                    sprite.getU0(), sprite.getV0(),
                    sprite.getU1(), sprite.getV1(),
                    getLightLevel(level, pos),
                    color
            );

            plusY -= 0.4f;
        }

        pPoseStack.popPose();
    }

    private static void drawQuad(
            VertexConsumer builder,
            PoseStack poseStack,
            float x0, float y, float z0,
            float x1, float y2, float z1,
            float u0, float v0, float u1, float v1,
            int packedLight,
            int color
    ) {
        builder.addVertex(poseStack.last(), x0, y,  z0).setColor(color).setUv(u0, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(poseStack.last(), 0f, 1f, 0f);
        builder.addVertex(poseStack.last(), x0, y2, z1).setColor(color).setUv(u0, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(poseStack.last(), 0f, 1f, 0f);
        builder.addVertex(poseStack.last(), x1, y2, z1).setColor(color).setUv(u1, v1).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(poseStack.last(), 0f, 1f, 0f);
        builder.addVertex(poseStack.last(), x1, y,  z0).setColor(color).setUv(u1, v0).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(poseStack.last(), 0f, 1f, 0f);
    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}