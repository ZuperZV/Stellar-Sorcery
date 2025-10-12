package net.zuperz.stellar_sorcery.item.custom.decorator;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

public class CodexTooltip implements TooltipComponent {
    private final ItemStack item;

    public CodexTooltip(ItemStack item) {
        this.item = item;
    }

    public ItemStack getItem() {
        return item;
    }
}