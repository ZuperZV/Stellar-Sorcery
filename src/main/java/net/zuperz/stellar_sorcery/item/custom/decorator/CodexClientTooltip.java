package net.zuperz.stellar_sorcery.item.custom.decorator;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;
import net.zuperz.stellar_sorcery.util.KeyBinding;

public class CodexClientTooltip implements ClientTooltipComponent {
    private final ItemStack item;
    private final String text;

    public CodexClientTooltip(CodexTooltip tooltip) {
        this.item = tooltip.getItem();
        this.text = getText();
    }

    public String getText() {
        Component keyName = KeyBinding.OPEN_BOOK.getTranslatedKeyMessage();

        Component message = Component.literal("")
                .append(Component.literal("[")
                        .withStyle(ChatFormatting.GRAY))
                .append(keyName)
                .append(Component.literal("]")
                        .withStyle(ChatFormatting.GRAY))
                .append(Component.literal(" to open codex"));

        return message.getString();
    }

    @Override
    public int getHeight() {
        return 16;
    }

    @Override
    public int getWidth(Font font) {
        return 16 + 4 + font.width(text);
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        // Tegn item
        graphics.renderItem(item, x, y - 2);
        graphics.renderItemDecorations(font, item, x, y - 2);

        int textX = x + 16 + 4;
        int textY = y + 3;

        graphics.drawString(font, text, textX, textY, 0xFFFFFF, true);
    }
}