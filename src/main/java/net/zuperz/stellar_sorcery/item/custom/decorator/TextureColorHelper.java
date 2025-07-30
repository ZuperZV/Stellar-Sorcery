package net.zuperz.stellar_sorcery.item.custom.decorator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class TextureColorHelper {

    private static final Map<Item, Integer> CACHE = new HashMap<>();
    private static final int DEFAULT_COLOR = 0xAAAAAA;

    public static int getAverageColor(ItemStack stack) {
        Item item = stack.getItem();
        if (CACHE.containsKey(item)) {
            return CACHE.get(item);
        }

        try {
            TextureAtlasSprite sprite = Minecraft.getInstance()
                    .getItemRenderer()
                    .getItemModelShaper()
                    .getItemModel(stack)
                    .getParticleIcon();

            int width = sprite.contents().width();
            int height = sprite.contents().height();

            int r = 0, g = 0, b = 0, count = 0;

            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int argb = sprite.getPixelRGBA(0, x, y);
                    int alpha = (argb >> 24) & 0xFF;
                    if (alpha < 32) continue;

                    r += (argb >> 16) & 0xFF;
                    g += (argb >> 8) & 0xFF;
                    b += argb & 0xFF;
                    count++;
                }
            }

            int color;
            if (count > 0) {
                r /= count;
                g /= count;
                b /= count;
                color = (r << 16) | (g << 8) | b;
            } else {
                color = DEFAULT_COLOR;
                System.out.println(color + " " + DEFAULT_COLOR + " DEFAULT_COLOR");
            }

            CACHE.put(item, color);
            return color;

        } catch (Exception e) {
            e.printStackTrace();
            CACHE.put(item, DEFAULT_COLOR);
            return DEFAULT_COLOR;
        }
    }
}