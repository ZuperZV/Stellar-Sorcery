package net.zuperz.stellar_sorcery.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.gaze.GazeSpellEngine;

public record GazeCastRequestPacket(int slotIndex) implements CustomPacketPayload {
    public static final Type<GazeCastRequestPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "gaze_cast_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GazeCastRequestPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeInt(pkt.slotIndex),
                    buf -> new GazeCastRequestPacket(buf.readInt())
            );

    @Override
    public Type<GazeCastRequestPacket> type() { return TYPE; }

    public static void handle(GazeCastRequestPacket msg, IPayloadContext ctx) {
        if (ctx.player() instanceof ServerPlayer player) {
            ctx.enqueueWork(() -> GazeSpellEngine.tryCast(player, msg.slotIndex));
        }
    }
}
