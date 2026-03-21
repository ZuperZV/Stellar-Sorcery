package net.zuperz.stellar_sorcery.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import net.zuperz.stellar_sorcery.block.entity.custom.SmartSpawnerBlockEntity;

import javax.annotation.Nullable;
import java.util.function.ToIntFunction;

public class SmartSpawnerBlock extends SpawnerBlock {

    public static final BooleanProperty ACTIVATED = BooleanProperty.create("activated");

    public SmartSpawnerBlock(Properties props) {
        super(props);
        this.registerDefaultState(this.stateDefinition.any().setValue(ACTIVATED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVATED);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos p_154687_, BlockState p_154688_) {
        return new SmartSpawnerBlockEntity(p_154687_, p_154688_);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type == ModBlockEntities.MOB_SMART_SPAWNER_BE.get()) {
            return level.isClientSide
                    ? (lvl, pos, st, be) -> SmartSpawnerBlockEntity.clientTick(lvl, pos, st, (SmartSpawnerBlockEntity) be)
                    : (lvl, pos, st, be) -> SmartSpawnerBlockEntity.serverTick(lvl, pos, st, (SmartSpawnerBlockEntity) be);
        }
        return null;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_55659_) {
        return this.defaultBlockState().setValue(ACTIVATED, Boolean.valueOf(p_55659_.getLevel().hasNeighborSignal(p_55659_.getClickedPos())));
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (!level.isClientSide) {

            boolean hasPower = level.hasNeighborSignal(pos);
            boolean current = state.getValue(ACTIVATED);

            if (current != hasPower) {
                level.setBlock(pos, state.setValue(ACTIVATED, hasPower), 3);
            }
        }
    }
}