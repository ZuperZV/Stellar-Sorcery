package net.zuperz.stellar_sorcery.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.block.entity.custom.BoilerTipBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.EssenceBoilerBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.ItemEmitterBlockEntity;
import org.jetbrains.annotations.Nullable;

public class BoilerTipBlock extends Block implements EntityBlock {
    public static final BooleanProperty ON = BooleanProperty.create("on");
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            box(5.5, 10, 14, 6.5, 13, 17),
            box(9.5, 10, 14, 10.5, 13, 17),
            box(6.5, 10, 14, 9.5, 11, 17)
    );

    private static final VoxelShape SHAPE_EAST = rotateShape(Direction.NORTH, Direction.EAST, SHAPE_NORTH);
    private static final VoxelShape SHAPE_SOUTH = rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_NORTH);
    private static final VoxelShape SHAPE_WEST = rotateShape(Direction.NORTH, Direction.WEST, SHAPE_NORTH);
    private static final VoxelShape SHAPE_UP = rotateShape(Direction.NORTH, Direction.UP, SHAPE_NORTH);
    private static final VoxelShape SHAPE_DOWN = rotateShape(Direction.NORTH, Direction.DOWN, SHAPE_NORTH);


    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction dir = state.getValue(FACING);

        return switch (dir) {
            case NORTH -> SHAPE_NORTH;
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            case UP -> SHAPE_UP;
            case DOWN -> SHAPE_DOWN;
        };
    }

    public BoilerTipBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ON, false));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {

        if (level.isClientSide) return InteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);

        if (!(be instanceof BoilerTipBlockEntity tipBE)) {
            return InteractionResult.FAIL;
        }

        tipBE.extractFluid = !tipBE.extractFluid;

        if (!tipBE.extractFluid) {
            return InteractionResult.SUCCESS;
        }

        BlockPos checkPos = pos.below();

        while (checkPos.getY() > level.getMinBuildHeight()) {

            BlockState checkState = level.getBlockState(checkPos);

            if (checkState.is(ModBlocks.ESSENCE_BOILER)) {
                tipBE.targetPosEntity = checkPos;
                return InteractionResult.SUCCESS;
            }

            if (!checkState.isAir()) {
                tipBE.extractFluid = false;
                return InteractionResult.FAIL;
            }

            checkPos = checkPos.below();
        }

        tipBE.extractFluid = false;
        return InteractionResult.FAIL;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        BlockPos pos = context.getClickedPos().relative(facing.getOpposite());

        boolean isBoiler =
                context.getLevel().getBlockState(pos).is(ModBlocks.ESSENCE_BOILER);

        return this.defaultBlockState()
                .setValue(FACING, facing)
                .setValue(ON, isBoiler);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos, boolean isMoving) {
        if (level.isClientSide) return;

        Direction facing = state.getValue(FACING);
        BlockPos behindPos = pos.relative(facing.getOpposite());

        boolean isBoiler =
                level.getBlockState(behindPos).is(ModBlocks.ESSENCE_BOILER);

        level.setBlock(pos, state.setValue(ON, isBoiler), 3);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ON);
    }

    public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        if (from == to) return shape;

        VoxelShape[] buffer = new VoxelShape[]{shape, Shapes.empty()};

        // NORTH/SOUTH/EAST/WEST
        if (to.getAxis().isHorizontal()) {
            int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
            for (int i = 0; i < times; i++) {
                buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                        buffer[1] = Shapes.or(buffer[1], Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX))
                );
                buffer[0] = buffer[1];
                buffer[1] = Shapes.empty();
            }
        }


        // UP/DOWN
        else if (to == Direction.UP) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    buffer[1] = Shapes.or(buffer[1], Shapes.box(minX, 1 - maxZ, minY, maxX, 1 - minZ, maxY))
            );
            buffer[0] = buffer[1];
        } else if (to == Direction.DOWN) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) ->
                    buffer[1] = Shapes.or(buffer[1], Shapes.box(minX, minZ, 1 - maxY, maxX, maxZ, 1 - minY))
            );
            buffer[0] = buffer[1];
        }

        return buffer[0];
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new BoilerTipBlockEntity(pPos, pState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {

        return level.isClientSide ? null : (lvl, pos, st, be) -> {
            if (be instanceof BoilerTipBlockEntity tip) {
                BoilerTipBlockEntity.tick(lvl, pos, st, tip);
            }
        };
    }
}