package net.zuperz.stellar_sorcery.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zuperz.stellar_sorcery.block.custom.LightBeamEmitterBlock;
import net.zuperz.stellar_sorcery.block.entity.custom.LightBeamEmitterBlockEntity;
import net.zuperz.stellar_sorcery.capability.IFluidHandler.IHasFluidTank;
import org.joml.Matrix4f;

public class lightBeamBlockEntityRenderer implements BlockEntityRenderer<LightBeamEmitterBlockEntity> {

    public lightBeamBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    private int cachedBeamLength = 0;
    private int beamTicksRemaining = 0;
    private static final int MAX_BEAM_TICKS = 40;

    @Override
    public void render(LightBeamEmitterBlockEntity be, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight, int packedOverlay) {
        Level level = be.getLevel();
        if (level == null) return;

        Direction facing = be.getBlockState().getValue(LightBeamEmitterBlock.FACING);
        BlockPos inputPos = be.getBlockPos().relative(facing.getOpposite());

        int currentLength = 0;
        boolean hasFluid = false;

        BlockEntity inputBE = level.getBlockEntity(inputPos);
        if (inputBE instanceof IHasFluidTank inputTank) {
            var handler = inputTank.getFluidHandler();
            hasFluid = handler.drain(
                    new net.neoforged.neoforge.fluids.FluidStack(
                            net.zuperz.stellar_sorcery.fluid.ModFluids.SOURCE_NOCTILUME.get(), 1
                    ),
                    net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction.SIMULATE
            ).getAmount() > 0;

            if (hasFluid) {
                BlockPos.MutableBlockPos current = be.getBlockPos().mutable();
                for (int i = 1; i < 32; i++) {
                    current.move(facing);
                    currentLength++;
                    if (!level.getBlockState(current).isAir()) break;
                }
            }
        }

        if (currentLength > 0) {
            cachedBeamLength = currentLength;
            beamTicksRemaining = MAX_BEAM_TICKS;
        } else if (beamTicksRemaining > 0) {
            beamTicksRemaining--;
        } else {
            cachedBeamLength = 0;
        }

        if (cachedBeamLength <= 0) return;

        float alpha = (float) beamTicksRemaining / MAX_BEAM_TICKS;

        Vec3 start = Vec3.atCenterOf(be.getBlockPos());
        Vec3 facingVec = new Vec3(facing.getStepX(), facing.getStepY(), facing.getStepZ());
        Vec3 end = start.add(facingVec.scale(cachedBeamLength));

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        renderBeam(poseStack, buffer, start, end, alpha);
        poseStack.popPose();
    }

    private void renderBeam(PoseStack poseStack, MultiBufferSource buffer,
                            Vec3 startPos, Vec3 endPos, float alpha) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
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

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}