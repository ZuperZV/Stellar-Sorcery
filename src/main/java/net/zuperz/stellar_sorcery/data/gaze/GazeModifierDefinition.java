package net.zuperz.stellar_sorcery.data.gaze;

import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;

public record GazeModifierDefinition(ResourceLocation id, String effect, double amount) {

    public static GazeModifierDefinition fromJson(ResourceLocation id, JsonObject obj) {
        String effect = GazeJsonHelper.getString(obj, "effect", "none");
        double amount = GazeJsonHelper.getDouble(obj, "amount", 0.0);
        return new GazeModifierDefinition(id, effect, amount);
    }
}
