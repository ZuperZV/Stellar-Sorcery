package net.zuperz.stellar_sorcery.animation;

import java.util.Collections;
import java.util.List;

public final class ArmAnimationView {
    private final ArmAnimationPose basePose;
    private final List<ArmAnimationKeyframe> keyframes;

    public ArmAnimationView(ArmAnimationPose basePose, List<ArmAnimationKeyframe> keyframes) {
        this.basePose = basePose;
        this.keyframes = keyframes != null ? keyframes : Collections.emptyList();
    }

    public ArmAnimationPose basePose() {
        return basePose;
    }

    public List<ArmAnimationKeyframe> keyframes() {
        return keyframes;
    }

    public static ArmAnimationView empty() {
        return new ArmAnimationView(new ArmAnimationPose(), Collections.emptyList());
    }
}
