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
    public static final ResourceLocation BEAM_LOCATION = ResourceLocation.withDefaultNamespace("textures/entity/beacon_beam.png");
    public static final int MAX_RENDER_Y = 1024;

    private static final int SEGMENTS = 8; // antal knæk – kan ændres nemt
    private static final int UPDATE_INTERVAL = 20; // antal ticks mellem opdatering af formen
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
        int startY = pBlockEntity.getBlockPos().getY();
        int height = 10;
        int color = FastColor.ARGB32.color(255, 255, 255, 255); // white

        renderLightningStyleBeam(pPoseStack, pBufferSource, gameTime, pPartialTick, startY, height, color);

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

    private void renderLightningStyleBeam(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            long gameTime,
            float partialTick,
            int startY,
            int height,
            int color
    ) {
        // Opdater kun hvert UPDATE_INTERVAL ticks
        if (lastUpdateTick < 0 || gameTime - lastUpdateTick >= UPDATE_INTERVAL) {
            lastUpdateTick = gameTime;
            RandomSource random = RandomSource.create(gameTime); // seed så den er stabil indtil næste update

            float offX = 0;
            float offZ = 0;
            for (int i = SEGMENTS - 1; i >= 0; i--) {
                offsetsX[i] = offX;
                offsetsZ[i] = offZ;
                offX += (float) (random.nextInt(11) - 5);
                offZ += (float) (random.nextInt(11) - 5);
            }
        }

        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lightning());
        Matrix4f mat = poseStack.last().pose();

        for (int layer = 0; layer < 2; layer++) {
            RandomSource random = RandomSource.create(gameTime);
            for (int branch = 0; branch < 1; branch++) {
                int segEnd = SEGMENTS - 1;
                int segStart = 0;

                float prevX = offsetsX[segEnd];
                float prevZ = offsetsZ[segEnd];

                for (int seg = segEnd; seg >= segStart; seg--) {
                    float currX = offsetsX[seg];
                    float currZ = offsetsZ[seg];

                    float thickness1 = 0.2F;
                    float thickness2 = 0.2F;

                    // Tegn segment – fire sider som i LightningBoltRenderer.quad()
                    quad(mat, vertexConsumer, currX, currZ, seg, prevX, prevZ,
                            0.45F, 0.45F, 0.5F,
                            thickness1, thickness2,
                            false, false, true, false);

                    prevX = currX;
                    prevZ = currZ;
                }
            }
        }
    }

    private static void quad(
            Matrix4f mat,
            VertexConsumer buf,
            float x1, float z1, int segIndex,
            float x2, float z2,
            float r, float g, float b,
            float size1, float size2,
            boolean b1, boolean b2, boolean b3, boolean b4
    ) {
        buf.addVertex(mat, x1 + (b1 ? size2 : -size2), segIndex * 16, z1 + (b2 ? size2 : -size2))
                .setColor(r, g, b, 0.3F);
        buf.addVertex(mat, x2 + (b1 ? size1 : -size1), (segIndex + 1) * 16, z2 + (b2 ? size1 : -size1))
                .setColor(r, g, b, 0.3F);
        buf.addVertex(mat, x2 + (b3 ? size1 : -size1), (segIndex + 1) * 16, z2 + (b4 ? size1 : -size1))
                .setColor(r, g, b, 0.3F);
        buf.addVertex(mat, x1 + (b3 ? size2 : -size2), segIndex * 16, z1 + (b4 ? size2 : -size2))
                .setColor(r, g, b, 0.3F);
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