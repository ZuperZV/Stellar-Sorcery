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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.zuperz.stellar_sorcery.block.entity.custom.LunarInfuserBlockEntity;
import org.jetbrains.annotations.Nullable;

public class LunarInfuserBlock extends BaseEntityBlock {
    public static final MapCodec<LunarInfuserBlock> CODEC = simpleCodec(LunarInfuserBlock::new);
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


    public LunarInfuserBlock(Properties properties) {
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

    @Nullable
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
        return new LunarInfuserBlockEntity(pPos, pState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos,
                            BlockState pNewState, boolean pMovedByPiston) {
        if(pState.getBlock() != pNewState.getBlock()) {
            if(pLevel.getBlockEntity(pPos) instanceof LunarInfuserBlockEntity AstralAltarBlockEntity) {
                AstralAltarBlockEntity.drops();
                pLevel.updateNeighbourForOutputSignal(pPos, this);
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;

        return (lvl, pos, st, blockEntity) -> {
            if (blockEntity instanceof LunarInfuserBlockEntity tile) {
                LunarInfuserBlockEntity.tick(level, pos, state, tile);
            }
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos,
                                              Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if (pLevel.getBlockEntity(pPos) instanceof LunarInfuserBlockEntity LunarInfuser) {
            IFluidHandlerItem fluidHandler = pStack.getCapability(Capabilities.FluidHandler.ITEM, null);

            if (fluidHandler != null && !pStack.isEmpty()) {
                FluidStack itemFluid = fluidHandler.getFluidInTank(0);
                FluidStack tankFluid = LunarInfuser.getFluidTank();

                if (LunarInfuser.getFluidTank().isEmpty() || tankFluid.getFluid().isSame(itemFluid.getFluid())) {
                    int amountToDrain = LunarInfuser.getFluidTankCapacity() - LunarInfuser.getFluidTankAmount();
                    FluidStack drainedSim = fluidHandler.drain(amountToDrain, IFluidHandler.FluidAction.SIMULATE);
                    if (!drainedSim.isEmpty()) {
                        FluidStack drained = fluidHandler.drain(amountToDrain, IFluidHandler.FluidAction.EXECUTE);
                        LunarInfuser.fillFluidTank(drained);

                        pPlayer.setItemInHand(pHand, fluidHandler.getContainer());
                        pLevel.playSound(null, pPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                        return ItemInteractionResult.SUCCESS;
                    }
                }

                if (!LunarInfuser.getFluidTank().isEmpty() && !pStack.isEmpty()) {
                    int amountAvailable = LunarInfuser.getFluidTankAmount();
                    FluidStack fluidToFill = LunarInfuser.getFluidTank().copy();
                    fluidToFill.setAmount(amountAvailable);

                    int filledAmount = fluidHandler.fill(fluidToFill, IFluidHandler.FluidAction.SIMULATE);

                    if (filledAmount > 0) {
                        FluidStack fluidFilled = fluidToFill.copy();
                        fluidFilled.setAmount(filledAmount);
                        fluidHandler.fill(fluidFilled, IFluidHandler.FluidAction.EXECUTE);

                        LunarInfuser.drainFluidTank(filledAmount);

                        pPlayer.setItemInHand(pHand, fluidHandler.getContainer());
                        pLevel.playSound(null, pPos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                        pLevel.playSound(null, pPos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.5f, 1f);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }
            if (LunarInfuser.inventory.getStackInSlot(0).isEmpty() && !pStack.isEmpty()) {
                LunarInfuser.inventory.insertItem(0, pStack.copy(), false);
                pStack.shrink(1);
                pLevel.playSound(pPlayer, pPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
                return ItemInteractionResult.SUCCESS;
            } else if (pStack.isEmpty() || !pStack.isEmpty() && !LunarInfuser.inventory.getStackInSlot(0).isEmpty()) {
                ItemStack extracted = LunarInfuser.inventory.extractItem(0, 1, true);

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
                        LunarInfuser.clearContents();
                        LunarInfuser.inventory.extractItem(0, 1, false);
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
            if(level.getBlockEntity(pos) instanceof LunarInfuserBlockEntity AstralAltarBlockEntity && !AstralAltarBlockEntity.getInputItems().getStackInSlot(0).isEmpty()) {
                level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, AstralAltarBlockEntity.getInputItems().getStackInSlot(0)),
                        xPos, yPos, zPos , 0.0, 0.0, 0.0);
            }
        }
    }
}