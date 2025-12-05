package net.zuperz.stellar_sorcery.data.spell;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class AreaHandler {

    /**
     * Returnerer en liste af targets baseret på area type
     */
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

            default -> result;
        };
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
}
