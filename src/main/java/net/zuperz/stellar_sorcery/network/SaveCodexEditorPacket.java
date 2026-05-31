package net.zuperz.stellar_sorcery.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.data.CodexDataLoader;
import net.zuperz.stellar_sorcery.data.CodexEditorPersistence;
import net.zuperz.stellar_sorcery.data.CodexEditorProject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

public record SaveCodexEditorPacket(String json) implements CustomPacketPayload {
    private static final int MAX_JSON_LENGTH = 1_048_576;

    public static final Type<SaveCodexEditorPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "save_codex_editor"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SaveCodexEditorPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> buf.writeUtf(pkt.json, MAX_JSON_LENGTH),
                    buf -> new SaveCodexEditorPacket(buf.readUtf(MAX_JSON_LENGTH))
            );

    @Override
    public Type<SaveCodexEditorPacket> type() {
        return TYPE;
    }

    public static void handle(SaveCodexEditorPacket msg, IPayloadContext ctx) {
        if (!(ctx.player() instanceof ServerPlayer player)) {
            return;
        }

        ctx.enqueueWork(() -> {
            if (!player.hasPermissions(2)) {
                PacketDistributor.sendToPlayer(player, new SyncCodexEditorPacket("", "You need operator permissions to edit the codex.", false));
                return;
            }

            try {
                MinecraftServer server = player.server;
                CodexEditorProject project = CodexEditorPersistence.fromJson(msg.json());
                project = CodexEditorPersistence.sanitizeProject(project);

                Path datapackDir = server
                        .getWorldPath(net.minecraft.world.level.storage.LevelResource.DATAPACK_DIR)
                        .resolve(project.packId);

                createBackup(datapackDir, project.packId);
                deleteDirectoryContents(datapackDir);

                CodexEditorPersistence.writeDatapack(server, project);

                var packRepository = server.getPackRepository();
                packRepository.reload();

                Set<String> selectedIds = new LinkedHashSet<>(packRepository.getSelectedIds());
                String packId = resolvePackId(packRepository.getAvailableIds(), project.packId);
                selectedIds.add(packId);

                server.reloadResources(selectedIds)
                        .thenRun(() -> {
                            CodexEditorProject refreshedProject = CodexEditorPersistence.fromCurrentData();
                            String refreshedJson = CodexEditorPersistence.toJson(refreshedProject);

                            for (ServerPlayer onlinePlayer : server.getPlayerList().getPlayers()) {
                                PacketDistributor.sendToPlayer(
                                        onlinePlayer,
                                        new SyncCodexEditorPacket(
                                                refreshedJson,
                                                "",
                                                false
                                        )
                                );
                            }

                            PacketDistributor.sendToPlayer(
                                    player,
                                    new SyncCodexEditorPacket("", "Codex datapack saved.", false)
                            );
                        })
                        .exceptionally(throwable -> {
                            PacketDistributor.sendToPlayer(
                                    player,
                                    new SyncCodexEditorPacket("", "Failed to reload codex datapack: " + rootMessage(throwable), false)
                            );
                            return null;
                        });
            } catch (Exception exception) {
                PacketDistributor.sendToPlayer(
                        player,
                        new SyncCodexEditorPacket("", "Failed to save codex datapack: " + rootMessage(exception), false)
                );
            }
        });
    }

    private static String rootMessage(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null) {
            current = current.getCause();
        }
        String message = current.getMessage();
        return message == null || message.isBlank() ? current.getClass().getSimpleName() : message;
    }

    private static String resolvePackId(java.util.Collection<String> availableIds, String folderName) {
        if (availableIds.contains(folderName)) {
            return folderName;
        }

        String filePackId = "file/" + folderName;
        if (availableIds.contains(filePackId)) {
            return filePackId;
        }

        for (String availableId : availableIds) {
            if (availableId.endsWith("/" + folderName) || availableId.endsWith("\\" + folderName) || availableId.endsWith(folderName)) {
                return availableId;
            }
        }

        return folderName;
    }

    private static void deleteDirectoryContents(Path directory) throws IOException {
        if (!Files.exists(directory)) return;

        try (var paths = Files.walk(directory)) {
            paths.sorted(java.util.Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    private static void createBackup(Path datapackDir, String packName) throws IOException {
        if (!Files.exists(datapackDir)) {
            return;
        }

        String timestamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));

        Path backupDir = datapackDir.getParent()
                .resolve(packName + "_backup")
                .resolve(timestamp);

        Files.createDirectories(backupDir);

        try (var paths = Files.walk(datapackDir)) {
            paths.forEach(source -> {
                try {
                    Path target = backupDir.resolve(datapackDir.relativize(source));

                    if (Files.isDirectory(source)) {
                        Files.createDirectories(target);
                    } else {
                        Files.copy(
                                source,
                                target,
                                java.nio.file.StandardCopyOption.REPLACE_EXISTING
                        );
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    public static Path getDatapackPath(MinecraftServer server, String packId) {
        return server.getWorldPath(LevelResource.DATAPACK_DIR)
                .resolve(packId);
    }
}
