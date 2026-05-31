package net.zuperz.stellar_sorcery.data;

import com.google.gson.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class EssenceIngredientEffectDataLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, String> itemToEffect = new HashMap<>();
    private static final Map<String, List<String>> ingredientEffects = new HashMap<>();

    public static void load() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            clear();
            return;
        }

        loadFromResourceManager(server.getResourceManager());
    }

    public static void loadFromResourceManager(ResourceManager resourceManager) {
        System.out.println("[EssenceIngredientEffectDataLoader] Loading...");

        clear();

        Map<ResourceLocation, Resource> resources =
                resourceManager.listResources("essence_ingredients",
                        path -> path.getPath().endsWith(".json"));

        for (ResourceLocation rl : resources.keySet()) {
            try (InputStream is = resourceManager.open(rl)) {

                JsonObject obj = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();

                String itemId = obj.get("item").getAsString();

                List<String> effects = new ArrayList<>();

                if (obj.has("effects")) {
                    for (JsonElement el : obj.getAsJsonArray("effects")) {
                        JsonObject e = el.getAsJsonObject();

                        String rawId = e.get("id").getAsString();
                        String normalized = normalizeEffectId(rawId);

                        effects.add(normalized);
                    }
                }

                ingredientEffects.put(itemId, effects);

                if (!effects.isEmpty()) {
                    itemToEffect.put(itemId, effects.get(0));
                }

            } catch (Exception e) {
                System.err.println("[EssenceIngredientEffectDataLoader] Failed: " + rl + " -> " + e.getMessage());
            }
        }

        System.out.println("[EssenceIngredientEffectDataLoader] Loaded ingredients: " + ingredientEffects.size());
    }

    private static String normalizeEffectId(String raw) {
        if (raw == null) return null;

        if (raw.endsWith(".json")) {
            raw = raw.substring(0, raw.length() - 5);
        }

        raw = raw.replace("essence_effects/", "");

        return raw;
    }

    public static List<String> getEffectsForItem(String itemId) {
        return ingredientEffects.getOrDefault(itemId, List.of());
    }

    public static String getPrimaryEffect(String itemId) {
        return itemToEffect.get(itemId);
    }

    public static void clear() {
        itemToEffect.clear();
        ingredientEffects.clear();
    }
}