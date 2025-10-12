package net.zuperz.stellar_sorcery.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.zuperz.stellar_sorcery.item.ModItems;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    }

    public static List<CodexCategory> getAllCategories() {
        Collection<CodexEntry> allEntries = getAllEntries();
        Map<String, List<CodexEntry>> grouped = new HashMap<>();

        Pattern tierPattern = Pattern.compile("tier_(\\d+)");

        for (CodexEntry entry : allEntries) {
            String path = entry.id;
            if (path == null || path.isEmpty()) continue;

            String[] split = path.split("/");
            String category = split.length > 0 ? split[0] : "misc";
            grouped.computeIfAbsent(category, k -> new ArrayList<>()).add(entry);
        }

        List<CodexCategory> categories = new ArrayList<>();
        for (Map.Entry<String, List<CodexEntry>> e : grouped.entrySet()) {
            String id = e.getKey();
            List<CodexEntry> entries = e.getValue();

            ItemStack icon = new ItemStack(ModItems.CODEX_ARCANUM.get());

            int tier = entries.stream().mapToInt(en -> {
                Matcher matcher = tierPattern.matcher(en.id);
                if (matcher.find()) {
                    return Integer.parseInt(matcher.group(1));
                } else {
                    return 0;
                }
            }).min().orElse(0);

            categories.add(new CodexCategory(id, icon, entries, tier));
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
