package net.zuperz.stellar_sorcery.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;
import net.zuperz.stellar_sorcery.block.entity.custom.SoulCandleBlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.function.ToIntFunction;

import static net.minecraft.world.entity.LivingEntity.getSlotForHand;

public class SoulCandleBlock extends BaseEntityBlock {
    public static final MapCodec<SoulCandleBlock> CODEC = simpleCodec(SoulCandleBlock::new);
    public static BooleanProperty CRAFTING = BooleanProperty.create("crafting");
    public static BooleanProperty LIT = BooleanProperty.create("lit");
    public static final ToIntFunction<BlockState> LIGHT_EMISSION = p_152848_ -> p_152848_.getValue(LIT) ? 5 : 0;

    private static final VoxelShape SHAPE = Shapes.or(
            box(6.5, 0, 6.5, 9.5, 5.5, 9.5),
            box(6.5, 5.5, 6.5, 9.5, 6, 7.25),
            box(6.5, 5.5, 8.75, 9.5, 6, 9.5),
            box(8.75, 5.5, 7.25, 9.5, 6, 8.75),
            box(6.5, 5.5, 7.25, 7.25, 6, 8.75),
            box(7.5, 5, 8, 8.5, 7, 8),
            box(7.5, 5, 8, 8.5, 7, 8),
            box(5.5, 0.001, 5.5, 10.5, 0.001, 10.5)
    );

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pBlockHitResult) {
        if (!pStack.isEmpty() && pStack.canPerformAction(ItemAbilities.FIRESTARTER_LIGHT) && !pState.getValue(LIT)) {
            if (pState.hasProperty(LIT)) {
                pLevel.setBlock(pPos, pState.setValue(LIT, true), 3);
                if (!pStack.isDamageableItem()) {
                    pStack.shrink(1);
                } else {
                    pStack.hurtAndBreak(1, pPlayer, getSlotForHand(pHand));
                }
                pLevel.playSound(null, pPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1f, 1f);
                return ItemInteractionResult.SUCCESS;
            }
        } else if (!pStack.isEmpty() && pStack.canPerformAction(ItemAbilities.SHOVEL_DOUSE) && pState.getValue(LIT)) {
            if (pState.hasProperty(LIT)) {
                pLevel.setBlock(pPos, pState.setValue(LIT, false), 3);
                if (!pStack.isDamageableItem()) {
                    pStack.shrink(1);
                } else {
                    pStack.hurtAndBreak(1, pPlayer, getSlotForHand(pHand));
                }
                pLevel.playSound(null, pPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return super.useItemOn(pStack, pState, pLevel, pPos, pPlayer, pHand, pBlockHitResult);
    }

    public SoulCandleBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CRAFTING, false).setValue(LIT, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState()
                .setValue(CRAFTING, false)
                .setValue(LIT, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(CRAFTING);
        pBuilder.add(LIT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new SoulCandleBlockEntity(pPos, pState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;

        return (lvl, pos, st, blockEntity) -> {
            if (blockEntity instanceof SoulCandleBlockEntity tile) {
                SoulCandleBlockEntity.tick(level, pos, state, tile);
            }
        };
    }

    @Override
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource randomSource) {
        if (blockState.getValue(LIT)) {
            Vec3 center = Vec3.atCenterOf(blockPos).add(0, 0, 0);
            addParticlesAndSound(level, center, randomSource);
        }
    }

    private static void addParticlesAndSound(Level level, Vec3 vec3, RandomSource randomSource) {
        float f = randomSource.nextFloat();
        if (f < 0.3F) {
            level.addParticle(ParticleTypes.SMOKE, vec3.x, vec3.y, vec3.z, 0.0, 0.0, 0.0);
            if (f < 0.17F) {
                System.out.println("playLocalSound");
                level.playLocalSound(
                        vec3.x + 0.5,
                        vec3.y + 0.5,
                        vec3.z + 0.5,
                        SoundEvents.CANDLE_AMBIENT,
                        SoundSource.BLOCKS,
                        1.0F + randomSource.nextFloat(),
                        randomSource.nextFloat() * 0.7F + 0.3F,
                        false
                );
            }
        }

        level.addParticle(ParticleTypes.SMALL_FLAME, vec3.x, vec3.y, vec3.z, 0.0, 0.0, 0.0);
    }
}