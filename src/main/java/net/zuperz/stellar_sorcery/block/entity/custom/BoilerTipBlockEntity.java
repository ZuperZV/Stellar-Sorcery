package net.zuperz.stellar_sorcery.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.block.custom.BoilerTipBlock;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.fluid.ModFluidTypes;

import java.util.Objects;

public class BoilerTipBlockEntity extends BlockEntity {

    public BoilerTipBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BOILER_TIP_BE.get(), pos, state);
    }

    public boolean extractFluid = false;
    public BlockPos targetPosEntity = null;
    private FluidStack cachedFluid = FluidStack.EMPTY;

    public static void tick(Level level, BlockPos pos, BlockState state, BoilerTipBlockEntity tip) {

        if (level.isClientSide) return;

        boolean powered = level.hasNeighborSignal(pos);

        Direction facing = state.getValue(BlockStateProperties.FACING);
        BlockPos behindPos = pos.relative(facing.getOpposite());

        BlockEntity sourceBE = level.getBlockEntity(behindPos);

        if (!(sourceBE instanceof EssenceBoilerBlockEntity sourceBoiler)) {
            tip.setFluidStack(FluidStack.EMPTY);
            return;
        }

        tip.setFluidStack(sourceBoiler.getTank().getFluid().copy());
        level.sendBlockUpdated(pos, state, state, 3);

        if (!tip.extractFluid && !powered) return;

        BlockPos targetPos = pos.below();

        while (targetPos.getY() > level.getMinBuildHeight()) {

            BlockState check = level.getBlockState(targetPos);

            if (check.is(ModBlocks.ESSENCE_BOILER)) {

                BlockEntity targetBE = level.getBlockEntity(targetPos);

                if (targetBE instanceof EssenceBoilerBlockEntity targetBoiler) {

                    boolean success = transfermb(sourceBoiler, targetBoiler, 5);

                    if (!success) {
                        tip.extractFluid = false;
                    }

                    return;
                }
            }

            if (!check.isAir()) {
                tip.extractFluid = false;
                return;
            }

            targetPos = targetPos.below();
        }
    }

    private static boolean transfermb(EssenceBoilerBlockEntity source, EssenceBoilerBlockEntity target, int amount) {

        var sourceTank = source.getTank();
        var targetTank = target.getTank();

        if (sourceTank.isEmpty()) return false;

        if (!targetTank.isEmpty()) {
            if (!Objects.equals(sourceTank.getFluid(), targetTank.getFluid())
                    && (((targetTank.getFluid().is(ModFluidTypes.POTION_FLUID_TYPE.get()))
                    || (sourceTank.getFluid().is(ModFluidTypes.POTION_FLUID_TYPE.get())))
                    && !Objects.equals(sourceTank.getFluid().getOrDefault(ModDataComponentTypes.POTION_CONTENTS, PotionContents.EMPTY),
                    targetTank.getFluid().getOrDefault(ModDataComponentTypes.POTION_CONTENTS, PotionContents.EMPTY)))) return false;
        }

        if (sourceTank.getFluidAmount() < amount) return false;
        if (targetTank.getCapacity() - targetTank.getFluidAmount() < amount) return false;

        FluidStack fluid = sourceTank.getFluid().copy();
        fluid.setAmount(amount);

        sourceTank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
        targetTank.fill(fluid, IFluidHandler.FluidAction.EXECUTE);

        return true;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);

        CompoundTag fluidTag = new CompoundTag();
        cachedFluid.save(provider, fluidTag);

        tag.put("fluid", fluidTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        if (tag.contains("fluid", Tag.TAG_COMPOUND)) {
            cachedFluid = FluidStack.parseOptional(provider, tag.getCompound("fluid"));
        } else {
            cachedFluid = FluidStack.EMPTY;
        }
    }

    public FluidStack getFluidStack() {
        return cachedFluid;
    }

    private void setFluidStack(FluidStack stack) {
        this.cachedFluid = stack;
    }
}