package net.zuperz.stellar_sorcery.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zuperz.stellar_sorcery.block.entity.custom.ArcForgeBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralAltarBlockEntity;
import org.jetbrains.annotations.Nullable;


public class ArcForgeBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Block.box(2, 0, 2, 14, 13, 14);
    public static final MapCodec<ArcForgeBlock> CODEC = simpleCodec(ArcForgeBlock::new);

    public ArcForgeBlock(Properties properties) {
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

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ArcForgeBlockEntity(pPos, pState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }



    @Override
    public @org.jetbrains.annotations.Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;

        return (lvl, pos, st, blockEntity) -> {
            if (blockEntity instanceof AstralAltarBlockEntity tile) {
                ArcForgeBlockEntity.tick(level, pos, state, tile);
            }
        };
    }

    @Override
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos,
                            BlockState pNewState, boolean pMovedByPiston) {
        if(pState.getBlock() != pNewState.getBlock()) {
            if(pLevel.getBlockEntity(pPos) instanceof ArcForgeBlockEntity ArcForgeBlockEntity) {
                ArcForgeBlockEntity.drops();
                ArcForgeBlockEntity.removeSigilEntity();
                pLevel.updateNeighbourForOutputSignal(pPos, this);
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos,
                                              Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if(pLevel.getBlockEntity(pPos) instanceof ArcForgeBlockEntity ArcForgeBlockEntity) {
            if(ArcForgeBlockEntity.inventory.getStackInSlot(0).isEmpty() && !pStack.isEmpty()) {
                ArcForgeBlockEntity.inventory.insertItem(0, pStack.copy(), false);
                pStack.shrink(1);
                ArcForgeBlockEntity.spawnSigilEntity(pStack);
                ArcForgeBlockEntity.setChanged();
                pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
            } else if(pStack.isEmpty()) {
                ItemStack stackOnPedestal = ArcForgeBlockEntity.inventory.extractItem(0, 1, false);
                pPlayer.setItemInHand(InteractionHand.MAIN_HAND, stackOnPedestal);
                ArcForgeBlockEntity.clearContents();
                ArcForgeBlockEntity.removeSigilEntity();
                ArcForgeBlockEntity.setChanged();
                pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
            }
        }
        return ItemInteractionResult.SUCCESS;
    }
}