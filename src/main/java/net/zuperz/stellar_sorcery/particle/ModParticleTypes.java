package net.zuperz.stellar_sorcery.particle;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zuperz.stellar_sorcery.StellarSorcery;

import java.util.function.Supplier;

public class ModParticleTypes {
    public static final DeferredRegister<ParticleType<?>> PARTICLES
            = DeferredRegister.create(Registries.PARTICLE_TYPE, StellarSorcery.MOD_ID);

    public static final Supplier<ParticleType<ColorBubbleData>> COLOR_BUBBLE
            = register("color_bubble", ColorBubbleType::new);

    private static <T extends ParticleType<?>> Supplier<T> register(String name, Supplier<T> sup) {
        return PARTICLES.register(name, sup);
    }

    public static void register(IEventBus eventBus) {
        PARTICLES.register(eventBus);
    }
}