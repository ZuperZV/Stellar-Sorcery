package net.zuperz.stellar_sorcery.block.entity.renderer;

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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.zuperz.stellar_sorcery.block.custom.LightBeamEmitterBlock;
import net.zuperz.stellar_sorcery.block.entity.custom.LightBeamEmitterBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.LunarJarBlockEntity;
import net.zuperz.stellar_sorcery.capability.IFluidHandler.IHasFluidTank;
import org.joml.Matrix4f;

// Credits to TurtyWurty
// Under MIT-License: https://github.com/DaRealTurtyWurty/1.20-Tutorial-Mod?tab=MIT-1-ov-file#readme
//
// And credits to Kaupenjoe
// Under MIT-License: https://github.com/Tutorials-By-Kaupenjoe/NeoForge-Course-121-Module-7/blob/main/src/main/java/net/kaupenjoe/mccourse/block/entity/renderer/TankBlockEntityRenderer.java

public class LunarJarBlockEntityRenderer implements BlockEntityRenderer<LunarJarBlockEntity> {

    public LunarJarBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(LunarJarBlockEntity pBlockEntity, float partialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int packedOverlay) {
        FluidStack fluidStack = pBlockEntity.getFluid();
        if (fluidStack.isEmpty()) return;
        Level level = pBlockEntity.getLevel();
        if (level == null) return;

        if (!fluidStack.isEmpty()) {
            IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
            ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(fluidStack);
            if (stillTexture != null) {
                FluidState state = fluidStack.getFluid().defaultFluidState();
                TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
                int tintColor = fluidTypeExtensions.getTintColor(state, level, pBlockEntity.getBlockPos());

                VertexConsumer builder = pBuffer.getBuffer(RenderType.translucent());

                float height = (((float) pBlockEntity.getTank(null).getFluidInTank(0).getAmount() / pBlockEntity.getTank(null).getTankCapacity(0)) * 0.625f) + 0.25f;

                float min = 3f / 16f + 0.02f;
                float max = 13f / 16f - 0.02f;
                float zFront = 3.01f / 16f + 0.02f;

                // Top
                drawQuad(builder, pPoseStack, min, height, min, max, height, max, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), pPackedLight, tintColor);

                // Bottom
                drawQuad(builder, pPoseStack, min, 0, min, max, 0, max, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), pPackedLight, tintColor);

                // (north)
                pPoseStack.pushPose();
                drawQuad(builder, pPoseStack, min, 0, zFront, max, height, zFront, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), pPackedLight, tintColor);
                pPoseStack.popPose();

                // (south)
                pPoseStack.pushPose();
                pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
                pPoseStack.translate(-1f, 0, -1f);
                drawQuad(builder, pPoseStack, min, 0, zFront, max, height, zFront, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), pPackedLight, tintColor);
                pPoseStack.popPose();

                // (west)
                pPoseStack.pushPose();
                pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
                pPoseStack.translate(-1f, 0, 0);
                drawQuad(builder, pPoseStack, min, 0, zFront, max, height, zFront, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), pPackedLight, tintColor);
                pPoseStack.popPose();

                // (east)
                pPoseStack.pushPose();
                pPoseStack.mulPose(Axis.YN.rotationDegrees(90));
                pPoseStack.translate(0, 0, -1f);
                drawQuad(builder, pPoseStack, min, 0, zFront, max, height, zFront, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), pPackedLight, tintColor);
                pPoseStack.popPose();
            }
        }
    }

    private static void drawVertex(VertexConsumer builder, PoseStack poseStack, float x, float y, float z, float u, float v, int packedLight, int color) {
        builder.addVertex(poseStack.last().pose(), x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setLight(packedLight)
                .setNormal(1, 0, 0);
    }

    private static void drawQuad(VertexConsumer builder, PoseStack poseStack, float x0, float y0, float z0, float x1, float y1, float z1, float u0, float v0, float u1, float v1, int packedLight, int color) {
        drawVertex(builder, poseStack, x0, y0, z0, u0, v0, packedLight, color);
        drawVertex(builder, poseStack, x0, y1, z1, u0, v1, packedLight, color);
        drawVertex(builder, poseStack, x1, y1, z1, u1, v1, packedLight, color);
        drawVertex(builder, poseStack, x1, y0, z0, u1, v0, packedLight, color);
    }
}