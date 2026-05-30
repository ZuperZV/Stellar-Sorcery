package net.zuperz.stellar_sorcery.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.block.custom.BoilerTipBlock;
import net.zuperz.stellar_sorcery.block.entity.custom.AugmentForgeBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.BoilerTipBlockEntity;
import org.jetbrains.annotations.Nullable;

public class BoilerTipBlockEntityRenderer implements BlockEntityRenderer<BoilerTipBlockEntity> {
    private static final float MIN_X = 6.5f / 16f;
    private static final float MAX_X = 9.5f / 16f;
    private static final float INNER_MIN_Z = 12f / 16f;
    private static final float INNER_MAX_Z = 14f / 16f;
    private static final float TOP_MIN_Y = 11f / 16f;
    private static final float TOP_MAX_Y = 13f / 16f;
    private static final float TOP_CAP_MAX_Z = 17f / 16f;
    private static final float BOTTOM_MIN_Y = 8f / 16f;

    public BoilerTipBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BoilerTipBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {
        Level level = entity.getLevel();
        if (level == null) {
            return;
        }

        FluidStack fluid = entity.getFluidStack();
        if (fluid.isEmpty()) {
            return;
        }

        renderFluidColumn(entity, fluid, level, poseStack, buffer);
    }

    @Override
    public AABB getRenderBoundingBox(BoilerTipBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        Level level = blockEntity.getLevel();

        if (level == null) {
            return new AABB(pos).inflate(1.0);
        }

        BlockPos bottom = findBottom(level, pos);

        if (bottom == null) {
            return new AABB(pos).inflate(2.0);
        }

        BlockPos top = findTop(pos);

        int difference = 5 + top.getY() - bottom.getY();

        return new AABB(
                pos.getX() - difference, pos.getY() - difference, pos.getZ() - difference,
                pos.getX() + difference, pos.getY() + difference, pos.getZ() + difference
        );
    }

    private BlockPos findTop(BlockPos pos) {
        return pos;
    }

    @Nullable
    private BlockPos findBottom(Level level, BlockPos pos) {
        BlockPos checkPos = pos.below();

        while (checkPos.getY() > level.getMinBuildHeight()) {
            BlockState state = level.getBlockState(checkPos);

            if (state.is(ModBlocks.ESSENCE_BOILER)) {
                return checkPos;
            }

            if (!state.isAir()) {
                return null;
            }

            checkPos = checkPos.below();
        }

        return null;
    }

    private void renderFluidColumn(BoilerTipBlockEntity entity, FluidStack fluid, Level level,
                                   PoseStack poseStack, MultiBufferSource buffer) {
        BlockPos top = findTop(entity.getBlockPos());
        BlockPos bottom = findBottom(level, top);
        if (bottom == null) {
            return;
        }

        int totalBlocks = top.getY() - bottom.getY() + 1;
        if (totalBlocks < 2) {
            return;
        }

        IClientFluidTypeExtensions fluidType = IClientFluidTypeExtensions.of(fluid.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(fluidType.getStillTexture(fluid));

        int color = fluidType.getTintColor(fluid.getFluid().defaultFluidState(), level, top);
        Direction facing = entity.getBlockState().getValue(BoilerTipBlock.FACING);
        VertexConsumer builder = buffer.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));

        for (int i = 0; i < totalBlocks; i++) {
            BlockPos currentPos = bottom.above(i);
            int blockLight = LevelRenderer.getLightColor(level, currentPos);

            poseStack.pushPose();
            poseStack.translate(0, currentPos.getY() - top.getY(), 0);
            applyBlockFacingRotation(poseStack, facing);

            if (currentPos.equals(top)) {
                renderTopSegment(builder, poseStack, sprite, blockLight, color);
            } else if (currentPos.equals(bottom)) {
                renderBottomSegment(builder, poseStack, sprite, blockLight, color);
            } else {
                renderMiddleSegment(builder, poseStack, sprite, blockLight, color);
            }

            poseStack.popPose();
        }
    }

    private void applyBlockFacingRotation(PoseStack poseStack, Direction facing) {
        poseStack.translate(0.5f, 0.5f, 0.5f);

        switch (facing) {
            case SOUTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180f));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(270f));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(90f));
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(90f));
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(-90f));
            default -> {
            }
        }

        poseStack.translate(-0.5f, -0.5f, -0.5f);
    }

    private void renderTopSegment(VertexConsumer builder, PoseStack poseStack,
                                  TextureAtlasSprite sprite, int light, int color) {
        renderBox(
                builder,
                poseStack,
                MIN_X, 0f, INNER_MIN_Z,
                MAX_X, TOP_MAX_Y, INNER_MAX_Z,
                sprite,
                light,
                color,
                true,
                false,
                true,
                false,
                true,
                true
        );

        renderBox(
                builder,
                poseStack,
                MIN_X, TOP_MIN_Y, INNER_MAX_Z,
                MAX_X, TOP_MAX_Y, TOP_CAP_MAX_Z,
                sprite,
                light,
                color,
                true,
                true,
                false,
                true,
                true,
                true
        );
    }

    private void renderMiddleSegment(VertexConsumer builder, PoseStack poseStack,
                                     TextureAtlasSprite sprite, int light, int color) {
        renderBox(
                builder,
                poseStack,
                MIN_X, 0f, INNER_MIN_Z,
                MAX_X, 1f, INNER_MAX_Z,
                sprite,
                light,
                color,
                false,
                false,
                true,
                true,
                true,
                true
        );
    }

    private void renderBottomSegment(VertexConsumer builder, PoseStack poseStack,
                                     TextureAtlasSprite sprite, int light, int color) {
        renderBox(
                builder,
                poseStack,
                MIN_X, BOTTOM_MIN_Y, INNER_MIN_Z,
                MAX_X, 1f, INNER_MAX_Z,
                sprite,
                light,
                color,
                false,
                true,
                true,
                true,
                true,
                true
        );
    }

    private void renderBox(VertexConsumer builder, PoseStack poseStack,
                           float x0, float y0, float z0,
                           float x1, float y1, float z1,
                           TextureAtlasSprite sprite, int light, int color,
                           boolean renderUp, boolean renderDown,
                           boolean renderNorth, boolean renderSouth,
                           boolean renderWest, boolean renderEast) {
        float u0 = sprite.getU0();
        float u1 = sprite.getU1();
        float v0 = sprite.getV0();
        float v1 = sprite.getV1();

        if (renderNorth) {
            addNorthFace(builder, poseStack, x0, y0, z0, x1, y1, u0, v0, u1, v1, light, color);
        }
        if (renderSouth) {
            addSouthFace(builder, poseStack, x0, y0, z1, x1, y1, u0, v0, u1, v1, light, color);
        }
        if (renderWest) {
            addWestFace(builder, poseStack, x0, y0, z0, z1, y1, u0, v0, u1, v1, light, color);
        }
        if (renderEast) {
            addEastFace(builder, poseStack, x1, y0, z0, z1, y1, u0, v0, u1, v1, light, color);
        }
        if (renderUp) {
            addUpFace(builder, poseStack, x0, y1, z0, x1, z1, u0, v0, u1, v1, light, color);
        }
        if (renderDown) {
            addDownFace(builder, poseStack, x0, y0, z0, x1, z1, u0, v0, u1, v1, light, color);
        }
    }

    private void addNorthFace(VertexConsumer builder, PoseStack poseStack,
                              float x0, float y0, float z,
                              float x1, float y1,
                              float u0, float v0, float u1, float v1,
                              int light, int color) {
        builder.addVertex(poseStack.last(), x0, y0, z)
                .setColor(color)
                .setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0f, 0f, -1f);
        builder.addVertex(poseStack.last(), x0, y1, z)
                .setColor(color)
                .setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0f, 0f, -1f);
        builder.addVertex(poseStack.last(), x1, y1, z)
                .setColor(color)
                .setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0f, 0f, -1f);
        builder.addVertex(poseStack.last(), x1, y0, z)
                .setColor(color)
                .setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0f, 0f, -1f);
    }

    private void addSouthFace(VertexConsumer builder, PoseStack poseStack,
                              float x0, float y0, float z,
                              float x1, float y1,
                              float u0, float v0, float u1, float v1,
                              int light, int color) {
        builder.addVertex(poseStack.last(), x1, y0, z)
                .setColor(color)
                .setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0f, 0f, 1f);
        builder.addVertex(poseStack.last(), x1, y1, z)
                .setColor(color)
                .setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0f, 0f, 1f);
        builder.addVertex(poseStack.last(), x0, y1, z)
                .setColor(color)
                .setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0f, 0f, 1f);
        builder.addVertex(poseStack.last(), x0, y0, z)
                .setColor(color)
                .setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0f, 0f, 1f);
    }

    private void addWestFace(VertexConsumer builder, PoseStack poseStack,
                             float x, float y0, float z0,
                             float z1, float y1,
                             float u0, float v0, float u1, float v1,
                             int light, int color) {
        builder.addVertex(poseStack.last(), x, y0, z1)
                .setColor(color)
                .setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(-1f, 0f, 0f);
        builder.addVertex(poseStack.last(), x, y1, z1)
                .setColor(color)
                .setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(-1f, 0f, 0f);
        builder.addVertex(poseStack.last(), x, y1, z0)
                .setColor(color)
                .setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(-1f, 0f, 0f);
        builder.addVertex(poseStack.last(), x, y0, z0)
                .setColor(color)
                .setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(-1f, 0f, 0f);
    }

    private void addEastFace(VertexConsumer builder, PoseStack poseStack,
                             float x, float y0, float z0,
                             float z1, float y1,
                             float u0, float v0, float u1, float v1,
                             int light, int color) {
        builder.addVertex(poseStack.last(), x, y0, z0)
                .setColor(color)
                .setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(1f, 0f, 0f);
        builder.addVertex(poseStack.last(), x, y1, z0)
                .setColor(color)
                .setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(1f, 0f, 0f);
        builder.addVertex(poseStack.last(), x, y1, z1)
                .setColor(color)
                .setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(1f, 0f, 0f);
        builder.addVertex(poseStack.last(), x, y0, z1)
                .setColor(color)
                .setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(1f, 0f, 0f);
    }

    private void addUpFace(VertexConsumer builder, PoseStack poseStack,
                           float x0, float y, float z0,
                           float x1, float z1,
                           float u0, float v0, float u1, float v1,
                           int light, int color) {
        builder.addVertex(poseStack.last(), x0, y, z0)
                .setColor(color)
                .setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0f, 1f, 0f);
        builder.addVertex(poseStack.last(), x0, y, z1)
                .setColor(color)
                .setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0f, 1f, 0f);
        builder.addVertex(poseStack.last(), x1, y, z1)
                .setColor(color)
                .setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0f, 1f, 0f);
        builder.addVertex(poseStack.last(), x1, y, z0)
                .setColor(color)
                .setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0f, 1f, 0f);
    }

    private void addDownFace(VertexConsumer builder, PoseStack poseStack,
                             float x0, float y, float z0,
                             float x1, float z1,
                             float u0, float v0, float u1, float v1,
                             int light, int color) {
        builder.addVertex(poseStack.last(), x0, y, z1)
                .setColor(color)
                .setUv(u0, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0f, -1f, 0f);
        builder.addVertex(poseStack.last(), x0, y, z0)
                .setColor(color)
                .setUv(u0, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0f, -1f, 0f);
        builder.addVertex(poseStack.last(), x1, y, z0)
                .setColor(color)
                .setUv(u1, v0)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0f, -1f, 0f);
        builder.addVertex(poseStack.last(), x1, y, z1)
                .setColor(color)
                .setUv(u1, v1)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(0f, -1f, 0f);
    }
}
