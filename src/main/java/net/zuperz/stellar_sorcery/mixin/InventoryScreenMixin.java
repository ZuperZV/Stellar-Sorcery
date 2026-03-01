package net.zuperz.stellar_sorcery.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.zuperz.stellar_sorcery.StellarSorcery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {
    public boolean hoveringExtraSlot = false;

    private static final ResourceLocation CUSTOM_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/inventory_extra_slots.png");

    @Inject(method = "renderBg", at = @At("TAIL"))
    private void checkMouseOverExtraSlot(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        InventoryScreen screen = (InventoryScreen)(Object)this;

        int guiMouseX = mouseX - screen.getGuiLeft();
        int guiMouseY = mouseY - screen.getGuiTop();

        hoveringExtraSlot = ((hoveringExtraSlot && guiMouseX >= 51 && guiMouseX <= 118 && guiMouseY >= 56 && guiMouseY <= 83)
                ||(guiMouseX >= 76 && guiMouseX <= 93 && guiMouseY >= 61 && guiMouseY <= 78));

        if (hoveringExtraSlot) {
            guiGraphics.blit(
                    CUSTOM_TEXTURE,
                    screen.getGuiLeft() + 51,
                    screen.getGuiTop() + 56,
                    0, 0,
                    68, 28
            );
        }
    }
}