package net.zuperz.stellar_sorcery.mixin;

import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.advancements.AdvancementWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(AdvancementTab.class)
public interface AdvancementTabMixin {

    @Accessor("scrollX")
    double getScrollX();

    @Accessor("scrollY")
    double getScrollY();

    @Accessor("scrollX")
    void setScrollX(double scrollX);

    @Accessor("scrollY")
    void setScrollY(double scrollY);

    @Accessor("fade")
    float getFade();

    @Accessor("fade")
    void setFade(float fade);

    @Accessor("widgets")
    Map<?, AdvancementWidget> getWidgets();

    @Accessor("centered")
    boolean isCentered();

    @Accessor("centered")
    void setCentered(boolean centered);

    @Accessor("minX")
    int getMinX();

    @Accessor("maxX")
    int getMaxX();

    @Accessor("minY")
    int getMinY();

    @Accessor("maxY")
    int getMaxY();

    @Accessor("display")
    DisplayInfo getDisplay();

    @Accessor("root")
    AdvancementWidget getRoot();
}
