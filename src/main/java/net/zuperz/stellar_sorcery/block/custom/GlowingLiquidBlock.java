package net.zuperz.stellar_sorcery.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.zuperz.stellar_sorcery.block.entity.custom.GlowingBlockEntity;
import net.zuperz.stellar_sorcery.block.light.IGlowingBlock;

import javax.annotation.Nullable;

public class GlowingLiquidBlock extends LiquidBlock implements EntityBlock, IGlowingBlock {
    public GlowingLiquidBlock(FlowingFluid fluid, Properties properties) {
        super(fluid, properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GlowingBlockEntity(pos, state);
    }
}
