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

@OnlyIn(Dist.CLIENT)
public class BookmarkButton extends Button {
    private static final ResourceLocation BOOK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/book.png");
    private final boolean isSetter;

    public BookmarkButton(int x, int y, boolean isSetter, OnPress onPress) {
        super(x, y, 11, 13, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
        this.isSetter = isSetter;
    }

    public boolean isSetter() {
        return this.isSetter;
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.isHoveredOrFocused();
        int texU = 0;
        int texV = hovered ? 223 : 210;
        int drawW = hovered ? 11 : 7;
        int drawH = 13;

        gui.blit(BOOK_TEXTURE, this.getX(), this.getY(), texU, texV, drawW, drawH);
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }
}