package net.zuperz.stellar_sorcery.event;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralAltarBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.SoulCandleBlockEntity;
import net.zuperz.stellar_sorcery.capability.RecipesHelper.SoulCandleCommand;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.data.CodexBookmarksData;
import net.zuperz.stellar_sorcery.data.IModPlayerData;
import net.zuperz.stellar_sorcery.data.SigilDataLoader;
import net.zuperz.stellar_sorcery.effect.ModEffects;
import net.zuperz.stellar_sorcery.item.ModItems;
import net.zuperz.stellar_sorcery.item.custom.SigilItem;
import net.zuperz.stellar_sorcery.network.SyncBookmarksPacket;
import net.zuperz.stellar_sorcery.shaders.post.EssenceBottleItemShaderRenderer;

import java.util.ArrayList;

@EventBusSubscriber(modid = StellarSorcery.MOD_ID)
public class ModEvents {

    @SubscribeEvent
    public static void onLivingDamagePre(LivingDamageEvent.Pre event) {
        LivingEntity entity = event.getEntity();

        if (!entity.hasEffect(ModEffects.VULNERABILITY)) return;

        if (event.getSource().is(DamageTypeTags.BYPASSES_RESISTANCE)) return;

        int amplifier = entity.getEffect(ModEffects.VULNERABILITY).getAmplifier();

        DamageContainer container = event.getContainer();

        float oldDamage = container.getNewDamage();

        float increase = (amplifier + 1) * 5F;
        float newDamage = oldDamage * (25F + increase) / 25F;

        container.setNewDamage(newDamage);

        float extraDamage = newDamage - oldDamage;
        container.setReduction(
                DamageContainer.Reduction.MOB_EFFECTS,
                -extraDamage
        );
    }

    @SubscribeEvent
    public static void onAnimalSacrifice(LivingDeathEvent event) {
        Level level = event.getEntity().level();

        AABB blocksAround = AABB.ofSize(event.getEntity().blockPosition().getCenter(), 5, 3, 5);

        int minX = Mth.floor(blocksAround.minX);
        int minY = Mth.floor(blocksAround.minY);
        int minZ = Mth.floor(blocksAround.minZ);
        int maxX = Mth.floor(blocksAround.maxX);
        int maxY = Mth.floor(blocksAround.maxY);
        int maxZ = Mth.floor(blocksAround.maxZ);

        for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX, maxY, maxZ)) {
            if (level.getBlockEntity(pos) instanceof AstralAltarBlockEntity altar) {
                altar.setSacrificedEntity(event.getEntity().getType());

                if (level instanceof ServerLevel serverLevel) {
                    EntityType.LIGHTNING_BOLT.spawn(serverLevel, event.getEntity().blockPosition(), MobSpawnType.TRIGGERED)
                            .setVisualOnly(true);
                }
                break;

            } else if (level.getBlockEntity(pos) instanceof SoulCandleBlockEntity soulCandleBlockEntity) {
                soulCandleBlockEntity.setSacrificedEntity(event.getEntity().getType());

                if (level instanceof ServerLevel serverLevel) {
                    EntityType.LIGHTNING_BOLT.spawn(serverLevel, event.getEntity().blockPosition(), MobSpawnType.TRIGGERED)
                            .setVisualOnly(true);
                }
                break;
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        if (player instanceof IModPlayerData data) {
            ArrayList<String> bookmarks = data.stellarSorceryGetBookmarks();
            PacketDistributor.sendToPlayer(player, new SyncBookmarksPacket(bookmarks));
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) return;

        Player player = event.getEntity();

        player.getArmorSlots().forEach(itemStack -> {

            if (itemStack.get(ModDataComponentTypes.SIGIL.get()) == null) return;

            for (int i = 0; i < itemStack.get(ModDataComponentTypes.SIGIL.get()).getSigils().size(); i++) {
                if (itemStack.get(ModDataComponentTypes.SIGIL.get()).getSigils().get(i) == ItemStack.EMPTY) return;
            }

            for (int i = 0; i < itemStack.get(ModDataComponentTypes.SIGIL.get()).getSigils().size(); i++) {
                String activeSigilName = SigilItem.getActiveSigil(itemStack.get(ModDataComponentTypes.SIGIL.get()).getSigils().get(i));

                SigilDataLoader.SigilDefinition sigil = SigilDataLoader.getByName(activeSigilName);
                if (sigil == null) {
                    return;
                }

                for (MobEffectInstance effect : sigil.effects()) {
                    player.addEffect(new MobEffectInstance(effect));
                }

                if (sigil.shader() != null) {
                    EssenceBottleItemShaderRenderer.enableShader(
                            sigil.shader().shaderId,
                            sigil.shader().durationTicks
                    );
                }

                for (SoulCandleCommand cmd : sigil.commands()) {
                    switch (cmd.getTrigger()) {
                        case EACH_TICK -> runSigilCommand(player, cmd);
                        default -> {}
                    }
                }
            }
        });
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Level world = event.getEntity().getCommandSenderWorld();
        BlockPos pos = event.getPos();

        if (world.getBlockState(pos).getBlock() == ModBlocks.DRIFTSOIL.get()) {
            ItemStack itemStack = event.getItemStack();
            if (itemStack.canPerformAction(ItemAbilities.HOE_TILL)) {

                Player player = event.getEntity();
                itemStack.hurtAndBreak(1, player,
                        LivingEntity.getSlotForHand(event.getHand()));

                world.setBlockAndUpdate(pos, ModBlocks.TILLED_DRIFTSOIL.get().defaultBlockState());

                event.setCancellationResult(InteractionResult.SUCCESS);
                event.setCanceled(true);
            }
        }
    }

    private static void runSigilCommand(Player player, SoulCandleCommand cmd) {
        MinecraftServer server = player.getServer();
        if (server == null) return;

        String baseCommand = cmd.getCommand()
                .replace("%player%", player.getName().getString())
                .replace("%x%", String.valueOf(player.getX()))
                .replace("%y%", String.valueOf(player.getY()))
                .replace("%z%", String.valueOf(player.getZ()));

        switch (cmd.getTarget()) {
            case SOUL_CANDLE -> {
                server.getCommands().performPrefixedCommand(server.createCommandSourceStack(), baseCommand);
            }

            case PLAYER_CLOSEST -> {
                Player target = player.level().getNearestPlayer(player, 5);
                if (target != null) {
                    String replaced = baseCommand
                            .replace("%player%", target.getName().getString())
                            .replace("%x%", String.valueOf(target.getX()))
                            .replace("%y%", String.valueOf(target.getY()))
                            .replace("%z%", String.valueOf(target.getZ()));

                    boolean isExecuteCommand = replaced.trim().startsWith("/execute")
                            || replaced.trim().startsWith("execute ");

                    CommandSourceStack sourceStack = isExecuteCommand
                            ? player.createCommandSourceStack()
                            : server.createCommandSourceStack();

                    server.getCommands().performPrefixedCommand(sourceStack, replaced);
                }
            }

            case ALL_PLAYERS -> {
                for (Player p : server.getPlayerList().getPlayers()) {
                    String replaced = baseCommand
                            .replace("%player%", p.getName().getString())
                            .replace("%x%", String.valueOf(p.getX()))
                            .replace("%y%", String.valueOf(p.getY()))
                            .replace("%z%", String.valueOf(p.getZ()));

                    server.getCommands().performPrefixedCommand(p.createCommandSourceStack(), replaced);
                }
            }

            case PLAYERS_IN_5_BLOCKS -> {
                for (Player p : player.level().getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(5))) {
                    String replaced = baseCommand
                            .replace("%player%", p.getName().getString())
                            .replace("%x%", String.valueOf(p.getX()))
                            .replace("%y%", String.valueOf(p.getY()))
                            .replace("%z%", String.valueOf(p.getZ()));

                    server.getCommands().performPrefixedCommand(p.createCommandSourceStack(), replaced);
                }
            }
        }
    }
}
