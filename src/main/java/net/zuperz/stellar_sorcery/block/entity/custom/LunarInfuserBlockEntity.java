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
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.block.custom.LunarInfuserBlock;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import net.zuperz.stellar_sorcery.capability.IFluidHandler.IHasFluidTank;
import net.zuperz.stellar_sorcery.recipes.FluidRecipeInput;
import net.zuperz.stellar_sorcery.recipes.ModRecipes;
import net.zuperz.stellar_sorcery.recipes.StarLightLunarInfuserRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static net.zuperz.stellar_sorcery.block.custom.LunarInfuserBlock.DONE;

public class LunarInfuserBlockEntity extends BlockEntity implements WorldlyContainer, IHasFluidTank {
    public static final int SLOT_INPUT = 0;
    private static final int[] INPUT_SLOT = new int[] {SLOT_INPUT};

    public int progress = 0;
    public int maxProgress = 80;
    private int prevProgress = 0;

    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null && !level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final FluidTank fluidTank = new FluidTank(4000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private float rotation;

    public LunarInfuserBlockEntity(BlockPos pos, BlockState state) {
        super(
                state.getBlock() == ModBlocks.LUNAR_INFUSER.get()
                        ? ModBlockEntities.LUNAR_INFUSER_BE.get()
                        : ModBlockEntities.LIGHT_INFUSER_BE.get(),
                pos,
                state
        );
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LunarInfuserBlockEntity altar) {
        BlockState oldState = level.getBlockState(pos).setValue(DONE, false);

        altar.prevProgress = altar.progress;
        if (altar.hasStarLightRecipe()) {
            altar.progress++;
            setCRAFTING(pos, level, true);

            if (altar.progress >= altar.maxProgress) {
                if (altar.craftItem()) {
                    oldState = oldState.setValue(DONE, true);
                } else {
                    altar.progress = altar.maxProgress;
                }
            }
            altar.setChanged();
        } else {
            altar.progress = 0;
            setCRAFTING(pos, level, false);
            altar.setChanged();
        }

        oldState = oldState.setValue(LunarInfuserBlock.CRAFTING, altar.progress > 0);
        level.setBlockAndUpdate(pos, oldState);

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }

                BlockPos checkPos = pos.offset(dx, 0, dz);
                BlockEntity be = level.getBlockEntity(checkPos);
                if (be instanceof AstralNexusBlockEntity nexus) {
                    nexus.setSavedPos(pos);
                    nexus.progress = altar.progress;
                    nexus.maxProgress = altar.maxProgress;
                    nexus.setChanged();
                    level.sendBlockUpdated(nexus.getBlockPos(), nexus.getBlockState(), nexus.getBlockState(), 3);
                }
            }
        }
    }

    public static void setCRAFTING(BlockPos altarPos, Level level, boolean crafting) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }

                BlockPos checkPos = altarPos.offset(dx, 0, dz);
                BlockEntity be = level.getBlockEntity(checkPos);

                if (be instanceof AstralNexusBlockEntity nexus) {
                    if (crafting) {
                        if (nexus.craftingStartTime == -1) {
                            nexus.craftingStartTime = level.getGameTime();
                            nexus.setChanged();
                        }
                    } else {
                        nexus.craftingStartTime = -1;
                        nexus.setChanged();
                    }
                }
            }
        }
    }

    private FluidRecipeInput getRecipeInput(SimpleContainer inventory, FluidStack fluidStack) {
        return new FluidRecipeInput(fluidStack) {
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

    private Optional<RecipeHolder<StarLightLunarInfuserRecipe>> getCurrentRecipe() {
        if (level == null) {
            return Optional.empty();
        }

        SimpleContainer simpleInventory = new SimpleContainer(inventory.getStackInSlot(SLOT_INPUT));
        FluidStack fluidInTank = this.fluidTank.getFluidInTank(0);
        return level.getRecipeManager().getRecipeFor(
                ModRecipes.STAR_LIGHT_LUNAR_INFUSER_RECIPE_TYPE.get(),
                getRecipeInput(simpleInventory, fluidInTank),
                level
        );
    }

    private boolean canAcceptFluidOutput(FluidStack output) {
        if (output.isEmpty()) {
            return false;
        }

        FluidStack tankFluid = fluidTank.getFluid();
        if (!tankFluid.isEmpty() && !tankFluid.getFluid().isSame(output.getFluid())) {
            return false;
        }

        return fluidTank.getFluidAmount() + output.getAmount() <= fluidTank.getCapacity();
    }

    public boolean hasStarLightRecipe() {
        if (level == null) {
            return false;
        }

        Optional<RecipeHolder<StarLightLunarInfuserRecipe>> recipeOpt = getCurrentRecipe();
        if (recipeOpt.isEmpty()) {
            return false;
        }

        StarLightLunarInfuserRecipe recipe = recipeOpt.get().value();
        if (!canAcceptFluidOutput(recipe.output)) {
            return false;
        }

        maxProgress = recipe.recipeTime;
        level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 3);
        return true;
    }

    public boolean craftItem() {
        if (level == null) {
            return false;
        }

        Optional<RecipeHolder<StarLightLunarInfuserRecipe>> recipeOpt = getCurrentRecipe();
        if (recipeOpt.isEmpty()) {
            return false;
        }

        StarLightLunarInfuserRecipe recipe = recipeOpt.get().value();
        FluidStack output = recipe.output.copy();
        if (!canAcceptFluidOutput(output)) {
            return false;
        }

        inventory.extractItem(SLOT_INPUT, 1, false);
        fillFluidTank(output);
        progress = 0;

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.ASH,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.3,
                    worldPosition.getZ() + 0.5,
                    1,
                    0.1,
                    0.1,
                    0.1,
                    0.01
            );

            serverLevel.sendParticles(
                    ParticleTypes.CRIT,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.3,
                    worldPosition.getZ() + 0.5,
                    1,
                    0.1,
                    0.1,
                    0.1,
                    0.01
            );
        }

        return true;
    }

    public Vec3 getStartPosition() {
        BlockPos pos = this.worldPosition;
        return new Vec3(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
    }

    public Vec3 getEndPosition() {
        BlockPos pos = this.worldPosition;
        return new Vec3(pos.getX() + 0.5, pos.getY() + 2.0, pos.getZ() + 0.5);
    }

    public ItemStackHandler getInputItems() {
        return inventory;
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return INPUT_SLOT;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return slot == SLOT_INPUT && inventory.getStackInSlot(SLOT_INPUT).isEmpty();
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack itemStack, Direction direction) {
        return direction == Direction.DOWN && slot == SLOT_INPUT && progress <= 0;
    }

    @Override
    public int getContainerSize() {
        return inventory.getSlots();
    }

    @Override
    public boolean isEmpty() {
        return inventory.getStackInSlot(SLOT_INPUT).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        if (slot == SLOT_INPUT) {
            return inventory.getStackInSlot(SLOT_INPUT);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot != SLOT_INPUT) {
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
        if (slotIndex == SLOT_INPUT && progress <= 0) {
            return inventory.extractItem(SLOT_INPUT, count, false);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slotIndex) {
        if (slotIndex != SLOT_INPUT) {
            return ItemStack.EMPTY;
        }

        ItemStack stackInSlot = inventory.getStackInSlot(SLOT_INPUT);
        if (stackInSlot.isEmpty()) {
            return ItemStack.EMPTY;
        }

        inventory.setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
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
        inventory.setStackInSlot(SLOT_INPUT, ItemStack.EMPTY);
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
        tag.putInt("prevProgress", prevProgress);

        CompoundTag fluidTankTag = new CompoundTag();
        this.fluidTank.writeToNBT(registries, fluidTankTag);
        tag.put("FluidTank", fluidTankTag);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory", Tag.TAG_COMPOUND)) {
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        if (tag.contains("progress", Tag.TAG_INT)) {
            progress = tag.getInt("progress");
        }
        if (tag.contains("prevProgress", Tag.TAG_INT)) {
            prevProgress = tag.getInt("prevProgress");
        }
        if (tag.contains("FluidTank", Tag.TAG_COMPOUND)) {
            this.fluidTank.readFromNBT(registries, tag.getCompound("FluidTank"));
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
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    public float getSmoothProgress(float partialTicks) {
        if (maxProgress <= 0) {
            return 0f;
        }
        float interpolated = Mth.lerp(partialTicks, prevProgress, progress);
        return Mth.clamp(interpolated / maxProgress, 0f, 1f);
    }

    @Override
    public IFluidHandler getFluidHandler() {
        return fluidTank;
    }
}
