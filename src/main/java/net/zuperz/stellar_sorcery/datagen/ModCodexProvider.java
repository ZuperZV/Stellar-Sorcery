package net.zuperz.stellar_sorcery.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.zuperz.stellar_sorcery.StellarSorcery;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class ModCodexProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public ModCodexProvider(PackOutput packOutput) {
        this.pathProvider = packOutput.createPathProvider(PackOutput.Target.DATA_PACK, "codex_entries");
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cachedOutput) {
        List<CompletableFuture<?>> writes = new ArrayList<>();

        for (GeneratedEntry entry : buildEntries()) {
            Path path = this.pathProvider.json(ResourceLocation.fromNamespaceAndPath(
                    StellarSorcery.MOD_ID,
                    entry.categoryFolder + "/tier_" + entry.tier + "/" + entry.fileName
            ));
            writes.add(DataProvider.saveStable(cachedOutput, entry.json, path));
        }

        return CompletableFuture.allOf(writes.toArray(CompletableFuture[]::new));
    }

    @Override
    public String getName() {
        return "Stellar Sorcery Codex Entries";
    }

    private List<GeneratedEntry> buildEntries() {
        List<GeneratedEntry> entries = new ArrayList<>();

        String codex = "codex_stellar_sorcery-codex_arcanum";
        String flora = "flora_stellar_sorcery-fritillaria_meleagris";
        String rituals = "rituals_stellar_sorcery-soul_candle";
        String lunar = "lunar_stellar_sorcery-moonshine_catalyst";
        String astral = "astral_stellar_sorcery-astral_altar";

        entries.add(entry(codex, 1, 3, "vital_stump", "stellar_sorcery:vital_stump",
                List.of("stellar_sorcery:vital_stump", "vital stump", "stump", "ritual", "flora"),
                List.of("stump", "moonshine_catalyst", "calendula"),
                page(
                        text("codex_arcanum.stellar_sorcery.entry.vital_stump.text.1"),
                        recipe(
                                List.of("DCD", "BAB"),
                                Map.of(
                                        "A", "stellar_sorcery:stump",
                                        "B", "stellar_sorcery:calendula",
                                        "C", "stellar_sorcery:red_campion",
                                        "D", "minecraft:gold_ingot"
                                ),
                                "stellar_sorcery:vital_stump"
                        ),
                        text("codex_arcanum.stellar_sorcery.entry.vital_stump.text.2")
                )
        ));

        return entries;
    }

    private GeneratedEntry entry(
            String categoryFolder,
            int tier,
            int order,
            String id,
            String icon,
            List<String> searchItems,
            List<String> related,
            JsonObject... pages
    ) {
        JsonObject root = new JsonObject();
        root.addProperty("id", id);
        root.addProperty("title_key", "codex_arcanum.stellar_sorcery.guide." + id + ".title");
        root.addProperty("type", "item");
        root.addProperty("icon", icon);
        root.add("search_items", toArray(searchItems));
        root.add("right_side", toArray(List.of(pages)));
        root.add("related", toArray(related));

        return new GeneratedEntry(categoryFolder, tier, String.format("%02d_%s", order, id), root);
    }

    private JsonObject page(JsonObject... modules) {
        JsonObject page = new JsonObject();
        page.add("modules", toArray(List.of(modules)));
        return page;
    }

    private JsonObject text(String key) {
        JsonObject module = new JsonObject();
        module.addProperty("module_type", "text");
        module.addProperty("text_key", key.replace(".entry.", ".guide."));
        return module;
    }

    private JsonObject recipe(List<String> pattern, Map<String, String> key, String result) {
        JsonObject module = new JsonObject();
        module.addProperty("module_type", "recipe");
        module.addProperty("recipe_type", "crafting_table");
        module.add("pattern", toArray(pattern));

        JsonObject keyObject = new JsonObject();
        key.forEach(keyObject::addProperty);
        module.add("key", keyObject);
        module.addProperty("result", result);
        return module;
    }

    private JsonObject furnace(String input, String output, float experience, int cookingTime) {
        JsonObject module = new JsonObject();
        module.addProperty("module_type", "furnace_recipe");
        module.addProperty("input", input);
        module.addProperty("output", output);
        module.addProperty("experience", experience);
        module.addProperty("cooking_time", cookingTime);
        return module;
    }

    private JsonArray toArray(List<?> values) {
        JsonArray array = new JsonArray();
        for (Object value : values) {
            if (value instanceof String stringValue) {
                array.add(stringValue);
            } else if (value instanceof JsonElement jsonElement) {
                array.add(jsonElement);
            }
        }
        return array;
    }

    private record GeneratedEntry(String categoryFolder, int tier, String fileName, JsonObject json) {
    }
}
