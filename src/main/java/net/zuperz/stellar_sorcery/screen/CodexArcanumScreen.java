package net.zuperz.stellar_sorcery.screen;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayoutDrawable;
import mezz.jei.api.ingredients.ITypedIngredient;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IFocusFactory;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.runtime.IJeiRuntime;
import mezz.jei.api.runtime.IClickableIngredient;
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
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FastColor;
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
import net.zuperz.stellar_sorcery.mixin.AdvancementTabMixin;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    private final List<ClickableItemRegion> clickableItemRegions = new ArrayList<>();
    private final List<TextLinkRegion> textLinkRegions = new ArrayList<>();
    private final List<RenderedJeiLayout> renderedJeiLayouts = new ArrayList<>();
    private final Map<String, Optional<IRecipeLayoutDrawable<?>>> jeiLayoutCache = new HashMap<>();
    public static final ResourceLocation BOOK_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/book.png");
    public static final ResourceLocation BOOK_TEXTURE_GRAY =
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
    public boolean showAdvancement = true;
    public int advancementX = 30;
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
    private static final int LINE_HEIGHT = 10;
    private static final int TEXT_COLOR = 0x282828;
    private static final int LINK_COLOR = 0x1f1f1f;
    private static final int LINK_COLOR_LOCKED = 0x141414;
    private static final int LINK_COLOR_HOVER = 0x3a3a3a;
    private static final int LINK_UNDERLINE_OFFSET = 9;
    private static final int SLOT_BORDER_COLOR = 0xFF1B1B1B;

    private static final Pattern LINK_PATTERN = Pattern.compile("\\[\\[(.+?)]]");

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

            ResourceLocation netherRootId = ResourceLocation.parse("stellar_sorcery:get_flower");
            var netherNode = clientAdvancements.getTree().get(netherRootId);

            if (netherNode != null) {
                try {
                    var tabsField = AdvancementsScreen.class.getDeclaredField("tabs");
                    var selectedField = AdvancementsScreen.class.getDeclaredField("selectedTab");
                    tabsField.setAccessible(true);
                    selectedField.setAccessible(true);

                    @SuppressWarnings("unchecked")
                    Map<AdvancementHolder, AdvancementTab> tabs =
                            (Map<net.minecraft.advancements.AdvancementHolder, AdvancementTab>) tabsField.get(advancementsScreen);

                    AdvancementTab createdTab = tabs.get(netherNode.holder());
                    if (createdTab == null) {
                        createdTab = AdvancementTab.create(Minecraft.getInstance(), advancementsScreen, tabs.size(), netherNode);
                        tabs.put(netherNode.holder(), createdTab);
                    }

                    selectedField.set(advancementsScreen, createdTab);

                    clientAdvancements.setSelectedTab(netherNode.holder(), true);

                } catch (ReflectiveOperationException e) {
                    e.printStackTrace();
                }
            } else {
                System.out.println("netherRootId = null");
            }
        }
    }

    public int getImageWidth() {
        return imageWidth;
    }

    public int getImageHeight() {
        return imageHeight;
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
            this.scrollOffset = 0;
            this.selectedPage = 0;

            this.isInCategoryView = false;
            this.selectedEntry = null;

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
    }

    protected void pageWardBack() {

        if (selectedEntry == null) return;

        if (this.selectedPage > 0) {
            this.selectedPage--;
        } else {

            List<CodexEntry> list = getAvailableEntries();
            int index = list.indexOf(selectedEntry);

            if (index > 0) {

                CodexEntry previousEntry = list.get(index - 1);

                CodexTierData tierData = getBookItem().getComponents().get(ModDataComponentTypes.CODEX_TIER.get());
                int playerTier = tierData != null ? tierData.getTier() : 0;

                int previousTier = getTierForEntry(previousEntry);

                if (previousTier > playerTier) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                    return;
                }

                this.selectedEntry = previousEntry;
                this.selectedPage = Math.max(0, previousEntry.right_side.size() - 1);
                this.isInCategoryView = false;
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

            List<CodexEntry> list = getAvailableEntries();
            int index = list.indexOf(selectedEntry);

            if (index < list.size() - 1) {

                CodexEntry nextEntry = list.get(index + 1);

                CodexTierData tierData = getBookItem().getComponents().get(ModDataComponentTypes.CODEX_TIER.get());
                int playerTier = tierData != null ? tierData.getTier() : 0;

                int nextTier = getTierForEntry(nextEntry);

                if (nextTier > playerTier) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                    return;
                }

                this.selectedEntry = nextEntry;
                this.selectedPage = 0;
                this.isInCategoryView = false;
            }
        }

        scrollOffset = 0;
        this.updateButtonVisibility();
    }

    public void updateButtonVisibility() {

        this.backButton.visible = !isInCategoryView;

        if (selectedEntry == null || isInCategoryView) {
            this.forwardButton.visible = false;
            this.backWardButton.visible = false;
            return;
        }

        int entryIndex = entryList.indexOf(selectedEntry);

        if (entryIndex == -1) {
            this.forwardButton.visible = false;
            this.backWardButton.visible = false;
            return;
        }

        this.backWardButton.visible =
                selectedPage > 0 || entryIndex > 0;

        this.forwardButton.visible =
                selectedPage < selectedEntry.right_side.size() - 1 ||
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
            if ("text".equals(module.module_type)) {
                drawY += measureTextHeight(getModuleText(module), areaW - 4);
                continue;
            }

            if ("recipe".equals(module.module_type) || "furnace_recipe".equals(module.module_type)) {
                drawY += getRecipeModuleHeight(module);
                continue;
            }

            drawY += LINE_HEIGHT + 2;
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
        this.hoveredStack = ItemStack.EMPTY;
        clearInteractiveRegions();

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


        if ((isInCategoryView) || (selectedCategory != null && selectedEntry == null)) {
            drawIconAndTitle(guiGraphics, mouseX, mouseY, x, y, getBookItem());
        }

        if (isInCategoryView) {
            renderCategoryOverview(guiGraphics, mouseX, mouseY);
        }
        else if (selectedCategory != null && selectedEntry == null) {
            renderCategoryEntries(guiGraphics, mouseX, mouseY);
        }
        else if (selectedEntry != null) {
            drawSelectedPage(guiGraphics, mouseX, mouseY, x, y);
            drawIconAndTitle(guiGraphics, mouseX, mouseY, x, y);
        }

        renderSearchBar(guiGraphics, mouseX, mouseY);

        renderBg(guiGraphics, delta, mouseX, mouseY);

        renderJeiOverlays(guiGraphics, mouseX, mouseY);

        if (hoveredStack != null && !hoveredStack.isEmpty()) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, Z_TOOLTIP);
            guiGraphics.renderTooltip(this.font, hoveredStack, mouseX, mouseY);
            guiGraphics.pose().popPose();
        }
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

        CodexTierData tierData = getBookItem().getComponents().get(ModDataComponentTypes.CODEX_TIER.get());
        int playerTier = tierData != null ? tierData.getTier() : 0;

        for (CodexCategory cat : categories) {

            long unlocked = cat.entries.stream()
                    .filter(e -> {
                        int index = cat.entries.indexOf(e);
                        int tier = cat.tiers.get(index);
                        return tier <= playerTier;
                    })
                    .count();

            if (unlocked == 0) continue;

            boolean hovered = MouseUtil.isMouseOver(mouseX, mouseY, areaX, drawY, SLOT_WIDTH, SLOT_HEIGHT);

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, Z_TOOLTIP- 10);

            guiGraphics.fill(areaX, drawY, areaX + SLOT_WIDTH, drawY + SLOT_HEIGHT, 0xAA202020);

            if (hovered) {
                guiGraphics.fill(areaX - 1, drawY - 1, areaX + SLOT_WIDTH + 1, drawY + SLOT_HEIGHT + 1, 0xAAFFFFFF);
            }

            guiGraphics.renderItem(cat.icon, areaX + ITEM_PADDING, drawY + (SLOT_HEIGHT - ITEM_SIZE) / 2);
            registerClickableItem(cat.icon, areaX + ITEM_PADDING, drawY + (SLOT_HEIGHT - ITEM_SIZE) / 2, ITEM_SIZE, ITEM_SIZE);

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

            ItemStack entryIcon = RecipeHelper.parseItem(entry.icon);
            guiGraphics.renderItem(entryIcon, areaX + ITEM_PADDING, drawY + (SLOT_HEIGHT - ITEM_SIZE) / 2);
            registerClickableItem(entryIcon, areaX + ITEM_PADDING, drawY + (SLOT_HEIGHT - ITEM_SIZE) / 2, ITEM_SIZE, ITEM_SIZE);

            ItemStack stack = getBookItem().copy();
            stack.set(ModDataComponentTypes.CODEX_TIER.get(), new CodexTierData(entryTierValue));

            renderScaledItem(guiGraphics, stack, areaX + ITEM_PADDING + 8, drawY + (SLOT_HEIGHT - ITEM_SIZE) / 2 + 8, Z_TOOLTIP - 5, 10);

            Component entryTitle = getEntryTitleComponent(entry);
            guiGraphics.drawString(font, entryTitle, areaX + ITEM_SIZE + ITEM_PADDING * 2, drawY + (SLOT_HEIGHT - 8) / 2, 0xFFFFFF);

            guiGraphics.pose().popPose();

            drawY += SLOT_HEIGHT + SLOT_SPACING;
        }

        guiGraphics.disableScissor();
    }

    private void drawIconAndTitle(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y) {
        if (selectedEntry == null) return;

        int yIcon = y + 11;
        int xIcon = x + 146;


        guiGraphics.drawString(this.font, getEntryTitleComponent(selectedEntry), x + 14, y + 14, ChatFormatting.DARK_GRAY.getColor(), false);

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
                        drawY = renderTextModule(guiGraphics, module, drawX, drawY, areaW, mouseX, mouseY);
                    }
                    case "recipe" -> {
                        drawY = renderRecipeModule(guiGraphics, module, drawX, drawY, areaX, areaY, areaW, areaH, mouseX, mouseY);
                    }
                    case "furnace_recipe" -> {
                        drawY = renderRecipeModule(guiGraphics, module, drawX, drawY, areaX, areaY, areaW, areaH, mouseX, mouseY);
                    }
                    default -> {
                        guiGraphics.drawString(this.font, Component.literal("Unknown module type: " + module.module_type), drawX, drawY, 0xFF0000);
                        drawY += LINE_HEIGHT + 2;
                    }
                }
            }
        }

        guiGraphics.disableScissor();
    }

    private int renderTextModule(GuiGraphics guiGraphics, CodexModule module, int drawX, int drawY, int areaW, int mouseX, int mouseY) {
        String text = getModuleText(module);
        if (text.isEmpty()) {
            return drawY;
        }

        TextLayout layout = layoutText(text, drawX, drawY, areaW - 4);
        int playerTier = getPlayerTier();

        for (PositionedTextToken token : layout.tokens) {
            if (token.entry == null || !token.isLink) {
                guiGraphics.drawString(this.font, token.text, token.x, token.y, TEXT_COLOR, false);
                continue;
            }

            boolean locked = isEntryLocked(token.entry, playerTier);
            boolean hovered = MouseUtil.isMouseOver(mouseX, mouseY, token.x, token.y, token.width, LINE_HEIGHT);
            int color = locked ? LINK_COLOR_LOCKED : (hovered ? LINK_COLOR_HOVER : LINK_COLOR);

            guiGraphics.drawString(this.font, token.text, token.x, token.y, color, false);
            guiGraphics.hLine(token.x, token.x + token.width - 1, token.y + LINK_UNDERLINE_OFFSET, color);
            textLinkRegions.add(new TextLinkRegion(token.entry, token.x, token.y, token.width, LINE_HEIGHT, locked));
        }

        return drawY + layout.height + 4;
    }

    private TextLayout layoutText(String text, int startX, int startY, int maxWidth) {
        List<TextSegment> segments = parseTextSegments(text);
        List<TextToken> tokens = tokenizeSegments(segments);
        List<PositionedTextToken> positioned = new ArrayList<>();

        int cursorX = startX;
        int cursorY = startY;

        for (TextToken token : tokens) {
            if (token.newline) {
                cursorX = startX;
                cursorY += LINE_HEIGHT;
                continue;
            }

            int tokenWidth = this.font.width(token.text);
            if (!token.whitespace && cursorX > startX && cursorX - startX + tokenWidth > maxWidth) {
                cursorX = startX;
                cursorY += LINE_HEIGHT;
            }

            if (token.whitespace && cursorX == startX) {
                continue;
            }

            if (!token.whitespace && !token.text.isEmpty()) {
                positioned.add(new PositionedTextToken(token.text, cursorX, cursorY, tokenWidth, token.entry, token.link));
            }

            cursorX += tokenWidth;
        }

        int height = (cursorY - startY) + LINE_HEIGHT;
        return new TextLayout(positioned, height);
    }

    private List<TextSegment> parseTextSegments(String text) {
        List<TextSegment> segments = new ArrayList<>();
        if (text == null || text.isEmpty()) return segments;

        Matcher matcher = LINK_PATTERN.matcher(text);
        int lastIndex = 0;

        while (matcher.find()) {
            if (matcher.start() > lastIndex) {
                segments.add(new TextSegment(text.substring(lastIndex, matcher.start()), null, false));
            }

            String content = matcher.group(1).trim();
            String display = null;
            String ref = content;
            int pipeIndex = content.indexOf('|');
            if (pipeIndex >= 0) {
                display = content.substring(0, pipeIndex).trim();
                ref = content.substring(pipeIndex + 1).trim();
            }

            CodexEntry entry = findEntryForReference(ref);
            if (entry != null) {
                String displayText = (display != null && !display.isEmpty()) ? display : getEntryTitleString(entry);
                if (displayText == null || displayText.isEmpty()) {
                    displayText = ref;
                }
                segments.add(new TextSegment(displayText, entry, true));
            } else {
                segments.add(new TextSegment(matcher.group(0), null, false));
            }

            lastIndex = matcher.end();
        }

        if (lastIndex < text.length()) {
            segments.add(new TextSegment(text.substring(lastIndex), null, false));
        }

        return segments;
    }

    private List<TextToken> tokenizeSegments(List<TextSegment> segments) {
        List<TextToken> tokens = new ArrayList<>();
        for (TextSegment segment : segments) {
            String text = segment.text;
            if (text == null || text.isEmpty()) continue;

            int i = 0;
            while (i < text.length()) {
                char c = text.charAt(i);
                if (c == '\n') {
                    tokens.add(new TextToken("\n", segment.entry, segment.isLink, true, false));
                    i++;
                    continue;
                }

                if (Character.isWhitespace(c)) {
                    int start = i;
                    while (i < text.length()) {
                        char wc = text.charAt(i);
                        if (wc == '\n' || !Character.isWhitespace(wc)) break;
                        i++;
                    }
                    tokens.add(new TextToken(text.substring(start, i), segment.entry, segment.isLink, false, true));
                    continue;
                }

                int start = i;
                while (i < text.length()) {
                    char nc = text.charAt(i);
                    if (Character.isWhitespace(nc) || nc == '\n') break;
                    i++;
                }
                tokens.add(new TextToken(text.substring(start, i), segment.entry, segment.isLink, false, false));
            }
        }
        return tokens;
    }

    private int measureTextHeight(String text, int maxWidth) {
        if (text == null || text.isEmpty()) return 0;
        TextLayout layout = layoutText(text, 0, 0, maxWidth);
        return layout.height + 4;
    }

    private int renderRecipeModule(GuiGraphics guiGraphics, CodexModule module, int drawX, int drawY, int areaX, int areaY, int areaW, int areaH, int mouseX, int mouseY) {
        boolean isFurnace = "furnace_recipe".equals(module.module_type);
        String title = isFurnace ? "Furnace Recipe:" : "Crafting Recipe:";
        int titleColor = isFurnace ? 0xFFAA00 : 0xAAAAFF;

        guiGraphics.drawString(this.font, Component.literal(title), drawX, drawY, titleColor);
        drawY += 12;

        Optional<IRecipeLayoutDrawable<?>> jeiLayout = getJeiLayoutForModule(module);
        if (jeiLayout.isPresent()) {
            IRecipeLayoutDrawable<?> layout = jeiLayout.get();
            Rect2i rect = layout.getRectWithBorder();
            int layoutWidth = rect.getWidth();
            int layoutHeight = rect.getHeight();
            int layoutX = layoutWidth < areaW ? drawX + (areaW - layoutWidth) / 2 : drawX;
            int layoutY = drawY;

            layout.setPosition(layoutX, layoutY);
            renderedJeiLayouts.add(new RenderedJeiLayout(layout, layoutX, layoutY));
            layout.tick();
            layout.drawRecipe(guiGraphics, mouseX, mouseY);

            return drawY + layoutHeight + 6;
        }

        if (isFurnace) {
            ItemStack input = RecipeHelper.parseItem(module.input);
            ItemStack output = RecipeHelper.parseItem(module.output);

            int slotX = drawX;
            int slotY = drawY;
            drawSlotBackground(guiGraphics, slotX, slotY);
            renderItem(guiGraphics, input, slotX + 1, slotY + 1);

            guiGraphics.disableScissor();
            renderItemTooltip(guiGraphics, input, slotX + 1, slotY + 1, mouseX, mouseY);
            guiGraphics.enableScissor(areaX, areaY, areaX + areaW, areaY + areaH);

            guiGraphics.blit(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/gui/arrow.png"), drawX + 25, drawY + 4, 0, 0, 23, 15);

            int outputX = drawX + 50;
            drawSlotBackground(guiGraphics, outputX, slotY);
            renderItem(guiGraphics, output, outputX + 1, slotY + 1);

            guiGraphics.disableScissor();
            renderItemTooltip(guiGraphics, output, outputX + 1, slotY + 1, mouseX, mouseY);
            guiGraphics.enableScissor(areaX, areaY, areaX + areaW, areaY + areaH);

            return drawY + 25;
        }

        List<ItemStack> grid = RecipeHelper.buildCraftingGrid(module);
        ItemStack result = RecipeHelper.parseItem(module.result);

        int slotSize = 18;
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int index = row * 3 + col;
                ItemStack stack = index < grid.size() ? grid.get(index) : ItemStack.EMPTY;

                int slotX = drawX + col * slotSize;
                int slotY = drawY + row * slotSize;

                drawSlotBackground(guiGraphics, slotX, slotY);
                renderItem(guiGraphics, stack, slotX + 1, slotY + 1);

                guiGraphics.disableScissor();
                renderItemTooltip(guiGraphics, stack, slotX + 1, slotY + 1, mouseX, mouseY);
                guiGraphics.enableScissor(areaX, areaY, areaX + areaW, areaY + areaH);
            }
        }

        int resultX = drawX + slotSize * 3 + 20;
        int resultY = drawY + slotSize;

        drawSlotBackground(guiGraphics, resultX, resultY);
        renderItem(guiGraphics, result, resultX + 1, resultY + 1);

        guiGraphics.disableScissor();
        renderItemTooltip(guiGraphics, result, resultX + 1, resultY + 1, mouseX, mouseY);
        guiGraphics.enableScissor(areaX, areaY, areaX + areaW, areaY + areaH);

        return drawY + slotSize * 3 + 25;
    }

    private int getRecipeModuleHeight(CodexModule module) {
        int titleHeight = 12;
        Optional<IRecipeLayoutDrawable<?>> jeiLayout = getJeiLayoutForModule(module);
        if (jeiLayout.isPresent()) {
            int layoutHeight = jeiLayout.get().getRectWithBorder().getHeight();
            return titleHeight + layoutHeight + 6;
        }

        if ("furnace_recipe".equals(module.module_type)) {
            return titleHeight + 25;
        }

        if ("recipe".equals(module.module_type)) {
            return titleHeight + 18 * 3 + 25;
        }

        return titleHeight + LINE_HEIGHT;
    }

    private Optional<IRecipeLayoutDrawable<?>> getJeiLayoutForModule(CodexModule module) {
        if (module == null) return Optional.empty();
        if (JEIPlugin.getJeiRuntime() == null) return Optional.empty();

        String key = buildJeiLayoutKey(module);
        return jeiLayoutCache.computeIfAbsent(key, k -> createJeiLayoutForModule(module));
    }

    private String buildJeiLayoutKey(CodexModule module) {
        return String.valueOf(module.module_type) + "|" +
                String.valueOf(module.recipe_type) + "|" +
                String.valueOf(module.result) + "|" +
                String.valueOf(module.input) + "|" +
                String.valueOf(module.output);
    }

    private Optional<IRecipeLayoutDrawable<?>> createJeiLayoutForModule(CodexModule module) {
        IJeiRuntime runtime = JEIPlugin.getJeiRuntime();
        if (runtime == null) return Optional.empty();

        JeiFocusData focusData = getJeiFocusData(module);
        if (focusData == null || focusData.stack.isEmpty()) return Optional.empty();

        Optional<ITypedIngredient<ItemStack>> typed = runtime.getIngredientManager()
                .createTypedIngredient(VanillaTypes.ITEM_STACK, focusData.stack);
        if (typed.isEmpty()) return Optional.empty();

        IFocusFactory focusFactory = runtime.getJeiHelpers().getFocusFactory();
        IFocus<ItemStack> focus = focusFactory.createFocus(focusData.role, typed.get());
        IFocusGroup focusGroup = focusFactory.createFocusGroup(List.of(focus));

        List<RecipeType<?>> preferredTypes = getPreferredRecipeTypes(module);
        return createJeiLayoutForFocus(focus, focusGroup, preferredTypes);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private Optional<IRecipeLayoutDrawable<?>> createJeiLayoutForFocus(IFocus<?> focus, IFocusGroup focusGroup, List<RecipeType<?>> preferredTypes) {
        IJeiRuntime runtime = JEIPlugin.getJeiRuntime();
        if (runtime == null) return Optional.empty();

        var recipeManager = runtime.getRecipeManager();
        List<IRecipeCategory<?>> categories = new ArrayList<>();

        for (RecipeType<?> type : preferredTypes) {
            try {
                IRecipeCategory category = recipeManager.getRecipeCategory((RecipeType) type);
                if (category != null) {
                    categories.add(category);
                }
            } catch (Exception ignored) {
            }
        }

        if (categories.isEmpty()) {
            categories.addAll(recipeManager.createRecipeCategoryLookup()
                    .limitFocus(List.of(focus))
                    .get()
                    .toList());
        }

        for (IRecipeCategory category : categories) {
            RecipeType type = category.getRecipeType();
            Optional recipe = recipeManager.createRecipeLookup(type)
                    .limitFocus(List.of(focus))
                    .get()
                    .findFirst();

            if (recipe.isEmpty()) continue;

            Optional layout = recipeManager.createRecipeLayoutDrawable(category, recipe.get(), focusGroup);
            if (layout.isPresent()) {
                return layout;
            }
        }

        return Optional.empty();
    }

    private void renderJeiOverlays(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (renderedJeiLayouts.isEmpty()) return;
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, Z_TOOLTIP);
        for (RenderedJeiLayout rendered : renderedJeiLayouts) {
            rendered.layout.drawOverlays(guiGraphics, mouseX, mouseY);
        }
        guiGraphics.pose().popPose();
    }

    private List<RecipeType<?>> getPreferredRecipeTypes(CodexModule module) {
        List<RecipeType<?>> types = new ArrayList<>();
        if ("recipe".equals(module.module_type)) {
            String type = module.recipe_type == null ? "" : module.recipe_type.toLowerCase();
            if (type.isEmpty() || "crafting_table".equals(type)) {
                types.add(RecipeTypes.CRAFTING);
            } else if ("stonecutting".equals(type)) {
                types.add(RecipeTypes.STONECUTTING);
            } else if ("smithing".equals(type)) {
                types.add(RecipeTypes.SMITHING);
            }
        } else if ("furnace_recipe".equals(module.module_type)) {
            types.add(RecipeTypes.SMELTING);
            types.add(RecipeTypes.BLASTING);
            types.add(RecipeTypes.SMOKING);
            types.add(RecipeTypes.CAMPFIRE_COOKING);
        }
        return types;
    }

    private JeiFocusData getJeiFocusData(CodexModule module) {
        if ("furnace_recipe".equals(module.module_type) && module.input != null) {
            ItemStack input = RecipeHelper.parseItem(module.input);
            if (!input.isEmpty()) {
                return new JeiFocusData(input, RecipeIngredientRole.INPUT);
            }
        }

        String resultId = module.result != null ? module.result : module.output;
        ItemStack result = RecipeHelper.parseItem(resultId);
        if (!result.isEmpty()) {
            return new JeiFocusData(result, RecipeIngredientRole.OUTPUT);
        }

        return null;
    }

    private void drawSlotBackground(GuiGraphics guiGraphics, int x, int y) {
        guiGraphics.fill(x, y, x + 18, y + 18, SLOT_BORDER_COLOR);
    }

    private void renderItemWithTooltip(GuiGraphics guiGraphics, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        guiGraphics.fill(x, y, x + 16, y + 16, 0xFF555555);
        guiGraphics.renderItem(stack, x, y);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, Z_TOOLTIP + 100);
        guiGraphics.renderItemDecorations(this.font, stack, x, y, null);
        guiGraphics.pose().popPose();

        registerClickableItem(stack, x, y, 16, 16);

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

        registerClickableItem(stack, x, y, 16, 16);
    }

    private void renderItemTooltip(GuiGraphics guiGraphics, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        if (mouseX >= x && mouseX <= x + 16 && mouseY >= y && mouseY <= y + 16 && !stack.isEmpty()) {
            hoveredStack = stack;
        }
    }

    private void clearInteractiveRegions() {
        clickableItemRegions.clear();
        textLinkRegions.clear();
        renderedJeiLayouts.clear();
    }

    private void registerClickableItem(ItemStack stack, int x, int y, int width, int height) {
        if (stack == null || stack.isEmpty()) return;
        clickableItemRegions.add(new ClickableItemRegion(stack, x, y, width, height));
    }

    public Optional<IClickableIngredient<?>> getJeiClickableIngredient(double mouseX, double mouseY) {
        IJeiRuntime runtime = JEIPlugin.getJeiRuntime();
        if (runtime == null) return Optional.empty();

        for (int i = renderedJeiLayouts.size() - 1; i >= 0; i--) {
            RenderedJeiLayout rendered = renderedJeiLayouts.get(i);
            Optional<mezz.jei.api.gui.inputs.RecipeSlotUnderMouse> slotUnderMouse =
                    rendered.layout.getSlotUnderMouse(mouseX, mouseY);
            if (slotUnderMouse.isEmpty()) continue;

            var slot = slotUnderMouse.get().slot();
            Optional<ITypedIngredient<?>> displayed = slot.getDisplayedIngredient();
            if (displayed.isEmpty()) continue;

            Rect2i rect = slot.getAreaIncludingBackground();
            Rect2i area = new Rect2i(rendered.x + rect.getX(), rendered.y + rect.getY(), rect.getWidth(), rect.getHeight());
            return createClickableIngredient(runtime, displayed.get(), area);
        }

        var ingredientManager = runtime.getIngredientManager();
        for (int i = clickableItemRegions.size() - 1; i >= 0; i--) {
            ClickableItemRegion region = clickableItemRegions.get(i);
            if (!MouseUtil.isMouseOver(mouseX, mouseY, region.x, region.y, region.width, region.height)) continue;

            Rect2i area = new Rect2i(region.x, region.y, region.width, region.height);
            Optional<IClickableIngredient<ItemStack>> clickable =
                    ingredientManager.createClickableIngredient(VanillaTypes.ITEM_STACK, region.stack, area, true);
            if (clickable.isPresent()) {
                return Optional.of(clickable.get());
            }
        }

        return Optional.empty();
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

            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;

            int advX = x + 16;
            int advY = y + 22;
            int advW = 92;
            int advH = 138;

            //if (MouseUtil.isMouseOver(mouseX, mouseY, advX, advY, advW, advH)) {

                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(advancementX, advancementY, 0);

                CustomAdvancementRenderer.renderTooltipsOnly(
                        advancementsScreen,
                        guiGraphics,
                        mouseX - advancementX,
                        mouseY - advancementY,
                        (this.width - 252) / 2,
                        (this.height - 140) / 2,
                        this
                );

                guiGraphics.pose().popPose();
            //}
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        if (handleTextLinkClick(mouseX, mouseY)) {
            return true;
        }

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

                    CodexTierData tierData = getBookItem().getComponents().get(ModDataComponentTypes.CODEX_TIER.get());
                    int playerTier = tierData != null ? tierData.getTier() : 0;

                    List<CodexEntry> unlocked = new ArrayList<>();

                    for (int i = 0; i < cat.entries.size(); i++) {
                        if (cat.tiers.get(i) <= playerTier) {
                            unlocked.add(cat.entries.get(i));
                        }
                    }

                    this.selectedCategory = cat;
                    this.selectedEntry = null;
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

    private boolean handleTextLinkClick(double mouseX, double mouseY) {
        if (textLinkRegions.isEmpty() || this.minecraft == null) return false;

        for (int i = textLinkRegions.size() - 1; i >= 0; i--) {
            TextLinkRegion region = textLinkRegions.get(i);
            if (!MouseUtil.isMouseOver(mouseX, mouseY, region.x, region.y, region.width, region.height)) {
                continue;
            }

            if (region.locked) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.VILLAGER_NO, 1.0F));
                return true;
            }

            openEntry(region.entry);
            this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, 1.0F));
            return true;
        }

        return false;
    }

    private void openEntry(CodexEntry entry) {
        if (entry == null) return;
        this.selectedEntry = entry;
        this.selectedCategory = getCategoryForEntry(entry);
        this.selectedPage = 0;
        this.scrollOffset = 0;
        this.isInCategoryView = false;
        updateButtonVisibility();
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
                //return this.advancementsScreen.mouseDragged(adj[0], adj[1], button, dragX, dragY);

                AdvancementTab selected = CustomAdvancementRenderer.getSelectedTab(advancementsScreen);
                AdvancementsScreenMixin screenMixin = (AdvancementsScreenMixin) advancementsScreen;

                if (button != 0) {
                    screenMixin.setIsScrolling(false);
                    return false;
                } else {
                    if (!screenMixin.getIsScrolling()) {
                        screenMixin.setIsScrolling(true);
                    } else if (selected != null) {
                        scroll(dragX, dragY);
                    }

                    return true;
                }
            }
        }

        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    public void scroll(double deltaX, double deltaY) {
        AdvancementTab selected = CustomAdvancementRenderer.getSelectedTab(advancementsScreen);
        AdvancementTabMixin tabMixin = (AdvancementTabMixin) selected;

        int viewWidth = 52;
        int viewHeight = 82;

        if (tabMixin.getMaxX() - tabMixin.getMinX() > viewWidth) {
            tabMixin.setScrollX(Mth.clamp(
                    tabMixin.getScrollX() + deltaX,
                    -(tabMixin.getMaxX() - viewWidth),
                    0.0
            ));
        }

        if (tabMixin.getMaxY() - tabMixin.getMinY() > viewHeight) {
            tabMixin.setScrollY(Mth.clamp(
                    tabMixin.getScrollY() + deltaY,
                    -(tabMixin.getMaxY() - viewHeight),
                    0.0
            ));
        }
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

        registerClickableItem(stack, x, y, size, size);

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
            boolean matched = false;
            if (entry.search_items != null) {
                for (String tag : entry.search_items) {
                    if (tag.toLowerCase().contains(lowerQuery)) {
                        matched = true;
                        break;
                    }
                }
            }

            if (!matched) {
                String title = getEntryTitleString(entry);
                if (!title.isBlank() && title.toLowerCase().contains(lowerQuery)) {
                    matched = true;
                }
            }

            if (matched) {
                searchResults.add(entry);
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
            Component title = getEntryTitleComponent(entry);
            int titleWidth = this.font.width(title);
            if (titleWidth > width) {
                width = titleWidth;
            }
        }

        for (int i = 0; i < shown; i++) {
            CodexEntry entry = searchResults.get(i);
            int yPos = startY + i * lineHeight;
            Component title = getEntryTitleComponent(entry);
            int titleWidth = this.font.width(title);

            if (titleWidth > width) {
                width = titleWidth;
            }

            boolean hover = mouseX >= startX && mouseX <= startX + width && mouseY >= yPos && mouseY <= yPos + lineHeight;
            int color = hover ? 0xFFFFFF55 : 0xFFFFFFFF;

            guiGraphics.drawString(font, title, startX, yPos, color);
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

    private Component getEntryTitleComponent(CodexEntry entry) {
        if (entry == null) return Component.empty();
        if (entry.title_key != null && !entry.title_key.isBlank()) {
            return Component.translatable(entry.title_key);
        }
        if (entry.title != null && !entry.title.isBlank()) {
            return Component.literal(entry.title);
        }
        return Component.empty();
    }

    private String getEntryTitleString(CodexEntry entry) {
        return getEntryTitleComponent(entry).getString();
    }

    private String getModuleText(CodexModule module) {
        if (module == null) return "";
        if (module.text_key != null && !module.text_key.isBlank()) {
            return Component.translatable(module.text_key).getString();
        }
        return module.text != null ? module.text : "";
    }

    private int getPlayerTier() {
        CodexTierData tierData = getBookItem().getComponents().get(ModDataComponentTypes.CODEX_TIER.get());
        return tierData != null ? tierData.getTier() : 0;
    }

    private boolean isEntryLocked(CodexEntry entry, int playerTier) {
        int entryTier = getTierForEntry(entry);
        return entryTier >= 0 && entryTier > playerTier;
    }

    private CodexEntry findEntryForReference(String reference) {
        if (reference == null || reference.isBlank()) return null;

        String ref = reference.trim();
        for (CodexEntry entry : entryList) {
            if (entry != null && entry.id != null && entry.id.equalsIgnoreCase(ref)) {
                return entry;
            }
        }

        String normalizedRef = normalizeItemId(ref);
        for (CodexEntry entry : entryList) {
            if (entry == null) continue;

            if (entry.icon != null && normalizeItemId(entry.icon).equalsIgnoreCase(normalizedRef)) {
                return entry;
            }

            if (entry.search_items != null) {
                for (String tag : entry.search_items) {
                    if (tag != null && (tag.equalsIgnoreCase(ref) || tag.equalsIgnoreCase(normalizedRef))) {
                        return entry;
                    }
                }
            }

            String title = getEntryTitleString(entry);
            if (!title.isBlank() && title.equalsIgnoreCase(ref)) {
                return entry;
            }
        }

        return null;
    }

    private String normalizeItemId(String id) {
        if (id == null || id.isBlank()) return "";
        String trimmed = id.trim();
        return trimmed.contains(":") ? trimmed : "minecraft:" + trimmed;
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

    private static class ClickableItemRegion {
        private final ItemStack stack;
        private final int x;
        private final int y;
        private final int width;
        private final int height;

        private ClickableItemRegion(ItemStack stack, int x, int y, int width, int height) {
            this.stack = stack;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
    }

    private static class TextLinkRegion {
        private final CodexEntry entry;
        private final int x;
        private final int y;
        private final int width;
        private final int height;
        private final boolean locked;

        private TextLinkRegion(CodexEntry entry, int x, int y, int width, int height, boolean locked) {
            this.entry = entry;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.locked = locked;
        }
    }

    private static class RenderedJeiLayout {
        private final IRecipeLayoutDrawable<?> layout;
        private final int x;
        private final int y;

        private RenderedJeiLayout(IRecipeLayoutDrawable<?> layout, int x, int y) {
            this.layout = layout;
            this.x = x;
            this.y = y;
        }
    }

    private static class TextSegment {
        private final String text;
        private final CodexEntry entry;
        private final boolean isLink;

        private TextSegment(String text, CodexEntry entry, boolean isLink) {
            this.text = text;
            this.entry = entry;
            this.isLink = isLink;
        }
    }

    private static class TextToken {
        private final String text;
        private final CodexEntry entry;
        private final boolean link;
        private final boolean newline;
        private final boolean whitespace;

        private TextToken(String text, CodexEntry entry, boolean link, boolean newline, boolean whitespace) {
            this.text = text;
            this.entry = entry;
            this.link = link;
            this.newline = newline;
            this.whitespace = whitespace;
        }
    }

    private static class PositionedTextToken {
        private final String text;
        private final int x;
        private final int y;
        private final int width;
        private final CodexEntry entry;
        private final boolean isLink;

        private PositionedTextToken(String text, int x, int y, int width, CodexEntry entry, boolean isLink) {
            this.text = text;
            this.x = x;
            this.y = y;
            this.width = width;
            this.entry = entry;
            this.isLink = isLink;
        }
    }

    private static class TextLayout {
        private final List<PositionedTextToken> tokens;
        private final int height;

        private TextLayout(List<PositionedTextToken> tokens, int height) {
            this.tokens = tokens;
            this.height = height;
        }
    }

    private static class JeiFocusData {
        private final ItemStack stack;
        private final RecipeIngredientRole role;

        private JeiFocusData(ItemStack stack, RecipeIngredientRole role) {
            this.stack = stack;
            this.role = role;
        }
    }

    private static <T> Optional<IClickableIngredient<?>> createClickableIngredient(
            IJeiRuntime runtime,
            ITypedIngredient<T> typedIngredient,
            Rect2i area
    ) {
        return runtime.getIngredientManager()
                .createClickableIngredient(typedIngredient.getType(), typedIngredient.getIngredient(), area, true)
                .map(clickable -> (IClickableIngredient<?>) clickable);
    }

    private List<CodexEntry> getAvailableEntries() {

        if (selectedCategory == null) return List.of();

        CodexTierData tierData = getBookItem().getComponents().get(ModDataComponentTypes.CODEX_TIER.get());
        int playerTier = tierData != null ? tierData.getTier() : 0;

        List<CodexEntry> list = new ArrayList<>();

        for (int i = 0; i < selectedCategory.entries.size(); i++) {

            if (selectedCategory.tiers.get(i) <= playerTier) {

                String entryId = selectedCategory.entries.get(i).id;

                CodexEntry entry = entryList.stream()
                        .filter(e -> e.id.equals(entryId))
                        .findFirst()
                        .orElse(null);

                if (entry != null) list.add(entry);
            }
        }

        return list;
    }
}
