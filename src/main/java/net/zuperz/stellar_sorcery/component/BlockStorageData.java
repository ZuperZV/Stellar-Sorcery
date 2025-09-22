package net.zuperz.stellar_sorcery.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Objects;

public class BlockStorageData {
    private final List<SavedBlock> blocks;

    public BlockStorageData(List<SavedBlock> blocks) {
        this.blocks = List.copyOf(blocks);
    }

    public List<SavedBlock> getBlocks() {
        return blocks;
    }

    public boolean isEmpty() {
        return blocks.isEmpty();
    }

    public static class SavedBlock {
        public final BlockPos pos;
        public final BlockState state;

        public SavedBlock(BlockPos pos, BlockState state) {
            this.pos = pos;
            this.state = state;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SavedBlock that)) return false;
            return pos.equals(that.pos) && state.equals(that.state);
        }

        @Override
        public int hashCode() {
            return Objects.hash(pos, state);
        }
    }

    public static final Codec<SavedBlock> SAVED_BLOCK_CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BlockPos.CODEC.fieldOf("pos").forGetter(b -> b.pos),
            ResourceLocation.CODEC.fieldOf("block").forGetter(b -> BuiltInRegistries.BLOCK.getKey(b.state.getBlock()))
    ).apply(instance, (pos, blockId) -> {
        Block block = BuiltInRegistries.BLOCK.get(blockId);
        return new SavedBlock(pos, block.defaultBlockState());
    }));

    public static final Codec<BlockStorageData> CODEC = SAVED_BLOCK_CODEC.listOf().xmap(
            BlockStorageData::new,
            BlockStorageData::getBlocks
    );

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BlockStorageData that)) return false;
        return blocks.equals(that.blocks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(blocks);
    }
}