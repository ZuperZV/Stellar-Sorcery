package net.zuperz.stellar_sorcery.block.entity.renderer;

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
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.custom.LunarInfuserBlock;
import net.zuperz.stellar_sorcery.block.entity.custom.LunarInfuserBlockEntity;
import org.joml.Matrix4f;

import java.util.List;

public class LunarInfuserBlockEntityRenderer implements BlockEntityRenderer<LunarInfuserBlockEntity> {

    private static final int SEGMENTS = 8;
    private static final int UPDATE_INTERVAL = 20;
    private final float[] offsetsX = new float[SEGMENTS];
    private final float[] offsetsZ = new float[SEGMENTS];
    private long lastUpdateTick = -1;

    public static final ModelLayerLocation MAGIC_AURA_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "magic_aura"), "main");

    private final ModelPart magicPlane;

    private Item lastRenderedItem = Items.AIR;
    private int fadeTicks = 0;

    public LunarInfuserBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.magicPlane = context.bakeLayer(MAGIC_AURA_LAYER).getChild("plane");
    }

    @Override
    public AABB getRenderBoundingBox(LunarInfuserBlockEntity blockEntity) {
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
    public void render(LunarInfuserBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack,
                       MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {

        long gameTime = pBlockEntity.getLevel().getGameTime();

        ItemStack input = pBlockEntity.inventory.getStackInSlot(0);
        ResourceLocation texture = null;

        Item currentItem = input.getItem();

        if (currentItem != lastRenderedItem) {
            fadeTicks = 0;
            lastRenderedItem = currentItem;
        } else if (!input.isEmpty()) {
            fadeTicks++;
        }

        float fadeAlpha = Mth.clamp((fadeTicks + pPartialTick) / 240.0f, 0.0f, 1.0f);

        if (pBlockEntity.getBlockState().getValue(LunarInfuserBlock.CRAFTING)) {
            texture = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/entity/magic_aura.png");
        }

        if (texture != null && fadeAlpha > 0.01f) {
            pPoseStack.pushPose();

            pPoseStack.translate(0.5, 1.01, 0.5);
            pPoseStack.mulPose(Axis.YP.rotationDegrees((pBlockEntity.getLevel().getGameTime() + pPartialTick) % 360));

            RenderType renderType = RenderType.entityTranslucent(texture);
            VertexConsumer buffer = pBufferSource.getBuffer(renderType);

            magicPlane.render(pPoseStack, buffer, pPackedLight, OverlayTexture.NO_OVERLAY);

            /*RenderSystem.enableBlend();
            RenderSystem.defaultBlendFunc();

            RenderSystem.setShaderColor(1f, 1f, 1f, fadeAlpha);

            RenderSystem.disableBlend();
             */

            pPoseStack.popPose();
        }


        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();

        ItemStack centerStack = pBlockEntity.inventory.getStackInSlot(0);
        pPoseStack.pushPose();
        pPoseStack.translate(0.5f, 1.15f, 0.5f);
        pPoseStack.scale(0.5f, 0.5f, 0.5f);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(pBlockEntity.getRenderingRotation()));

        Block[] blockOptions = new Block[] {
                Blocks.GRASS_BLOCK, Blocks.MOSS_BLOCK, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT
        };

        int count = 5;
        double baseRadius = 0.75;

        for (int game = 0; game < count; game++) {
            double t = (gameTime + pPartialTick + game * 11) / 100.0;

            double disappearChance = Math.sin(t * 2 * Math.PI);
            if (disappearChance < -0.6) continue;

            double angle = (game / (double) count) * 2 * Math.PI + t;
            double radius = baseRadius + Math.sin(t * 1.5) * 0.05;

            double xOffset = Math.cos(angle) * radius;
            double zOffset = Math.sin(angle) * radius;

            double yOffset = 0.2 + (Math.sin(t * Math.PI * 2) + 1.0) * 0.5;

            float spin = (float) ((t * 360) % 360);

            float scale = 0.15f + (float) ((Math.sin(t * Math.PI * 2) + 1.0) * 0.065f);

            Block block = blockOptions[(game + (int)(gameTime / 40)) % blockOptions.length];
            ItemStack stack = new ItemStack(block);

            pPoseStack.pushPose();
            pPoseStack.translate(0.5 + xOffset, yOffset, 0.5 + zOffset);
            pPoseStack.scale(scale, scale, scale);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(spin));
            itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
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