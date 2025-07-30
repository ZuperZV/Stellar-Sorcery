package net.zuperz.stellar_sorcery.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {
    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, StellarSorcery.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.ASTRAL_ALTAR.get())
                .add(ModBlocks.ASTRAL_NEXUS.get());

        this.tag(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.VITAL_STUMP.get())
                .add(ModBlocks.STUMP.get());

        this.tag(BlockTags.MINEABLE_WITH_AXE)
                .add(ModBlocks.VITAL_STUMP.get())
                .add(ModBlocks.STUMP.get());

        this.tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.ASTRAL_ALTAR.get())
                .add(ModBlocks.ASTRAL_NEXUS.get());

        this.tag(BlockTags.CROPS)
                .add(ModBlocks.FRITILLARIA_MELEAGRIS_CROP.get());

        this.tag(ModTags.Blocks.STELLER_SORCERY_FLOWERS_BLOCKS)
                .add(ModBlocks.RED_CAMPION.get())
                .add(ModBlocks.CALENDULA.get())
                .add(ModBlocks.NIGELLA_DAMASCENA.get());

        this.tag(BlockTags.FLOWERS)
                .add(ModBlocks.RED_CAMPION.get())
                .add(ModBlocks.CALENDULA.get())
                .add(ModBlocks.NIGELLA_DAMASCENA.get());

        this.tag(BlockTags.FLOWER_POTS)
                .add(ModBlocks.POTTED_RED_CAMPION.get())
                .add(ModBlocks.POTTED_CALENDULA.get())
                .add(ModBlocks.POTTED_NIGELLA_DAMASCENA.get());
    }
}