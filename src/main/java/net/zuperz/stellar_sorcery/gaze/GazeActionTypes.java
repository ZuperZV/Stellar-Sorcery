package net.zuperz.stellar_sorcery.gaze;

import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.zuperz.stellar_sorcery.data.gaze.GazeDefinition;
import net.zuperz.stellar_sorcery.data.gaze.GazeJsonHelper;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class GazeActionTypes {

    public interface Executor {
        void execute(GazeSpellContext context, GazeDefinition.Action action);
    }

    private static final Map<String, Executor> EXECUTORS = new HashMap<>();

    static {
        register("none", (ctx, action) -> {});
        register("grow_plants", GazeActionTypes::growPlants);
        register("apply_effect", GazeActionTypes::applyEffect);
        register("damage", GazeActionTypes::damageTargets);
        register("spawn_entity", GazeActionTypes::spawnEntity);
        register("set_block", GazeActionTypes::setBlock);
        register("run_command", GazeActionTypes::runCommand);
    }

    private GazeActionTypes() {}

    public static void register(String type, Executor executor) {
        EXECUTORS.put(type.toLowerCase(Locale.ROOT), executor);
    }

    public static Executor get(String type) {
        return EXECUTORS.getOrDefault(type.toLowerCase(Locale.ROOT), EXECUTORS.get("none"));
    }

    private static void growPlants(GazeSpellContext ctx, GazeDefinition.Action action) {
        if (!(ctx.level() instanceof ServerLevel serverLevel)) return;

        int radius = (int) Math.round(ctx.stats().actionRadius);
        BlockPos center = ctx.target().getCenterPos(ctx.player().blockPosition());

        if (radius <= 0) {
            BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), serverLevel, center, ctx.player());
            return;
        }

        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-radius, -radius, -radius), center.offset(radius, radius, radius))) {
            if (pos.distSqr(center) > radius * radius) continue;
            BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), serverLevel, pos, ctx.player());
        }
    }

    private static void applyEffect(GazeSpellContext ctx, GazeDefinition.Action action) {
        JsonObject data = action.data();
        String effectId = GazeJsonHelper.getString(data, "effect", "");
        if (effectId.isBlank()) return;

        ResourceLocation rl = ResourceLocation.tryParse(effectId);
        if (rl == null) return;

        Holder<MobEffect> holder = BuiltInRegistries.MOB_EFFECT.getHolder(rl).orElse(null);
        if (holder == null) return;

        int duration = GazeJsonHelper.getInt(data, "duration", 100);
        int amplifier = GazeJsonHelper.getInt(data, "amplifier", 0);
        boolean ambient = GazeJsonHelper.getBoolean(data, "ambient", false);
        boolean showParticles = GazeJsonHelper.getBoolean(data, "show_particles", true);

        MobEffectInstance instance = new MobEffectInstance(holder, duration, amplifier, ambient, showParticles);

        List<Entity> targets = ctx.target().entities();
        if (targets.isEmpty() && ctx.target().entity() != null) {
            targets = List.of(ctx.target().entity());
        }

        for (Entity entity : targets) {
            if (entity instanceof Player player) {
                player.addEffect(new MobEffectInstance(instance));
            } else if (entity instanceof net.minecraft.world.entity.LivingEntity living) {
                living.addEffect(new MobEffectInstance(instance));
            }
        }
    }

    private static void damageTargets(GazeSpellContext ctx, GazeDefinition.Action action) {
        JsonObject data = action.data();
        float amount = (float) GazeJsonHelper.getDouble(data, "amount", 2.0);

        List<Entity> targets = ctx.target().entities();
        if (targets.isEmpty() && ctx.target().entity() != null) {
            targets = List.of(ctx.target().entity());
        }

        for (Entity entity : targets) {
            entity.hurt(ctx.player().damageSources().magic(), amount);
        }
    }

    private static void spawnEntity(GazeSpellContext ctx, GazeDefinition.Action action) {
        if (!(ctx.level() instanceof ServerLevel serverLevel)) return;

        JsonObject data = action.data();
        String entityId = GazeJsonHelper.getString(data, "entity", "");
        int count = GazeJsonHelper.getInt(data, "count", 1);

        ResourceLocation rl = ResourceLocation.tryParse(entityId);
        if (rl == null) return;

        EntityType<?> type = BuiltInRegistries.ENTITY_TYPE.get(rl);
        BlockPos center = ctx.target().getCenterPos(ctx.player().blockPosition());

        for (int i = 0; i < count; i++) {
            type.spawn(serverLevel, center, MobSpawnType.TRIGGERED);
        }
    }

    private static void setBlock(GazeSpellContext ctx, GazeDefinition.Action action) {
        if (!(ctx.level() instanceof ServerLevel serverLevel)) return;

        JsonObject data = action.data();
        String blockId = GazeJsonHelper.getString(data, "block", "");
        if (blockId.isBlank()) return;

        ResourceLocation rl = ResourceLocation.tryParse(blockId);
        if (rl == null) return;

        Block block = BuiltInRegistries.BLOCK.get(rl);
        BlockPos pos = ctx.target().getCenterPos(ctx.player().blockPosition());
        serverLevel.setBlockAndUpdate(pos, block.defaultBlockState());
    }

    private static void runCommand(GazeSpellContext ctx, GazeDefinition.Action action) {
        if (!(ctx.player() instanceof ServerPlayer player)) return;

        JsonObject data = action.data();
        String command = GazeJsonHelper.getString(data, "command", "");
        if (command.isBlank()) return;

        String resolved = command
                .replace("%player%", player.getName().getString())
                .replace("%x%", String.valueOf(player.getX()))
                .replace("%y%", String.valueOf(player.getY()))
                .replace("%z%", String.valueOf(player.getZ()));

        player.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), resolved);
    }
}
