package net.zuperz.stellar_sorcery.client.rendering.glow;

import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.entity.renderer.GlowingBlockRenderer;
import net.zuperz.stellar_sorcery.block.entity.renderer.LunarInfuserBlockEntityRenderer;
import net.zuperz.stellar_sorcery.block.entity.renderer.LunarJarBlockEntityRenderer;
import net.zuperz.stellar_sorcery.block.entity.renderer.lightBeamBlockEntityRenderer;

@EventBusSubscriber(modid = StellarSorcery.MOD_ID, value = Dist.CLIENT)
public class GlowRenderHandler {

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            boolean hasPending = GlowingBlockRenderer.hasPending()
                    || LunarJarBlockEntityRenderer.hasGlowPending()
                    || LunarInfuserBlockEntityRenderer.hasGlowPending()
                    || lightBeamBlockEntityRenderer.hasGlowPending();

            if (!hasPending) {
                return;
            }

            GlowBloomRenderer.beginGlowPass();

            boolean rendered;
            try (ByteBufferBuilder byteBufferBuilder = new ByteBufferBuilder(256 * 1024)) {
                MultiBufferSource.BufferSource bufferSource = MultiBufferSource.immediate(byteBufferBuilder);
                rendered = GlowingBlockRenderer.renderAll(
                        event.getPoseStack(),
                        event.getCamera(),
                        event.getFrustum(),
                        Minecraft.getInstance().getBlockRenderer(),
                        bufferSource
                );
                rendered |= LunarJarBlockEntityRenderer.renderGlowQueue(
                        event.getPoseStack(),
                        event.getCamera(),
                        event.getFrustum(),
                        bufferSource
                );
                rendered |= LunarInfuserBlockEntityRenderer.renderGlowQueue(
                        event.getPoseStack(),
                        event.getCamera(),
                        event.getFrustum(),
                        bufferSource
                );
                rendered |= lightBeamBlockEntityRenderer.renderGlowQueue(
                        event.getPoseStack(),
                        event.getCamera(),
                        event.getFrustum(),
                        bufferSource
                );
                bufferSource.endBatch();
            } finally {
                GlowBloomRenderer.bindMainTarget();
            }

            GlowBloomRenderer.markGlowRendered(rendered);
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            GlowBloomRenderer.applyAndComposite(event.getPartialTick().getRealtimeDeltaTicks());
        }
    }
}
