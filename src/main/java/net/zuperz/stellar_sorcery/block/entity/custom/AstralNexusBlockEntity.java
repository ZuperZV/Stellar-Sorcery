package net.zuperz.stellar_sorcery.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zuperz.stellar_sorcery.block.custom.AstralNexusBlock;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import org.jetbrains.annotations.Nullable;

public class AstralNexusBlockEntity extends BlockEntity implements WorldlyContainer {
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

    public static void tickClient(Level level, BlockPos pos, BlockState state, AstralNexusBlockEntity blockEntity) {
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

    public static void tickServer(Level level, BlockPos pos, BlockState state, AstralNexusBlockEntity blockEntity) {
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


    public AstralNexusBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.ASTRAL_NEXUS_BE.get(), pPos, pBlockState);
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

}