package net.zuperz.stellar_sorcery.item.custom.decorator;

import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class EssenceBottleTooltip implements TooltipComponent {
    private final List<ItemStack> items;

    public EssenceBottleTooltip(ItemStack i0, ItemStack i1, ItemStack i2) {
        this.items = List.of(i0, i1, i2);
    }

    public List<ItemStack> getItems() {
        return items;
    }
}
