package net.zuperz.stellar_sorcery.component;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.neoforged.fml.loading.FMLPaths;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class EssenceNameLoader {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Map<String, String> nameMap = new HashMap<>();

    public static void load() {
        System.out.println("load");
        try (InputStreamReader reader = new InputStreamReader(
                EssenceNameLoader.class.getResourceAsStream("/data/stellar_sorcery/essence_names.json"))) {

            if (reader == null) {
                System.err.println("[EssenceNameLoader] Couldn't find essence_names.json in resources!");
                return;
            }

            Type mapType = new TypeToken<Map<String, String>>() {}.getType();
            Map<String, String> parsed = GSON.fromJson(reader, mapType);

            nameMap.clear();

            for (Map.Entry<String, String> entry : parsed.entrySet()) {
                String cleanKey = entry.getKey().replace(" ", "");
                nameMap.put(cleanKey, entry.getValue());
            }

        } catch (Exception e) {
            System.err.println("[EssenceNameLoader] Failed to load essence_names.json: " + e.getMessage());
        }
    }

    public static String getCustomTranslationKey(String key) {
        return nameMap.get(key);
    }
}