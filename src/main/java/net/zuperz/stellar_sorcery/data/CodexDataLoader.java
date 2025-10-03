package net.zuperz.stellar_sorcery.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

        try {
            Map<ResourceLocation, Resource> resources = resourceManager.listResources(
                    "codex_entries",
                    path -> path.getNamespace().equals("stellar_sorcery") && path.getPath().endsWith(".json")
            );

            if (resources.isEmpty()) {
                System.err.println("[Codex] Ingen JSON-filer fundet i data/stellar_sorcery/codex_entries!");
            }

            for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
                try (InputStreamReader reader = new InputStreamReader(entry.getValue().open())) {
                    CodexEntry codexEntry = GSON.fromJson(reader, CodexEntry.class);
                    int id = nextId++;
                    entriesById.put(id, codexEntry);
                    idToInt.put(codexEntry.id, id);
                    System.out.println("[Codex] Indlæst: " + entry.getKey());
                } catch (Exception e) {
                    System.err.println("[Codex] Fejl ved indlæsning af " + entry.getKey() + ": " + e.getMessage());
                }
            }

            System.out.println("[Codex] Loaded " + entriesById.size() + " codex entries.");

        } catch (Exception e) {
            e.printStackTrace();
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
