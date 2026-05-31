package net.zuperz.stellar_sorcery.data;

import com.google.gson.*;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;

public class EssenceEffectDataLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, String> nameMap = new HashMap<>();
    private static final Map<String, String> iconMap = new HashMap<>();
    private static final Map<String, List<MobEffectInstance>> effectMap = new HashMap<>();
    private static final Map<String, List<ShaderData>> shaderMap = new HashMap<>();

    public static void load() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            clear();
            return;
        }

        loadFromResourceManager(server.getResourceManager());
    }

    public static void loadFromResourceManager(ResourceManager resourceManager) {
        System.out.println("[EssenceEffectDataLoader] Loading essence effects...");

        clear();

        Map<ResourceLocation, Resource> resources =
                resourceManager.listResources("essence_effects",
                        path -> path.getPath().endsWith(".json"));

        for (ResourceLocation rl : resources.keySet()) {
            try (InputStream is = resourceManager.open(rl)) {

                JsonObject obj = JsonParser.parseReader(new InputStreamReader(is)).getAsJsonObject();

                String id = normalizeId(rl);

                if (obj.has("name")) {
                    nameMap.put(id, obj.get("name").getAsString());
                }

                if (obj.has("icon")) {
                    iconMap.put(id, obj.get("icon").getAsString());
                }

                List<MobEffectInstance> effects = new ArrayList<>();
                if (obj.has("effects")) {
                    for (JsonElement el : obj.getAsJsonArray("effects")) {
                        JsonObject e = el.getAsJsonObject();

                        ResourceLocation effectId = ResourceLocation.tryParse(e.get("id").getAsString());
                        int duration = e.has("duration") ? e.get("duration").getAsInt() : 200;
                        int amp = e.has("amplifier") ? e.get("amplifier").getAsInt() : 0;

                        if (effectId != null) {
                            Holder<MobEffect> holder =
                                    BuiltInRegistries.MOB_EFFECT.getHolder(effectId).orElse(null);

                            if (holder != null) {
                                effects.add(new MobEffectInstance(holder, duration, amp));
                            }
                        }
                    }
                }
                effectMap.put(id, effects);

                List<ShaderData> shaders = new ArrayList<>();
                if (obj.has("shaders")) {
                    for (JsonElement el : obj.getAsJsonArray("shaders")) {
                        JsonObject s = el.getAsJsonObject();

                        ResourceLocation shaderId = ResourceLocation.tryParse(s.get("id").getAsString());
                        int duration = s.get("duration").getAsInt();

                        if (shaderId != null) {
                            shaders.add(new ShaderData(shaderId, duration));
                        }
                    }
                }
                shaderMap.put(id, shaders);

            } catch (Exception e) {
                System.err.println("[EssenceEffectDataLoader] Failed: " + rl + " -> " + e.getMessage());
            }
        }

        System.out.println("[EssenceEffectDataLoader] Loaded: " + nameMap.size());
    }

    private static String normalizeId(ResourceLocation rl) {
        String path = rl.getPath();
        if (path.endsWith(".json")) {
            path = path.substring(0, path.length() - 5);
        }
        return path;
    }

    public static String getName(String id) {
        return nameMap.get(id);
    }

    public static String getIcon(String id) {
        return iconMap.get(id);
    }

    public static List<MobEffectInstance> getEffects(String id) {
        return effectMap.getOrDefault(id, List.of());
    }

    public static List<ShaderData> getShaders(String id) {
        return shaderMap.getOrDefault(id, List.of());
    }

    public static void clear() {
        nameMap.clear();
        iconMap.clear();
        effectMap.clear();
        shaderMap.clear();
    }

    public static class ShaderData {
        public final ResourceLocation shaderId;
        public final int duration;

        public ShaderData(ResourceLocation shaderId, int duration) {
            this.shaderId = shaderId;
            this.duration = duration;
        }
    }
}