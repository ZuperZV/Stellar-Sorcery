package net.zuperz.stellar_sorcery.client.animation;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.HumanoidArm;
import net.zuperz.stellar_sorcery.animation.ArmAnimation;
import net.zuperz.stellar_sorcery.animation.ArmAnimationPose;

public final class ArmAnimationApplier {

    private ArmAnimationApplier() {}

    public static void applyToModel(
            PlayerModel<?> model,
            ArmAnimationPose pose,
            ArmAnimation.Hands hands,
            boolean mirror,
            HumanoidArm mainArm
    ) {
        if (!pose.body.isEmpty()) {
            applyPosePart(model.body, pose.body, false);
        }
        if (!pose.head.isEmpty()) {
            applyPosePart(model.head, pose.head, false);
            applyPosePart(model.hat, pose.head, false);
        }

        boolean rightIsMain = mainArm == HumanoidArm.RIGHT;
        switch (hands) {
            case BOTH -> {
                applyArm(model, true, pose, mirror);
                applyArm(model, false, pose, mirror);
            }
            case MAIN_HAND -> applyArm(model, rightIsMain, pose, mirror);
            case OFF_HAND -> applyArm(model, !rightIsMain, pose, mirror);
        }
    }

    public static void applyToArm(ModelPart arm, ModelPart sleeve, ArmAnimationPose.PosePart part) {
        if (arm == null || sleeve == null || part == null || part.isEmpty()) return;

        applyPosePart(arm, part, false);
        applyPosePart(sleeve, part, true);
    }

    private static void applyArm(PlayerModel<?> model, boolean right, ArmAnimationPose pose, boolean mirror) {
        ArmAnimationPose.PosePart part = resolveArmPose(pose, right, mirror);
        if (part == null || part.isEmpty()) return;

        if (right) {
            applyToArm(model.rightArm, model.rightSleeve, part);
        } else {
            applyToArm(model.leftArm, model.leftSleeve, part);
        }
    }

    public static ArmAnimationPose.PosePart resolveArmPose(ArmAnimationPose pose, boolean right, boolean mirror) {
        ArmAnimationPose.PosePart direct = right ? pose.rightArm : pose.leftArm;
        if (direct != null && !direct.isEmpty()) return direct;
        if (!mirror) return direct;

        ArmAnimationPose.PosePart other = right ? pose.leftArm : pose.rightArm;
        if (other == null || other.isEmpty()) return direct;

        ArmAnimationPose.PosePart mirrored = new ArmAnimationPose.PosePart();
        mirrored.mirrorFrom(other);
        return mirrored;
    }

    private static void applyPosePart(ModelPart part, ArmAnimationPose.PosePart pose, boolean applyOffset) {
        if (!Float.isNaN(pose.xRot)) part.xRot = pose.xRot;
        if (!Float.isNaN(pose.yRot)) part.yRot = pose.yRot;
        if (!Float.isNaN(pose.zRot)) part.zRot = pose.zRot;

        if (applyOffset) {
            if (!Float.isNaN(pose.xOffset)) part.x += pose.xOffset;
            if (!Float.isNaN(pose.yOffset)) part.y += pose.yOffset;
            if (!Float.isNaN(pose.zOffset)) part.z += pose.zOffset;
        }
    }

    public static Snapshot capture(PlayerModel<?> model) {
        return new Snapshot(model);
    }

    public static final class Snapshot {
        private final float rightArmXRot;
        private final float rightArmYRot;
        private final float rightArmZRot;
        private final float rightArmX;
        private final float rightArmY;
        private final float rightArmZ;

        private final float leftArmXRot;
        private final float leftArmYRot;
        private final float leftArmZRot;
        private final float leftArmX;
        private final float leftArmY;
        private final float leftArmZ;

        private final float rightSleeveXRot;
        private final float rightSleeveYRot;
        private final float rightSleeveZRot;
        private final float rightSleeveX;
        private final float rightSleeveY;
        private final float rightSleeveZ;

        private final float leftSleeveXRot;
        private final float leftSleeveYRot;
        private final float leftSleeveZRot;
        private final float leftSleeveX;
        private final float leftSleeveY;
        private final float leftSleeveZ;

        private final float bodyXRot;
        private final float bodyYRot;
        private final float bodyZRot;
        private final float bodyX;
        private final float bodyY;
        private final float bodyZ;

        private final float headXRot;
        private final float headYRot;
        private final float headZRot;
        private final float headX;
        private final float headY;
        private final float headZ;

        private final float hatXRot;
        private final float hatYRot;
        private final float hatZRot;
        private final float hatX;
        private final float hatY;
        private final float hatZ;

        public Snapshot(PlayerModel<?> model) {
            this.rightArmXRot = model.rightArm.xRot;
            this.rightArmYRot = model.rightArm.yRot;
            this.rightArmZRot = model.rightArm.zRot;
            this.rightArmX = model.rightArm.x;
            this.rightArmY = model.rightArm.y;
            this.rightArmZ = model.rightArm.z;

            this.leftArmXRot = model.leftArm.xRot;
            this.leftArmYRot = model.leftArm.yRot;
            this.leftArmZRot = model.leftArm.zRot;
            this.leftArmX = model.leftArm.x;
            this.leftArmY = model.leftArm.y;
            this.leftArmZ = model.leftArm.z;

            this.rightSleeveXRot = model.rightSleeve.xRot;
            this.rightSleeveYRot = model.rightSleeve.yRot;
            this.rightSleeveZRot = model.rightSleeve.zRot;
            this.rightSleeveX = model.rightSleeve.x;
            this.rightSleeveY = model.rightSleeve.y;
            this.rightSleeveZ = model.rightSleeve.z;

            this.leftSleeveXRot = model.leftSleeve.xRot;
            this.leftSleeveYRot = model.leftSleeve.yRot;
            this.leftSleeveZRot = model.leftSleeve.zRot;
            this.leftSleeveX = model.leftSleeve.x;
            this.leftSleeveY = model.leftSleeve.y;
            this.leftSleeveZ = model.leftSleeve.z;

            this.bodyXRot = model.body.xRot;
            this.bodyYRot = model.body.yRot;
            this.bodyZRot = model.body.zRot;
            this.bodyX = model.body.x;
            this.bodyY = model.body.y;
            this.bodyZ = model.body.z;

            this.headXRot = model.head.xRot;
            this.headYRot = model.head.yRot;
            this.headZRot = model.head.zRot;
            this.headX = model.head.x;
            this.headY = model.head.y;
            this.headZ = model.head.z;

            this.hatXRot = model.hat.xRot;
            this.hatYRot = model.hat.yRot;
            this.hatZRot = model.hat.zRot;
            this.hatX = model.hat.x;
            this.hatY = model.hat.y;
            this.hatZ = model.hat.z;
        }

        public void restore(PlayerModel<?> model) {
            model.rightArm.xRot = rightArmXRot;
            model.rightArm.yRot = rightArmYRot;
            model.rightArm.zRot = rightArmZRot;
            model.rightArm.x = rightArmX;
            model.rightArm.y = rightArmY;
            model.rightArm.z = rightArmZ;

            model.leftArm.xRot = leftArmXRot;
            model.leftArm.yRot = leftArmYRot;
            model.leftArm.zRot = leftArmZRot;
            model.leftArm.x = leftArmX;
            model.leftArm.y = leftArmY;
            model.leftArm.z = leftArmZ;

            model.rightSleeve.xRot = rightSleeveXRot;
            model.rightSleeve.yRot = rightSleeveYRot;
            model.rightSleeve.zRot = rightSleeveZRot;
            model.rightSleeve.x = rightSleeveX;
            model.rightSleeve.y = rightSleeveY;
            model.rightSleeve.z = rightSleeveZ;

            model.leftSleeve.xRot = leftSleeveXRot;
            model.leftSleeve.yRot = leftSleeveYRot;
            model.leftSleeve.zRot = leftSleeveZRot;
            model.leftSleeve.x = leftSleeveX;
            model.leftSleeve.y = leftSleeveY;
            model.leftSleeve.z = leftSleeveZ;

            model.body.xRot = bodyXRot;
            model.body.yRot = bodyYRot;
            model.body.zRot = bodyZRot;
            model.body.x = bodyX;
            model.body.y = bodyY;
            model.body.z = bodyZ;

            model.head.xRot = headXRot;
            model.head.yRot = headYRot;
            model.head.zRot = headZRot;
            model.head.x = headX;
            model.head.y = headY;
            model.head.z = headZ;

            model.hat.xRot = hatXRot;
            model.hat.yRot = hatYRot;
            model.hat.zRot = hatZRot;
            model.hat.x = hatX;
            model.hat.y = hatY;
            model.hat.z = hatZ;
        }
    }
}
