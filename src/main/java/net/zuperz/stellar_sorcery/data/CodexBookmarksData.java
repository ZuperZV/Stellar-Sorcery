package net.zuperz.stellar_sorcery.data;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class CodexBookmarksData {

    private static final ArrayList<String> clientBookmarks = new ArrayList<>();

    public static List<String> getBookmarks(Player player) {
        if (player == null) return List.of();

        // --- SERVER ---
        if (player instanceof ServerPlayer serverPlayer && serverPlayer instanceof IModPlayerData serverData) {
            return new ArrayList<>(serverData.stellarSorceryGetBookmarks());
        }

        // --- CLIENT ---
        if (Minecraft.getInstance().player != null) {
            return new ArrayList<>(clientBookmarks);
        }

        return List.of();
    }

    public static void addBookmark(Player player, String entryId) {
        if (player instanceof IModPlayerData data) {
            ArrayList<String> bookmarks = data.stellarSorceryGetBookmarks();
            if (!bookmarks.contains(entryId) && bookmarks.size() < 24) {
                bookmarks.add(entryId);
                data.stellarSorcerySetBookmarks(bookmarks);
            }
        } else {
            if (!clientBookmarks.contains(entryId) && clientBookmarks.size() < 24) {
                clientBookmarks.add(entryId);
            }
        }
    }

    public static void removeBookmark(Player player, String entryId) {
        if (player instanceof IModPlayerData data) {
            ArrayList<String> bookmarks = data.stellarSorceryGetBookmarks();
            bookmarks.remove(entryId);
            data.stellarSorcerySetBookmarks(bookmarks);
        } else {
            clientBookmarks.remove(entryId);
        }
    }

    public static void syncClientBookmarks(List<String> bookmarks) {
        clientBookmarks.clear();
        clientBookmarks.addAll(bookmarks);
    }
}