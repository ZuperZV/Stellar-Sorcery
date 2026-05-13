package net.zuperz.stellar_sorcery.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CampfireBlock.class)
public class CampfireBlockSylphEmberMixin {

    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void stellar_sorcery$entityInside(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {

        if (!(entity instanceof ItemEntity itemEntity)) {
            return;
        }

        ItemStack stack = itemEntity.getItem();

        if (stack.is(ModBlocks.DEATH_BLOOM.get().asItem())) {

            BlockEntity oldBlockEntity = level.getBlockEntity(pos);
            CompoundTag tag = null;

            if (oldBlockEntity != null) {
                tag = oldBlockEntity.saveWithoutMetadata(level.registryAccess());
            }

            BlockState newState = ModBlocks.SYLPH_EMBER_CAMPFIRE.get()
                    .defaultBlockState();

            for (var property : state.getProperties()) {
                if (newState.hasProperty(property)) {
                    newState = copyProperty(state, newState, property);
                }
            }

            itemEntity.discard();

            level.removeBlockEntity(pos);

            level.setBlock(pos, newState, 3);

            if (tag != null) {
                BlockEntity newBlockEntity = level.getBlockEntity(pos);

                if (newBlockEntity != null) {
                    newBlockEntity.loadWithComponents(tag, level.registryAccess());
                    newBlockEntity.setChanged();
                }
            }

            level.playSound(
                    null,
                    pos,
                    SoundEvents.ALLAY_DEATH,
                    SoundSource.BLOCKS,
                    1f,
                    1f
            );

            ci.cancel();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends Comparable<T>> BlockState copyProperty(
            BlockState from,
            BlockState to,
            net.minecraft.world.level.block.state.properties.Property<T> property
    ) {
        return to.setValue(property, from.getValue(property));
    }
}