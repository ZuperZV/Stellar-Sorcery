package net.zuperz.stellar_sorcery.api.jei.custom;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.gui.drawable.IDrawable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.joml.Vector2i;

import java.io.InputStream;

public class PlanetDrawable implements IDrawable {

    private final ResourceLocation texture;
    private final int drawSize;
    private final int textureSize;
    private final int cropSize;

    private final int frameTime = 20;

    public PlanetDrawable(ResourceLocation texture, int drawSize, int textureSize, int cropSize) {
        this.texture = texture;
        this.drawSize = drawSize;
        this.textureSize = textureSize;
        this.cropSize = cropSize;
    }

    @Override
    public int getWidth() {
        return drawSize;
    }

    @Override
    public int getHeight() {
        return drawSize;
    }

    @Override
    public void draw(GuiGraphics graphics, int xOffset, int yOffset) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);

        int start = calculateCenteredCropStart(textureSize, cropSize);

        Vector2i textureSizei = getTextureSize(texture);

        int totalFrames = textureSizei.y() / textureSizei.x();

        long time = getTime();
        int currentFrame = (int) ((time / frameTime) % totalFrames);

        graphics.blit(
                texture,
                xOffset, yOffset,
                drawSize, drawSize,
                start, start + currentFrame * textureSizei.x(),
                cropSize, cropSize,
                textureSize, textureSize * (textureSizei.y() / textureSizei.x())
        );
    }

    private static long getTime() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            return mc.level.getDayTime();
        }
        return System.currentTimeMillis() / 50L;
    }

    private static int calculateCenteredCropStart(int textureSize, int cropSize) {
        cropSize = Math.min(cropSize, textureSize);
        return (textureSize - cropSize) / 2;
    }

    public static Vector2i getTextureSize(ResourceLocation texture) {
        ResourceManager rm = Minecraft.getInstance().getResourceManager();

        try {
            Resource res = rm.getResource(texture).orElseThrow();

            try (InputStream is = res.open();
                 NativeImage img = NativeImage.read(is)) {

                return new Vector2i(img.getWidth(), img.getHeight());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return new Vector2i(0, 0);
    }
}
