package net.zuperz.stellar_sorcery.mixin;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.zuperz.stellar_sorcery.capability.RecipesHelper.SoulCandleCommand;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.component.SigilData;
import net.zuperz.stellar_sorcery.data.SigilDataLoader;
import net.zuperz.stellar_sorcery.item.custom.SigilItem;
import net.zuperz.stellar_sorcery.shaders.post.EssenceBottleItemShaderRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SwordItem.class)
public class SwordItemSigilMixin {

    @Inject(method = "postHurtEnemy", at = @At("HEAD"), cancellable = true)

    private void injectPostHurtEnemy(ItemStack itemStack, LivingEntity living, LivingEntity playerEntity, CallbackInfo cir) {

        SigilData sigilData = itemStack.get(ModDataComponentTypes.SIGIL);
        if (sigilData == null || sigilData.getSigils().isEmpty()) {
            return;
        }

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
                living.addEffect(new MobEffectInstance(effect));
            }

            if (sigil.shader() != null) {
                EssenceBottleItemShaderRenderer.enableShader(
                        sigil.shader().shaderId,
                        sigil.shader().durationTicks
                );
            }

            for (SoulCandleCommand cmd : sigil.commands()) {
                switch (cmd.getTrigger()) {
                    case EACH_TICK -> runSigilCommand(living, cmd);
                    default -> {}
                }
            }

        }
    }

    private static void runSigilCommand(LivingEntity player, SoulCandleCommand cmd) {
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
