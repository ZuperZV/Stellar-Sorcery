package net.zuperz.stellar_sorcery.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.zuperz.stellar_sorcery.block.ModBlocks;

// Credits:
// Glowing block rendering approach based on Legend of Steve by DeadlyDiamond (CC0 1.0).

public class EchoThornBlock extends CactusBlock {

    public EchoThornBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean canSurvive(BlockState p_51153_, LevelReader p_51154_, BlockPos p_51155_) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos sidePos = p_51155_.relative(direction);
            BlockState sideState = p_51154_.getBlockState(sidePos);

            boolean isGloomMossCarpet = sideState.is(ModBlocks.GLOOM_MOSS_CARPET);
            boolean isEchoThorn = sideState.is(ModBlocks.ECHO_THORN);
            boolean isEchoThornFlower = sideState.is(ModBlocks.ECHO_THORN_FLOWER);

            if ((sideState.isSolid() && !isGloomMossCarpet && !isEchoThorn && !isEchoThornFlower) ||
                    p_51154_.getFluidState(sidePos).is(FluidTags.LAVA)) {
                return false;
            }
        }

        BlockState blockstate1 = p_51154_.getBlockState(p_51155_.below());
        net.neoforged.neoforge.common.util.TriState soilDecision = blockstate1.canSustainPlant(p_51154_, p_51155_.below(), Direction.UP, p_51153_);
        if (!soilDecision.isDefault()) return soilDecision.isTrue();
        return (blockstate1.is(ModBlocks.ECHO_THORN) || blockstate1.is(ModBlocks.ECHO_THORN_FLOWER) || blockstate1.is(BlockTags.SAND) || blockstate1.is(ModBlocks.GLOOM_MOSS_BLOCK)) && !p_51154_.getBlockState(p_51155_.above()).liquid();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BlockPos abovePos = pos.above();

        if (level.isEmptyBlock(abovePos)) {

            int height = 1;
            while (level.getBlockState(pos.below(height)).is(this)) {
                height++;
            }

            int age = state.getValue(AGE);

            if (height < 2) {
                if (net.neoforged.neoforge.common.CommonHooks.canCropGrow(level, abovePos, state, true)) {
                    if (age == 15) {
                        level.setBlockAndUpdate(abovePos, this.defaultBlockState());
                        level.setBlock(pos, state.setValue(AGE, 0), 4);
                    } else {
                        level.setBlock(pos, state.setValue(AGE, age + 1), 4);
                    }
                    net.neoforged.neoforge.common.CommonHooks.fireCropGrowPost(level, pos, state);
                }
            }

            else if (height == 2) {
                if (random.nextFloat() < 0.5F) { // 50%
                    level.setBlockAndUpdate(abovePos, ModBlocks.ECHO_THORN_FLOWER.get().defaultBlockState());
                }
            }
        }
    }
}
