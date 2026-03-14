package net.zuperz.stellar_sorcery.gaze;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zuperz.stellar_sorcery.data.gaze.GazeDefinition;
import net.zuperz.stellar_sorcery.data.gaze.GazeRegistry;
import net.zuperz.stellar_sorcery.item.custom.GazeItem;
import net.zuperz.stellar_sorcery.network.GazeCastPacket;
import net.zuperz.stellar_sorcery.screen.Helpers.IExtraSlotsProvider;

public final class GazeSpellEngine {

    private GazeSpellEngine() {}

    public static boolean tryCast(Player player, int slotIndex) {
        if (!(player instanceof IExtraSlotsProvider provider)) return false;

        Container extra = provider.getExtraSlots();
        if (slotIndex < 0 || slotIndex >= extra.getContainerSize()) return false;

        ItemStack stack = extra.getItem(slotIndex);
        if (stack.isEmpty() || !(stack.getItem() instanceof GazeItem)) {
            return fail(player, "gaze.stellar_sorcery.fail.no_gaze");
        }

        return tryCast(player, stack);
    }

    public static boolean tryCast(Player player, ItemStack gazeStack) {
        if (player.level().isClientSide()) return false;

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(gazeStack.getItem());
        if (itemId == null) return fail(player, "gaze.stellar_sorcery.fail.no_definition");

        GazeDefinition def = GazeRegistry.getByItemId(itemId);
        if (def == null) return fail(player, "gaze.stellar_sorcery.fail.no_definition");

        if (player.getCooldowns().isOnCooldown(gazeStack.getItem())) {
            return fail(player, "gaze.stellar_sorcery.fail.cooldown");
        }

        GazeMutableStats stats = GazeMutableStats.fromDefinition(def);
        GazeModifierEngine.apply(stats, def.modifiers());

        GazeCostHandler.CostResult costResult = GazeCostHandler.check(player, stats);
        if (costResult != GazeCostHandler.CostResult.OK) {
            return switch (costResult) {
                case NOT_ENOUGH_MANA -> fail(player, "gaze.stellar_sorcery.fail.mana");
                case NOT_ENOUGH_XP -> fail(player, "gaze.stellar_sorcery.fail.xp");
                case NOT_ENOUGH_HEALTH -> fail(player, "gaze.stellar_sorcery.fail.health");
                default -> fail(player, "gaze.stellar_sorcery.fail.cost");
            };
        }

        GazeSpellContext preContext = new GazeSpellContext(player, player.level(), gazeStack, def, stats, GazeTarget.empty());
        GazeTarget target = GazeTargetTypes.get(def.target().type()).resolve(preContext, def.target());
        if (target == null) return fail(player, "gaze.stellar_sorcery.fail.target");
        if (target.entity() == null && target.blockPos() == null && target.entities().isEmpty() && target.blocks().isEmpty()) {
            return fail(player, "gaze.stellar_sorcery.fail.target");
        }

        GazeSpellContext context = new GazeSpellContext(player, player.level(), gazeStack, def, stats, target);

        GazeSacrificeTypes.Handler sacrificeHandler = GazeSacrificeTypes.get(def.sacrifice().type());
        if (!sacrificeHandler.canPay(context, def.sacrifice())) {
            return fail(player, "gaze.stellar_sorcery.fail.sacrifice");
        }

        GazeCostHandler.pay(player, stats);
        sacrificeHandler.pay(context, def.sacrifice());

        GazeActionTypes.get(def.action().type()).execute(context, def.action());
        GazeParticleSpawner.spawn(context);
        triggerClientEffects(context);

        if (stats.cooldown > 0) {
            player.getCooldowns().addCooldown(gazeStack.getItem(), stats.cooldown);
        }

        return true;
    }

    private static boolean fail(Player player, String key) {
        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(Component.translatable(key), true);
        }
        return false;
    }

    private static void triggerClientEffects(GazeSpellContext context) {
        if (!(context.player() instanceof ServerPlayer serverPlayer)) return;

        GazeDefinition.HandEffect effect = context.definition().handEffect();
        String handAnimation = context.definition().handAnimation();

        boolean hasEffect = effect != null && effect.duration() > 0 && !"none".equalsIgnoreCase(effect.type());

        int animationDuration = effect != null && effect.duration() > 0 ? effect.duration() : 12;

        String effectType = hasEffect ? effect.type() : "";
        String effectColor = hasEffect ? effect.color() : "";
        String effectParticle = hasEffect ? effect.particle() : "";
        int effectDuration = hasEffect ? effect.duration() : 0;

        PacketDistributor.sendToPlayer(serverPlayer, new GazeCastPacket(
                context.player().getId(),
                handAnimation != null ? handAnimation : "",
                animationDuration,
                hasEffect,
                effectType,
                effectColor,
                effectParticle,
                effectDuration
        ));
    }
}
