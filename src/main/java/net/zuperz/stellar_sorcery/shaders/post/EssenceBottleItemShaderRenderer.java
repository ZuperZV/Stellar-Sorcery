package net.zuperz.stellar_sorcery.shaders.post;

import com.mojang.blaze3d.shaders.Uniform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.effect.ModEffects;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = StellarSorcery.MOD_ID, value = Dist.CLIENT)
public class EssenceBottleItemShaderRenderer {

    private static class ActiveShader {
        final PostChain chain;
        final long startTime;
        final long durationMs;

        ActiveShader(PostChain chain, long durationMs) {
            this.chain = chain;
            this.startTime = System.currentTimeMillis();
            this.durationMs = durationMs;
        }

        boolean isExpired() {
            return durationMs > 0 && (System.currentTimeMillis() - startTime) >= durationMs;
        }
    }

    private static final Map<ResourceLocation, ActiveShader> activeShaders = new HashMap<>();
    private static final Map<ResourceLocation, PostChain> loadedChains = new HashMap<>();

    private static PostChain loadShader(ResourceLocation id) throws IOException {
        if (loadedChains.containsKey(id)) {
            return loadedChains.get(id);
        }

        Minecraft mc = Minecraft.getInstance();
        PostChain chain = new PostChain(
                mc.getTextureManager(),
                mc.getResourceManager(),
                mc.getMainRenderTarget(),
                id //assets/stellar_sorcery/shaders/post/
        );

        chain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
        loadedChains.put(id, chain);
        return chain;
    }


    @SubscribeEvent
    public static void onRenderStage(RenderLevelStageEvent event) {
        if (activeShaders.isEmpty()) return;

        Minecraft mc = Minecraft.getInstance();

        if (mc.player != null && mc.player.hasEffect(ModEffects.OMNIVISION)) return;

        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_LEVEL) {

            Iterator<Map.Entry<ResourceLocation, ActiveShader>> it = activeShaders.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<ResourceLocation, ActiveShader> entry = it.next();
                ActiveShader shader = entry.getValue();

                if (shader.isExpired()) {
                    it.remove();
                    continue;
                }

                PostChain chain = shader.chain;

                chain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
                try {
                    Field passesField = PostChain.class.getDeclaredField("passes");
                    passesField.setAccessible(true);
                    @SuppressWarnings("unchecked")
                    List<PostPass> passes = (List<PostPass>) passesField.get(chain);

                    for (PostPass pass : passes) {
                        var shaderInstance = pass.getEffect();
                        if (shaderInstance == null) continue;

                        Uniform timeU = shaderInstance.getUniform("Time");
                        if (timeU != null) {
                            float time = (System.currentTimeMillis() % 100000L) / 1000.0f;
                            timeU.set(time);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                chain.process(event.getPartialTick().getRealtimeDeltaTicks());
            }
        }
    }

    public static void enableShader(ResourceLocation shaderId, int ticks) {
        Minecraft mc = Minecraft.getInstance();

        mc.execute(() -> {
            try {
                PostChain chain = loadShader(shaderId);
                long durationMs = ticks * 50L;
                activeShaders.put(shaderId, new ActiveShader(chain, durationMs));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }
}