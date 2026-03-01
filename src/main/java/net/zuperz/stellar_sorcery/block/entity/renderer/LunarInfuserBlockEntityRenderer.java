package net.zuperz.stellar_sorcery.block.entity.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.custom.LunarInfuserBlock;
import net.zuperz.stellar_sorcery.block.entity.custom.LunarInfuserBlockEntity;
import net.zuperz.stellar_sorcery.client.rendering.glow.GlowRenderTypes;
import net.zuperz.stellar_sorcery.util.ModTags;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

public class LunarInfuserBlockEntityRenderer implements BlockEntityRenderer<LunarInfuserBlockEntity> {
    private static final List<GlowEntry> GLOW_QUEUE = new ArrayList<>();

    public static final ModelLayerLocation MAGIC_AURA_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "magic_aura"), "main");

    private final ModelPart magicPlane;

    private Item lastRenderedItem = Items.AIR;
    private int fadeTicks = 0;

    private record GlowEntry(LunarInfuserBlockEntityRenderer renderer, LunarInfuserBlockEntity entity, float partialTick, float fadeAlpha) {}

    public LunarInfuserBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.magicPlane = context.bakeLayer(MAGIC_AURA_LAYER).getChild("plane");
    }

    @Override
    public void render(LunarInfuserBlockEntity pBlockEntity, float pPartialTick, PoseStack pPoseStack,
                       MultiBufferSource pBufferSource, int pPackedLight, int pPackedOverlay) {

        long gameTime = pBlockEntity.getLevel().getGameTime();

        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        ItemStack input = pBlockEntity.inventory.getStackInSlot(0);
        ResourceLocation texture = null;
        Level level = pBlockEntity.getLevel();

        renderIdleItem(pPoseStack, pBufferSource, itemRenderer, input, level, pBlockEntity.getBlockPos(), pBlockEntity.getRenderingRotation());

        Item currentItem = input.getItem();

        if (currentItem != lastRenderedItem) {
            fadeTicks = 0;
            lastRenderedItem = currentItem;
        } else if (!input.isEmpty()) {
            fadeTicks++;
        }

        float fadeAlpha = Mth.clamp((fadeTicks + pPartialTick) / 240.0f, 0.0f, 1.0f);
        boolean isNoctilume = pBlockEntity.getFluidTank().getFluid().defaultFluidState().is(ModTags.Fluids.NOCTILUME);
        float glowIntensity = getGlowIntensity(level, pBlockEntity.getBlockPos());

        if (pBlockEntity.getBlockState().getValue(LunarInfuserBlock.CRAFTING)) {
            texture = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/entity/magic_aura.png");
        }

        if (texture != null && fadeAlpha > 0.01f) {

            // magicPlane render
            pPoseStack.pushPose();

            pPoseStack.translate(0.5, 1.01, 0.5);
            pPoseStack.mulPose(Axis.YP.rotationDegrees((pBlockEntity.getLevel().getGameTime() + pPartialTick) % 360));

            RenderType renderType = RenderType.entityTranslucent(texture);
            VertexConsumer buffer = pBufferSource.getBuffer(renderType);
            int planeColor = FastColor.ARGB32.color(Mth.clamp((int) (fadeAlpha * 255.0f), 0, 255), 255, 255, 255);
            magicPlane.render(pPoseStack, buffer, pPackedLight, OverlayTexture.NO_OVERLAY, planeColor);

            pPoseStack.popPose();

            // lightningBolt render
            pPoseStack.pushPose();

            pPoseStack.translate(0.5f, 1.0f, 0.5f);
            renderLightningBolt(pPoseStack, pBufferSource, pBlockEntity.getBlockPos().asLong() + (gameTime / 5), RenderType.lightning(), fadeAlpha);

            pPoseStack.popPose();

            if (isNoctilume && glowIntensity > 0.01f) {
                GLOW_QUEUE.add(new GlowEntry(this, pBlockEntity, pPartialTick, fadeAlpha));
            }
        }

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

        pPoseStack.popPose();
    }

    private void renderIdleItem(PoseStack poseStack, MultiBufferSource bufferSource, ItemRenderer itemRenderer,
                                ItemStack stack, Level level, BlockPos pos, float rotation) {

        poseStack.pushPose();
        poseStack.translate(0.5f, 1.15f, 0.5f);
        poseStack.scale(0.5f, 0.5f, 0.5f);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));

        itemRenderer.renderStatic(stack, ItemDisplayContext.FIXED,
                getLightLevel(level, pos),
                OverlayTexture.NO_OVERLAY, poseStack, bufferSource, level, 1);

        poseStack.popPose();
    }

    private void renderLightningBolt(PoseStack poseStack, MultiBufferSource buffer, long seed, RenderType renderType, float alphaMultiplier) {
        RandomSource random = RandomSource.create(seed);

        float[] xOffsets = new float[8];
        float[] zOffsets = new float[8];

        float x = 0.0F;
        float z = 0.0F;

        for (int i = 7; i >= 0; i--) {
            xOffsets[i] = x;
            zOffsets[i] = z;
            x += (float)(random.nextInt(11) - 5);
            z += (float)(random.nextInt(11) - 5);
        }

        VertexConsumer consumer = buffer.getBuffer(renderType);
        Matrix4f matrix = poseStack.last().pose();

        for (int j = 0; j < 4; j++) {
            RandomSource randomLayer = RandomSource.create(seed);

            for (int k = 0; k < 3; k++) {
                int start = 7;
                int end = 0;

                if (k > 0) {
                    start = 7 - k;
                    end = start - 2;
                }

                float dx = xOffsets[start] - x;
                float dz = zOffsets[start] - z;

                for (int index = start; index >= end; index--) {
                    float prevDx = dx;
                    float prevDz = dz;

                    if (k == 0) {
                        dx += (float)(randomLayer.nextInt(11) - 5);
                        dz += (float)(randomLayer.nextInt(11) - 5);
                    } else {
                        dx += (float)(randomLayer.nextInt(31) - 15);
                        dz += (float)(randomLayer.nextInt(31) - 15);
                    }

                    float r = 0.45F;
                    float g = 0.45F;
                    float b = 0.5F;

                    float f10 = 0.1F + (float)j * 0.2F;
                    if (k == 0) {
                        f10 *= (float)index * 0.1F + 1.0F;
                    }

                    float f11 = 0.1F + (float)j * 0.2F;
                    if (k == 0) {
                        f11 *= ((float)index - 1.0F) * 0.1F + 1.0F;
                    }

                    quad(matrix, consumer, dx, dz, index, prevDx, prevDz, r, g, b, f10, f11, false, false, true, false, alphaMultiplier);
                    quad(matrix, consumer, dx, dz, index, prevDx, prevDz, r, g, b, f10, f11, true, false, true, true, alphaMultiplier);
                    quad(matrix, consumer, dx, dz, index, prevDx, prevDz, r, g, b, f10, f11, true, true, false, true, alphaMultiplier);
                    quad(matrix, consumer, dx, dz, index, prevDx, prevDz, r, g, b, f10, f11, false, true, false, false, alphaMultiplier);
                }
            }
        }
    }

    private static void quad(Matrix4f matrix, VertexConsumer consumer,
                             float x1, float z1, int segment,
                             float x2, float z2,
                             float r, float g, float b,
                             float f10, float f11,
                             boolean b1, boolean b2, boolean b3, boolean b4, float alphaMultiplier) {
        float alpha = 0.3F * alphaMultiplier;
        consumer.addVertex(matrix, x1 + (b1 ? f11 : -f11), (float)(segment * 16), z1 + (b2 ? f11 : -f11))
                .setColor(r, g, b, alpha);
        consumer.addVertex(matrix, x2 + (b1 ? f10 : -f10), (float)((segment + 1) * 16), z2 + (b2 ? f10 : -f10))
                .setColor(r, g, b, alpha);
        consumer.addVertex(matrix, x2 + (b3 ? f10 : -f10), (float)((segment + 1) * 16), z2 + (b4 ? f10 : -f10))
                .setColor(r, g, b, alpha);
        consumer.addVertex(matrix, x1 + (b3 ? f11 : -f11), (float)(segment * 16), z1 + (b4 ? f11 : -f11))
                .setColor(r, g, b, alpha);
    }

    @Override
    public boolean shouldRenderOffScreen(LunarInfuserBlockEntity blockEntity) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(LunarInfuserBlockEntity blockEntity) {
        return AABB.INFINITE;
    }

    public static boolean hasGlowPending() {
        return !GLOW_QUEUE.isEmpty();
    }

    public static boolean renderGlowQueue(PoseStack matrices, Camera camera, Frustum frustum, MultiBufferSource vertexConsumers) {
        if (GLOW_QUEUE.isEmpty()) {
            return false;
        }

        boolean renderedAny = false;
        Vec3 cameraPos = camera.getPosition();
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/entity/magic_aura.png");

        matrices.pushPose();
        matrices.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        for (GlowEntry entry : GLOW_QUEUE) {
            LunarInfuserBlockEntity entity = entry.entity();
            Level level = entity.getLevel();
            if (level == null || !frustum.isVisible(new AABB(entity.getBlockPos()))) {
                continue;
            }
            if (!entity.getFluidTank().getFluid().defaultFluidState().is(ModTags.Fluids.NOCTILUME)) {
                continue;
            }

            float glowIntensity = entry.renderer().getGlowIntensity(level, entity.getBlockPos());
            float glowAlpha = entry.fadeAlpha() * glowIntensity;
            if (glowAlpha <= 0.01f) {
                continue;
            }

            BlockPos pos = entity.getBlockPos();
            long seed = pos.asLong() + (level.getGameTime() / 5);

            matrices.pushPose();
            matrices.translate(pos.getX(), pos.getY(), pos.getZ());
            matrices.translate(0.5, 1.01, 0.5);
            matrices.mulPose(Axis.YP.rotationDegrees((level.getGameTime() + entry.partialTick()) % 360));
            VertexConsumer glowBuffer = vertexConsumers.getBuffer(GlowRenderTypes.entityBloom(texture));
            int glowPlaneColor = FastColor.ARGB32.color(Mth.clamp((int) (glowAlpha * 255.0f), 0, 255), 255, 255, 255);
            entry.renderer().magicPlane.render(matrices, glowBuffer, LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, glowPlaneColor);
            matrices.popPose();

            matrices.pushPose();
            matrices.translate(pos.getX() + 0.5f, pos.getY() + 1.0f, pos.getZ() + 0.5f);
            entry.renderer().renderLightningBolt(matrices, vertexConsumers, seed, GlowRenderTypes.BLOOM_LIGHTNING, glowAlpha);
            matrices.popPose();

            renderedAny = true;
        }

        GLOW_QUEUE.clear();
        matrices.popPose();

        return renderedAny;
    }

    private float getGlowIntensity(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        float ambient = Math.max(bLight, sLight) / 15.0f;
        return Mth.clamp(1.0f - ambient * 0.85f, 0.15f, 1.0f);
    }

    private int getLightLevel(Level level, BlockPos pos) {
        int bLight = level.getBrightness(LightLayer.BLOCK, pos);
        int sLight = level.getBrightness(LightLayer.SKY, pos);
        return LightTexture.pack(bLight, sLight);
    }
}
