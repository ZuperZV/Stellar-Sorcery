package net.zuperz.stellar_sorcery.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Transformation;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.model.pipeline.TransformingVertexPipeline;
import net.neoforged.neoforge.client.model.pipeline.VertexConsumerWrapper;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import net.zuperz.stellar_sorcery.block.entity.custom.GlowingBlockEntity;
import net.zuperz.stellar_sorcery.block.light.IGlowingBlock;
import net.zuperz.stellar_sorcery.client.rendering.glow.GlowRenderTypes;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

// Based on Legend of Steve's GlowingBlockRenderer by DeadlyDiamond (CC0 1.0).
// https://github.com/Deadlydiamond98/LegendOfSteveRewrite/blob/master/src/main/java/net/deadlydiamond/legend_of_steve/client/rendering/block/glowing/GlowingBlockRenderer.java

public class GlowingBlockRenderer implements BlockEntityRenderer<GlowingBlockEntity> {
    private static final float FLUID_GLOW_INTENSITY = 0.5f;
    private static final List<GlowingBlockEntity> GLOWING_BLOCKS = new ArrayList<>();

    public GlowingBlockRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(GlowingBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertexConsumers, int light, int overlay) {
        if (entity.getLevel() == null) return;
        if (entity.getType() != ModBlockEntities.GLOWING_BLOCK_BE.get()) return;
        GLOWING_BLOCKS.add(entity);
    }

    public static boolean hasPending() {
        return !GLOWING_BLOCKS.isEmpty();
    }

    public static boolean renderAll(PoseStack matrices, Camera camera, Frustum frustum, BlockRenderDispatcher blockRenderer, MultiBufferSource vertexConsumers) {
        if (GLOWING_BLOCKS.isEmpty()) return false;

        Vec3 cameraPos = camera.getPosition();
        boolean renderedAny = false;

        matrices.pushPose();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        VertexConsumer glowingLayer = vertexConsumers.getBuffer(GlowRenderTypes.BLOOM_GLOW);
        for (GlowingBlockEntity entity : GLOWING_BLOCKS) {
            if (frustum.isVisible(new AABB(entity.getBlockPos()))) {
                renderedAny |= renderGlowing(entity, matrices, glowingLayer, blockRenderer, cameraPos);
            }
        }

        GLOWING_BLOCKS.clear();
        matrices.popPose();

        return renderedAny;
    }

    private static boolean renderGlowing(GlowingBlockEntity entity, PoseStack matrices, VertexConsumer glowingLayer, BlockRenderDispatcher blockRenderer, Vec3 cameraPos) {
        Level level = entity.getLevel();
        if (level == null) return false;

        BlockState state = entity.getBlockState();
        FluidState fluidState = state.getFluidState();

        float glowScale = 1.0f;
        boolean stopZFighting = true;
        if (state.getBlock() instanceof IGlowingBlock glowingBlock) {
            glowScale = glowingBlock.getGlowScale(level, entity.getBlockPos());
            stopZFighting = glowingBlock.stopZFighting();
        }

        if (!fluidState.isEmpty() && state.getBlock() instanceof LiquidBlock) {
            renderFluid(entity.getBlockPos(), level, state, fluidState, blockRenderer, glowingLayer, cameraPos);
            return true;
        }

        renderModel(entity, matrices, glowingLayer, blockRenderer, glowScale, stopZFighting);
        return true;
    }

    private static void renderModel(GlowingBlockEntity entity, PoseStack matrices, VertexConsumer glowingLayer, BlockRenderDispatcher blockRenderer, float glowScale, boolean stopZFighting) {
        matrices.pushPose();
        alignBlock(entity, matrices);

        float scaleOffset = (float) (((Math.sin(entity.getBlockPos().getCenter().length() * 2) * 0.05f) + 0.05f) * 0.5f) + 0.01f;
        float scale = stopZFighting ? glowScale + scaleOffset : glowScale;

        matrices.translate(0.5, 0.5, 0.5);
        matrices.scale(scale, scale, scale);
        matrices.translate(-0.5, -0.5, -0.5);

        renderBlock(entity, matrices, glowingLayer, blockRenderer);
        matrices.popPose();
    }

    private static void alignBlock(GlowingBlockEntity entity, PoseStack matrices) {
        BlockPos pos = entity.getBlockPos();
        matrices.translate(pos.getX(), pos.getY(), pos.getZ());
    }

    private static void renderBlock(GlowingBlockEntity entity, PoseStack matrices, VertexConsumer vertexConsumer, BlockRenderDispatcher blockRenderer) {
        Level level = entity.getLevel();
        if (level == null) return;

        BlockState state = entity.getBlockState();
        BakedModel model = blockRenderer.getBlockModel(state);
        blockRenderer.getModelRenderer().tesselateBlock(
                level,
                model,
                state,
                entity.getBlockPos(),
                matrices,
                vertexConsumer,
                true,
                RandomSource.create(),
                state.getSeed(entity.getBlockPos()),
                OverlayTexture.NO_OVERLAY
        );
    }

    private static void renderFluid(BlockPos pos, Level level, BlockState state, FluidState fluidState, BlockRenderDispatcher blockRenderer, VertexConsumer vertexConsumer, Vec3 cameraPos) {
        int originX = pos.getX() & ~15;
        int originY = pos.getY() & ~15;
        int originZ = pos.getZ() & ~15;

        Matrix4f transform = new Matrix4f().translation(
                (float) (originX - cameraPos.x),
                (float) (originY - cameraPos.y),
                (float) (originZ - cameraPos.z)
        );

        VertexConsumer transformed = new TransformingVertexPipeline(vertexConsumer, new Transformation(transform));
        VertexConsumer dimmedFluidGlow = new FluidGlowVertexConsumer(transformed, FLUID_GLOW_INTENSITY);
        blockRenderer.renderLiquid(pos, level, dimmedFluidGlow, state, fluidState);
    }

    private static final class FluidGlowVertexConsumer extends VertexConsumerWrapper {
        private final float intensity;

        private FluidGlowVertexConsumer(VertexConsumer parent, float intensity) {
            super(parent);
            this.intensity = intensity;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            int scaledR = Math.max(0, Math.min(255, (int) (r * intensity)));
            int scaledG = Math.max(0, Math.min(255, (int) (g * intensity)));
            int scaledB = Math.max(0, Math.min(255, (int) (b * intensity)));
            int scaledA = Math.max(0, Math.min(255, (int) (a * intensity)));
            parent.setColor(scaledR, scaledG, scaledB, scaledA);
            return this;
        }
    }
}
