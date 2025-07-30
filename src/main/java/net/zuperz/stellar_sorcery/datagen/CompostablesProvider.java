package net.zuperz.stellar_sorcery.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.registries.datamaps.builtin.Compostable;
import net.neoforged.neoforge.registries.datamaps.builtin.NeoForgeDataMaps;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.item.ModItems;

import java.util.concurrent.CompletableFuture;

public class CompostablesProvider extends DataMapProvider {
    protected CompostablesProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(packOutput, lookupProvider);
    }

    @Override
    protected void gather() {
        builder(NeoForgeDataMaps.COMPOSTABLES)
                .add(ModItems.FRITILLARIA_MELEAGRIS, new Compostable(0.3f), false)
                .add(ModItems.FRITILLARIA_MELEAGRIS_SEEDS, new Compostable(0.5f), false)

                .add(ModBlocks.RED_CAMPION.get().asItem().builtInRegistryHolder(), new Compostable(0.65f), false)
                .add(ModBlocks.CALENDULA.get().asItem().builtInRegistryHolder(), new Compostable(0.65f), false)
                .add(ModBlocks.NIGELLA_DAMASCENA.get().asItem().builtInRegistryHolder(), new Compostable(0.65f), false)
                .build();
    }
}