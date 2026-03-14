package net.zuperz.stellar_sorcery.data.gaze;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public record GazeDefinition(
        ResourceLocation id,
        String type,
        Target target,
        Cost cost,
        Action action,
        Sacrifice sacrifice,
        Particles particles,
        HandEffect handEffect,
        String handAnimation,
        int cooldown,
        List<ResourceLocation> modifiers
) {

    public static GazeDefinition fromJson(ResourceLocation id, JsonObject obj) {
        String type = GazeJsonHelper.getString(obj, "type", "spell");

        Target target = parseTarget(GazeJsonHelper.getObject(obj, "target"));
        Cost cost = parseCost(GazeJsonHelper.getObject(obj, "cost"));
        Action action = parseAction(GazeJsonHelper.getObject(obj, "action"));
        Sacrifice sacrifice = parseSacrifice(GazeJsonHelper.getObject(obj, "sacrifice"));
        Particles particles = parseParticles(GazeJsonHelper.getObject(obj, "particles"));
        HandEffect handEffect = parseHandEffect(GazeJsonHelper.getObject(obj, "hand_effect"));

        String handAnimation = GazeJsonHelper.getString(obj, "hand_animation", "");
        int cooldown = GazeJsonHelper.getInt(obj, "cooldown", 0);

        List<ResourceLocation> modifiers = new ArrayList<>();
        JsonArray modArray = GazeJsonHelper.getArray(obj, "modifiers");
        if (modArray != null) {
            for (JsonElement el : modArray) {
                if (el.isJsonPrimitive()) {
                    ResourceLocation rl = ResourceLocation.tryParse(el.getAsString());
                    if (rl != null) modifiers.add(rl);
                }
            }
        }

        return new GazeDefinition(
                id,
                type,
                target,
                cost,
                action,
                sacrifice,
                particles,
                handEffect,
                handAnimation,
                cooldown,
                modifiers
        );
    }

    private static Target parseTarget(JsonObject obj) {
        if (obj == null) {
            return new Target("self", 0.0, 0.0, "player", "", true, false);
        }

        String type = GazeJsonHelper.getString(obj, "type", "self");
        double range = GazeJsonHelper.getDouble(obj, "range", 6.0);
        double radius = GazeJsonHelper.getDouble(obj, "radius", 0.0);
        String center = GazeJsonHelper.getString(obj, "center", "player");
        String structure = GazeJsonHelper.getString(obj, "structure", "");
        boolean includeEntities = GazeJsonHelper.getBoolean(obj, "include_entities", true);
        boolean includeBlocks = GazeJsonHelper.getBoolean(obj, "include_blocks", false);

        return new Target(type, range, radius, center, structure, includeEntities, includeBlocks);
    }

    private static Cost parseCost(JsonObject obj) {
        if (obj == null) {
            return new Cost(0, 0, 0, 0.0f);
        }

        int mana = GazeJsonHelper.getInt(obj, "mana", 0);
        int xpLevels = GazeJsonHelper.getInt(obj, "xp_levels", 0);
        int health = GazeJsonHelper.getInt(obj, "health", 0);
        float exhaustion = (float) GazeJsonHelper.getDouble(obj, "exhaustion", 0.0);

        return new Cost(mana, xpLevels, health, exhaustion);
    }

    private static Action parseAction(JsonObject obj) {
        if (obj == null) {
            return new Action("none", new JsonObject());
        }

        String type = GazeJsonHelper.getString(obj, "type", "none");
        JsonObject data = obj.deepCopy();
        return new Action(type, data);
    }

    private static Sacrifice parseSacrifice(JsonObject obj) {
        if (obj == null) {
            return new Sacrifice("none", "", 0, "", 0, "", 0, 0.0f, 0, -1, -1, "player", List.of());
        }

        String type = GazeJsonHelper.getString(obj, "type", "none");
        String item = GazeJsonHelper.getString(obj, "item", "");
        int count = GazeJsonHelper.getInt(obj, "count", 1);
        String fluid = GazeJsonHelper.getString(obj, "fluid", "");
        int amount = GazeJsonHelper.getInt(obj, "amount", 0);
        String entity = GazeJsonHelper.getString(obj, "entity", "");
        int radius = GazeJsonHelper.getInt(obj, "radius", 4);
        float health = (float) GazeJsonHelper.getDouble(obj, "health", 0.0);
        int lifeEnergy = GazeJsonHelper.getInt(obj, "life_energy", 0);
        int timeMin = GazeJsonHelper.getInt(obj, "time_min", -1);
        int timeMax = GazeJsonHelper.getInt(obj, "time_max", -1);
        String origin = GazeJsonHelper.getString(obj, "origin", "player");

        List<BlockRequirement> blocks = new ArrayList<>();
        JsonArray blockArray = GazeJsonHelper.getArray(obj, "blocks");
        if (blockArray != null) {
            for (JsonElement el : blockArray) {
                if (!el.isJsonObject()) continue;
                JsonObject b = el.getAsJsonObject();
                int x = GazeJsonHelper.getInt(b, "x", 0);
                int y = GazeJsonHelper.getInt(b, "y", 0);
                int z = GazeJsonHelper.getInt(b, "z", 0);
                String block = GazeJsonHelper.getString(b, "block", "");
                if (!block.isBlank()) blocks.add(new BlockRequirement(x, y, z, block));
            }
        }

        return new Sacrifice(type, item, count, fluid, amount, entity, radius, health, lifeEnergy, timeMin, timeMax, origin, blocks);
    }

    private static Particles parseParticles(JsonObject obj) {
        if (obj == null) {
            return new Particles("none", "", 0, 0.0, 0.0, 0.0);
        }

        String type = GazeJsonHelper.getString(obj, "type", "none");
        String particle = GazeJsonHelper.getString(obj, "particle", "");
        int count = GazeJsonHelper.getInt(obj, "count", 0);
        double spread = GazeJsonHelper.getDouble(obj, "spread", 0.2);
        double speed = GazeJsonHelper.getDouble(obj, "speed", 0.0);
        double radius = GazeJsonHelper.getDouble(obj, "radius", 0.0);

        return new Particles(type, particle, count, spread, speed, radius);
    }

    private static HandEffect parseHandEffect(JsonObject obj) {
        if (obj == null) {
            return new HandEffect("none", "", "", 0, 0.0);
        }

        String type = GazeJsonHelper.getString(obj, "type", "none");
        String particle = GazeJsonHelper.getString(obj, "particle", "");
        String color = GazeJsonHelper.getString(obj, "color", "");
        int duration = GazeJsonHelper.getInt(obj, "duration", 12);
        double intensity = GazeJsonHelper.getDouble(obj, "intensity", 1.0);

        return new HandEffect(type, particle, color, duration, intensity);
    }

    public record Target(
            String type,
            double range,
            double radius,
            String center,
            String structure,
            boolean includeEntities,
            boolean includeBlocks
    ) {}

    public record Cost(
            int mana,
            int xpLevels,
            int health,
            float exhaustion
    ) {}

    public record Action(
            String type,
            JsonObject data
    ) {}

    public record Sacrifice(
            String type,
            String item,
            int count,
            String fluid,
            int amount,
            String entity,
            int radius,
            float health,
            int lifeEnergy,
            int timeMin,
            int timeMax,
            String origin,
            List<BlockRequirement> blocks
    ) {}

    public record BlockRequirement(int x, int y, int z, String block) {}

    public record Particles(
            String type,
            String particle,
            int count,
            double spread,
            double speed,
            double radius
    ) {}

    public record HandEffect(
            String type,
            String particle,
            String color,
            int duration,
            double intensity
    ) {}
}
