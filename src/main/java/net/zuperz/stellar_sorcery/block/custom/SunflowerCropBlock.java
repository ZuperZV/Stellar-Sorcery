package net.zuperz.stellar_sorcery.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.TriState;
import net.zuperz.stellar_sorcery.item.ModItems;

public class SunflowerCropBlock extends CropBlock {
    public static final int MAX_AGE = 4;
    public static final int SECOND_MAX_AGE = 2;
    public static final IntegerProperty AGE = IntegerProperty.create("age", 0, MAX_AGE + SECOND_MAX_AGE);

    private static final VoxelShape[] SHAPE_BY_AGE =
            new VoxelShape[]{
                    Block.box(3.0D, 0.0D, 3.0D, 10.0D, 2.0D, 10.0D),
                    Block.box(3.0D, 0.0D, 3.0D, 10.0D, 6.0D, 10.0D),
                    Block.box(3.0D, 0.0D, 3.0D, 10.0D, 8.0D, 10.0D),
                    Block.box(3.0D, 0.0D, 3.0D, 10.0D, 12.0D, 10.0D),
                    Block.box(3.0D, 0.0D, 3.0D, 10.0D, 16.0D, 10.0D),
                    Block.box(3.0D, 0.0D, 3.0D, 10.0D, 6.0D, 10.0D),
                    Block.box(3.0D, 0.0D, 3.0D, 10.0D, 8.0D, 10.0D)};

    public SunflowerCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter blockGetter, BlockPos pos, CollisionContext collisionContext) {
        return SHAPE_BY_AGE[this.getAge(pState)];
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.SUNFLOWER_SEEDS;
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE + SECOND_MAX_AGE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(AGE);
    }

    @Override
    public void growCrops(Level pLevel, BlockPos pPos, BlockState pState) {
        int nextAge = this.getAge(pState) + this.getBonemealAgeIncrease(pLevel);
        int maxAge = this.getMaxAge();
        if (nextAge > maxAge) {
            nextAge = maxAge;
        }

        if(this.getAge(pState) == MAX_AGE && pLevel.getBlockState(pPos.above(1)).is(Blocks.AIR)) {
            pLevel.setBlock(pPos.above(1), this.getStateForAge(nextAge), 2);
        } else {
            pLevel.setBlock(pPos, this.getStateForAge(nextAge - SECOND_MAX_AGE), 2);
        }
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 1)) return;
        if (level.getRawBrightness(pos, 0) >= 9) {
            int currentAge = this.getAge(state);
            if (currentAge < this.getMaxAge()) {
                float growthSpeed = getGrowthSpeed(state, level, pos);
                if (net.neoforged.neoforge.common.CommonHooks.canCropGrow(level, pos, state, random.nextInt((int)(25.0F / growthSpeed) + 1) == 0
                )) {
                    if (currentAge == MAX_AGE) {
                        if (level.getBlockState(pos.above()).isAir()) {
                            level.setBlock(pos.above(), this.getStateForAge(currentAge + 1), 2);
                        }
                    } else {
                        level.setBlock(pos, this.getStateForAge(currentAge + 1), 2);
                    }
                    net.neoforged.neoforge.common.CommonHooks.fireCropGrowPost(level, pos, state
                    );
                }
            }
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return !this.isMaxAge(state) && !level.getBlockState(pos.below()).is(this) && !level.getBlockState(pos.above()).is(this);
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, net.minecraft.world.level.block.entity.BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (!level.isClientSide && !(player instanceof FakePlayer)) {
            player.addEffect(new MobEffectInstance(MobEffects.UNLUCK, 100, 0));
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        super.animateTick(state, level, pos, random);
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter world, BlockPos pos) {
        return state.getValue(getAgeProperty()) == getMaxAge() ? 10 : 0;
    }

    @Override
    public TriState canSustainPlant(BlockState state, BlockGetter level, BlockPos soilPosition, Direction facing, BlockState plant) {
        return super.canSustainPlant(state, level, soilPosition, facing, plant);
    }

    @Override
    public boolean canSurvive(BlockState pState, LevelReader pLevel, BlockPos pPos) {
        return super.canSurvive(pState, pLevel, pPos) || (pLevel.getBlockState(pPos.below(1)).is(this) &&
                pLevel.getBlockState(pPos.below(1)).getValue(AGE) == MAX_AGE);
    }
}