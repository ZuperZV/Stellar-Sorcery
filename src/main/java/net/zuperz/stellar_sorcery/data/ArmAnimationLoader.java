package net.zuperz.stellar_sorcery.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.zuperz.stellar_sorcery.animation.ArmAnimation;
import net.zuperz.stellar_sorcery.animation.ArmAnimationRegistry;

import java.util.Map;

public class ArmAnimationLoader extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = new Gson();

    public ArmAnimationLoader() {
        super(GSON, "arm_animations");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager resourceManager, ProfilerFiller profiler) {
        ArmAnimationRegistry.clear();

        for (Map.Entry<ResourceLocation, JsonElement> entry : jsonMap.entrySet()) {
            ResourceLocation id = entry.getKey();
            if (id.getPath().endsWith("_schema")) {
                continue;
            }

            try {
                JsonObject obj = entry.getValue().getAsJsonObject();
                ArmAnimation animation = ArmAnimation.fromJson(id, obj);
                ArmAnimationRegistry.register(animation);
            } catch (Exception e) {
                System.err.println("[ArmAnimationLoader] Error loading animation " + id + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        System.out.println("[ArmAnimationLoader] Loaded animations: " + ArmAnimationRegistry.getAll().size());
    }
}
