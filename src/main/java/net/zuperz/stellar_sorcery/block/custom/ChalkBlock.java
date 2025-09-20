package net.zuperz.stellar_sorcery.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Supplier;

public class ChalkBlock extends Block {
    private final SimpleParticleType particle;
    private final float chance;
    private final int amount;
    private final float speed;
    private final Supplier<Item> item;

    private static final VoxelShape SHAPE = Shapes.or(
            box(2, 0, 2, 14, 1, 14)
    );

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    public ChalkBlock(Properties properties, SimpleParticleType particle, float chance, int amount, float speed, Supplier<Item> item) {
        super(properties);
        this.particle = particle;
        this.amount = amount;
        this.speed = speed;
        this.chance = chance;
        this.item = item;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader p_304395_, BlockPos p_49824_, BlockState p_49825_) {
        return new ItemStack(item.get());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);

        if (random.nextFloat() < chance) {
            return;
        }

        for (int i = 0; i < amount; i++) {
            double x = pos.getX() + 0.5D + (random.nextDouble() - 0.5D);
            double y = pos.getY() + 0.01D;
            double z = pos.getZ() + 0.5D + (random.nextDouble() - 0.5D);

            double xSpeed = (random.nextDouble() - 0.5D) * speed;
            double ySpeed = (random.nextDouble() - 0.5D) * speed;
            double zSpeed = (random.nextDouble() - 0.5D) * speed;

            level.addParticle(particle, x, y, z, xSpeed, ySpeed, zSpeed);
        }
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return 2;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState stateBelow = level.getBlockState(below);
        return stateBelow.isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }
}

