package net.zuperz.stellar_sorcery.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.zuperz.stellar_sorcery.StellarSorcery;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, StellarSorcery.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        /*this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.KILN.get())
                .add(ModBlocks.HARDENED_BRICKS.get());

        this.tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(ModBlocks.TERRAMIX.get());

        this.tag(ModTags.Blocks.KILN)
                .add(ModBlocks.KILN.get())
                .add(ModBlocks.HARDENED_BRICKS.get());

        this.tag(ModTags.Blocks.FORGE)
                .add(ModBlocks.FORGE_CORE.get())
                .add(ModBlocks.KILN.get())
                .add(ModBlocks.TERRAMIX.get());
         */


    }
}