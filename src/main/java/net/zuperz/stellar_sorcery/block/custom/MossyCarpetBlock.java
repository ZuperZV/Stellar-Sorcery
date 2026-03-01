package net.zuperz.stellar_sorcery.block.custom;


import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.mojang.serialization.MapCodec;
import java.util.Map;
import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Plane;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.WallSide;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.AABB;
import net.zuperz.stellar_sorcery.block.ModBlocks;

import javax.annotation.Nullable;

public class MossyCarpetBlock extends Block implements BonemealableBlock {
    public static final MapCodec<MossyCarpetBlock> CODEC = simpleCodec(MossyCarpetBlock::new);
    public static final BooleanProperty BASE;
    public static final EnumProperty<WallSide> NORTH;
    public static final EnumProperty<WallSide> EAST;
    public static final EnumProperty<WallSide> SOUTH;
    public static final EnumProperty<WallSide> WEST;
    public static final Map<Direction, EnumProperty<WallSide>> PROPERTY_BY_DIRECTION;
    private final ImmutableMap<BlockState, VoxelShape> shapes;

    public MapCodec<MossyCarpetBlock> codec() {
        return CODEC;
    }

    public MossyCarpetBlock(BlockBehaviour.Properties p_380381_) {
        super(p_380381_);
        this.registerDefaultState((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)((BlockState)this.stateDefinition.any()).setValue(BASE, true)).setValue(NORTH, WallSide.NONE)).setValue(EAST, WallSide.NONE)).setValue(SOUTH, WallSide.NONE)).setValue(WEST, WallSide.NONE));
        this.shapes = this.makeShapes();
    }

    public ImmutableMap<BlockState, VoxelShape> makeShapes() {
        Map<Direction, VoxelShape> map = rotateHorizontal(boxZ(16.0, 0.0, 10.0, 0.0, 1.0));
        Map<Direction, VoxelShape> map1 = rotateAll(boxZ(16.0, 0.0, 1.0));
        return this.getShapeForEachState(p_393367_ -> {
            VoxelShape voxelshape = (Boolean)p_393367_.getValue(BASE) ? (VoxelShape)map1.get(Direction.DOWN) : Shapes.empty();

            for(Map.Entry<Direction, EnumProperty<WallSide>> entry : PROPERTY_BY_DIRECTION.entrySet()) {
                switch ((WallSide)p_393367_.getValue((Property)entry.getValue())) {
                    case NONE:
                    default:
                        break;
                    case LOW:
                        voxelshape = Shapes.or(voxelshape, (VoxelShape)map.get(entry.getKey()));
                        break;
                    case TALL:
                        voxelshape = Shapes.or(voxelshape, (VoxelShape)map1.get(entry.getKey()));
                }
            }

            return voxelshape.isEmpty() ? Shapes.block() : voxelshape;
        });
    }

    protected VoxelShape getShape(BlockState p_380262_, BlockGetter p_379532_, BlockPos p_379586_, CollisionContext p_380281_) {
        return this.shapes.get(p_380262_);
    }

    protected VoxelShape getCollisionShape(BlockState p_380336_, BlockGetter p_380068_, BlockPos p_379717_, CollisionContext p_379651_) {
        return (Boolean)p_380336_.getValue(BASE) ? this.shapes.get(this.defaultBlockState()) : Shapes.empty();
    }

    protected boolean propagatesSkylightDown(BlockState p_379750_) {
        return true;
    }

    protected boolean canSurvive(BlockState p_379574_, LevelReader p_379768_, BlockPos p_380354_) {
        BlockState blockstate = p_379768_.getBlockState(p_380354_.below());
        return (Boolean)p_379574_.getValue(BASE) ? !blockstate.isAir() : blockstate.is(this) && (Boolean)blockstate.getValue(BASE);
    }

    private static boolean hasFaces(BlockState state) {
        if ((Boolean)state.getValue(BASE)) {
            return true;
        } else {
            for(EnumProperty<WallSide> enumproperty : PROPERTY_BY_DIRECTION.values()) {
                if (state.getValue(enumproperty) != WallSide.NONE) {
                    return true;
                }
            }

            return false;
        }
    }

    private static boolean canSupportAtFace(BlockGetter level, BlockPos pos, Direction direction) {
        if (direction == Direction.UP) {
            return false;
        }

        BlockPos supportPos = pos.relative(direction);
        return MultifaceBlock.canAttachTo(level, direction, supportPos, level.getBlockState(supportPos));
    }

    private static BlockState getUpdatedState(BlockState state, BlockGetter level, BlockPos pos, boolean tip) {
        BlockState blockstate = null;
        BlockState blockstate1 = null;
        tip |= (Boolean)state.getValue(BASE);

        for(Direction direction : Plane.HORIZONTAL) {
            EnumProperty<WallSide> enumproperty = getPropertyForFace(direction);
            WallSide wallside = canSupportAtFace(level, pos, direction) ? (tip ? WallSide.LOW : (WallSide)state.getValue(enumproperty)) : WallSide.NONE;
            if (wallside == WallSide.LOW) {
                if (blockstate == null) {
                    blockstate = level.getBlockState(pos.above());
                }

                if (blockstate.is(ModBlocks.GLOOM_MOSS_CARPET) && blockstate.getValue(enumproperty) != WallSide.NONE && !(Boolean)blockstate.getValue(BASE)) {
                    wallside = WallSide.TALL;
                }

                if (!(Boolean)state.getValue(BASE)) {
                    if (blockstate1 == null) {
                        blockstate1 = level.getBlockState(pos.below());
                    }

                    if (blockstate1.is(ModBlocks.GLOOM_MOSS_CARPET) && blockstate1.getValue(enumproperty) == WallSide.NONE) {
                        wallside = WallSide.NONE;
                    }
                }
            }

            state = (BlockState)state.setValue(enumproperty, wallside);
        }

        return state;
    }

    public @Nullable BlockState getStateForPlacement(BlockPlaceContext p_380111_) {
        return getUpdatedState(this.defaultBlockState(), p_380111_.getLevel(), p_380111_.getClickedPos(), true);
    }

    public static void placeAt(LevelAccessor level, BlockPos pos, RandomSource random, int flags) {
        BlockState blockstate = ModBlocks.GLOOM_MOSS_CARPET.get().defaultBlockState();
        BlockState blockstate1 = getUpdatedState(blockstate, level, pos, true);
        level.setBlock(pos, blockstate1, flags);
        Objects.requireNonNull(random);
        BlockState blockstate2 = createTopperWithSideChance(level, pos, random::nextBoolean);
        if (!blockstate2.isAir()) {
            level.setBlock(pos.above(), blockstate2, flags);
            BlockState blockstate3 = getUpdatedState(blockstate1, level, pos, true);
            level.setBlock(pos, blockstate3, flags);
        }

    }

    public void setPlacedBy(Level p_380310_, BlockPos p_380202_, BlockState p_379659_, @Nullable LivingEntity p_379877_, ItemStack p_380344_) {
        if (!p_380310_.isClientSide()) {
            RandomSource randomsource = p_380310_.getRandom();
            Objects.requireNonNull(randomsource);
            BlockState blockstate = createTopperWithSideChance(p_380310_, p_380202_, randomsource::nextBoolean);
            if (!blockstate.isAir()) {
                p_380310_.setBlock(p_380202_.above(), blockstate, 3);
            }
        }

    }

    private static BlockState createTopperWithSideChance(BlockGetter level, BlockPos pos, BooleanSupplier placeSide) {
        BlockPos blockpos = pos.above();
        BlockState blockstate = level.getBlockState(blockpos);
        boolean flag = blockstate.is(ModBlocks.GLOOM_MOSS_CARPET);
        if ((!flag || !(Boolean)blockstate.getValue(BASE)) && (flag || blockstate.canBeReplaced())) {
            BlockState blockstate1 = (BlockState)ModBlocks.GLOOM_MOSS_CARPET.get().defaultBlockState().setValue(BASE, false);
            BlockState blockstate2 = getUpdatedState(blockstate1, level, pos.above(), true);

            for(Direction direction : Plane.HORIZONTAL) {
                EnumProperty<WallSide> enumproperty = getPropertyForFace(direction);
                if (blockstate2.getValue(enumproperty) != WallSide.NONE && !placeSide.getAsBoolean()) {
                    blockstate2 = (BlockState)blockstate2.setValue(enumproperty, WallSide.NONE);
                }
            }

            return hasFaces(blockstate2) && blockstate2 != blockstate ? blockstate2 : Blocks.AIR.defaultBlockState();
        } else {
            return Blocks.AIR.defaultBlockState();
        }
    }

    protected BlockState updateShape(
        BlockState p_379698_, Direction p_380408_, BlockState p_379613_, LevelAccessor p_379600_, BlockPos p_380051_, BlockPos p_380380_
    ) {
        if (!p_379698_.canSurvive(p_379600_, p_380051_)) {
            return Blocks.AIR.defaultBlockState();
        } else {
            BlockState blockstate = getUpdatedState(p_379698_, p_379600_, p_380051_, false);
            return !hasFaces(blockstate) ? Blocks.AIR.defaultBlockState() : blockstate;
        }
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_379510_) {
        p_379510_.add(new Property[]{BASE, NORTH, EAST, SOUTH, WEST});
    }

    protected BlockState rotate(BlockState p_379325_, Rotation p_380164_) {
        BlockState var10000;
        switch (p_380164_) {
            case CLOCKWISE_180 -> var10000 = (BlockState)((BlockState)((BlockState)((BlockState)p_379325_.setValue(NORTH, (WallSide)p_379325_.getValue(SOUTH))).setValue(EAST, (WallSide)p_379325_.getValue(WEST))).setValue(SOUTH, (WallSide)p_379325_.getValue(NORTH))).setValue(WEST, (WallSide)p_379325_.getValue(EAST));
            case COUNTERCLOCKWISE_90 -> var10000 = (BlockState)((BlockState)((BlockState)((BlockState)p_379325_.setValue(NORTH, (WallSide)p_379325_.getValue(EAST))).setValue(EAST, (WallSide)p_379325_.getValue(SOUTH))).setValue(SOUTH, (WallSide)p_379325_.getValue(WEST))).setValue(WEST, (WallSide)p_379325_.getValue(NORTH));
            case CLOCKWISE_90 -> var10000 = (BlockState)((BlockState)((BlockState)((BlockState)p_379325_.setValue(NORTH, (WallSide)p_379325_.getValue(WEST))).setValue(EAST, (WallSide)p_379325_.getValue(NORTH))).setValue(SOUTH, (WallSide)p_379325_.getValue(EAST))).setValue(WEST, (WallSide)p_379325_.getValue(SOUTH));
            default -> var10000 = p_379325_;
        }

        return var10000;
    }

    protected BlockState mirror(BlockState p_379462_, Mirror p_380184_) {
        BlockState var10000;
        switch (p_380184_) {
            case LEFT_RIGHT -> var10000 = (BlockState)((BlockState)p_379462_.setValue(NORTH, (WallSide)p_379462_.getValue(SOUTH))).setValue(SOUTH, (WallSide)p_379462_.getValue(NORTH));
            case FRONT_BACK -> var10000 = (BlockState)((BlockState)p_379462_.setValue(EAST, (WallSide)p_379462_.getValue(WEST))).setValue(WEST, (WallSide)p_379462_.getValue(EAST));
            default -> var10000 = super.mirror(p_379462_, p_380184_);
        }

        return var10000;
    }

    public static @Nullable EnumProperty<WallSide> getPropertyForFace(Direction direction) {
        return (EnumProperty)PROPERTY_BY_DIRECTION.get(direction);
    }

    public boolean isValidBonemealTarget(LevelReader p_379909_, BlockPos p_379807_, BlockState p_379358_) {
        return (Boolean)p_379358_.getValue(BASE) && !createTopperWithSideChance(p_379909_, p_379807_, () -> true).isAir();
    }

    public boolean isBonemealSuccess(Level p_380168_, RandomSource p_380045_, BlockPos p_380299_, BlockState p_379595_) {
        return true;
    }

    public void performBonemeal(ServerLevel p_379402_, RandomSource p_379670_, BlockPos p_379387_, BlockState p_379934_) {
        BlockState blockstate = createTopperWithSideChance(p_379402_, p_379387_, () -> true);
        if (!blockstate.isAir()) {
            p_379402_.setBlock(p_379387_.above(), blockstate, 3);
        }

    }

    static {
        BASE = BlockStateProperties.BOTTOM;
        NORTH = BlockStateProperties.NORTH_WALL;
        EAST = BlockStateProperties.EAST_WALL;
        SOUTH = BlockStateProperties.SOUTH_WALL;
        WEST = BlockStateProperties.WEST_WALL;
        PROPERTY_BY_DIRECTION = ImmutableMap.copyOf(Maps.newEnumMap(Map.of(Direction.NORTH, NORTH, Direction.EAST, EAST, Direction.SOUTH, SOUTH, Direction.WEST, WEST)));
    }

    private static VoxelShape boxZ(double width, double yMin, double yMax, double zMin, double zMax) {
        return Block.box(0.0, yMin, zMin, width, yMax, zMax);
    }

    private static VoxelShape boxZ(double width, double zMin, double zMax) {
        return Block.box(0.0, 0.0, zMin, width, 16.0, zMax);
    }

    private static Map<Direction, VoxelShape> rotateHorizontal(VoxelShape shape) {
        Map<Direction, VoxelShape> map = Maps.newEnumMap(Direction.class);
        map.put(Direction.NORTH, shape);
        map.put(Direction.EAST, rotateYClockwise(shape));
        map.put(Direction.SOUTH, rotateY180(shape));
        map.put(Direction.WEST, rotateYCounterClockwise(shape));
        return map;
    }

    private static Map<Direction, VoxelShape> rotateAll(VoxelShape shape) {
        Map<Direction, VoxelShape> map = Maps.newEnumMap(Direction.class);
        map.put(Direction.NORTH, shape);
        map.put(Direction.EAST, rotateYClockwise(shape));
        map.put(Direction.SOUTH, rotateY180(shape));
        map.put(Direction.WEST, rotateYCounterClockwise(shape));
        map.put(Direction.UP, rotateNorthToUp(shape));
        map.put(Direction.DOWN, rotateNorthToDown(shape));
        return map;
    }

    private static VoxelShape rotateYClockwise(VoxelShape shape) {
        return transformShape(shape, box -> new AABB(
            1.0 - box.maxZ, box.minY, box.minX,
            1.0 - box.minZ, box.maxY, box.maxX
        ));
    }

    private static VoxelShape rotateYCounterClockwise(VoxelShape shape) {
        return transformShape(shape, box -> new AABB(
            box.minZ, box.minY, 1.0 - box.maxX,
            box.maxZ, box.maxY, 1.0 - box.minX
        ));
    }

    private static VoxelShape rotateY180(VoxelShape shape) {
        return transformShape(shape, box -> new AABB(
            1.0 - box.maxX, box.minY, 1.0 - box.maxZ,
            1.0 - box.minX, box.maxY, 1.0 - box.minZ
        ));
    }

    private static VoxelShape rotateNorthToUp(VoxelShape shape) {
        return transformShape(shape, box -> new AABB(
            box.minX, 1.0 - box.maxZ, box.minY,
            box.maxX, 1.0 - box.minZ, box.maxY
        ));
    }

    private static VoxelShape rotateNorthToDown(VoxelShape shape) {
        return transformShape(shape, box -> new AABB(
            box.minX, box.minZ, 1.0 - box.maxY,
            box.maxX, box.maxZ, 1.0 - box.minY
        ));
    }

    private static VoxelShape transformShape(VoxelShape shape, Function<AABB, AABB> transform) {
        VoxelShape rotated = Shapes.empty();
        for (AABB box : shape.toAabbs()) {
            AABB rotatedBox = transform.apply(box);
            rotated = Shapes.or(rotated, Shapes.box(rotatedBox.minX, rotatedBox.minY, rotatedBox.minZ, rotatedBox.maxX, rotatedBox.maxY, rotatedBox.maxZ));
        }
        return rotated;
    }
}
