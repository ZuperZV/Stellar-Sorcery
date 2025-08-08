package net.zuperz.stellar_sorcery.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.zuperz.stellar_sorcery.block.ModBlocks;

public class MoonshineBuddingBlock extends AmethystBlock {
    public static final MapCodec<BuddingAmethystBlock> CODEC = simpleCodec(BuddingAmethystBlock::new);
    public static final int GROWTH_CHANCE = 5;
    private static final Direction[] DIRECTIONS = Direction.values();

    @Override
    public MapCodec<BuddingAmethystBlock> codec() {
        return CODEC;
    }

    public MoonshineBuddingBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        if (randomSource.nextInt(5) == 0) {
            Direction direction = DIRECTIONS[randomSource.nextInt(DIRECTIONS.length)];
            BlockPos blockpos = pos.relative(direction);
            BlockState blockstate = level.getBlockState(blockpos);
            Block block = null;
            if (canClusterGrowAtState(blockstate)) {
                block = ModBlocks.MOONSHINE_SMALL_BUD.get();
            } else if (blockstate.is(ModBlocks.MOONSHINE_SMALL_BUD) && blockstate.getValue(AmethystClusterBlock.FACING) == direction) {
                block = ModBlocks.MOONSHINE_MEDIUM_BUD.get();
            } else if (blockstate.is(ModBlocks.MOONSHINE_MEDIUM_BUD) && blockstate.getValue(AmethystClusterBlock.FACING) == direction) {
                block = ModBlocks.MOONSHINE_LARGE_BUD.get();
            } else if (blockstate.is(ModBlocks.MOONSHINE_LARGE_BUD) && blockstate.getValue(AmethystClusterBlock.FACING) == direction) {
                block = ModBlocks.MOONSHINE_CLUSTER.get();
            }

            if (block != null) {
                BlockState blockstate1 = block.defaultBlockState()
                        .setValue(AmethystClusterBlock.FACING, direction)
                        .setValue(AmethystClusterBlock.WATERLOGGED, Boolean.valueOf(blockstate.getFluidState().getType() == Fluids.WATER));
                level.setBlockAndUpdate(blockpos, blockstate1);
            }
        }
    }

    public static boolean canClusterGrowAtState(BlockState p_152735_) {
        return p_152735_.isAir() || p_152735_.is(Blocks.WATER) && p_152735_.getFluidState().getAmount() == 8;
    }
}
