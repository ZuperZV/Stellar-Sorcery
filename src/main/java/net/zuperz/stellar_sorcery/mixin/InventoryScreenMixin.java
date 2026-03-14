package net.zuperz.stellar_sorcery.mixin;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.CyclingSlotBackground;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.screen.Helpers.ExtraSlot;
import net.zuperz.stellar_sorcery.screen.Helpers.IRecipeBookComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Mixin(InventoryScreen.class)
public abstract class InventoryScreenMixin {

    @Unique
    private boolean hoveringExtraSlot;

    private static final ResourceLocation CUSTOM_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(
                    StellarSorcery.MOD_ID,
                    "textures/gui/inventory_extra_slots.png"
            );

    private static final ResourceLocation EMPTY_SLOT_GAZE_ITEM = ResourceLocation.fromNamespaceAndPath(
            StellarSorcery.MOD_ID, "item/empty_slot_gaze"
    );
    private static final ResourceLocation EMPTY_SLOT_GAZE_UPGRADE = ResourceLocation.fromNamespaceAndPath(
            StellarSorcery.MOD_ID, "item/empty_slot_gaze_upgrade"
    );
    private static final List<ResourceLocation> EMPTY_SLOT_GAZE = List.of(
            EMPTY_SLOT_GAZE_ITEM, EMPTY_SLOT_GAZE_UPGRADE
    );

    @Unique
    private final Map<Integer, CyclingSlotBackground> extraGazeIcons = new HashMap<>();

    @Inject(method = "render", at = @At("TAIL"))
    private void renderOnTop(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {

        InventoryScreen screen = (InventoryScreen) (Object) this;

        int guiMouseX = mouseX - screen.getGuiLeft();
        int guiMouseY = mouseY - screen.getGuiTop();
        hoveringExtraSlot = ((hoveringExtraSlot
                && guiMouseX >= 51 && guiMouseX <= 118
                && guiMouseY >= 56 && guiMouseY <= 83)

                || (guiMouseX >= 76 && guiMouseX <= 93
                && guiMouseY >= 61 && guiMouseY <= 78));

        IRecipeBookComponent recipeBook = (IRecipeBookComponent) screen.getRecipeBookComponent();
        recipeBook.setHoveringExtraSlot(hoveringExtraSlot);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 100);

        if (hoveringExtraSlot) {

            guiGraphics.blit(CUSTOM_TEXTURE,
                    screen.getGuiLeft() + 51,
                    screen.getGuiTop() + 56,
                    0, 0,
                    68, 28
            );

            for (Slot slot : screen.getMenu().slots) {
                if (slot instanceof ExtraSlot extraSlot) {
                    int slotIndex = slot.index;

                    CyclingSlotBackground icon = extraGazeIcons.computeIfAbsent(slotIndex,
                            i -> new CyclingSlotBackground(i));

                    icon.render(screen.getMenu(), guiGraphics, partialTick, screen.getGuiLeft(), screen.getGuiTop());
                }
            }

            guiGraphics.pose().popPose();

            guiGraphics.blit(CUSTOM_TEXTURE,
                    screen.getGuiLeft() + 51,
                    screen.getGuiTop() + 56,
                    0, 0,
                    68, 28
            );
        }

        guiGraphics.pose().popPose();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void updateExtraSlotVisibility(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {

        InventoryScreen screen = (InventoryScreen) (Object) this;

        screen.getMenu().slots.forEach(slot -> {
            if (slot instanceof ExtraSlot extraSlot) {
                extraSlot.setActive(hoveringExtraSlot);
            }
        });
    }

    @Inject(method = "containerTick", at = @At("HEAD"))
    public void containerTick(CallbackInfo ci) {
        extraGazeIcons.values().forEach(icon -> icon.tick(EMPTY_SLOT_GAZE));
    }
}