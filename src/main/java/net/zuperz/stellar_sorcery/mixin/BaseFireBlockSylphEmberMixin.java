package net.zuperz.stellar_sorcery.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BaseFireBlock.class)
public class BaseFireBlockSylphEmberMixin {

    @Inject(method = "entityInside", at = @At("HEAD"), cancellable = true)
    private void stellar_sorcery$entityInside(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {

        if (!(entity instanceof ItemEntity itemEntity)) {
            return;
        }

        ItemStack stack = itemEntity.getItem();

        if (stack.is(ModBlocks.DEATH_BLOOM.get().asItem())) {

            itemEntity.discard();

            level.setBlock(
                    pos,
                    ModBlocks.SYLPH_EMBER.get().defaultBlockState(),
                    3
            );

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
}