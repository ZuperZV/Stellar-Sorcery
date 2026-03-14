package net.zuperz.stellar_sorcery.animation;

import net.minecraft.util.Mth;

public final class ArmAnimationPose {

    public final PosePart rightArm = new PosePart();
    public final PosePart leftArm = new PosePart();
    public final PosePart body = new PosePart();
    public final PosePart head = new PosePart();

    public void clear() {
        rightArm.clear();
        leftArm.clear();
        body.clear();
        head.clear();
    }

    public void set(ArmAnimationPose other) {
        rightArm.set(other.rightArm);
        leftArm.set(other.leftArm);
        body.set(other.body);
        head.set(other.head);
    }

    public static void interpolate(ArmAnimationPose base, ArmAnimationPose a, ArmAnimationPose b, float t, ArmAnimationPose out) {
        PosePart.interpolate(base.rightArm, a.rightArm, b.rightArm, t, out.rightArm);
        PosePart.interpolate(base.leftArm, a.leftArm, b.leftArm, t, out.leftArm);
        PosePart.interpolate(base.body, a.body, b.body, t, out.body);
        PosePart.interpolate(base.head, a.head, b.head, t, out.head);
    }

    public static final class PosePart {
        public float xRot = Float.NaN;
        public float yRot = Float.NaN;
        public float zRot = Float.NaN;
        public float xOffset = Float.NaN;
        public float yOffset = Float.NaN;
        public float zOffset = Float.NaN;

        public void clear() {
            xRot = Float.NaN;
            yRot = Float.NaN;
            zRot = Float.NaN;
            xOffset = Float.NaN;
            yOffset = Float.NaN;
            zOffset = Float.NaN;
        }

        public void set(PosePart other) {
            xRot = other.xRot;
            yRot = other.yRot;
            zRot = other.zRot;
            xOffset = other.xOffset;
            yOffset = other.yOffset;
            zOffset = other.zOffset;
        }

        public boolean isEmpty() {
            return Float.isNaN(xRot) && Float.isNaN(yRot) && Float.isNaN(zRot)
                    && Float.isNaN(xOffset) && Float.isNaN(yOffset) && Float.isNaN(zOffset);
        }

        public void mirrorFrom(PosePart src) {
            if (!Float.isNaN(src.xRot)) this.xRot = src.xRot;
            if (!Float.isNaN(src.yRot)) this.yRot = -src.yRot;
            if (!Float.isNaN(src.zRot)) this.zRot = -src.zRot;

            if (!Float.isNaN(src.xOffset)) this.xOffset = -src.xOffset;
            if (!Float.isNaN(src.yOffset)) this.yOffset = src.yOffset;
            if (!Float.isNaN(src.zOffset)) this.zOffset = src.zOffset;
        }

        private static float resolve(float base, float a, float b, float t) {
            boolean hasA = !Float.isNaN(a);
            boolean hasB = !Float.isNaN(b);
            if (hasA && hasB) {
                return Mth.lerp(t, a, b);
            }
            if (hasA) return a;
            if (hasB) return b;
            return Float.isNaN(base) ? Float.NaN : base;
        }

        public static void interpolate(PosePart base, PosePart a, PosePart b, float t, PosePart out) {
            out.xRot = resolve(base.xRot, a.xRot, b.xRot, t);
            out.yRot = resolve(base.yRot, a.yRot, b.yRot, t);
            out.zRot = resolve(base.zRot, a.zRot, b.zRot, t);
            out.xOffset = resolve(base.xOffset, a.xOffset, b.xOffset, t);
            out.yOffset = resolve(base.yOffset, a.yOffset, b.yOffset, t);
            out.zOffset = resolve(base.zOffset, a.zOffset, b.zOffset, t);
        }
    }
}
