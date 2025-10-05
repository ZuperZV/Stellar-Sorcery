package net.zuperz.stellar_sorcery.screen;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.api.jei.JEIPlugin;
import net.zuperz.stellar_sorcery.data.*;
import net.zuperz.stellar_sorcery.network.SetBookmarksPacket;
import net.zuperz.stellar_sorcery.screen.Helpers.BookmarkButton;
import net.zuperz.stellar_sorcery.screen.Helpers.RecipeHelper;
import net.zuperz.stellar_sorcery.util.MouseUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@OnlyIn(Dist.CLIENT)
public class CodexArcanumScreen extends AbstractContainerScreen<CodexArcanumMenu> {
    private CodexEntry selectedEntry = null;
    private int selectedPage = 0;
    private PageButton forwardButton;
    private PageButton backButton;
    private final boolean playTurnSound;
    private List<CodexEntry> entryList = List.of();
    private ItemStack hoveredStack = ItemStack.EMPTY;
    private static final ResourceLocation BOOK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/book.png");
    protected int imageWidth = 248;
    protected int imageHeight = 180;
    private int scrollOffset = 0;

    private static final int Z_TOOLTIP = 1000;
    private static final int Z_BOOK_EDGE = 600;
    private static final int Z_BOOKMARK_ITEM = 400;

    private final List<String> playerBookmarks = new ArrayList<>();
    private final List<BookmarkButton> bookmarkButtons = new ArrayList<>();
    private BookmarkButton setterButton = null;

    public CodexArcanumScreen(CodexArcanumMenu menu, net.minecraft.world.entity.player.Inventory inv, Component title) {
        super(menu, inv, title);
        this.playTurnSound = true;

        this.entryList = List.copyOf(CodexDataLoader.getAllEntries());
        if (!entryList.isEmpty()) {
            this.selectedEntry = entryList.get(0);
        }
    }

    public boolean setPage(int p_98276_) {
        int i = Mth.clamp(p_98276_, 0, this.selectedEntry.right_side.size() - 1);
        if (i != this.selectedPage) {
            this.selectedPage = i;
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

        this.playerBookmarks.clear();
        this.playerBookmarks.addAll(CodexBookmarksData.getBookmarks(this.minecraft.player));

        this.createBookmarkButtons();
    }

    protected void createMenuControls() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(this.width / 2 - 100, (height - imageHeight) / 2 + 191, 200, 20).build());
    }

    protected void createPageControlButtons() {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        this.forwardButton = this.addRenderableWidget(new PageButton(x + 203, y + 156, true, button -> this.pageForward(), this.playTurnSound));
        this.backButton = this.addRenderableWidget(new PageButton(x + 23, y + 156, false, button -> this.pageBack(), this.playTurnSound));
        this.updateButtonVisibility();
    }

    @Override
    protected void renderLabels(GuiGraphics p_281635_, int p_282681_, int p_283686_) {
    }

    protected void pageBack() {
        if (selectedEntry == null) return;

        if (this.selectedPage > 0) {
            this.selectedPage--;
        } else {
            int index = this.entryList.indexOf(this.selectedEntry);
            if (index > 0) {
                this.selectedEntry = this.entryList.get(index - 1);
                this.selectedPage = this.selectedEntry.right_side.size() - 1;
            }
        }
        scrollOffset = 0;

        this.updateButtonVisibility();
    }

    protected void pageForward() {
        if (selectedEntry == null) return;

        if (this.selectedPage < this.selectedEntry.right_side.size() - 1) {
            this.selectedPage++;
        } else {
            int index = this.entryList.indexOf(this.selectedEntry);
            if (index < this.entryList.size() - 1) {
                this.selectedEntry = this.entryList.get(index + 1);
                this.selectedPage = 0;
            }
        }
        scrollOffset = 0;

        this.updateButtonVisibility();
    }

    private void updateButtonVisibility() {
        if (selectedEntry == null) {
            this.forwardButton.visible = false;
            this.backButton.visible = false;
            return;
        }

        int entryIndex = entryList.indexOf(selectedEntry);

        this.backButton.visible = this.selectedPage > 0 || entryIndex > 0;

        this.forwardButton.visible =
                this.selectedPage < this.selectedEntry.right_side.size() - 1 ||
                        entryIndex < entryList.size() - 1;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else {
            var runtime = JEIPlugin.getJeiRuntime();
            if (runtime != null && !hoveredStack.isEmpty()) {
                var keyMappings = runtime.getKeyMappings();
                InputConstants.Key key = InputConstants.getKey(keyCode, scanCode);

                if (keyMappings.getShowRecipe().isActiveAndMatches(key)) {
                    openJeiForStack(hoveredStack, false); // recipes (R)
                }
                if (keyMappings.getShowUses().isActiveAndMatches(key)) {
                    openJeiForStack(hoveredStack, true); // uses (U)
                }
            }

            switch (keyCode) {
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

    private int getCurrentPageContentHeight(int areaW) {
        if (selectedEntry == null || selectedEntry.right_side == null || selectedEntry.right_side.isEmpty()) return 0;
        if (selectedPage < 0 || selectedPage >= selectedEntry.right_side.size()) return 0;

        CodexPage page = selectedEntry.right_side.get(selectedPage);
        if (page == null || page.modules == null) return 0;

        int drawY = 0;

        for (CodexModule module : page.modules) {
            if (module.text != null && !module.text.isEmpty()) {
                List<FormattedCharSequence> lines = this.font.split(Component.literal(module.text), areaW - 4);
                drawY += lines.size() * 10 + 4;
                continue;
            }

            if (module.result != null) {
                drawY += 18 * 3 + 25 + 12;
                continue;
            }

            if (module.input != null && module.output != null) {
                drawY += 25 + 12;
                continue;
            }

            drawY += 12;
        }
        return drawY;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        int areaX = x + 138;
        int areaY = y + 44;
        int areaW = 96;
        int areaH = 112;

        if (MouseUtil.isMouseOver(mouseX, mouseY, areaX, areaY, areaW, areaH)) {
            int contentHeight = getCurrentPageContentHeight(areaW);

            int maxScroll = Math.max(0, contentHeight - areaH);

            scrollOffset -= verticalAmount > 0 ? 10 : -10;

            if (scrollOffset < 0) scrollOffset = 0;
            if (scrollOffset > maxScroll) scrollOffset = maxScroll;

            return true;
        }

        int entryIndex = entryList.indexOf(selectedEntry);
        if ((verticalAmount > 0) && (this.selectedPage > 0 || entryIndex > 0)) {
            this.pageBack();
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            return true;
        } else if ((verticalAmount < 0) && (this.selectedPage < this.selectedEntry.right_side.size() - 1 || entryIndex < entryList.size() - 1)) {
            this.pageForward();
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    public static void openJeiForStack(ItemStack stack, boolean showUses) {
        IJeiRuntime runtime = JEIPlugin.getJeiRuntime();
        if (runtime == null || stack.isEmpty()) return;

        IFocusFactory focusFactory = runtime.getJeiHelpers().getFocusFactory();
        IRecipesGui recipesGui = runtime.getRecipesGui();

        Optional<ITypedIngredient<ItemStack>> typed = runtime.getJeiHelpers()
                .getIngredientManager()
                .createTypedIngredient(VanillaTypes.ITEM_STACK, stack);

        if (typed.isPresent()) {
            IFocus<ItemStack> focus = focusFactory.createFocus(
                    showUses ? RecipeIngredientRole.INPUT : RecipeIngredientRole.OUTPUT,
                    typed.get()
            );

            recipesGui.show(focus);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBg(guiGraphics, delta, mouseX, mouseY);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        renderBookmarks(guiGraphics);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, Z_BOOK_EDGE);
        guiGraphics.blit(BOOK_TEXTURE, x + 241, y, 241, 0, 7, 180);
        guiGraphics.pose().popPose();

        if (hoveredStack != null && !hoveredStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, Z_TOOLTIP);
            guiGraphics.renderTooltip(this.font, hoveredStack, mouseX, mouseY);
            guiGraphics.pose().popPose();
            hoveredStack = ItemStack.EMPTY;
        }

        drawIconAndTitle(guiGraphics, mouseX, mouseY, x, y);
        drawSelectedPage(guiGraphics, mouseX, mouseY, x, y);
    }

    private void drawIconAndTitle(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y) {
        int yIcon = y + 11;
        int xIcon = x + 146;

        guiGraphics.drawString(this.font, Component.literal(selectedEntry.title), x + 14, y + 14, 0xa9a9a9);

        if (selectedEntry.icon != null && !selectedEntry.icon.equals("")) {
            ItemStack iconStack = RecipeHelper.parseItem(selectedEntry.icon.toString());
            renderItemWithTooltip(guiGraphics, iconStack, xIcon + 34, yIcon + 7, mouseX, mouseY);
            guiGraphics.blit(BOOK_TEXTURE, xIcon, yIcon, 0, 180, 84, 30);
        }
    }

    private void drawSelectedPage(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y) {
        int areaX = x + 138;
        int areaY = y + 44;
        int areaW = 96;
        int areaH = 112;

        guiGraphics.enableScissor(areaX, areaY, areaX + areaW, areaY + areaH);

        if (selectedEntry != null && selectedEntry.right_side != null && !selectedEntry.right_side.isEmpty()) {
            if (selectedPage < 0 || selectedPage >= selectedEntry.right_side.size()) return;
            CodexPage page = selectedEntry.right_side.get(selectedPage);
            if (page == null || page.modules == null) return;

            int drawY = areaY - scrollOffset;
            int drawX = areaX + 2;

            for (CodexModule module : page.modules) {
                switch (module.module_type) {
                    case "text" -> {
                        List<FormattedCharSequence> lines = this.font.split(Component.literal(module.text), areaW - 4);
                        for (FormattedCharSequence line : lines) {
                            guiGraphics.drawString(this.font, line, drawX, drawY, 0x282828, false);
                            drawY += 10;
                        }
                        drawY += 4;
                    }
                    case "recipe" -> {
                        guiGraphics.drawString(this.font, Component.literal("Crafting Recipe:"), drawX, drawY, 0xAAAAFF);
                        drawY += 12;

                        List<ItemStack> grid = RecipeHelper.buildCraftingGrid(module);
                        ItemStack result = RecipeHelper.parseItem(module.result);

                        int slotSize = 18;
                        for (int row = 0; row < 3; row++) {
                            for (int col = 0; col < 3; col++) {
                                int index = row * 3 + col;
                                ItemStack stack = grid.get(index);

                                int xPos = drawX + col * slotSize;
                                int yPos = drawY + row * slotSize;

                                renderItemWithTooltip(guiGraphics, stack, xPos, yPos, mouseX, mouseY);
                            }
                        }

                        int resultX = drawX + slotSize * 3 + 20;
                        int resultY = drawY + slotSize;
                        renderItemWithTooltip(guiGraphics, result, resultX, resultY, mouseX, mouseY);

                        drawY += slotSize * 3 + 25;
                    }
                    case "furnace_recipe" -> {
                        guiGraphics.drawString(this.font, Component.literal("Furnace Recipe:"), drawX, drawY, 0xFFAA00);
                        drawY += 12;

                        ItemStack input = RecipeHelper.parseItem(module.input);
                        ItemStack output = RecipeHelper.parseItem(module.output);

                        renderItemWithTooltip(guiGraphics, input, drawX, drawY, mouseX, mouseY);

                        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/arrow.png"), drawX + 25, drawY + 4, 0, 0, 23, 15);

                        renderItemWithTooltip(guiGraphics, output, drawX + 50, drawY, mouseX, mouseY);

                        drawY += 25;
                    }
                    default -> {
                        guiGraphics.drawString(this.font, Component.literal("Unknown module type: " + module.module_type), drawX, drawY, 0xFF0000);
                        drawY += 12;
                    }
                }
            }
        }

        guiGraphics.disableScissor();
    }

    private void renderItemWithTooltip(GuiGraphics guiGraphics, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        guiGraphics.fill(x, y, x + 16, y + 16, 0xFF555555);
        guiGraphics.renderItem(stack, x, y);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 1000);
        guiGraphics.renderItemDecorations(this.font, stack, x, y, null);
        guiGraphics.pose().popPose();

        if (mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16 && !stack.isEmpty()) {
            hoveredStack = stack;
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int p_296491_, int p_294260_, float p_296330_) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        this.renderTransparentBackground(guiGraphics);
        guiGraphics.blit(BOOK_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
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

    // Bookmark
    private void createBookmarkButtons() {
        for (BookmarkButton b : bookmarkButtons) {
            try { this.removeWidget(b); } catch (Exception ignored) {}
        }
        bookmarkButtons.clear();
        if (setterButton != null) {
            try { this.removeWidget(setterButton); } catch (Exception ignored) {}
            setterButton = null;
        }

        int baseX = (width - imageWidth) / 2 + 248;
        int baseY = (height - imageHeight) / 2 + 8;

        int maxBookmarks = Math.min(playerBookmarks.size(), 22);

        for (int bookmarkSize = 0; bookmarkSize < maxBookmarks; bookmarkSize++) {
            final String entryId = playerBookmarks.get(bookmarkSize);
            CodexEntry entry = entryList.stream().filter(e -> e.id.equals(entryId)).findFirst().orElse(null);
            if (entry == null) continue;

            int col = bookmarkSize / 11;
            int row = bookmarkSize % 11;

            int x = baseX + col * 6;
            int y = baseY + row * 15;

            int layerZ = col == 0 ? Z_BOOKMARK_ITEM : Z_BOOKMARK_ITEM / 2;

            BookmarkButton b = new BookmarkButton(x, y, layerZ, btn -> {
                // Delete bookmark if shift is down
                if (hasShiftDown()) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                            new SetBookmarksPacket(entryId, false)
                    );
                    CodexBookmarksData.removeBookmark(this.minecraft.player, entryId);
                    playerBookmarks.remove(entryId);
                    this.createBookmarkButtons();
                    return;
                }
                // Else open entry
                if (entry != null) {
                    this.selectedEntry = entry;
                    this.selectedPage = 0;
                    this.scrollOffset = 0;
                    this.updateButtonVisibility();
                }
            });

            this.addRenderableWidget(b);
            bookmarkButtons.add(b);
        }

        // Add "add bookmark" button if there's space
        if (playerBookmarks.size() < 22) {
            int idx = playerBookmarks.size();
            int col = idx / 11;
            int row = idx % 11;
            int x = baseX + col * 6;
            int y = baseY + row * 15;

            int layerZ = col == 0 ? Z_BOOKMARK_ITEM : Z_BOOKMARK_ITEM / 2;

            setterButton = new BookmarkButton(x, y, layerZ, btn -> {
                if (this.selectedEntry != null && this.minecraft.player != null && !playerBookmarks.contains(this.selectedEntry.id)) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                            new SetBookmarksPacket(this.selectedEntry.id, true)
                    );

                    CodexBookmarksData.addBookmark(this.minecraft.player, this.selectedEntry.id);
                    this.playerBookmarks.add(this.selectedEntry.id);
                    this.createBookmarkButtons();
                }
            });

            this.addRenderableWidget(setterButton);
        }
    }

    private void renderBookmarks(GuiGraphics guiGraphics) {
        int baseX = (width - imageWidth) / 2 + 248;
        int baseY = (height - imageHeight) / 2 + 8;

        for (int bookmarkSize = 0; bookmarkSize < bookmarkButtons.size(); bookmarkSize++) {
            String id = playerBookmarks.get(bookmarkSize);
            CodexEntry entry = entryList.stream().filter(e -> e.id.equals(id)).findFirst().orElse(null);
            if (entry == null || entry.icon == null) continue;

            ItemStack iconStack = RecipeHelper.parseItem(entry.icon);
            BookmarkButton b = bookmarkButtons.get(bookmarkSize);

            int col = (bookmarkSize >= 11) ? 1 : 0;
            int row = (bookmarkSize >= 11) ? (bookmarkSize - 12) : bookmarkSize;

            int colx = baseX + (col * 6);
            int coly = baseY + (row * 15);

            if (col == 1) coly += 15;

            if (!b.isHoveredOrFocused()) colx -= 5;

            int layerZ = col == 0 ? Z_BOOKMARK_ITEM : 100;

            renderScaledItem(guiGraphics, iconStack, colx, coly + 3, layerZ, 7);
        }
    }

    private void renderScaledItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y, int z, int size) {
        if (stack.isEmpty()) return;

        float scale = size / 16.0f;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(x, y, z);
        guiGraphics.pose().scale(scale, scale, 1f);
        guiGraphics.renderItem(stack, 0, 0);
        guiGraphics.pose().popPose();
    }
}
