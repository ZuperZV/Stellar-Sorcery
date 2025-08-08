package net.zuperz.stellar_sorcery.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;
import net.minecraft.tags.FluidTags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.fluid.ModFluids;
import net.zuperz.stellar_sorcery.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModFluidTagsProvider extends FluidTagsProvider {
    public ModFluidTagsProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pProvider,
                                @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pProvider, StellarSorcery.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(ModTags.Fluids.NOCTILUME)
                .add(ModFluids.SOURCE_NOCTILUME.get())
                .add(ModFluids.FLOWING_NOCTILUME.get());
    }
}