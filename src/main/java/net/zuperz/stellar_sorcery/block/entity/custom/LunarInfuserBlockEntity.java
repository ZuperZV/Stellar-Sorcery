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
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.InfestedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.Lazy;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zuperz.stellar_sorcery.block.custom.LunarInfuserBlock;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import net.zuperz.stellar_sorcery.recipes.FluidRecipeInput;
import net.zuperz.stellar_sorcery.recipes.ModRecipes;
import net.zuperz.stellar_sorcery.recipes.StarLightLunarInfuserRecipe;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

import static net.zuperz.stellar_sorcery.block.custom.LunarInfuserBlock.DONE;

public class LunarInfuserBlockEntity extends BlockEntity implements WorldlyContainer {
    public int progress = 0;
    public int maxProgress = 80;
    private int prevProgress = 0;

    private List<BeaconBeamSection> beamSections = new java.util.ArrayList<BeaconBeamSection>();

    public List<BeaconBeamSection> getBeamSections() {
        return beamSections;
    }

    public final ItemStackHandler inventory = new ItemStackHandler(1) {
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

    private final FluidTank fluidTank = new FluidTank(4000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    private final Lazy<FluidTank> fluidOptional = Lazy.of(() -> this.fluidTank);

    private float rotation;

    public LunarInfuserBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.LUNAR_INFUSER_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, LunarInfuserBlockEntity altar) {
        altar.beamSections.add(new BeaconBeamSection(0xFF0000));

        BlockState oldState = level.getBlockState(pos);

        oldState = oldState.setValue(DONE, false);

        altar.prevProgress = altar.progress;
        if (altar.hasStarLightRecipe()) {
            altar.progress++;
            setCRAFTING(pos, level, true);
            if (altar.progress >= altar.maxProgress) {
                altar.craftItem();
                oldState = oldState.setValue(DONE, true);
            }
            altar.setChanged();
        } else {
            altar.progress = 0;
            altar.setChanged();
        }

        oldState = oldState.setValue(LunarInfuserBlock.CRAFTING, altar.progress > 0);

        level.setBlockAndUpdate(pos, oldState);

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;

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

    public static void setCRAFTING(BlockPos altarPos, Level level, boolean boo) {
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos checkPos = altarPos.offset(dx, 0, dz);
                BlockState state = level.getBlockState(checkPos);
                BlockEntity be = level.getBlockEntity(checkPos);

                if (be instanceof AstralNexusBlockEntity nexus) {
                    if (boo) {
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

    public boolean hasStarLightRecipe() {
        if (level == null) return false;

        SimpleContainer simpleInventory = new SimpleContainer(inventory.getStackInSlot(0));

        FluidStack fluidInTank =this.fluidTank.getFluidInTank(0);

        Optional<RecipeHolder<StarLightLunarInfuserRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(ModRecipes.STAR_LIGHT_LUNAR_INFUSER_RECIPE_TYPE.get(), getRecipeInput(simpleInventory, fluidInTank), level);

        if (recipe.isEmpty()) return false;

        StarLightLunarInfuserRecipe StarLightRecipe = recipe.get().value();

        maxProgress = StarLightRecipe.recipeTime;
        level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 3);

        return true;
    }

    public void craftItem() {
        Level level = this.level;
        if (level == null) return;

        SimpleContainer simpleInventory = new SimpleContainer(inventory.getStackInSlot(0));

        FluidStack fluidInTank = this.fluidTank.getFluidInTank(0);

        Optional<RecipeHolder<StarLightLunarInfuserRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(ModRecipes.STAR_LIGHT_LUNAR_INFUSER_RECIPE_TYPE.get(), getRecipeInput(simpleInventory, fluidInTank), level);

        if (recipe.isEmpty()) return;

        StarLightLunarInfuserRecipe StarLightrecipe = recipe.get().value();

        fillFluidTank(StarLightrecipe.output);

        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ASH,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.3,
                    worldPosition.getZ() + 0.5,
                    20, // antal partikler
                    0.1, 0.1, 0.1, // spread
                    0.01 // fart
            );

            serverLevel.sendParticles(ParticleTypes.CRIT,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.3,
                    worldPosition.getZ() + 0.5,
                    10, // antal partikler
                    0.1, 0.1, 0.1, // spread
                    0.01 // fart
            );

        level.playSound(null, worldPosition, SoundEvents.ALLAY_HURT,
                SoundSource.BLOCKS, 0.12f, 0.17f);
        level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME,
                SoundSource.BLOCKS, 0.3f, 0.2f);
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


    public ItemStackHandler getInputItems() {
        return inventory;
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
        tag.putInt("prevProgress", prevProgress);

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