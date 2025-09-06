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
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.zuperz.stellar_sorcery.block.entity.custom.LunarJarBlockEntity;
import org.jetbrains.annotations.Nullable;

public class LunarJarBlock extends BaseEntityBlock {
    public static final MapCodec<LunarJarBlock> CODEC = simpleCodec(LunarJarBlock::new);

    private static final VoxelShape SHAPE = Shapes.or(
            box(13, 3, 4, 14, 6, 12),
            box(2, 3, 4, 3, 6, 12),
            box(4, 3, 2, 12, 6, 3),
            box(4, 3, 13, 12, 6, 14),
            box(2, 0, 2, 14, 3, 14),
            box(2, 13, 2, 14, 16, 14),
            box(3.01, 3, 3, 13.01, 13, 13),
            box(3.01, 3, 3, 3.01, 13, 13),
            box(3, 3, 3.01, 13, 13, 3.01),
            box(12.99, 3, 3, 12.99, 13, 13),
            box(3, 3, 12.99, 13, 13, 12.99),
            box(2, 16, 12, 4, 17, 14),
            box(2, 16, 2, 4, 17, 4),
            box(12, 16, 2, 14, 17, 4),
            box(12, 16, 12, 14, 17, 14)
    );

    public LunarJarBlock(Properties properties) {
        super(properties);
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
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new LunarJarBlockEntity(pPos, pState);
    }

    @Override
    protected RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide) return null;

        return (lvl, pos, st, blockEntity) -> {
            if (blockEntity instanceof LunarJarBlockEntity tile) {
                LunarJarBlockEntity.tick(level, pos, state, tile);
            }
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack pStack, BlockState pState, Level pLevel, BlockPos pPos,
                                              Player pPlayer, InteractionHand pHand, BlockHitResult pHitResult) {
        if (pLevel.getBlockEntity(pPos) instanceof LunarJarBlockEntity LunarInfuser) {
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
        }
    }
}