package net.zuperz.stellar_sorcery.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zuperz.stellar_sorcery.screen.Helpers.IExtraSlotsProvider;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Shadow
    protected abstract Player getCameraPlayer();

    @Shadow
    protected abstract void renderSlot(GuiGraphics guiGraphics, int x, int y, DeltaTracker deltaTracker, Player player, ItemStack stack, int seed);

    @Shadow
    @Final
    private static ResourceLocation HOTBAR_OFFHAND_LEFT_SPRITE;

    @Shadow
    @Final
    private static ResourceLocation HOTBAR_OFFHAND_RIGHT_SPRITE;

    @Inject(method = "renderItemHotbar", at = @At("TAIL"))
    private void renderExtraOffhandSlots(GuiGraphics guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        Player player = this.getCameraPlayer();
        if (player == null) return;
        if (!(player instanceof IExtraSlotsProvider provider)) return;

        Container extra = provider.getExtraSlots();
        if (extra.getContainerSize() < IExtraSlotsProvider.TOTAL_SLOTS) return;

        int center = guiGraphics.guiWidth() / 2;
        int bgY = guiGraphics.guiHeight() - 23;
        int itemY = guiGraphics.guiHeight() - 16 - 3;

        int leftBgX = center - 91 - 29;
        int rightBgX = center + 91;

        ItemStack vanillaOffhand = player.getOffhandItem();
        if (!vanillaOffhand.isEmpty()) {
            HumanoidArm offhandSide = player.getMainArm().getOpposite();
            if (offhandSide == HumanoidArm.LEFT) {
                leftBgX -= 29;
            } else {
                rightBgX += 29;
            }
        }

        ItemStack leftStack = extra.getItem(IExtraSlotsProvider.GAZE_SLOT_LEFT);
        ItemStack rightStack = extra.getItem(IExtraSlotsProvider.GAZE_SLOT_RIGHT);

        int seed = 200;
        if (!leftStack.isEmpty()) {
            RenderSystem.enableBlend();
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, -90.0F);
            guiGraphics.blitSprite(HOTBAR_OFFHAND_LEFT_SPRITE, leftBgX, bgY, 29, 24);
            guiGraphics.pose().popPose();
            RenderSystem.disableBlend();

            this.renderSlot(guiGraphics, leftBgX + 3, itemY, deltaTracker, player, leftStack, seed++);
        }
        if (!rightStack.isEmpty()) {
            RenderSystem.enableBlend();
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, -90.0F);
            guiGraphics.blitSprite(HOTBAR_OFFHAND_RIGHT_SPRITE, rightBgX, bgY, 29, 24);
            guiGraphics.pose().popPose();
            RenderSystem.disableBlend();

            this.renderSlot(guiGraphics, rightBgX + 10, itemY, deltaTracker, player, rightStack, seed++);
        }
    }
}
