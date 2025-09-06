package net.zuperz.stellar_sorcery.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zuperz.stellar_sorcery.block.entity.custom.LightBeamEmitterBlockEntity;
import org.jetbrains.annotations.Nullable;

public class LightBeamEmitterBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            box(5, 5, 17, 11, 11, 20),
            box(6, 6, 13, 10, 10, 15),
            box(9.75, 9.75, 14, 11.75, 11.75, 19),
            box(7, 7, 9, 9, 9, 14),
            box(4.25, 9.75, 14, 6.25, 11.75, 19),
            box(4.25, 4.25, 14, 6.25, 6.25, 19),
            box(9.75, 4.25, 14, 11.75, 6.25, 19)
    );

    private static final VoxelShape SHAPE_EAST = rotateShape(Direction.NORTH, Direction.EAST, SHAPE_NORTH);
    private static final VoxelShape SHAPE_SOUTH = rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_NORTH);
    private static final VoxelShape SHAPE_WEST = rotateShape(Direction.NORTH, Direction.WEST, SHAPE_NORTH);
    private static final VoxelShape SHAPE_UP = rotateShape(Direction.NORTH, Direction.UP, SHAPE_NORTH);
    private static final VoxelShape SHAPE_DOWN = rotateShape(Direction.NORTH, Direction.DOWN, SHAPE_NORTH);

    @Override
    protected VoxelShape getShape(BlockState p_54561_, BlockGetter p_54562_, BlockPos p_54563_, CollisionContext p_54564_) {
        switch ((Direction)p_54561_.getValue(FACING)) {
            case NORTH:
                return SHAPE_NORTH;
            case SOUTH:
                return SHAPE_SOUTH;
            case EAST:
                return SHAPE_EAST;
            case WEST:
                return SHAPE_WEST;
            case UP:
                return SHAPE_UP;
            case DOWN:
                return SHAPE_DOWN;
            default:
                return SHAPE_NORTH;
        }
    }

    public LightBeamEmitterBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LightBeamEmitterBlockEntity(pos, state);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getClickedFace());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;

        return (lvl, pos, st, blockEntity) -> {
            if (blockEntity instanceof LightBeamEmitterBlockEntity tile) {
                LightBeamEmitterBlockEntity.tick(level, pos, st, tile);
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