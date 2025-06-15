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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralAltarBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralNexusBlockEntity;

public class AstralNexusBlockEntityRenderer implements BlockEntityRenderer<AstralNexusBlockEntity> {
    public AstralNexusBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(AstralNexusBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack,
                       MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {


        BlockPos nexusPos = pBlockEntity.getBlockPos();
        BlockPos linkedAltarPos = pBlockEntity.getSavedPos();
        Level level = pBlockEntity.getLevel();

        if (linkedAltarPos == null || level == null) return;

        BlockEntity linkedEntity = level.getBlockEntity(linkedAltarPos);
        if (!(linkedEntity instanceof AstralAltarBlockEntity linkedAltar)) {
            return;
        }

        ItemStack stack = pBlockEntity.inventory.getStackInSlot(0);

        if (stack.isEmpty()) {
            return;
        }

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        int current = pBlockEntity.progress;
        int max = pBlockEntity.maxProgress;
        float progress = max == 0 ? 0f : Mth.clamp((float) current / max, 0f, 1f);

        if (progress <= 0f) {
            pPoseStack.pushPose();
            pPoseStack.translate(0.5f, 1.15f, 0.5f);
            pPoseStack.scale(0.5f, 0.5f, 0.5f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(pBlockEntity.getRenderingRotation()));
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                    getLightLevel(level, nexusPos),
                    OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, level, 1);
            pPoseStack.popPose();
            return;
        }

        double startX = nexusPos.getX() + 0.5;
        double startY = nexusPos.getY() + 1.0;
        double startZ = nexusPos.getZ() + 0.5;

        double endX = linkedAltar.getBlockPos().getX() + 0.5;
        double endY = linkedAltar.getBlockPos().getY() + 1.15;
        double endZ = linkedAltar.getBlockPos().getZ() + 0.5;

        double currentX = Mth.lerp(progress, startX, endX);
        double currentY = Mth.lerp(progress, startY, endY);
        double currentZ = Mth.lerp(progress, startZ, endZ);

        System.out.printf("Interpolated position: (%.2f, %.2f, %.2f)%n", currentX, currentY, currentZ);

        pPoseStack.pushPose();
        pPoseStack.translate(
                currentX - nexusPos.getX(),
                currentY - nexusPos.getY(),
                currentZ - nexusPos.getZ()
        );

        pPoseStack.scale(0.5f, 0.5f, 0.5f);
        float rotation = (level.getGameTime() + pPartialTick) * 4f % 360;
        pPoseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                getLightLevel(level, nexusPos),
                OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, level, 1);
        pPoseStack.popPose();

    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}