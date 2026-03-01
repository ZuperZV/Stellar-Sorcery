package net.zuperz.stellar_sorcery.client.rendering.glow;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.pipeline.TextureTarget;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.zuperz.stellar_sorcery.StellarSorcery;

import java.io.IOException;
import java.util.Objects;

public final class GlowBloomRenderer {
    private static TextureTarget glowTarget;
    private static PostChain bloomChain;
    private static boolean hasGlowThisFrame = false;

    private GlowBloomRenderer() {}

    public static void beginGlowPass() {
        ensureTargets();
        if (glowTarget == null) return;

        glowTarget.clear(Minecraft.ON_OSX);
        glowTarget.copyDepthFrom(Minecraft.getInstance().getMainRenderTarget());
        // Ensure subsequent world rendering stays on the main target even if no glow draw happens this frame.
        bindMainTarget();
    }

    public static void bindGlowTarget() {
        ensureTargets();
        if (glowTarget != null) {
            glowTarget.bindWrite(false);
        }
    }

    public static void bindMainTarget() {
        Minecraft.getInstance().getMainRenderTarget().bindWrite(false);
    }

    public static void markGlowRendered(boolean rendered) {
        hasGlowThisFrame = hasGlowThisFrame || rendered;
    }

    public static void applyAndComposite(float partialTicks) {
        if (!hasGlowThisFrame) return;

        ensureTargets();
        initChain();

        if (bloomChain != null) {
            Minecraft mc = Minecraft.getInstance();
            bloomChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
            bloomChain.process(partialTicks);
        }

        compositeToMain();
        hasGlowThisFrame = false;
    }

    private static void ensureTargets() {
        Minecraft mc = Minecraft.getInstance();
        int width = mc.getWindow().getWidth();
        int height = mc.getWindow().getHeight();

        if (glowTarget == null) {
            glowTarget = new TextureTarget(width, height, true, Minecraft.ON_OSX);
            glowTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        } else if (glowTarget.width != width || glowTarget.height != height) {
            glowTarget.resize(width, height, Minecraft.ON_OSX);
            glowTarget.setClearColor(0.0F, 0.0F, 0.0F, 0.0F);
        }
    }

    private static void initChain() {
        if (bloomChain != null) return;

        Minecraft mc = Minecraft.getInstance();
        try {
            bloomChain = new PostChain(
                    mc.getTextureManager(),
                    mc.getResourceManager(),
                    glowTarget,
                    ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "shaders/post/glow_bloom.json")
            );
            bloomChain.resize(mc.getWindow().getWidth(), mc.getWindow().getHeight());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void compositeToMain() {
        Minecraft mc = Minecraft.getInstance();
        mc.getMainRenderTarget().bindWrite(false);

        GlStateManager._colorMask(true, true, true, false);
        GlStateManager._disableDepthTest();
        GlStateManager._depthMask(false);
        GlStateManager._viewport(0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight());

        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 0.5F);

        ShaderInstance shader = Objects.requireNonNull(mc.gameRenderer.blitShader, "Blit shader not loaded");
        shader.setSampler("DiffuseSampler", glowTarget.getColorTextureId());
        shader.apply();

        BufferBuilder bufferBuilder = RenderSystem.renderThreadTesselator().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.BLIT_SCREEN);
        bufferBuilder.addVertex(0.0F, 0.0F, 0.0F);
        bufferBuilder.addVertex(1.0F, 0.0F, 0.0F);
        bufferBuilder.addVertex(1.0F, 1.0F, 0.0F);
        bufferBuilder.addVertex(0.0F, 1.0F, 0.0F);
        BufferUploader.draw(bufferBuilder.buildOrThrow());

        shader.clear();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        GlStateManager._depthMask(true);
        GlStateManager._enableDepthTest();
        GlStateManager._colorMask(true, true, true, true);
    }
}
