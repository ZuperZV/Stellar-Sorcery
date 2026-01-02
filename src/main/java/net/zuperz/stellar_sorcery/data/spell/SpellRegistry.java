package net.zuperz.stellar_sorcery.data.spell;

import java.util.HashMap;
import java.util.Map;

public class SpellRegistry {

    public static final Map<String, AreaFile> AREAS = new HashMap<>();
    public static final Map<String, RuneFile> RUNES = new HashMap<>();
    public static final Map<String, ModifierFile> MODIFIERS = new HashMap<>();

    public static void clear() {
        AREAS.clear();
        RUNES.clear();
        MODIFIERS.clear();
    }

    public static AreaFile getArea(String id) {
        return AREAS.get(id);
    }

    public static RuneFile getRune(String id) {
        return RUNES.get(id);
    }

    public static ModifierFile getModifier(String id) {
        return MODIFIERS.get(id);
    }

    public static AreaFile getRandomArea() {
        if (AREAS.isEmpty()) return null;
        return AREAS.values().stream().skip((int)(Math.random() * AREAS.size())).findFirst().orElse(null);
    }

    public static RuneFile getRandomRune() {
        if (RUNES.isEmpty()) return null;
        return RUNES.values().stream().skip((int)(Math.random() * RUNES.size())).findFirst().orElse(null);
    }

    public static ModifierFile getRandomModifier() {
        if (MODIFIERS.isEmpty()) return null;
        return MODIFIERS.values().stream().skip((int)(Math.random() * MODIFIERS.size())).findFirst().orElse(null);
    }
}
