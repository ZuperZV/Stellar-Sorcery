package net.zuperz.stellar_sorcery.planet;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.data.PlanetDataLoader;

import static net.zuperz.stellar_sorcery.planet.PlanetRenderer.movePlanetTo;
import static net.zuperz.stellar_sorcery.planet.PlanetRenderer.returnPlanetToOrbit;

public class PlanetCommandHandler {

    public static void execute(ServerLevel level, String command) {
        // Eksempel:
        // spawn luna overworld 200 20 0.01 15
        // move luna 120 90 false
        // return luna true

        String[] args = command.split(" ");
        if (args.length < 2) return;

        String action = args[0];
        String planetId = args[1];

        PlanetData planet = PlanetDataLoader.getByName(planetId);
        if (planet == null && !action.equals("spawn")) return;

        switch (action) {
            case "spawn" -> spawn(args);
            case "move" -> move(planet, args);
            case "return" -> ret(planet, args);
        }
    }

    private static void spawn(String[] args) {
        // spawn <id> <dimension> <orbit> <size> <speed> <tilt>
        if (args.length < 7) return;

        PlanetData planetData = PlanetDataLoader.getByName(args[1]);

        PlanetData newPlanetData = planetData;

        if (planetData == null) {
            System.out.println("PlanetCommandHandler: Needed to make a new Planet: " + args[1]);
            String id = args[1];
            ResourceLocation dim = ResourceLocation.parse(args[2]);

            float orbit = Float.parseFloat(args[3]);
            float size = Float.parseFloat(args[4]);
            float speed = Float.parseFloat(args[5]);
            float tilt = Float.parseFloat(args[6]);

            newPlanetData = PlanetRenderer.spawnPlanet(
                    ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/planet/" + id + ".png"),
                    orbit,
                    size,
                    speed,
                    0f,
                    tilt,
                    dim,
                    true
            );
        }
        else {
            if (planetData.startSpawned || PlanetRenderer.PLANETS_TO_RENDER.contains(planetData)) {
                return;
            }
            PlanetRenderer.spawnPlanet(planetData);
        }

        movePlanetTo(newPlanetData, newPlanetData.orbitRadius, 30f, 90f, 90f, true);
        returnPlanetToOrbit(newPlanetData, newPlanetData.orbitRadius, 30f, -90f, 90f, false);
    }

    private static void move(PlanetData planet, String[] args) {
        // move <id> <orbit> <angle> <tilt?> <instant?>
        if (args.length < 4) return;

        float orbit = Float.parseFloat(args[2]);
        float angle = Float.parseFloat(args[3]);

        float tilt;
        boolean instant;

        if (args.length > 4 && (args[4].equalsIgnoreCase("true") || args[4].equalsIgnoreCase("false"))) {
            tilt = planet.tilt;
            instant = Boolean.parseBoolean(args[4]);
        } else {
            tilt = args.length > 4 ? Float.parseFloat(args[4]) : planet.tilt;
            instant = args.length > 5 && Boolean.parseBoolean(args[5]);
        }

        movePlanetTo(planet, orbit, angle, tilt, planet.startAngle, instant);
    }

    private static void ret(PlanetData planet, String[] args) {
        // return <id> <instant>
        boolean instant = args.length > 2 && Boolean.parseBoolean(args[2]);
        returnPlanetToOrbit(planet, instant);
    }
}