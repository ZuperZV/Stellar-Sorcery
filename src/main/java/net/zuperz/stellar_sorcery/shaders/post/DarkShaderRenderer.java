package net.zuperz.stellar_sorcery.shaders.post;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zuperz.stellar_sorcery.StellarSorcery;

import java.io.IOException;

@EventBusSubscriber(modid = StellarSorcery.MOD_ID, value = Dist.CLIENT)
public class DarkShaderRenderer {

    private static PostChain darkShaderChain;
    private static boolean enabled = false;

    // Timing fields
    private static long enableStartTime = 0;
    private static long durationMs = 0;

    private static void initShader() {
        if (darkShaderChain == null) {
            Minecraft mc = Minecraft.getInstance();
            try {
                darkShaderChain = new PostChain(
                        mc.getTextureManager(),
                        mc.getResourceManager(),
                        mc.getMainRenderTarget(),
                        ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "shaders/post/dark.json")
                );
                darkShaderChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (!enabled) return;

        if (durationMs > 0) {
            long elapsed = System.currentTimeMillis() - enableStartTime;
            if (elapsed >= durationMs) {
                enabled = false;
                return;
            }
        }

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            initShader();

            if (darkShaderChain != null) {
                Minecraft mc = Minecraft.getInstance();
                darkShaderChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());

                darkShaderChain.process(event.getPartialTick().getRealtimeDeltaTicks());
            }
        }
    }

    public static void enableForTicks(int ticks) {
        enabled = true;
        enableStartTime = System.currentTimeMillis();
        durationMs = ticks * 50L;
    }
}

