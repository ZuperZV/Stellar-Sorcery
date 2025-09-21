package net.zuperz.stellar_sorcery.item.custom.decorator;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import net.zuperz.stellar_sorcery.component.StarDustData;
import net.zuperz.stellar_sorcery.item.custom.AuroraSkullItem;

public class StarDustNumberBarDecorator implements IItemDecorator {

    @Override
    public boolean render(GuiGraphics guiGraphics, Font font, ItemStack stack, int xOffset, int yOffset) {
        StarDustData starDustData = getStarDustDataFromItemStack(stack);

        if (starDustData == null) {
            return false;
        }

        int numberValue = starDustData.getValue1();

        int maxBarWidth = 12;
        int barWidth = (numberValue * maxBarWidth) / 100;

        boolean isBarVisible = barWidth > 0;

        if (!isBarVisible) {
            return false;
        }

        int barY = yOffset + 13;
        int barX = xOffset + 2;

        renderBar(guiGraphics, barX, barY, barWidth, 0xFb6c2c2);

        return true;
    }

    private void renderBar(GuiGraphics guiGraphics, int x, int y, int width, int color) {
        guiGraphics.fill(RenderType.guiOverlay(), x, y, x + 12, y + 2, 0xFF303030);
        guiGraphics.fill(RenderType.guiOverlay(), x, y, x + width, y + 1, color | 0xFF000000);
    }

    private StarDustData getStarDustDataFromItemStack(ItemStack stack) {
        if (stack.getItem() instanceof AuroraSkullItem auroraSkullItem) {
            return auroraSkullItem.GetStarDust(stack);
        }
        return null;
    }
}