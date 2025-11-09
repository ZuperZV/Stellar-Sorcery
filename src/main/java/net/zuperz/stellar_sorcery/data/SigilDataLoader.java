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
import net.zuperz.stellar_sorcery.capability.RecipesHelper.SoulCandleCommand;

import java.io.InputStreamReader;
import java.util.*;

public class SigilDataLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<ResourceLocation, SigilDefinition> SIGIL_DATA = new HashMap<>();
    private static final Map<String, ResourceLocation> NAME_TO_ID = new HashMap<>();
    private static final Map<ResourceLocation, ShaderData> SHADER_DATA = new HashMap<>();

    public static void load() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ResourceManager manager = server.getResourceManager();

        SIGIL_DATA.clear();
        SHADER_DATA.clear();
        NAME_TO_ID.clear();

        Map<ResourceLocation, Resource> resources = manager.listResources(
                "sigils",
                path -> path.getPath().endsWith(".json")
        );

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {
            ResourceLocation id = entry.getKey();
            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open())) {

                JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();

                String name = obj.has("name") ? obj.get("name").getAsString() : id.toString();
                String description = obj.has("description") ? obj.get("description").getAsString() : "";

                String color = obj.has("color") ? obj.get("color").getAsString() : "#FFFFFF";
                String armor = obj.has("armor") ? obj.get("armor").getAsString().toLowerCase(Locale.ROOT) : "none";

                List<MobEffectInstance> effects = new ArrayList<>();
                if (obj.has("effects")) {
                    for (JsonElement el : obj.getAsJsonArray("effects")) {
                        JsonObject eff = el.getAsJsonObject();
                        ResourceLocation effId = ResourceLocation.parse(eff.get("id").getAsString());
                        int duration = eff.get("duration").getAsInt();
                        int amplifier = eff.get("amplifier").getAsInt();

                        Holder<MobEffect> mobEffect = BuiltInRegistries.MOB_EFFECT.getHolder(effId).orElse(null);
                        if (mobEffect != null) {
                            effects.add(new MobEffectInstance(mobEffect, duration, amplifier));
                        } else {
                            System.err.println("[SigilLoader] Effekt ikke fundet: " + effId);
                        }
                    }
                }

                List<SoulCandleCommand> commands = new ArrayList<>();
                if (obj.has("commands")) {
                    for (JsonElement el : obj.getAsJsonArray("commands")) {
                        try {
                            JsonObject cmdObj = el.getAsJsonObject();
                            String command = cmdObj.get("command").getAsString();

                            SoulCandleCommand.Target target = SoulCandleCommand.Target.valueOf(
                                    cmdObj.get("target").getAsString().toUpperCase(Locale.ROOT)
                            );

                            SoulCandleCommand.Trigger trigger = SoulCandleCommand.Trigger.valueOf(
                                    cmdObj.get("trigger").getAsString().toUpperCase(Locale.ROOT)
                            );

                            commands.add(new SoulCandleCommand(command, target, trigger));

                        } catch (Exception ex) {
                            System.err.println("[SigilLoader] Fejl ved parsing af command i " + id + ": " + ex.getMessage());
                        }
                    }
                }

                ShaderData shaderData = null;
                if (obj.has("shader")) {
                    JsonObject shaderObj = obj.getAsJsonObject("shader");
                    String shaderId = shaderObj.get("id").getAsString();
                    int duration = shaderObj.get("duration").getAsInt();

                    ResourceLocation rl = ResourceLocation.tryParse(shaderId);
                    if (rl != null) {
                        shaderData = new ShaderData(rl, duration);
                        SHADER_DATA.put(id, shaderData);
                    } else {
                        System.err.println("[SigilLoader] Ugyldig shader-id i " + id);
                    }
                }

                SIGIL_DATA.put(id, new SigilDefinition(name, description, effects, shaderData, commands, color, armor));
                String simpleKey = id.getPath()
                        .replace("sigils/", "")
                        .replace(".json", "")
                        .toLowerCase(Locale.ROOT);

                NAME_TO_ID.put(simpleKey, id);
                NAME_TO_ID.put(name.toLowerCase(Locale.ROOT), id);

                System.out.println("[SigilLoader] Indlæst sigil: " + id);

            } catch (Exception e) {
                System.err.println("[SigilLoader] Fejl i fil " + id + ": " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static SigilDefinition get(ResourceLocation id) {
        return SIGIL_DATA.get(id);
    }

    public static SigilDefinition getByName(String name) {
        ResourceLocation id = NAME_TO_ID.get(name.toLowerCase(Locale.ROOT));
        return id != null ? SIGIL_DATA.get(id) : null;
    }

    public static ShaderData getShaderByName(String name) {
        ResourceLocation id = NAME_TO_ID.get(name.toLowerCase(Locale.ROOT));
        return id != null ? SHADER_DATA.get(id) : null;
    }

    public static List<MobEffectInstance> getEffectsByName(String name) {
        SigilDefinition def = getByName(name);
        return def != null ? def.effects() : List.of();
    }

    public static Collection<SigilDefinition> getAll() {
        return SIGIL_DATA.values();
    }

    public static List<MobEffectInstance> getAllEffects() {
        return SIGIL_DATA.values().stream()
                .flatMap(def -> def.effects().stream())
                .toList();
    }

    public static String getColorByName(String name) {
        SigilDefinition def = getByName(name);
        return def != null ? def.color() : "#FFFFFF";
    }

    public static String getArmorByName(String name) {
        SigilDefinition def = getByName(name);
        return def != null ? def.armor() : "none";
    }

    public static List<SoulCandleCommand> getCommandsByName(String name) {
        SigilDefinition def = getByName(name);
        return def != null ? def.commands() : Collections.emptyList();
    }

    public static String getRandomName(Random random) {
        if (NAME_TO_ID.isEmpty()) {
            System.err.println("[SigilLoader] Ingen sigils indlæst - kan ikke vælge tilfældigt navn!");
            return null;
        }

        List<String> keys = new ArrayList<>(NAME_TO_ID.keySet());
        return keys.get(random.nextInt(keys.size()));
    }

    public record SigilDefinition(
            String name,
            String description,
            List<MobEffectInstance> effects,
            ShaderData shader,
            List<SoulCandleCommand> commands,
            String color,
            String armor
    ) {}

    public static class ShaderData {
        public final ResourceLocation shaderId;
        public final int durationTicks;

        public ShaderData(ResourceLocation shaderId, int durationTicks) {
            this.shaderId = shaderId;
            this.durationTicks = durationTicks;
        }
    }
}