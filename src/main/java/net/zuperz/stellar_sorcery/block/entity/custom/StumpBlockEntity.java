package net.zuperz.stellar_sorcery.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import net.zuperz.stellar_sorcery.recipes.StumpRecipe;
import net.zuperz.stellar_sorcery.recipes.ModRecipes;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class StumpBlockEntity extends BlockEntity implements WorldlyContainer {
    public long craftingStartTime = -1;
    public long animationStartTime = 1;
    public float clientProgress = 0f;
    public int progress = 0;
    public int maxProgress = 80;

    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected int getStackLimit(int slot, ItemStack stack) {
            return 1;
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };
    private float rotation;
    private BlockPos savedPos;

    public static void tickClient(Level level, BlockPos pos, BlockState state, StumpBlockEntity blockEntity) {
        if (level.isClientSide) {
            blockEntity.clientProgress = blockEntity.progress;
        }

        ItemStack currentStack = blockEntity.inventory.getStackInSlot(0);

        if (!currentStack.isEmpty() && blockEntity.progress > 0) {
            if (blockEntity.animationStartTime == 1) {
                blockEntity.animationStartTime = -1;
            }

            long elapsedTicks = level.getGameTime() - blockEntity.animationStartTime;
            if (elapsedTicks >= blockEntity.maxProgress) {
                blockEntity.animationStartTime = 1;
            }
        } else {
            blockEntity.animationStartTime = 1;
        }
    }

    public ItemStackHandler getInputItems() {
        return inventory;
    }

    public static void tickServer(Level level, BlockPos pos, BlockState state, StumpBlockEntity blockEntity) {
    }

    public void setSavedPos(BlockPos pos) {
        this.savedPos = pos;
        setChanged();

        if (!level.isClientSide()) {
            level.sendBlockUpdated(this.getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }

    public BlockPos getSavedPos() {
        return savedPos;
    }


    public StumpBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.STUMP_BE.get(), pPos, pBlockState);
    }

    public void clearContents() {
        inventory.setStackInSlot(0, ItemStack.EMPTY);
    }

    public void drops() {
        SimpleContainer inv = new SimpleContainer(inventory.getSlots());
        for(int i = 0; i < inventory.getSlots(); i++) {
            inv.setItem(i, inventory.getStackInSlot(i));
        }

        Containers.dropContents(this.level, this.worldPosition, inv);
    }

    @Override
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        pTag.put("inventory", inventory.serializeNBT(pRegistries));

        pTag.putInt("progress", progress);
        pTag.putInt("maxProgress", maxProgress);

        if (savedPos != null) {
            pTag.putInt("SavedX", savedPos.getX());
            pTag.putInt("SavedY", savedPos.getY());
            pTag.putInt("SavedZ", savedPos.getZ());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        inventory.deserializeNBT(pRegistries, pTag.getCompound("inventory"));

        progress = pTag.getInt("progress");
        maxProgress = pTag.getInt("maxProgress");

        if (pTag.contains("SavedX") && pTag.contains("SavedY") && pTag.contains("SavedZ")) {
            savedPos = new BlockPos(pTag.getInt("SavedX"), pTag.getInt("SavedY"), pTag.getInt("SavedZ"));
        }
    }

    public float getRenderingRotation() {
        rotation += 0.5f;
        if(rotation >= 360) {
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

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider provider) {
        this.loadAdditional(tag, provider);
    }

    @Override
    public int[] getSlotsForFace(Direction direction) {
        return new int[]{0};
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        if (slot == 0) {
            return inventory.getStackInSlot(0).isEmpty();
        }
        return false;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return direction == Direction.DOWN && slot == 0;
    }

    @Override
    public int getContainerSize() {
        return inventory.getSlots();
    }

    @Override
    public boolean isEmpty() {
        return inventory.getStackInSlot(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return slot == 0 ? inventory.getStackInSlot(0) : ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            inventory.setStackInSlot(0, stack);
            setChanged();
        }
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        if (slot == 0) {
            return inventory.extractItem(0, count, false);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot == 0) {
            ItemStack stack = inventory.getStackInSlot(0);
            inventory.setStackInSlot(0, ItemStack.EMPTY);
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void clearContent() {
        inventory.setStackInSlot(0, ItemStack.EMPTY);
    }

    @Override
    public boolean stillValid(Player player) {
        final double MAX_DISTANCE = 64.0;
        return player.distanceToSqr(worldPosition.getCenter()) < MAX_DISTANCE;
    }

    public Optional<Vec3> getFlyingItemPosition(float partialTicks) {
        if (level == null || savedPos == null || inventory.getStackInSlot(0).isEmpty()) return Optional.empty();

        BlockEntity linkedEntity = level.getBlockEntity(savedPos);
        if (!(linkedEntity instanceof VitalStumpBlockEntity linkedAltar)) return Optional.empty();

        Optional<RecipeHolder<StumpRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(ModRecipes.STUMP_RECIPE_TYPE.get(),
                        new VitalStumpBlockEntity.BlockRecipeInput(linkedAltar.inventory.getStackInSlot(0), linkedAltar.getBlockPos()),
                        level);

        if (recipe.isEmpty()) return Optional.empty();

        StumpRecipe altarRecipe = recipe.get().value();
        if (!isIngredientUsedInRecipeForThisNexus(this, altarRecipe)) return Optional.empty();

        float interpolatedProgress = Mth.lerp(partialTicks, clientProgress, progress);
        float prog = maxProgress == 0 ? 0f : Mth.clamp(interpolatedProgress / maxProgress, 0f, 1f);
        if (prog <= 0f) return Optional.empty();

        float smoothProgress = prog * prog * (3f - 2f * prog);

        double startX = worldPosition.getX() + 0.5;
        double startY = worldPosition.getY() + 1.15;
        double startZ = worldPosition.getZ() + 0.5;

        double endX = savedPos.getX() + 0.5;
        double endY = savedPos.getY() + 1.15;
        double endZ = savedPos.getZ() + 0.5;

        double x = Mth.lerp(smoothProgress, startX, endX);
        double y = Mth.lerp(smoothProgress, startY, endY);
        double z = Mth.lerp(smoothProgress, startZ, endZ);

        return Optional.of(new Vec3(x, y, z));
    }

    private boolean isIngredientUsedInRecipeForThisNexus(StumpBlockEntity nexus, StumpRecipe recipe) {
        BlockPos thisPos = nexus.getBlockPos();
        Level level = nexus.getLevel();
        if (level == null) return false;

        List<Ingredient> ingredientsToMatch = recipe.additionalIngredients.stream()
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        Set<Ingredient> matchedIngredients = new HashSet<>();

        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (dx == 0 && dz == 0) continue;

                BlockPos checkPos = nexus.getSavedPos().offset(dx, 0, dz);
                BlockEntity be = level.getBlockEntity(checkPos);

                if (!(be instanceof StumpBlockEntity nearbyNexus)) continue;

                for (int slot = 0; slot < nearbyNexus.inventory.getSlots(); slot++) {
                    ItemStack stack = nearbyNexus.inventory.getStackInSlot(slot);
                    if (stack.isEmpty()) continue;

                    for (Ingredient ingredient : ingredientsToMatch) {
                        if (matchedIngredients.contains(ingredient)) continue;

                        if (ingredient.test(stack)) {
                            matchedIngredients.add(ingredient);

                            if (nearbyNexus.getBlockPos().equals(thisPos)) {
                                return true;
                            }

                            break;
                        }
                    }
                }
            }
        }
        return false;
    }
}