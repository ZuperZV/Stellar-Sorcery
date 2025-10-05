package net.zuperz.stellar_sorcery.data;

import java.util.ArrayList;

public class ClientPlayerData implements IModPlayerData {
    public static final ClientPlayerData INSTANCE = new ClientPlayerData();

    private final ArrayList<String> bookmarks = new ArrayList<>();

    @Override
    public ArrayList<String> stellarSorceryGetBookmarks() {
        return bookmarks;
    }

    @Override
    public void stellarSorcerySetBookmarks(ArrayList<String> list) {
        bookmarks.clear();
        bookmarks.addAll(list);
    }
}
