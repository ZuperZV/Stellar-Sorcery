package net.zuperz.stellar_sorcery.data;

import com.google.gson.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.zuperz.stellar_sorcery.item.ModItems;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class CodexDataLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<Integer, CodexEntry> entriesById = new HashMap<>();
    private static final Map<String, Integer> idToInt = new HashMap<>();
    private static final List<String> CATEGORY_ORDER = List.of("codex", "flora", "rituals", "lunar", "astral");

    public static void load() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ResourceManager resourceManager = server.getResourceManager();
        entriesById.clear();
        idToInt.clear();
        int nextId = 0;

        Map<ResourceLocation, Resource> resources = resourceManager.listResources("codex_entries", path -> path.getPath().endsWith(".json"));
        List<Map.Entry<ResourceLocation, Resource>> sortedResources = new ArrayList<>(resources.entrySet());
        sortedResources.sort(Comparator.comparing(entry -> entry.getKey().getPath()));

        for (Map.Entry<ResourceLocation, Resource> entry : sortedResources) {
            String path = entry.getKey().getPath();
            if (!isSupportedCategoryPath(path)) {
                continue;
            }

            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open())) {
                JsonElement root = JsonParser.parseReader(reader);
                if (root.isJsonArray()) {
                    for (JsonElement elem : root.getAsJsonArray()) {
                        CodexEntry codexEntry = GSON.fromJson(elem, CodexEntry.class);
                        int id = nextId++;
                        entriesById.put(id, codexEntry);
                        idToInt.put(codexEntry.id, id);
                    }
                } else {
                    CodexEntry codexEntry = GSON.fromJson(root, CodexEntry.class);
                    int id = nextId++;
                    entriesById.put(id, codexEntry);
                    idToInt.put(codexEntry.id, id);
                }
                System.out.println("[Codex] Indlæst: " + entry.getKey());
            } catch (Exception e) {
                System.err.println("[Codex] Fejl ved indlæsning af " + entry.getKey() + ": " + e.getMessage());
            }
        }


        Map<ResourceLocation, Resource> category = resourceManager.listResources("codex_entries", path -> path.getPath().endsWith(".cat"));
        System.out.println("category: " + category);
    }

    public static List<CodexCategory> getAllCategories() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ResourceManager resourceManager = server.getResourceManager();
        List<CodexCategory> categories = new ArrayList<>();

        Map<ResourceLocation, Resource> resources = resourceManager.listResources(
                "codex_entries",
                path -> path.getPath().endsWith(".json")
        );

        Map<String, List<ResourceLocation>> categoryToFiles = new HashMap<>();

        for (ResourceLocation rl : resources.keySet()) {
            String fullPath = rl.getPath();
            if (!fullPath.startsWith("codex_entries/")) continue;

            String relative = fullPath.substring("codex_entries/".length());
            String[] split = relative.split("/");
            if (split.length < 3) continue;

            String category = split[0];
            if (!isSupportedCategoryFolder(category)) continue;
            categoryToFiles.computeIfAbsent(category, k -> new ArrayList<>()).add(rl);
        }

        Set<String> foundFolders = new HashSet<>();

        List<String> sortedFolders = new ArrayList<>(categoryToFiles.keySet());
        sortedFolders.sort(Comparator
                .<String>comparingInt(folderName -> {
                    String[] parts = folderName.split("_", 2);
                    String id = parts.length > 0 ? parts[0] : folderName;
                    int index = CATEGORY_ORDER.indexOf(id);
                    return index >= 0 ? index : Integer.MAX_VALUE;
                })
                .thenComparing(folder -> folder));

        for (String folderName : sortedFolders) {
            if (!foundFolders.add(folderName)) continue;

            String[] parts = folderName.split("_", 2);
            String id = parts.length > 0 ? parts[0] : folderName;
            String iconName = parts.length > 1 ? parts[1] : "minecraft-book";

            String[] iconParts = iconName.split("-");
            String namespace;
            String path;
            if (iconParts.length == 2) {
                namespace = iconParts[0];
                path = iconParts[1];
            } else {
                namespace = "minecraft";
                path = iconParts[0];
            }

            ResourceLocation iconRL = ResourceLocation.fromNamespaceAndPath(namespace, path);
            Item iconItem = BuiltInRegistries.ITEM.get(iconRL);
            ItemStack icon = iconItem != null && iconItem != Items.AIR
                    ? new ItemStack(iconItem)
                    : new ItemStack(ModItems.CODEX_ARCANUM.get());

            List<CodexEntry> entries = new ArrayList<>();
            List<Integer> tiers = new ArrayList<>();

            List<ResourceLocation> sortedFiles = new ArrayList<>(categoryToFiles.get(folderName));
            sortedFiles.sort(Comparator.comparing(ResourceLocation::getPath));

            for (ResourceLocation fileRL : sortedFiles) {
                String relative = fileRL.getPath().substring("codex_entries/".length());
                String[] split = relative.split("/");

                if (split.length < 3) continue;

                String tierFolder = split[1];
                if (!tierFolder.toLowerCase(Locale.ROOT).startsWith("tier_")) continue;

                int tier;
                try {
                    tier = Integer.parseInt(tierFolder.substring(5));
                } catch (NumberFormatException e) {
                    tier = 0;
                }

                try (InputStream is = resourceManager.open(fileRL)) {
                    JsonObject json = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();
                    CodexEntry entry = CodexEntry.fromJson(json);
                    entries.add(entry);
                    tiers.add(tier);
                } catch (Exception e) {
                    System.err.println("[Codex] Failed to load entry from " + fileRL + ": " + e.getMessage());
                }
            }

            categories.add(new CodexCategory(id, icon, entries, tiers));
        }

        if (categories.isEmpty()) {
            System.out.println("[Codex] No codex_entries folders found — using fallback.");
            categories.add(new CodexCategory("default", new ItemStack(ModItems.CODEX_ARCANUM.get()), Collections.emptyList(), Collections.emptyList()));
        }

        return categories;
    }

    public static CodexEntry getEntryByInt(int id) {
        return entriesById.get(id);
    }

    public static Integer getIntForEntryId(String entryId) {
        return idToInt.get(entryId);
    }

    public static Collection<CodexEntry> getAllEntries() {
        return entriesById.values();
    }

    private static boolean isSupportedCategoryPath(String fullPath) {
        if (!fullPath.startsWith("codex_entries/")) {
            return false;
        }

        String relative = fullPath.substring("codex_entries/".length());
        String[] split = relative.split("/");
        if (split.length < 3) {
            return false;
        }

        return isSupportedCategoryFolder(split[0]);
    }

    private static boolean isSupportedCategoryFolder(String folderName) {
        String[] parts = folderName.split("_", 2);
        if (parts.length < 2 || !parts[1].contains("-")) {
            return false;
        }

        return CATEGORY_ORDER.contains(parts[0]);
    }
}
