package net.zuperz.stellar_sorcery.worldgen.dimension;

import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.worldgen.biome.ModBiomes;
import net.zuperz.stellar_sorcery.worldgen.biome.surface.ModSurfaceRules;

import java.util.List;
import java.util.OptionalLong;

public class ModDimensions {
    public static final ResourceKey<LevelStem> HOLLOWDIM_KEY = ResourceKey.create(Registries.LEVEL_STEM,
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "hollowdim"));

    public static final ResourceKey<Level> HOLLOWDIM_LEVEL_KEY = ResourceKey.create(Registries.DIMENSION,
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "hollowdim"));

    public static final ResourceKey<DimensionType> HOLLOW_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE,
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "hollowdim_type"));

    public static final ResourceKey<NoiseGeneratorSettings> HOLLOW_NOISE_SETTINGS = ResourceKey.create(Registries.NOISE_SETTINGS,
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "hollow_noise_settings"));

    public static final ResourceKey<NormalNoise.NoiseParameters> HOLLOW_NOISE = ResourceKey.create(Registries.NOISE,
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "hollow_noise"));

    public static final ResourceKey<NormalNoise.NoiseParameters> HOLLOW_SURFACE_NOISE = ResourceKey.create(Registries.NOISE,
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "hollow_surface"));


    public static void bootstrapType(BootstrapContext<DimensionType> context) {
        context.register(HOLLOW_DIM_TYPE, new DimensionType(
                OptionalLong.of(18000), // fixedTime
                false, // hasSkylight
                false, // hasCeiling
                false, // ultraWarm
                false, // natural
                1.0, // coordinateScale
                true, // bedWorks
                false, // respawnAnchorWorks
                -128, // minY
                384, // height
                384, // logicalHeight
                BlockTags.INFINIBURN_OVERWORLD, // infiniburn
                BuiltinDimensionTypes.END_EFFECTS, // effectsLocation
                0.0f, // ambientLight
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 7)));
    }

    public static void bootstrapNoise(BootstrapContext<NormalNoise.NoiseParameters> context) {
        context.register(HOLLOW_NOISE,
                new NormalNoise.NoiseParameters(
                        -7,       // firstOctave
                        1, 0, 0.5, 0.25, 0.25
                )
        );
        context.register(HOLLOW_SURFACE_NOISE,
                new NormalNoise.NoiseParameters(
                        -6,
                        1, 0, 0.5, 0.25, 0.25
                )
        );
    }

    public static void bootstrapNoiseSettings(BootstrapContext<NoiseGeneratorSettings> context) {
        final NoiseSettings HOLLOW_NOISE_SETTING = NoiseSettings.create(-128, 384, 1, 2);

        HolderGetter<NormalNoise.NoiseParameters> noiseGetter =
                context.lookup(Registries.NOISE);

        context.register(HOLLOW_NOISE_SETTINGS, new NoiseGeneratorSettings(
                HOLLOW_NOISE_SETTING,
                ModBlocks.GRIMROCK.get().defaultBlockState(),               // Default block
                Blocks.AIR.defaultBlockState(),                      // Default fluid
                ModNoiseRouter.createHollowDimRouter(noiseGetter),   // NoiseRouter
                ModSurfaceRules.makeRules(),                         // SurfaceRules
                List.of(),
                0,
                false,
                false,
                false,
                true
        ));
    }

    public static void bootstrapStem(BootstrapContext<LevelStem> context) {
        HolderGetter<Biome> biomeRegistry = context.lookup(Registries.BIOME);
        HolderGetter<DimensionType> dimTypes = context.lookup(Registries.DIMENSION_TYPE);
        HolderGetter<NoiseGeneratorSettings> noiseGenSettings = context.lookup(Registries.NOISE_SETTINGS);

        NoiseBasedChunkGenerator noiseBasedChunkGenerator = new NoiseBasedChunkGenerator(
                MultiNoiseBiomeSource.createFromList(
                        new Climate.ParameterList<>(List.of(Pair.of(
                                        Climate.parameters(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(ModBiomes.TEST_BIOME)),
                                Pair.of(Climate.parameters(1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.BADLANDS)),
                                Pair.of(Climate.parameters(-1.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F), biomeRegistry.getOrThrow(Biomes.CHERRY_GROVE))
                        ))),
                noiseGenSettings.getOrThrow(ModDimensions.HOLLOW_NOISE_SETTINGS));

        LevelStem stem = new LevelStem(dimTypes.getOrThrow(ModDimensions.HOLLOW_DIM_TYPE), noiseBasedChunkGenerator);

        context.register(HOLLOWDIM_KEY, stem);
    }
}
