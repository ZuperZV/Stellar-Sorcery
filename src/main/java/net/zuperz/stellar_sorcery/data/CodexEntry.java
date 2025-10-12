package net.zuperz.stellar_sorcery.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.List;

public class CodexEntry {
    public List<CodexPage> right_side;
    public String id;
    public List<String> search_items;
    public String title;
    public String type;
    public String icon;
    public List<String> related;

    private static final Gson GSON = new Gson();

    public static CodexEntry fromJson(JsonObject json) {
        return GSON.fromJson(json, CodexEntry.class);
    }
}

