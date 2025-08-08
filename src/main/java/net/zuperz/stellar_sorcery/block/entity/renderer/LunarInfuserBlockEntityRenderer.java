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

import java.util.List;

public class LunarInfuserBlockEntityRenderer implements BlockEntityRenderer<LunarInfuserBlockEntity> {
    public static final ResourceLocation BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");
    public static final int MAX_RENDER_Y = 1024;

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

        long i = pBlockEntity.getLevel().getGameTime();
        List<LunarInfuserBlockEntity.BeaconBeamSection> list = pBlockEntity.getBeamSections();
        int j = 0;

        for (int k = 0; k < list.size(); k++) {
            LunarInfuserBlockEntity.BeaconBeamSection beaconblockentity$beaconbeamsection = list.get(k);
            renderBeaconBeam(
                    pPoseStack,
                    pBufferSource,
                    pPartialTick,
                    i,
                    j,
                    k == list.size() - 1 ? MAX_RENDER_Y : beaconblockentity$beaconbeamsection.getHeight(),
                    beaconblockentity$beaconbeamsection.getColor()
            );
            j += beaconblockentity$beaconbeamsection.getHeight();
        }


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

        float progress = pBlockEntity.maxProgress == 0 ? 0f : Mth.clamp((float) pBlockEntity.progress / pBlockEntity.maxProgress, 0f, 1f);

        if (progress < 0) {
            renderFlyingItem(pPoseStack, pBufferSource, itemRenderer, centerStack, pBlockEntity.getLevel(),
                    pBlockEntity.getBlockPos(), pBlockEntity.getBlockPos(), progress, pBlockEntity.getRenderingRotation());
        } else {
            itemRenderer.renderStatic(centerStack, ItemDisplayContext.FIXED,
                    getLightLevel(pBlockEntity.getLevel(), pBlockEntity.getBlockPos()),
                    OverlayTexture.NO_OVERLAY, pPoseStack, pBufferSource, pBlockEntity.getLevel(), 1);
            pPoseStack.popPose();
        }


        Block[] blockOptions = new Block[] {
                Blocks.GRASS_BLOCK, Blocks.MOSS_BLOCK, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT
        };

        long gameTime = pBlockEntity.getLevel().getGameTime();
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

    private static void renderBeaconBeam(
            PoseStack p_112185_, MultiBufferSource p_112186_, float p_112188_, long p_112190_, int p_112191_, int p_112192_, int p_350457_
    ) {
        renderBeaconBeam(p_112185_, p_112186_, BEAM_LOCATION, p_112188_, 1.0F, p_112190_, p_112191_, p_112192_, p_350457_, 0.2F, 0.25F);
    }

    public static void renderBeaconBeam(
            PoseStack p_112177_,
            MultiBufferSource p_112178_,
            ResourceLocation p_350504_,
            float p_112179_,
            float p_350618_,
            long p_112180_,
            int p_112181_,
            int p_112182_,
            int p_350915_,
            float p_350604_,
            float p_350669_
    ) {
        int i = p_112181_ + p_112182_;
        p_112177_.pushPose();
        p_112177_.translate(0.5, 0.0, 0.5);
        float f = (float)Math.floorMod(p_112180_, 40) + p_112179_;
        float f1 = p_112182_ < 0 ? f : -f;
        float f2 = Mth.frac(f1 * 0.2F - (float)Mth.floor(f1 * 0.1F));
        p_112177_.pushPose();
        p_112177_.mulPose(Axis.YP.rotationDegrees(f * 2.25F - 45.0F));
        float f3 = 0.0F;
        float f5 = 0.0F;
        float f6 = -p_350604_;
        float f7 = 0.0F;
        float f8 = 0.0F;
        float f9 = -p_350604_;
        float f10 = 0.0F;
        float f11 = 1.0F;
        float f12 = -1.0F + f2;
        float f13 = (float)p_112182_ * p_350618_ * (0.5F / p_350604_) + f12;
        renderPart(
                p_112177_,
                p_112178_.getBuffer(RenderType.beaconBeam(p_350504_, false)),
                p_350915_,
                p_112181_,
                i,
                0.0F,
                p_350604_,
                p_350604_,
                0.0F,
                f6,
                0.0F,
                0.0F,
                f9,
                0.0F,
                1.0F,
                f13,
                f12
        );
        p_112177_.popPose();
        f3 = -p_350669_;
        float f4 = -p_350669_;
        f5 = -p_350669_;
        f6 = -p_350669_;
        f10 = 0.0F;
        f11 = 1.0F;
        f12 = -1.0F + f2;
        f13 = (float)p_112182_ * p_350618_ + f12;
        renderPart(
                p_112177_,
                p_112178_.getBuffer(RenderType.beaconBeam(p_350504_, true)),
                FastColor.ARGB32.color(32, p_350915_),
                p_112181_,
                i,
                f3,
                f4,
                p_350669_,
                f5,
                f6,
                p_350669_,
                p_350669_,
                p_350669_,
                0.0F,
                1.0F,
                f13,
                f12
        );
        p_112177_.popPose();
    }

    private static void renderPart(
            PoseStack p_112156_,
            VertexConsumer p_112157_,
            int p_112162_,
            int p_112163_,
            int p_351014_,
            float p_112158_,
            float p_112159_,
            float p_112160_,
            float p_112161_,
            float p_112164_,
            float p_112165_,
            float p_112166_,
            float p_112167_,
            float p_112168_,
            float p_112169_,
            float p_112170_,
            float p_112171_
    ) {
        PoseStack.Pose posestack$pose = p_112156_.last();
        renderQuad(
                posestack$pose, p_112157_, p_112162_, p_112163_, p_351014_, p_112158_, p_112159_, p_112160_, p_112161_, p_112168_, p_112169_, p_112170_, p_112171_
        );
        renderQuad(
                posestack$pose, p_112157_, p_112162_, p_112163_, p_351014_, p_112166_, p_112167_, p_112164_, p_112165_, p_112168_, p_112169_, p_112170_, p_112171_
        );
        renderQuad(
                posestack$pose, p_112157_, p_112162_, p_112163_, p_351014_, p_112160_, p_112161_, p_112166_, p_112167_, p_112168_, p_112169_, p_112170_, p_112171_
        );
        renderQuad(
                posestack$pose, p_112157_, p_112162_, p_112163_, p_351014_, p_112164_, p_112165_, p_112158_, p_112159_, p_112168_, p_112169_, p_112170_, p_112171_
        );
    }

    private static void renderQuad(
            PoseStack.Pose p_323955_,
            VertexConsumer p_112122_,
            int p_112127_,
            int p_112128_,
            int p_350566_,
            float p_112123_,
            float p_112124_,
            float p_112125_,
            float p_112126_,
            float p_112129_,
            float p_112130_,
            float p_112131_,
            float p_112132_
    ) {
        addVertex(p_323955_, p_112122_, p_112127_, p_350566_, p_112123_, p_112124_, p_112130_, p_112131_);
        addVertex(p_323955_, p_112122_, p_112127_, p_112128_, p_112123_, p_112124_, p_112130_, p_112132_);
        addVertex(p_323955_, p_112122_, p_112127_, p_112128_, p_112125_, p_112126_, p_112129_, p_112132_);
        addVertex(p_323955_, p_112122_, p_112127_, p_350566_, p_112125_, p_112126_, p_112129_, p_112131_);
    }

    private static void addVertex(
            PoseStack.Pose p_324495_, VertexConsumer p_253894_, int p_254357_, int p_350652_, float p_253871_, float p_253841_, float p_254568_, float p_254361_
    ) {
        p_253894_.addVertex(p_324495_, p_253871_, (float)p_350652_, p_253841_)
                .setColor(p_254357_)
                .setUv(p_254568_, p_254361_)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(15728880)
                .setNormal(p_324495_, 0.0F, 1.0F, 0.0F);
    }

    private void renderFlyingItem(PoseStack poseStack, MultiBufferSource bufferSource, ItemRenderer itemRenderer,
                                  ItemStack stack, Level level, BlockPos start, BlockPos end,
                                  float progress, float rotation) {
        float smoothProgress = easeInOut(progress);

        double startX = start.getX() + 0.5;
        double startY = start.getY() + 1.15;
        double startZ = start.getZ() + 0.5;

        double endX = end.getX() + 0.5;
        double endY = end.getY() + 1.15 + 0.2;
        double endZ = end.getZ() + 0.5;

        double x = Mth.lerp(smoothProgress, startX, endX);
        double y = Mth.lerp(smoothProgress, startY, endY);
        double z = Mth.lerp(smoothProgress, startZ, endZ);

        poseStack.pushPose();
        poseStack.translate(x - start.getX(), y - start.getY(), z - start.getZ());
        poseStack.scale(0.5f, 0.5f, 0.5f);

        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                getLightLevel(level, start), OverlayTexture.NO_OVERLAY,
                poseStack, bufferSource, level, 1);

        poseStack.popPose();
    }

    private float easeInOut(float tp) {
        tp = Mth.clamp(tp, 0f, 1f);
        return tp * tp * (3f - 2f * tp);
    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}