package net.zuperz.stellar_sorcery.gaze;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.zuperz.stellar_sorcery.data.gaze.GazeDefinition;

public final class GazeParticleSpawner {

    private GazeParticleSpawner() {}

    public static void spawn(GazeSpellContext ctx) {
        GazeDefinition.Particles particles = ctx.definition().particles();
        if (particles == null) return;

        String type = particles.type();
        if (type == null || type.isBlank() || "none".equalsIgnoreCase(type)) return;
        if (!(ctx.level() instanceof ServerLevel serverLevel)) return;

        ParticleOptions particle = GazeParticleUtil.resolveParticle(
                particles.particle(),
                -1,
                ParticleTypes.HAPPY_VILLAGER
        );

        int count = Math.max(0, ctx.stats().particleCount);
        if (count == 0) return;

        double spread = ctx.stats().particleSpread;
        double speed = ctx.stats().particleSpeed;

        BlockPos center = ctx.target().getCenterPos(ctx.player().blockPosition());

        switch (type) {
            case "burst" -> serverLevel.sendParticles(
                    particle,
                    center.getX() + 0.5,
                    center.getY() + 0.5,
                    center.getZ() + 0.5,
                    count,
                    spread,
                    spread,
                    spread,
                    speed
            );

            case "beam" -> spawnBeam(serverLevel, particle, ctx.player().getEyePosition(1.0f),
                    ctx.target().hitPos() != null ? ctx.target().hitPos() : center.getCenter(), count);

            default -> spawnNature(serverLevel, particle, center, count,
                    particles.radius() > 0 ? particles.radius() : ctx.stats().actionRadius);
        }
    }

    private static void spawnNature(ServerLevel level, ParticleOptions particle, BlockPos center, int count, double radius) {
        double r = Math.max(0.5, radius);
        for (int i = 0; i < count; i++) {
            double ox = (level.random.nextDouble() - 0.5) * 2.0 * r;
            double oy = (level.random.nextDouble() - 0.5) * 1.0 * r;
            double oz = (level.random.nextDouble() - 0.5) * 2.0 * r;

            level.sendParticles(
                    particle,
                    center.getX() + 0.5 + ox,
                    center.getY() + 0.5 + oy,
                    center.getZ() + 0.5 + oz,
                    1,
                    0.0,
                    0.0,
                    0.0,
                    0.0
            );
        }
    }

    private static void spawnBeam(ServerLevel level, ParticleOptions particle, Vec3 start, Vec3 end, int count) {
        int steps = Math.max(4, Math.min(64, count));
        for (int i = 0; i <= steps; i++) {
            double t = i / (double) steps;
            Vec3 pos = start.lerp(end, t);
            level.sendParticles(particle, pos.x, pos.y, pos.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }
}
