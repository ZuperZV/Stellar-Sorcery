package net.zuperz.stellar_sorcery.mixin;

import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.ClickType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(InventoryScreen.class)
public interface InventoryScreenInvoker {

    @Invoker("slotClicked")
    void callSlotClicked(Slot slot, int slotId, int button, ClickType clickType);
}