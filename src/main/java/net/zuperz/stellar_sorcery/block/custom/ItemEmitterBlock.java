package net.zuperz.stellar_sorcery.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.zuperz.stellar_sorcery.block.entity.custom.ItemEmitterBlockEntity;
import org.jetbrains.annotations.Nullable;

public class ItemEmitterBlock extends Block implements EntityBlock {
    public static final BooleanProperty ON = BooleanProperty.create("on");
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape SHAPE_NORTH_OFF = Shapes.or(
            box(5, 5, 14.99, 11, 11, 17.99),
            box(6, 6, 13.49, 10, 10, 15.99),
            box(9.75, 9.75, 13.99, 11.75, 11.75, 18.99),
            box(4.25, 9.75, 13.99, 6.25, 11.75, 18.99),
            box(4.25, 4.25, 13.99, 6.25, 6.25, 18.99),
            box(9.75, 4.25, 13.99, 11.75, 6.25, 18.99)
    );

    private static final VoxelShape SHAPE_NORTH_ON = Shapes.or(
            box(5, 5, 14.99, 11, 11, 17.99),
            box(6, 6, 14.49, 10, 10, 16.99),
            box(9.75, 9.75, 13.99, 11.75, 11.75, 18.99),
            box(4.25, 9.75, 13.99, 6.25, 11.75, 18.99),
            box(4.25, 4.25, 13.99, 6.25, 6.25, 18.99),
            box(9.75, 4.25, 13.99, 11.75, 6.25, 18.99)
    );

    private static final VoxelShape SHAPE_EAST_OFF = rotateShape(Direction.NORTH, Direction.EAST, SHAPE_NORTH_OFF);
    private static final VoxelShape SHAPE_SOUTH_OFF = rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_NORTH_OFF);
    private static final VoxelShape SHAPE_WEST_OFF = rotateShape(Direction.NORTH, Direction.WEST, SHAPE_NORTH_OFF);
    private static final VoxelShape SHAPE_UP_OFF = rotateShape(Direction.NORTH, Direction.UP, SHAPE_NORTH_OFF);
    private static final VoxelShape SHAPE_DOWN_OFF = rotateShape(Direction.NORTH, Direction.DOWN, SHAPE_NORTH_OFF);

    private static final VoxelShape SHAPE_EAST_ON = rotateShape(Direction.NORTH, Direction.EAST, SHAPE_NORTH_ON);
    private static final VoxelShape SHAPE_SOUTH_ON = rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_NORTH_ON);
    private static final VoxelShape SHAPE_WEST_ON = rotateShape(Direction.NORTH, Direction.WEST, SHAPE_NORTH_ON);
    private static final VoxelShape SHAPE_UP_ON = rotateShape(Direction.NORTH, Direction.UP, SHAPE_NORTH_ON);
    private static final VoxelShape SHAPE_DOWN_ON = rotateShape(Direction.NORTH, Direction.DOWN, SHAPE_NORTH_ON);


    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        boolean on = state.getValue(ON);

        Direction dir = state.getValue(FACING);

        if (!on) {
            return switch (dir) {
                case NORTH -> SHAPE_NORTH_OFF;
                case SOUTH -> SHAPE_SOUTH_OFF;
                case EAST -> SHAPE_EAST_OFF;
                case WEST -> SHAPE_WEST_OFF;
                case UP -> SHAPE_UP_OFF;
                case DOWN -> SHAPE_DOWN_OFF;
            };
        } else {
            return switch (dir) {
                case NORTH -> SHAPE_NORTH_ON;
                case SOUTH -> SHAPE_SOUTH_ON;
                case EAST -> SHAPE_EAST_ON;
                case WEST -> SHAPE_WEST_ON;
                case UP -> SHAPE_UP_ON;
                case DOWN -> SHAPE_DOWN_ON;
            };
        }
    }

    public ItemEmitterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ON, false));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState p_60503_, Level p_60504_, BlockPos p_60505_, Player p_60506_, BlockHitResult p_60508_) {
        if (!p_60504_.isClientSide) {
            p_60503_ = p_60503_.setValue(ON, !p_60503_.getValue(ON));
            p_60504_.setBlock(p_60505_, p_60503_, 3);
            return InteractionResult.SUCCESS;
        }
        return super.useWithoutItem(p_60503_, p_60504_, p_60505_, p_60506_, p_60508_);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemEmitterBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace()).setValue(ON, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ON);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;

        return (lvl, pos, st, blockEntity) -> {
            if (blockEntity instanceof ItemEmitterBlockEntity tile) {
                ItemEmitterBlockEntity.tick(level, pos, st, tile);
            }
        };
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
}