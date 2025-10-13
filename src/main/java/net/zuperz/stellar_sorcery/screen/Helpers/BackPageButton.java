package net.zuperz.stellar_sorcery.screen.Helpers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.screen.CodexArcanumScreen;

@OnlyIn(Dist.CLIENT)
public class BackPageButton extends Button {
    private static final ResourceLocation PAGE_BACK_HIGHLIGHTED_SPRITE = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/page_back_highlighted.png");
    private static final ResourceLocation PAGE_BACK_SPRITE = ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/page_back.png");
    private final boolean playTurnSound;

    public BackPageButton(int p_99225_, int p_99226_, Button.OnPress p_99228_, boolean p_99229_) {
        super(p_99225_, p_99226_, 23, 13, CommonComponents.EMPTY, p_99228_, DEFAULT_NARRATION);
        this.playTurnSound = p_99229_;
    }

    @Override
    public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        ResourceLocation texture = this.isHoveredOrFocused() ? PAGE_BACK_HIGHLIGHTED_SPRITE : PAGE_BACK_SPRITE;

        graphics.pose().pushPose();
        graphics.pose().translate(0, 0, CodexArcanumScreen.Z_BOOK_EDGE + 100);
        graphics.blit(texture, this.getX(), this.getY(), 0, 0, 23, 13, 23, 13);
        graphics.pose().popPose();
    }


    @Override
    public void playDownSound(SoundManager p_99231_) {
        if (this.playTurnSound) {
            p_99231_.play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
        }
    }
}