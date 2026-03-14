package net.zuperz.stellar_sorcery.client.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.zuperz.stellar_sorcery.animation.ArmAnimation;
import net.zuperz.stellar_sorcery.animation.ArmAnimationRegistry;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public final class ArmAnimationController {

    private static final Map<Integer, ArmAnimationInstance> ACTIVE = new HashMap<>();

    private ArmAnimationController() {}

    public static void play(int playerId, ResourceLocation animationId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return;

        ArmAnimation anim = ArmAnimationRegistry.get(animationId);
        if (anim == null) return;

        long startTick = mc.level.getGameTime();
        ACTIVE.put(playerId, new ArmAnimationInstance(animationId, startTick, anim.mode(), anim.duration()));
    }

    public static void stop(int playerId) {
        ACTIVE.remove(playerId);
    }

    public static ArmAnimationInstance get(int playerId) {
        return ACTIVE.get(playerId);
    }

    public static void onClientTick(Minecraft mc) {
        if (mc.level == null) return;
        long gameTime = mc.level.getGameTime();

        Iterator<Map.Entry<Integer, ArmAnimationInstance>> it = ACTIVE.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<Integer, ArmAnimationInstance> entry = it.next();
            if (entry.getValue().shouldExpire(gameTime)) {
                it.remove();
            }
        }
    }
}
