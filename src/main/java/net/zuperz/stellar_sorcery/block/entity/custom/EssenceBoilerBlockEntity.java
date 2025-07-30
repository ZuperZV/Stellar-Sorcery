package net.zuperz.stellar_sorcery.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandlerItem;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import net.zuperz.stellar_sorcery.component.EssenceBottleData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.item.ModItems;
import org.jetbrains.annotations.Nullable;


public class EssenceBoilerBlockEntity extends BlockEntity implements WorldlyContainer {
    public int progress = 0;
    public int maxProgress = 60;
    public int splashTicks = 0;

    public final ItemStackHandler inventory = new ItemStackHandler(3) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final FluidTank fluidTank = new FluidTank(1000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final Lazy<FluidTank> fluidOptional = Lazy.of(() -> this.fluidTank);

    private float rotation;

    public EssenceBoilerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ESSENCE_BOILER_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, EssenceBoilerBlockEntity boiler) {
        if (level.isClientSide) return;
        if (boiler.splashTicks > 0) boiler.splashTicks--;

        boolean hasAllItems = true;

        for (int i = 0; i < 3; i++) {
            if (boiler.inventory.getStackInSlot(i).isEmpty()) {
                hasAllItems = false;
                break;
            }
        }

        if (hasAllItems && boiler.getFluidTankAmount() > 0 && boiler.getFluidTank().getFluid().isSame(Fluids.WATER)) {
            boiler.progress++;

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.BUBBLE,
                        pos.getX() + 0.5,
                        pos.getY() + 0.8,
                        pos.getZ() + 0.5,
                        1, // antal partikler
                        0.2, 0.2, 0.2, // spread
                        1f // fart
                );
            }

            level.playSound(null, pos, SoundEvents.WATER_AMBIENT,
                    SoundSource.BLOCKS, 0.12f, 0.17f);

            if (boiler.progress >= boiler.maxProgress) {
                ItemStack input0 = boiler.inventory.getStackInSlot(0).copy();
                ItemStack input1 = boiler.inventory.getStackInSlot(1).copy();
                ItemStack input2 = boiler.inventory.getStackInSlot(2).copy();

                for (int i = 0; i < 3; i++) {
                    boiler.inventory.setStackInSlot(i, ItemStack.EMPTY);
                }

                ItemStack essensBottle = new ItemStack(ModItems.ESSENCE_BOTTLE.get());

                essensBottle.set(ModDataComponentTypes.ESSENCE_BOTTLE.get(), new EssenceBottleData(input0, input1, input2));

                boiler.drainFluidTank(1000);

                Containers.dropItemStack(level, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, essensBottle);

                boiler.progress = 0;
            }


        } else {
            if (boiler.progress != 0) {
                boiler.progress = 0;
            }
        }

        boiler.setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }
    private RecipeInput getRecipeInput(SimpleContainer inventory) {
        return new RecipeInput() {
            @Override
            public ItemStack getItem(int index) {
                return inventory.getItem(index).copy();
            }

            @Override
            public int size() {
                return inventory.getContainerSize();
            }
        };
    }

    public ItemStackHandler getInputItems() {
        return inventory;
    }

    public void triggerSplash() {
        if (level == null || worldPosition == null) return;

        this.splashTicks = 10;
        var state = level.getBlockState(worldPosition);
        level.sendBlockUpdated(worldPosition, state, state, 3);
    }

    public int getSplashTicks() {
        return this.splashTicks;
    }

    @Override
    public int[] getSlotsForFace(Direction p_58363_) {
        if (p_58363_ == Direction.DOWN) {
            return new int[]{0};
        } else {
            return p_58363_ == Direction.UP ? new int[]{0} : new int[]{0};
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        if (slot == 0) {
            return inventory.getStackInSlot(0).isEmpty();
        }
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack itemStack, Direction direction) {
        return direction == Direction.DOWN && slot == 0 && progress >= maxProgress;
    }

    @Override
    public int getContainerSize() {
        return inventory.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        for (int i = 0; i < inventory.getSlots(); i++) {
            if (!inventory.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int pSlot) {
        if (pSlot < 4) {
            return inventory.getStackInSlot(pSlot);
        } else {
            return inventory.getStackInSlot(pSlot - 4);
        }
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 4) {
            inventory.setStackInSlot(slot, stack);
        }
        setChanged();
        if (!level.isClientSide) {
            markForUpdate();
        }
    }

    private void markForUpdate() {
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public ItemStack removeItem(int slotIndex, int count) {
        if (slotIndex >= 0 && slotIndex < inventory.getSlots()) {
            if (progress >= maxProgress) {
                return inventory.extractItem(slotIndex, count, false);
            }
        }
        return ItemStack.EMPTY;
    }


    @Override
    public ItemStack removeItemNoUpdate(int slotIndex) {
        if (slotIndex >= 0 && slotIndex < inventory.getSlots()) {
            ItemStack stackInSlot = inventory.getStackInSlot(slotIndex);

            if (!stackInSlot.isEmpty()) {
                inventory.setStackInSlot(slotIndex, ItemStack.EMPTY);
                return stackInSlot;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        final double MAX_DISTANCE = 64.0;
        double distanceSquared = player.distanceToSqr(this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 0.5,
                this.worldPosition.getZ() + 0.5);
        return distanceSquared <= MAX_DISTANCE;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
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

    public static class BlockRecipeInput implements RecipeInput {
        private final ItemStack stack;
        private final BlockPos pos;

        public BlockRecipeInput(ItemStack stack, BlockPos pos) {
            this.stack = stack;
            this.pos = pos;
        }

        @Override
        public ItemStack getItem(int pIndex) {
            return stack;
        }

        @Override
        public int size() {
            return 1;
        }

        public ItemStack stack() {
            return stack;
        }

        public BlockPos pos() {
            return pos;
        }
    }


    public void clearContents() {
        inventory.setStackInSlot(0, ItemStack.EMPTY);
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            inv.setItem(i, inventory.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("progress", progress);

        CompoundTag fluidTankTag = new CompoundTag();
        this.fluidTank.writeToNBT(registries, fluidTankTag);
        tag.put("FluidTank", fluidTankTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        progress = tag.getInt("progress");

        if (tag.contains("FluidTank", Tag.TAG_COMPOUND)) {
            this.fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
        }
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

    public Lazy<FluidTank> getFluidOptional() {
        return this.fluidOptional;
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

    public FluidStack extractFluid(Direction direction, int amount, boolean simulate) {
        return fluidTank.drain(amount, simulate ? IFluidHandler.FluidAction.SIMULATE : IFluidHandler.FluidAction.EXECUTE);
    }

    public void receiveFluid(Direction direction, FluidStack fluid) {
        if (fluid.isEmpty()) {
            return;
        }

        fluidTank.fill(fluid, IFluidHandler.FluidAction.EXECUTE);
    }

}