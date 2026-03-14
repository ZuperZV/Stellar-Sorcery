package net.zuperz.stellar_sorcery.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.zuperz.stellar_sorcery.screen.Helpers.IRecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(RecipeBookComponent.class)
public abstract class RecipeBookComponentMixin implements IRecipeBookComponent {

    @Shadow
    protected Minecraft minecraft;
    @Unique
    private boolean hoveringExtraSlot = false;

    @Unique
    private double MouseX;
    @Unique
    private double MouseY;

    @Override
    public void setHoveringExtraSlot(boolean hovering) {
        this.hoveringExtraSlot = hovering;
    }

    @Inject(method = "isMouseOver", at = @At("HEAD"), cancellable = true)
    private void blockRecipeBookHover(double mouseX, double mouseY, CallbackInfoReturnable<Boolean> cir) {
        MouseX = mouseX;
        MouseY = mouseY;

        if (hoveringExtraSlot) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "setVisible", at = @At("HEAD"), cancellable = true)
    private void blockSetVisible(boolean visible, CallbackInfo ci) {
        if (this.hoveringExtraSlot) {
            if (MouseX >= 104 && MouseX <= 113 &&
                    MouseY >= 61  && MouseY <= 78) {
                InventoryScreen invScreen = (InventoryScreen) this.minecraft.screen;

                Slot hoveredSlot = findHoveredSlot(MouseX, MouseY, invScreen);
                if (hoveredSlot != null) {
                    ((InventoryScreenInvoker) invScreen).callSlotClicked(
                            hoveredSlot, hoveredSlot.index, 0, ClickType.PICKUP
                    );
                }

            }

            ci.cancel();
        }
    }

    @Unique
    private Slot findHoveredSlot(double mouseX, double mouseY, InventoryScreen screen) {
        for (Slot slot : screen.getMenu().slots) {
            if (mouseX >= slot.x && mouseX < slot.x + 16 &&
                    mouseY >= slot.y && mouseY < slot.y + 16) {
                return slot;
            }
        }
        return null;
    }
}