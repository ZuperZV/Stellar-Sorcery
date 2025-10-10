package net.zuperz.stellar_sorcery.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.item.ModItems;
import net.zuperz.stellar_sorcery.util.ModTags;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider,
                              CompletableFuture<TagLookup<Block>> pBlockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, StellarSorcery.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        this.tag(ItemTags.DYEABLE)
                .add(ModItems.CODEX_ARCANUM.get());

        this.tag(Tags.Items.TOOLS_BRUSH)
                .add(Items.BRUSH);

        this.tag(Tags.Items.MELEE_WEAPON_TOOLS)
                .add(ModItems.CELESTIAL_BLADE.get());

        this.tag(ItemTags.VILLAGER_PLANTABLE_SEEDS)
                .add(ModItems.FRITILLARIA_MELEAGRIS_SEEDS.get());

        this.tag(Tags.Items.SEEDS)
                .add(ModItems.FRITILLARIA_MELEAGRIS_SEEDS.get());

        this.tag(Tags.Items.FOODS_FOOD_POISONING)
                .add(ModItems.FRITILLARIA_MELEAGRIS.get());

        this.tag(ModTags.Items.STELLAR_SORCERY_FLOWER_ITEMS)
                .add(ModBlocks.RED_CAMPION.get().asItem())
                .add(ModBlocks.CALENDULA.get().asItem())
                .add(ModBlocks.NIGELLA_DAMASCENA.get().asItem());

        this.tag(ModTags.Items.CHALK_STICK_ITEMS)
                .add(ModItems.WHITE_CHALK_STICK.get());
    }
}