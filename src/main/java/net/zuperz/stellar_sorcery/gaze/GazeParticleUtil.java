package net.zuperz.stellar_sorcery.gaze;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;

public final class GazeParticleUtil {

    private GazeParticleUtil() {}

    public static ParticleOptions resolveParticle(String particleId, int colorRgb, ParticleOptions fallback) {
        if (particleId != null && !particleId.isBlank()) {
            ResourceLocation rl = ResourceLocation.tryParse(particleId);
            if (rl != null) {
                var type = BuiltInRegistries.PARTICLE_TYPE.get(rl);
                if (type instanceof SimpleParticleType simple) {
                    return simple;
                }
            }
        }

        if (colorRgb >= 0) {
            Vector3f color = new Vector3f(
                    ((colorRgb >> 16) & 0xFF) / 255.0f,
                    ((colorRgb >> 8) & 0xFF) / 255.0f,
                    (colorRgb & 0xFF) / 255.0f
            );
            return new DustParticleOptions(color, 1.0f);
        }

        return fallback != null ? fallback : ParticleTypes.END_ROD;
    }

    public static int parseColor(String color, int fallbackRgb) {
        if (color == null || color.isBlank()) return fallbackRgb;
        String clean = color.startsWith("#") ? color.substring(1) : color;
        try {
            if (clean.length() == 6) {
                return Integer.parseInt(clean, 16);
            }
            if (clean.length() == 8) {
                return Integer.parseInt(clean.substring(2), 16);
            }
        } catch (NumberFormatException ignored) {
        }
        return fallbackRgb;
    }
}
