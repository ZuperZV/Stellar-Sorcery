package net.zuperz.stellar_sorcery.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
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
        }

        @Override
        protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
            super.loadAdditional(pTag, pRegistries);
            inventory.deserializeNBT(pRegistries, pTag.getCompound("inventory"));
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
    }