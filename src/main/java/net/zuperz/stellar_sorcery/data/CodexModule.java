package net.zuperz.stellar_sorcery.data;

import java.util.List;
import java.util.Map;

public class CodexModule {
    public String module_type; // fx "text", "recipe", "furnace_recipe"

    public String text;

    // Crafting recipe
    public String recipe_type; // fx "crafting_table"
    public List<String> pattern;
    public Map<String, String> key;
    public String result;

    // Furnace recipe
    public String input;
    public String output;
    public float experience;
    public int cooking_time;
}
