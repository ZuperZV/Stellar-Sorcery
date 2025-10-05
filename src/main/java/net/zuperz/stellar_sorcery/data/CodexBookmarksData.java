package net.zuperz.stellar_sorcery.data;

import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public class CodexBookmarksData {

    public static List<String> getBookmarks(Player player) {
        if (player == null) return List.of();

        if (player instanceof ServerPlayer serverPlayer && serverPlayer instanceof IModPlayerData serverData) {
            return new ArrayList<>(serverData.stellarSorceryGetBookmarks());
        }

        return new ArrayList<>(ClientPlayerData.INSTANCE.stellarSorceryGetBookmarks());
    }

    public static void addBookmark(Player player, String entryId) {
        if (player instanceof IModPlayerData data) {
            ArrayList<String> bookmarks = data.stellarSorceryGetBookmarks();
            if (!bookmarks.contains(entryId) && bookmarks.size() < 24) {
                bookmarks.add(entryId);
                data.stellarSorcerySetBookmarks(bookmarks);
            }
        } else {
            ArrayList<String> bookmarks = ClientPlayerData.INSTANCE.stellarSorceryGetBookmarks();
            if (!bookmarks.contains(entryId) && bookmarks.size() < 24) {
                bookmarks.add(entryId);
            }
        }
    }

    public static void removeBookmark(Player player, String entryId) {
        if (player instanceof IModPlayerData data) {
            ArrayList<String> bookmarks = data.stellarSorceryGetBookmarks();
            bookmarks.remove(entryId);
            data.stellarSorcerySetBookmarks(bookmarks);
        } else {
            ArrayList<String> bookmarks = ClientPlayerData.INSTANCE.stellarSorceryGetBookmarks();
            bookmarks.remove(entryId);
        }
    }

    public static void syncClientBookmarks(List<String> bookmarks) {
        ClientPlayerData.INSTANCE.stellarSorcerySetBookmarks(new ArrayList<>(bookmarks));
    }
}