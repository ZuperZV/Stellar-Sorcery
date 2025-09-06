package net.zuperz.stellar_sorcery.item.custom.decorator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class EssenceBottleClientTooltip implements ClientTooltipComponent {
    private final List<ItemStack> items;

    public EssenceBottleClientTooltip(EssenceBottleTooltip tooltip) {
        this.items = tooltip.getItems();
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public int getWidth(Font font) {
        return items.size() * 18;
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            int drawX = x + i * 18;
            graphics.renderItem(stack, drawX, y);
            graphics.renderItemDecorations(Minecraft.getInstance().font, stack, drawX, y);
        }
    }
}
