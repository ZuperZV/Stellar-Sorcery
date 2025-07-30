package net.zuperz.stellar_sorcery.datagen;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.block.custom.FritillariaMeleagrisCropBlock;

import java.util.function.Function;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, StellarSorcery.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        makeCrop(((FritillariaMeleagrisCropBlock) ModBlocks.FRITILLARIA_MELEAGRIS_CROP.get()), "fritillaria_meleagris_stage","fritillaria_meleagris_stage");

        flowerWithPot(ModBlocks.RED_CAMPION, ModBlocks.POTTED_RED_CAMPION, "red_campion", "flower_pot_cross");
        flowerWithPot(ModBlocks.CALENDULA, ModBlocks.POTTED_CALENDULA, "calendula", "flower_pot_cross");
        flowerWithPot(ModBlocks.NIGELLA_DAMASCENA, ModBlocks.POTTED_NIGELLA_DAMASCENA, "nigella_damascena", "flower_pot_cross");
    }

    private void blockWithItem(DeferredBlock<Block> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    private void blockItem(DeferredBlock<Block> deferredBlock) {
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("stellar_sorcery:block/" + deferredBlock.getId().getPath()));
    }

    private void blockItem(DeferredBlock<Block> deferredBlock, String appendix) {
        simpleBlockItem(deferredBlock.get(), new ModelFile.UncheckedModelFile("stellar_sorcery:block/" + deferredBlock.getId().getPath() + appendix));
    }

    private void leavesBlock(DeferredBlock<Block> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(),
                models().singleTexture(BuiltInRegistries.BLOCK.getKey(deferredBlock.get()).getPath(), ResourceLocation.parse("minecraft:block/leaves"),
                        "all", blockTexture(deferredBlock.get())).renderType("cutout"));
    }

    private void saplingBlock(DeferredBlock<Block> deferredBlock) {
        simpleBlock(deferredBlock.get(), models().cross(BuiltInRegistries.BLOCK.getKey(deferredBlock.get()).getPath(), blockTexture(deferredBlock.get())).renderType("cutout"));
    }

    public void makeCrop(CropBlock block, String modelName, String textureName) {
        Function<BlockState, ConfiguredModel[]> function = state -> states(state, block, modelName, textureName);

        getVariantBuilder(block).forAllStates(function);
    }

    private ConfiguredModel[] states(BlockState state, CropBlock block, String modelName, String textureName) {
        ConfiguredModel[] models = new ConfiguredModel[1];
        models[0] = new ConfiguredModel(models().crop(modelName + state.getValue(((FritillariaMeleagrisCropBlock) block).getAgeProperty()),
                ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "block/" + textureName +
                        state.getValue(((FritillariaMeleagrisCropBlock) block).getAgeProperty()))).renderType("cutout"));

        return models;
    }

    private void flowerWithPot(DeferredBlock<Block> flowerBlock, DeferredBlock<Block> pottedBlock, String name, String flowerPotShape) {
        simpleBlock(flowerBlock.get(),
                models().cross(blockTexture(flowerBlock.get()).getPath(), blockTexture(flowerBlock.get()))
                        .renderType("cutout"));

        simpleBlock(pottedBlock.get(),
                models().singleTexture("potted_" + name,
                                ResourceLocation.parse(flowerPotShape),
                                "plant",
                                blockTexture(flowerBlock.get()))
                        .renderType("cutout"));
    }
}