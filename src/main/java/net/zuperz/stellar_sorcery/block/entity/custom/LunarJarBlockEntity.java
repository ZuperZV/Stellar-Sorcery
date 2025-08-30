package net.zuperz.stellar_sorcery.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class LunarJarBlockEntity extends BlockEntity {
    private List<BeaconBeamSection> beamSections = new java.util.ArrayList<BeaconBeamSection>();

    public List<BeaconBeamSection> getBeamSections() {
        return beamSections;
    }

    private final FluidTank fluidTank = new FluidTank(10000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final Lazy<FluidTank> fluidOptional = Lazy.of(() -> this.fluidTank);

    private float rotation;

    public LunarJarBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LUNAR_JAR_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LunarJarBlockEntity altar) {
        altar.beamSections.add(new BeaconBeamSection(0xFF0000));

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos checkPos = pos.offset(dx, 0, dz);
                BlockEntity be = level.getBlockEntity(checkPos);
                if (be instanceof AstralNexusBlockEntity nexus) {
                    nexus.setSavedPos(pos);

                    level.sendBlockUpdated(nexus.getBlockPos(), nexus.getBlockState(), nexus.getBlockState(), 3);
                }
            }
        }
    }

    public Vec3 getStartPosition() {
        BlockPos pos = this.worldPosition;
        return new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }

    public Vec3 getEndPosition() {
        BlockPos pos = this.worldPosition;
        return new Vec3(pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5);
    }

    private void markForUpdate() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    private static class MatchedItem {
        public final AstralNexusBlockEntity nexus;
        public final int slot;

        public MatchedItem(AstralNexusBlockEntity nexus, int slot) {
            this.nexus = nexus;
            this.slot = slot;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        CompoundTag fluidTankTag = new CompoundTag();
        this.fluidTank.writeToNBT(registries, fluidTankTag);
        tag.put("FluidTank", fluidTankTag);

        CompoundTag beamsTag = new CompoundTag();
        beamsTag.putInt("size", beamSections.size());
        for (int i = 0; i < beamSections.size(); i++) {
            CompoundTag sectionTag = new CompoundTag();
            sectionTag.putInt("color", beamSections.get(i).getColor());
            sectionTag.putInt("height", beamSections.get(i).getHeight());
            beamsTag.put("section" + i, sectionTag);
        }
        tag.put("beamSections", beamsTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("FluidTank", Tag.TAG_COMPOUND)) {
            this.fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
        }

        beamSections.clear();
        if (tag.contains("beamSections", Tag.TAG_COMPOUND)) {
            CompoundTag beamsTag = tag.getCompound("beamSections");
            int size = beamsTag.contains("size", Tag.TAG_INT) ? beamsTag.getInt("size") : 0;

            for (int i = 0; i < size; i++) {
                String sectionKey = "section" + i;
                if (beamsTag.contains(sectionKey, Tag.TAG_COMPOUND)) {
                    CompoundTag sectionTag = beamsTag.getCompound(sectionKey);

                    if (sectionTag.contains("color", Tag.TAG_INT)) {
                        int color = sectionTag.getInt("color");

                        BeaconBeamSection section = new BeaconBeamSection(color);
                        if (sectionTag.contains("height", Tag.TAG_INT)) {
                            int height = sectionTag.getInt("height");
                            for (int h = 1; h < height; h++) {
                                section.increaseHeight();
                            }
                        }
                        beamSections.add(section);
                    }
                }
            }
        }
    }

    public FluidStack getFluidTank() {
        return this.fluidTank.getFluid();
    }

    public int getFluidTankAmount() {
        return this.fluidTank.getFluidAmount();
    }

    public int getFluidTankCapacity() {
        return this.fluidTank.getCapacity();
    }

    public void fillFluidTank(FluidStack fluid) {
        fluidTank.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
    }

    public void drainFluidTank(int amount) {
        fluidTank.drain(amount, IFluidHandler.FluidAction.EXECUTE);
    }

    public float getRenderingRotation() {
        rotation += 0.5f;
        if (rotation >= 360) {
            rotation = 0;
        }
        return rotation;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return saveWithoutMetadata(pRegistries);
    }

    public static class BeaconBeamSection {
        private final int color;
        private int height;

        public BeaconBeamSection(int color) {
            this.color = color;
            this.height = 1;
        }

        public void increaseHeight() {
            this.height++;
        }

        public int getColor() {
            return this.color;
        }

        public int getHeight() {
            return this.height;
        }
    }
}