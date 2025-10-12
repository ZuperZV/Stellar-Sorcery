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

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class CodexDataLoader {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Map<Integer, CodexEntry> entriesById = new HashMap<>();
    private static final Map<String, Integer> idToInt = new HashMap<>();

    public static void load() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ResourceManager resourceManager = server.getResourceManager();
        entriesById.clear();
        idToInt.clear();
        int nextId = 0;

        Map<ResourceLocation, Resource> resources = resourceManager.listResources("codex_entries", path -> path.getPath().endsWith(".json"));
        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
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
            categoryToFiles.computeIfAbsent(category, k -> new ArrayList<>()).add(rl);
        }

        Set<String> foundFolders = new HashSet<>();

        for (String folderName : categoryToFiles.keySet()) {
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

            for (ResourceLocation fileRL : categoryToFiles.get(folderName)) {
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
}
