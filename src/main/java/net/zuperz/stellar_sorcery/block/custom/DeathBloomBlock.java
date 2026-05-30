package net.zuperz.stellar_sorcery.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.util.FyldeEnum;
import net.zuperz.stellar_sorcery.util.SunflowerEnum;

import javax.annotation.Nullable;

public class DeathBloomBlock extends Block implements BonemealableBlock {
    public static final EnumProperty<SunflowerEnum> PLACEMENT_STATE = EnumProperty.create("placement_state", SunflowerEnum.class);

    public DeathBloomBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(PLACEMENT_STATE, SunflowerEnum.GROUND_BOTTOM));
    }

    private static final VoxelShape SHAPE_GROUND_BUTTOM = Shapes.or(
            box(1, 0, 1, 14, 7, 14)
    );
    private static final VoxelShape SHAPE_BUTTOM = Shapes.or(
            box(3, 0, 3, 11, 16, 11)
    );
    private static final VoxelShape SHAPE_TOP = Shapes.or(
            box(1, 0, 1, 12, 15, 12)
    );

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        VoxelShape shape;

        switch (state.getValue(PLACEMENT_STATE)) {
            case TOP -> shape = SHAPE_TOP;
            case BOTTOM -> shape = SHAPE_BUTTOM;
            default -> shape = SHAPE_GROUND_BUTTOM;
        }

        Vec3 vec3 = state.getOffset(world, pos);
        return shape.move(vec3.x, vec3.y, vec3.z);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PLACEMENT_STATE);
    }

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelAccessor level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);

        if (belowState.getBlock() instanceof DeathBloomBlock) {

            if (belowState.getValue(PLACEMENT_STATE) == SunflowerEnum.GROUND_BOTTOM) {
                level.setBlock(belowPos,
                        belowState.setValue(PLACEMENT_STATE, SunflowerEnum.BOTTOM),
                        3);
            }

            return this.defaultBlockState().setValue(PLACEMENT_STATE, SunflowerEnum.TOP);
        }

        return this.defaultBlockState().setValue(PLACEMENT_STATE, SunflowerEnum.GROUND_BOTTOM);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        updateState(level, pos, state);
    }

    private void updateState(Level level, BlockPos pos, BlockState state) {
        BlockState above = level.getBlockState(pos.above());
        BlockState below = level.getBlockState(pos.below());

        boolean hasAbove = above.getBlock() instanceof DeathBloomBlock;
        boolean hasBelow = below.getBlock() instanceof DeathBloomBlock;

        if (hasAbove) {
            level.setBlock(pos, state.setValue(PLACEMENT_STATE, SunflowerEnum.BOTTOM), 3);
            return;
        }

        if (hasBelow) {

            SunflowerEnum belowState = below.getValue(PLACEMENT_STATE);

            if (belowState == SunflowerEnum.GROUND_BOTTOM) {
                level.setBlock(pos.below(),
                        below.setValue(PLACEMENT_STATE, SunflowerEnum.BOTTOM),
                        3);
            }

            level.setBlock(pos, state.setValue(PLACEMENT_STATE, SunflowerEnum.TOP), 3);
            return;
        }

        level.setBlock(pos, state.setValue(PLACEMENT_STATE, SunflowerEnum.GROUND_BOTTOM), 3);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {

        if (!(newState.getBlock() instanceof DeathBloomBlock)) {

            BlockPos above = pos.above();
            BlockState aboveState = level.getBlockState(above);

            if (aboveState.getBlock() instanceof DeathBloomBlock) {
                level.scheduleTick(above, aboveState.getBlock(), 1);
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());

        if (below.getBlock() instanceof DeathBloomBlock) {
            return true;
        }

        return below.isFaceSturdy(level, pos.below(), Direction.UP);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource randomSource) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return state.getValue(PLACEMENT_STATE) != SunflowerEnum.GROUND_BOTTOM;
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader p_256559_, BlockPos p_50898_, BlockState p_50899_) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level p_220878_, RandomSource p_220879_, BlockPos p_220880_, BlockState p_220881_) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        BlockPos currentPos = pos.above();
        BlockState currentState = level.getBlockState(currentPos);

        while (currentState.getBlock() == this && currentPos.getY() < level.getMaxBuildHeight()) {
            currentPos = currentPos.above();
            currentState = level.getBlockState(currentPos);
        }

        if (currentState.isAir()) {
            BlockPos below = currentPos.below();
            BlockState belowState = level.getBlockState(below);

            level.setBlock(
                    currentPos,
                    belowState.setValue(PLACEMENT_STATE, SunflowerEnum.TOP),
                    3
            );
        }
    }
}
