package net.zuperz.stellar_sorcery.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zuperz.stellar_sorcery.block.entity.custom.AugmentForgeBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.AugmentForgeBlockEntity;
import org.jetbrains.annotations.Nullable;

public class AugmentForgeBlock extends BaseEntityBlock {

public static final MapCodec<AstralNexusBlock> CODEC = simpleCodec(AstralNexusBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(
            box(13, 12, 1, 15, 13, 3),
            box(1, 12, 1, 3, 13, 3),
            box(1, 12, 13, 3, 13, 15),
            box(13, 12, 13, 15, 13, 15),
            box(1, 0, 1, 15, 2, 15),
            box(1, 9, 1, 15, 12, 15),
            box(3, 2, 3, 13, 9, 13),
            box(4.999, 2.5, 1.5, 10.999, 8.5, 4.5),
            box(11.5, 2.5, 4.999, 14.5, 8.5, 10.999),
            box(5.001, 2.5, 11.5, 11.001, 8.5, 14.5),
            box(1.5, 2.5, 5.001, 4.5, 8.5, 11.001),
            box(12, 8, 2, 14, 9, 4),
            box(12, 2, 12, 14, 3, 14),
            box(2, 2, 12, 4, 3, 14),
            box(2, 2, 2, 4, 3, 4),
            box(12, 2, 2, 14, 3, 4),
            box(2, 8, 2, 4, 9, 4),
            box(2, 8, 12, 4, 9, 14),
            box(12, 8, 12, 14, 9, 14)
    );

    public AugmentForgeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AugmentForgeBlockEntity(pPos, pState);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState();
    }

    @Override
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos,
                            BlockState pNewState, boolean pMovedByPiston) {
        if(pState.getBlock() != pNewState.getBlock()) {
            if(pLevel.getBlockEntity(pPos) instanceof AugmentForgeBlockEntity AugmentForgeBlockEntity) {
                AugmentForgeBlockEntity.drops();
                pLevel.updateNeighbourForOutputSignal(pPos, this);
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos,
                                              Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if (pLevel.getBlockEntity(pPos) instanceof AugmentForgeBlockEntity nexus) {

            if (nexus.inventory.getStackInSlot(0).isEmpty() && !pStack.isEmpty()) {
                nexus.inventory.insertItem(0, pStack.copy(), false);
                pStack.shrink(1);
                pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
                return ItemInteractionResult.SUCCESS;
            }

            else if (pStack.isEmpty() || !pStack.isEmpty() && !nexus.inventory.getStackInSlot(0).isEmpty()) {
                ItemStack extracted = nexus.inventory.extractItem(0, 1, true);

                if (!extracted.isEmpty()) {
                    boolean addedToInventory = false;

                    for (int i = 0; i < pPlayer.getInventory().items.size(); i++) {
                        ItemStack playerStack = pPlayer.getInventory().items.get(i);

                        if (!playerStack.isEmpty()
                                && ItemStack.isSameItem(playerStack, extracted)
                                && playerStack.getCount() < playerStack.getMaxStackSize()) {

                            playerStack.grow(1);
                            addedToInventory = true;
                            break;
                        }
                    }

                    if (!addedToInventory && pStack.isEmpty()) {
                        pPlayer.setItemInHand(InteractionHand.MAIN_HAND, extracted);
                        addedToInventory = true;
                    }

                    if (addedToInventory) {
                        nexus.clearContents();
                        nexus.inventory.extractItem(0, 1, false);
                        pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
                    }
                    return ItemInteractionResult.SUCCESS;
                }
            }
        }

        return ItemInteractionResult.SUCCESS;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof AugmentForgeBlockEntity nexus)) return;

        ItemStack stack = nexus.getInputItems().getStackInSlot(0);
        if (stack.isEmpty()) return;
    }
}
