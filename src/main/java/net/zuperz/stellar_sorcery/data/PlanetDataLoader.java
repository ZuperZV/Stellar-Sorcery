package net.zuperz.stellar_sorcery.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import net.zuperz.stellar_sorcery.planet.PlanetData;
import net.zuperz.stellar_sorcery.planet.PlanetManager;
import net.zuperz.stellar_sorcery.planet.PlanetRenderer;

import java.io.InputStreamReader;
import java.util.*;

public class PlanetDataLoader {

    private static final Gson GSON = new Gson();

    private static final Map<ResourceLocation, PlanetData> PLANET_DATA = new HashMap<>();

    private static final Map<String, ResourceLocation> NAME_TO_ID = new HashMap<>();

    public static void load() {

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ResourceManager resourceManager = server.getResourceManager();

        PLANET_DATA.clear();
        NAME_TO_ID.clear();

        Map<ResourceLocation, Resource> resources = resourceManager.listResources(
                "planets",
                path -> path.getPath().endsWith(".json")
        );

        for (Map.Entry<ResourceLocation, Resource> entry : resources.entrySet()) {

            ResourceLocation id = entry.getKey();

            try (InputStreamReader reader = new InputStreamReader(entry.getValue().open())) {

                JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();

                ResourceLocation texture = ResourceLocation.parse(obj.get("texture").getAsString());
                float orbitRadius = obj.get("orbitRadius").getAsFloat();
                float size = obj.get("size").getAsFloat();
                float speed = obj.get("speed").getAsFloat();
                float startAngle = obj.get("startAngle").getAsFloat();
                float tilt = obj.get("tilt").getAsFloat();
                ResourceLocation dimension = ResourceLocation.parse(obj.get("dimension").getAsString());
                boolean startSpawned = obj.has("startSpawned") && obj.get("startSpawned").getAsBoolean();

                PlanetData planetData = new PlanetData(
                        texture,
                        orbitRadius,
                        size,
                        speed,
                        startAngle,
                        tilt,
                        dimension,
                        startSpawned
                );

                System.out.println("planet: " + planetData.texture);

                PLANET_DATA.put(id, planetData);
                PlanetRenderer.registerPlanet(planetData);
                if (startSpawned) {
                    PlanetRenderer.registerPlanetToRender(planetData);
                }
                PlanetManager.registerPlanet(planetData);

                String simpleName = id.getPath()
                        .replace("planets/", "")
                        .replace(".json", "")
                        .toLowerCase(Locale.ROOT);

                NAME_TO_ID.put(simpleName, id);

                System.out.println("[PlanetLoader] Indlæst planet: " + id);

            } catch (Exception e) {
                System.err.println("[PlanetLoader] Fejl i fil " + id);
                e.printStackTrace();
            }
        }
    }

    public static PlanetData get(ResourceLocation id) {
        return PLANET_DATA.get(id);
    }

    public static PlanetData getByName(String name) {
        ResourceLocation id = NAME_TO_ID.get(name.toLowerCase(Locale.ROOT));
        return id != null ? PLANET_DATA.get(id) : null;
    }

    public static Collection<PlanetData> getAll() {
        return PLANET_DATA.values();
    }
}