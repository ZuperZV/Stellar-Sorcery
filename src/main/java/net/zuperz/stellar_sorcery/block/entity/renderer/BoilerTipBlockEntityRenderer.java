package net.zuperz.stellar_sorcery.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.zuperz.stellar_sorcery.block.entity.custom.BoilerTipBlockEntity;

public class BoilerTipBlockEntityRenderer implements BlockEntityRenderer<BoilerTipBlockEntity> {

    public BoilerTipBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(BoilerTipBlockEntity entity, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int light, int overlay) {

        Level level = entity.getLevel();
        if (level == null) return;

        FluidStack fluid = entity.getFluidStack();
        System.out.println("fluid: " + fluid);
        if (fluid.isEmpty()) return;

        poseStack.pushPose();

        renderFluidColumn(entity, fluid, level, poseStack, buffer, light);

        poseStack.popPose();
    }

    private BlockPos findTop(Level level, BlockPos pos) {
        return pos;
    }

    private BlockPos findBottom(Level level, BlockPos pos, BoilerTipBlockEntity e) {
        System.out.println("e.targetPosEntity: " + e.targetPosEntity);
        return e.targetPosEntity;
    }

    private void renderFluidColumn(BoilerTipBlockEntity entity, FluidStack fluid,
                                   Level level, PoseStack poseStack,
                                   MultiBufferSource buffer, int light) {

        BlockPos top = findTop(level, entity.getBlockPos());
        BlockPos bottom = findBottom(level, entity.getBlockPos(), entity);

        int blocks = top.getY() - bottom.getY() + 1;

        VertexConsumer vc = buffer.getBuffer(RenderType.translucent());

        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(ext.getStillTexture(fluid));

        int color = ext.getTintColor(fluid.getFluid().defaultFluidState(), level, entity.getBlockPos());

        poseStack.pushPose();

        for (int i = 0; i < blocks; i++) {

            float y0 = i;
            float y1 = i + 1;

            drawQuad(vc, poseStack,
                    0.2f, y0, 0.2f,
                    0.8f, y1, 0.8f,
                    sprite, light, color);
        }

        poseStack.popPose();
    }

    private void drawQuad(VertexConsumer vc, PoseStack poseStack,
                          float x0, float y, float z0,
                          float x1, float y2, float z1,
                          TextureAtlasSprite sprite,
                          int light,
                          int color) {

        vc.addVertex(poseStack.last(), x0, y, z0)
                .setColor(color)
                .setUv(sprite.getU0(), sprite.getV0())
                .setLight(light)
                .setNormal(0, 1, 0);

        vc.addVertex(poseStack.last(), x0, y2, z1)
                .setColor(color)
                .setUv(sprite.getU0(), sprite.getV1())
                .setLight(light)
                .setNormal(0, 1, 0);

        vc.addVertex(poseStack.last(), x1, y2, z1)
                .setColor(color)
                .setUv(sprite.getU1(), sprite.getV1())
                .setLight(light)
                .setNormal(0, 1, 0);

        vc.addVertex(poseStack.last(), x1, y, z0)
                .setColor(color)
                .setUv(sprite.getU1(), sprite.getV0())
                .setLight(light)
                .setNormal(0, 1, 0);
    }
}