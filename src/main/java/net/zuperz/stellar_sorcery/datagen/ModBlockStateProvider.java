package net.zuperz.stellar_sorcery.datagen;

import com.google.gson.JsonElement;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.models.blockstates.*;
import net.minecraft.data.models.model.ModelTemplates;
import net.minecraft.data.models.model.TextureMapping;
import net.minecraft.data.models.model.TexturedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.block.custom.FritillariaMeleagrisCropBlock;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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

        blockWithItem(ModBlocks.BUDDING_MOONSHINE);

        createAmethystCluster(ModBlocks.MOONSHINE_SMALL_BUD.get());
        createAmethystCluster(ModBlocks.MOONSHINE_MEDIUM_BUD.get());
        createAmethystCluster(ModBlocks.MOONSHINE_LARGE_BUD.get());
        createAmethystCluster(ModBlocks.MOONSHINE_CLUSTER.get());
    }

    private void createAmethystCluster(Block block) {
        ModelFile model = models().cross(name(block), blockTexture(block)).renderType("cutout");

        getVariantBuilder(block).forAllStates(state -> {
            Direction facing = state.getValue(BlockStateProperties.FACING);
            int xRot = switch (facing) {
                case DOWN -> 180;
                case UP -> 0;
                default -> 90;
            };
            int yRot = switch (facing) {
                case NORTH -> 0;
                case SOUTH -> 180;
                case WEST -> 270;
                case EAST -> 90;
                default -> 0;
            };

            return ConfiguredModel.builder()
                    .modelFile(model)
                    .rotationX(xRot)
                    .rotationY(yRot)
                    .build();
        });
    }

    private String name(Block block) {
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
        return id.getPath();
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