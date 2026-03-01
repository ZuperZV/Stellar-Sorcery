package net.zuperz.stellar_sorcery.datagen;

import net.minecraft.advancements.critereon.ItemPredicate;
import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.entries.LootPoolEntryContainer;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.predicates.MatchTool;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.block.custom.FritillariaMeleagrisCropBlock;
import net.zuperz.stellar_sorcery.block.custom.SoulBloomCropBlock;
import net.zuperz.stellar_sorcery.item.ModItems;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    protected ModBlockLootTableProvider(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);

        dropSelf(ModBlocks.ASTRAL_ALTAR.get());
        dropSelf(ModBlocks.ASTRAL_NEXUS.get());
        dropSelf(ModBlocks.VITAL_STUMP.get());
        dropSelf(ModBlocks.STUMP.get());
        dropSelf(ModBlocks.SOUL_CANDLE.get());
        dropSelf(ModBlocks.ESSENCE_BOILER.get());
        dropSelf(ModBlocks.ARCFORGE.get());
        dropSelf(ModBlocks.LUNAR_INFUSER.get());
        dropSelf(ModBlocks.LUNAR_JAR.get());
        dropSelf(ModBlocks.LUNAR_LIGHT_BEAM_EMITTER.get());
        dropSelf(ModBlocks.ITEM_EMITTER.get());
        dropSelf(ModBlocks.LIGHT_INFUSER.get());
        dropSelf(ModBlocks.LIGHT_JAR.get());
        dropSelf(ModBlocks.LIGHT_BEAM_EMITTER.get());

        dropSelf(ModBlocks.AUGMENT_FORGE.get());
        dropSelf(ModBlocks.ELDRITE.get());
        dropSelf(ModBlocks.MOON_SHARD_ELDRITE.get());

        dropSelf(ModBlocks.GRIMROCK.get());
        dropSelf(ModBlocks.GLOOM_MOSS_BLOCK.get());
        dropSelf(ModBlocks.GLOOM_MOSS_CARPET.get());
        dropSelf(ModBlocks.ECHO_THORN.get());
        dropSelf(ModBlocks.ECHO_THORN_FLOWER.get());

        dropSelf(ModBlocks.DRIFTSOIL.get());
        this.add(ModBlocks.TILLED_DRIFTSOIL.get(), createPotFlowerItemTable(ModBlocks.DRIFTSOIL));

        this.add(ModBlocks.WHITE_CHALK.get(), noDrop());

        LootItemCondition.Builder fritillariaMeleagrisLootItemConditionBuilder = LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.FRITILLARIA_MELEAGRIS_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(FritillariaMeleagrisCropBlock.AGE, 5));
        this.add(ModBlocks.FRITILLARIA_MELEAGRIS_CROP.get(), this.createCropDrops(ModBlocks.FRITILLARIA_MELEAGRIS_CROP.get(),
                ModItems.FRITILLARIA_MELEAGRIS.get(), ModItems.FRITILLARIA_MELEAGRIS_SEEDS.asItem(), fritillariaMeleagrisLootItemConditionBuilder));

        LootItemCondition.Builder soulBloomLootItemConditionBuilder = LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.SOUL_BLOOM_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(SoulBloomCropBlock.AGE, 6));
        this.add(ModBlocks.SOUL_BLOOM_CROP.get(), this.createCropDrops(ModBlocks.SOUL_BLOOM_CROP.get(),
                ModItems.FRITILLARIA_MELEAGRIS.get(), ModItems.FRITILLARIA_MELEAGRIS_SEEDS.asItem(), soulBloomLootItemConditionBuilder));

        this.dropSelf(ModBlocks.RED_CAMPION.get());
        this.add(ModBlocks.POTTED_RED_CAMPION.get(), createPotFlowerItemTable(ModBlocks.RED_CAMPION));

        this.dropSelf(ModBlocks.CALENDULA.get());
        this.add(ModBlocks.POTTED_CALENDULA.get(), createPotFlowerItemTable(ModBlocks.CALENDULA));

        this.dropSelf(ModBlocks.NIGELLA_DAMASCENA.get());
        this.add(ModBlocks.POTTED_NIGELLA_DAMASCENA.get(), createPotFlowerItemTable(ModBlocks.NIGELLA_DAMASCENA));

        this.add(
                ModBlocks.MOONSHINE_CLUSTER.get(),
                p_344203_ -> this.createSilkTouchDispatchTable(
                        p_344203_,
                        LootItem.lootTableItem(ModItems.MOONSHINE_SHARD)
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(4.0F)))
                                .apply(ApplyBonusCount.addOreBonusCount(registrylookup.getOrThrow(Enchantments.FORTUNE)))
                                .when(MatchTool.toolMatches(ItemPredicate.Builder.item().of(ItemTags.CLUSTER_MAX_HARVESTABLES)))
                                .otherwise(
                                        (LootPoolEntryContainer.Builder<?>)this.applyExplosionDecay(
                                                p_344203_, LootItem.lootTableItem(ModItems.MOONSHINE_SHARD).apply(SetItemCountFunction.setCount(ConstantValue.exactly(2.0F)))
                                        )
                                )
                )
        );

        this.dropWhenSilkTouch(ModBlocks.MOONSHINE_SMALL_BUD.get());
        this.dropWhenSilkTouch(ModBlocks.MOONSHINE_MEDIUM_BUD.get());
        this.dropWhenSilkTouch(ModBlocks.MOONSHINE_LARGE_BUD.get());

        this.add(ModBlocks.BUDDING_MOONSHINE.get(), noDrop());
        this.add(ModBlocks.STAR_LIGHT.get(), noDrop());
    }


    protected LootTable.Builder createMultipleOreDrops(Block pBlock, Item item, float minDrops, float maxDrops) {
        HolderLookup.RegistryLookup<Enchantment> registrylookup = this.registries.lookupOrThrow(Registries.ENCHANTMENT);
        return this.createSilkTouchDispatchTable(pBlock, this.applyExplosionDecay(pBlock,
                LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(UniformGenerator.between(minDrops, maxDrops)))
                        .apply(ApplyBonusCount.addOreBonusCount(registrylookup.getOrThrow(Enchantments.FORTUNE)))));
    }

    //this.add(ModBlocks.CHROMIUM_ORE.get(),
    //        block -> createMultipleOreDrops(ModBlocks.CHROMIUM_ORE.get(), ModItems.RAW_CHROMIUM.get(), 1, 3));

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream().map(Holder::value)::iterator;
    }
}