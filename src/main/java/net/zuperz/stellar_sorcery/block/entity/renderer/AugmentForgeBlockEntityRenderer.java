package net.zuperz.stellar_sorcery.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zuperz.stellar_sorcery.block.entity.custom.AugmentForgeBlockEntity;

public class AugmentForgeBlockEntityRenderer implements BlockEntityRenderer<AugmentForgeBlockEntity> {

    public AugmentForgeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {}

    private Vec3 currentOffset = Vec3.ZERO;
    private Vec3 targetOffset = Vec3.ZERO;
    private float currentBaseY = -0.3f;
    private float targetBaseY = -0.3f;
    private float currentYaw = 0f;
    private float targetYaw = 0f;
    private long lastUpdateTime = 0;
    private final RandomSource random = RandomSource.create();

    private final float maxRadius = 0.4f;

    @Override
    public AABB getRenderBoundingBox(AugmentForgeBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        Vec3 min = new Vec3(pos.getX() - 3, pos.getY() - 3, pos.getZ() - 3);
        Vec3 max = new Vec3(pos.getX() + 4, pos.getY() + 4, pos.getZ() + 4);
        return new AABB(min, max);
    }

    @Override
    public void render(AugmentForgeBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null) return;

        float baseScale = 0.04f;

        Player player = level.getNearestPlayer(
                blockEntity.getBlockPos().getX() + 0.5,
                blockEntity.getBlockPos().getY() + 0.5,
                blockEntity.getBlockPos().getZ() + 0.5,
                5.0, false);

        if (player != null) {
            Vec3 playerPos = player.position();
            Vec3 blockCenter = Vec3.atCenterOf(blockEntity.getBlockPos());
            Vec3 dir = playerPos.subtract(blockCenter).normalize();

            targetOffset = dir.scale(maxRadius * 0.6f);
            targetBaseY = -0.1f;

            float desiredYaw = (float) Math.toDegrees(Math.atan2(dir.x, dir.z));

            float delta = Mth.wrapDegrees(desiredYaw - targetYaw);
            targetYaw += delta;
        }
        else {
            long time = level.getGameTime();
            if (time - lastUpdateTime > 40) {
                lastUpdateTime = time;
                targetOffset = new Vec3(
                        (random.nextFloat() - 0.5f) * 2 * maxRadius,
                        (random.nextFloat() - 0.5f) * 0.2f,
                        (random.nextFloat() - 0.5f) * 2 * maxRadius
                );
            }

            targetBaseY = -0.33f;
            targetYaw = (targetYaw + 0.1f) % 360f;
        }

        currentOffset = currentOffset.lerp(targetOffset, 0.002f);
        currentBaseY = Mth.lerp(0.02f, currentBaseY, targetBaseY);
        currentYaw = Mth.rotLerp(0.05f, currentYaw, targetYaw);

        double bob = Math.sin((level.getGameTime() + partialTick) * 0.1f) * 0.05f;

        RenderItem(blockEntity, partialTick, poseStack, bufferSource, level,
                currentBaseY + (float) bob, baseScale, player);
    }

    private void RenderItem(AugmentForgeBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                            MultiBufferSource bufferSource, Level level, float baseY, float baseScale, Player player) {

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        EntityRenderDispatcher entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher();

        ItemStack stack = blockEntity.getItem(0);
        if (stack.isEmpty()) return;

        poseStack.pushPose();

        poseStack.translate(0.5 + currentOffset.x, 1.2 + currentOffset.y, 0.5 + currentOffset.z);

        poseStack.mulPose(Axis.YP.rotationDegrees(currentYaw));

        int light = getLightLevel(level, blockEntity.getBlockPos().above());

        if (stack.getItem() instanceof ArmorItem armorItem) {
            ArmorStand fakeStand = new ArmorStand(level, 0, 0, 0);
            fakeStand.setInvisible(true);
            fakeStand.setNoBasePlate(true);

            EquipmentSlot slot = armorItem.getEquipmentSlot();

            float yOffset = switch (slot) {
                case FEET -> 1.7f;
                case LEGS -> 1.45f;
                case CHEST -> 1f;
                case HEAD -> 0.4f;
                default -> 1.0f;
            };

            fakeStand.setItemSlot(slot, stack);

            poseStack.pushPose();
            poseStack.translate(0, -1 + baseY, 0);
            poseStack.scale(baseScale + 0.6f, baseScale + 0.6f, baseScale + 0.6f);

            entityRenderer.render(fakeStand, 0.0, baseY + yOffset, 0.0, 0.0f,
                    partialTick, poseStack, bufferSource, light);
            poseStack.popPose();

        } else {
            poseStack.translate(0, baseY, 0);
            poseStack.scale(baseScale + 0.95f, baseScale + 0.95f, baseScale + 0.95f);

            itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, light,
                    OverlayTexture.NO_OVERLAY, poseStack, bufferSource, level, 0);
        }

        poseStack.popPose();
    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}