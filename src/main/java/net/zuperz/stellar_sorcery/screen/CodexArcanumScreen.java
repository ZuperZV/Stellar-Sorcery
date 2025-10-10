package net.zuperz.stellar_sorcery.screen;

import com.mojang.blaze3d.platform.InputConstants;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
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

    private EditBox searchBox;
    private boolean searchBarActive = false;
    private boolean mouseWasOverSearch = false;
    private List<CodexEntry> searchResults = new ArrayList<>();

    private final int SEARCH_TEX_X_P = 158;
    private final int SEARCH_TEX_Y_P = -16;
    private final int SEARCH_TEX_W_P = 83;
    private final int SEARCH_TEX_H_P = 16;

    private final int SEARCH_FIELD_X_P = 177;
    private final int SEARCH_FIELD_Y_P = -15;
    private final int SEARCH_FIELD_W_P = 56;
    private final int SEARCH_FIELD_H_P = 11;

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

        int searchX = SEARCH_FIELD_X_P;
        int searchY = SEARCH_FIELD_Y_P;

        this.searchBox = new EditBox(this.font, searchX, searchY, SEARCH_FIELD_W_P, SEARCH_FIELD_H_P, Component.literal("Search"));
        this.searchBox.setBordered(false);
        this.searchBox.setVisible(false);
        this.searchBox.setTextColor(0x000000);
        this.searchBox.setMaxLength(30);
        this.searchBox.setResponder(this::updateSearchResults);

        this.addRenderableWidget(searchBox);
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

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 5);
        guiGraphics.blit(BOOK_TEXTURE, x + SEARCH_TEX_X_P, y + SEARCH_TEX_Y_P, 158, 180, 83, 16);
        guiGraphics.pose().popPose();

        if (hoveredStack != null && !hoveredStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, Z_TOOLTIP);
            guiGraphics.renderTooltip(this.font, hoveredStack, mouseX, mouseY);
            guiGraphics.pose().popPose();
            hoveredStack = ItemStack.EMPTY;
        }

        renderSearchBar(guiGraphics, mouseX, mouseY);

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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!searchResults.isEmpty()) {
            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;

            int startX = x + SEARCH_FIELD_X_P;
            int startY = y + SEARCH_FIELD_Y_P + SEARCH_FIELD_H_P + 2;
            int widthBox = 90;
            int lineHeight = 10;
            int maxVisible = Math.min(searchResults.size(), 6);

            for (int i = 0; i < maxVisible; i++) {
                int yPos = startY + i * lineHeight;

                if (mouseX >= startX && mouseX <= startX + widthBox &&
                        mouseY >= yPos && mouseY <= yPos + lineHeight) {

                    CodexEntry selected = searchResults.get(i);
                    this.selectedEntry = selected;
                    this.selectedPage = 0;
                    this.scrollOffset = 0;
                    updateButtonVisibility();

                    searchResults.clear();
                    searchBox.setFocused(false);

                    return true;
                }
            }

            if (!isMouseOverSearchArea(mouseX, mouseY, x, y)) {
                searchResults.clear();
                searchBox.setFocused(false);
                return super.mouseClicked(mouseX, mouseY, button);
            }
        }

        if (searchBox != null) {
            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;

            int fieldX = x + SEARCH_FIELD_X_P;
            int fieldY = y + SEARCH_FIELD_Y_P;

            boolean inField = mouseX >= fieldX && mouseX <= fieldX + SEARCH_FIELD_W_P &&
                    mouseY >= fieldY && mouseY <= fieldY + SEARCH_FIELD_H_P;

            if (!inField) {
                searchBox.setFocused(false);
                searchResults.clear();
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private boolean isMouseOverSearchArea(double mouseX, double mouseY, int baseX, int baseY) {
        int fieldX = baseX + SEARCH_FIELD_X_P;
        int fieldY = baseY + SEARCH_FIELD_Y_P;
        int fieldW = SEARCH_FIELD_W_P;
        int fieldH = SEARCH_FIELD_H_P;

        int resultX = fieldX;
        int resultY = fieldY + fieldH + 2;
        int resultW = 90;
        int resultH = 10 * Math.min(searchResults.size(), 6);

        return (mouseX >= fieldX && mouseX <= fieldX + fieldW && mouseY >= fieldY && mouseY <= fieldY + fieldH)
                || (mouseX >= resultX && mouseX <= resultX + resultW && mouseY >= resultY && mouseY <= resultY + resultH);
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

    private void updateSearchResults(String query) {
        searchResults.clear();
        if (query == null || query.isBlank()) return;

        String lowerQuery = query.toLowerCase();
        for (CodexEntry entry : entryList) {
            if (entry.search_items == null) continue;
            for (String tag : entry.search_items) {
                if (tag.toLowerCase().contains(lowerQuery)) {
                    searchResults.add(entry);
                    break;
                }
            }
        }
    }

    private void renderSearchBar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int barX = x + SEARCH_TEX_X_P;
        int barY = y + SEARCH_TEX_Y_P;

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, Z_BOOK_EDGE);
        guiGraphics.blit(BOOK_TEXTURE, barX, barY, SEARCH_TEX_X_P, SEARCH_TEX_Y_P, SEARCH_TEX_W_P, SEARCH_TEX_H_P);
        guiGraphics.pose().popPose();

        boolean mouseOver = MouseUtil.isMouseOver(mouseX, mouseY, barX, barY, SEARCH_TEX_W_P, SEARCH_TEX_H_P);
        if (mouseOver) mouseWasOverSearch = true;

        if (mouseWasOverSearch && !searchBox.isVisible()) {
            searchBox.setVisible(true);
            searchBox.setFocused(true);
        }

        searchBox.setX(x + SEARCH_FIELD_X_P);
        searchBox.setY(y + SEARCH_FIELD_Y_P);
        searchBox.render(guiGraphics, mouseX, mouseY, 0);

        if (!searchResults.isEmpty()) {
            renderSearchResults(guiGraphics, x, y, mouseX, mouseY);
        }
    }

    private void renderSearchResults(GuiGraphics guiGraphics, int baseX, int baseY, int mouseX, int mouseY) {
        int startX = baseX + SEARCH_FIELD_X_P;
        int startY = baseY + SEARCH_FIELD_Y_P + SEARCH_FIELD_H_P + 2;
        int width = 90;
        int lineHeight = 10;
        int maxVisible = 6;

        int shown = Math.min(searchResults.size(), maxVisible);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, Z_BOOK_EDGE + 50);

        guiGraphics.fill(startX - 2, startY - 2, startX + width, startY + shown * lineHeight + 2, 0xCC000000);

        for (int i = 0; i < shown; i++) {
            CodexEntry entry = searchResults.get(i);
            int yPos = startY + i * lineHeight;

            boolean hover = mouseX >= startX && mouseX <= startX + width && mouseY >= yPos && mouseY <= yPos + lineHeight;
            int color = hover ? 0xFFFFFF55 : 0xFFFFFFFF;

            guiGraphics.drawString(font, entry.title, startX, yPos, color);
        }

        guiGraphics.pose().popPose();
    }
}
