package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.zuperz.stellar_sorcery.block.ModBlocks;

public class MoonshineCatalystItem extends Item {
    public MoonshineCatalystItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos blockpos = context.getClickedPos();
        BlockState blockstate = level.getBlockState(blockpos);
        ItemStack itemStack = context.getItemInHand();

        if (blockstate.is(Blocks.BUDDING_AMETHYST)) {
            itemStack.shrink(1);
            level.setBlockAndUpdate(blockpos, ModBlocks.BUDDING_MOONSHINE.get().defaultBlockState());

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.CRIT,
                        blockpos.getX() + 0.5,
                        blockpos.getY() + 0.5,
                        blockpos.getZ() + 0.5,
                        24,
                        0.3, 0.3, 0.3,
                        0.01
                );
            }

            level.playSound(null, blockpos, SoundEvents.AMETHYST_BLOCK_RESONATE,
                    SoundSource.BLOCKS, 5.5f, 1.3f);

            level.playSound(null, blockpos, SoundEvents.AMETHYST_BLOCK_PLACE,
                    SoundSource.BLOCKS, 1.5f, 1.3f);

            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        return super.useOn(context);
    }
}
