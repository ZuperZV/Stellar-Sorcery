package net.zuperz.stellar_sorcery.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.zuperz.stellar_sorcery.util.FyldeTilstand;

public class EldriteBlock extends Block {
    public static final EnumProperty<FyldeTilstand> FYLDE = EnumProperty.create("fylde", FyldeTilstand.class);

    public EldriteBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FYLDE, FyldeTilstand.EMPTY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FYLDE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FYLDE, FyldeTilstand.EMPTY);
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        System.out.println("Tick test for " + pos);

        if (hasValidBlockAbove(level, pos)) {
            if (random.nextFloat() < 0.68688889F) {
                addFylde(level, pos, blockState);
            }
        } else {
            System.out.println("Ingen fyldt Eldrite over " + pos);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState blockState) {
        return blockState.getValue(FYLDE) != FyldeTilstand.FULL;
    }

    private boolean hasValidBlockAbove(ServerLevel level, BlockPos pos) {
        BlockPos above1 = pos.above(1);
        BlockPos above2 = pos.above(2);

        BlockState state1 = level.getBlockState(above1);
        if (state1.getBlock() instanceof EldriteBlock eld1) {
            FyldeTilstand fylde1 = state1.getValue(FYLDE);
            if (fylde1 == FyldeTilstand.HALF || fylde1 == FyldeTilstand.FULL) {
                return true;
            }
        }

        BlockState state2 = level.getBlockState(above2);
        if (state2.getBlock() instanceof EldriteBlock eld2) {
            FyldeTilstand fylde2 = state2.getValue(FYLDE);
            if (fylde2 == FyldeTilstand.FULL) {
                return true;
            }
        }

        return false;
    }

    public void addFylde(ServerLevel level, BlockPos pos, BlockState blockState) {
        FyldeTilstand current = blockState.getValue(FYLDE);
        FyldeTilstand newFylde = current;

        if (current == FyldeTilstand.EMPTY) {
            newFylde = FyldeTilstand.HALF;
        } else if (current == FyldeTilstand.HALF) {
            newFylde = FyldeTilstand.FULL;
        }

        if (newFylde != current) {
            BlockState newState = blockState.setValue(FYLDE, newFylde);
            level.setBlock(pos, newState, 3);
            System.out.println("EldriteBlock ved " + pos + " blev opgraderet til " + newFylde);
        }
    }
}