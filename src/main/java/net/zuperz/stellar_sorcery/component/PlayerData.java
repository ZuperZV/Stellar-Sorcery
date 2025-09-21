package net.zuperz.stellar_sorcery.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

public class PlayerData {
    private final UUID playerUUID;
    private final String playerName;

    public PlayerData(UUID playerUUID, String playerName) {
        this.playerUUID = playerUUID;
        this.playerName = playerName;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public String getPlayerName() {
        return playerName;
    }

    public Optional<ServerPlayer> getPlayer(Level level) {
        if (level.getServer() == null) return Optional.empty();
        return Optional.ofNullable(level.getServer().getPlayerList().getPlayer(playerUUID));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerData that)) return false;
        return Objects.equals(this.playerUUID, that.playerUUID);
    }

    @Override
    public int hashCode() {
        return Objects.hash(playerUUID);
    }

    public static final Codec<PlayerData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.xmap(UUID::fromString, UUID::toString).fieldOf("player_uuid").forGetter(PlayerData::getPlayerUUID),
            Codec.STRING.fieldOf("player_name").forGetter(PlayerData::getPlayerName)
    ).apply(instance, PlayerData::new));
}