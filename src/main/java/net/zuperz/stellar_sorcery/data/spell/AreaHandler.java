package net.zuperz.stellar_sorcery.data.spell;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class AreaHandler {

    public static List<Entity> getTargets(Player player, AreaFile area) {

        List<Entity> result = new ArrayList<>();

        return switch (area.shape) {

            case "single" -> {
                Entity target = getSingleTarget(player, area.range);
                if (target != null) result.add(target);
                yield result;
            }

            case "circle" -> getAreaCircle(player, area.radius, area.max_targets);

            case "cone" -> getConeTargets(player, area.range, area.angle, area.max_targets);

            case "self" -> {
                result.add(player);
                yield result;
            }

            case "projectile" -> result;

            case "beam" -> getBeamTargets(player, area);

            default -> result;
        };
    }

    private static List<Entity> getBeamTargets(Player player, AreaFile area) {

        List<Entity> result = new ArrayList<>();

        Vec3 start = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 end = start.add(look.scale(area.range));

        HitResult hit = player.level().clip(
                new ClipContext(start, end,
                        ClipContext.Block.OUTLINE,
                        ClipContext.Fluid.NONE, player));

        Vec3 hitPos = hit.getLocation();

        float useTime = player.getUseItemRemainingTicks() / 20f;
        float size = Math.min(area.max_size, 0.3f + area.grow_speed * useTime);

        for (Entity e : player.level().getEntities(player, new AABB(start, hitPos).inflate(size))) {
            if (!(e instanceof LivingEntity)) continue;

            double dist = e.position().distanceTo(lineClosestPoint(start, hitPos, e.position()));

            if (dist <= size) {
                result.add(e);
            }
        }

        if (player.level() instanceof ServerLevel sl) {
            SpellParticles.spawnBeamParticles(sl, start, hitPos, size);
        }

        return result;
    }


    private static Entity getSingleTarget(Player player, float range) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(range));

        Entity hit = null;

        for (Entity e : player.level().getEntities(player, player.getBoundingBox().inflate(range))) {
            if (!(e instanceof LivingEntity)) continue;

            AABB box = e.getBoundingBox();
            if (box.clip(start, end).isPresent()) {
                hit = e;
                break;
            }
        }
        return hit;
    }

    private static List<Entity> getAreaCircle(Player player, float radius, int max) {
        return player.level().getEntities(player,
                new AABB(
                        player.getX() - radius,
                        player.getY() - radius,
                        player.getZ() - radius,
                        player.getX() + radius,
                        player.getY() + radius,
                        player.getZ() + radius
                )).stream().limit(max).toList();
    }

    private static List<Entity> getConeTargets(Player player, float range, float angle, int max) {

        List<Entity> result = new ArrayList<>();
        Vec3 origin = player.getEyePosition();
        Vec3 look = player.getLookAngle().normalize();

        double cosAngle = Math.cos(Math.toRadians(angle));

        for (Entity e : player.level().getEntities(player, player.getBoundingBox().inflate(range))) {
            if (!(e instanceof LivingEntity)) continue;

            Vec3 toTarget = e.position().subtract(origin).normalize();

            double dot = look.dot(toTarget);

            if (dot >= cosAngle) {
                result.add(e);
                if (result.size() >= max) break;
            }
        }
        return result;
    }

    public static BlockPos getAreaPos(Player player, AreaFile area) {
        Vec3 start = player.getEyePosition();
        Vec3 end = start.add(player.getLookAngle().scale(area.range));

        return new BlockPos(
                (int) end.x,
                (int) end.y,
                (int) end.z
        );
    }

    public static List<BlockPos> getAllBlocksInArea(BlockPos center, float range) {
        List<BlockPos> list = new ArrayList<>();

        for (float x = -range; x <= range; x++) {
            for (float y = -range; y <= range; y++) {
                for (float z = -range; z <= range; z++) {
                    list.add(center.offset((int) x, (int) y, (int) z));
                }
            }
        }
        return list;
    }

    private static Vec3 lineClosestPoint(Vec3 a, Vec3 b, Vec3 p) {
        Vec3 ab = b.subtract(a);
        double t = (p.subtract(a)).dot(ab) / ab.lengthSqr();
        t = Math.max(0, Math.min(1, t));
        return a.add(ab.scale(t));
    }
}
