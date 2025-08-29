package net.zuperz.stellar_sorcery.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import net.zuperz.stellar_sorcery.entity.ModEntities;
import net.zuperz.stellar_sorcery.entity.custom.SigilOrbEntity;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ArcForgeBlockEntity extends BlockEntity {
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

        public ArcForgeBlockEntity(BlockPos pPos, BlockState pBlockState) {
            super(ModBlockEntities.ARCFORGE_BE.get(), pPos, pBlockState);
        }

    public static void tick(Level level, BlockPos pos, BlockState state, AstralAltarBlockEntity altar) {

        if (!level.isClientSide) {
            level.sendBlockUpdated(pos, state, state, 3);
        }
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        try {
            tag.put("inventory", inventory.serializeNBT(registries));
        } catch (Exception e) {
            System.err.println("Failed to save ArcForge inventory: " + e.getMessage());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        if (tag.contains("inventory")) {
            try {
                inventory.deserializeNBT(registries, tag.getCompound("inventory"));
            } catch (Exception e) {
                System.err.println("Failed to load ArcForge inventory: " + e.getMessage());
            }
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


        public void spawnSigilEntity(ItemStack stack) {
            if (level != null && !level.isClientSide()) {
                SigilOrbEntity sigil = new SigilOrbEntity(ModEntities.SIGIL_ORB.get(), level);

                sigil.setPos(worldPosition.getX() + 0.5, worldPosition.getY() + 1.9, worldPosition.getZ() + 0.5);

                level.addFreshEntity(sigil);
            }
        }

        public void removeSigilEntity() {
            if (level != null && !level.isClientSide()) {
                List<SigilOrbEntity> sigils = level.getEntitiesOfClass(
                        SigilOrbEntity.class,
                        new AABB(
                                worldPosition.getX() - 2, worldPosition.getY() - 2, worldPosition.getZ() - 2,
                                worldPosition.getX() + 3, worldPosition.getY() + 3, worldPosition.getZ() + 3
                        )
                );

                for (SigilOrbEntity sigil : sigils) {
                    sigil.discard();
                }
            }
        }

    @Override
    public void setChanged() {
        super.setChanged();

        if (!this.level.isClientSide) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), 3);
        }
    }
}