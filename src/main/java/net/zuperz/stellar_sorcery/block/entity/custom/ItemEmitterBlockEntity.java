package net.zuperz.stellar_sorcery.block.entity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.zuperz.stellar_sorcery.block.custom.ItemEmitterBlock;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;

public class ItemEmitterBlockEntity extends BlockEntity {

    private static final int MAX_BEAM_LENGTH = 32;
    private static final int MAX_BEAM_TICKS = 20;

    private int beamTicksRemaining = 0;

    public ItemEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ITEM_EMITTER_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, ItemEmitterBlockEntity self) {
        if (level.isClientSide) return;

        if (state.getValue(ItemEmitterBlock.ON)) return;

        if (self.beamTicksRemaining-- > 0) return;
        self.beamTicksRemaining = MAX_BEAM_TICKS;

        Direction facing = state.getValue(ItemEmitterBlock.FACING);

        ItemStack extracted = extractOneItem(level, pos, facing);
        if (extracted.isEmpty()) return;

        boolean success = sendToTarget(level, pos, facing, extracted);

        if (!success) {
            returnToSource(level, pos, facing, extracted);
        }
    }

    private static ItemStack extractOneItem(Level level, BlockPos pos, Direction facing) {
        BlockEntity sourceBE = level.getBlockEntity(pos.relative(facing.getOpposite()));
        if (sourceBE instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack stack = container.getItem(i);
                if (!stack.isEmpty()) {
                    ItemStack oneItem = stack.split(1);
                    container.setItem(i, stack);
                    markChanged(sourceBE);
                    return oneItem;
                }
            }
        }
        return ItemStack.EMPTY;
    }

    private static boolean sendToTarget(Level level, BlockPos pos, Direction facing, ItemStack stack) {
        BlockPos.MutableBlockPos cursor = pos.mutable();

        for (int i = 1; i <= MAX_BEAM_LENGTH; i++) {
            cursor.move(facing);
            BlockEntity targetBE = level.getBlockEntity(cursor);
            if (targetBE == null) continue;

            Container container = null;


            if (targetBE instanceof ItemEmitterBlockEntity emitter) {
                if (!emitter.getBlockState().getValue(ItemEmitterBlock.ON)) continue;

                Direction targetDirection = emitter.getBlockState().getValue(ItemEmitterBlock.FACING);
                BlockPos newTargetPos = emitter.getBlockPos().relative(targetDirection);
                BlockEntity targetContainerBE = level.getBlockEntity(newTargetPos);

                if (targetContainerBE instanceof Container c) {
                    container = c;
                    targetBE = targetContainerBE;
                }
            } else if (targetBE instanceof Container c) {
                container = c;
            }

            if (container != null) {
                for (int slot = 0; slot < container.getContainerSize(); slot++) {
                    ItemStack slotStack = container.getItem(slot);

                    if (slotStack.isEmpty()) {
                        container.setItem(slot, stack);
                        markChanged(targetBE);
                        return true;
                    }

                    if (ItemStack.isSameItemSameComponents(slotStack, stack)
                            && slotStack.getCount() < slotStack.getMaxStackSize()) {
                        slotStack.grow(1);
                        markChanged(targetBE);
                        return true;
                    }
                }
            }
        }

        return false;
    }

    private static void returnToSource(Level level, BlockPos pos, Direction facing, ItemStack stack) {
        BlockEntity sourceBE = level.getBlockEntity(pos.relative(facing.getOpposite()));
        if (sourceBE instanceof Container container) {
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack slotStack = container.getItem(slot);

                if (slotStack.isEmpty()) {
                    container.setItem(slot, stack);
                    markChanged(sourceBE);
                    return;
                }

                if (ItemStack.isSameItemSameComponents(slotStack, stack)
                        && slotStack.getCount() < slotStack.getMaxStackSize()) {
                    slotStack.grow(1);
                    markChanged(sourceBE);
                    return;
                }
            }
        }
    }

    private static void markChanged(BlockEntity be) {
        if (be instanceof RandomizableContainerBlockEntity r) {
            r.setChanged();
        }
    }
}