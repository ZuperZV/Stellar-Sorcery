package net.zuperz.stellar_sorcery.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.zuperz.stellar_sorcery.screen.Helpers.RecipeHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class ItemSelectionScreen extends Screen {
    private static final int GRID_COLS = 8;
    private static final int ITEM_SIZE = 18;
    private static final int PADDING = 8;
    private static final int SEARCH_BOX_HEIGHT = 20;
    private static final int GRID_TOP_OFFSET = 90;

    private final Screen parentScreen;
    private final ItemSelectionCallback callback;

    private List<Item> allItems;
    private List<Item> filteredItems;
    private EditBox searchBox;
    private int scrollOffset = 0;

    private ItemStack currentlySelectedItem = ItemStack.EMPTY;

    public interface ItemSelectionCallback {
        void onItemSelected(String itemId);
    }

    public ItemSelectionScreen(Screen parentScreen, ItemSelectionCallback callback) {
        this(parentScreen, callback, ItemStack.EMPTY);
    }

    public ItemSelectionScreen(Screen parentScreen, ItemSelectionCallback callback, ItemStack currentItem) {
        super(Component.literal("Select Item"));
        this.parentScreen = parentScreen;
        this.callback = callback;
        this.currentlySelectedItem = currentItem != null ? currentItem : ItemStack.EMPTY;
        this.allItems = new ArrayList<>(BuiltInRegistries.ITEM.stream().collect(Collectors.toList()));
        this.filteredItems = new ArrayList<>(allItems);
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();

        addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> onClose())
                .bounds(PADDING, PADDING, 80, 20)
                .build());

        int titleX = (width - font.width("Select Item")) / 2;

        int selectedItemY = PADDING + 30;
        addRenderableWidget(Button.builder(Component.literal("Selected Item"), button -> {
        }).bounds(PADDING, selectedItemY, 200, 20).build());

        searchBox = new EditBox(font, PADDING, selectedItemY + 30, width - 2 * PADDING, SEARCH_BOX_HEIGHT, Component.literal("Search"));
        searchBox.setMaxLength(50);
        searchBox.setResponder(this::updateSearch);
        addRenderableWidget(searchBox);
    }

    private void updateSearch(String searchTerm) {
        scrollOffset = 0;
        if (searchTerm.isEmpty()) {
            filteredItems = new ArrayList<>(allItems);
        } else {
            String term = searchTerm.toLowerCase();
            filteredItems = allItems.stream()
                    .filter(item -> {
                        String id = BuiltInRegistries.ITEM.getKey(item).toString();
                        String name = item.getDescription().getString().toLowerCase();
                        return id.contains(term) || name.contains(term);
                    })
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int titleX = (width - font.width("Select Item")) / 2;
        guiGraphics.drawString(font, "Select Item", titleX, PADDING, 0xFFFFFF);

        if (!currentlySelectedItem.isEmpty()) {
            guiGraphics.drawString(font, "Current: " + currentlySelectedItem.getHoverName().getString(), PADDING + 10, PADDING + 35, 0xFFFFFF);
        }

        int gridY = GRID_TOP_OFFSET;
        int gridHeight = height - gridY - PADDING;
        int visibleRows = Math.max(1, gridHeight / ITEM_SIZE);
        int totalRows = (filteredItems.size() + GRID_COLS - 1) / GRID_COLS;
        int maxScroll = Math.max(0, (totalRows - visibleRows) * ITEM_SIZE);

        guiGraphics.enableScissor(PADDING, gridY, width - PADDING, height - PADDING);

        for (int i = 0; i < filteredItems.size(); i++) {
            int row = i / GRID_COLS;
            int col = i % GRID_COLS;
            int x = PADDING + col * ITEM_SIZE;
            int y = gridY + row * ITEM_SIZE - scrollOffset;

            if (y + ITEM_SIZE < gridY || y > height - PADDING) {
                continue;
            }

            Item item = filteredItems.get(i);
            ItemStack stack = new ItemStack(item);

            guiGraphics.fill(x, y, x + ITEM_SIZE, y + ITEM_SIZE, 0xFF1B1B1B);

            if (mouseX >= x && mouseX < x + ITEM_SIZE && mouseY >= y && mouseY < y + ITEM_SIZE) {
                guiGraphics.fill(x, y, x + ITEM_SIZE, y + ITEM_SIZE, 0xFF3F3F3F);
            }

            guiGraphics.renderItem(stack, x + 1, y + 1);
        }

        guiGraphics.disableScissor();
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int gridY = GRID_TOP_OFFSET;
        int gridHeight = height - gridY - PADDING;
        int visibleRows = Math.max(1, gridHeight / ITEM_SIZE);
        int totalRows = (filteredItems.size() + GRID_COLS - 1) / GRID_COLS;
        int maxScroll = Math.max(0, (totalRows - visibleRows) * ITEM_SIZE);

        scrollOffset = Math.max(0, Math.min(maxScroll, (int) (scrollOffset - scrollY * 10)));
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }

        int gridY = GRID_TOP_OFFSET;
        int gridHeight = height - gridY - PADDING;

        if (mouseX >= PADDING && mouseX < width - PADDING && mouseY >= gridY && mouseY < gridY + gridHeight) {
            int relativeY = (int) (mouseY - gridY + scrollOffset);
            int row = relativeY / ITEM_SIZE;
            int col = (int) ((mouseX - PADDING) / ITEM_SIZE);
            int index = row * GRID_COLS + col;

            if (index >= 0 && index < filteredItems.size()) {
                Item item = filteredItems.get(index);
                ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
                if (id != null && callback != null) {
                    callback.onItemSelected(id.toString());
                    onClose();
                }
                return true;
            }
        }

        return false;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parentScreen);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}

