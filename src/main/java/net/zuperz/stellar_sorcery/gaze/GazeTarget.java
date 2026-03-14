package net.zuperz.stellar_sorcery.gaze;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public record GazeTarget(
        BlockPos blockPos,
        Vec3 hitPos,
        Entity entity,
        List<Entity> entities,
        List<BlockPos> blocks
) {

    public static GazeTarget empty() {
        return new GazeTarget(null, null, null, List.of(), List.of());
    }

    public BlockPos getCenterPos(BlockPos fallback) {
        if (blockPos != null) return blockPos;
        if (entity != null) return entity.blockPosition();
        if (hitPos != null) return BlockPos.containing(hitPos);
        return fallback;
    }
}
