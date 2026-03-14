package net.zuperz.stellar_sorcery.animation;

import net.minecraft.resources.ResourceLocation;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public final class ArmAnimationRegistry {

    private static final Map<ResourceLocation, ArmAnimation> ANIMATIONS = new HashMap<>();

    private ArmAnimationRegistry() {}

    public static void clear() {
        ANIMATIONS.clear();
    }

    public static void register(ArmAnimation animation) {
        ANIMATIONS.put(animation.id(), animation);
    }

    public static ArmAnimation get(ResourceLocation id) {
        return ANIMATIONS.get(id);
    }

    public static Collection<ArmAnimation> getAll() {
        return Collections.unmodifiableCollection(ANIMATIONS.values());
    }
}
