package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.zuperz.stellar_sorcery.util.ModTags;

import java.util.List;

public class BloomMealItem extends BoneMealItem {

    public BloomMealItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockPos = context.getClickedPos();
        BlockState blockState = level.getBlockState(blockPos);

        assert context.getPlayer() != null;
        if (!context.getPlayer().isShiftKeyDown()) {
            if ((blockState.is(BlockTags.FLOWERS) && !blockState.is(BlockTags.TALL_FLOWERS)) ||
                    blockState.is(ModTags.Blocks.STELLER_SORCERY_FLOWERS_BLOCKS) && !blockState.is(BlockTags.TALL_FLOWERS)) {

                if (!level.isClientSide()) {

                    List<Holder<Block>> flowers = level.registryAccess()
                            .lookupOrThrow(Registries.BLOCK)
                            .getOrThrow(ModTags.Blocks.STELLER_SORCERY_FLOWERS_BLOCKS)
                            .stream()
                            .filter(holder -> holder.value() != blockState.getBlock())
                            .toList();

                    if (!flowers.isEmpty()) {

                        RandomSource random = level.getRandom();

                        Holder<Block> randomFlowerHolder =
                                flowers.get(random.nextInt(flowers.size()));

                        Block randomFlower = randomFlowerHolder.value();

                        BlockState newState = randomFlower.defaultBlockState();

                        if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                                && newState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {

                            newState = newState.setValue(
                                    BlockStateProperties.HORIZONTAL_FACING,
                                    blockState.getValue(BlockStateProperties.HORIZONTAL_FACING)
                            );
                        }

                        level.setBlock(blockPos, newState, 3);
                        addGrowthParticlesAndSound(level, blockPos, 30);

                        if (context.getPlayer() != null &&
                                !context.getPlayer().getAbilities().instabuild) {

                            context.getItemInHand().shrink(1);
                        }

                        return InteractionResult.SUCCESS;
                    }
                }

                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }

        return super.useOn(context);
    }

    public static void addGrowthParticlesAndSound(Level level, BlockPos pos, int amount) {
        if (level.isClientSide()) {
            return;
        }

        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.HAPPY_VILLAGER,
                    pos.getX() + 0.5D,
                    pos.getY() + 0.5D,
                    pos.getZ() + 0.5D,
                    amount,
                    0.3D,
                    0.3D,
                    0.3D,
                    0.0D
            );
        }

        level.playSound(
                null,
                pos,
                SoundEvents.BONE_MEAL_USE,
                SoundSource.BLOCKS,
                1.0F,
                1.0F
        );
    }
}