package net.zuperz.stellar_sorcery.worldgen;

import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.*;
import net.zuperz.stellar_sorcery.StellarSorcery;

import java.util.List;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> RED_CAMPION_PLACED_KEY = registerKey("red_campion_placed");
    public static final ResourceKey<PlacedFeature> CALENDULA_PLACED_KEY = registerKey("calendula_placed");
    public static final ResourceKey<PlacedFeature> NIGELLA_DAMASCENA_PLACED_KEY = registerKey("nigella_damascena_placed");

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);
        register(context, RED_CAMPION_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.RED_CAMPION_KEY),
                List.of(RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()));

        register(context, CALENDULA_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.CALENDULA_KEY),
                List.of(RarityFilter.onAverageOnceEvery(6), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()));

        register(context, NIGELLA_DAMASCENA_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.NIGELLA_DAMASCENA_KEY),
                List.of(RarityFilter.onAverageOnceEvery(7), InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome()));
    }

    private static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration,
                                 List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, List.copyOf(modifiers)));
    }
}