package net.zuperz.stellar_sorcery.client.gaze;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.zuperz.stellar_sorcery.gaze.GazeParticleUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class GazeClientEffects {

    private static final List<ActiveHandEffect> ACTIVE_EFFECTS = new ArrayList<>();
    private static final Map<Integer, ActiveAnimation> ACTIVE_ANIMATIONS = new HashMap<>();

    private GazeClientEffects() {}

    public static void trigger(
            int playerId,
            String handAnimation,
            int animationDuration,
            boolean hasEffect,
            String effectType,
            String effectColor,
            String effectParticle,
            int effectDuration
    ) {
        if (handAnimation != null && !handAnimation.isBlank() && animationDuration > 0) {
            ACTIVE_ANIMATIONS.put(playerId, new ActiveAnimation(handAnimation, animationDuration));
        }

        if (hasEffect && effectDuration > 0) {
            int color = GazeParticleUtil.parseColor(effectColor, -1);
            ACTIVE_EFFECTS.add(new ActiveHandEffect(
                    playerId,
                    effectType == null ? "" : effectType,
                    effectParticle == null ? "" : effectParticle,
                    color,
                    effectDuration
            ));
        }
    }

    public static void onClientTick(Minecraft mc) {
        ClientLevel level = mc.level;
        if (level == null) return;

        Iterator<ActiveHandEffect> it = ACTIVE_EFFECTS.iterator();
        while (it.hasNext()) {
            ActiveHandEffect effect = it.next();
            if (effect.ticksLeft-- <= 0) {
                it.remove();
                continue;
            }

            if (!(level.getEntity(effect.playerId) instanceof Player player)) {
                it.remove();
                continue;
            }

            spawnHandEffect(level, player, effect);
        }

        Iterator<Map.Entry<Integer, ActiveAnimation>> animIt = ACTIVE_ANIMATIONS.entrySet().iterator();
        while (animIt.hasNext()) {
            Map.Entry<Integer, ActiveAnimation> entry = animIt.next();
            ActiveAnimation anim = entry.getValue();
            if (anim.ticksLeft-- <= 0) {
                animIt.remove();
            }
        }
    }

    public static String getActiveAnimation(int playerId) {
        ActiveAnimation anim = ACTIVE_ANIMATIONS.get(playerId);
        return anim != null ? anim.animationId : "";
    }

    private static void spawnHandEffect(ClientLevel level, Player player, ActiveHandEffect effect) {
        ParticleOptions particle = GazeParticleUtil.resolveParticle(
                effect.particleId,
                effect.colorRgb,
                ParticleTypes.END_ROD
        );

        String type = effect.type;
        if (type == null || type.isBlank()) type = "energy_flow";

        switch (type) {
            case "magic_circle" -> spawnMagicCircle(level, player, particle);
            case "hand_glow" -> spawnHandGlow(level, player, particle, 1);
            case "beam" -> spawnBeam(level, player, particle, 8);
            default -> spawnHandGlow(level, player, particle, 2);
        }
    }

    private static void spawnHandGlow(ClientLevel level, Player player, ParticleOptions particle, int countPerHand) {
        Vec3 rightHand = getHandPosition(player, true);
        Vec3 leftHand = getHandPosition(player, false);

        for (int i = 0; i < countPerHand; i++) {
            level.addParticle(
                    particle,
                    rightHand.x + randomOffset(level),
                    rightHand.y + randomOffset(level),
                    rightHand.z + randomOffset(level),
                    0.0,
                    0.0,
                    0.0
            );
            level.addParticle(
                    particle,
                    leftHand.x + randomOffset(level),
                    leftHand.y + randomOffset(level),
                    leftHand.z + randomOffset(level),
                    0.0,
                    0.0,
                    0.0
            );
        }
    }

    private static void spawnMagicCircle(ClientLevel level, Player player, ParticleOptions particle) {
        Vec3 center = player.position();
        int points = 16;
        double radius = 0.7;

        for (int i = 0; i < points; i++) {
            double angle = (Math.PI * 2.0) * (i / (double) points);
            double x = center.x + Math.cos(angle) * radius;
            double z = center.z + Math.sin(angle) * radius;
            level.addParticle(particle, x, center.y + 0.1, z, 0.0, 0.0, 0.0);
        }
    }

    private static void spawnBeam(ClientLevel level, Player player, ParticleOptions particle, int steps) {
        Vec3 start = player.getEyePosition(1.0f);
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(3.0));

        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            Vec3 pos = start.lerp(end, t);
            level.addParticle(particle, pos.x, pos.y, pos.z, 0.0, 0.0, 0.0);
        }
    }

    private static Vec3 getHandPosition(Player player, boolean right) {
        Vec3 eye = player.getEyePosition(1.0f);
        Vec3 look = player.getLookAngle();
        Vec3 up = new Vec3(0, 1, 0);
        Vec3 rightVec = look.cross(up);
        if (rightVec.lengthSqr() < 1.0E-4) {
            rightVec = new Vec3(1, 0, 0);
        } else {
            rightVec = rightVec.normalize();
        }

        double side = right ? 0.25 : -0.25;
        Vec3 offset = rightVec.scale(side).add(look.scale(0.2)).add(0.0, -0.2, 0.0);
        return eye.add(offset);
    }

    private static double randomOffset(ClientLevel level) {
        return (level.random.nextDouble() - 0.5) * 0.1;
    }

    private static final class ActiveHandEffect {
        private final int playerId;
        private final String type;
        private final String particleId;
        private final int colorRgb;
        private int ticksLeft;

        private ActiveHandEffect(int playerId, String type, String particleId, int colorRgb, int ticksLeft) {
            this.playerId = playerId;
            this.type = type;
            this.particleId = particleId;
            this.colorRgb = colorRgb;
            this.ticksLeft = ticksLeft;
        }
    }

    private static final class ActiveAnimation {
        private final String animationId;
        private int ticksLeft;

        private ActiveAnimation(String animationId, int ticksLeft) {
            this.animationId = animationId;
            this.ticksLeft = ticksLeft;
        }
    }
}
