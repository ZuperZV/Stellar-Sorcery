package net.zuperz.stellar_sorcery.planet;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlanetManager {

    private static final List<PlanetData> PLANETS = new ArrayList<>();

    public static void registerPlanet(PlanetData planet) {
        PLANETS.add(planet);
    }

    public static List<PlanetData> getPlanetsFor(Level level) {
        ResourceLocation dim = level.dimension().location();
        return PLANETS.stream()
                .filter(p -> p.dimension.equals(dim))
                .toList();
    }

    public static boolean isPlanetActive(Level level, PlanetData planet) {
        long time = level.getDayTime() % 24000L;

        if (time < 13000 || time > 23000) return false;

        float angle = getPlanetAngle(level, planet);
        return angle > 0.25f && angle < 0.75f;
    }

    public static float getPlanetAngle(Level level, PlanetData planet) {
        long time = level.getDayTime();
        return (time * planet.speed) % 1.0f;
    }

    public static List<PlanetData> getActivePlanets(Level level) {
        return getPlanetsFor(level).stream()
                .filter(p -> isPlanetActive(level, p))
                .toList();
    }

    @Nullable
    public static PlanetData getDominantPlanet(Level level) {
        return getActivePlanets(level).stream()
                .max(Comparator.comparing(p -> getDominance(level, p)))
                .orElse(null);
    }

    public static float getDominance(Level level, PlanetData planet) {
        float angle = getPlanetAngle(level, planet);

        float distanceFromPeak = Math.abs(0.5f - angle);
        return 1.0f - (distanceFromPeak * 2.0f);
    }
}