package net.zuperz.stellar_sorcery.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.zuperz.stellar_sorcery.screen.Helpers.ExtraSlot;
import net.zuperz.stellar_sorcery.screen.Helpers.IExtraSlotsProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryMenu.class)
public abstract class InventoryMenuMixin {

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addExtraSlots(Inventory playerInventory, boolean p_39707_, Player player, CallbackInfo ci) {

        if (!(player instanceof IExtraSlotsProvider provider)) {
            return;
        }

        InventoryMenu menu = (InventoryMenu) (Object) this;
        AbstractContainerMenuMixin containerMixin = (AbstractContainerMenuMixin) menu;

        Container extraInventory = provider.getExtraSlots();

        ExtraSlot slot0 = new ExtraSlot(extraInventory, 0, 97, 62);
        ExtraSlot slot1 = new ExtraSlot(extraInventory, 1, 57, 62);

        containerMixin.callAddSlot(slot0);
        containerMixin.callAddSlot(slot1);
    }
}