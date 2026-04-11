package net.zuperz.stellar_sorcery.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.zuperz.stellar_sorcery.util.FyldeEnum;

public class EldriteBlock extends Block {
    public static final EnumProperty<FyldeEnum> FYLDE = EnumProperty.create("fylde", FyldeEnum.class);
    public static final EnumProperty<FyldeEnum> BEDROCK_FYLDE = EnumProperty.create("bedrock_fylde", FyldeEnum.class);

    public EldriteBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FYLDE, FyldeEnum.EMPTY).setValue(BEDROCK_FYLDE, FyldeEnum.EMPTY));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FYLDE, BEDROCK_FYLDE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FYLDE, FyldeEnum.EMPTY).setValue(BEDROCK_FYLDE, FyldeEnum.EMPTY);
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
        return blockState.getValue(FYLDE) != FyldeEnum.FULL
                || blockState.getValue(BEDROCK_FYLDE) != FyldeEnum.FULL;
    }

    private boolean hasValidBlockAbove(ServerLevel level, BlockPos pos) {
        BlockPos above1 = pos.above(1);
        BlockPos above2 = pos.above(2);

        BlockState state1 = level.getBlockState(above1);
        if (state1.getBlock() instanceof EldriteBlock eld1) {
            FyldeEnum fylde1 = state1.getValue(FYLDE);
            if (fylde1 == FyldeEnum.HALF || fylde1 == FyldeEnum.FULL) {
                return true;
            }
        }

        BlockState state2 = level.getBlockState(above2);
        if (state2.getBlock() instanceof EldriteBlock eld2) {
            FyldeEnum fylde2 = state2.getValue(FYLDE);
            if (fylde2 == FyldeEnum.FULL) {
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
            FyldeEnum fylde = belowState.getValue(BEDROCK_FYLDE);
            if (fylde == FyldeEnum.HALF || fylde == FyldeEnum.FULL) {
                return true;
            }
        }

        return false;
    }

    public void addFylde(ServerLevel level, BlockPos pos, BlockState blockState) {
        FyldeEnum current = blockState.getValue(FYLDE);
        FyldeEnum newFylde = current;

        if (current == FyldeEnum.EMPTY) {
            newFylde = FyldeEnum.HALF;
        } else if (current == FyldeEnum.HALF) {
            newFylde = FyldeEnum.FULL;
        }

        if (newFylde != current) {
            BlockState newState = blockState.setValue(FYLDE, newFylde);
            level.setBlock(pos, newState, 3);
            System.out.println("EldriteBlock ved " + pos + " blev opgraderet til " + newFylde);
        }
    }

    public void addBedrockFylde(ServerLevel level, BlockPos pos, BlockState blockState) {
        FyldeEnum current = blockState.getValue(BEDROCK_FYLDE);
        FyldeEnum newFylde = nextFylde(current);

        if (newFylde != current) {
            BlockState newState = blockState.setValue(BEDROCK_FYLDE, newFylde);
            level.setBlock(pos, newState, 3);
            System.out.println("EldriteBlock ved " + pos + " blev opgraderet til BEDROCK_FYLDE = " + newFylde);
        }
    }

    private FyldeEnum nextFylde(FyldeEnum current) {
        if (current == FyldeEnum.EMPTY) return FyldeEnum.HALF;
        if (current == FyldeEnum.HALF) return FyldeEnum.FULL;
        return current;
    }
}