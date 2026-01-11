package net.zuperz.stellar_sorcery.planet;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.entity.player.Player;
import org.joml.Quaternionf;
import org.joml.Vector2i;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PlanetRenderer {

    private static final List<PlanetData> PLANETS = new ArrayList<>();
    public static final List<PlanetData> PLANETS_TO_RENDER = new ArrayList<>();

    public static void registerPlanet(PlanetData planet) {
        PLANETS.add(planet);
    }

    public static void registerPlanetToRender(PlanetData planet) {
        PLANETS_TO_RENDER.add(planet);
    }

    public static PlanetData spawnPlanet(
            ResourceLocation texture,
            float orbitRadius,
            float size,
            float speed,
            float startAngle,
            float tilt,
            ResourceLocation dimension,
            boolean startSpawned
    ) {
        PlanetData planet = new PlanetData(
                texture,
                orbitRadius,
                size,
                speed,
                startAngle,
                tilt,
                dimension,
                startSpawned
        );

        PlanetRenderer.registerPlanetToRender(planet);

        return planet;
    }

    public static PlanetData spawnPlanet(
            PlanetData planetData
    ) {
        PlanetRenderer.registerPlanetToRender(planetData);
        return planetData;
    }

    public static void render(PoseStack poseStack, ClientLevel level, float partialTick) {
        long time = level.getDayTime();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (PlanetData planet : PLANETS_TO_RENDER) {
            updatePlanetMovement(planet);
            renderPlanet(poseStack, planet, level, partialTick);
        }

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private static void renderPlanet(PoseStack poseStack, PlanetData planet, ClientLevel level, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null && player.level().dimension().location().equals(planet.dimension)) {
            poseStack.pushPose();

            float angletilt;
            long time = level.getDayTime();
            if (planet.movementMode == PlanetData.MovementMode.FIXED) {
                angletilt = planet.currentStartAngle + planet.currentAngleOffset;
            } else {
                angletilt = (time + partialTick) * planet.speed + planet.currentStartAngle + planet.currentAngleOffset;
            }

            Quaternionf tiltQuat = Axis.XP.rotationDegrees(planet.currentTilt);
            Quaternionf spinQuat = Axis.YP.rotationDegrees(angletilt);
            tiltQuat.mul(spinQuat);
            poseStack.mulPose(tiltQuat);

            poseStack.translate(0.0D, 0.0D, -planet.currentOrbitRadius);

            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA,
                    GlStateManager.DestFactor.ONE,
                    GlStateManager.SourceFactor.ONE,
                    GlStateManager.DestFactor.ZERO
            );

            mc.getTextureManager().bindForSetup(planet.texture);

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderTexture(0, planet.texture);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);

            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.disableCull();

            BufferBuilder buffer = Tesselator.getInstance()
                    .begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);

            float s = planet.size;
            PoseStack.Pose pose = poseStack.last();
            var matrix = pose.pose();

            Vector2i size = getTextureSize(planet.texture);
            int totalFrames = size.y / size.x;

            int frameTime = 20;
            int currentFrame = (int)((time / frameTime) % totalFrames);

            float u0 = 0f;
            float u1 = 1f;
            float v0 = currentFrame / (float) totalFrames;
            float v1 = (currentFrame + 1) / (float) totalFrames;

            buffer.addVertex(matrix, -s, -s, 0).setUv(u0, v1);
            buffer.addVertex(matrix,  s, -s, 0).setUv(u1, v1);
            buffer.addVertex(matrix,  s,  s, 0).setUv(u1, v0);
            buffer.addVertex(matrix, -s,  s, 0).setUv(u0, v0);

            BufferUploader.drawWithShader(buffer.build());

            RenderSystem.enableCull();
            RenderSystem.depthMask(true);

            poseStack.popPose();
        }
    }

    private static void updatePlanetMovement(PlanetData planet) {
        if (planet.movementMode == PlanetData.MovementMode.ORBIT ||
                planet.movementMode == PlanetData.MovementMode.FIXED) {
            return;
        }

        planet.moveProgress += planet.moveSpeed / 1000f;
        float t = Math.min(planet.moveProgress, 1f);

        planet.currentOrbitRadius = lerp(planet.startOrbitRadius, planet.targetOrbitRadius, t);
        planet.currentAngleOffset = lerp(planet.startAngleOffset, planet.targetAngleOffset, t);
        planet.currentTilt = lerp(planet.startTilt, planet.targetTilt, t);
        planet.currentStartAngle = lerp(planet.startStartAngle, planet.targetStartAngle, t);

        if (t >= 1f) {
            planet.movementMode = PlanetData.MovementMode.ORBIT;
        }
    }

    private static float lerp(float a, float b, float t) {
        return a + (b - a) * t;
    }

    public static void movePlanetTo(PlanetData planet, float newOrbitRadius, float angleOffset, float tiltOffset, float startAngleOffset, boolean instant) {
        planet.targetOrbitRadius = newOrbitRadius;
        planet.targetAngleOffset = angleOffset;
        planet.targetTilt = tiltOffset;
        planet.targetStartAngle = startAngleOffset;

        if (instant) {
            planet.currentOrbitRadius = newOrbitRadius;
            planet.currentAngleOffset = angleOffset;
            planet.currentTilt = tiltOffset;
            planet.movementMode = PlanetData.MovementMode.FIXED;
        } else {
            planet.startOrbitRadius = planet.currentOrbitRadius;
            planet.startAngleOffset = planet.currentAngleOffset;
            planet.startTilt = planet.currentTilt;
            planet.startStartAngle = planet.currentStartAngle;
            planet.moveProgress = 0f;
            planet.movementMode = PlanetData.MovementMode.MOVING_TO_TARGET;
        }
    }

    public static void returnPlanetToOrbit(PlanetData planet, float newOrbitRadius, float angleOffset, float tiltOffset, float startAngleOffset, boolean instant) {
        planet.targetOrbitRadius = newOrbitRadius;
        planet.targetAngleOffset = angleOffset;
        planet.targetTilt = tiltOffset;
        planet.targetStartAngle = startAngleOffset;

        if (instant) {
            planet.currentOrbitRadius = planet.orbitRadius;
            planet.currentAngleOffset = 0f;
            planet.movementMode = PlanetData.MovementMode.ORBIT;
        } else {
            planet.currentOrbitRadius = newOrbitRadius;
            planet.currentAngleOffset = angleOffset;
            planet.currentTilt = tiltOffset;

            planet.startOrbitRadius = newOrbitRadius;
            planet.startAngleOffset = angleOffset;
            planet.startTilt = tiltOffset;
            planet.startStartAngle = startAngleOffset;

            planet.targetOrbitRadius = planet.orbitRadius;
            planet.targetAngleOffset = 0f;
            planet.targetTilt = planet.tilt;
            planet.targetStartAngle = planet.startAngle;

            planet.moveProgress = 0f;
            planet.movementMode = PlanetData.MovementMode.RETURNING_TO_ORBIT;
        }
    }

    public static void returnPlanetToOrbit(PlanetData planet, boolean instant) {
        if (instant) {
            planet.currentOrbitRadius = planet.orbitRadius;
            planet.currentAngleOffset = 0f;
            planet.movementMode = PlanetData.MovementMode.ORBIT;
        } else {
            planet.startOrbitRadius = planet.currentOrbitRadius;
            planet.startAngleOffset = planet.currentAngleOffset;
            planet.startTilt = planet.currentTilt;
            planet.startStartAngle = planet.currentStartAngle;

            planet.targetOrbitRadius = planet.orbitRadius;
            planet.targetAngleOffset = 0f;
            planet.targetTilt = planet.tilt;
            planet.targetStartAngle = planet.startAngle;

            planet.moveProgress = 0f;
            planet.movementMode = PlanetData.MovementMode.RETURNING_TO_ORBIT;
        }
    }

    public static Vector2i getTextureSize(ResourceLocation texture) {
        ResourceManager rm = Minecraft.getInstance().getResourceManager();

        try {
            Resource res = rm.getResource(texture).orElseThrow();

            try (InputStream is = res.open();
                 NativeImage img = NativeImage.read(is)) {

                return new Vector2i(img.getWidth(), img.getHeight());
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return new Vector2i(0, 0);
    }
}
