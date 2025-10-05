package net.zuperz.stellar_sorcery.event;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralAltarBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.SoulCandleBlockEntity;
import net.zuperz.stellar_sorcery.data.CodexBookmarksData;
import net.zuperz.stellar_sorcery.data.IModPlayerData;
import net.zuperz.stellar_sorcery.network.SyncBookmarksPacket;

import java.util.ArrayList;

@EventBusSubscriber(modid = StellarSorcery.MOD_ID)
public class ModEvents {

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
}
