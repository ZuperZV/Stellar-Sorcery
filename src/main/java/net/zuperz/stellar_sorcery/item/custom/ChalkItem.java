package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.BlockItemStateProperties;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

public class ChalkItem extends BlockItem {

    public ChalkItem(Properties properties, Block block) {
        super(block, properties);
    }

    @Override
    public String getDescriptionId() {
        return getOrCreateDescriptionId();
    }

    @Override
    public InteractionResult place(BlockPlaceContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        Block block = this.getBlock();

        if (!block.isEnabled(level.enabledFeatures()) || !context.canPlace()) {
            return InteractionResult.FAIL;
        }

        BlockPlaceContext blockContext = this.updatePlacementContext(context);
        if (blockContext == null) return InteractionResult.FAIL;

        BlockState state = this.getPlacementState(blockContext);
        if (state == null) return InteractionResult.FAIL;

        if (!this.placeBlock(blockContext, state)) return InteractionResult.FAIL;

        BlockPos pos = blockContext.getClickedPos();
        BlockState placedState = level.getBlockState(pos);

        BlockItemStateProperties props = stack.getOrDefault(DataComponents.BLOCK_STATE, BlockItemStateProperties.EMPTY);
        if (!props.isEmpty()) {
            BlockState newState = props.apply(placedState);
            if (newState != placedState) {
                placedState = newState;
                level.setBlock(pos, newState, 2);
            }
        }

        BlockEntity be = level.getBlockEntity(pos);
        if (be != null) {
            be.applyComponentsFromItemStack(stack);
            be.setChanged();
        }

        updateCustomBlockEntityTag(level, player, pos, stack);

        placedState.getBlock().setPlacedBy(level, pos, placedState, player, stack);
        if (player instanceof ServerPlayer sp) {
            CriteriaTriggers.PLACED_BLOCK.trigger(sp, pos, stack);
        }

        SoundType soundtype = placedState.getSoundType(level, pos, player);
        level.playSound(player, pos, this.getPlaceSound(placedState, level, pos, player),
                SoundSource.BLOCKS, (soundtype.getVolume() + 1.0F) / 2.0F,
                soundtype.getPitch() * 0.8F);
        level.gameEvent(GameEvent.BLOCK_PLACE, pos, GameEvent.Context.of(player, placedState));

        if (player != null) {
            var slot = context.getHand() == net.minecraft.world.InteractionHand.MAIN_HAND
                    ? net.minecraft.world.entity.EquipmentSlot.MAINHAND
                    : net.minecraft.world.entity.EquipmentSlot.OFFHAND;

            stack.hurtAndBreak(1, player, slot);
        }

        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
