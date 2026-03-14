package net.zuperz.stellar_sorcery.data;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.zuperz.stellar_sorcery.data.gaze.GazeDefinition;
import net.zuperz.stellar_sorcery.data.gaze.GazeModifierDefinition;
import net.zuperz.stellar_sorcery.data.gaze.GazeRegistry;

import java.io.InputStreamReader;
import java.util.Map;

public class GazeDataLoader {

    public static void load() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ResourceManager rm = server.getResourceManager();

        GazeRegistry.clear();

        loadGazes(rm);
        loadModifiers(rm);
    }

    private static void loadGazes(ResourceManager rm) {
        Map<ResourceLocation, Resource> files =
                rm.listResources("gaze", p -> isJsonDataFile(p.getPath()));

        for (ResourceLocation rl : files.keySet()) {
            try (InputStreamReader reader = new InputStreamReader(files.get(rl).open())) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                String baseName = rl.getPath()
                        .replace("gaze/", "")
                        .replace(".json", "");

                ResourceLocation gazeId = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), baseName);
                GazeDefinition def = GazeDefinition.fromJson(gazeId, json);

                GazeRegistry.registerGaze(def);
                System.out.println("[GazeLoader] Loaded GAZE: " + gazeId);

            } catch (Exception e) {
                System.err.println("[GazeLoader] Error loading gaze " + rl + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static void loadModifiers(ResourceManager rm) {
        Map<ResourceLocation, Resource> files =
                rm.listResources("gaze_modifiers", p -> isJsonDataFile(p.getPath()));

        for (ResourceLocation rl : files.keySet()) {
            try (InputStreamReader reader = new InputStreamReader(files.get(rl).open())) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();

                String baseName = rl.getPath()
                        .replace("gaze_modifiers/", "")
                        .replace(".json", "");

                ResourceLocation modId = ResourceLocation.fromNamespaceAndPath(rl.getNamespace(), baseName);
                GazeModifierDefinition def = GazeModifierDefinition.fromJson(modId, json);

                GazeRegistry.registerModifier(def);
                System.out.println("[GazeLoader] Loaded MODIFIER: " + modId);

            } catch (Exception e) {
                System.err.println("[GazeLoader] Error loading modifier " + rl + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private static boolean isJsonDataFile(String path) {
        if (!path.endsWith(".json")) return false;
        return !path.endsWith("_schema.json") && !path.contains("/schemas/");
    }
}
