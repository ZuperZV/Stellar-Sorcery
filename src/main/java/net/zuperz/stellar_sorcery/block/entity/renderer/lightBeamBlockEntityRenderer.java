package net.zuperz.stellar_sorcery.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zuperz.stellar_sorcery.block.custom.LightBeamEmitterBlock;
import net.zuperz.stellar_sorcery.block.entity.custom.LightBeamEmitterBlockEntity;
import net.zuperz.stellar_sorcery.client.rendering.glow.GlowRenderTypes;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class lightBeamBlockEntityRenderer implements BlockEntityRenderer<LightBeamEmitterBlockEntity> {
    private static final List<GlowEntry> GLOW_QUEUE = new ArrayList<>();

    public lightBeamBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}
    int MAX_BEAM_TICKS = 40;

    private record GlowEntry(lightBeamBlockEntityRenderer renderer, LightBeamEmitterBlockEntity entity) {}

    @Override
    public void render(LightBeamEmitterBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = be.getLevel();
        if (level == null) return;

        if (be.beamLength <= 0) return;

        Direction facing = be.getBlockState().getValue(LightBeamEmitterBlock.FACING);

        Vec3 start = Vec3.atCenterOf(be.getBlockPos());
        Vec3 facingVec = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());
        Vec3 end = start.add(facingVec.scale(be.beamLength));

        float alpha = (float) be.beamTicksRemaining / MAX_BEAM_TICKS;

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);

        renderBeam(poseStack, buffer, start, end, alpha, RenderType.lightning());
        if (be.needsToBeNoctilume) {
            GLOW_QUEUE.add(new GlowEntry(this, be));
        }

        poseStack.popPose();
    }

    private void renderBeam(PoseStack poseStack, MultiBufferSource buffer,
                            Vec3 startPos, Vec3 endPos, float alpha, RenderType renderType) {
        VertexConsumer consumer = buffer.getBuffer(renderType);
        Matrix4f matrix = poseStack.last().pose();

        double dx = endPos.x - startPos.x;
        double dy = endPos.y - startPos.y;
        double dz = endPos.z - startPos.z;

        float baseThickness = 0.1F;

        float r = 0.45F;
        float g = 0.45F;
        float b = 0.5F;

        drawBeamSegment(matrix, consumer, dx, dy, dz, baseThickness, r, g, b, 0.4F * alpha);
        drawBeamSegment(matrix, consumer, dx, dy, dz, 0.04F, r + 0.05f, g + 0.05f, b + 0.05f, 0.9F * alpha);

        float pixel = 1.0F / 16.0F;

        drawCube(matrix, consumer, new Vec3(0, 0, 0), baseThickness + 0.01F, r, g, b, 0.3F * alpha);
        drawCube(matrix, consumer, new Vec3(0, 0, 0), baseThickness + pixel + 0.01f, r + 0.05f, g + 0.05f, b + 0.05f, 0.1F * alpha);

        drawCube(matrix, consumer, new Vec3(dx, dy, dz), baseThickness + 0.01F, r, g, b, 0.3F * alpha);
        drawCube(matrix, consumer, new Vec3(dx, dy, dz), baseThickness + pixel + 0.01F, r + 0.05f, g + 0.05f, b + 0.05f, 0.1F * alpha);
    }

    private void drawCube(Matrix4f matrix, VertexConsumer consumer,
                          Vec3 center, float halfSize,
                          float r, float g, float b, float a) {
        Vec3[] corners = new Vec3[8];
        int i = 0;
        for (int x = -1; x <= 1; x += 2) {
            for (int y = -1; y <= 1; y += 2) {
                for (int z = -1; z <= 1; z += 2) {
                    corners[i++] = center.add(
                            x * halfSize,
                            y * halfSize,
                            z * halfSize
                    );
                }
            }
        }

        int[][] faces = {
                {0,1,3,2}, // b
                {4,5,7,6}, // t
                {0,1,5,4}, // f
                {2,3,7,6}, // b
                {0,2,6,4}, // l
                {1,3,7,5}  // r
        };

        for (int[] face : faces) {
            addVertex(consumer, matrix, corners[face[0]], r, g, b, a);
            addVertex(consumer, matrix, corners[face[1]], r, g, b, a);
            addVertex(consumer, matrix, corners[face[2]], r, g, b, a);
            addVertex(consumer, matrix, corners[face[3]], r, g, b, a);

            addVertex(consumer, matrix, corners[face[3]], r, g, b, a);
            addVertex(consumer, matrix, corners[face[2]], r, g, b, a);
            addVertex(consumer, matrix, corners[face[1]], r, g, b, a);
            addVertex(consumer, matrix, corners[face[0]], r, g, b, a);
        }
    }

    private void drawBeamSegment(Matrix4f matrix, VertexConsumer consumer, double dx, double dy, double dz, float thickness, float r, float g, float b, float a) {
        Vec3 dir = new Vec3(dx, dy, dz).normalize();
        Vec3 up = Math.abs(dir.y) > 0.99 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0);
        Vec3 right = dir.cross(up).normalize().scale(thickness);
        up = right.cross(dir).normalize().scale(thickness);

        Vec3 start = new Vec3(0,0,0);
        Vec3 end = new Vec3(dx,dy,dz);

        Vec3[] cornersStart = new Vec3[] {
                start.subtract(right).subtract(up),
                start.add(right).subtract(up),
                start.add(right).add(up),
                start.subtract(right).add(up)
        };
        Vec3[] cornersEnd = new Vec3[] {
                end.subtract(right).subtract(up),
                end.add(right).subtract(up),
                end.add(right).add(up),
                end.subtract(right).add(up)
        };

        for (int i=0;i<4;i++) {
            int next = (i+1)%4;
            addVertex(consumer, matrix, cornersStart[i], r,g,b,a);
            addVertex(consumer, matrix, cornersEnd[i], r,g,b,a);
            addVertex(consumer, matrix, cornersEnd[next], r,g,b,a);
            addVertex(consumer, matrix, cornersStart[next], r,g,b,a);
        }
        // Top/bund
        for (int i=0;i<4;i++) {
            int next = (i+1)%4;
            addVertex(consumer, matrix, cornersStart[i], r,g,b,a);
            addVertex(consumer, matrix, cornersStart[next], r,g,b,a);
            addVertex(consumer, matrix, cornersStart[next], r,g,b,a);
            addVertex(consumer, matrix, cornersStart[i], r,g,b,a);

            addVertex(consumer, matrix, cornersEnd[i], r,g,b,a);
            addVertex(consumer, matrix, cornersEnd[next], r,g,b,a);
            addVertex(consumer, matrix, cornersEnd[next], r,g,b,a);
            addVertex(consumer, matrix, cornersEnd[i], r,g,b,a);
        }
    }

    private void addVertex(VertexConsumer consumer, Matrix4f matrix, Vec3 pos, float r, float g, float b, float a) {
        consumer.addVertex(matrix, (float) pos.x, (float) pos.y, (float) pos.z)
                .setColor(r, g, b, a);
    }

    @Override
    public boolean shouldRenderOffScreen(LightBeamEmitterBlockEntity blockEntity) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(LightBeamEmitterBlockEntity blockEntity) {
        return AABB.INFINITE;
    }

    public static boolean hasGlowPending() {
        return !GLOW_QUEUE.isEmpty();
    }

    public static boolean renderGlowQueue(PoseStack matrices, Camera camera, Frustum frustum, MultiBufferSource vertexConsumers) {
        if (GLOW_QUEUE.isEmpty()) {
            return false;
        }

        Vec3 cameraPos = camera.getPosition();
        boolean renderedAny = false;

        matrices.pushPose();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (GlowEntry entry : GLOW_QUEUE) {
            LightBeamEmitterBlockEntity be = entry.entity();
            Level level = be.getLevel();
            if (level == null || be.beamLength <= 0 || !be.needsToBeNoctilume || !frustum.isVisible(new AABB(be.getBlockPos()))) {
                continue;
            }

            Direction facing = be.getBlockState().getValue(LightBeamEmitterBlock.FACING);
            Vec3 start = Vec3.atCenterOf(be.getBlockPos());
            Vec3 facingVec = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());
            Vec3 end = start.add(facingVec.scale(be.beamLength));
            float alpha = (float) be.beamTicksRemaining / entry.renderer().MAX_BEAM_TICKS;
            float lightFade = entry.renderer().getGlowIntensity(level, be.getBlockPos());

            matrices.pushPose();
            matrices.translate(start.x, start.y, start.z);
            entry.renderer().renderBeam(matrices, vertexConsumers, start, end, alpha * lightFade, GlowRenderTypes.BLOOM_LIGHTNING);
            matrices.popPose();

            renderedAny = true;
        }

        GLOW_QUEUE.clear();
        matrices.popPose();
        return renderedAny;
    }

    private float getGlowIntensity(Level level, BlockPos pos) {
        int block = level.getBrightness(LightLayer.BLOCK, pos);
        int sky = level.getBrightness(LightLayer.SKY, pos);
        float ambient = Math.max(block, sky) / 15.0f;
        return Mth.clamp(1.0f - ambient * 0.85f, 0.15f, 1.0f);
    }
}
