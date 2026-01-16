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

        SourceSlot source = findValidSource(level, pos, facing);
        if (source == null) return;

        if (!sendToTarget(level, pos, facing, source)) return;

        source.container.removeItem(source.slot, 1);
        markChanged(source.be);
    }

    private static SourceSlot findValidSource(Level level, BlockPos pos, Direction facing) {
        BlockEntity be = level.getBlockEntity(pos.relative(facing.getOpposite()));
        if (!(be instanceof Container container)) return null;

        Direction side = facing;
        int[] slots = getSlots(container, side);

        for (int slot : slots) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) continue;

            if (!canTakeItem(container, slot, stack, side)) continue;

            ItemStack preview = stack.copy();
            preview.setCount(1);

            if (canInsertAnywhere(level, pos, facing, preview)) {
                return new SourceSlot(container, be, slot, preview);
            }
        }
        return null;
    }

    private static boolean canInsertAnywhere(Level level, BlockPos pos, Direction facing, ItemStack stack) {
        BlockPos.MutableBlockPos cursor = pos.mutable();

        for (int i = 1; i <= MAX_BEAM_LENGTH; i++) {
            cursor.move(facing);
            BlockEntity be = level.getBlockEntity(cursor);
            if (be == null) continue;

            Container container = null;
            Direction side = facing.getOpposite();

            if (be instanceof ItemEmitterBlockEntity emitter) {
                if (!emitter.getBlockState().getValue(ItemEmitterBlock.ON)) continue;

                Direction newFacing = emitter.getBlockState().getValue(ItemEmitterBlock.FACING);
                BlockEntity next = level.getBlockEntity(emitter.getBlockPos().relative(newFacing));
                if (next instanceof Container c) {
                    container = c;
                    side = newFacing.getOpposite();
                }
            } else if (be instanceof Container c) {
                container = c;
            }

            if (container != null && canInsert(container, stack, side)) {
                return true;
            }
        }
        return false;
    }

    private static boolean sendToTarget(Level level, BlockPos pos, Direction facing, SourceSlot source) {
        BlockPos.MutableBlockPos cursor = pos.mutable();

        for (int i = 1; i <= MAX_BEAM_LENGTH; i++) {
            cursor.move(facing);
            BlockEntity be = level.getBlockEntity(cursor);
            if (be == null) continue;

            Container container = null;
            Direction side = facing.getOpposite();

            if (be instanceof ItemEmitterBlockEntity emitter) {
                if (!emitter.getBlockState().getValue(ItemEmitterBlock.ON)) continue;

                Direction newFacing = emitter.getBlockState().getValue(ItemEmitterBlock.FACING);
                BlockEntity next = level.getBlockEntity(emitter.getBlockPos().relative(newFacing));
                if (next instanceof Container c) {
                    container = c;
                    be = next;
                    side = newFacing.getOpposite();
                }
            } else if (be instanceof Container c) {
                container = c;
            }

            if (container != null && tryInsert(container, be, source.stack.copy(), side)) {
                return true;
            }
        }
        return false;
    }

    private static boolean canInsert(Container container, ItemStack stack, Direction side) {
        int[] slots = getSlots(container, side);
        for (int slot : slots) {
            if (!canPlaceItem(container, stack, slot, side)) continue;

            ItemStack existing = container.getItem(slot);
            int max = Math.min(container.getMaxStackSize(stack), stack.getMaxStackSize());

            if (existing.isEmpty()) return true;
            if (ItemStack.isSameItemSameComponents(existing, stack)
                    && existing.getCount() < max) return true;
        }
        return false;
    }

    private static boolean tryInsert(Container container, BlockEntity be, ItemStack stack, Direction side) {
        int[] slots = getSlots(container, side);

        for (int slot : slots) {
            if (!canPlaceItem(container, stack, slot, side)) continue;

            ItemStack existing = container.getItem(slot);
            int max = Math.min(container.getMaxStackSize(stack), stack.getMaxStackSize());

            if (existing.isEmpty()) {
                container.setItem(slot, stack);
                markChanged(be);
                return true;
            }

            if (ItemStack.isSameItemSameComponents(existing, stack)
                    && existing.getCount() < max) {
                existing.grow(1);
                markChanged(be);
                return true;
            }
        }
        return false;
    }

    private static int[] getSlots(Container container, Direction side) {
        if (container instanceof WorldlyContainer wc) {
            return wc.getSlotsForFace(side);
        }
        int size = container.getContainerSize();
        int[] slots = new int[size];
        for (int i = 0; i < size; i++) slots[i] = i;
        return slots;
    }

    private static boolean canPlaceItem(Container container, ItemStack stack, int slot, Direction side) {
        if (!container.canPlaceItem(slot, stack)) return false;
        return !(container instanceof WorldlyContainer wc)
                || wc.canPlaceItemThroughFace(slot, stack, side);
    }

    private static boolean canTakeItem(Container container, int slot, ItemStack stack, Direction side) {
        return !(container instanceof WorldlyContainer wc)
                || wc.canTakeItemThroughFace(slot, stack, side);
    }

    private static void markChanged(BlockEntity be) {
        be.setChanged();

        if (be.getLevel() != null) {
            be.getLevel().sendBlockUpdated(
                    be.getBlockPos(),
                    be.getBlockState(),
                    be.getBlockState(),
                    3
            );
        }
    }

    private record SourceSlot(Container container, BlockEntity be, int slot, ItemStack stack) {}
}