package net.zuperz.stellar_sorcery.planet;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.zuperz.stellar_sorcery.StellarSorcery;
import org.joml.Quaternionf;

import java.util.List;

public class PlanetRenderer {

    private static final List<PlanetData> PLANETS = List.of();

    public static void registerPlanet(PlanetData planet) {
        PLANETS.add(planet);
    }

    public static void render(
            PoseStack poseStack,
            ClientLevel level,
            float partialTick
    ) {
        //if (level.isDay()) return;

        long time = level.getDayTime();

        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();

        for (PlanetData planet : PLANETS) {
            renderPlanet(poseStack, planet, time, partialTick);
            System.out.println("PlanetRenderer: " + PLANETS);
        }

        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
    }

    private static void renderPlanet(
            PoseStack poseStack,
            PlanetData planet,
            long time,
            float partialTick
    ) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player != null) {
            if (player.level().dimension().location().toString().equals(planet.dimension.toString())) {
                poseStack.pushPose();

                float angletilt = (time + partialTick) * planet.speed + planet.startAngle;
                Quaternionf tiltQuat = Axis.XP.rotationDegrees(planet.tilt);
                Quaternionf spinQuat = Axis.YP.rotationDegrees(angletilt);
                tiltQuat.mul(spinQuat);
                poseStack.mulPose(tiltQuat);

                poseStack.translate(0.0D, 0.0D, -planet.orbitRadius);

                RenderSystem.enableBlend();
                RenderSystem.blendFuncSeparate( GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO );

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

                buffer.addVertex(matrix, -s, -s, 0).setUv(0, 1);
                buffer.addVertex(matrix,  s, -s, 0).setUv(1, 1);
                buffer.addVertex(matrix,  s,  s, 0).setUv(1, 0);
                buffer.addVertex(matrix, -s,  s, 0).setUv(0, 0);

                BufferUploader.drawWithShader(buffer.buildOrThrow());

                RenderSystem.enableCull();
                RenderSystem.depthMask(true);

                poseStack.popPose();
            }
            else {
                System.out.println(("Skipping planet render, wrong dimension: " + player.level().dimension().location() + " " + planet.dimension));
            }
        }
    }
}