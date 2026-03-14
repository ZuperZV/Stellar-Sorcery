package net.zuperz.stellar_sorcery.gaze;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.zuperz.stellar_sorcery.data.gaze.GazeDefinition;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class GazeTargetTypes {

    public interface Resolver {
        GazeTarget resolve(GazeSpellContext context, GazeDefinition.Target targetDef);
    }

    private static final Map<String, Resolver> RESOLVERS = new HashMap<>();

    static {
        register("self", (ctx, def) -> new GazeTarget(null, null, ctx.player(), List.of(ctx.player()), List.of()));

        register("block", (ctx, def) -> {
            BlockHitResult hit = raycastBlock(ctx, def);
            if (hit == null || hit.getType() == HitResult.Type.MISS) return GazeTarget.empty();
            return new GazeTarget(hit.getBlockPos(), hit.getLocation(), null, List.of(), List.of(hit.getBlockPos()));
        });

        register("entity", (ctx, def) -> {
            EntityHitResult hit = raycastEntity(ctx, def);
            if (hit == null || hit.getType() == HitResult.Type.MISS) return GazeTarget.empty();
            Entity entity = hit.getEntity();
            return new GazeTarget(entity.blockPosition(), hit.getLocation(), entity, List.of(entity), List.of());
        });

        register("raycast", (ctx, def) -> {
            BlockHitResult blockHit = raycastBlock(ctx, def);
            EntityHitResult entityHit = raycastEntity(ctx, def);

            if (entityHit != null && entityHit.getType() != HitResult.Type.MISS) {
                double entityDist = entityHit.getLocation().distanceToSqr(ctx.player().getEyePosition(1.0f));
                double blockDist = blockHit != null ? blockHit.getLocation().distanceToSqr(ctx.player().getEyePosition(1.0f)) : Double.MAX_VALUE;

                if (entityDist <= blockDist) {
                    Entity entity = entityHit.getEntity();
                    return new GazeTarget(entity.blockPosition(), entityHit.getLocation(), entity, List.of(entity), List.of());
                }
            }

            if (blockHit == null || blockHit.getType() == HitResult.Type.MISS) return GazeTarget.empty();
            return new GazeTarget(blockHit.getBlockPos(), blockHit.getLocation(), null, List.of(), List.of(blockHit.getBlockPos()));
        });

        register("area", (ctx, def) -> {
            BlockPos center = resolveAreaCenter(ctx, def);
            double radius = def.radius() > 0.0 ? def.radius() : ctx.stats().actionRadius;

            List<Entity> entities = new ArrayList<>();
            if (def.includeEntities()) {
                AABB box = new AABB(center).inflate(radius);
                entities.addAll(ctx.level().getEntitiesOfClass(LivingEntity.class, box));
            }

            List<BlockPos> blocks = new ArrayList<>();
            if (def.includeBlocks() && radius > 0.0) {
                int r = (int) Math.ceil(radius);
                for (BlockPos pos : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
                    if (pos.distSqr(center) <= radius * radius) {
                        blocks.add(pos.immutable());
                    }
                }
            }

            return new GazeTarget(center, center.getCenter(), null, entities, blocks);
        });

        register("structure", (ctx, def) -> {
            if (!(ctx.level() instanceof ServerLevel serverLevel)) return GazeTarget.empty();
            String structureId = def.structure();
            if (structureId == null || structureId.isBlank()) return GazeTarget.empty();

            BlockPos origin = ctx.player().blockPosition();
            BlockPos found = findNearestStructure(serverLevel, origin, structureId, (int) def.range());
            if (found == null) return GazeTarget.empty();

            return new GazeTarget(found, found.getCenter(), null, List.of(), List.of(found));
        });
    }

    private GazeTargetTypes() {}

    public static void register(String type, Resolver resolver) {
        RESOLVERS.put(type.toLowerCase(Locale.ROOT), resolver);
    }

    public static Resolver get(String type) {
        return RESOLVERS.getOrDefault(type.toLowerCase(Locale.ROOT), RESOLVERS.get("self"));
    }

    private static BlockPos resolveAreaCenter(GazeSpellContext ctx, GazeDefinition.Target def) {
        String center = def.center();
        if (center == null) center = "player";

        if ("raycast".equalsIgnoreCase(center) || "block".equalsIgnoreCase(center)) {
            BlockHitResult hit = raycastBlock(ctx, def);
            if (hit != null && hit.getType() != HitResult.Type.MISS) {
                return hit.getBlockPos();
            }
        }

        return ctx.player().blockPosition();
    }

    private static BlockHitResult raycastBlock(GazeSpellContext ctx, GazeDefinition.Target def) {
        double range = def.range() > 0.0 ? def.range() : ctx.stats().targetRange;
        if (range <= 0.0) range = 6.0;

        Vec3 start = ctx.player().getEyePosition(1.0f);
        Vec3 end = start.add(ctx.player().getViewVector(1.0f).scale(range));

        ClipContext clip = new ClipContext(
                start,
                end,
                ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE,
                ctx.player()
        );

        return ctx.level().clip(clip);
    }

    private static EntityHitResult raycastEntity(GazeSpellContext ctx, GazeDefinition.Target def) {
        double range = def.range() > 0.0 ? def.range() : ctx.stats().targetRange;
        if (range <= 0.0) range = 6.0;

        Vec3 start = ctx.player().getEyePosition(1.0f);
        Vec3 end = start.add(ctx.player().getViewVector(1.0f).scale(range));

        AABB box = ctx.player().getBoundingBox().expandTowards(ctx.player().getViewVector(1.0f).scale(range)).inflate(1.0);
        return ProjectileUtil.getEntityHitResult(
                ctx.level(),
                ctx.player(),
                start,
                end,
                box,
                entity -> entity instanceof LivingEntity && entity.isPickable()
        );
    }

    private static BlockPos findNearestStructure(ServerLevel level, BlockPos origin, String structureId, int radius) {
        ResourceLocation rl = ResourceLocation.tryParse(structureId.startsWith("#")
                ? structureId.substring(1)
                : structureId);
        if (rl == null) return null;

        TagKey<Structure> tagKey = TagKey.create(Registries.STRUCTURE, rl);

        try {
            for (Method method : ServerLevel.class.getMethods()) {
                if (!method.getName().equals("findNearestMapStructure")) continue;
                Class<?>[] params = method.getParameterTypes();
                if (params.length != 4) continue;

                Object firstArg = null;
                if (TagKey.class.isAssignableFrom(params[0])) {
                    firstArg = tagKey;
                } else if (HolderSet.class.isAssignableFrom(params[0])) {
                    ResourceKey<Structure> key = ResourceKey.create(Registries.STRUCTURE, rl);
                    Holder<Structure> holder = level.registryAccess()
                            .registryOrThrow(Registries.STRUCTURE)
                            .getHolder(key)
                            .orElse(null);
                    if (holder != null) {
                        firstArg = HolderSet.direct(holder);
                    }
                }

                if (firstArg == null) continue;

                Object result = method.invoke(level, firstArg, origin, radius, false);
                if (result == null) return null;

                Method firstGetter = result.getClass().getMethod("getFirst");
                Object first = firstGetter.invoke(result);
                if (first instanceof BlockPos pos) {
                    return pos;
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }
}
