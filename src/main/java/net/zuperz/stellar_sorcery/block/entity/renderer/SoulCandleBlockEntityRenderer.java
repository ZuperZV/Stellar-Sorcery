package net.zuperz.stellar_sorcery.block.entity.renderer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.entity.custom.SoulCandleBlockEntity;

import static net.zuperz.stellar_sorcery.block.custom.SoulCandleBlock.CRAFTING;

public class SoulCandleBlockEntityRenderer implements BlockEntityRenderer<SoulCandleBlockEntity> {

    public static final ModelLayerLocation MAGIC_AURA_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "magic_aura"), "main");

    private final ModelPart magicPlane;
    private final ModelPart bigMagicPlane;

    private int progress = 0;
    public Entity entity = null;

    public SoulCandleBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.magicPlane = context.bakeLayer(MAGIC_AURA_LAYER).getChild("plane");
        this.bigMagicPlane = context.bakeLayer(MAGIC_AURA_LAYER).getChild("plane");
    }

    @Override
    public AABB getRenderBoundingBox(SoulCandleBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();

        Vec3 min = new Vec3(
                pos.getX() - 2,
                pos.getY() - 2,
                pos.getZ() - 2
        );

        Vec3 max = new Vec3(
                pos.getX() + 2,
                pos.getY() + 2,
                pos.getZ() + 2
        );

        return new AABB(min, max);
    }

    public static LayerDefinition createMagicAuraLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        root.addOrReplaceChild("plane",
                CubeListBuilder.create()
                        .texOffs(0, 0)
                        .addBox(-24.0F, 0.0F, -24.0F, 48.0F, 0.1F, 48.0F),
                PartPose.offset(0.0F, -16.0F, 0.0F)
        );

        return LayerDefinition.create(mesh, 48, 48);
    }

    @Override
    public void render(SoulCandleBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack,
                       MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {
        // Entity //

        EntityRenderDispatcher entityRenderDispatcher = Minecraft.getInstance().getEntityRenderDispatcher();

        pPoseStack.pushPose();
        pPoseStack.translate(0.5f, 0.6f, 0.5f);
        pPoseStack.scale(0.35f, 0.35f, 0.35f);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(pBlockEntity.getRenderingRotation() + 135));

        if(pBlockEntity.entityLastSacrificed != null) {
            entity = pBlockEntity.entityLastSacrificed.create(Minecraft.getInstance().level);
        } else {
            entity = null;
        }

        if(entity != null) {
            entityRenderDispatcher.render(
                    entity,
                    0,
                    1.25,
                    0,
                    0, pPartialTick, pPoseStack, pBufferSource, pPackedLight);
        }

        pPoseStack.popPose();

        ResourceLocation texture = null;

        if (pBlockEntity.getBlockState().getValue(CRAFTING)) {
            texture = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/entity/magic_aura.png");
        }

        ResourceLocation bigTexture = null;

        if (pBlockEntity.getBlockState().getValue(CRAFTING)) {
            bigTexture = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/entity/2x_magic_aura.png");
        }

        if (pBlockEntity.getBlockState().getValue(CRAFTING)) {
            progress++;
        } else {
            progress = 0;
        }

        float fadeAlpha = Mth.clamp((float) progress / (pBlockEntity.maxProgress * 5), 0f, 1f);
        float bigFadeAlpha = Mth.clamp((float) progress / (pBlockEntity.maxProgress * 14), 0f, 1f);

        if (texture != null) {
            pPoseStack.pushPose();

            pPoseStack.translate(0.5, 1.01, 0.5);
            pPoseStack.mulPose(Axis.YP.rotationDegrees((pBlockEntity.getLevel().getGameTime() + pPartialTick) % 360));

            RenderType renderType = RenderType.entityTranslucent(texture);
            VertexConsumer buffer = pBufferSource.getBuffer(renderType);

            RenderSystem.enableBlend();
            int alpha = (int)(fadeAlpha * 255.0f) & 0xFF;
            int color = (alpha << 24) | 0xFFFFFF;

            magicPlane.render(pPoseStack, buffer, pPackedLight, OverlayTexture.NO_OVERLAY, color);

            RenderSystem.disableBlend();
            pPoseStack.popPose();
        }

        if (bigTexture != null) {
            pPoseStack.pushPose();

            pPoseStack.translate(0.5, 1.01, 0.5);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(-(pBlockEntity.getLevel().getGameTime() + pPartialTick) % 360));

            RenderType renderType = RenderType.entityTranslucent(bigTexture);
            VertexConsumer buffer = pBufferSource.getBuffer(renderType);

            RenderSystem.enableBlend();
            int alpha = (int)(bigFadeAlpha * 255.0f) & 0xFF;
            int color = (alpha << 24) | 0xFFFFFF;

            bigMagicPlane.render(pPoseStack, buffer, pPackedLight, OverlayTexture.NO_OVERLAY, color);

            RenderSystem.disableBlend();
            pPoseStack.popPose();
        }
    }
}