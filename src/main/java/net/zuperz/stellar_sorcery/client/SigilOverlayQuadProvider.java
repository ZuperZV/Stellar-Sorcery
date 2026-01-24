package net.zuperz.stellar_sorcery.client;

import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.*;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.zuperz.stellar_sorcery.StellarSorcery;

import java.util.ArrayList;
import java.util.List;

public class SigilOverlayQuadProvider  {

    public static List<BakedQuad> generateQuads() {
        List<BakedQuad> bakedQuads = new ArrayList<>();
        ModelState modelState = new ModelState() {};

        Minecraft mc = Minecraft.getInstance();

        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getModelManager()
                .getModel(ModelResourceLocation.inventory(ResourceLocation.fromNamespaceAndPath(
                        StellarSorcery.MOD_ID,
                        "item/sigil_overlay_chestplate"
                )))
                .getParticleIcon();

        ItemModelGenerator generator = new ItemModelGenerator();
        List<BlockElement> elements =
                generator.processFrames(0, "layer0", sprite.contents());

        FaceBakery faceBakery = new FaceBakery();

        for (BlockElement element : elements) {
            for (Direction direction : Direction.values()) {

                BlockElementFace face = element.faces.get(direction);
                if (face == null) continue;

                BakedQuad quad = faceBakery.bakeQuad(
                        element.from,
                        element.to,
                        face,
                        sprite,
                        direction,
                        modelState,
                        null,
                        false
                );

                bakedQuads.add(quad);
            }
        }

        return bakedQuads;
    }
}