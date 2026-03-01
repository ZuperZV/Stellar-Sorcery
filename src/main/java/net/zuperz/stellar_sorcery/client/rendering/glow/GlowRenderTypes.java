package net.zuperz.stellar_sorcery.client.rendering.glow;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GlowRenderTypes {
    private static final Map<ResourceLocation, RenderType> ENTITY_BLOOM_CACHE = new ConcurrentHashMap<>();

    private static final RenderStateShard.OutputStateShard GLOW_TARGET = new RenderStateShard.OutputStateShard(
            "glow_target",
            GlowBloomRenderer::bindGlowTarget,
            GlowBloomRenderer::bindMainTarget
    );

    public static final RenderType BLOOM_GLOW = RenderType.create(
            "stellar_sorcery:bloom_glow",
            DefaultVertexFormat.BLOCK,
            VertexFormat.Mode.QUADS,
            786432,
            true,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_TRANSLUCENT_SHADER)
                    .setTextureState(RenderStateShard.BLOCK_SHEET_MIPPED)
                    .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setOverlayState(RenderStateShard.OVERLAY)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setOutputState(GLOW_TARGET)
                    .createCompositeState(true)
    );

    public static final RenderType BLOOM_LIGHTNING = RenderType.create(
            "stellar_sorcery:bloom_lightning",
            DefaultVertexFormat.POSITION_COLOR,
            VertexFormat.Mode.QUADS,
            1536,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_LIGHTNING_SHADER)
                    .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                    .setTransparencyState(RenderStateShard.LIGHTNING_TRANSPARENCY)
                    .setOutputState(GLOW_TARGET)
                    .createCompositeState(false)
    );

    public static RenderType entityBloom(ResourceLocation texture) {
        return ENTITY_BLOOM_CACHE.computeIfAbsent(texture, key ->
                RenderType.create(
                        "stellar_sorcery:bloom_entity/" + key,
                        DefaultVertexFormat.NEW_ENTITY,
                        VertexFormat.Mode.QUADS,
                        1536,
                        true,
                        true,
                        RenderType.CompositeState.builder()
                                .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                                .setTextureState(new RenderStateShard.TextureStateShard(key, false, false))
                                .setTransparencyState(RenderStateShard.ADDITIVE_TRANSPARENCY)
                                .setCullState(RenderStateShard.NO_CULL)
                                .setLightmapState(RenderStateShard.LIGHTMAP)
                                .setOverlayState(RenderStateShard.OVERLAY)
                                .setWriteMaskState(RenderStateShard.COLOR_WRITE)
                                .setOutputState(GLOW_TARGET)
                                .createCompositeState(true)
                )
        );
    }

    private GlowRenderTypes() {}
}
