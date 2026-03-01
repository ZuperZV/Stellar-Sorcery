package net.zuperz.stellar_sorcery.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;

public class GlowingBlockEntity extends BlockEntity {
    public GlowingBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GLOWING_BLOCK_BE.get(), pos, state);
    }
}
