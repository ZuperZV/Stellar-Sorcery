package net.zuperz.stellar_sorcery.screen.Helpers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.item.ModItems;

@OnlyIn(Dist.CLIENT)
public class BookmarkButton extends Button {
    private static final ResourceLocation BOOK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/book.png");
    private static final ResourceLocation BOOK_TEXTURE_GRAY =
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/book_gray.png");
    int zLayer = 0;

    public BookmarkButton(int x, int y, int zLayer, OnPress onPress) {
        super(x, y, 11, 13, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
        this.zLayer = zLayer;
    }

    @Override
    public void renderWidget(GuiGraphics gui, int mouseX, int mouseY, float partialTick) {
        boolean hovered = this.isHoveredOrFocused();
        int texU = 0;
        int texV = hovered ? 223 : 210;
        int drawW = hovered ? 11 : 7;
        int drawH = 13;

        gui.pose().pushPose();
        gui.pose().translate(0, 0, zLayer);
        gui.blit(BOOK_TEXTURE, this.getX(), this.getY(), texU, texV, drawW, drawH);
        drawColoredOverlay(gui, this.getX(), this.getY(), texU, texV, drawW, drawH, zLayer);
        gui.pose().popPose();
    }

    @Override
    public void playDownSound(SoundManager soundManager) {
        soundManager.play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
    }

    private void drawColoredOverlay(GuiGraphics guiGraphics, int x_p, int y_p, int x, int y, int width, int height, int z_Layer) {
        var minecraft = net.minecraft.client.Minecraft.getInstance();
        if (minecraft.player == null) return;

        ItemStack stack = minecraft.player.getMainHandItem();

        if (stack.isEmpty()) stack = minecraft.player.getOffhandItem();

        if (stack.isEmpty() || !stack.is(ModItems.CODEX_ARCANUM.get())) {
            stack = minecraft.player.getInventory().items.stream()
                    .filter(stackItem -> !stackItem.isEmpty() && stackItem.getItem() == ModItems.CODEX_ARCANUM.get())
                    .findFirst()
                    .orElse(ItemStack.EMPTY);
        }

        if (!stack.is(ModItems.CODEX_ARCANUM.get())) return;

        DyedItemColor dyedColor = stack.get(DataComponents.DYED_COLOR);

        int color = dyedColor != null
                ? FastColor.ARGB32.opaque(dyedColor.rgb())
                : FastColor.ARGB32.opaque(0x4f4972);

        float r = ((color >> 16) & 0xFF) / 255.0f;
        float g = ((color >> 8) & 0xFF) / 255.0f;
        float b = (color & 0xFF) / 255.0f;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, z_Layer + 1);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(r, g, b, 1.0f);

        guiGraphics.blit(BOOK_TEXTURE_GRAY, x_p, y_p, x, y, width, height);

        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.disableBlend();
        guiGraphics.pose().popPose();
    }
}