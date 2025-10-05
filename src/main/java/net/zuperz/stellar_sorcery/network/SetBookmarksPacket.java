package net.zuperz.stellar_sorcery.network;

import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.data.IModPlayerData;

import java.util.ArrayList;

public record SetBookmarksPacket(String entryId, boolean isSetter) implements CustomPacketPayload {
    public static final Type<SetBookmarksPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "set_bookmark"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SetBookmarksPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeUtf(pkt.entryId);
                        buf.writeBoolean(pkt.isSetter);
                    },
                    buf -> new SetBookmarksPacket(buf.readUtf(), buf.readBoolean())
            );

    @Override
    public Type<SetBookmarksPacket> type() { return TYPE; }

    public static void handle(SetBookmarksPacket msg, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            ctx.enqueueWork(() -> {
                if (player instanceof IModPlayerData data) {
                    ArrayList<String> bookmarks = data.stellarSorceryGetBookmarks();

                    if (msg.isSetter()) {
                        if (!bookmarks.contains(msg.entryId()) && bookmarks.size() < 24) {
                            bookmarks.add(msg.entryId());
                            StellarSorcery.LOGGER.info("Tilføjede bogmærke '{}' for {}", msg.entryId(), player.getName().getString());
                        }
                    }
                    else {
                        bookmarks.remove(msg.entryId());
                        StellarSorcery.LOGGER.info("Fjernede bogmærke '{}' for {}", msg.entryId(), player.getName().getString());
                    }

                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                            player,
                            new SyncBookmarksPacket(bookmarks)
                    );

                    net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(
                            player, new SyncBookmarksPacket(bookmarks)
                    );
                }
            });
        }
    }
}