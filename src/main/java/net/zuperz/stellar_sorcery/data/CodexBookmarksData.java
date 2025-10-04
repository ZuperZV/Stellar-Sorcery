package net.zuperz.stellar_sorcery.data;

import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;

public class CodexBookmarksData {
    private static final String TAG_BOOKMARKS = "stellar_sorcery_bookmarks";

    public static List<String> getBookmarks(Player player) {
        List<String> list = new ArrayList<>();
        if (player.getPersistentData().contains(TAG_BOOKMARKS)) {
            ListTag tagList = player.getPersistentData().getList(TAG_BOOKMARKS, 8);
            for (int i = 0; i < tagList.size(); i++) {
                list.add(tagList.getString(i));
            }
        }
        return list;
    }

    public static void addBookmark(Player player, String entryId) {
        List<String> bookmarks = getBookmarks(player);
        if (!bookmarks.contains(entryId) && bookmarks.size() < 24) {
            bookmarks.add(entryId);
            save(player, bookmarks);
        }
    }

    public static void removeBookmark(Player player, String entryId) {
        List<String> bookmarks = getBookmarks(player);
        bookmarks.remove(entryId);
        save(player, bookmarks);
    }

    private static void save(Player player, List<String> bookmarks) {
        ListTag listTag = new ListTag();
        for (String id : bookmarks) {
            listTag.add(StringTag.valueOf(id));
        }
        player.getPersistentData().put(TAG_BOOKMARKS, listTag);
    }
}
