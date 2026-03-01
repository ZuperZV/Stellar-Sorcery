package net.zuperz.stellar_sorcery.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class TilledSoilBlock extends FarmBlock {
    private final BlockState fallbackBlock;
    private final VoxelShape shape;

    public TilledSoilBlock(Properties properties, BlockState fallbackBlock, float yOffset) {
        super(properties);
        this.fallbackBlock = fallbackBlock;
        this.shape = Block.box(0.0, 0.0, 0.0, 16.0, 16.0 + yOffset, 16.0);
    }

    @Override
    protected VoxelShape getShape(BlockState p_53290_, BlockGetter p_53291_, BlockPos p_53292_, CollisionContext p_53293_) {
        return this.shape;
    }

    @Override
    protected void tick(BlockState p_221134_, ServerLevel p_221135_, BlockPos p_221136_, RandomSource p_221137_) {
        if (!p_221134_.canSurvive(p_221135_, p_221136_)) {
            this.convertToFallbackBlock(null, p_221134_, p_221135_, p_221136_);
        }
    }

    @Override
    protected void randomTick(BlockState p_221139_, ServerLevel p_221140_, BlockPos p_221141_, RandomSource p_221142_) {
        int i = p_221139_.getValue(MOISTURE);
        if (!isNearWater(p_221140_, p_221141_) && !p_221140_.isRainingAt(p_221141_.above())) {
            if (i > 0) {
                p_221140_.setBlock(p_221141_, p_221139_.setValue(MOISTURE, Integer.valueOf(i - 1)), 2);
            } else if (!shouldMaintainFarmland(p_221140_, p_221141_)) {
                this.convertToFallbackBlock(null, p_221139_, p_221140_, p_221141_);
            }
        } else if (i < 7) {
            p_221140_.setBlock(p_221141_, p_221139_.setValue(MOISTURE, Integer.valueOf(7)), 2);
        }
    }

    @Override
    public void fallOn(Level p_153227_, BlockState p_153228_, BlockPos p_153229_, Entity p_153230_, float p_153231_) {
        if (!p_153227_.isClientSide
                && net.neoforged.neoforge.common.CommonHooks.onFarmlandTrample(p_153227_, p_153229_, Blocks.DIRT.defaultBlockState(), p_153231_, p_153230_)) { // Forge: Move logic to Entity#canTrample
            this.convertToFallbackBlock(p_153230_, p_153228_, p_153227_, p_153229_);
        }

        super.fallOn(p_153227_, p_153228_, p_153229_, p_153230_, p_153231_);
    }

    private void convertToFallbackBlock(@Nullable Entity p_270981_, BlockState p_270402_, Level p_270568_, BlockPos p_270551_) {
        BlockState blockstate = pushEntitiesUp(p_270402_, this.fallbackBlock, p_270568_, p_270551_);
        p_270568_.setBlockAndUpdate(p_270551_, blockstate);
        p_270568_.gameEvent(GameEvent.BLOCK_CHANGE, p_270551_, GameEvent.Context.of(p_270981_, blockstate));
    }

    private static boolean shouldMaintainFarmland(BlockGetter p_279219_, BlockPos p_279209_) {
        return p_279219_.getBlockState(p_279209_.above()).is(BlockTags.MAINTAINS_FARMLAND);
    }

    private static boolean isNearWater(LevelReader p_53259_, BlockPos p_53260_) {
        BlockState state = p_53259_.getBlockState(p_53260_);
        for (BlockPos blockpos : BlockPos.betweenClosed(p_53260_.offset(-4, 0, -4), p_53260_.offset(4, 1, 4))) {
            if (state.canBeHydrated(p_53259_, p_53260_, p_53259_.getFluidState(blockpos), blockpos)) {
                return true;
            }
        }

        return net.neoforged.neoforge.common.FarmlandWaterManager.hasBlockWaterTicket(p_53259_, p_53260_);
    }
}
