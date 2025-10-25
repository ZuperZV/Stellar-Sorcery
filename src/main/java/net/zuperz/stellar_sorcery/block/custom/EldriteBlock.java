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
    public static final EnumProperty<FyldeTilstand> BEDROCK_FYLDE = EnumProperty.create("bedrock_fylde", FyldeTilstand.class);

    public EldriteBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FYLDE, FyldeTilstand.EMPTY).setValue(BEDROCK_FYLDE, FyldeTilstand.EMPTY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FYLDE, BEDROCK_FYLDE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FYLDE, FyldeTilstand.EMPTY).setValue(BEDROCK_FYLDE, FyldeTilstand.EMPTY);
    }

    @Override
    protected void randomTick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        System.out.println("Tick test for " + pos);

        boolean validAbove = hasValidBlockAbove(level, pos);
        boolean validBelow = hasValidBlockBelow(level, pos);

        if (validAbove && random.nextFloat() < 0.68688889F) {
            addFylde(level, pos, blockState);
        } else if (validBelow && random.nextFloat() < 0.68688889F) {
            addBedrockFylde(level, pos, blockState);
        } else {
            System.out.println("Ingen fyldt Eldrite over eller under " + pos);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState blockState) {
        return blockState.getValue(FYLDE) != FyldeTilstand.FULL
                || blockState.getValue(BEDROCK_FYLDE) != FyldeTilstand.FULL;
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

    private boolean hasValidBlockBelow(ServerLevel level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState belowState = level.getBlockState(below);

        if (belowState.is(net.minecraft.world.level.block.Blocks.BEDROCK)) {
            return true;
        }

        if (belowState.getBlock() instanceof EldriteBlock) {
            FyldeTilstand fylde = belowState.getValue(BEDROCK_FYLDE);
            if (fylde == FyldeTilstand.HALF || fylde == FyldeTilstand.FULL) {
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

    public void addBedrockFylde(ServerLevel level, BlockPos pos, BlockState blockState) {
        FyldeTilstand current = blockState.getValue(BEDROCK_FYLDE);
        FyldeTilstand newFylde = nextFylde(current);

        if (newFylde != current) {
            BlockState newState = blockState.setValue(BEDROCK_FYLDE, newFylde);
            level.setBlock(pos, newState, 3);
            System.out.println("EldriteBlock ved " + pos + " blev opgraderet til BEDROCK_FYLDE = " + newFylde);
        }
    }

    private FyldeTilstand nextFylde(FyldeTilstand current) {
        if (current == FyldeTilstand.EMPTY) return FyldeTilstand.HALF;
        if (current == FyldeTilstand.HALF) return FyldeTilstand.FULL;
        return current;
    }
}