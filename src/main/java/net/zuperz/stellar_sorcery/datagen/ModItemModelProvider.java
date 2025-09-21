package net.zuperz.stellar_sorcery.datagen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.fluid.ModFluids;
import net.zuperz.stellar_sorcery.item.ModItems;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, StellarSorcery.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        basicItem(ModItems.FRITILLARIA_MELEAGRIS.get());
        basicItem(ModItems.FRITILLARIA_MELEAGRIS_SEEDS.get());

        basicItem(ModItems.ROOT.get());
        basicItem(ModItems.EMPTY_ESSENCE_BOTTLE.get());
        basicItem(ModItems.EMPTY_ESSENCE_AMULET.get());

        basicItem(ModItems.MOONSHINE_SHARD.get());
        basicItem(ModItems.MOONSHINE_CATALYST.get());

        basicItem(ModItems.SOFT_CLAY_JAR.get());
        basicItem(ModItems.CLAY_JAR.get());
        basicItem(ModItems.FIRE_CLAY_JAR.get());
        basicItem(ModItems.TWIG_CLAY_JAR.get());
        basicItem(ModItems.WIND_CLAY_JAR.get());
        basicItem(ModItems.WATER_CLAY_JAR.get());
        basicItem(ModItems.SHADOW_CLAY_JAR.get());
        basicItem(ModItems.STONE_CLAY_JAR.get());
        basicItem(ModItems.SUN_CLAY_JAR.get());
        basicItem(ModItems.FROST_CLAY_JAR.get());
        basicItem(ModItems.STORM_CLAY_JAR.get());
        basicItem(ModItems.EXTRACTER_CLAY_JAR.get());
        basicItem(ModItems.WHITE_CHALK_STICK.get());

        flowerItem(ModBlocks.RED_CAMPION);
        flowerItem(ModBlocks.CALENDULA);
        flowerItem(ModBlocks.NIGELLA_DAMASCENA);

        blockItem(ModBlocks.MOONSHINE_SMALL_BUD.get());
        blockItem(ModBlocks.MOONSHINE_MEDIUM_BUD.get());
        blockItem(ModBlocks.MOONSHINE_LARGE_BUD.get());
        blockItem(ModBlocks.MOONSHINE_CLUSTER.get());

        basicItem(ModFluids.NOCTILUME_BUCKET.get());

        basicItem(ModItems.BLUESTONE_DUST.get());

        handheldItem(ModItems.RITUAL_DAGGER);
    }

    private void blockItem(Block block) {
        String name = BuiltInRegistries.BLOCK.getKey(block).getPath();

        getBuilder(name)
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", modLoc("block/" + name));
    }

    public ItemModelBuilder basiclayerItem(String item, String layer2, String _ingot) {
        return getBuilder(item + "_" + layer2)
                .parent(new ModelFile.UncheckedModelFile("item/generated"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "item/" + _ingot))
                .texture("layer1", ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "item/" + layer2));
    }

    private ItemModelBuilder handheldItem(DeferredItem<Item> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/handheld")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "item/" + item.getId().getPath()));
    }

    private ItemModelBuilder saplingItem(DeferredBlock<Block> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID,"block/" + item.getId().getPath()));
    }
    
    public void buttonItem(DeferredBlock<Block> block, DeferredBlock<Block> baseBlock) {
        this.withExistingParent(block.getId().getPath(), mcLoc("block/button_inventory"))
                .texture("texture",  ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID,
                        "block/" + baseBlock.getId().getPath()));
    }

    public void fenceItem(DeferredBlock<Block> block, DeferredBlock<Block> baseBlock) {
        this.withExistingParent(block.getId().getPath(), mcLoc("block/fence_inventory"))
                .texture("texture",  ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID,
                        "block/" + baseBlock.getId().getPath()));
    }

    public void wallItem(DeferredBlock<Block> block, DeferredBlock<Block> baseBlock) {
        this.withExistingParent(block.getId().getPath(), mcLoc("block/wall_inventory"))
                .texture("wall",  ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID,
                        "block/" + baseBlock.getId().getPath()));
    }

    public void flowerItem(DeferredBlock<Block> block) {
        this.withExistingParent(block.getId().getPath(), mcLoc("item/generated"))
                .texture("layer0",  ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID,
                        "block/" + block.getId().getPath()));
    }
}