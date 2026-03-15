package net.zuperz.stellar_sorcery.client.animation;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.animation.ArmAnimation;
import net.zuperz.stellar_sorcery.animation.ArmAnimationPose;
import net.zuperz.stellar_sorcery.animation.ArmAnimationRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(modid = StellarSorcery.MOD_ID, value = Dist.CLIENT)
public class ArmAnimationRenderHandler {

    private static final Map<Integer, ArmAnimationApplier.Snapshot> SNAPSHOTS = new HashMap<>();

    @SubscribeEvent
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) return;

        ArmAnimationInstance inst = ArmAnimationController.get(player.getId());
        if (inst == null) return;

        ArmAnimation anim = ArmAnimationRegistry.get(inst.animationId());
        if (anim == null) return;

        PlayerModel<?> model = event.getRenderer().getModel();
        SNAPSHOTS.put(player.getId(), ArmAnimationApplier.capture(model));

        float time = inst.sampleTime(player.level().getGameTime(), event.getPartialTick());
        ArmAnimationPose pose = anim.samplePose(ArmAnimation.View.THIRD_PERSON, time, inst.poseBuffer());

        ArmAnimationApplier.applyToModel(model, pose, anim.hands(), anim.mirror(), player.getMainArm());
    }

    @SubscribeEvent
    public static void onRenderPlayerPost(RenderPlayerEvent.Post event) {
        if (!(event.getEntity() instanceof AbstractClientPlayer player)) return;
        ArmAnimationApplier.Snapshot snapshot = SNAPSHOTS.remove(Optional.of(player.getId()));
        if (snapshot == null) return;

        PlayerModel<?> model = event.getRenderer().getModel();
        snapshot.restore(model);
    }
}
