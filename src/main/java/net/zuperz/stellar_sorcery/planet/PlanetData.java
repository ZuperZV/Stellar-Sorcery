package net.zuperz.stellar_sorcery.planet;

import net.minecraft.resources.ResourceLocation;

public class PlanetData {

    public enum MovementMode {
        ORBIT,          // Normal bane
        MOVING_TO_TARGET,
        RETURNING_TO_ORBIT,
        FIXED
        }

    public final ResourceLocation texture;
    public final float orbitRadius;
    public final float size;
    public final float speed;
    public final float startAngle;
    public final float tilt;
    public final ResourceLocation dimension;
    public final Boolean startSpawned;

    // === Movement ===
    public float currentOrbitRadius;
    public float targetOrbitRadius;

    public float currentAngleOffset = 0f;
    public float targetAngleOffset = 0f;

    public float currentTilt;
    public float targetTilt;

    public float currentStartAngle;
    public float targetStartAngle;

    public float moveProgress = 0f; // max = 1
    public float moveSpeed = 0.005f;

    public float startOrbitRadius;
    public float startAngleOffset;
    public float startTilt;
    public float startStartAngle;


    public MovementMode movementMode = MovementMode.ORBIT;

    public PlanetData(
            ResourceLocation texture,
            float orbitRadius,
            float size,
            float speed,
            float startAngle,
            float tilt,
            ResourceLocation dimension,
            boolean startSpawned) {

        this.texture = texture;
        this.orbitRadius = orbitRadius;
        this.size = size;
        this.speed = speed;
        this.startAngle = startAngle;
        this.tilt = tilt;
        this.dimension = dimension;
        this.startSpawned = startSpawned;
    }
}
