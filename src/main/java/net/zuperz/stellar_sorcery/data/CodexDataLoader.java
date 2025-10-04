package net.zuperz.stellar_sorcery.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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
