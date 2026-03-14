package net.zuperz.stellar_sorcery.client.animation;

import net.minecraft.resources.ResourceLocation;
import net.zuperz.stellar_sorcery.animation.ArmAnimation;
import net.zuperz.stellar_sorcery.animation.ArmAnimationPose;

public final class ArmAnimationInstance {
    private final ResourceLocation animationId;
    private final long startTick;
    private final ArmAnimation.Mode mode;
    private final int duration;
    private final ArmAnimationPose poseBuffer = new ArmAnimationPose();

    public ArmAnimationInstance(ResourceLocation animationId, long startTick, ArmAnimation.Mode mode, int duration) {
        this.animationId = animationId;
        this.startTick = startTick;
        this.mode = mode;
        this.duration = Math.max(1, duration);
    }

    public ResourceLocation animationId() {
        return animationId;
    }

    public ArmAnimationPose poseBuffer() {
        return poseBuffer;
    }

    public float sampleTime(long gameTime, float partialTick) {
        float elapsed = (gameTime - startTick) + partialTick;
        if (elapsed < 0.0F) elapsed = 0.0F;

        return switch (mode) {
            case LOOP, CHANNEL -> elapsed % duration;
            case HOLD_POSE, ONESHOT -> Math.min(elapsed, duration);
        };
    }

    public boolean shouldExpire(long gameTime) {
        if (mode != ArmAnimation.Mode.ONESHOT) return false;
        return (gameTime - startTick) >= duration;
    }
}
