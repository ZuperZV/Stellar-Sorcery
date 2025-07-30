package net.zuperz.stellar_sorcery.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
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
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zuperz.stellar_sorcery.block.custom.VitalStumpBlock;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import net.zuperz.stellar_sorcery.recipes.StumpRecipe;
import net.zuperz.stellar_sorcery.recipes.ModRecipes;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static net.zuperz.stellar_sorcery.block.custom.VitalStumpBlock.DONE;

public class VitalStumpBlockEntity extends BlockEntity implements WorldlyContainer {
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
            if (!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    private float rotation;

    public VitalStumpBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.VITAL_STUMP_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, VitalStumpBlockEntity altar) {
        BlockState oldState = level.getBlockState(pos);

        oldState = oldState.setValue(DONE, false);

        altar.prevProgress = altar.progress;
        if (altar.hasRecipe()) {
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

        oldState = oldState.setValue(VitalStumpBlock.CRAFTING, altar.progress > 0);

        level.setBlockAndUpdate(pos, oldState);

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos checkPos = pos.offset(dx, 0, dz);
                BlockEntity be = level.getBlockEntity(checkPos);
                if (be instanceof StumpBlockEntity nexus) {
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

                if (be instanceof StumpBlockEntity nexus) {
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

    public boolean hasRecipe() {
        if (level == null) return false;

        Optional<RecipeHolder<StumpRecipe>> recipeOpt = level.getRecipeManager()
                .getRecipeFor(ModRecipes.STUMP_RECIPE_TYPE.get(), new BlockRecipeInput(inventory.getStackInSlot(0), worldPosition), level);

        if (recipeOpt.isEmpty()) return false;

        StumpRecipe altarRecipe = recipeOpt.get().value();
        ItemStack inputStack = inventory.getStackInSlot(0);

        if (!altarRecipe.moldIngredient.test(inputStack)) return false;

        List<Ingredient> ingredientsToMatch = altarRecipe.additionalIngredients.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        boolean allMatched = true;

        for (Ingredient ingredient : ingredientsToMatch) {
            boolean matched = false;

            outer:
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dz == 0) continue;

                    BlockPos checkPos = worldPosition.offset(dx, 0, dz);
                    BlockEntity be = level.getBlockEntity(checkPos);

                    if (!(be instanceof StumpBlockEntity nexus)) continue;

                    for (int slot = 0; slot < nexus.inventory.getSlots(); slot++) {
                        ItemStack stack = nexus.inventory.getStackInSlot(slot);
                        if (!stack.isEmpty() && ingredient.test(stack)) {
                            matched = true;
                            break outer;
                        }
                    }
                }
            }

            if (!matched) {
                allMatched = false;
                break;
            }
        }

        if (allMatched) {
            maxProgress = altarRecipe.recipeTime;
            level.sendBlockUpdated(worldPosition, level.getBlockState(worldPosition), level.getBlockState(worldPosition), 3);
        }

        return allMatched;
    }

    public void craftItem() {
        Level level = this.level;
        if (level == null) return;

        SimpleContainer inventoryContainer = new SimpleContainer(inventory.getSlots());
        for (int i = 0; i < inventory.getSlots(); i++) {
            inventoryContainer.setItem(i, inventory.getStackInSlot(i));
        }

        Optional<RecipeHolder<StumpRecipe>> recipe = this.level.getRecipeManager()
                .getRecipeFor(ModRecipes.STUMP_RECIPE_TYPE.get(), new BlockRecipeInput(inventory.getStackInSlot(0), worldPosition), level);

        if (recipe.isEmpty()) return;

        StumpRecipe altarRecipe = recipe.get().value();
        ItemStack inputStack = inventory.getStackInSlot(0);

        if (!altarRecipe.moldIngredient.test(inputStack)) return;

        Map<Ingredient, MatchedItem> matchedIngredientSources = new HashMap<>();
        List<Ingredient> ingredientsToMatch = altarRecipe.additionalIngredients.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        boolean allMatched = true;

        for (Ingredient ingredient : ingredientsToMatch) {
            boolean matched = false;

            outer:
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if (dx == 0 && dz == 0) continue;

                    BlockPos checkPos = worldPosition.offset(dx, 0, dz);
                    BlockEntity be = level.getBlockEntity(checkPos);

                    if (!(be instanceof StumpBlockEntity nexus)) continue;

                    for (int slot = 0; slot < nexus.inventory.getSlots(); slot++) {
                        ItemStack stack = nexus.inventory.getStackInSlot(slot);
                        if (!stack.isEmpty() && ingredient.test(stack)) {
                            matchedIngredientSources.put(ingredient, new MatchedItem(nexus, slot));
                            matched = true;
                            break outer;
                        }
                    }
                }
            }

            if (!matched) {
                allMatched = false;
                break;
            }
        }

        if (allMatched) {
            inventory.extractItem(0, 1, false);
            for (MatchedItem matched : matchedIngredientSources.values()) {
                matched.nexus.inventory.extractItem(matched.slot, 1, false);
            }
            inventory.setStackInSlot(0, altarRecipe.output.copy());

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
            }

            level.playSound(null, worldPosition, SoundEvents.ALLAY_HURT,
                    SoundSource.BLOCKS, 0.12f, 0.17f);
            level.playSound(null, worldPosition, SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.BLOCKS, 0.3f, 0.2f);

            if (altarRecipe.additionalBlock.isPresent() && altarRecipe.blockOutput.isPresent()) {
                Block requiredBlock = altarRecipe.additionalBlock.get();
                Block newBlock = altarRecipe.blockOutput.get();

                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        if (dx == 0 && dz == 0) continue;

                        BlockPos checkPos = worldPosition.offset(dx, 0, dz);
                        Block blockAt = level.getBlockState(checkPos).getBlock();
                        boolean matchesState = true;

                        BlockState stateAt = level.getBlockState(checkPos);
                        if (stateAt.getBlock().equals(requiredBlock)) {

                            if (altarRecipe.blockState.isPresent()) {
                                Map<String, String> requiredStates = altarRecipe.blockState.get();

                                for (Map.Entry<String, String> entry : requiredStates.entrySet()) {
                                    Property<?> property = stateAt.getBlock().getStateDefinition().getProperty(entry.getKey());

                                    if (property == null) {
                                        matchesState = false;
                                        break;
                                    }

                                    Optional<? extends Comparable<?>> parsed = property.getValue(entry.getValue());
                                    if (parsed.isEmpty() || !stateAt.getValue(property).equals(parsed.get())) {
                                        matchesState = false;
                                        break;
                                    }
                                }
                            }

                            if (matchesState) {
                                level.setBlockAndUpdate(checkPos, newBlock.defaultBlockState());

                                if (level instanceof ServerLevel serverLevel) {
                                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                                            checkPos.getX() + 0.5,
                                            checkPos.getY() + 0.5,
                                            checkPos.getZ() + 0.5,
                                            20,
                                            0.3, 0.3, 0.3,
                                            0.01
                                    );
                                }

                                level.playSound(null, checkPos, SoundEvents.AMETHYST_BLOCK_CHIME,
                                        SoundSource.BLOCKS, 1.0f, 1.2f);
                            }

                            if (matchesState) {
                                level.setBlockAndUpdate(checkPos, newBlock.defaultBlockState());

                                if (level instanceof ServerLevel serverLevel) {
                                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                                            checkPos.getX() + 0.5,
                                            checkPos.getY() + 0.5,
                                            checkPos.getZ() + 0.5,
                                            20,
                                            0.3, 0.3, 0.3,
                                            0.01
                                    );
                                }

                                level.playSound(null, checkPos, SoundEvents.AMETHYST_BLOCK_CHIME,
                                        SoundSource.BLOCKS, 1.0f, 1.2f);
                            }
                        }
                    }
                }
            }
        }
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
        public final StumpBlockEntity nexus;
        public final int slot;

        public MatchedItem(StumpBlockEntity nexus, int slot) {
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
        tag.putInt("prevProgress", prevProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        progress = tag.getInt("progress");
        prevProgress = tag.getInt("prevProgress");
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
}