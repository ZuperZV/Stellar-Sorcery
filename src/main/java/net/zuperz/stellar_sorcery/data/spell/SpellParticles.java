package net.zuperz.stellar_sorcery.data.spell;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

public class SpellParticles {

    public static void spawnBeamParticles(ServerLevel level, Vec3 start, Vec3 end, float size) {

        Vec3 diff = end.subtract(start);
        int steps = (int)(diff.length() * 6);

        for (int i = 0; i < steps; i++) {

            double t = i / (double) steps;
            Vec3 pos = start.lerp(end, t);

            level.sendParticles(
                    ParticleTypes.END_ROD,
                    pos.x, pos.y, pos.z,
                    4,
                    size * 0.2, size * 0.2, size * 0.2,
                    0.02
            );

            level.sendParticles(
                    ParticleTypes.ELECTRIC_SPARK,
                    pos.x, pos.y, pos.z,
                    1,
                    size * 0.15, size * 0.15, size * 0.15,
                    0.01
            );
        }
    }

}
