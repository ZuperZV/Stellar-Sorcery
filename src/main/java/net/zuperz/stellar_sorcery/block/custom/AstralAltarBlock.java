package net.zuperz.stellar_sorcery.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralAltarBlockEntity;
import org.jetbrains.annotations.Nullable;

public class AstralAltarBlock extends BaseEntityBlock {
    public static final MapCodec<AstralAltarBlock> CODEC = simpleCodec(AstralAltarBlock::new);
    public static BooleanProperty CRAFTING = BooleanProperty.create("crafting");
    public static BooleanProperty DONE = BooleanProperty.create("done");

    private static final VoxelShape SHAPE = Shapes.or(
            box(1, 0, 1, 15, 2, 15),
            box(1, 11, 1, 15, 14, 15),
            box(3, 2, 3, 13, 11, 13),
            box(1, 7, 1, 1, 11, 15),
            box(15, 7, 1, 15, 11, 15),
            box(1, 7, 15, 15, 11, 15),
            box(1, 7, 1, 15, 11, 1),
            box(13, 14, 1, 15, 15, 3),
            box(1, 14, 1, 3, 15, 3),
            box(1, 14, 13, 3, 15, 15),
            box(13, 14, 13, 15, 15, 15)
    );


    public AstralAltarBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CRAFTING, false).setValue(DONE, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState()
                .setValue(CRAFTING, false)
                .setValue(DONE, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(CRAFTING);
        pBuilder.add(DONE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AstralAltarBlockEntity(pPos, pState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos,
                            BlockState pNewState, boolean pMovedByPiston) {
        if(pState.getBlock() != pNewState.getBlock()) {
            if(pLevel.getBlockEntity(pPos) instanceof AstralAltarBlockEntity AstralAltarBlockEntity) {
                AstralAltarBlockEntity.drops();
                pLevel.updateNeighbourForOutputSignal(pPos, this);
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    public @org.jetbrains.annotations.Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;

        return (lvl, pos, st, blockEntity) -> {
            if (blockEntity instanceof AstralAltarBlockEntity tile) {
                AstralAltarBlockEntity.tick(level, pos, state, tile);
            }
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos,
                                              Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if (pLevel.getBlockEntity(pPos) instanceof AstralAltarBlockEntity altar) {

            if (altar.inventory.getStackInSlot(0).isEmpty() && !pStack.isEmpty()) {
                altar.inventory.insertItem(0, pStack.copy(), false);
                pStack.shrink(1);
                pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
                return ItemInteractionResult.SUCCESS;
            }

            else if (pStack.isEmpty() || !pStack.isEmpty() && !altar.inventory.getStackInSlot(0).isEmpty()) {
                ItemStack extracted = altar.inventory.extractItem(0, 1, true);

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
                        altar.clearContents();
                        altar.inventory.extractItem(0, 1, false);
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
        double xPos = (double)pos.getX() + 0.5;
        double yPos = pos.getY() + 1.2;
        double zPos = (double)pos.getZ() + 0.5;
        if (random.nextDouble() < 0.1) {
            level.playLocalSound(xPos, yPos, zPos, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
            if(level.getBlockEntity(pos) instanceof AstralAltarBlockEntity AstralAltarBlockEntity && !AstralAltarBlockEntity.getInputItems().getStackInSlot(0).isEmpty()) {
                level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, AstralAltarBlockEntity.getInputItems().getStackInSlot(0)),
                        xPos, yPos, zPos , 0.0, 0.0, 0.0);
            }
        }
    }
}