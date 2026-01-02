package net.zuperz.stellar_sorcery.planet;

import net.minecraft.resources.ResourceLocation;

public class PlanetData {

    public final ResourceLocation texture;
    public final float orbitRadius;
    public final float size;
    public final float speed;
    public final float startAngle;
    public final float tilt;
    public final ResourceLocation dimension;

    public PlanetData(
            ResourceLocation texture,
            float orbitRadius,
            float size,
            float speed,
            float startAngle,
            float tilt,
            ResourceLocation dimension) {

        this.texture = texture;
        this.orbitRadius = orbitRadius;
        this.size = size;
        this.speed = speed;
        this.startAngle = startAngle;
        this.tilt = tilt;
        this.dimension = dimension;
    }
}
