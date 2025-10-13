package net.zuperz.stellar_sorcery.screen.Helpers;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.zuperz.stellar_sorcery.mixin.AdvancementTabMixin;
import net.zuperz.stellar_sorcery.mixin.AdvancementsScreenMixin;
import net.zuperz.stellar_sorcery.screen.CodexArcanumScreen;

import java.util.Map;

public class CustomAdvancementRenderer {

    public static void renderTooltipsOnly(AdvancementsScreen screen, GuiGraphics graphics, int mouseX, int mouseY, int guiLeft, int guiTop, int x, int y, CodexArcanumScreen codexScreen) {
        AdvancementTab selected = getSelectedTab(screen);
        if (selected == null) return;

        drawTooltips(graphics, mouseX - guiLeft - 9, mouseY - guiTop - 18, guiLeft, guiTop, selected);
        drawContents(graphics, guiLeft, guiTop, selected);

        AdvancementTabMixin tabMixin = (AdvancementTabMixin) selected;
        AdvancementsScreenMixin screenMixin = (AdvancementsScreenMixin) screen;

        graphics.pose().pushPose();
        graphics.pose().translate(guiLeft + 9, guiTop + 18, 900.0F);
        RenderSystem.enableDepthTest();

        RenderSystem.disableDepthTest();
        graphics.pose().popPose();

        RenderSystem.enableBlend();
        if (screenMixin.getTabs().size() > 1) {
            for (AdvancementTab advancementtab : screenMixin.getTabs().values()) {
                if (advancementtab.getPage() == AdvancementsScreenMixin.getTabPage())
                    advancementtab.drawTab(graphics, x, y, advancementtab == selected);
            }

            for (AdvancementTab advancementtab1 : screenMixin.getTabs().values()) {
                if (advancementtab1.getPage() == AdvancementsScreenMixin.getTabPage())
                    advancementtab1.drawIcon(graphics, x, y);
            }
        }
    }

    private static AdvancementTab getSelectedTab(AdvancementsScreen screen) {
        try {
            var field = AdvancementsScreen.class.getDeclaredField("selectedTab");
            field.setAccessible(true);
            return (AdvancementTab) field.get(screen);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void drawTooltips(GuiGraphics p_282892_, int mouseX, int mouseY, int p_282652_, int p_283595_, AdvancementTab selected) {
        p_282892_.pose().pushPose();
        p_282892_.pose().translate(0.0F, 0.0F, -200.0F);

        AdvancementTabMixin mixin = (AdvancementTabMixin) selected;

        int i = Mth.floor(mixin.getScrollX());
        int j = Mth.floor(mixin.getScrollY());
        float fade = mixin.getFade();
        Map<?, AdvancementWidget> widgets = mixin.getWidgets();

        p_282892_.fill(0, 0, 234, 113, Mth.floor(fade * 255.0F) << 24);

        boolean flag = false;

        if (mouseX > 0 && mouseX < 234 && mouseY > 0 && mouseY < 113) {
            for (AdvancementWidget widget : widgets.values()) {
                if (widget.isMouseOver(i, j, mouseX, mouseY)) {
                    flag = true;
                    widget.drawHover(p_282892_, i, j, fade, p_282652_, p_283595_);
                    break;
                }
            }
        }

        if (flag) {
            mixin.setFade(Math.min(fade + 0.02F, 0.3F));
        } else {
            mixin.setFade(Math.max(fade - 0.04F, 0.0F));
        }

        p_282892_.pose().popPose();
    }

    public static void drawContents(GuiGraphics graphics, int x, int y, AdvancementTab tab) {
        AdvancementTabMixin mixin = (AdvancementTabMixin) tab;

        if (!mixin.isCentered()) {
            mixin.setScrollX(117 - (mixin.getMaxX() + mixin.getMinX()) / 2.0);
            mixin.setScrollY(56 - (mixin.getMaxY() + mixin.getMinY()) / 2.0);
            mixin.setCentered(true);
        }

        graphics.enableScissor(x, y, x + 234, y + 113);
        graphics.pose().pushPose();
        graphics.pose().translate((float)x, (float)y, 0.0F);

        int scrollX = (int) Math.floor(mixin.getScrollX());
        int scrollY = (int) Math.floor(mixin.getScrollY());

        for (AdvancementWidget widget : mixin.getWidgets().values()) {
            widget.drawConnectivity(graphics, scrollX, scrollY, true);
        }
        for (AdvancementWidget widget : mixin.getWidgets().values()) {
            widget.drawConnectivity(graphics, scrollX, scrollY, false);
        }
        for (AdvancementWidget widget : mixin.getWidgets().values()) {
            widget.draw(graphics, scrollX, scrollY);
        }

        mixin.getRoot().drawConnectivity(graphics, scrollX, scrollY, true);
        mixin.getRoot().drawConnectivity(graphics, scrollX, scrollY, false);
        mixin.getRoot().draw(graphics, scrollX, scrollY);

        graphics.pose().popPose();
        graphics.disableScissor();
    }
}
