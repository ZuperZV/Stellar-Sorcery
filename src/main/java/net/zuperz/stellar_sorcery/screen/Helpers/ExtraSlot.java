package net.zuperz.stellar_sorcery.screen.Helpers;

import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.zuperz.stellar_sorcery.item.custom.GazeItem;

public class ExtraSlot extends Slot {

    private boolean shouldRender = false;

    private final int slotIndex;
    public static final EquipmentSlot GAZE = EquipmentSlot.HEAD;

    public ExtraSlot(Container container, int index, int x, int y) {
        super(container, index, x, y);
        this.slotIndex = index;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (stack.getItem() instanceof GazeItem gazeItem) {
            return true;
        }
        return false;
    }

    @Override
    public boolean isActive() {
        return shouldRender;
    }

    public void setActive(boolean active) {
        this.shouldRender = active;
    }

    public int getSlotIndex() {
        return this.slotIndex;
    }
}