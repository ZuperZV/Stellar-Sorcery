package net.zuperz.stellar_sorcery.fluid;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidInteractionRegistry;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.item.ModItems;

import java.util.function.Supplier;

public class ModFluids {
    public static final DeferredRegister<Fluid> FLUIDS =
            DeferredRegister.create(BuiltInRegistries.FLUID, StellarSorcery.MOD_ID);

    public static final Supplier<FlowingFluid> SOURCE_NOCTILUME = FLUIDS.register("source_noctilume",
            () -> new BaseFlowingFluid.Source(ModFluids.NOCTILUME_PROPERTIES));
    public static final Supplier<FlowingFluid> FLOWING_NOCTILUME = FLUIDS.register("flowing_noctilume",
            () -> new BaseFlowingFluid.Flowing(ModFluids.NOCTILUME_PROPERTIES));

    public static final DeferredBlock<LiquidBlock> NOCTILUME_BLOCK = ModBlocks.BLOCKS.register("noctilume_block",
            () -> new LiquidBlock(ModFluids.SOURCE_NOCTILUME.get(), BlockBehaviour.Properties.ofFullCopy(Blocks.WATER).noLootTable()));
    public static final DeferredItem<Item> NOCTILUME_BUCKET = ModItems.ITEMS.registerItem("noctilume_bucket",
            properties -> new BucketItem(ModFluids.SOURCE_NOCTILUME.get(), properties.craftRemainder(Items.BUCKET).stacksTo(1)));

    public static final BaseFlowingFluid.Properties NOCTILUME_PROPERTIES = new BaseFlowingFluid.Properties(
            ModFluidTypes.NOCTILUME_FLUID_TYPE, SOURCE_NOCTILUME, FLOWING_NOCTILUME)
            .slopeFindDistance(2).levelDecreasePerBlock(1)
            .block(ModFluids.NOCTILUME_BLOCK).bucket(ModFluids.NOCTILUME_BUCKET);

    public static void registerFluidInteractions() {
        FluidInteractionRegistry.addInteraction(NeoForgeMod.LAVA_TYPE.value(), new FluidInteractionRegistry.InteractionInformation(
                SOURCE_NOCTILUME.get().getFluidType(),
                fluidState -> {
                    if (fluidState.isSource()) {
                        return Blocks.SANDSTONE.defaultBlockState();
                    } else {
                        return Blocks.SAND.defaultBlockState();
                    }
                }));

        FluidInteractionRegistry.addInteraction(NeoForgeMod.WATER_TYPE.value(), new FluidInteractionRegistry.InteractionInformation(
                SOURCE_NOCTILUME.get().getFluidType(),
                fluidState -> {
                    if (fluidState.isSource()) {
                        return Blocks.ANDESITE.defaultBlockState();
                    } else {
                        return Blocks.GRAVEL.defaultBlockState();
                    }
                }));
    }


    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
    }
}