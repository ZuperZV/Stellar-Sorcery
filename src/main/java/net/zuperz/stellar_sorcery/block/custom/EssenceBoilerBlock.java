package net.zuperz.stellar_sorcery.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.zuperz.stellar_sorcery.block.entity.custom.EssenceBoilerBlockEntity;
import net.zuperz.stellar_sorcery.item.ModItems;
import net.zuperz.stellar_sorcery.util.ModTags;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.entity.LivingEntity.getSlotForHand;

public class EssenceBoilerBlock extends BaseEntityBlock {
    public static final MapCodec<EssenceBoilerBlock> CODEC = simpleCodec(EssenceBoilerBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static BooleanProperty DONE = BooleanProperty.create("done");

    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            box(0, 0.5, 10, 16, 4.5, 14),
            box(10, 0, 0, 14, 4, 16),
            box(2, 0, 0, 6, 4, 16),
            box(0, 0.5, 2, 16, 4.5, 6),
            box(1, 6.5, 1, 15, 8.5, 15),
            box(1, 8.5, 1, 3, 16.5, 13),
            box(13, 8.5, 1, 15, 16.5, 15),
            box(3, 8.5, 1, 13, 16.5, 3),
            box(1, 8.5, 13, 13, 16.5, 15),
            box(2, 15.5, 0, 14, 18.5, 2),
            box(14, 15.5, 0, 16, 18.5, 16),
            box(0, 15.5, 14, 14, 18.5, 16),
            box(0, 15.5, 0, 2, 18.5, 14)
    );

    private static final VoxelShape SHAPE_EAST = rotateShape(Direction.NORTH, Direction.EAST, SHAPE_NORTH);
    private static final VoxelShape SHAPE_SOUTH = rotateShape(Direction.NORTH, Direction.SOUTH, SHAPE_NORTH);
    private static final VoxelShape SHAPE_WEST = rotateShape(Direction.NORTH, Direction.WEST, SHAPE_NORTH);

    public EssenceBoilerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(LIT, false).setValue(DONE, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return switch (facing) {
            case NORTH -> SHAPE_NORTH;
            case EAST -> SHAPE_EAST;
            case SOUTH -> SHAPE_SOUTH;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState()
                .setValue(FACING, pContext.getHorizontalDirection().getOpposite())
                .setValue(LIT, false)
                .setValue(DONE, false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, LIT, DONE);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new EssenceBoilerBlockEntity(pPos, pState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected void onRemove(BlockState pState, Level pLevel, BlockPos pPos,
                            BlockState pNewState, boolean pMovedByPiston) {
        if(pState.getBlock() != pNewState.getBlock()) {
            if(pLevel.getBlockEntity(pPos) instanceof EssenceBoilerBlockEntity EssenceBoilerBlockEntity) {
                EssenceBoilerBlockEntity.drops();
                pLevel.updateNeighbourForOutputSignal(pPos, this);
            }
        }
        super.onRemove(pState, pLevel, pPos, pNewState, pMovedByPiston);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;

        return (lvl, pos, st, blockEntity) -> {
            if (blockEntity instanceof EssenceBoilerBlockEntity tile) {
                EssenceBoilerBlockEntity.tick(level, pos, state, tile);
            }
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos,
                                              Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if (pLevel.getBlockEntity(pPos) instanceof EssenceBoilerBlockEntity boiler) {

            IFluidHandlerItem fluidHandler = pStack.getCapability(Capabilities.FluidHandler.ITEM, null);

            if (!pStack.isEmpty() && pStack.is(Items.FLINT_AND_STEEL) && !pState.getValue(BlockStateProperties.LIT)) {
                if (pState.hasProperty(BlockStateProperties.LIT)) {
                    pLevel.setBlock(pPos, pState.setValue(BlockStateProperties.LIT, true), 3);
                    if (!pStack.isDamageableItem()) {
                        pStack.shrink(1);
                    } else {
                        pStack.hurtAndBreak(1, pPlayer, getSlotForHand(pHand));
                    }
                    pLevel.playSound(null, pPos, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1f, 1f);
                    return ItemInteractionResult.SUCCESS;
                }
            } else if (!pStack.isEmpty() && pStack.is(ItemTags.SHOVELS) && pState.getValue(BlockStateProperties.LIT)) {
                if (pState.hasProperty(BlockStateProperties.LIT)) {
                    pLevel.setBlock(pPos, pState.setValue(BlockStateProperties.LIT, false), 3);
                    if (!pStack.isDamageableItem()) {
                        pStack.shrink(1);
                    } else {
                        pStack.hurtAndBreak(1, pPlayer, getSlotForHand(pHand));
                    }
                    pLevel.playSound(null, pPos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1f, 1f);
                    return ItemInteractionResult.SUCCESS;
                }
            }

            if (!(boiler.progress > 0)) {
                if (fluidHandler != null && !pStack.isEmpty()) {
                    FluidStack itemFluid = fluidHandler.getFluidInTank(0);
                    FluidStack tankFluid = boiler.getFluidTank();

                    if (boiler.getFluidTank().isEmpty() || tankFluid.getFluid().isSame(itemFluid.getFluid())) {
                        int amountToDrain = boiler.getFluidTankCapacity() - boiler.getFluidTankAmount();
                        FluidStack drainedSim = fluidHandler.drain(amountToDrain, IFluidHandler.FluidAction.SIMULATE);
                        if (!drainedSim.isEmpty()) {
                            FluidStack drained = fluidHandler.drain(amountToDrain, IFluidHandler.FluidAction.EXECUTE);
                            boiler.fillFluidTank(drained);

                            pPlayer.setItemInHand(pHand, fluidHandler.getContainer());
                            pLevel.playSound(null, pPos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                            return ItemInteractionResult.SUCCESS;
                        }
                    }

                    if (!boiler.getFluidTank().isEmpty() && !pStack.isEmpty()) {
                        int amountAvailable = boiler.getFluidTankAmount();
                        FluidStack fluidToFill = boiler.getFluidTank().copy();
                        fluidToFill.setAmount(amountAvailable);

                        int filledAmount = fluidHandler.fill(fluidToFill, IFluidHandler.FluidAction.SIMULATE);

                        if (filledAmount > 0) {
                            FluidStack fluidFilled = fluidToFill.copy();
                            fluidFilled.setAmount(filledAmount);
                            fluidHandler.fill(fluidFilled, IFluidHandler.FluidAction.EXECUTE);

                            boiler.drainFluidTank(filledAmount);

                            pPlayer.setItemInHand(pHand, fluidHandler.getContainer());
                            pLevel.playSound(null, pPos, SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                            pLevel.playSound(null, pPos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.5f, 1f);
                            boiler.wobble(EssenceBoilerBlockEntity.WobbleStyle.POSITIVE);
                            return ItemInteractionResult.SUCCESS;
                        }
                    }
                }

                if (!pStack.isEmpty()) {
                    for (int slot = 0; slot < 3; slot++) {
                        if (boiler.inventory.getStackInSlot(slot).isEmpty()) {
                            boiler.inventory.insertItem(slot, pStack.copyWithCount(1), false);
                            if (boiler.getFluidTankAmount() > 0) {
                                pLevel.playSound(null, pPos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1f, 1f);
                                boiler.wobble(EssenceBoilerBlockEntity.WobbleStyle.POSITIVE);

                                pLevel.playSound(null, pPos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1.0F, 0.7F + 0.5F * 1f);
                                if (pLevel instanceof ServerLevel serverlevel) {
                                    serverlevel.sendParticles(
                                            ParticleTypes.DUST_PLUME,
                                            (double) pPos.getX() + 0.5,
                                            (double) pPos.getY() + 1,
                                            (double) pPos.getZ() + 0.5,
                                            7,
                                            0.0,
                                            0.0,
                                            0.0,
                                            0.0
                                    );
                                }
                            }
                            pStack.shrink(1);
                            pLevel.playSound(null, pPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
                            return ItemInteractionResult.SUCCESS;
                        }
                    }

                    if (boiler.inventory.getStackInSlot(3).isEmpty() && pStack.is(ModItems.EMPTY_ESSENCE_BOTTLE) && boiler.getFluidTank().getFluid().isSame(Fluids.WATER)) {
                        boiler.inventory.insertItem(3, pStack.copyWithCount(1), false);
                        if (boiler.getFluidTankAmount() > 0) {
                            pLevel.playSound(null, pPos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 1.2f, 1f);
                            boiler.wobble(EssenceBoilerBlockEntity.WobbleStyle.NEGATIVE);

                            pLevel.playSound(null, pPos, SoundEvents.DECORATED_POT_INSERT, SoundSource.BLOCKS, 1.0F, 0.7F + 0.5F * 1f);
                            if (pLevel instanceof ServerLevel serverlevel) {
                                serverlevel.sendParticles(
                                        ParticleTypes.DUST_PLUME,
                                        (double) pPos.getX() + 0.5,
                                        (double) pPos.getY() + 1,
                                        (double) pPos.getZ() + 0.5,
                                        7,
                                        0.0,
                                        0.0,
                                        0.0,
                                        0.0
                                );
                            }
                        }
                        pStack.shrink(1);
                        pLevel.playSound(null, pPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 2f);
                        return ItemInteractionResult.SUCCESS;
                    }
                }

                for (int slot = 0; slot < 3; slot++) {
                    ItemStack extracted = boiler.inventory.getStackInSlot(slot);
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
                            pPlayer.setItemInHand(pHand, extracted.copy());
                            addedToInventory = true;
                        }

                        if (addedToInventory) {
                            boiler.inventory.extractItem(slot, 1, false);
                            pLevel.playSound(null, pPos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1f, 1f);
                            return ItemInteractionResult.SUCCESS;
                        }
                    }
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
            if(level.getBlockEntity(pos) instanceof EssenceBoilerBlockEntity EssenceBoilerBlockEntity && !EssenceBoilerBlockEntity.getInputItems().getStackInSlot(0).isEmpty() && level.getBlockEntity(pos) instanceof EssenceBoilerBlockEntity Boiler) {
                if (Boiler.getFluidTank().getFluid().isSame(Fluids.WATER)) {
                    if (!Boiler.inventory.getStackInSlot(0).isEmpty()) {
                        level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, EssenceBoilerBlockEntity.getInputItems().getStackInSlot(0)),
                                xPos, yPos, zPos, 0.2, -0.4, 0.1);
                    }

                    if (!Boiler.inventory.getStackInSlot(1).isEmpty()) {
                        level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, EssenceBoilerBlockEntity.getInputItems().getStackInSlot(1)),
                                xPos, yPos, zPos, 0.2, -0.2, 0.3);
                    }

                    if (!Boiler.inventory.getStackInSlot(2).isEmpty()) {
                        level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, EssenceBoilerBlockEntity.getInputItems().getStackInSlot(2)),
                                xPos, yPos, zPos, 0.4, -0.1, 0.4);
                    }
                }
            }
        }

        if (state.getValue(LIT)) {
            if (random.nextInt(10) == 0) {
                level.playLocalSound(
                        (double)pos.getX() + 0.5,
                        (double)pos.getY() + 0.5,
                        (double)pos.getZ() + 0.5,
                        SoundEvents.CAMPFIRE_CRACKLE,
                        SoundSource.BLOCKS,
                        0.5F + random.nextFloat(),
                        random.nextFloat() * 0.7F + 0.6F,
                        false
                );
            }

            if (random.nextInt(5) == 0) {
                for (int i = 0; i < random.nextInt(1) + 1; i++) {
                    level.addParticle(
                            ParticleTypes.LAVA,
                            (double)pos.getX() + 0.5,
                            (double)pos.getY() + 0.5,
                            (double)pos.getZ() + 0.5,
                            (double)(random.nextFloat() / 2.0F),
                            5.0E-5,
                            (double)(random.nextFloat() / 2.0F)
                    );
                }
            }
        }
    }

    public static VoxelShape rotateShape(Direction from, Direction to, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, Shapes.empty()};

        int times = (to.get2DDataValue() - from.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1], Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }

        return buffer[0];
    }
}