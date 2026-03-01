package net.zuperz.stellar_sorcery.screen.Helpers;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.function.BooleanSupplier;

public class ExtraSlot extends Slot {
    private final BooleanSupplier activeSupplier;

    public ExtraSlot(Container inventory, int index, int x, int y, BooleanSupplier activeSupplier) {
        super(inventory, index, x, y);
        this.activeSupplier = activeSupplier;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return activeSupplier.getAsBoolean();
    }

    @Override
    public boolean isActive() {
        return activeSupplier.getAsBoolean();
    }
}