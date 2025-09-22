package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.component.BlockStorageData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;

import java.util.ArrayList;
import java.util.List;

public class BlockCanisterItem extends Item {
    private final int radius;
    private final TagKey<Block> blockTag;

    public BlockCanisterItem(Properties properties, int radius, TagKey<Block> blockTagId) {
        super(properties);
        this.radius = radius;
        this.blockTag = blockTagId;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        if (level.isClientSide) return InteractionResult.SUCCESS;

        var player = context.getPlayer();
        var stack = context.getItemInHand();

        if (level.getBlockState(pos).getBlock() != ModBlocks.SOUL_CANDLE.get()) {
            return InteractionResult.PASS;
        }

        BlockStorageData existingData = stack.get(ModDataComponentTypes.BLOCK_STORAGE_DATA);

        if (existingData == null || existingData.isEmpty()) {
            List<BlockStorageData.SavedBlock> foundBlocks = new ArrayList<>();

            BlockPos.betweenClosedStream(pos.offset(-radius, -radius, -radius),
                            pos.offset(radius, radius, radius))
                    .forEach(p -> {
                        BlockState state = level.getBlockState(p);
                        if (state.is(blockTag)) {
                            foundBlocks.add(new BlockStorageData.SavedBlock(p.subtract(pos), state));
                        }
                    });

            for (var block : foundBlocks) {
                if (level instanceof ServerLevel serverLevel) {
                    BlockPos absolutePos = pos.offset(block.relativePos);
                    serverLevel.sendParticles(ParticleTypes.ASH,
                            absolutePos.getX() + 0.5, absolutePos.getY() + 0.1, absolutePos.getZ() + 0.5,
                            8, 0.3, 0.3, 0.3, 0.01);
                }
                level.removeBlock(pos.offset(block.relativePos), false);
            }

            if (!foundBlocks.isEmpty()) {
                BlockStorageData newData = new BlockStorageData(foundBlocks);
                stack.set(ModDataComponentTypes.BLOCK_STORAGE_DATA, newData);

                level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE,
                        SoundSource.PLAYERS, 1.0f, 0.5f);

                if (player != null) player.displayClientMessage(
                        net.minecraft.network.chat.Component.literal("Blocks Saved: " + foundBlocks.size()), true);
            }

        } else {
            if (level instanceof ServerLevel serverLevel) {
                for (var block : existingData.getBlocks()) {
                    BlockPos placePos = pos.offset(block.relativePos);

                    BlockState currentState = level.getBlockState(placePos);

                    if (currentState.isAir() && canSurvive(serverLevel, placePos)) {
                        serverLevel.setBlockAndUpdate(placePos, block.state);
                        serverLevel.sendParticles(ParticleTypes.ASH,
                                placePos.getX() + 0.5, placePos.getY() + 0.1, placePos.getZ() + 0.5,
                                6, 0.3, 0.3, 0.3, 0.01);
                    }
                }
            }

            level.playSound(null, pos, SoundEvents.BEACON_ACTIVATE,
                    SoundSource.PLAYERS, 1.0f, 0.8f);

            stack.remove(ModDataComponentTypes.BLOCK_STORAGE_DATA);

            if (player != null) player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("Blocks Placed"), true);
        }

        return InteractionResult.SUCCESS;
    }

    public boolean canSurvive(LevelReader level, BlockPos pos) {
        BlockPos below = pos.below();
        BlockState stateBelow = level.getBlockState(below);
        return stateBelow.isFaceSturdy(level, below, Direction.UP);
    }
}