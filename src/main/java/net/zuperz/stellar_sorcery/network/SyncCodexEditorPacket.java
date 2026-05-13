package net.zuperz.stellar_sorcery.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.data.CodexDataLoader;
import net.zuperz.stellar_sorcery.data.CodexEditorPersistence;
import net.zuperz.stellar_sorcery.data.CodexEditorProject;
import net.zuperz.stellar_sorcery.screen.CodexEditorScreen;

public record SyncCodexEditorPacket(String json, String message, boolean openScreen) implements CustomPacketPayload {
    private static final int MAX_JSON_LENGTH = 1_048_576;
    private static final int MAX_MESSAGE_LENGTH = 32767;

    public static final Type<SyncCodexEditorPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "sync_codex_editor"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncCodexEditorPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeUtf(pkt.json, MAX_JSON_LENGTH);
                        buf.writeUtf(pkt.message, MAX_MESSAGE_LENGTH);
                        buf.writeBoolean(pkt.openScreen);
                    },
                    buf -> new SyncCodexEditorPacket(
                            buf.readUtf(MAX_JSON_LENGTH),
                            buf.readUtf(MAX_MESSAGE_LENGTH),
                            buf.readBoolean()
                    )
            );

    @Override
    public Type<SyncCodexEditorPacket> type() {
        return TYPE;
    }

    public static void handle(SyncCodexEditorPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Minecraft minecraft = Minecraft.getInstance();
            CodexEditorProject project = null;

            if (msg.json != null && !msg.json.isBlank()) {
                project = CodexEditorPersistence.fromJson(msg.json);
                CodexDataLoader.applyEditorProject(project);
            }

            if (msg.openScreen) {
                if (project != null) {
                    minecraft.setScreen(new CodexEditorScreen(project));
                }
            } else if (minecraft.screen instanceof CodexEditorScreen editor && project != null) {
                editor.handleServerSync(project);
            }

            if (minecraft.player != null && msg.message != null && !msg.message.isBlank()) {
                minecraft.player.displayClientMessage(Component.literal(msg.message), false);
            }
        });
    }
}
