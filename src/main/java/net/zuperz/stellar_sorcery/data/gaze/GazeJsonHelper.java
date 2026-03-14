package net.zuperz.stellar_sorcery.data.gaze;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

public final class GazeJsonHelper {

    private GazeJsonHelper() {}

    public static String getString(JsonObject obj, String key, String fallback) {
        JsonElement el = obj.get(key);
        return el != null && el.isJsonPrimitive() ? el.getAsString() : fallback;
    }

    public static int getInt(JsonObject obj, String key, int fallback) {
        JsonElement el = obj.get(key);
        return el != null && el.isJsonPrimitive() ? el.getAsInt() : fallback;
    }

    public static double getDouble(JsonObject obj, String key, double fallback) {
        JsonElement el = obj.get(key);
        return el != null && el.isJsonPrimitive() ? el.getAsDouble() : fallback;
    }

    public static boolean getBoolean(JsonObject obj, String key, boolean fallback) {
        JsonElement el = obj.get(key);
        return el != null && el.isJsonPrimitive() ? el.getAsBoolean() : fallback;
    }

    public static JsonObject getObject(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return el != null && el.isJsonObject() ? el.getAsJsonObject() : null;
    }

    public static JsonArray getArray(JsonObject obj, String key) {
        JsonElement el = obj.get(key);
        return el != null && el.isJsonArray() ? el.getAsJsonArray() : null;
    }
}
