package net.zuperz.stellar_sorcery.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
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

    private final FluidTank visualTank = new FluidTank(1000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final Lazy<FluidTank> fluidOptional = Lazy.of(() -> this.visualTank);

    public boolean extractFluid = false;

    public static void tick(Level level, BlockPos pos, BlockState state, BoilerTipBlockEntity tip) {

        if (level.isClientSide) return;

        boolean powered = level.hasNeighborSignal(pos);

        Direction facing = state.getValue(BlockStateProperties.FACING);
        BlockPos behindPos = pos.relative(facing.getOpposite());

        BlockEntity sourceBE = level.getBlockEntity(behindPos);

        if (sourceBE instanceof EssenceBoilerBlockEntity sourceBoiler) {

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
                        } else {

                            tip.fluidOptional.get().setFluid(sourceBoiler.getTank().getFluid().copy());
                            tip.setChanged();

                            if (level != null) {
                                level.sendBlockUpdated(pos, state, state, 3);
                            }

                            level.sendBlockUpdated(
                                    pos,
                                    state,
                                    state,
                                    3
                            );
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

        } else {
            tip.extractFluid = false;
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

        CompoundTag tankTag = new CompoundTag();
        visualTank.writeToNBT(provider, tankTag);
        tag.put("visualTank", tankTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);

        if (tag.contains("visualTank", Tag.TAG_COMPOUND)) {
            visualTank.readFromNBT(provider, tag.getCompound("visualTank"));
        } else {
            visualTank.setFluid(FluidStack.EMPTY);
        }
    }

    public FluidStack getFluidStack() {
        return this.visualTank.getFluid();
    }


    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        saveAdditional(tag, provider);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        loadAdditional(tag, provider);
    }
}
