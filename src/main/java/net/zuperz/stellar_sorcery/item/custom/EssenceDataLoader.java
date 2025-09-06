package net.zuperz.stellar_sorcery.item.custom;

import com.google.gson.*;
import com.google.common.reflect.TypeToken;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.*;

public class EssenceDataLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<String, String> nameMap = new HashMap<>();
    private static final Map<String, List<MobEffectInstance>> effectMap = new HashMap<>();

    public static void load() {
        System.out.println("[EssenceDataLoader] Loader essence_data.json...");

        try (InputStreamReader reader = new InputStreamReader(
                EssenceDataLoader.class.getResourceAsStream("/data/stellar_sorcery/essence_data.json"))) {

            if (reader == null) {
                System.err.println("[EssenceDataLoader] FEJL: essence_data.json ikke fundet!");
                return;
            }

            Type mapType = new TypeToken<Map<String, JsonObject>>(){}.getType();
            Map<String, JsonObject> parsed = GSON.fromJson(reader, mapType);

            nameMap.clear();
            effectMap.clear();

            for (Map.Entry<String, JsonObject> entry : parsed.entrySet()) {
                String key = entry.getKey();
                JsonObject obj = entry.getValue();

                // Navn
                String name = obj.has("name") ? obj.get("name").getAsString() : null;
                if (name != null) {
                    nameMap.put(key, name);
                }

                // Effekter
                List<MobEffectInstance> effects = new ArrayList<>();
                if (obj.has("effects")) {
                    for (JsonElement el : obj.getAsJsonArray("effects")) {
                        JsonObject eff = el.getAsJsonObject();
                        String id = eff.get("id").getAsString();
                        int duration = eff.get("duration").getAsInt();
                        int amplifier = eff.get("amplifier").getAsInt();

                        ResourceLocation rl = ResourceLocation.tryParse(id);
                        if (rl != null) {
                            Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.getHolder(rl).orElse(null);
                            if (holder != null) {
                                effects.add(new MobEffectInstance(holder, duration, amplifier));
                            } else {
                                System.err.println("[EssenceDataLoader] Ingen effekt fundet for: " + rl);
                            }
                        }
                    }
                }
                effectMap.put(key, effects);
            }

            System.out.println("[EssenceDataLoader] Færdig med at loade. Keys: " + nameMap.size());

        } catch (Exception e) {
            System.err.println("[EssenceDataLoader] FEJL under load: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static String getName(String key) {
        return nameMap.get(key);
    }

    public static List<MobEffectInstance> getEffects(String key) {
        return effectMap.getOrDefault(key, List.of());
    }
}