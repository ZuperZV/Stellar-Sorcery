package net.zuperz.stellar_sorcery.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.SharedConstants;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class CodexEditorPersistence {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private CodexEditorPersistence() {
    }

    public static CodexEditorProject fromCurrentData() {
        CodexEditorProject project = new CodexEditorProject();
        project.categories = new ArrayList<>();

        for (CodexCategory category : CodexDataLoader.getAllCategories()) {
            CodexEditorProject.Category editorCategory = new CodexEditorProject.Category();
            editorCategory.id = category.id;
            editorCategory.title = category.getDisplayTitle();
            editorCategory.icon = category.iconId;
            editorCategory.order = category.order;
            editorCategory.entries = new ArrayList<>();

            for (int i = 0; i < category.entries.size(); i++) {
                CodexEntry sourceEntry = category.entries.get(i);

                CodexEditorProject.Entry editorEntry = new CodexEditorProject.Entry();
                editorEntry.id = safe(sourceEntry.id, "entry_" + (i + 1));
                editorEntry.title = !isBlank(sourceEntry.title) ? sourceEntry.title : humanizeId(sourceEntry.id);
                editorEntry.type = safe(sourceEntry.type, "item");
                editorEntry.icon = safe(sourceEntry.icon, "minecraft:book");
                editorEntry.tier = i < category.tiers.size() ? category.tiers.get(i) : 0;
                editorEntry.searchItems = sourceEntry.search_items != null ? new ArrayList<>(sourceEntry.search_items) : new ArrayList<>();
                editorEntry.related = sourceEntry.related != null ? new ArrayList<>(sourceEntry.related) : new ArrayList<>();
                editorEntry.pages = new ArrayList<>();

                if (sourceEntry.right_side != null) {
                    for (CodexPage sourcePage : sourceEntry.right_side) {
                        CodexEditorProject.Page editorPage = new CodexEditorProject.Page();
                        editorPage.modules = new ArrayList<>();

                        if (sourcePage != null && sourcePage.modules != null) {
                            for (CodexModule sourceModule : sourcePage.modules) {
                                CodexEditorProject.Module editorModule = new CodexEditorProject.Module();
                                editorModule.moduleType = safe(sourceModule.module_type, "text");
                                editorModule.text = !isBlank(sourceModule.text) ? sourceModule.text : safe(sourceModule.text_key, "");
                                editorModule.recipeType = safe(sourceModule.recipe_type, "crafting_table");
                                editorModule.pattern = sourceModule.pattern != null ? new ArrayList<>(sourceModule.pattern) : new ArrayList<>();
                                editorModule.key = sourceModule.key != null ? new LinkedHashMap<>(sourceModule.key) : new LinkedHashMap<>();
                                editorModule.result = safe(sourceModule.result, "");
                                editorModule.input = safe(sourceModule.input, "");
                                editorModule.output = safe(sourceModule.output, "");
                                editorModule.experience = sourceModule.experience;
                                editorModule.cookingTime = sourceModule.cooking_time;
                                editorPage.modules.add(editorModule);
                            }
                        }

                        editorPage.normalize();
                        editorEntry.pages.add(editorPage);
                    }
                }

                editorEntry.normalize(i);
                editorCategory.entries.add(editorEntry);
            }

            editorCategory.normalize(project.categories.size());
            project.categories.add(editorCategory);
        }

        project.normalize();
        return project;
    }

    public static CodexEditorProject fromJson(String json) {
        CodexEditorProject project = GSON.fromJson(json, CodexEditorProject.class);
        if (project == null) {
            project = new CodexEditorProject();
        }
        project.normalize();
        return project;
    }

    public static String toJson(CodexEditorProject project) {
        if (project == null) {
            project = new CodexEditorProject();
        }
        project.normalize();
        return GSON.toJson(project);
    }

    public static CodexEditorProject sanitizeProject(CodexEditorProject project) {
        if (project == null) {
            project = new CodexEditorProject();
        }
        project.normalize();

        project.packId = sanitizePackId(project.packId);

        Map<String, String> entryIdRemap = new LinkedHashMap<>();
        Set<String> usedCategoryIds = new LinkedHashSet<>();
        for (int i = 0; i < project.categories.size(); i++) {
            CodexEditorProject.Category category = project.categories.get(i);
            category.id = uniqueId(sanitizeId(category.id, "category_" + (i + 1)), usedCategoryIds);
            category.order = i;
        }

        Set<String> usedEntryIds = new LinkedHashSet<>();
        for (CodexEditorProject.Category category : project.categories) {
            for (int i = 0; i < category.entries.size(); i++) {
                CodexEditorProject.Entry entry = category.entries.get(i);
                String originalId = entry.id;
                String sanitizedId = uniqueId(sanitizeId(entry.id, "entry_" + (i + 1)), usedEntryIds);
                entry.id = sanitizedId;
                if (originalId != null && !originalId.isBlank()) {
                    entryIdRemap.put(originalId, sanitizedId);
                }
            }
        }

        for (CodexEditorProject.Category category : project.categories) {
            for (CodexEditorProject.Entry entry : category.entries) {
                List<String> updatedRelated = new ArrayList<>();
                for (String related : entry.related) {
                    if (related == null || related.isBlank()) {
                        continue;
                    }
                    updatedRelated.add(entryIdRemap.getOrDefault(related, related));
                }
                entry.related = updatedRelated;
            }
        }

        project.normalize();
        return project;
    }

    public static void writeDatapack(MinecraftServer server, CodexEditorProject inputProject) throws IOException {
        CodexEditorProject project = sanitizeProject(inputProject);

        Path datapacksRoot = server.getWorldPath(LevelResource.DATAPACK_DIR).normalize();
        Files.createDirectories(datapacksRoot);

        Path packRoot = datapacksRoot.resolve(project.packId).normalize();
        if (!packRoot.startsWith(datapacksRoot)) {
            throw new IOException("Refused to write datapack outside the world's datapacks directory");
        }

        deleteRecursively(packRoot);
        Files.createDirectories(packRoot);

        writePackMcmeta(packRoot, project);
        writeCategoryMetadata(packRoot, project);
        writeEntries(packRoot, project);
        writeLangFile(packRoot, project);
    }

    private static void writePackMcmeta(Path packRoot, CodexEditorProject project) throws IOException {
        JsonObject root = new JsonObject();
        JsonObject pack = new JsonObject();
        pack.addProperty("pack_format", SharedConstants.getCurrentVersion().getPackVersion(PackType.SERVER_DATA));
        pack.addProperty("description", safe(project.packDescription, "Stellar Sorcery Codex editor"));
        root.add("pack", pack);
        writeJson(packRoot.resolve("pack.mcmeta"), root);
    }

    private static void writeCategoryMetadata(Path packRoot, CodexEditorProject project) throws IOException {
        JsonObject root = new JsonObject();
        JsonArray categories = new JsonArray();

        for (CodexEditorProject.Category category : project.categories) {
            JsonObject json = new JsonObject();
            json.addProperty("folder", category.id);
            json.addProperty("id", category.id);
            json.addProperty("title", safe(category.title, category.id));
            json.addProperty("icon", safe(category.icon, "minecraft:book"));
            json.addProperty("order", category.order);
            categories.add(json);
        }

        root.add("categories", categories);
        writeJson(packRoot.resolve("data/stellar_sorcery/codex_entries/_categories.json"), root);
    }

    private static void writeEntries(Path packRoot, CodexEditorProject project) throws IOException {
        for (CodexEditorProject.Category category : project.categories) {
            for (int entryIndex = 0; entryIndex < category.entries.size(); entryIndex++) {
                CodexEditorProject.Entry entry = category.entries.get(entryIndex);
                Path entryPath = packRoot.resolve(
                        "data/stellar_sorcery/codex_entries/"
                                + category.id
                                + "/tier_" + Math.max(0, entry.tier)
                                + "/" + String.format("%03d_%s.json", entryIndex + 1, entry.id)
                );
                writeJson(entryPath, buildEntryJson(entry));
            }
        }
    }

    private static JsonObject buildEntryJson(CodexEditorProject.Entry entry) {
        JsonObject root = new JsonObject();
        root.addProperty("id", entry.id);
        root.addProperty("title", safe(entry.title, entry.id));
        root.addProperty("title_key", entry.getTitleKey());
        root.addProperty("type", safe(entry.type, "item"));
        root.addProperty("icon", safe(entry.icon, "minecraft:book"));
        root.add("search_items", toStringArray(entry.searchItems));
        root.add("related", toStringArray(entry.related));

        JsonArray pages = new JsonArray();
        for (int pageIndex = 0; pageIndex < entry.pages.size(); pageIndex++) {
            CodexEditorProject.Page page = entry.pages.get(pageIndex);
            JsonObject pageJson = new JsonObject();
            JsonArray modules = new JsonArray();

            for (int moduleIndex = 0; moduleIndex < page.modules.size(); moduleIndex++) {
                modules.add(buildModuleJson(entry, page.modules.get(moduleIndex), pageIndex, moduleIndex));
            }

            pageJson.add("modules", modules);
            pages.add(pageJson);
        }

        root.add("right_side", pages);
        return root;
    }

    private static JsonObject buildModuleJson(CodexEditorProject.Entry entry, CodexEditorProject.Module module, int pageIndex, int moduleIndex) {
        JsonObject json = new JsonObject();
        String moduleType = safe(module.moduleType, "text");
        json.addProperty("module_type", moduleType);

        if ("text".equals(moduleType)) {
            json.addProperty("text", safe(module.text, ""));
            json.addProperty("text_key", entry.getTextKey(pageIndex, moduleIndex));
            return json;
        }

        if ("furnace_recipe".equals(moduleType)) {
            json.addProperty("input", safe(module.input, ""));
            json.addProperty("output", safe(module.output, ""));
            json.addProperty("experience", module.experience);
            json.addProperty("cooking_time", Math.max(0, module.cookingTime));
            return json;
        }

        json.addProperty("recipe_type", safe(module.recipeType, "crafting_table"));
        json.add("pattern", toStringArray(module.pattern));

        JsonObject key = new JsonObject();
        for (Map.Entry<String, String> entrySet : module.key.entrySet()) {
            if (entrySet.getKey() == null || entrySet.getKey().isBlank()) {
                continue;
            }
            if (entrySet.getValue() == null || entrySet.getValue().isBlank()) {
                continue;
            }
            key.addProperty(entrySet.getKey(), entrySet.getValue());
        }
        json.add("key", key);
        json.addProperty("result", safe(module.result, ""));
        return json;
    }

    private static void writeLangFile(Path packRoot, CodexEditorProject project) throws IOException {
        JsonObject root = new JsonObject();

        for (CodexEditorProject.Category category : project.categories) {
            root.addProperty("codex_arcanum.stellar_sorcery." + category.id, safe(category.title, category.id));

            for (CodexEditorProject.Entry entry : category.entries) {
                root.addProperty(entry.getTitleKey(), safe(entry.title, entry.id));
                for (int pageIndex = 0; pageIndex < entry.pages.size(); pageIndex++) {
                    CodexEditorProject.Page page = entry.pages.get(pageIndex);
                    for (int moduleIndex = 0; moduleIndex < page.modules.size(); moduleIndex++) {
                        CodexEditorProject.Module module = page.modules.get(moduleIndex);
                        if ("text".equals(module.moduleType)) {
                            root.addProperty(entry.getTextKey(pageIndex, moduleIndex), safe(module.text, ""));
                        }
                    }
                }
            }
        }

        writeJson(packRoot.resolve("assets/stellar_sorcery/lang/en_us.json"), root);
    }

    private static JsonArray toStringArray(List<String> values) {
        JsonArray array = new JsonArray();
        if (values == null) {
            return array;
        }

        for (String value : values) {
            if (value == null || value.isBlank()) {
                continue;
            }
            array.add(value);
        }
        return array;
    }

    private static void writeJson(Path path, JsonObject json) throws IOException {
        Files.createDirectories(path.getParent());
        Files.writeString(path, GSON.toJson(json), StandardCharsets.UTF_8);
    }

    private static void deleteRecursively(Path path) throws IOException {
        if (!Files.exists(path)) {
            return;
        }

        try (var stream = Files.walk(path)) {
            stream.sorted(Comparator.reverseOrder()).forEach(current -> {
                try {
                    Files.deleteIfExists(current);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        } catch (RuntimeException exception) {
            if (exception.getCause() instanceof IOException ioException) {
                throw ioException;
            }
            throw exception;
        }
    }

    private static String sanitizePackId(String value) {
        if (value == null || value.isBlank()) {
            return "stellar_sorcery_codex_editor";
        }

        String sanitized = value.toLowerCase().replaceAll("[^a-z0-9._-]", "_");
        return sanitized.isBlank() ? "stellar_sorcery_codex_editor" : sanitized;
    }

    private static String sanitizeId(String value, String fallback) {
        if (value == null || value.isBlank()) {
            value = fallback;
        }

        String sanitized = value.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        sanitized = sanitized.replaceAll("_+", "_");
        sanitized = sanitized.replaceAll("^_+", "").replaceAll("_+$", "");
        return sanitized.isBlank() ? fallback : sanitized;
    }

    private static String uniqueId(String baseId, Set<String> used) {
        String candidate = baseId;
        int counter = 2;
        while (!used.add(candidate)) {
            candidate = baseId + "_" + counter;
            counter++;
        }
        return candidate;
    }

    private static String safe(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String humanizeId(String id) {
        if (isBlank(id)) {
            return "Entry";
        }

        String normalized = id.replace('_', ' ');
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
}
