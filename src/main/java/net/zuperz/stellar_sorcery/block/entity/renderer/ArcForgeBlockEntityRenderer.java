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
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.zuperz.stellar_sorcery.block.entity.custom.ArcForgeBlockEntity;
import net.zuperz.stellar_sorcery.component.CelestialData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;

public class ArcForgeBlockEntityRenderer implements BlockEntityRenderer<ArcForgeBlockEntity> {
    public ArcForgeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(ArcForgeBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack,
                       MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack stack = pBlockEntity.inventory.getStackInSlot(0);

        Block[] blockOptions = new Block[] {
                Blocks.COBBLESTONE, Blocks.CLAY
        };

        long gameTime = pBlockEntity.getLevel().getGameTime();
        int count = 3;
        double baseRadius = 0.75;

        for (int i = 0; i < count; i++) {
            double t = (gameTime + pPartialTick + i * 11) / 100.0;

            double disappearChance = Math.sin(t * 2 * Math.PI);
            if (disappearChance < -0.6) continue;

            double angle = (i / (double) count) * 2 * Math.PI + t;
            double radius = baseRadius + Math.sin(t * 1.5) * 0.05;

            double xOffset = Math.cos(angle) * radius;
            double zOffset = Math.sin(angle) * radius;

            double yOffset = 0.2 + (Math.sin(t * Math.PI * 2) + 1.0) * 0.5;

            float spin = (float) ((t * 360) % 360);

            float scale = 0.05f + (float) ((Math.sin(t * Math.PI * 2) + 1.0) * 0.065f);

            Block block = blockOptions[(i + (int)(gameTime / 40)) % blockOptions.length];
            ItemStack blockstack = new ItemStack(block);

            pPoseStack.pushPose();
            pPoseStack.translate(0.5 + xOffset, yOffset, 0.5 + zOffset);
            pPoseStack.scale(scale, scale, scale);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(spin));
            itemRenderer.renderStatic(blockstack, ItemDisplayContext.FIXED,
                    getLightLevel(pBlockEntity.getLevel(), pBlockEntity.getBlockPos()),
                    OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, pBlockEntity.getLevel(), 1);
            pPoseStack.popPose();
        }



        pPoseStack.pushPose();
        pPoseStack.translate(0.5f, 1.15f, 0.5f);
        pPoseStack.scale(0.5f, 0.5f, 0.5f);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(pBlockEntity.getRenderingRotation()));

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED, getLightLevel(pBlockEntity.getLevel(),
                pBlockEntity.getBlockPos()), OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, pBlockEntity.getLevel(), 1);
        pPoseStack.popPose();


        ItemStack input = pBlockEntity.inventory.getStackInSlot(0);

        //System.out.println("input: " + input.getComponents());

        if (input.has(ModDataComponentTypes.CELESTIAL.get())) {
            CelestialData data = input.get(ModDataComponentTypes.CELESTIAL.get());

            //System.out.println("Text: " + data);

            pPoseStack.pushPose();
            pPoseStack.translate(0.5f, 1.9f, 0.5f);
            pPoseStack.scale(0.5f, 0.5f, 0.5f);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(pBlockEntity.getRenderingRotation()));
            itemRenderer.renderStatic(data.getEmbeddedItem(), ItemDisplayContext.FIXED,
                    getLightLevel(pBlockEntity.getLevel(), pBlockEntity.getBlockPos()),
                    OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, pBlockEntity.getLevel(), 1);
            pPoseStack.popPose();
        }
    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}