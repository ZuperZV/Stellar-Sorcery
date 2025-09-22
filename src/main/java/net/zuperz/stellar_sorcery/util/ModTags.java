package net.zuperz.stellar_sorcery.util;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.zuperz.stellar_sorcery.StellarSorcery;

public class ModTags {

    public static class Items {

        public static final TagKey<Item> STELLAR_SORCERY_FLOWER_ITEMS = tag("stellar_sorcery_flower_items");
        public static final TagKey<Item> CHALK_STICK_ITEMS = tag("chalk_stick_blocks");

        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, name));
        }

        private static TagKey<Item> moddedTag(String modid, String name) {
            return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(modid, name));
        }
    }

    public static class Blocks {

        public static final TagKey<Block> STELLER_SORCERY_FLOWERS_BLOCKS = tag("steller_sorcery_flowers_blocks");
        public static final TagKey<Block> CHALK_BLOCKS = tag("chalk_blocks");


        private static TagKey<Block> tag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, name));
        }

        private static TagKey<Block> forgeTag(String name) {
            return BlockTags.create(ResourceLocation.fromNamespaceAndPath("forge", name));
        }
    }

    public static class Fluids {

        public static final TagKey<Fluid> NOCTILUME = tag("noctilume");

        private static TagKey<Fluid> tag(String name) {
            return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, name));
        }

        private static TagKey<Fluid> moddedTag(String modid, String name) {
            return TagKey.create(Registries.FLUID, ResourceLocation.fromNamespaceAndPath(modid, name));
        }
    }

    public static class Entities {

        //public static final TagKey<EntityType<?>> ARMADILLO_HIVE_INHABITORS = tag("armadillo_hive_inhabitors");

        private static TagKey<EntityType<?>> tag(String name) {
            return TagKey.create(Registries.ENTITY_TYPE, ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, name));
        }
    }
}
