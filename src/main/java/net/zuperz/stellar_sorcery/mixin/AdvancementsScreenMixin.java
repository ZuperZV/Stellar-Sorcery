package net.zuperz.stellar_sorcery.mixin;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;

@Mixin(AdvancementsScreen.class)
public interface AdvancementsScreenMixin {

    @Invoker("renderInside")
    void callRenderInside(GuiGraphics guiGraphics, int mouseX, int mouseY, int left, int top);

    @Accessor("isScrolling")
    boolean getIsScrolling();

    @Mutable
    @Accessor("isScrolling")
    void setIsScrolling(boolean value);

    @Accessor("tabs")
    Map<AdvancementHolder, AdvancementTab> getTabs();

    @Accessor(value = "tabPage", remap = false)
    static int getTabPage() {
        throw new UnsupportedOperationException();
    }

    @Accessor(value = "tabPage", remap = false)
    static void setTabPage(int page) {
        throw new UnsupportedOperationException();
    }

    @Accessor(value = "maxPages", remap = false)
    static int getMaxPages() {
        throw new UnsupportedOperationException();
    }

    @Accessor(value = "maxPages", remap = false)
    static void setMaxPages(int pages) {
        throw new UnsupportedOperationException();
    }
}