package net.zuperz.stellar_sorcery.block.entity.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zuperz.stellar_sorcery.block.entity.custom.ArcForgeBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.EssenceBoilerBlockEntity;
import net.zuperz.stellar_sorcery.component.CelestialData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;

// Credits to TurtyWurty
// Under MIT-License: https://github.com/DaRealTurtyWurty/1.20-Tutorial-Mod?tab=MIT-1-ov-file#readme
//
// And credits to Kaupenjoe
// Under MIT-License: https://github.com/Tutorials-By-Kaupenjoe/NeoForge-Course-121-Module-7/blob/main/src/main/java/net/kaupenjoe/mccourse/block/entity/renderer/TankBlockEntityRenderer.java

public class EssenceBoilerBlockEntityRenderer implements BlockEntityRenderer<EssenceBoilerBlockEntity> {
    private final ItemRenderer itemRenderer;

    public EssenceBoilerBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(EssenceBoilerBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack,
                       MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        Level level = pBlockEntity.getLevel();
        if (level == null) return;
        BlockPos pos = pBlockEntity.getBlockPos();
        ItemStackHandler itemHandler = pBlockEntity.inventory;

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

        // FLUID RENDERING
        FluidStack fluidStack = pBlockEntity.getFluidTank();
        if (!fluidStack.isEmpty()) {
            IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
            ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(fluidStack);
            if (stillTexture != null) {
                FluidState state = fluidStack.getFluid().defaultFluidState();
                TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
                int tintColor = fluidTypeExtensions.getTintColor(state, level, pos);

                float yOffset = 0.2f;

                VertexConsumer builder = pBufferSource.getBuffer(RenderType.translucent());

                float xMin = 0.10f, xMax = 0.90f, zMin = 0.10f, zMax = 0.90f;
                float yBase = 0.7f;

                float y00 = yBase + yOffset * 0.8f;
                float y01 = yBase + yOffset * 1.0f;
                float y11 = yBase + yOffset  * 0.6f;
                float y10 = yBase + yOffset * 0.4f;

                drawQuad(builder, pPoseStack, xMin, y00, zMin, xMax, y11, zMax,
                        sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(),
                        pPackedLight, tintColor);
            }
        }

        pPoseStack.popPose();

        // ITEMS RENDERING
        RenderSystem.enableBlend();
        RenderSystem.disableDepthTest();

        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 0.7, 0.5);
        pPoseStack.scale(0.3f, 0.3f, 0.3f);

        float rotation = pBlockEntity.getRenderingRotation();
        float radius = 0.4f;
        int itemCount = Math.min(itemHandler.getSlots(), 3);

        for (int i = 0; i < itemCount; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                pPoseStack.pushPose();

                float angle = (rotation + (360f / itemCount) * i) * (float) Math.PI / 180f;
                float baseX = radius * (float) Math.cos(angle);
                float baseZ = radius * (float) Math.sin(angle);

                float slotSeed = i * 31.7f;
                float time = (pBlockEntity.getLevel().getGameTime() + pPartialTick + slotSeed) * 0.1f;

                float bobbingY = (float) Math.sin(time) * 0.05f;
                float jitterX = (float) Math.sin(time * 0.7f) * 0.05f;
                float jitterZ = (float) Math.cos(time * 0.7f) * 0.05f;

                pPoseStack.translate(baseX + jitterX, 0.15 + bobbingY, baseZ + jitterZ);

                float itemRotation = rotation * 1.5f;
                pPoseStack.mulPose(Axis.YP.rotationDegrees(itemRotation));
                pPoseStack.mulPose(Axis.YP.rotationDegrees(90));

                itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                        getLightLevel(level, pos), pPackedOverlay, pPoseStack, pBufferSource, level, 1);

                pPoseStack.popPose();
            }
        }

        pPoseStack.popPose();

        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
    }


    private static void drawVertex(VertexConsumer builder, PoseStack poseStack, float x, float y, float z, float u, float v, int packedLight, int color) {
        builder.addVertex(poseStack.last().pose(), x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setLight(packedLight)
                .setNormal(1, 0, 0);
    }

    private static void drawQuad(VertexConsumer builder, PoseStack poseStack,
                                 float x0, float y0, float z0,
                                 float x1, float y1, float z1,
                                 float u0, float v0, float u1, float v1,
                                 int packedLight, int color) {
        drawVertex(builder, poseStack, x0, y0, z0, u0, v0, packedLight, color);
        drawVertex(builder, poseStack, x0, y1, z1, u0, v1, packedLight, color);
        drawVertex(builder, poseStack, x1, y1, z1, u1, v1, packedLight, color);
        drawVertex(builder, poseStack, x1, y0, z0, u1, v0, packedLight, color);
    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}