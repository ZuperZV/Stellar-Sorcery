package net.zuperz.stellar_sorcery.screen;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zuperz.stellar_sorcery.data.CodexDataLoader;
import net.zuperz.stellar_sorcery.data.CodexEditorPersistence;
import net.zuperz.stellar_sorcery.data.CodexEditorProject;
import net.zuperz.stellar_sorcery.network.SaveCodexEditorPacket;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class CodexEditorScreen extends Screen {
    private static final int OUTER_MARGIN = 12;
    private static final int COLUMN_GAP = 8;
    private static final int LEFT_COLUMN_WIDTH = 150;
    private static final int MIDDLE_COLUMN_WIDTH = 150;
    private static final int TOP_BAR_HEIGHT = 24;
    private static final int BUTTON_HEIGHT = 20;
    private static final int FIELD_HEIGHT = 18;
    private static final int FIELD_ROW_SPACING = 30;
    private static final int LABEL_GAP = -2;
    private static final int LIST_ROW_HEIGHT = 20;
    private static final int CATEGORY_VISIBLE_ROWS = 6;
    private static final int ENTRY_VISIBLE_ROWS = 6;
    private static final int MODULE_VISIBLE_ROWS = 4;

    private CodexEditorProject project;

    private int selectedCategoryIndex;
    private int selectedEntryIndex;
    private int selectedPageIndex;
    private int selectedModuleIndex;

    private int categoryScrollOffset;
    private int entryScrollOffset;
    private int moduleScrollOffset;

    private boolean syncingFields;
    private boolean saveInProgress;

    private EditorTab activeTab = EditorTab.ENTRY;

    private final List<Button> categoryButtons = new ArrayList<>();
    private final List<Button> entryButtons = new ArrayList<>();
    private final List<Button> moduleButtons = new ArrayList<>();
    private final List<EditBox> allEditBoxes = new ArrayList<>();

    private Button saveButton;
    private Button categoryUpButton;
    private Button categoryDownButton;
    private Button entryUpButton;
    private Button entryDownButton;
    private Button moduleUpButton;
    private Button moduleDownButton;
    private Button addCategoryButton;
    private Button deleteCategoryButton;
    private Button addEntryButton;
    private Button deleteEntryButton;
    private Button entryTabButton;
    private Button moduleTabButton;
    private Button previousPageButton;
    private Button nextPageButton;
    private Button addPageButton;
    private Button deletePageButton;
    private Button addTextModuleButton;
    private Button addRecipeModuleButton;
    private Button addFurnaceModuleButton;
    private Button deleteModuleButton;
    private Button moduleTypeButton;

    private String lastCategoryTitle = "";
    private String lastEntryTitle = "";
    private boolean categoryIdWasManuallyChanged = false;
    private boolean entryIdWasManuallyChanged = false;

    private EditBox categoryIdBox;
    private EditBox categoryTitleBox;
    private EditBox categoryIconBox;

    private EditBox entryIdBox;
    private EditBox entryTitleBox;
    private EditBox entryTypeBox;
    private EditBox entryIconBox;
    private EditBox entryTierBox;
    private EditBox entrySearchBox;
    private EditBox entryRelatedBox;

    private EditBox textModuleBox;

    private EditBox recipeTypeBox;
    private EditBox recipeResultBox;
    private EditBox recipePattern1Box;
    private EditBox recipePattern2Box;
    private EditBox recipePattern3Box;
    private EditBox recipeKeysBox;

    private EditBox furnaceInputBox;
    private EditBox furnaceOutputBox;
    private EditBox furnaceExperienceBox;
    private EditBox furnaceTimeBox;

    private Button categoryIconSelectButton;

    private Button entryIconSelectButton;
    private Button entrySearchSelectButton;
    private Button entryRelatedSelectButton;

    private Button recipeResultSelectButton;

    private Button furnaceInputSelectButton;
    private Button furnaceOutputSelectButton;

    private int refreshCounter = 9;

    public CodexEditorScreen(CodexEditorProject project) {
        super(Component.literal("Codex Editor"));
        this.project = project;
        this.project.normalize();
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();
        categoryButtons.clear();
        entryButtons.clear();
        moduleButtons.clear();
        allEditBoxes.clear();

        ensureSelections();

        int rightX = getRightColumnX();
        int rightWidth = getRightColumnWidth();

        saveButton = addRenderableWidget(Button.builder(Component.literal("Save"), button -> saveProject())
                .bounds(width - OUTER_MARGIN - 90, OUTER_MARGIN, 90, BUTTON_HEIGHT)
                .build());
        addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, button -> onClose())
                .bounds(OUTER_MARGIN, OUTER_MARGIN, 80, BUTTON_HEIGHT)
                .build());

        initCategoryList();
        initEntryList();
        initCategoryFields();
        initEntryFields(rightX, rightWidth);
        initModuleFields(rightX, rightWidth);

        entryTabButton = addRenderableWidget(Button.builder(Component.literal("Entry"), button -> switchTab(EditorTab.ENTRY))
                .bounds(rightX + 40, OUTER_MARGIN + TOP_BAR_HEIGHT, 70, BUTTON_HEIGHT)
                .build());
        moduleTabButton = addRenderableWidget(Button.builder(Component.literal("Module"), button -> switchTab(EditorTab.MODULE))
                .bounds(rightX + 76 + 40, OUTER_MARGIN + TOP_BAR_HEIGHT, 80, BUTTON_HEIGHT)
                .build());

        refreshAllState();
    }

    private void initCategoryList() {
        int x = OUTER_MARGIN;
        int top = OUTER_MARGIN + TOP_BAR_HEIGHT + 18;

        categoryUpButton = addRenderableWidget(Button.builder(Component.literal("^"), button -> {
            categoryScrollOffset = Math.max(0, categoryScrollOffset - 1);
            refreshCategoryButtons();
        }).bounds(x + LEFT_COLUMN_WIDTH - 42, top - 18, 20, 18).build());

        categoryDownButton = addRenderableWidget(Button.builder(Component.literal("v"), button -> {
            categoryScrollOffset = Math.min(getMaxCategoryScroll(), categoryScrollOffset + 1);
            refreshCategoryButtons();
        }).bounds(x + LEFT_COLUMN_WIDTH - 20, top - 18, 20, 18).build());

        for (int i = 0; i < CATEGORY_VISIBLE_ROWS; i++) {
            final int row = i;
            Button button = addRenderableWidget(Button.builder(Component.empty(), btn -> {
                selectedCategoryIndex = categoryScrollOffset + row;
                selectedEntryIndex = 0;
                selectedPageIndex = 0;
                selectedModuleIndex = 0;
                ensureSelections();
                refreshAllState();
            }).bounds(x, top + i * LIST_ROW_HEIGHT, LEFT_COLUMN_WIDTH, LIST_ROW_HEIGHT).build());
            categoryButtons.add(button);
        }

        addCategoryButton = addRenderableWidget(Button.builder(Component.literal("+ Category"), button -> {
            CodexEditorProject.Category category = new CodexEditorProject.Category();
            category.id = "category_" + (project.categories.size() + 1);
            category.title = "Category " + (project.categories.size() + 1);
            category.entries = new ArrayList<>();
            category.entries.add(createDefaultEntry(category.entries.size()));
            category.normalize(project.categories.size());
            project.categories.add(category);
            selectedCategoryIndex = project.categories.size() - 1;
            selectedEntryIndex = 0;
            selectedPageIndex = 0;
            selectedModuleIndex = 0;
            refreshAllState();
        }).bounds(x, top + CATEGORY_VISIBLE_ROWS * LIST_ROW_HEIGHT + 8, 95, BUTTON_HEIGHT).build());

        deleteCategoryButton = addRenderableWidget(Button.builder(Component.literal("Delete"), button -> {
            if (selectedCategory() == null) {
                return;
            }
            project.categories.remove(selectedCategoryIndex);
            selectedCategoryIndex = Math.max(0, selectedCategoryIndex - 1);
            selectedEntryIndex = 0;
            selectedPageIndex = 0;
            selectedModuleIndex = 0;
            refreshAllState();
        }).bounds(x + 100, top + CATEGORY_VISIBLE_ROWS * LIST_ROW_HEIGHT + 8, 50, BUTTON_HEIGHT).build());
    }

    private void initEntryList() {
        int x = OUTER_MARGIN + LEFT_COLUMN_WIDTH + COLUMN_GAP;
        int top = OUTER_MARGIN + TOP_BAR_HEIGHT + 18;

        entryUpButton = addRenderableWidget(Button.builder(Component.literal("^"), button -> {
            entryScrollOffset = Math.max(0, entryScrollOffset - 1);
            refreshEntryButtons();
        }).bounds(x + MIDDLE_COLUMN_WIDTH - 42, top - 18, 20, 18).build());

        entryDownButton = addRenderableWidget(Button.builder(Component.literal("v"), button -> {
            entryScrollOffset = Math.min(getMaxEntryScroll(), entryScrollOffset + 1);
            refreshEntryButtons();
        }).bounds(x + MIDDLE_COLUMN_WIDTH - 20, top - 18, 20, 18).build());

        for (int i = 0; i < ENTRY_VISIBLE_ROWS; i++) {
            final int row = i;
            Button button = addRenderableWidget(Button.builder(Component.empty(), btn -> {
                selectedEntryIndex = entryScrollOffset + row;
                selectedPageIndex = 0;
                selectedModuleIndex = 0;
                ensureSelections();
                refreshAllState();
            }).bounds(x, top + i * LIST_ROW_HEIGHT, MIDDLE_COLUMN_WIDTH, LIST_ROW_HEIGHT).build());
            entryButtons.add(button);
        }

        addEntryButton = addRenderableWidget(Button.builder(Component.literal("+ Entry"), button -> {
            CodexEditorProject.Category category = selectedCategory();
            if (category == null) {
                return;
            }
            category.entries.add(createDefaultEntry(category.entries.size()));
            selectedEntryIndex = category.entries.size() - 1;
            selectedPageIndex = 0;
            selectedModuleIndex = 0;
            refreshAllState();
        }).bounds(x, top + ENTRY_VISIBLE_ROWS * LIST_ROW_HEIGHT + 8, 85, BUTTON_HEIGHT).build());

        deleteEntryButton = addRenderableWidget(Button.builder(Component.literal("Delete"), button -> {
            CodexEditorProject.Category category = selectedCategory();
            if (category == null || selectedEntry() == null) {
                return;
            }
            category.entries.remove(selectedEntryIndex);
            selectedEntryIndex = Math.max(0, selectedEntryIndex - 1);
            selectedPageIndex = 0;
            selectedModuleIndex = 0;
            refreshAllState();
        }).bounds(x + 90, top + ENTRY_VISIBLE_ROWS * LIST_ROW_HEIGHT + 8, 60, BUTTON_HEIGHT).build());
    }

    private void initCategoryFields() {
        int x = OUTER_MARGIN;
        int y = OUTER_MARGIN + TOP_BAR_HEIGHT + 18 + CATEGORY_VISIBLE_ROWS * LIST_ROW_HEIGHT + 36;

        categoryIdBox = addField(new EditBox(font, x, y, LEFT_COLUMN_WIDTH - 22, FIELD_HEIGHT, Component.empty()));
        categoryTitleBox = addField(new EditBox(font, x, y + FIELD_ROW_SPACING, LEFT_COLUMN_WIDTH, FIELD_HEIGHT, Component.empty()));
        categoryIconBox = addField(new EditBox(font, x, y + FIELD_ROW_SPACING * 2, LEFT_COLUMN_WIDTH - 22, FIELD_HEIGHT, Component.empty()));

        categoryIconSelectButton = addRenderableWidget(
                Button.builder(Component.literal("..."), button -> {
                    minecraft.setScreen(new ItemSelectionScreen(this, itemId -> {
                        if (selectedCategory() != null) {
                            selectedCategory().icon = itemId;
                            categoryIconBox.setValue(itemId);
                        }
                    }));
                }).bounds(x + LEFT_COLUMN_WIDTH - 20, y + FIELD_ROW_SPACING * 2, 20, FIELD_HEIGHT).build());

        categoryIdBox.setResponder(value -> {
            if (syncingFields || selectedCategory() == null) {
                return;
            }
            selectedCategory().id = value;
            refreshCategoryButtons();
        });
        categoryTitleBox.setResponder(value -> {
            if (syncingFields || selectedCategory() == null) {
                return;
            }

            CodexEditorProject.Category category = selectedCategory();

            String oldGeneratedId = generateId(lastCategoryTitle);

            if (category.id.isBlank() || category.id.equals(oldGeneratedId)) {
                String newId = generateId(value);

                category.id = newId;

                syncingFields = true;
                categoryIdBox.setValue(newId);
                syncingFields = false;
            }

            category.title = value;
            lastCategoryTitle = value;

            refreshCategoryButtons();
        });
        categoryIconBox.setResponder(value -> {
            if (syncingFields || selectedCategory() == null) {
                return;
            }
            selectedCategory().icon = value;
        });
    }

    private void initEntryFields(int rightX, int rightWidth) {
        int y = OUTER_MARGIN + TOP_BAR_HEIGHT + 46;

        entryIdBox = addField(new EditBox(font, rightX, y, rightWidth, FIELD_HEIGHT, Component.empty()));
        entryTitleBox = addField(new EditBox(font, rightX, y + FIELD_ROW_SPACING, rightWidth, FIELD_HEIGHT, Component.empty()));
        entryTypeBox = addField(new EditBox(font, rightX, y + FIELD_ROW_SPACING * 2, rightWidth, FIELD_HEIGHT, Component.empty()));
        entryIconBox = addField(new EditBox(font, rightX, y + FIELD_ROW_SPACING * 3, rightWidth - 22, FIELD_HEIGHT, Component.empty()));
        entryTierBox = addField(new EditBox(font, rightX, y + FIELD_ROW_SPACING * 4, rightWidth, FIELD_HEIGHT, Component.empty()));
        entrySearchBox = addField(new EditBox(font, rightX, y + FIELD_ROW_SPACING * 5, rightWidth - 22, FIELD_HEIGHT, Component.empty()));
        entryRelatedBox = addField(new EditBox(font, rightX, y + FIELD_ROW_SPACING * 6, rightWidth - 22, FIELD_HEIGHT, Component.empty()));

        // Add item selection buttons for entry fields
        entryIconSelectButton = addRenderableWidget(Button.builder(Component.literal("..."), button -> {
            minecraft.setScreen(new ItemSelectionScreen(this, itemId -> {
                if (selectedEntry() != null) {
                    selectedEntry().icon = itemId;
                    entryIconBox.setValue(itemId);
                }
            }));
        }).bounds(rightX + rightWidth - 20, y + FIELD_ROW_SPACING * 3, 20, FIELD_HEIGHT).build());

        entrySearchSelectButton = addRenderableWidget(Button.builder(Component.literal("..."), button -> {
            minecraft.setScreen(new ItemSelectionScreen(this, itemId -> {
                if (selectedEntry() != null) {
                    List<String> items = selectedEntry().searchItems;
                    if (!items.contains(itemId)) {
                        items.add(itemId);
                        entrySearchBox.setValue(String.join(", ", items));
                    }
                }
            }));
        }).bounds(rightX + rightWidth - 20, y + FIELD_ROW_SPACING * 5, 20, FIELD_HEIGHT).build());

         entryRelatedSelectButton = addRenderableWidget(Button.builder(Component.literal("..."), button -> {
            minecraft.setScreen(new ItemSelectionScreen(this, itemId -> {
                if (selectedEntry() != null) {
                    List<String> items = selectedEntry().related;
                    if (!items.contains(itemId)) {
                        items.add(itemId);
                        entryRelatedBox.setValue(String.join(", ", items));
                    }
                }
            }));
        }).bounds(rightX + rightWidth - 20, y + FIELD_ROW_SPACING * 6, 20, FIELD_HEIGHT).build());

        entryIdBox.setResponder(value -> {
            if (syncingFields || selectedEntry() == null) {
                return;
            }
            selectedEntry().id = value;
            refreshEntryButtons();
        });

        entryTitleBox.setResponder(value -> {
            if (syncingFields || selectedEntry() == null) {
                return;
            }

            CodexEditorProject.Entry entry = selectedEntry();

            String oldGeneratedId = generateId(lastEntryTitle);

            if (entry.id.isBlank() || entry.id.equals(oldGeneratedId)) {
                String newId = generateId(value);

                entry.id = newId;

                syncingFields = true;
                entryIdBox.setValue(newId);
                syncingFields = false;
            }

            entry.title = value;
            lastEntryTitle = value;

            refreshEntryButtons();
        });

        entryTypeBox.setResponder(value -> {
            if (syncingFields || selectedEntry() == null) {
                return;
            }
            selectedEntry().type = value;
        });
        entryIconBox.setResponder(value -> {
            if (syncingFields || selectedEntry() == null) {
                return;
            }
            selectedEntry().icon = value;
        });
        entryTierBox.setResponder(value -> {
            if (syncingFields || selectedEntry() == null) {
                return;
            }
            selectedEntry().tier = parseInt(value, selectedEntry().tier);
        });
        entrySearchBox.setResponder(value -> {
            if (syncingFields || selectedEntry() == null) {
                return;
            }
            selectedEntry().searchItems = splitCsv(value);
        });
        entryRelatedBox.setResponder(value -> {
            if (syncingFields || selectedEntry() == null) {
                return;
            }
            selectedEntry().related = splitCsv(value);
        });
    }

    private String generateId(String title) {
        if (title == null) {
            return "";
        }

        return title
                .trim()
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s_]", "")
                .replaceAll("\\s+", "_");
    }

    private void initModuleFields(int rightX, int rightWidth) {
        int top = OUTER_MARGIN + TOP_BAR_HEIGHT + 46;

        previousPageButton = addRenderableWidget(Button.builder(Component.literal("<"), button -> {
            selectedPageIndex = Math.max(0, selectedPageIndex - 1);
            selectedModuleIndex = 0;
            refreshAllState();
        }).bounds(rightX, top, 22, BUTTON_HEIGHT).build());

        nextPageButton = addRenderableWidget(Button.builder(Component.literal(">"), button -> {
            CodexEditorProject.Entry entry = selectedEntry();
            if (entry == null) {
                return;
            }
            selectedPageIndex = Math.min(entry.pages.size() - 1, selectedPageIndex + 1);
            selectedModuleIndex = 0;
            refreshAllState();
        }).bounds(rightX + 26, top, 22, BUTTON_HEIGHT).build());

        addPageButton = addRenderableWidget(Button.builder(Component.literal("+ Page"), button -> {
            CodexEditorProject.Entry entry = selectedEntry();
            if (entry == null) {
                return;
            }
            entry.pages.add(new CodexEditorProject.Page());
            selectedPageIndex = entry.pages.size() - 1;
            selectedModuleIndex = 0;
            refreshAllState();
        }).bounds(rightX + 56, top, 62, BUTTON_HEIGHT).build());

        deletePageButton = addRenderableWidget(Button.builder(Component.literal("Delete"), button -> {
            CodexEditorProject.Entry entry = selectedEntry();
            if (entry == null || selectedPage() == null) {
                return;
            }
            entry.pages.remove(selectedPageIndex);
            if (entry.pages.isEmpty()) {
                entry.pages.add(new CodexEditorProject.Page());
            }
            selectedPageIndex = Math.max(0, selectedPageIndex - 1);
            selectedModuleIndex = 0;
            refreshAllState();
        }).bounds(rightX + 124, top, 62, BUTTON_HEIGHT).build());

        addTextModuleButton = addRenderableWidget(Button.builder(Component.literal("+ Text"), button -> {
            addModule("text");
        }).bounds(rightX, top + 26, 58, BUTTON_HEIGHT).build());
        addRecipeModuleButton = addRenderableWidget(Button.builder(Component.literal("+ Recipe"), button -> {
            addModule("recipe");
        }).bounds(rightX + 62, top + 26, 68, BUTTON_HEIGHT).build());
        addFurnaceModuleButton = addRenderableWidget(Button.builder(Component.literal("+ Furnace"), button -> {
            addModule("furnace_recipe");
        }).bounds(rightX + 134, top + 26, 74, BUTTON_HEIGHT).build());

        deleteModuleButton = addRenderableWidget(Button.builder(Component.literal("Delete"), button -> {
            CodexEditorProject.Page page = selectedPage();
            if (page == null || selectedModule() == null) {
                return;
            }
            page.modules.remove(selectedModuleIndex);
            if (page.modules.isEmpty()) {
                page.modules.add(new CodexEditorProject.Module());
            }
            selectedModuleIndex = Math.max(0, selectedModuleIndex - 1);
            refreshAllState();
        }).bounds(rightX + rightWidth - 70, top + 52, 70, BUTTON_HEIGHT).build());

        moduleTypeButton = addRenderableWidget(Button.builder(Component.literal("Type"), button -> cycleModuleType())
                .bounds(rightX, top + 52, 96, BUTTON_HEIGHT)
                .build());

        moduleUpButton = addRenderableWidget(Button.builder(Component.literal("^"), button -> {
            moduleScrollOffset = Math.max(0, moduleScrollOffset - 1);
            refreshModuleButtons();
        }).bounds(rightX + 102, top + 52, 20, BUTTON_HEIGHT).build());
        moduleDownButton = addRenderableWidget(Button.builder(Component.literal("v"), button -> {
            moduleScrollOffset = Math.min(getMaxModuleScroll(), moduleScrollOffset + 1);
            refreshModuleButtons();
        }).bounds(rightX + 126, top + 52, 20, BUTTON_HEIGHT).build());

        int listTop = top + 78;
        for (int i = 0; i < MODULE_VISIBLE_ROWS; i++) {
            final int row = i;
            Button button = addRenderableWidget(Button.builder(Component.empty(), btn -> {
                selectedModuleIndex = moduleScrollOffset + row;
                ensureSelections();
                refreshAllState();
            }).bounds(rightX, listTop + i * LIST_ROW_HEIGHT, rightWidth, LIST_ROW_HEIGHT).build());
            moduleButtons.add(button);
        }

        int fieldTop = listTop + MODULE_VISIBLE_ROWS * LIST_ROW_HEIGHT + 10;

        textModuleBox = addField(new EditBox(font, rightX, fieldTop, rightWidth, FIELD_HEIGHT, Component.empty()));

        recipeTypeBox = addField(new EditBox(font, rightX, fieldTop, rightWidth, FIELD_HEIGHT, Component.empty()));
        recipeResultBox = addField(new EditBox(font, rightX, fieldTop + FIELD_ROW_SPACING, rightWidth - 22, FIELD_HEIGHT, Component.empty()));
        recipePattern1Box = addField(new EditBox(font, rightX, fieldTop + FIELD_ROW_SPACING * 2, rightWidth, FIELD_HEIGHT, Component.empty()));
        recipePattern2Box = addField(new EditBox(font, rightX, fieldTop + FIELD_ROW_SPACING * 3, rightWidth, FIELD_HEIGHT, Component.empty()));
        recipePattern3Box = addField(new EditBox(font, rightX, fieldTop + FIELD_ROW_SPACING * 4, rightWidth, FIELD_HEIGHT, Component.empty()));
        recipeKeysBox = addField(new EditBox(font, rightX, fieldTop + FIELD_ROW_SPACING * 5, rightWidth, FIELD_HEIGHT, Component.empty()));

        furnaceInputBox = addField(new EditBox(font, rightX, fieldTop, rightWidth - 22, FIELD_HEIGHT, Component.empty()));
        furnaceOutputBox = addField(new EditBox(font, rightX, fieldTop + FIELD_ROW_SPACING, rightWidth - 22, FIELD_HEIGHT, Component.empty()));
        furnaceExperienceBox = addField(new EditBox(font, rightX, fieldTop + FIELD_ROW_SPACING * 2, rightWidth, FIELD_HEIGHT, Component.empty()));
        furnaceTimeBox = addField(new EditBox(font, rightX, fieldTop + FIELD_ROW_SPACING * 3, rightWidth, FIELD_HEIGHT, Component.empty()));

        // Add item selection buttons for recipe and furnace fields
        recipeResultSelectButton = addRenderableWidget(Button.builder(Component.literal("..."), button -> {
            minecraft.setScreen(new ItemSelectionScreen(this, itemId -> {
                if (selectedModule() != null) {
                    selectedModule().result = itemId;
                    recipeResultBox.setValue(itemId);
                }
            }));
        }).bounds(rightX + rightWidth - 20, fieldTop + FIELD_ROW_SPACING, 20, FIELD_HEIGHT).build());

        furnaceInputSelectButton = addRenderableWidget(Button.builder(Component.literal("..."), button -> {
            minecraft.setScreen(new ItemSelectionScreen(this, itemId -> {
                if (selectedModule() != null) {
                    selectedModule().input = itemId;
                    furnaceInputBox.setValue(itemId);
                }
            }));
        }).bounds(rightX + rightWidth - 20, fieldTop, 20, FIELD_HEIGHT).build());

        furnaceOutputSelectButton = addRenderableWidget(Button.builder(Component.literal("..."), button -> {
            minecraft.setScreen(new ItemSelectionScreen(this, itemId -> {
                if (selectedModule() != null) {
                    selectedModule().output = itemId;
                    furnaceOutputBox.setValue(itemId);
                }
            }));
        }).bounds(rightX + rightWidth - 20, fieldTop + FIELD_ROW_SPACING, 20, FIELD_HEIGHT).build());

        textModuleBox.setResponder(value -> {
            if (syncingFields || selectedModule() == null) {
                return;
            }
            selectedModule().text = value;
        });
        recipeTypeBox.setResponder(value -> {
            if (syncingFields || selectedModule() == null) {
                return;
            }
            selectedModule().recipeType = value;
        });
        recipeResultBox.setResponder(value -> {
            if (syncingFields || selectedModule() == null) {
                return;
            }
            selectedModule().result = value;
        });
        recipePattern1Box.setResponder(value -> updateRecipePatterns());
        recipePattern2Box.setResponder(value -> updateRecipePatterns());
        recipePattern3Box.setResponder(value -> updateRecipePatterns());
        recipeKeysBox.setResponder(value -> {
            if (syncingFields || selectedModule() == null) {
                return;
            }
            selectedModule().key = parseKeyMap(value);
        });
        furnaceInputBox.setResponder(value -> {
            if (syncingFields || selectedModule() == null) {
                return;
            }
            selectedModule().input = value;
        });
        furnaceOutputBox.setResponder(value -> {
            if (syncingFields || selectedModule() == null) {
                return;
            }
            selectedModule().output = value;
        });
        furnaceExperienceBox.setResponder(value -> {
            if (syncingFields || selectedModule() == null) {
                return;
            }
            selectedModule().experience = parseFloat(value, selectedModule().experience);
        });
        furnaceTimeBox.setResponder(value -> {
            if (syncingFields || selectedModule() == null) {
                return;
            }
            selectedModule().cookingTime = parseInt(value, selectedModule().cookingTime);
        });
    }

    private void saveProject() {
        project.normalize();

        saveInProgress = true;
        refreshAllState();

        String json = CodexEditorPersistence.toJson(project);

        PacketDistributor.sendToServer(
                new SaveCodexEditorPacket(json)
        );
    }

    public void handleServerSync(CodexEditorProject project) {
        this.project = project;
        this.project.normalize();
        saveInProgress = false;
        ensureSelections();
        refreshAllState();
    }

    private void switchTab(EditorTab tab) {
        activeTab = tab;
        refreshAllState();
    }

    private void addModule(String moduleType) {
        CodexEditorProject.Page page = selectedPage();
        if (page == null) {
            return;
        }

        CodexEditorProject.Module module = new CodexEditorProject.Module();
        module.moduleType = moduleType;
        if ("recipe".equals(moduleType)) {
            module.recipeType = "crafting_table";
        } else if ("furnace_recipe".equals(moduleType)) {
            module.cookingTime = 200;
        }

        page.modules.add(module);
        selectedModuleIndex = page.modules.size() - 1;
        refreshAllState();
    }

    private void cycleModuleType() {
        CodexEditorProject.Module module = selectedModule();
        if (module == null) {
            return;
        }

        module.moduleType = switch (module.moduleType) {
            case "text" -> "recipe";
            case "recipe" -> "furnace_recipe";
            default -> "text";
        };
        refreshAllState();
    }

    private void refreshAllState() {
        project.normalize();
        ensureSelections();
        refreshCategoryButtons();
        refreshEntryButtons();
        refreshModuleButtons();
        refreshFieldValues();
        refreshVisibility();
    }

    private void refreshCategoryButtons() {
        int maxScroll = getMaxCategoryScroll();
        categoryScrollOffset = Math.min(categoryScrollOffset, maxScroll);

        for (int i = 0; i < categoryButtons.size(); i++) {
            int index = categoryScrollOffset + i;
            Button button = categoryButtons.get(i);
            if (index < project.categories.size()) {
                CodexEditorProject.Category category = project.categories.get(index);
                button.visible = true;
                button.active = true;
                button.setMessage(Component.literal(prefixSelected(index == selectedCategoryIndex) + trim(category.title, 18)));
            } else {
                button.visible = false;
            }
        }

        categoryUpButton.active = categoryScrollOffset > 0;
        categoryDownButton.active = categoryScrollOffset < maxScroll;
        deleteCategoryButton.active = selectedCategory() != null;
    }

    private void refreshEntryButtons() {
        CodexEditorProject.Category category = selectedCategory();
        int maxScroll = getMaxEntryScroll();
        entryScrollOffset = Math.min(entryScrollOffset, maxScroll);

        for (int i = 0; i < entryButtons.size(); i++) {
            int index = entryScrollOffset + i;
            Button button = entryButtons.get(i);
            if (category != null && index < category.entries.size()) {
                CodexEditorProject.Entry entry = category.entries.get(index);
                button.visible = true;
                button.active = true;
                button.setMessage(Component.literal(prefixSelected(index == selectedEntryIndex) + trim(entry.title, 18)));
            } else {
                button.visible = false;
            }
        }

        entryUpButton.active = entryScrollOffset > 0;
        entryDownButton.active = entryScrollOffset < maxScroll;
        addEntryButton.active = category != null;
        deleteEntryButton.active = selectedEntry() != null;
    }

    private void refreshModuleButtons() {
        CodexEditorProject.Page page = selectedPage();
        int maxScroll = getMaxModuleScroll();
        moduleScrollOffset = Math.min(moduleScrollOffset, maxScroll);

        for (int i = 0; i < moduleButtons.size(); i++) {
            int index = moduleScrollOffset + i;
            Button button = moduleButtons.get(i);
            if (page != null && index < page.modules.size()) {
                CodexEditorProject.Module module = page.modules.get(index);
                button.visible = true;
                button.active = true;
                button.setMessage(Component.literal(prefixSelected(index == selectedModuleIndex) + trim(describeModule(module, index), 22)));
            } else {
                button.visible = false;
            }
        }

        moduleUpButton.active = moduleScrollOffset > 0;
        moduleDownButton.active = moduleScrollOffset < maxScroll;
        deleteModuleButton.active = selectedModule() != null;
        moduleTypeButton.active = selectedModule() != null;
        moduleTypeButton.setMessage(Component.literal("Type: " + describeModuleType(selectedModule())));
    }

    private void refreshFieldValues() {
        syncingFields = true;

        CodexEditorProject.Category category = selectedCategory();
        categoryIdBox.setValue(category != null ? category.id : "");
        categoryTitleBox.setValue(category != null ? category.title : "");
        categoryIconBox.setValue(category != null ? category.icon : "");

        CodexEditorProject.Entry entry = selectedEntry();
        entryIdBox.setValue(entry != null ? entry.id : "");
        entryTitleBox.setValue(entry != null ? entry.title : "");
        entryTypeBox.setValue(entry != null ? entry.type : "");
        entryIconBox.setValue(entry != null ? entry.icon : "");
        entryTierBox.setValue(entry != null ? Integer.toString(entry.tier) : "");
        entrySearchBox.setValue(entry != null ? String.join(", ", entry.searchItems) : "");
        entryRelatedBox.setValue(entry != null ? String.join(", ", entry.related) : "");

        if (category != null) {
            lastCategoryTitle = category.title;
        }

        if (entry != null) {
            lastEntryTitle = entry.title;
        }

        CodexEditorProject.Module module = selectedModule();
        textModuleBox.setValue(module != null ? module.text : "");

        recipeTypeBox.setValue(module != null ? module.recipeType : "");
        recipeResultBox.setValue(module != null ? module.result : "");
        recipePattern1Box.setValue(module != null && module.pattern.size() > 0 ? module.pattern.get(0) : "");
        recipePattern2Box.setValue(module != null && module.pattern.size() > 1 ? module.pattern.get(1) : "");
        recipePattern3Box.setValue(module != null && module.pattern.size() > 2 ? module.pattern.get(2) : "");
        recipeKeysBox.setValue(module != null ? formatKeyMap(module.key) : "");

        furnaceInputBox.setValue(module != null ? module.input : "");
        furnaceOutputBox.setValue(module != null ? module.output : "");
        furnaceExperienceBox.setValue(module != null ? Float.toString(module.experience) : "");
        furnaceTimeBox.setValue(module != null ? Integer.toString(module.cookingTime) : "");

        syncingFields = false;
    }

    private void refreshVisibility() {
        refreshFieldValues();

        boolean entryTab = activeTab == EditorTab.ENTRY;
        boolean hasCategory = selectedCategory() != null;
        boolean hasEntry = selectedEntry() != null;
        boolean hasModule = selectedModule() != null;

        saveButton.active = !saveInProgress;
        entryTabButton.active = activeTab != EditorTab.ENTRY;
        moduleTabButton.active = activeTab != EditorTab.MODULE;

        setVisible(categoryIdBox, hasCategory);
        setVisible(categoryTitleBox, hasCategory);
        setVisible(categoryIconBox, hasCategory);

        setVisible(entryIdBox, entryTab && hasEntry);
        setVisible(entryTitleBox, entryTab && hasEntry);
        setVisible(entryTypeBox, entryTab && hasEntry);
        setVisible(entryIconBox, entryTab && hasEntry);
        setVisible(entryTierBox, entryTab && hasEntry);
        setVisible(entrySearchBox, entryTab && hasEntry);
        setVisible(entryRelatedBox, entryTab && hasEntry);

        boolean moduleTab = activeTab == EditorTab.MODULE;
        previousPageButton.visible = moduleTab;
        nextPageButton.visible = moduleTab;
        addPageButton.visible = moduleTab;
        deletePageButton.visible = moduleTab;
        addTextModuleButton.visible = moduleTab;
        addRecipeModuleButton.visible = moduleTab;
        addFurnaceModuleButton.visible = moduleTab;
        deleteModuleButton.visible = moduleTab;
        moduleTypeButton.visible = moduleTab;
        moduleUpButton.visible = moduleTab;
        moduleDownButton.visible = moduleTab;

        refreshButtonForFields();

        for (Button button : moduleButtons) {
            button.visible = moduleTab && button.visible;
        }

        String moduleType = hasModule ? selectedModule().moduleType : "text";
        setVisible(textModuleBox, moduleTab && hasModule && "text".equals(moduleType));

        boolean showRecipe = moduleTab && hasModule && "recipe".equals(moduleType);
        setVisible(recipeTypeBox, showRecipe);
        setVisible(recipeResultBox, showRecipe);
        setVisible(recipePattern1Box, showRecipe);
        setVisible(recipePattern2Box, showRecipe);
        setVisible(recipePattern3Box, showRecipe);
        setVisible(recipeKeysBox, showRecipe);

        boolean showFurnace = moduleTab && hasModule && "furnace_recipe".equals(moduleType);
        setVisible(furnaceInputBox, showFurnace);
        setVisible(furnaceOutputBox, showFurnace);
        setVisible(furnaceExperienceBox, showFurnace);
        setVisible(furnaceTimeBox, showFurnace);

        previousPageButton.active = moduleTab && hasEntry && selectedPageIndex > 0;
        nextPageButton.active = moduleTab && hasEntry && selectedEntry().pages != null && selectedPageIndex < selectedEntry().pages.size() - 1;
        addPageButton.active = moduleTab && hasEntry;
        deletePageButton.active = moduleTab && hasEntry;
        addTextModuleButton.active = moduleTab && hasEntry;
        addRecipeModuleButton.active = moduleTab && hasEntry;
        addFurnaceModuleButton.active = moduleTab && hasEntry;
        deleteModuleButton.active = moduleTab && hasModule;
        moduleTypeButton.active = moduleTab && hasModule;
    }

    private void refreshButtonForFields() {
        categoryIconSelectButton.visible = categoryIconBox.visible;
        categoryIconSelectButton.active = categoryIconBox.visible;

        entryIconSelectButton.visible = entryIconBox.visible;
        entryIconSelectButton.active = entryIconBox.visible;

        entrySearchSelectButton.visible = entrySearchBox.visible;
        entrySearchSelectButton.active = entrySearchBox.visible;

        entryRelatedSelectButton.visible = entryRelatedBox.visible;
        entryRelatedSelectButton.active = entryRelatedBox.visible;

        recipeResultSelectButton.visible = recipeResultBox.visible;
        recipeResultSelectButton.active = recipeResultBox.visible;

        furnaceInputSelectButton.visible = furnaceInputBox.visible;
        furnaceInputSelectButton.active = furnaceInputBox.visible;

        furnaceOutputSelectButton.visible = furnaceOutputBox.visible;
        furnaceOutputSelectButton.active = furnaceOutputBox.visible;
    }

    private void ensureSelections() {
        if (project.categories.isEmpty()) {
            selectedCategoryIndex = 0;
            selectedEntryIndex = 0;
            selectedPageIndex = 0;
            selectedModuleIndex = 0;
            return;
        }

        selectedCategoryIndex = clamp(selectedCategoryIndex, 0, project.categories.size() - 1);
        CodexEditorProject.Category category = project.categories.get(selectedCategoryIndex);
        if (category.entries.isEmpty()) {
            selectedEntryIndex = 0;
            selectedPageIndex = 0;
            selectedModuleIndex = 0;
            return;
        }

        selectedEntryIndex = clamp(selectedEntryIndex, 0, category.entries.size() - 1);
        CodexEditorProject.Entry entry = category.entries.get(selectedEntryIndex);
        if (entry.pages.isEmpty()) {
            entry.pages.add(new CodexEditorProject.Page());
        }

        selectedPageIndex = clamp(selectedPageIndex, 0, entry.pages.size() - 1);
        CodexEditorProject.Page page = entry.pages.get(selectedPageIndex);
        if (page.modules.isEmpty()) {
            page.modules.add(new CodexEditorProject.Module());
        }

        selectedModuleIndex = clamp(selectedModuleIndex, 0, page.modules.size() - 1);
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        renderPanels(guiGraphics);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        renderLabels(guiGraphics);

        refreshCounter++;

        if (refreshCounter >= 10) {
            refreshButtonForFields();
            refreshCounter = 0;
        }
    }

    private void renderPanels(GuiGraphics guiGraphics) {
        int top = OUTER_MARGIN + TOP_BAR_HEIGHT + 10;
        int bottom = height - OUTER_MARGIN;
        int left = OUTER_MARGIN - 4;
        int middle = OUTER_MARGIN + LEFT_COLUMN_WIDTH + COLUMN_GAP - 4;
        int right = getRightColumnX() - 4;

        guiGraphics.fill(left, top, left + LEFT_COLUMN_WIDTH + 8, bottom, 0xAA1B1B1B);
        guiGraphics.fill(middle, top, middle + MIDDLE_COLUMN_WIDTH + 8, bottom, 0xAA1B1B1B);
        guiGraphics.fill(right, top, right + getRightColumnWidth() + 8, bottom, 0xAA1B1B1B);
    }

    private void renderLabels(GuiGraphics guiGraphics) {
        int leftX = OUTER_MARGIN;
        int middleX = OUTER_MARGIN + LEFT_COLUMN_WIDTH + COLUMN_GAP;
        int rightX = getRightColumnX();
        int titleY = OUTER_MARGIN + TOP_BAR_HEIGHT;

        guiGraphics.drawString(font, "Categories", leftX, titleY, 0xFFFFFF);
        guiGraphics.drawString(font, "Entries", middleX, titleY, 0xFFFFFF);
        guiGraphics.drawString(font, "Editor", rightX, titleY, 0xFFFFFF);

        int categoryFieldY = OUTER_MARGIN + TOP_BAR_HEIGHT + 18 + CATEGORY_VISIBLE_ROWS * LIST_ROW_HEIGHT + 26;
        drawFieldLabel(guiGraphics, "Category Id", leftX, categoryFieldY);
        drawFieldLabel(guiGraphics, "Category Title", leftX, categoryFieldY + FIELD_ROW_SPACING);
        drawFieldLabel(guiGraphics, "Category Icon", leftX, categoryFieldY + FIELD_ROW_SPACING * 2);

        if (activeTab == EditorTab.ENTRY) {
            int y = OUTER_MARGIN + TOP_BAR_HEIGHT + 36;
            drawFieldLabel(guiGraphics, "Entry Id", rightX, y);
            drawFieldLabel(guiGraphics, "Title", rightX, y + FIELD_ROW_SPACING);
            drawFieldLabel(guiGraphics, "Type", rightX, y + FIELD_ROW_SPACING * 2);
            drawFieldLabel(guiGraphics, "Icon", rightX, y + FIELD_ROW_SPACING * 3);
            drawFieldLabel(guiGraphics, "Tier", rightX, y + FIELD_ROW_SPACING * 4);
            drawFieldLabel(guiGraphics, "Search Items", rightX, y + FIELD_ROW_SPACING * 5);
            drawFieldLabel(guiGraphics, "Related", rightX, y + FIELD_ROW_SPACING * 6);
            return;
        }

        CodexEditorProject.Entry entry = selectedEntry();
        int pageCount = entry != null ? entry.pages.size() : 0;
        guiGraphics.drawString(font, "Page " + (selectedPageIndex + 1) + " / " + Math.max(1, pageCount), rightX + 190, OUTER_MARGIN + TOP_BAR_HEIGHT + 52, 0xFFFFFF);

        String moduleType = selectedModule() != null ? selectedModule().moduleType : "text";
        int fieldTop = OUTER_MARGIN + TOP_BAR_HEIGHT + 46 + 78 + MODULE_VISIBLE_ROWS * LIST_ROW_HEIGHT;
        if ("text".equals(moduleType)) {
            drawFieldLabel(guiGraphics, "Text", rightX, fieldTop);
        } else if ("recipe".equals(moduleType)) {
            drawFieldLabel(guiGraphics, "Recipe Type", rightX, fieldTop);
            drawFieldLabel(guiGraphics, "Result", rightX, fieldTop + FIELD_ROW_SPACING);
            drawFieldLabel(guiGraphics, "Pattern 1", rightX, fieldTop + FIELD_ROW_SPACING * 2);
            drawFieldLabel(guiGraphics, "Pattern 2", rightX, fieldTop + FIELD_ROW_SPACING * 3);
            drawFieldLabel(guiGraphics, "Pattern 3", rightX, fieldTop + FIELD_ROW_SPACING * 4);
            drawFieldLabel(guiGraphics, "Keys (A=item,B=item)", rightX, fieldTop + FIELD_ROW_SPACING * 5);
        } else {
            drawFieldLabel(guiGraphics, "Input", rightX, fieldTop);
            drawFieldLabel(guiGraphics, "Output", rightX, fieldTop + FIELD_ROW_SPACING);
            drawFieldLabel(guiGraphics, "Experience", rightX, fieldTop + FIELD_ROW_SPACING * 2);
            drawFieldLabel(guiGraphics, "Cook Time", rightX, fieldTop + FIELD_ROW_SPACING * 3);
        }
    }

    private void drawFieldLabel(GuiGraphics guiGraphics, String label, int x, int y) {
        guiGraphics.drawString(font, label, x, y - LABEL_GAP, 0xD0D0D0);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, width, height, 0xFF111216);
    }

    @Override
    public void onClose() {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null) {
            minecraft.setScreen(new CodexArcanumScreen(
                    new CodexArcanumMenu(0, minecraft.player),
                    minecraft.player.getInventory(),
                    Component.literal("Codex Arcanum")
            ));
            return;
        }
        super.onClose();
    }

    private EditBox addField(EditBox editBox) {
        editBox.setMaxLength(2048);
        addRenderableWidget(editBox);
        allEditBoxes.add(editBox);
        return editBox;
    }

    private void setVisible(EditBox editBox, boolean visible) {
        editBox.setVisible(visible);
        editBox.active = visible;
    }

    private int getRightColumnX() {
        return OUTER_MARGIN + LEFT_COLUMN_WIDTH + COLUMN_GAP + MIDDLE_COLUMN_WIDTH + COLUMN_GAP;
    }

    private int getRightColumnWidth() {
        return width - getRightColumnX() - OUTER_MARGIN;
    }

    private int getMaxCategoryScroll() {
        return Math.max(0, project.categories.size() - CATEGORY_VISIBLE_ROWS);
    }

    private int getMaxEntryScroll() {
        CodexEditorProject.Category category = selectedCategory();
        int size = category != null ? category.entries.size() : 0;
        return Math.max(0, size - ENTRY_VISIBLE_ROWS);
    }

    private int getMaxModuleScroll() {
        CodexEditorProject.Page page = selectedPage();
        int size = page != null ? page.modules.size() : 0;
        return Math.max(0, size - MODULE_VISIBLE_ROWS);
    }

    private CodexEditorProject.Category selectedCategory() {
        if (project.categories.isEmpty() || selectedCategoryIndex < 0 || selectedCategoryIndex >= project.categories.size()) {
            return null;
        }
        return project.categories.get(selectedCategoryIndex);
    }

    private CodexEditorProject.Entry selectedEntry() {
        CodexEditorProject.Category category = selectedCategory();
        if (category == null || category.entries.isEmpty() || selectedEntryIndex < 0 || selectedEntryIndex >= category.entries.size()) {
            return null;
        }
        return category.entries.get(selectedEntryIndex);
    }

    private CodexEditorProject.Page selectedPage() {
        CodexEditorProject.Entry entry = selectedEntry();
        if (entry == null || entry.pages.isEmpty() || selectedPageIndex < 0 || selectedPageIndex >= entry.pages.size()) {
            return null;
        }
        return entry.pages.get(selectedPageIndex);
    }

    private CodexEditorProject.Module selectedModule() {
        CodexEditorProject.Page page = selectedPage();
        if (page == null || page.modules.isEmpty() || selectedModuleIndex < 0 || selectedModuleIndex >= page.modules.size()) {
            return null;
        }
        return page.modules.get(selectedModuleIndex);
    }

    private CodexEditorProject.Entry createDefaultEntry(int index) {
        CodexEditorProject.Entry entry = new CodexEditorProject.Entry();
        entry.id = "entry_" + (index + 1);
        entry.title = "Entry " + (index + 1);
        entry.pages = new ArrayList<>();
        entry.pages.add(new CodexEditorProject.Page());
        entry.normalize(index);
        return entry;
    }

    private void updateRecipePatterns() {
        if (syncingFields || selectedModule() == null) {
            return;
        }
        List<String> pattern = new ArrayList<>();
        if (!recipePattern1Box.getValue().isBlank()) {
            pattern.add(recipePattern1Box.getValue());
        }
        if (!recipePattern2Box.getValue().isBlank()) {
            pattern.add(recipePattern2Box.getValue());
        }
        if (!recipePattern3Box.getValue().isBlank()) {
            pattern.add(recipePattern3Box.getValue());
        }
        selectedModule().pattern = pattern;
    }

    private Map<String, String> parseKeyMap(String value) {
        Map<String, String> result = new LinkedHashMap<>();
        if (value == null || value.isBlank()) {
            return result;
        }

        String[] pairs = value.split("[,;]");
        for (String pair : pairs) {
            String[] split = pair.split("=", 2);
            if (split.length != 2) {
                continue;
            }
            String key = split[0].trim();
            String item = split[1].trim();
            if (!key.isEmpty() && !item.isEmpty()) {
                result.put(key, item);
            }
        }
        return result;
    }

    private String formatKeyMap(Map<String, String> keyMap) {
        if (keyMap == null || keyMap.isEmpty()) {
            return "";
        }

        List<String> parts = new ArrayList<>();
        for (Map.Entry<String, String> entry : keyMap.entrySet()) {
            parts.add(entry.getKey() + "=" + entry.getValue());
        }
        return String.join(", ", parts);
    }

    private List<String> splitCsv(String value) {
        List<String> result = new ArrayList<>();
        if (value == null || value.isBlank()) {
            return result;
        }

        for (String part : value.split(",")) {
            String trimmed = part.trim();
            if (!trimmed.isEmpty()) {
                result.add(trimmed);
            }
        }
        return result;
    }

    private int parseInt(String value, int fallback) {
        try {
            return Integer.parseInt(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value.trim());
        } catch (Exception ignored) {
            return fallback;
        }
    }

    private String describeModule(CodexEditorProject.Module module, int index) {
        String label = describeModuleType(module);
        if ("text".equals(module.moduleType) && module.text != null && !module.text.isBlank()) {
            label = label + ": " + module.text;
        } else if ("recipe".equals(module.moduleType) && module.result != null && !module.result.isBlank()) {
            label = label + ": " + module.result;
        } else if ("furnace_recipe".equals(module.moduleType) && module.output != null && !module.output.isBlank()) {
            label = label + ": " + module.output;
        }
        return (index + 1) + ". " + label;
    }

    private String describeModuleType(CodexEditorProject.Module module) {
        if (module == null) {
            return "None";
        }
        return switch (module.moduleType) {
            case "recipe" -> "Recipe";
            case "furnace_recipe" -> "Furnace";
            default -> "Text";
        };
    }

    private String prefixSelected(boolean selected) {
        return selected ? "> " : "";
    }

    private String trim(String text, int maxLength) {
        if (text == null) {
            return "";
        }
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    private enum EditorTab {
        ENTRY,
        MODULE
    }

    private void applyRecipeToModule(JsonObject json, CodexEditorProject.Module module) {
        module.moduleType = json.get("type").getAsString().replace("minecraft:", "");

        JsonObject result = json.getAsJsonObject("result");
        module.result = result.get("item").getAsString();

        JsonArray pattern = json.getAsJsonArray("pattern");
        module.pattern = new ArrayList<>();
        for (var el : pattern) {
            module.pattern.add(el.getAsString());
        }

        JsonObject key = json.getAsJsonObject("key");
        module.key = new LinkedHashMap<>();
        for (var entry : key.entrySet()) {
            module.key.put(entry.getKey(),
                    entry.getValue().getAsJsonObject().get("item").getAsString());
        }
    }

    public static JsonObject loadRecipe(MinecraftServer server, String recipeId) throws IOException {
        String[] split = recipeId.split(":");

        String namespace = split[0];
        String path = split[1];

        Path file = server.getWorldPath(LevelResource.DATAPACK_DIR)
                .resolve(namespace)
                .resolve("recipes")
                .resolve(path + ".json");

        if (!Files.exists(file)) {
            throw new FileNotFoundException("Recipe not found: " + recipeId);
        }

        return com.google.gson.JsonParser.parseString(Files.readString(file)).getAsJsonObject();
    }
}
