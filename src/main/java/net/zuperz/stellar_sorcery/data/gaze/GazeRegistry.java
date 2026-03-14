package net.zuperz.stellar_sorcery.data.gaze;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public final class GazeRegistry {

    private static final Map<ResourceLocation, GazeDefinition> GAZES = new HashMap<>();
    private static final Map<ResourceLocation, GazeModifierDefinition> MODIFIERS = new HashMap<>();

    private GazeRegistry() {}

    public static void clear() {
        GAZES.clear();
        MODIFIERS.clear();
    }

    public static void registerGaze(GazeDefinition def) {
        GAZES.put(def.id(), def);
    }

    public static void registerModifier(GazeModifierDefinition def) {
        MODIFIERS.put(def.id(), def);
    }

    public static GazeDefinition getGaze(ResourceLocation id) {
        return GAZES.get(id);
    }

    public static GazeDefinition getByItemId(ResourceLocation itemId) {
        return GAZES.get(itemId);
    }

    public static GazeModifierDefinition getModifier(ResourceLocation id) {
        return MODIFIERS.get(id);
    }

    public static Collection<GazeDefinition> getAllGazes() {
        return GAZES.values();
    }
}
