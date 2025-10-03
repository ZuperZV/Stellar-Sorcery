package net.zuperz.stellar_sorcery.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.zuperz.stellar_sorcery.data.CodexDataLoader;
import net.zuperz.stellar_sorcery.data.CodexEntry;
import net.zuperz.stellar_sorcery.data.CodexPage;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

@OnlyIn(Dist.CLIENT)
public class CodexArcanumScreen extends AbstractContainerScreen<CodexArcanumMenu> {
    public static final int PAGE_INDICATOR_TEXT_Y_OFFSET = 16;
    public static final int PAGE_TEXT_X_OFFSET = 36;
    public static final int PAGE_TEXT_Y_OFFSET = 30;
    private CodexEntry selectedEntry = null;
    private int selectedPage = 0;
    private List<FormattedCharSequence> cachedPageComponents = Collections.emptyList();
    private int cachedPage = -1;
    private Component pageMsg = CommonComponents.EMPTY;
    private PageButton forwardButton;
    private PageButton backButton;
    private final boolean playTurnSound;
    private List<CodexEntry> entryList = List.of();

    public CodexArcanumScreen(CodexArcanumMenu menu, net.minecraft.world.entity.player.Inventory inv, Component title) {
        super(menu, inv, title);
        this.playTurnSound = true;
        // Load entries from manager
        this.entryList = List.copyOf(CodexDataLoader.getAllEntries());
        if (!entryList.isEmpty()) {
            this.selectedEntry = entryList.get(0);
        }
    }

    public void setSelectedEntry(CodexEntry entry) {
        this.selectedEntry = entry;
        this.selectedPage = 0;
        this.cachedPage = -1;
    }

    public boolean setPage(int p_98276_) {
        int i = Mth.clamp(p_98276_, 0, this.selectedEntry.pages.size() - 1);
        if (i != this.selectedPage) {
            this.selectedPage = i;
            this.cachedPage = -1;
            return true;
        } else {
            return false;
        }
    }

    protected boolean forcePage(int p_98295_) {
        return this.setPage(p_98295_);
    }

    @Override
    protected void init() {
        this.createMenuControls();
        this.createPageControlButtons();

        int y = 30;
        for (int i = 0; i < entryList.size(); i++) {
            CodexEntry entry = entryList.get(i);
            int entryIndex = i;
            this.addRenderableWidget(Button.builder(Component.literal(entry.title), b -> {
                this.selectedEntry = entryList.get(entryIndex);
                this.selectedPage = 0;
            }).bounds(this.leftPos + 10, y, 120, 20).build());
            y += 22;
        }
    }

    protected void createMenuControls() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, p_315823_ -> this.onClose()).bounds(this.width / 2 - 100, 196, 200, 20).build());
    }

    protected void createPageControlButtons() {
        int i = (this.width - 192) / 2;
        int j = 2;
        this.forwardButton = this.addRenderableWidget(new PageButton(i + 116, 159, true, p_98297_ -> this.pageForward(), this.playTurnSound));
        this.backButton = this.addRenderableWidget(new PageButton(i + 43, 159, false, p_98287_ -> this.pageBack(), this.playTurnSound));
        this.updateButtonVisibility();
    }

    private int getNumPages() {
        return this.selectedEntry != null ? this.selectedEntry.pages.size() : 0;
    }

    protected void pageBack() {
        if (this.selectedPage > 0) {
            this.selectedPage--;
        }

        this.updateButtonVisibility();
    }

    protected void pageForward() {
        if (this.selectedPage < this.getNumPages() - 1) {
            this.selectedPage++;
        }

        this.updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        this.forwardButton.visible = this.selectedPage < this.getNumPages() - 1;
        this.backButton.visible = this.selectedPage > 0;
    }

    @Override
    public boolean keyPressed(int p_98278_, int p_98279_, int p_98280_) {
        if (super.keyPressed(p_98278_, p_98279_, p_98280_)) {
            return true;
        } else {
            switch (p_98278_) {
                case 266:
                    this.backButton.onPress();
                    return true;
                case 267:
                    this.forwardButton.onPress();
                    return true;
                default:
                    return false;
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        // Draw selected entry and page
        if (selectedEntry != null && selectedEntry.pages != null && !selectedEntry.pages.isEmpty()) {
            CodexPage page = selectedEntry.pages.get(selectedPage);
            guiGraphics.drawString(this.font, Component.literal(selectedEntry.title), this.leftPos + 140, 30, 0xFFFFFF);
            guiGraphics.drawString(this.font, Component.literal(page.text != null ? page.text : ""), this.leftPos + 140, 50, 0xCCCCCC);
        }
    }

    @Override
    public void renderBackground(GuiGraphics p_295678_, int p_296491_, int p_294260_, float p_294869_) {
        this.renderTransparentBackground(p_295678_);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float p_97788_, int p_97789_, int p_97790_) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, ResourceLocation.withDefaultNamespace("textures/gui/book.png"));
    }

    @Override
    public boolean mouseClicked(double p_98272_, double p_98273_, int p_98274_) {
        return super.mouseClicked(p_98272_, p_98273_, p_98274_);
    }

    @Override
    public boolean handleComponentClicked(Style p_98293_) {
        ClickEvent clickevent = p_98293_.getClickEvent();
        if (clickevent == null) {
            return false;
        } else if (clickevent.getAction() == ClickEvent.Action.CHANGE_PAGE) {
            String s = clickevent.getValue();

            try {
                int i = Integer.parseInt(s) - 1;
                return this.forcePage(i);
            } catch (Exception exception) {
                return false;
            }
        } else {
            boolean flag = super.handleComponentClicked(p_98293_);
            if (flag && clickevent.getAction() == ClickEvent.Action.RUN_COMMAND) {
                this.closeScreen();
            }

            return flag;
        }
    }

    protected void closeScreen() {
        this.minecraft.setScreen(null);
    }

    @Nullable
    public Style getClickedComponentStyleAt(double p_98269_, double p_98270_) {
        if (this.cachedPageComponents.isEmpty()) {
            return null;
        } else {
            int i = Mth.floor(p_98269_ - (double)((this.width - 192) / 2) - 36.0);
            int j = Mth.floor(p_98270_ - 2.0 - 30.0);
            if (i >= 0 && j >= 0) {
                int k = Math.min(128 / 9, this.cachedPageComponents.size());
                if (i <= 114 && j < 9 * k + k) {
                    int l = j / 9;
                    if (l >= 0 && l < this.cachedPageComponents.size()) {
                        FormattedCharSequence formattedcharsequence = this.cachedPageComponents.get(l);
                        return this.minecraft.font.getSplitter().componentStyleAtWidth(formattedcharsequence, i);
                    } else {
                        return null;
                    }
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }
    }
}
