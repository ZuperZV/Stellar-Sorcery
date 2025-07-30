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
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralNexusBlockEntity;
import org.jetbrains.annotations.Nullable;

public class AstralNexusBlock extends BaseEntityBlock {
    public static final BooleanProperty CRAFTING = BooleanProperty.create("crafting");
    public static final MapCodec<AstralNexusBlock> CODEC = simpleCodec(AstralNexusBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(
            box(2, 0, 2, 14, 2, 14),
            box(4, 2, 4, 12, 10, 12),
            box(2, 9, 2, 14, 12, 14),
            box(2, 5, 2, 2, 9, 14),
            box(14, 5, 2, 14, 9, 14),
            box(2, 5, 14, 14, 9, 14),
            box(2, 5, 2, 14, 9, 2),
            box(12, 12, 2, 14, 13, 4),
            box(12, 12, 12, 14, 13, 14),
            box(2, 12, 12, 4, 13, 14),
            box(2, 12, 2, 4, 13, 4),
            box(12, 8, 2, 14, 9, 4),
            box(2, 8, 12, 4, 9, 14),
            box(12, 8, 12, 14, 9, 14),
            box(2, 8, 2, 4, 9, 4)
    );

    public AstralNexusBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(CRAFTING, false));
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
        return new AstralNexusBlockEntity(pPos, pState);
    }

    @org.jetbrains.annotations.Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState()
                .setValue(CRAFTING, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(CRAFTING);
    }

    @Override
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos,
                            BlockState pNewState, boolean pMovedByPiston) {
        if(pState.getBlock() != pNewState.getBlock()) {
            if(pLevel.getBlockEntity(pPos) instanceof AstralNexusBlockEntity AstralNexusBlockEntity) {
                AstralNexusBlockEntity.drops();
                pLevel.updateNeighbourForOutputSignal(pPos, this);
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos,
                                              Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if (pLevel.getBlockEntity(pPos) instanceof AstralNexusBlockEntity nexus) {

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
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (blockEntityType != ModBlockEntities.ASTRAL_NEXUS_BE.get()) return null;

        if (level.isClientSide) {
            return (lvl, pos, st, be) -> {
                if (be instanceof AstralNexusBlockEntity tile) {
                    AstralNexusBlockEntity.tickClient(lvl, pos, st, tile);
                }
            };
        } else {
            return (lvl, pos, st, be) -> {
                if (be instanceof AstralNexusBlockEntity tile) {
                    AstralNexusBlockEntity.tickServer(lvl, pos, st, tile);
                }
            };
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof AstralNexusBlockEntity nexus)) return;

        ItemStack stack = nexus.getInputItems().getStackInSlot(0);
        if (stack.isEmpty()) return;

        nexus.getFlyingItemPosition(0.0f).ifPresent(currentPos -> {
            BlockPos altarPos = nexus.getSavedPos();
            if (altarPos == null) return;

            Vec3 targetPos = new Vec3(altarPos.getX() + 0.5, altarPos.getY() + 1.15, altarPos.getZ() + 0.5);
            Vec3 direction = targetPos.subtract(currentPos).normalize().scale(0.05);

            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, stack),
                    currentPos.x, currentPos.y, currentPos.z,
                    direction.x, direction.y + 0.10, direction.z);
        });
    }
}