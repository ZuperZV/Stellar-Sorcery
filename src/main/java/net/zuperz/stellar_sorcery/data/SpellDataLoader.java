package net.zuperz.stellar_sorcery.data;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.zuperz.stellar_sorcery.data.spell.*;

import java.io.InputStreamReader;
import java.util.Map;

public class SpellDataLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void load() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ResourceManager rm = server.getResourceManager();

        SpellRegistry.clear();

        loadAreas(rm);
        loadRunes(rm);
        loadModifiers(rm);
    }

    private static void loadAreas(ResourceManager rm) {

        Map<ResourceLocation, Resource> files =
                rm.listResources("spell/areas", p -> p.getPath().endsWith(".json"));

        for (ResourceLocation rl : files.keySet()) {
            try (InputStreamReader reader = new InputStreamReader(files.get(rl).open())) {

                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                AreaFile area = GSON.fromJson(json, AreaFile.class);

                SpellRegistry.AREAS.put(area.id, area);
                System.out.println("[Spell Loader] Loaded AREA: " + area.id);

            } catch (Exception e) {
                System.err.println("[Spell Loader] Error loading area " + rl + ": " + e);
            }
        }
    }

    private static void loadRunes(ResourceManager rm) {

        Map<ResourceLocation, Resource> files =
                rm.listResources("spell/runes", p -> p.getPath().endsWith(".json"));

        for (ResourceLocation rl : files.keySet()) {
            try (InputStreamReader reader = new InputStreamReader(files.get(rl).open())) {

                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                RuneFile rune = GSON.fromJson(json, RuneFile.class);

                SpellRegistry.RUNES.put(rune.id, rune);
                System.out.println("[Spell Loader] Loaded RUNE: " + rune.id);

            } catch (Exception e) {
                System.err.println("[Spell Loader] Error loading rune " + rl + ": " + e);
            }
        }
    }

    private static void loadModifiers(ResourceManager rm) {

        Map<ResourceLocation, Resource> files =
                rm.listResources("spell/catalyst_modifiers", p -> p.getPath().endsWith(".json"));

        for (ResourceLocation rl : files.keySet()) {
            try (InputStreamReader reader = new InputStreamReader(files.get(rl).open())) {

                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                ModifierFile mod = GSON.fromJson(json, ModifierFile.class);

                SpellRegistry.MODIFIERS.put(mod.id, mod);
                System.out.println("[Spell Loader] Loaded MODIFIER: " + mod.id);

            } catch (Exception e) {
                System.err.println("[Spell Loader] Error loading modifier " + rl + ": " + e);
            }
        }
    }
}
