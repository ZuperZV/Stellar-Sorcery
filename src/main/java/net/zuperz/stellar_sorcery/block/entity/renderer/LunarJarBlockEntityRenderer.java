package net.zuperz.stellar_sorcery.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.zuperz.stellar_sorcery.block.entity.custom.LunarJarBlockEntity;
import net.zuperz.stellar_sorcery.client.rendering.glow.GlowRenderTypes;
import net.zuperz.stellar_sorcery.util.ModTags;

import java.util.ArrayList;
import java.util.List;

// Credits to TurtyWurty
// Under MIT-License: https://github.com/DaRealTurtyWurty/1.20-Tutorial-Mod?tab=MIT-1-ov-file#readme
//
// And credits to Kaupenjoe
// Under MIT-License: https://github.com/Tutorials-By-Kaupenjoe/NeoForge-Course-121-Module-7/blob/main/src/main/java/net/kaupenjoe/mccourse/block/entity/renderer/TankBlockEntityRenderer.java

public class LunarJarBlockEntityRenderer implements BlockEntityRenderer<LunarJarBlockEntity> {
    private static final List<LunarJarBlockEntity> GLOW_QUEUE = new ArrayList<>();

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
                float height = (((float) pBlockEntity.getTank(null).getFluidInTank(0).getAmount() / pBlockEntity.getTank(null).getTankCapacity(0)) * 0.625f) + 0.25f;

                float min = 3f / 16f + 0.02f;
                float max = 13f / 16f - 0.02f;
                float zFront = 3.01f / 16f + 0.02f;

                VertexConsumer builder = pBuffer.getBuffer(RenderType.translucent());
                renderFluidCuboid(builder, pPoseStack, min, max, zFront, height, sprite, pPackedLight, tintColor);

                if (isNoctilume(fluidStack)) {
                    GLOW_QUEUE.add(pBlockEntity);
                }
            }
        }
    }

    public static boolean hasGlowPending() {
        return !GLOW_QUEUE.isEmpty();
    }

    public static boolean renderGlowQueue(PoseStack matrices, Camera camera, Frustum frustum, MultiBufferSource vertexConsumers) {
        if (GLOW_QUEUE.isEmpty()) {
            return false;
        }

        Vec3 cameraPos = camera.getPosition();
        VertexConsumer glowBuilder = vertexConsumers.getBuffer(GlowRenderTypes.BLOOM_GLOW);
        boolean renderedAny = false;

        matrices.pushPose();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (LunarJarBlockEntity entity : GLOW_QUEUE) {
            Level level = entity.getLevel();
            if (level == null || !frustum.isVisible(new AABB(entity.getBlockPos()))) {
                continue;
            }

            FluidStack fluidStack = entity.getFluid();
            if (fluidStack.isEmpty() || !isNoctilume(fluidStack)) {
                continue;
            }

            IClientFluidTypeExtensions fluidTypeExtensions = IClientFluidTypeExtensions.of(fluidStack.getFluid());
            ResourceLocation stillTexture = fluidTypeExtensions.getStillTexture(fluidStack);
            if (stillTexture == null) {
                continue;
            }

            FluidState state = fluidStack.getFluid().defaultFluidState();
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(stillTexture);
            int tintColor = fluidTypeExtensions.getTintColor(state, level, entity.getBlockPos());
            float glowIntensity = getGlowIntensity(level, entity.getBlockPos());
            int glowColor = withScaledAlpha(tintColor, glowIntensity);
            float height = (((float) entity.getTank(null).getFluidInTank(0).getAmount() / entity.getTank(null).getTankCapacity(0)) * 0.625f) + 0.25f;

            float min = 3f / 16f + 0.02f;
            float max = 13f / 16f - 0.02f;
            float zFront = 3.01f / 16f + 0.02f;

            matrices.pushPose();
            BlockPos pos = entity.getBlockPos();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            renderFluidCuboid(glowBuilder, matrices, min, max, zFront, height, sprite, LightTexture.FULL_BRIGHT, glowColor);
            matrices.popPose();

            renderedAny = true;
        }

        GLOW_QUEUE.clear();
        matrices.popPose();
        return renderedAny;
    }

    private static void renderFluidCuboid(VertexConsumer builder, PoseStack poseStack, float min, float max, float zFront, float height, TextureAtlasSprite sprite, int packedLight, int color) {
        // Top
        drawQuad(builder, poseStack, min, height, min, max, height, max, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), packedLight, color);
        // Bottom
        drawQuad(builder, poseStack, min, 0, min, max, 0, max, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), packedLight, color);

        // North
        poseStack.pushPose();
        drawQuad(builder, poseStack, min, 0, zFront, max, height, zFront, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), packedLight, color);
        poseStack.popPose();

        // South
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180));
        poseStack.translate(-1f, 0, -1f);
        drawQuad(builder, poseStack, min, 0, zFront, max, height, zFront, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), packedLight, color);
        poseStack.popPose();

        // West
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(90));
        poseStack.translate(-1f, 0, 0);
        drawQuad(builder, poseStack, min, 0, zFront, max, height, zFront, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), packedLight, color);
        poseStack.popPose();

        // East
        poseStack.pushPose();
        poseStack.mulPose(Axis.YN.rotationDegrees(90));
        poseStack.translate(0, 0, -1f);
        drawQuad(builder, poseStack, min, 0, zFront, max, height, zFront, sprite.getU0(), sprite.getV0(), sprite.getU1(), sprite.getV1(), packedLight, color);
        poseStack.popPose();
    }

    private static boolean isNoctilume(FluidStack fluidStack) {
        return fluidStack.getFluid().defaultFluidState().is(ModTags.Fluids.NOCTILUME);
    }

    private static float getGlowIntensity(Level level, BlockPos pos) {
        int block = level.getBrightness(LightLayer.BLOCK, pos);
        int sky = level.getBrightness(LightLayer.SKY, pos);
        float ambient = Math.max(block, sky) / 15.0f;
        return Mth.clamp(1.0f - ambient * 0.85f, 0.15f, 1.0f);
    }

    private static int withScaledAlpha(int color, float alphaScale) {
        int alpha = (color >>> 24) & 0xFF;
        if (alpha == 0) {
            alpha = 0xFF;
        }
        int scaledAlpha = Mth.clamp((int) (alpha * alphaScale), 0, 0xFF);
        return (scaledAlpha << 24) | (color & 0x00FFFFFF);
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
