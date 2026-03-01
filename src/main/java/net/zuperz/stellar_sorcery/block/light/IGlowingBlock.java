package net.zuperz.stellar_sorcery.block.light;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface IGlowingBlock {
    default float getGlowScale() {
        return 1.0f;
    }

    default float getGlowScale(Level level, BlockPos pos) {
        return getGlowScale();
    }

    default boolean stopZFighting() {
        return true;
    }
}
