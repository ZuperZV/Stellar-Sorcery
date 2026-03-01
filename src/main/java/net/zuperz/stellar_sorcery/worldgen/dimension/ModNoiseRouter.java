package net.zuperz.stellar_sorcery.worldgen.dimension;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

public class ModNoiseRouter {

    public static NoiseRouter createHollowDimRouter(HolderGetter<NormalNoise.NoiseParameters> noiseGetter) {

        DensityFunction zero = DensityFunctions.zero();

        // --- Barrier / lava / fluid ---
        DensityFunction barrier = zero;
        DensityFunction lava = zero;
        DensityFunction fluidFloodedness = zero;
        DensityFunction fluidSpread = zero;

        Holder<NormalNoise.NoiseParameters> hollowSurfaceNoise =
                noiseGetter.getOrThrow(ModDimensions.HOLLOW_SURFACE_NOISE);

        DensityFunction surfaceNoise = DensityFunctions.cache2d(
                DensityFunctions.noise(
                        hollowSurfaceNoise,
                        0.7,
                        0.0
                )
        );
        DensityFunction crackNoise = DensityFunctions.cache2d(
                DensityFunctions.noise(
                        hollowSurfaceNoise,
                        1.6,
                        0.0
                )
        );
        DensityFunction crackAbs = DensityFunctions.max(
                crackNoise,
                DensityFunctions.mul(DensityFunctions.constant(-1.0), crackNoise)
        );
        DensityFunction crackMask = DensityFunctions.rangeChoice(
                crackAbs,
                0.0,
                0.08,
                DensityFunctions.constant(1.0),
                DensityFunctions.constant(0.0)
        );
        DensityFunction crackDepth = DensityFunctions.yClampedGradient(
                -64,
                200,
                1.0,
                0.0
        );
        DensityFunction cracks = DensityFunctions.mul(
                DensityFunctions.constant(-1.1),
                DensityFunctions.mul(crackMask, crackDepth)
        );

        // --- Temperature / vegetation ---
        DensityFunction temperature = surfaceNoise;
        DensityFunction vegetation = surfaceNoise;

        // --- Terrain noise ---
        DensityFunction continents = surfaceNoise;
        DensityFunction erosion = surfaceNoise;
        DensityFunction depth = surfaceNoise;
        DensityFunction ridges = surfaceNoise;

        // --- Initial density ---
        DensityFunction initialDensityWithoutJaggedness = DensityFunctions.constant(0.0);

        DensityFunction gradient = DensityFunctions.yClampedGradient(
                20,
                180,
                1.25,
                -1.25
        );

        Holder<NormalNoise.NoiseParameters> hollowNoise =
                noiseGetter.getOrThrow(ModDimensions.HOLLOW_NOISE);

        DensityFunction noise = DensityFunctions.noise(
                hollowNoise,
                0.55,
                0.35
        );
        DensityFunction cavernNoise = DensityFunctions.noise(
                hollowNoise,
                0.25,
                0.9
        );
        DensityFunction caverns = DensityFunctions.mul(
                DensityFunctions.constant(-0.6),
                DensityFunctions.max(cavernNoise, DensityFunctions.constant(0.0))
        );
        DensityFunction strata = DensityFunctions.mul(
                DensityFunctions.constant(0.15),
                DensityFunctions.noise(
                        hollowSurfaceNoise,
                        0.2,
                        2.5
                )
        );

        DensityFunction finalDensity = DensityFunctions.add(
                DensityFunctions.add(
                        DensityFunctions.add(
                                gradient,
                                DensityFunctions.mul(DensityFunctions.constant(-1.0), surfaceNoise)
                        ),
                        DensityFunctions.add(noise, strata)
                ),
                DensityFunctions.add(cracks, caverns)
        );

        // --- Ore / vein ---
        DensityFunction veinToggle = zero;
        DensityFunction veinRidged = zero;
        DensityFunction veinGap = zero;

        return new NoiseRouter(
                barrier,
                fluidFloodedness,
                fluidSpread,
                lava,
                temperature,
                vegetation,
                continents,
                erosion,
                depth,
                ridges,
                initialDensityWithoutJaggedness,
                finalDensity,
                veinToggle,
                veinRidged,
                veinGap
        );
    }
}
