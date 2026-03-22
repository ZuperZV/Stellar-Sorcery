package net.zuperz.stellar_sorcery.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.zuperz.stellar_sorcery.item.ModItems;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ModChestLootTableProvider extends LootTableProvider {

    public ModChestLootTableProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(
                output,
                Set.of(),
                List.of(
                        new SubProviderEntry(ModChestLootTableSubProvider::new, LootTable.DEFAULT_PARAM_SET)
                ),
                registries
        );
    }

    public static class ModChestLootTableSubProvider implements LootTableSubProvider {

        private final HolderLookup.Provider registries;

        public ModChestLootTableSubProvider(HolderLookup.Provider registries) {
            this.registries = registries;
        }

        @Override
        public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> consumer) {

            ResourceKey<LootTable> stumpKey = ResourceKey.create(Registries.LOOT_TABLE,
                     ResourceLocation.fromNamespaceAndPath("stellar_sorcery", "chests/vital_stump"));

            LootPool.Builder stumpPool = new LootPool.Builder()
                    .setRolls(UniformGenerator.between(1, 1))
                    .add(LootItem.lootTableItem(ModItems.SMART_UPGRADE_TEMPLATE))
                    .add(LootItem.lootTableItem(ModItems.TWIG_CLAY_JAR))
                    .add(LootItem.lootTableItem(ModItems.EXTRACTER_CLAY_JAR));

            LootTable.Builder stumpTable = LootTable.lootTable()
                    .withPool(stumpPool);

            consumer.accept(stumpKey, stumpTable);
        }
    }
}