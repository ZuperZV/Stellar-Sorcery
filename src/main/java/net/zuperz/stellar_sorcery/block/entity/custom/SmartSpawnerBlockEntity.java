package net.zuperz.stellar_sorcery.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SpawnData;
import net.minecraft.world.level.Spawner;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.block.custom.SmartSpawnerBlock;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import net.zuperz.stellar_sorcery.mixin.BaseSpawnerAccessor;

import javax.annotation.Nullable;

public class SmartSpawnerBlockEntity extends BlockEntity implements Spawner {
    private final BaseSpawner spawner = new BaseSpawner() {
        @Override
        public void broadcastEvent(Level p_155767_, BlockPos p_155768_, int p_155769_) {
            p_155767_.blockEvent(p_155768_, Blocks.SPAWNER, p_155769_, 0);
        }

        @Override
        public void setNextSpawnData(@Nullable Level p_155771_, BlockPos p_155772_, SpawnData p_155773_) {
            super.setNextSpawnData(p_155771_, p_155772_, p_155773_);
            if (p_155771_ != null) {
                BlockState blockstate = p_155771_.getBlockState(p_155772_);
                p_155771_.sendBlockUpdated(p_155772_, blockstate, blockstate, 4);
            }
        }

        @Override
        public com.mojang.datafixers.util.Either<net.minecraft.world.level.block.entity.BlockEntity, net.minecraft.world.entity.Entity> getOwner() {
            return com.mojang.datafixers.util.Either.left(SmartSpawnerBlockEntity.this);
        }
    };

    public SmartSpawnerBlockEntity(BlockPos p_155752_, BlockState p_155753_) {
        super(ModBlockEntities.MOB_SMART_SPAWNER_BE.get(), p_155752_, p_155753_);
    }

    @Override
    protected void loadAdditional(CompoundTag p_338334_, HolderLookup.Provider p_338853_) {
        super.loadAdditional(p_338334_, p_338853_);
        this.spawner.load(this.level, this.worldPosition, p_338334_);
    }

    @Override
    protected void saveAdditional(CompoundTag p_187521_, HolderLookup.Provider p_324509_) {
        super.saveAdditional(p_187521_, p_324509_);
        this.spawner.save(p_187521_);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, SmartSpawnerBlockEntity be) {
        clientTickSmart(level, pos, be);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, SmartSpawnerBlockEntity be) {
        if (state.is(ModBlocks.SMART_SPAWNER) && state.getValue(SmartSpawnerBlock.ACTIVATED)) {
            be.spawner.serverTick((ServerLevel) level, pos);
        }
    }

    public static void clientTickSmart(Level level, BlockPos pos, SmartSpawnerBlockEntity be) {
        BaseSpawner spawner = be.getSpawner();
        BaseSpawnerAccessor accessor = (BaseSpawnerAccessor) spawner;

        if (!accessor.callIsNearPlayer(level, pos)) {
            accessor.setOSpin(accessor.getSpin());
        } else if (accessor.getDisplayEntity() != null) {
            RandomSource random = level.getRandom();

            double d0 = pos.getX() + random.nextDouble();
            double d1 = pos.getY() + random.nextDouble();
            double d2 = pos.getZ() + random.nextDouble();

            BlockState state = level.getBlockState(pos);

            if (state.is(ModBlocks.SMART_SPAWNER) && state.getValue(SmartSpawnerBlock.ACTIVATED)) {
                if (random.nextFloat() < 0.5f) {
                    level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0, 0.0, 0.0);
                }
                if (random.nextFloat() < 0.5f) {
                    level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, d0, d1, d2, 0.0, 0.0, 0.0);
                }
                if (random.nextFloat() < 0.5f) {
                    level.addParticle(ParticleTypes.TRIAL_OMEN, d0, d1, d2, 0.0, 0.0, 0.0);
                }
            } else {
                if (random.nextFloat() < 0.1f) {
                    level.addParticle(ParticleTypes.SMOKE, d0, d1, d2, 0.0, 0.0, 0.0);
                }
                if (random.nextFloat() < 0.1f) {
                    level.addParticle(ParticleTypes.SOUL_FIRE_FLAME, d0, d1, d2, 0.0, 0.0, 0.0);
                }
            }

            if (accessor.getSpawnDelay() > 0) {
                accessor.setSpawnDelay(accessor.getSpawnDelay() - 1);
            }

            accessor.setOSpin(accessor.getSpin());
            accessor.setSpin((accessor.getSpin() + (1000.0F / (accessor.getSpawnDelay() + 200.0F))) % 360.0);
        }
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider p_324015_) {
        CompoundTag compoundtag = this.saveCustomOnly(p_324015_);
        compoundtag.remove("SpawnPotentials");
        return compoundtag;
    }

    @Override
    public boolean triggerEvent(int p_59797_, int p_59798_) {
        return this.spawner.onEventTriggered(this.level, p_59797_) ? true : super.triggerEvent(p_59797_, p_59798_);
    }

    @Override
    public boolean onlyOpCanSetNbt() {
        return true;
    }

    @Override
    public void setEntityId(EntityType<?> p_254530_, RandomSource p_253719_) {
        this.spawner.setEntityId(p_254530_, this.level, p_253719_, this.worldPosition);
        this.setChanged();
    }

    public BaseSpawner getSpawner() {
        return this.spawner;
    }
}
