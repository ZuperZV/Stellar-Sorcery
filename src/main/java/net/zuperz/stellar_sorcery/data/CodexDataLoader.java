package net.zuperz.stellar_sorcery.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.zuperz.stellar_sorcery.item.ModItems;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class CodexDataLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<Integer, CodexEntry> entriesById = new LinkedHashMap<>();
    private static final Map<String, Integer> idToInt = new HashMap<>();
    private static final List<CodexCategory> cachedCategories = new ArrayList<>();
    private static final List<String> LEGACY_CATEGORY_ORDER = List.of(
            "codex",
            "flora",
            "rituals",
            "lunar",
            "astral",
            "stations",
            "materials",
            "relics",
            "essences",
            "jars",
            "light"
    );

    public static void load() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            clear();
            return;
        }

        loadFromResourceManager(server.getResourceManager());
    }

    public static void loadFromResourceManager(ResourceManager resourceManager) {
        clear();

        Map<String, CategoryMetadata> metadataByFolder = loadCategoryMetadata(resourceManager);
        Map<ResourceLocation, Resource> resources = resourceManager.listResources("codex_entries", path -> path.getPath().endsWith(".json"));
        Map<String, List<ResourceLocation>> categoryToFiles = new LinkedHashMap<>();

        for (ResourceLocation rl : resources.keySet()) {
            String fullPath = rl.getPath();
            if (!fullPath.startsWith("codex_entries/")) {
                continue;
            }

            String relative = fullPath.substring("codex_entries/".length());
            if ("_categories.json".equals(relative)) {
                continue;
            }

            String[] split = relative.split("/");
            if (split.length < 3) {
                continue;
            }

            String categoryFolder = split[0];
            String tierFolder = split[1];
            if (!tierFolder.toLowerCase(Locale.ROOT).startsWith("tier_")) {
                continue;
            }

            categoryToFiles.computeIfAbsent(categoryFolder, key -> new ArrayList<>()).add(rl);
        }

        List<String> sortedFolders = new ArrayList<>(categoryToFiles.keySet());
        sortedFolders.sort(
                Comparator.comparingInt((String folder) -> resolveCategoryOrder(folder, metadataByFolder.get(folder)))
                        .thenComparing(folder -> resolveCategoryTitle(folder, metadataByFolder.get(folder)))
                        .thenComparing(folder -> folder)
        );

        int nextId = 0;
        for (String folder : sortedFolders) {
            CategoryMetadata metadata = metadataByFolder.get(folder);
            String categoryId = resolveCategoryId(folder, metadata);
            String categoryTitle = resolveCategoryTitle(folder, metadata);
            String categoryIconId = resolveCategoryIconId(folder, metadata);
            int categoryOrder = resolveCategoryOrder(folder, metadata);

            ItemStack icon = createIconStack(categoryIconId);
            List<CodexEntry> entries = new ArrayList<>();
            List<Integer> tiers = new ArrayList<>();

            List<ResourceLocation> files = new ArrayList<>(categoryToFiles.get(folder));
            files.sort(Comparator.comparing(ResourceLocation::getPath));

            for (ResourceLocation fileRL : files) {
                String relative = fileRL.getPath().substring("codex_entries/".length());
                String[] split = relative.split("/");
                if (split.length < 3) {
                    continue;
                }

                int tier = parseTier(split[1]);
                try (InputStream is = resourceManager.open(fileRL)) {
                    JsonObject json = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();
                    CodexEntry entry = CodexEntry.fromJson(json);
                    entries.add(entry);
                    tiers.add(tier);

                    entriesById.put(nextId, entry);
                    idToInt.put(entry.id, nextId);
                    nextId++;
                } catch (Exception e) {
                    System.err.println("[Codex] Failed to load entry from " + fileRL + ": " + e.getMessage());
                }
            }

            cachedCategories.add(new CodexCategory(categoryId, categoryTitle, categoryIconId, icon, entries, tiers, categoryOrder));
        }

        if (cachedCategories.isEmpty()) {
            cachedCategories.add(new CodexCategory(
                    "default",
                    "Default",
                    "minecraft:book",
                    new ItemStack(ModItems.CODEX_ARCANUM.get()),
                    List.of(),
                    List.of(),
                    Integer.MAX_VALUE
            ));
        }
    }

    public static void applyEditorProject(CodexEditorProject project) {
        clear();
        if (project == null) {
            return;
        }

        project.normalize();

        int nextId = 0;
        for (CodexEditorProject.Category category : project.categories) {
            List<CodexEntry> entries = new ArrayList<>();
            List<Integer> tiers = new ArrayList<>();

            for (CodexEditorProject.Entry editorEntry : category.entries) {
                CodexEntry entry = new CodexEntry();
                entry.id = editorEntry.id;
                entry.title = editorEntry.title;
                entry.title_key = editorEntry.getTitleKey();
                entry.type = editorEntry.type;
                entry.icon = editorEntry.icon;
                entry.search_items = new ArrayList<>(editorEntry.searchItems);
                entry.related = new ArrayList<>(editorEntry.related);
                entry.right_side = new ArrayList<>();

                for (int pageIndex = 0; pageIndex < editorEntry.pages.size(); pageIndex++) {
                    CodexEditorProject.Page editorPage = editorEntry.pages.get(pageIndex);
                    CodexPage page = new CodexPage();
                    page.modules = new ArrayList<>();

                    for (int moduleIndex = 0; moduleIndex < editorPage.modules.size(); moduleIndex++) {
                        CodexEditorProject.Module editorModule = editorPage.modules.get(moduleIndex);
                        CodexModule module = new CodexModule();
                        module.module_type = editorModule.moduleType;
                        module.text = editorModule.text;
                        module.text_key = editorEntry.getTextKey(pageIndex, moduleIndex);
                        module.recipe_type = editorModule.recipeType;
                        module.pattern = new ArrayList<>(editorModule.pattern);
                        module.key = new LinkedHashMap<>(editorModule.key);
                        module.result = editorModule.result;
                        module.input = editorModule.input;
                        module.output = editorModule.output;
                        module.experience = editorModule.experience;
                        module.cooking_time = editorModule.cookingTime;
                        page.modules.add(module);
                    }

                    entry.right_side.add(page);
                }

                entries.add(entry);
                tiers.add(editorEntry.tier);
                entriesById.put(nextId, entry);
                idToInt.put(entry.id, nextId);
                nextId++;
            }

            cachedCategories.add(new CodexCategory(
                    category.id,
                    category.title,
                    category.icon,
                    createIconStack(category.icon),
                    entries,
                    tiers,
                    category.order
            ));
        }
    }

    public static List<CodexCategory> getAllCategories() {
        return List.copyOf(cachedCategories);
    }

    public static CodexEntry getEntryByInt(int id) {
        return entriesById.get(id);
    }

    public static Integer getIntForEntryId(String entryId) {
        return idToInt.get(entryId);
    }

    public static Collection<CodexEntry> getAllEntries() {
        return List.copyOf(entriesById.values());
    }

    private static void clear() {
        entriesById.clear();
        idToInt.clear();
        cachedCategories.clear();
    }

    private static Map<String, CategoryMetadata> loadCategoryMetadata(ResourceManager resourceManager) {
        Map<String, CategoryMetadata> metadataByFolder = new HashMap<>();
        Map<ResourceLocation, Resource> resources = resourceManager.listResources("codex_entries", path -> path.getPath().endsWith("_categories.json"));

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open())) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray categories = root.getAsJsonArray("categories");
                if (categories == null) {
                    continue;
                }

                for (JsonElement element : categories) {
                    if (!element.isJsonObject()) {
                        continue;
                    }

                    CategoryMetadata metadata = GSON.fromJson(element, CategoryMetadata.class);
                    if (metadata == null) {
                        continue;
                    }

                    String folder = metadata.folder;
                    if ((folder == null || folder.isBlank()) && metadata.id != null) {
                        folder = metadata.id;
                    }
                    if (folder == null || folder.isBlank()) {
                        continue;
                    }

                    metadataByFolder.put(folder, metadata);
                }
            } catch (Exception e) {
                System.err.println("[Codex] Failed to load category metadata from " + entry.getKey() + ": " + e.getMessage());
            }
        }

        return metadataByFolder;
    }

    private static int parseTier(String tierFolder) {
        try {
            return Integer.parseInt(tierFolder.substring(5));
        } catch (Exception ignored) {
            return 0;
        }
    }

    private static String resolveCategoryId(String folder, CategoryMetadata metadata) {
        if (metadata != null && metadata.id != null && !metadata.id.isBlank()) {
            return metadata.id;
        }

        int splitIndex = folder.indexOf('_');
        if (splitIndex > 0) {
            return folder.substring(0, splitIndex);
        }

        return folder;
    }

    private static String resolveCategoryTitle(String folder, CategoryMetadata metadata) {
        if (metadata != null && metadata.title != null && !metadata.title.isBlank()) {
            return metadata.title;
        }

        return humanize(resolveCategoryId(folder, metadata));
    }

    private static String resolveCategoryIconId(String folder, CategoryMetadata metadata) {
        if (metadata != null && metadata.icon != null && !metadata.icon.isBlank()) {
            return metadata.icon;
        }

        int splitIndex = folder.indexOf('_');
        if (splitIndex > 0 && splitIndex + 1 < folder.length()) {
            String rawIcon = folder.substring(splitIndex + 1);
            int namespaceSplit = rawIcon.indexOf('-');
            if (namespaceSplit > 0 && namespaceSplit + 1 < rawIcon.length()) {
                return rawIcon.substring(0, namespaceSplit) + ":" + rawIcon.substring(namespaceSplit + 1);
            }
            return rawIcon;
        }

        return "minecraft:book";
    }

    private static int resolveCategoryOrder(String folder, CategoryMetadata metadata) {
        if (metadata != null && metadata.order != null) {
            return metadata.order;
        }

        String categoryId = resolveCategoryId(folder, metadata);
        int index = LEGACY_CATEGORY_ORDER.indexOf(categoryId);
        return index >= 0 ? index : Integer.MAX_VALUE;
    }

    private static ItemStack createIconStack(String iconId) {
        if (iconId == null || iconId.isBlank()) {
            return new ItemStack(ModItems.CODEX_ARCANUM.get());
        }

        ResourceLocation rl = ResourceLocation.tryParse(iconId);
        if (rl == null) {
            return new ItemStack(ModItems.CODEX_ARCANUM.get());
        }

        Item item = BuiltInRegistries.ITEM.get(rl);
        if (item == null || item == Items.AIR) {
            return new ItemStack(ModItems.CODEX_ARCANUM.get());
        }

        return new ItemStack(item);
    }

    private static String humanize(String id) {
        if (id == null || id.isBlank()) {
            return "Category";
        }

        String normalized = id.replace('_', ' ').trim();
        StringBuilder builder = new StringBuilder(normalized.length());
        boolean upper = true;
        for (char character : normalized.toCharArray()) {
            if (character == ' ') {
                upper = true;
                builder.append(character);
                continue;
            }

            builder.append(upper ? Character.toUpperCase(character) : character);
            upper = false;
        }

        return builder.toString();
    }

    private static class CategoryMetadata {
        private String folder;
        private String id;
        private String title;
        private String icon;
        private Integer order;
    }
}
