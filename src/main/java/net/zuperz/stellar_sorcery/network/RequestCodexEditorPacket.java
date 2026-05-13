package net.zuperz.stellar_sorcery.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.data.CodexEditorPersistence;
import net.zuperz.stellar_sorcery.data.CodexEditorProject;

public record RequestCodexEditorPacket() implements CustomPacketPayload {
    public static final Type<RequestCodexEditorPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "request_codex_editor"));

    public static final StreamCodec<RegistryFriendlyByteBuf, RequestCodexEditorPacket> STREAM_CODEC =
            StreamCodec.unit(new RequestCodexEditorPacket());

    @Override
    public Type<RequestCodexEditorPacket> type() {
        return TYPE;
    }

    public static void handle(RequestCodexEditorPacket msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) {
            return;
        }

        ctx.enqueueWork(() -> {
            if (!player.hasPermissions(2)) {
                PacketDistributor.sendToPlayer(player, new SyncCodexEditorPacket("", "You need operator permissions to edit the codex.", false));
                return;
            }

            CodexEditorProject project = CodexEditorPersistence.fromCurrentData();
            PacketDistributor.sendToPlayer(player, new SyncCodexEditorPacket(CodexEditorPersistence.toJson(project), "", true));
        });
    }
}
