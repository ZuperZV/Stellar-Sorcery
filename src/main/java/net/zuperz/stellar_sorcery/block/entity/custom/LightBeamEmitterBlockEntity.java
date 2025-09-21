package net.zuperz.stellar_sorcery.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.block.custom.LightBeamEmitterBlock;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import net.zuperz.stellar_sorcery.capability.IFluidHandler.IHasFluidTank;
import net.zuperz.stellar_sorcery.fluid.ModFluids;

public class LightBeamEmitterBlockEntity extends BlockEntity {
    public int beamLength = 0;
    public boolean needsToBeNoctilume = false;
    public int beamTicksRemaining = 0;
    private static final int MAX_BEAM_TICKS = 40;

    public LightBeamEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(state.getBlock() == ModBlocks.LUNAR_LIGHT_BEAM_EMITTER.get()
                ? ModBlockEntities.LUNAR_LIGHT_BEAM_EMITTER_BE.get()
                : ModBlockEntities.LIGHT_BEAM_EMITTER_BE.get(), pos, state);
        this.needsToBeNoctilume = state.getBlock() == ModBlocks.LUNAR_LIGHT_BEAM_EMITTER.get();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LightBeamEmitterBlockEntity be) {
        if (level.isClientSide) return;

        Direction facing = state.getValue(LightBeamEmitterBlock.FACING);
        BlockPos inputPos = pos.relative(facing.getOpposite());
        BlockEntity inputBE = level.getBlockEntity(inputPos);

        if (!(inputBE instanceof IHasFluidTank inputTank)) {
            if (be.beamLength > 0) {
                be.beamTicksRemaining--;
                if (be.beamTicksRemaining <= 0) {
                    level.sendBlockUpdated(pos, state, state, 3);
                }
            }
            return;
        }

        IFluidHandler inputHandler = inputTank.getFluidHandler();

        FluidStack requiredFluid = be.needsToBeNoctilume
                ? new FluidStack(ModFluids.SOURCE_NOCTILUME.get(), 100)
                : inputHandler.drain(100, IFluidHandler.FluidAction.SIMULATE);

        if (requiredFluid.isEmpty()) {
            be.beamTicksRemaining--;
            if (be.beamTicksRemaining <= 0) {
                level.sendBlockUpdated(pos, state, state, 3);
            }
            return;
        }

        be.beamTicksRemaining = MAX_BEAM_TICKS;
        be.shootBeam(level, pos, facing, inputHandler, inputBE, pos, state, requiredFluid);
    }

    private void shootBeam(Level level, BlockPos start, Direction dir,
                           IFluidHandler inputHandler, BlockEntity inputBE, BlockPos pos,
                           BlockState state, FluidStack requiredFluid) {
        BlockPos.MutableBlockPos current = start.mutable();
        FluidStack drainedSim = inputHandler.drain(requiredFluid, IFluidHandler.FluidAction.SIMULATE);

        BlockEntity targetBE = null;

        this.beamLength = 0;

        for (int i = 1; i < 32; i++) {
            current.move(dir);
            this.beamLength++;
            level.sendBlockUpdated(pos, state, state, 3);
            BlockState stateAt = level.getBlockState(current);
            if (!stateAt.isAir()) {
                BlockEntity beAt = level.getBlockEntity(current);
                if (beAt instanceof IHasFluidTank) {
                    targetBE = beAt;
                }
                break;
            }
        }

        if (targetBE instanceof IHasFluidTank outputTank) {
            IFluidHandler outputHandler = outputTank.getFluidHandler();
            int filled = outputHandler.fill(drainedSim, IFluidHandler.FluidAction.EXECUTE);
            if (filled > 0) {
                inputHandler.drain(new FluidStack(drainedSim.getFluid(), filled), IFluidHandler.FluidAction.EXECUTE);
                updateBlock(level, inputBE.getBlockPos(), inputBE.getBlockState(), inputBE);
                updateBlock(level, targetBE.getBlockPos(), targetBE.getBlockState(), targetBE);
            }
        }
    }

    private void updateBlock(Level level, BlockPos pos, BlockState state, BlockEntity be) {
        be.setChanged();
        level.sendBlockUpdated(pos, state, state, 3);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider provider) {
        CompoundTag tag = super.getUpdateTag(provider);
        tag.putInt("BeamLength", this.beamLength);
        tag.putInt("beamTicksRemaining", this.beamTicksRemaining);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.saveAdditional(tag, provider);
        tag.putBoolean("NeedsNoctilume", this.needsToBeNoctilume);
        tag.putInt("BeamLength", this.beamLength);
        tag.putInt("beamTicksRemaining", this.beamTicksRemaining);
        if (tag.contains("beamTicksRemaining")) {
            this.beamTicksRemaining = tag.getInt("beamTicksRemaining");
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider provider) {
        super.loadAdditional(tag, provider);
        this.needsToBeNoctilume = tag.getBoolean("NeedsNoctilume");
        if (tag.contains("BeamLength")) {
            this.beamLength = tag.getInt("BeamLength");
        }
        if (tag.contains("beamTicksRemaining")) {
            this.beamTicksRemaining = tag.getInt("beamTicksRemaining");
        }
    }
}
