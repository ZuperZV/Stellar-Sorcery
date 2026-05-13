package net.zuperz.stellar_sorcery.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CodexEditorProject {
    public String packId = "stellar_sorcery_codex_editor";
    public String packDescription = "Stellar Sorcery Codex editor";
    public List<Category> categories = new ArrayList<>();

    public void normalize() {
        if (packId == null || packId.isBlank()) {
            packId = "stellar_sorcery_codex_editor";
        }
        if (packDescription == null || packDescription.isBlank()) {
            packDescription = "Stellar Sorcery Codex editor";
        }
        if (categories == null) {
            categories = new ArrayList<>();
        }

        for (int i = 0; i < categories.size(); i++) {
            Category category = categories.get(i);
            if (category == null) {
                category = new Category();
                categories.set(i, category);
            }
            category.normalize(i);
        }
    }

    public static class Category {
        public String id = "category";
        public String title = "Category";
        public String icon = "minecraft:book";
        public int order;
        public List<Entry> entries = new ArrayList<>();

        public void normalize(int index) {
            if (id == null || id.isBlank()) {
                id = "category_" + (index + 1);
            }
            if (title == null || title.isBlank()) {
                title = "Category " + (index + 1);
            }
            if (icon == null || icon.isBlank()) {
                icon = "minecraft:book";
            }
            if (entries == null) {
                entries = new ArrayList<>();
            }

            order = index;

            for (int i = 0; i < entries.size(); i++) {
                Entry entry = entries.get(i);
                if (entry == null) {
                    entry = new Entry();
                    entries.set(i, entry);
                }
                entry.normalize(i);
            }
        }
    }

    public static class Entry {
        public String id = "entry";
        public String title = "Entry";
        public String type = "item";
        public String icon = "minecraft:book";
        public int tier = 1;
        public List<String> searchItems = new ArrayList<>();
        public List<String> related = new ArrayList<>();
        public List<Page> pages = new ArrayList<>();

        public void normalize(int index) {
            if (id == null || id.isBlank()) {
                id = "entry_" + (index + 1);
            }
            if (title == null || title.isBlank()) {
                title = "Entry " + (index + 1);
            }
            if (type == null || type.isBlank()) {
                type = "item";
            }
            if (icon == null || icon.isBlank()) {
                icon = "minecraft:book";
            }
            if (tier < 0) {
                tier = 0;
            }
            if (searchItems == null) {
                searchItems = new ArrayList<>();
            }
            if (related == null) {
                related = new ArrayList<>();
            }
            if (pages == null || pages.isEmpty()) {
                pages = new ArrayList<>();
                pages.add(new Page());
            }

            for (int i = 0; i < pages.size(); i++) {
                Page page = pages.get(i);
                if (page == null) {
                    page = new Page();
                    pages.set(i, page);
                }
                page.normalize();
            }
        }

        public String getTitleKey() {
            return "codex_arcanum.stellar_sorcery.guide." + id + ".title";
        }

        public String getTextKey(int pageIndex, int moduleIndex) {
            return "codex_arcanum.stellar_sorcery.guide." + id + ".page." + (pageIndex + 1) + ".module." + (moduleIndex + 1);
        }
    }

    public static class Page {
        public List<Module> modules = new ArrayList<>();

        public void normalize() {
            if (modules == null || modules.isEmpty()) {
                modules = new ArrayList<>();
                modules.add(new Module());
            }

            for (int i = 0; i < modules.size(); i++) {
                Module module = modules.get(i);
                if (module == null) {
                    module = new Module();
                    modules.set(i, module);
                }
                module.normalize();
            }
        }
    }

    public static class Module {
        public String moduleType = "text";
        public String text = "";
        public String recipeType = "crafting_table";
        public List<String> pattern = new ArrayList<>();
        public Map<String, String> key = new LinkedHashMap<>();
        public String result = "";
        public String input = "";
        public String output = "";
        public float experience = 0.0f;
        public int cookingTime = 200;

        public void normalize() {
            if (moduleType == null || moduleType.isBlank()) {
                moduleType = "text";
            }
            if (text == null) {
                text = "";
            }
            if (recipeType == null || recipeType.isBlank()) {
                recipeType = "crafting_table";
            }
            if (pattern == null) {
                pattern = new ArrayList<>();
            }
            if (key == null) {
                key = new LinkedHashMap<>();
            }
            if (result == null) {
                result = "";
            }
            if (input == null) {
                input = "";
            }
            if (output == null) {
                output = "";
            }
            if (cookingTime < 0) {
                cookingTime = 0;
            }
        }
    }
}
