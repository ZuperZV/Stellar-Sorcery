package net.zuperz.stellar_sorcery.fluid;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.zuperz.stellar_sorcery.StellarSorcery;
import org.joml.Vector3f;

import java.util.function.Supplier;

public class ModFluidTypes {
    public static final ResourceLocation WATER_STILL_RL = ResourceLocation.parse("block/water_still");
    public static final ResourceLocation WATER_FLOWING_RL = ResourceLocation.parse("block/water_flow");
    public static final ResourceLocation WATER_OVERLAY_RL = ResourceLocation.parse("block/water_overlay");

    public static final DeferredRegister<FluidType> FLUID_TYPES =
            DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, StellarSorcery.MOD_ID);

    public static final Supplier<FluidType> NOCTILUME_FLUID_TYPE = registerFluidType("noctilume",
            new BaseFluidType(WATER_STILL_RL, WATER_FLOWING_RL, WATER_OVERLAY_RL, 0xE99E9BA9,
                    new Vector3f(108f / 255f, 168f / 255f, 212f / 255f),
                    FluidType.Properties.create()
                            .temperature(600)
                            .density(1500)
                            .viscosity(3000)
                            .fallDistanceModifier(0.5F)
                            .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                            .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)));

    private static Supplier<FluidType> registerFluidType(String name, FluidType fluidType) {
        return FLUID_TYPES.register(name, () -> fluidType);
    }

    public static void register(IEventBus eventBus) {
        FLUID_TYPES.register(eventBus);
    }
}