package net.zuperz.stellar_sorcery.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IRecipesGui;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.advancements.AdvancementTab;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.PageButton;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.api.jei.JEIPlugin;
import net.zuperz.stellar_sorcery.component.CodexTierData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.data.*;
import net.zuperz.stellar_sorcery.item.ModItems;
import net.zuperz.stellar_sorcery.mixin.AdvancementsScreenMixin;
import net.zuperz.stellar_sorcery.network.SetBookmarksPacket;
import net.zuperz.stellar_sorcery.screen.Helpers.BackPageButton;
import net.zuperz.stellar_sorcery.screen.Helpers.BookmarkButton;
import net.zuperz.stellar_sorcery.screen.Helpers.CustomAdvancementRenderer;
import net.zuperz.stellar_sorcery.screen.Helpers.RecipeHelper;
import net.zuperz.stellar_sorcery.util.MouseUtil;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.gui.screens.advancements.AdvancementsScreen;
import net.minecraft.advancements.Advancement;
import net.minecraft.client.multiplayer.ClientAdvancements;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@OnlyIn(Dist.CLIENT)
public class CodexArcanumScreen extends AbstractContainerScreen<CodexArcanumMenu> {
    public CodexEntry selectedEntry = null;
    public int selectedPage = 0;
    private PageButton forwardButton;
    private PageButton backWardButton;
    private BackPageButton backButton;
    private final boolean playTurnSound;
    private List<CodexEntry> entryList = List.of();
    private ItemStack hoveredStack = ItemStack.EMPTY;
    private static final ResourceLocation BOOK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/book.png");
    private static final ResourceLocation BOOK_TEXTURE_GRAY =
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/book_gray.png");
    private int scrollOffset = 0;

    private float easedIconX = 0f;
    private float easedIconY = 0f;

    public static final int Z_TOOLTIP = 300;
    public static final int Z_BOOK_EDGE = 200;
    public static final int Z_BOOKMARK_ITEM = 100;

    private final List<String> playerBookmarks = new ArrayList<>();
    private final List<BookmarkButton> bookmarkButtons = new ArrayList<>();
    private BookmarkButton setterButton = null;

    private EditBox searchBox;
    private boolean mouseWasOverSearch = false;
    private List<CodexEntry> searchResults = new ArrayList<>();

    public List<CodexCategory> categories = new ArrayList<>();
    public CodexCategory selectedCategory = null;
    public boolean isInCategoryView = true;

    private AdvancementsScreen advancementsScreen;
    private boolean showAdvancement = true;
    public int advancementX = -36;
    public int advancementY = 18;

    private final int SEARCH_TEX_X_P = 158;
    private final int SEARCH_TEX_Y_P = -16;
    private final int SEARCH_TEX_W_P = 83;
    private final int SEARCH_TEX_H_P = 16;

    private final int SEARCH_FIELD_X_P = 177;
    private final int SEARCH_FIELD_Y_P = -10;
    private final int SEARCH_FIELD_W_P = 56;
    private final int SEARCH_FIELD_H_P = 11;

    private static final int SLOT_WIDTH = 97;
    private static final int SLOT_HEIGHT = 20;
    private static final int SLOT_SPACING = 2;
    private static final int ITEM_SIZE = 16;
    private static final int ITEM_PADDING = 2;

    public CodexArcanumScreen(CodexArcanumMenu menu, net.minecraft.world.entity.player.Inventory inv, Component title) {
        super(menu, inv, title);
        this.playTurnSound = true;

        this.imageWidth = 248;
        this.imageHeight = 180;

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
        this.categories = CodexDataLoader.getAllCategories();
        this.selectedPage = 0;

        this.playerBookmarks.clear();
        this.playerBookmarks.addAll(CodexBookmarksData.getBookmarks(this.minecraft.player));
        this.createBookmarkButtons();

        int searchX = SEARCH_FIELD_X_P;
        int searchY = SEARCH_FIELD_Y_P;

        this.searchBox = new EditBox(this.font, searchX, searchY, SEARCH_FIELD_W_P, SEARCH_FIELD_H_P, Component.literal("Search"));
        this.searchBox.setBordered(false);
        this.searchBox.setVisible(false);
        this.searchBox.setTextColor(0x000000);
        this.searchBox.setTextShadow(false);
        this.searchBox.setMaxLength(30);
        this.searchBox.setResponder(this::updateSearchResults);

        this.addRenderableWidget(searchBox);

        this.createMenuControls();
        this.createPageControlButtons();

        if (showAdvancement) {
            ClientAdvancements clientAdvancements = Minecraft.getInstance().player.connection.getAdvancements();

            advancementsScreen = new AdvancementsScreen(clientAdvancements, null);
            advancementsScreen.init(minecraft, this.width, this.height);
        }
    }

    protected void createMenuControls() {
        this.addRenderableWidget(Button.builder(CommonComponents.GUI_DONE, button -> this.onClose()).bounds(this.width / 2 - 100, (height - imageHeight) / 2 + 194, 200, 20).build());
    }

    protected void createPageControlButtons() {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        this.forwardButton = this.addRenderableWidget(new PageButton(x + 203, y + 156, true, button -> this.pageForward(), this.playTurnSound));
        this.backWardButton = this.addRenderableWidget(new PageButton(x + 23, y + 156, false, button -> this.pageWardBack(), this.playTurnSound));
        this.backButton = this.addRenderableWidget(new BackPageButton(x + 112, y + 180, button -> this.pageBack(), this.playTurnSound));
        this.updateButtonVisibility();
    }

    @Override
    protected void renderLabels(GuiGraphics p_281635_, int p_282681_, int p_283686_) {
    }

    protected void pageBack() {
        if (this.selectedEntry != null) {
            this.selectedEntry = null;
            this.scrollOffset = 0;
            this.selectedPage = 0;

            this.isInCategoryView = false;

            this.updateButtonVisibility();

            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            return;
        }

        if (this.selectedCategory != null) {
            this.isInCategoryView = true;
            this.selectedCategory = null;
            this.scrollOffset = 0;
            this.selectedPage = 0;

            this.updateButtonVisibility();

            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            return;
        }

        // Hvis vi allerede er helt tilbage i oversigten (intet selectedCategory, intet selectedEntry)
        // så kan du vælge at lukke bogen eller blot ikke gøre noget.
        // Her gør vi bare ingenting.
    }

    protected void pageWardBack() {
        if (selectedEntry == null) return;

        CodexTierData tierData = getBookItem().getComponents().get(ModDataComponentTypes.CODEX_TIER.get());
        int playerTier = tierData != null ? tierData.getTier() : 0;

        if (this.selectedPage > 0) {
            this.selectedPage--;
        } else {
            int index = this.entryList.indexOf(this.selectedEntry);
            if (index > 0) {
                CodexEntry previousEntry = this.entryList.get(index - 1);

                int previousTier = getTierForEntry(previousEntry);

                if (previousTier == -1 && previousEntry != null && previousEntry.id != null) {
                    try {
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("tier_(\\d+)").matcher(previousEntry.id);
                        if (m.find()) {
                            previousTier = Integer.parseInt(m.group(1));
                        }
                    } catch (Exception ignored) { }
                }

                try {
                    StellarSorcery.LOGGER.debug("pageBack: playerTier={}, previousEntryId={}, previousTier={}", playerTier, previousEntry.id, previousTier);
                } catch (Exception ignored) {}

                if (previousTier > playerTier) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                    return;
                }

                this.selectedEntry = previousEntry;
                this.isInCategoryView = false;
                this.selectedPage = Math.max(0, this.selectedEntry.right_side.size() - 1);
            }
        }

        scrollOffset = 0;
        this.updateButtonVisibility();
    }

    protected void pageForward() {
        if (selectedEntry == null) return;

        CodexTierData tierData = getBookItem().getComponents().get(ModDataComponentTypes.CODEX_TIER.get());
        int playerTier = tierData != null ? tierData.getTier() : 0;

        if (this.selectedPage < this.selectedEntry.right_side.size() - 1) {
            this.selectedPage++;
        } else {
            int index = this.entryList.indexOf(this.selectedEntry);
            if (index < this.entryList.size() - 1) {
                CodexEntry nextEntry = this.entryList.get(index + 1);

                int nextTier = getTierForEntry(nextEntry);

                if (nextTier == -1 && nextEntry != null && nextEntry.id != null) {
                    try {
                        java.util.regex.Matcher m = java.util.regex.Pattern.compile("tier_(\\d+)").matcher(nextEntry.id);
                        if (m.find()) {
                            nextTier = Integer.parseInt(m.group(1));
                        }
                    } catch (Exception ignored) { }
                }

                try {
                    StellarSorcery.LOGGER.debug("pageForward: playerTier={}, nextEntryId={}, nextTier={}", playerTier, nextEntry.id, nextTier);
                } catch (Exception ignored) {}

                if (nextTier > playerTier) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                    return;
                }

                this.selectedEntry = nextEntry;
                this.isInCategoryView = false;
                this.selectedPage = 0;
            }
        }

        scrollOffset = 0;
        this.updateButtonVisibility();
    }

    public void updateButtonVisibility() {
        if (isInCategoryView) {
            this.backButton.visible = false;
        } else {
            this.backButton.visible = true;
        }

        if (selectedEntry == null || this.isInCategoryView) {
            this.forwardButton.visible = false;
            this.backWardButton.visible = false;
            return;
        }

        int entryIndex = entryList.indexOf(selectedEntry);

        this.backWardButton.visible = this.selectedPage > 0 || entryIndex > 0;

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
                case 266, GLFW.GLFW_KEY_LEFT:
                    this.backWardButton.onPress();
                    return true;
                case 267, GLFW.GLFW_KEY_RIGHT:
                    this.forwardButton.onPress();
                    return true;
                case GLFW.GLFW_KEY_UP, GLFW.GLFW_KEY_DOWN:
                    this.backButton.onPress();
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

    private int getCategoryOverviewContentHeight() {
        int spacingY = 50;
        return categories.size() * spacingY;
    }

    private int getCategoryEntriesContentHeight() {
        if (selectedCategory == null) return 0;
        int spacingY = 20;
        CodexTierData tierData = getBookItem().getComponents().get(ModDataComponentTypes.CODEX_TIER.get());
        int playerTier = tierData != null ? tierData.getTier() : 0;

        int count = 0;
        Pattern tierPattern = Pattern.compile("tier_(\\d+)");
        for (CodexEntry entry : selectedCategory.entries) {
            Matcher matcher = tierPattern.matcher(entry.id);
            int entryTier = matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
            if (entryTier <= playerTier) count++;
        }
        return count * spacingY;
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
            int contentHeight = 0;

            if (isInCategoryView) {
                contentHeight = getCategoryOverviewContentHeight();
            } else if (!isInCategoryView && selectedCategory != null && selectedEntry == null) {
                contentHeight = getCategoryEntriesContentHeight();
            } else if (selectedEntry != null) {
                contentHeight = getCurrentPageContentHeight(areaW);
            }

            int maxScroll = Math.max(0, contentHeight - areaH);
            scrollOffset -= verticalAmount > 0 ? 10 : -10;
            scrollOffset = Mth.clamp(scrollOffset, 0, maxScroll);

            return true;
        }

        if (selectedEntry != null) {
            int entryIndex = entryList.indexOf(selectedEntry);
            if ((verticalAmount > 0) && (this.selectedPage > 0 || entryIndex > 0)) {
                this.pageWardBack();
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
                return true;
            } else if ((verticalAmount < 0) && (this.selectedPage < this.selectedEntry.right_side.size() - 1 || entryIndex < entryList.size() - 1)) {
                this.pageForward();
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
                return true;
            }
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
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        renderBookmarks(guiGraphics);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, Z_BOOK_EDGE);
        guiGraphics.blit(BOOK_TEXTURE, x + 241, y, 241, 0, 7, 180);
        drawColoredOverlay(guiGraphics, x + 241, y, 241, 0, 7, 180, Z_BOOK_EDGE - 1);
        guiGraphics.blit(BOOK_TEXTURE, x + 124, y, 124, 0, 124, 12);
        drawColoredOverlay(guiGraphics, x + 124, y, 124, 0, 124, 12, Z_BOOK_EDGE - 1);
        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 0);

        int searchy = 0;
        if (!mouseWasOverSearch) {
            searchy = 9;
        }

        guiGraphics.blit(BOOK_TEXTURE, x + SEARCH_TEX_X_P, y + SEARCH_TEX_Y_P + searchy, 158, 180, 83, 16);
        drawColoredOverlay(guiGraphics, x + SEARCH_TEX_X_P, y + SEARCH_TEX_Y_P + searchy, 158, 180, 83, 16, 0);

        int iconBaseX = x + SEARCH_TEX_X_P + 6;
        int iconBaseY = y + SEARCH_TEX_Y_P + 5 + searchy;

        float[] target = getMouseEasedOffset(iconBaseX, iconBaseY, mouseX, mouseY, 90f, 1f);

        easedIconX += (target[0] - easedIconX) * 0.2f;
        easedIconY += (target[1] - easedIconY) * 0.2f;

        guiGraphics.blit(BOOK_TEXTURE, (int)(iconBaseX + easedIconX), (int)(iconBaseY + easedIconY), 145, 182, 12, 12);
        drawColoredOverlay(guiGraphics, (int)(iconBaseX + easedIconX), (int)(iconBaseY + easedIconY), 145, 182, 12, 12, 0);

        guiGraphics.pose().popPose();


        if (hoveredStack != null && !hoveredStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, Z_TOOLTIP);
            guiGraphics.renderTooltip(this.font, hoveredStack, mouseX, mouseY);
            guiGraphics.pose().popPose();
            hoveredStack = ItemStack.EMPTY;
        }

        if ((isInCategoryView) || (selectedCategory != null && selectedEntry == null)) {
            drawIconAndTitle(guiGraphics, mouseX, mouseY, x, y, getBookItem());
        }

        if (isInCategoryView) {
            renderCategoryOverview(guiGraphics, mouseX, mouseY);
        } else if (selectedCategory != null && selectedEntry == null) {
            renderCategoryEntries(guiGraphics, mouseX, mouseY);
        } else {
            drawSelectedPage(guiGraphics, mouseX, mouseY, x, y);
            drawIconAndTitle(guiGraphics, mouseX, mouseY, x, y);
        }

        renderSearchBar(guiGraphics, mouseX, mouseY);

        renderBg(guiGraphics, delta, mouseX, mouseY);
    }


    private void renderCategoryOverview(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int areaX = x + 138;
        int areaY = y + 44;
        int areaW = SLOT_WIDTH;
        int areaH = 112;
        guiGraphics.enableScissor(areaX, areaY, areaX + areaW, areaY + areaH);

        int drawY = areaY - scrollOffset;

        for (CodexCategory cat : categories) {
            boolean hovered = MouseUtil.isMouseOver(mouseX, mouseY, areaX, drawY, SLOT_WIDTH, SLOT_HEIGHT);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, Z_TOOLTIP- 10);

            guiGraphics.fill(areaX, drawY, areaX + SLOT_WIDTH, drawY + SLOT_HEIGHT, 0xAA202020);

            if (hovered) {
                guiGraphics.fill(areaX - 1, drawY - 1, areaX + SLOT_WIDTH + 1, drawY + SLOT_HEIGHT + 1, 0xAAFFFFFF);
            }

            guiGraphics.renderItem(cat.icon, areaX + ITEM_PADDING, drawY + (SLOT_HEIGHT - ITEM_SIZE) / 2);

            //yte
            Component message = Component.translatable("codex_arcanum.stellar_sorcery." + cat.id);

            guiGraphics.drawString(font, message, areaX + ITEM_SIZE + ITEM_PADDING * 2, drawY + (SLOT_HEIGHT - 8) / 2, 0xFFFFFF);

            guiGraphics.pose().popPose();

            drawY += SLOT_HEIGHT + SLOT_SPACING;
        }

        guiGraphics.disableScissor();
    }

    private void renderCategoryEntries(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (selectedCategory == null) return;

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        int areaX = x + 138;
        int areaY = y + 44;
        int areaW = SLOT_WIDTH;
        int areaH = 112;
        guiGraphics.enableScissor(areaX, areaY, areaX + areaW, areaY + areaH);

        int drawY = areaY - scrollOffset;

        CodexTierData tierData = getBookItem().getComponents().get(ModDataComponentTypes.CODEX_TIER.get());
        int playerTier = tierData != null ? tierData.getTier() : 0;

        for (int i = 0; i < selectedCategory.entries.size(); i++) {
            CodexEntry entry = selectedCategory.entries.get(i);
            int entryTierValue = selectedCategory.tiers.get(i);

            if (entryTierValue > playerTier) continue;

            boolean hovered = MouseUtil.isMouseOver(mouseX, mouseY, areaX, drawY, SLOT_WIDTH, SLOT_HEIGHT);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, Z_TOOLTIP - 10);

            guiGraphics.fill(areaX, drawY, areaX + SLOT_WIDTH, drawY + SLOT_HEIGHT, 0xAA202020);
            if (hovered) {
                guiGraphics.fill(areaX - 1, drawY - 1, areaX + SLOT_WIDTH + 1, drawY + SLOT_HEIGHT + 1, 0xAAFFFFFF);
            }

            guiGraphics.renderItem(RecipeHelper.parseItem(entry.icon), areaX + ITEM_PADDING, drawY + (SLOT_HEIGHT - ITEM_SIZE) / 2);

            ItemStack stack = getBookItem().copy();
            stack.set(ModDataComponentTypes.CODEX_TIER.get(), new CodexTierData(entryTierValue));

            renderScaledItem(guiGraphics, stack, areaX + ITEM_PADDING + 8, drawY + (SLOT_HEIGHT - ITEM_SIZE) / 2 + 8, Z_TOOLTIP - 5, 10);

            guiGraphics.drawString(font, entry.title, areaX + ITEM_SIZE + ITEM_PADDING * 2, drawY + (SLOT_HEIGHT - 8) / 2, 0xFFFFFF);

            guiGraphics.pose().popPose();

            drawY += SLOT_HEIGHT + SLOT_SPACING;
        }

        guiGraphics.disableScissor();
    }

    private void drawIconAndTitle(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y) {
        int yIcon = y + 11;
        int xIcon = x + 146;

        guiGraphics.drawString(this.font, Component.literal(selectedEntry.title), x + 14, y + 14, ChatFormatting.DARK_GRAY.getColor(), false);

        if (selectedEntry.icon != null && !selectedEntry.icon.equals("")) {
            ItemStack iconStack = RecipeHelper.parseItem(selectedEntry.icon.toString());
            renderItemWithTooltip(guiGraphics, iconStack, xIcon + 34, yIcon + 7, mouseX, mouseY);
            guiGraphics.blit(BOOK_TEXTURE, xIcon, yIcon, 0, 180, 84, 30);
            drawColoredOverlay(guiGraphics, xIcon, yIcon, 0, 180, 84, 30, 0);
        }
    }

    private void drawIconAndTitle(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y, ItemStack iconStack) {
        int yIcon = y + 11;
        int xIcon = x + 146;

        guiGraphics.drawString(this.font, Component.translatable(iconStack.getDescriptionId()), x + 14, y + 14, ChatFormatting.DARK_GRAY.getColor(), false);

        renderItemWithTooltip(guiGraphics, iconStack, xIcon + 34, yIcon + 7, mouseX, mouseY);
        guiGraphics.blit(BOOK_TEXTURE, xIcon, yIcon, 0, 180, 84, 30);
        drawColoredOverlay(guiGraphics, xIcon, yIcon, 0, 180, 84, 30, 0);
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

                                renderItem(guiGraphics, stack, xPos, yPos);

                                guiGraphics.disableScissor();
                                renderItemTooltip(guiGraphics, stack, xPos, yPos, mouseX, mouseY);
                                guiGraphics.enableScissor(areaX, areaY, areaX + areaW, areaY + areaH);
                            }
                        }

                        int resultX = drawX + slotSize * 3 + 20;
                        int resultY = drawY + slotSize;

                        renderItem(guiGraphics, result, resultX, resultY);

                        guiGraphics.disableScissor();
                        renderItemTooltip(guiGraphics, result, resultX, resultY, mouseX, mouseY);
                        guiGraphics.enableScissor(areaX, areaY, areaX + areaW, areaY + areaH);

                        drawY += slotSize * 3 + 25;
                    }
                    case "furnace_recipe" -> {
                        guiGraphics.drawString(this.font, Component.literal("Furnace Recipe:"), drawX, drawY, 0xFFAA00);
                        drawY += 12;

                        ItemStack input = RecipeHelper.parseItem(module.input);
                        ItemStack output = RecipeHelper.parseItem(module.output);

                        renderItem(guiGraphics, input, drawX, drawY);

                        guiGraphics.disableScissor();
                        renderItemTooltip(guiGraphics, input, drawX, drawY, mouseX, mouseY);
                        guiGraphics.enableScissor(areaX, areaY, areaX + areaW, areaY + areaH);

                        guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/arrow.png"), drawX + 25, drawY + 4, 0, 0, 23, 15);

                        renderItem(guiGraphics, output, drawX + 50, drawY);

                        guiGraphics.disableScissor();
                        renderItemTooltip(guiGraphics, output, drawX + 50, drawY, mouseX, mouseY);
                        guiGraphics.enableScissor(areaX, areaY, areaX + areaW, areaY + areaH);

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
        guiGraphics.pose().translate(0, 0, Z_TOOLTIP + 100);
        guiGraphics.renderItemDecorations(this.font, stack, x, y, null);
        guiGraphics.pose().popPose();

        if (mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16 && !stack.isEmpty()) {
            hoveredStack = stack;
        }
    }

    private void renderItem(GuiGraphics guiGraphics, ItemStack stack, int x, int y) {
        guiGraphics.fill(x, y, x + 16, y + 16, 0xFF555555);
        guiGraphics.renderItem(stack, x, y);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, Z_TOOLTIP + 100);
        guiGraphics.renderItemDecorations(this.font, stack, x, y, null);
        guiGraphics.pose().popPose();
    }

    private void renderItemTooltip(GuiGraphics guiGraphics, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        if (mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16 && !stack.isEmpty()) {
            hoveredStack = stack;
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        this.renderTransparentBackground(guiGraphics);

        guiGraphics.blit(BOOK_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        drawColoredOverlay(guiGraphics, x, y, 0, 0, imageWidth, imageHeight, 0);
    }


    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        if (showAdvancement && advancementsScreen != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(advancementX, advancementY, 0);

            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;

            int advGuiLeft = (this.width - AdvancementsScreen.WINDOW_WIDTH) / 2;
            int advGuiTop  = (this.height - AdvancementsScreen.WINDOW_HEIGHT) / 2;

            //((AdvancementsScreenMixin) advancementsScreen).callRenderInside(
            //        guiGraphics,
            //        mouseX - advancementX,
            //        mouseY - advancementY,
            //        advGuiLeft,
            //        advGuiTop
            //);

            CustomAdvancementRenderer.renderTooltipsOnly(
                    advancementsScreen,
                    guiGraphics,
                    mouseX - advancementX,
                    mouseY - advancementY,
                    (this.width - 252) / 2,
                    (this.height - 140) / 2,
                    x,
                    y,
                    this
            );

            guiGraphics.pose().popPose();
        }
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        if (!searchResults.isEmpty()) {

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
                    this.isInCategoryView = false;
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

            int fieldX = x + SEARCH_FIELD_X_P;
            int fieldY = y + SEARCH_FIELD_Y_P;

            boolean inField = mouseX >= fieldX && mouseX <= fieldX + SEARCH_FIELD_W_P &&
                    mouseY >= fieldY && mouseY <= fieldY + SEARCH_FIELD_H_P;

            if (!inField) {
                searchBox.setFocused(false);
                searchResults.clear();
            }
        }

        int areaX = x + 138;
        int areaY = y + 44;

        if (isInCategoryView) {
            int drawY = areaY - scrollOffset;

            for (CodexCategory cat : categories) {
                if (mouseX >= areaX && mouseX <= areaX + SLOT_WIDTH &&
                        mouseY >= drawY && mouseY <= drawY + SLOT_HEIGHT) {

                    this.selectedCategory = cat;
                    this.isInCategoryView = false;
                    this.updateButtonVisibility();
                    this.selectedPage = 0;
                    this.scrollOffset = 0;
                    return true;
                }
                drawY += SLOT_HEIGHT + SLOT_SPACING;
            }
        }

        if (!isInCategoryView && selectedCategory != null && selectedEntry == null) {
            int drawY = areaY - scrollOffset;

            CodexTierData tierData = getBookItem().getComponents().get(ModDataComponentTypes.CODEX_TIER.get());
            int playerTier = tierData != null ? tierData.getTier() : 0;

            for (int i = 0; i < selectedCategory.entries.size(); i++) {
                CodexEntry entry = selectedCategory.entries.get(i);
                int entryTierValue = selectedCategory.tiers.get(i);

                if (entryTierValue > playerTier) continue;

                if (mouseX >= areaX && mouseX <= areaX + SLOT_WIDTH &&
                        mouseY >= drawY && mouseY <= drawY + SLOT_HEIGHT) {

                    CodexEntry codexEntry = entryList.stream().filter(e -> e.id.equals(entry.id)).findFirst().orElse(null);
                    if (codexEntry == null) continue;

                    this.selectedEntry = codexEntry;
                    this.selectedPage = 0;
                    this.scrollOffset = 0;
                    this.isInCategoryView = false;
                    this.updateButtonVisibility();
                    return true;
                }
                drawY += SLOT_HEIGHT + SLOT_SPACING;
            }
        }

        if (showAdvancement && this.advancementsScreen != null) {

            int relX = (int) (mouseX - x);
            int relY = (int) (mouseY - y);

            if (relX >= 16 && relY >= 22 && relX < 16 + 92 && relY < 22 + 138) {
                double[] adj = mapToAdvancementCoords(mouseX, mouseY);
                return this.advancementsScreen.mouseClicked(adj[0], adj[1], button);
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (showAdvancement && this.advancementsScreen != null) {

            int imageWidth = 252;
            int imageHeight = 140;
            int x = (this.width - imageWidth) / 2;
            int y = (this.height - imageHeight) / 2;

            int relX = (int) (mouseX - x);
            int relY = (int) (mouseY - y);

            if (relX >= 16 && relY >= 22 && relX < 16 + 92 && relY < 22 + 138) {
                double[] adj = mapToAdvancementCoords(mouseX, mouseY);
                return this.advancementsScreen.mouseDragged(adj[0], adj[1], button, dragX, dragY);
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    private double[] mapToAdvancementCoords(double screenMouseX, double screenMouseY) {
        int advGuiLeft = (this.width - 252) / 2;
        int advGuiTop  = (this.height - 140) / 2;

        double dx = this.advancementX - advGuiLeft;
        double dy = this.advancementY - advGuiTop;

        double adjustedX = screenMouseX - dx;
        double adjustedY = screenMouseY - dy;
        return new double[] { adjustedX, adjustedY };
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
                if (hasShiftDown()) {
                    net.neoforged.neoforge.network.PacketDistributor.sendToServer(
                            new SetBookmarksPacket(entryId, false)
                    );
                    CodexBookmarksData.removeBookmark(this.minecraft.player, entryId);
                    playerBookmarks.remove(entryId);
                    this.createBookmarkButtons();
                    return;
                }

                if (entry != null) {
                    this.selectedEntry = entry;
                    this.selectedPage = 0;
                    this.scrollOffset = 0;
                    this.isInCategoryView = false;
                    this.updateButtonVisibility();
                }
            });

            this.addRenderableWidget(b);
            bookmarkButtons.add(b);
        }

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
        drawColoredOverlay(guiGraphics, barX, barY, SEARCH_TEX_X_P, SEARCH_TEX_Y_P, SEARCH_TEX_W_P, SEARCH_TEX_H_P, Z_BOOK_EDGE);
        guiGraphics.pose().popPose();

        drawColoredOverlay(guiGraphics, barX, barY, SEARCH_TEX_X_P, SEARCH_TEX_Y_P, SEARCH_TEX_W_P, SEARCH_TEX_H_P, Z_BOOK_EDGE);

        boolean mouseOver = MouseUtil.isMouseOver(mouseX, mouseY, barX, barY, SEARCH_TEX_W_P, SEARCH_TEX_H_P);
        if (mouseOver) mouseWasOverSearch = true;

        if (mouseWasOverSearch && !searchBox.isVisible()) {
            searchBox.setVisible(true);
            searchBox.setFocused(true);
        }

        searchBox.setX(x + SEARCH_FIELD_X_P);
        searchBox.setY(y + SEARCH_FIELD_Y_P);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, Z_TOOLTIP + Z_BOOKMARK_ITEM);
        searchBox.render(guiGraphics, mouseX, mouseY, 0);
        guiGraphics.pose().popPose();

        if (!searchResults.isEmpty()) {
            renderSearchResults(guiGraphics, x, y, mouseX, mouseY);
        }

        if (showAdvancement && advancementsScreen != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(advancementX, advancementY, 200);
            guiGraphics.pose().popPose();
        }
    }

    private void renderSearchResults(GuiGraphics guiGraphics, int baseX, int baseY, int mouseX, int mouseY) {
        int startX = baseX + SEARCH_FIELD_X_P;
        int startY = baseY + SEARCH_FIELD_Y_P + SEARCH_FIELD_H_P + 2;
        int width = 0;
        int lineHeight = 10;
        int maxVisible = 6;

        int shown = Math.min(searchResults.size(), maxVisible);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, Z_BOOK_EDGE + Z_BOOK_EDGE);

        for (int i = 0; i < shown; i++) {
            CodexEntry entry = searchResults.get(i);

            if (this.font.width(entry.title) > width) {
                width = this.font.width(entry.title);
            }
        }

        for (int i = 0; i < shown; i++) {
            CodexEntry entry = searchResults.get(i);
            int yPos = startY + i * lineHeight;

            if (this.font.width(entry.title) > width) {
                width = this.font.width(entry.title);
            }

            boolean hover = mouseX >= startX && mouseX <= startX + width && mouseY >= yPos && mouseY <= yPos + lineHeight;
            int color = hover ? 0xFFFFFF55 : 0xFFFFFFFF;

            guiGraphics.drawString(font, entry.title, startX, yPos, color);
        }

        guiGraphics.fill(startX - 2, startY - 2, startX + width, startY + shown * lineHeight + 2, 0xCC000000);

        guiGraphics.pose().popPose();
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    private float[] getMouseEasedOffset(float baseX, float baseY, double mouseX, double mouseY, float radius, float maxOffset) {
        double dx = mouseX - baseX;
        double dy = mouseY - baseY;
        double dist = Math.sqrt(dx * dx + dy * dy);

        if (dist > radius) return new float[]{0f, 0f};

        float strength = (float) (1.0 - (dist / radius));

        float dirX = (float) (dx / dist);
        float dirY = (float) (dy / dist);

        float offsetX = dirX * strength * maxOffset;
        float offsetY = dirY * strength * maxOffset;

        return new float[]{offsetX, offsetY};
    }

    private void drawColoredOverlay(GuiGraphics guiGraphics, int x_p, int y_p, int x, int y, int width, int height, int z_Layer) {
        if (this.minecraft == null || this.minecraft.player == null) return;

        ItemStack stack = this.minecraft.player.getMainHandItem();

        if (stack.isEmpty()) stack = this.minecraft.player.getOffhandItem();

        if (stack.isEmpty() || !stack.is(ModItems.CODEX_ARCANUM.get())) {
            stack = getBookItem();
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

    private ItemStack getBookItem() {
        ItemStack stack = minecraft.player.getMainHandItem();

        if (stack.isEmpty()) stack = minecraft.player.getOffhandItem();

        if (stack.isEmpty() || !stack.is(ModItems.CODEX_ARCANUM.get())) {
            stack = minecraft.player.getInventory().items.stream()
                    .filter(stackItem -> !stackItem.isEmpty() && stackItem.getItem() == ModItems.CODEX_ARCANUM.get())
                    .findFirst()
                    .orElse(ItemStack.EMPTY);
        }

        return stack;
    }

    public int getTierForEntry(CodexEntry entry) {
        if (entry == null || entry.id == null) return -1;
        if (categories == null || categories.isEmpty()) return -1;

        for (CodexCategory category : categories) {
            if (category == null || category.entries == null) continue;

            for (int i = 0; i < category.entries.size(); i++) {
                CodexEntry e = category.entries.get(i);
                if (e == null || e.id == null) continue;
                if (e.id.equals(entry.id)) {
                    if (category.tiers != null && i < category.tiers.size()) {
                        return category.tiers.get(i);
                    } else {
                        return -1;
                    }
                }
            }
        }

        return -1;
    }

    public CodexCategory getCategoryForEntry(CodexEntry entry) {
        if (entry == null || entry.id == null) return null;
        if (categories == null || categories.isEmpty()) return null;

        for (CodexCategory category : categories) {
            if (category == null || category.entries == null) continue;

            for (CodexEntry e : category.entries) {
                if (e == null || e.id == null) continue;

                if (e.id.equals(entry.id)) {
                    return category;
                }
            }
        }

        return null;
    }
}