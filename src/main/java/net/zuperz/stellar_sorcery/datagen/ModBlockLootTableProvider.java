package net.zuperz.stellar_sorcery.datagen;

import net.minecraft.advancements.critereon.StatePropertiesPredicate;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.ApplyBonusCount;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemBlockStatePropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.block.custom.FritillariaMeleagrisCropBlock;
import net.zuperz.stellar_sorcery.item.ModItems;

import java.util.Set;

public class ModBlockLootTableProvider extends BlockLootSubProvider {
    protected ModBlockLootTableProvider(HolderLookup.Provider provider) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), provider);
    }

    @Override
    protected void generate() {
        dropSelf(ModBlocks.ASTRAL_ALTAR.get());
        dropSelf(ModBlocks.ASTRAL_NEXUS.get());
        dropSelf(ModBlocks.VITAL_STUMP.get());
        dropSelf(ModBlocks.STUMP.get());
        dropSelf(ModBlocks.ESSENCE_BOILER.get());
        dropSelf(ModBlocks.ARCFORGE.get());
        dropSelf(ModBlocks.STAR_LIGHT.get());

        LootItemCondition.Builder lootItemConditionBuilder = LootItemBlockStatePropertyCondition.hasBlockStateProperties(ModBlocks.FRITILLARIA_MELEAGRIS_CROP.get())
                .setProperties(StatePropertiesPredicate.Builder.properties().hasProperty(FritillariaMeleagrisCropBlock.AGE, 5));
        this.add(ModBlocks.FRITILLARIA_MELEAGRIS_CROP.get(), this.createCropDrops(ModBlocks.FRITILLARIA_MELEAGRIS_CROP.get(),
                ModItems.FRITILLARIA_MELEAGRIS.get(), ModItems.FRITILLARIA_MELEAGRIS_SEEDS.asItem(), lootItemConditionBuilder));

        this.dropSelf(ModBlocks.RED_CAMPION.get());
        this.add(ModBlocks.POTTED_RED_CAMPION.get(), createPotFlowerItemTable(ModBlocks.RED_CAMPION));

        this.dropSelf(ModBlocks.CALENDULA.get());
        this.add(ModBlocks.POTTED_CALENDULA.get(), createPotFlowerItemTable(ModBlocks.CALENDULA));

        this.dropSelf(ModBlocks.NIGELLA_DAMASCENA.get());
        this.add(ModBlocks.POTTED_NIGELLA_DAMASCENA.get(), createPotFlowerItemTable(ModBlocks.NIGELLA_DAMASCENA));
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