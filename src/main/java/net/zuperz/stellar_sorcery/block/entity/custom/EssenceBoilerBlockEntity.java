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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import net.zuperz.stellar_sorcery.capability.IFluidHandler.IHasFluidTank;
import net.zuperz.stellar_sorcery.component.EssenceBottleData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.item.ModItems;
import org.jetbrains.annotations.Nullable;

public class EssenceBoilerBlockEntity extends BlockEntity implements WorldlyContainer, IHasFluidTank {
    public static final int SLOT_INGREDIENT_1 = 0;
    public static final int SLOT_INGREDIENT_2 = 1;
    public static final int SLOT_INGREDIENT_3 = 2;
    public static final int SLOT_CONTAINER = 3;
    public static final int SLOT_OUTPUT = 4;
    public static final int TOTAL_SLOTS = 5;

    private static final int[] INPUT_SLOTS = new int[] {
            SLOT_INGREDIENT_1, SLOT_INGREDIENT_2, SLOT_INGREDIENT_3, SLOT_CONTAINER
    };
    private static final int[] OUTPUT_SLOTS = new int[] {SLOT_OUTPUT};

    public int progress = 0;
    public int maxProgress = 60;
    public static final int EVENT_WOBBLE = 1;
    public long wobbleStartedAtTick;
    @Nullable
    public WobbleStyle lastWobbleStyle;

    public final ItemStackHandler inventory = new ItemStackHandler(TOTAL_SLOTS) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return slot == SLOT_OUTPUT ? stack.getMaxStackSize() : 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
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
        if (level.isClientSide) {
            return;
        }

        if (boiler.tryCraftAmulet(level, pos, state)) {
            return;
        }

        boolean hasAllIngredients = !boiler.inventory.getStackInSlot(SLOT_INGREDIENT_1).isEmpty()
                && !boiler.inventory.getStackInSlot(SLOT_INGREDIENT_2).isEmpty()
                && !boiler.inventory.getStackInSlot(SLOT_INGREDIENT_3).isEmpty();

        boolean hasBottle = boiler.inventory.getStackInSlot(SLOT_CONTAINER).is(ModItems.EMPTY_ESSENCE_BOTTLE.get());
        boolean hasWater = boiler.getFluidTankAmount() >= 1000
                && boiler.getFluidTank().getFluid().isSame(Fluids.WATER);

        if (boiler.progress > 0 && level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.BUBBLE,
                    pos.getX() + 0.5,
                    pos.getY() + 1,
                    pos.getZ() + 0.5,
                    1,
                    0.2,
                    0.2,
                    0.2,
                    0.0
            );
        }

        if (hasAllIngredients && hasBottle && hasWater) {
            boiler.progress++;
            level.playSound(null, pos, SoundEvents.WATER_AMBIENT, SoundSource.BLOCKS, 0.12f, 0.17f);

            if (boiler.progress >= boiler.maxProgress) {
                ItemStack input0 = boiler.inventory.getStackInSlot(SLOT_INGREDIENT_1).copy();
                ItemStack input1 = boiler.inventory.getStackInSlot(SLOT_INGREDIENT_2).copy();
                ItemStack input2 = boiler.inventory.getStackInSlot(SLOT_INGREDIENT_3).copy();

                ItemStack essenceBottle = new ItemStack(ModItems.ESSENCE_BOTTLE.get());
                essenceBottle.set(
                        ModDataComponentTypes.ESSENCE_BOTTLE.get(),
                        new EssenceBottleData(input0, input1, input2)
                );

                if (!boiler.canOutput(essenceBottle)) {
                    boiler.progress = boiler.maxProgress;
                    boiler.setChanged();
                    level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
                    return;
                }

                boiler.inventory.extractItem(SLOT_INGREDIENT_1, 1, false);
                boiler.inventory.extractItem(SLOT_INGREDIENT_2, 1, false);
                boiler.inventory.extractItem(SLOT_INGREDIENT_3, 1, false);
                boiler.inventory.extractItem(SLOT_CONTAINER, 1, false);
                boiler.drainFluidTank(1000);
                boiler.insertOutput(essenceBottle);

                boiler.progress = 0;
            }
        } else if (boiler.progress != 0) {
            boiler.progress = 0;
        }

        boiler.setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }

    private boolean tryCraftAmulet(Level level, BlockPos pos, BlockState state) {
        int essenceBottleSlot = -1;
        int ghastTearSlot = -1;
        int emptyAmuletSlot = -1;

        for (int slot : INPUT_SLOTS) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }

            if (essenceBottleSlot == -1 && stack.is(ModItems.ESSENCE_BOTTLE.get())) {
                essenceBottleSlot = slot;
            } else if (ghastTearSlot == -1 && stack.is(Items.GHAST_TEAR)) {
                ghastTearSlot = slot;
            } else if (emptyAmuletSlot == -1 && stack.is(ModItems.EMPTY_ESSENCE_AMULET.get())) {
                emptyAmuletSlot = slot;
            }
        }

        if (essenceBottleSlot == -1 || ghastTearSlot == -1 || emptyAmuletSlot == -1) {
            return false;
        }

        ItemStack essenceBottleStack = inventory.getStackInSlot(essenceBottleSlot).copy();
        ItemStack amulet = new ItemStack(ModItems.ESSENCE_AMULET.get());
        if (essenceBottleStack.has(ModDataComponentTypes.ESSENCE_BOTTLE.get())) {
            EssenceBottleData data = essenceBottleStack.get(ModDataComponentTypes.ESSENCE_BOTTLE.get());
            amulet.set(ModDataComponentTypes.ESSENCE_BOTTLE.get(), data);
        }

        if (!canOutput(amulet)) {
            return true;
        }

        inventory.extractItem(essenceBottleSlot, 1, false);
        inventory.extractItem(ghastTearSlot, 1, false);
        inventory.extractItem(emptyAmuletSlot, 1, false);
        insertOutput(amulet);

        progress = 0;
        setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
        return true;
    }

    private boolean canOutput(ItemStack output) {
        ItemStack currentOutput = inventory.getStackInSlot(SLOT_OUTPUT);
        if (currentOutput.isEmpty()) {
            return true;
        }

        if (!ItemStack.isSameItemSameComponents(currentOutput, output)) {
            return false;
        }

        return currentOutput.getCount() + output.getCount() <= currentOutput.getMaxStackSize();
    }

    private void insertOutput(ItemStack output) {
        ItemStack currentOutput = inventory.getStackInSlot(SLOT_OUTPUT);
        if (currentOutput.isEmpty()) {
            inventory.setStackInSlot(SLOT_OUTPUT, output.copy());
            return;
        }

        if (ItemStack.isSameItemSameComponents(currentOutput, output)) {
            currentOutput.grow(output.getCount());
            inventory.setStackInSlot(SLOT_OUTPUT, currentOutput);
        }
    }

    public ItemStackHandler getInputItems() {
        return inventory;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return direction == Direction.DOWN ? OUTPUT_SLOTS : INPUT_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        if (slot == SLOT_OUTPUT || direction == Direction.DOWN) {
            return false;
        }

        if (slot == SLOT_CONTAINER) {
            return inventory.getStackInSlot(slot).isEmpty()
                    && (stack.is(ModItems.EMPTY_ESSENCE_BOTTLE.get()) || stack.is(ModItems.EMPTY_ESSENCE_AMULET.get()));
        }

        if (slot >= SLOT_INGREDIENT_1 && slot <= SLOT_INGREDIENT_3) {
            return inventory.getStackInSlot(slot).isEmpty();
        }

        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack itemStack, Direction direction) {
        return direction == Direction.DOWN && slot == SLOT_OUTPUT;
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
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot >= 0 && slot < inventory.getSlots()) {
            return inventory.getStackInSlot(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot < 0 || slot >= inventory.getSlots()) {
            return;
        }

        inventory.setStackInSlot(slot, stack);
        setChanged();
        if (level != null && !level.isClientSide) {
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
            return inventory.extractItem(slotIndex, count, false);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slotIndex) {
        if (slotIndex < 0 || slotIndex >= inventory.getSlots()) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = inventory.getStackInSlot(slotIndex);
        if (stackInSlot.isEmpty()) {
            return ItemStack.EMPTY;
        }

        inventory.setStackInSlot(slotIndex, ItemStack.EMPTY);
        return stackInSlot;
    }

    @Override
    public boolean stillValid(Player player) {
        final double maxDistance = 64.0;
        double distanceSquared = player.distanceToSqr(
                this.worldPosition.getX() + 0.5,
                this.worldPosition.getY() + 0.5,
                this.worldPosition.getZ() + 0.5
        );
        return distanceSquared <= maxDistance;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventory.setStackInSlot(i, ItemStack.EMPTY);
        }
    }

    public void clearContents() {
        clearContent();
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
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
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

    public enum WobbleStyle {
        POSITIVE(7),
        NEGATIVE(10);

        public final int duration;

        WobbleStyle(int duration) {
            this.duration = duration;
        }
    }

    public void wobble(WobbleStyle style) {
        if (this.level != null && !this.level.isClientSide()) {
            this.level.blockEvent(this.getBlockPos(), this.getBlockState().getBlock(), EVENT_WOBBLE, style.ordinal());
        }
    }

    @Override
    public boolean triggerEvent(int id, int type) {
        if (id == EVENT_WOBBLE && type >= 0 && type < WobbleStyle.values().length) {
            this.wobbleStartedAtTick = this.level.getGameTime();
            this.lastWobbleStyle = WobbleStyle.values()[type];
            return true;
        }
        return super.triggerEvent(id, type);
    }

    @Override
    public IFluidHandler getFluidHandler() {
        return fluidTank;
    }
}
