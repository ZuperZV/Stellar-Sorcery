package net.zuperz.stellar_sorcery.animation;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class ArmAnimation {

    public enum Hands {
        MAIN_HAND,
        OFF_HAND,
        BOTH;

        public static Hands fromString(String value) {
            if (value == null) return BOTH;
            return switch (value.toLowerCase()) {
                case "main_hand", "mainhand", "main" -> MAIN_HAND;
                case "off_hand", "offhand", "off" -> OFF_HAND;
                default -> BOTH;
            };
        }
    }

    public enum Mode {
        ONESHOT,
        LOOP,
        CHANNEL,
        HOLD_POSE;

        public static Mode fromString(String value) {
            if (value == null) return ONESHOT;
            return switch (value.toLowerCase()) {
                case "loop" -> LOOP;
                case "channel" -> CHANNEL;
                case "hold_pose", "hold" -> HOLD_POSE;
                default -> ONESHOT;
            };
        }
    }

    public enum View {
        FIRST_PERSON,
        THIRD_PERSON
    }

    private final ResourceLocation id;
    private final Hands hands;
    private final Mode mode;
    private final int duration;
    private final boolean mirror;
    private final ArmAnimationView firstPerson;
    private final ArmAnimationView thirdPerson;
    private final List<ArmAnimationKeyframe> keyframes;

    public ArmAnimation(
            ResourceLocation id,
            Hands hands,
            Mode mode,
            int duration,
            boolean mirror,
            ArmAnimationView firstPerson,
            ArmAnimationView thirdPerson,
            List<ArmAnimationKeyframe> keyframes
    ) {
        this.id = id;
        this.hands = hands;
        this.mode = mode;
        this.duration = duration;
        this.mirror = mirror;
        this.firstPerson = firstPerson;
        this.thirdPerson = thirdPerson;
        this.keyframes = keyframes;
    }

    public ResourceLocation id() { return id; }
    public Hands hands() { return hands; }
    public Mode mode() { return mode; }
    public int duration() { return duration; }
    public boolean mirror() { return mirror; }

    public ArmAnimationPose samplePose(View view, float time, ArmAnimationPose out) {
        ArmAnimationView v = view == View.FIRST_PERSON ? firstPerson : thirdPerson;
        List<ArmAnimationKeyframe> frames = !v.keyframes().isEmpty() ? v.keyframes() : keyframes;
        ArmAnimationPose base = v.basePose();

        if (frames.isEmpty()) {
            out.set(base);
            return out;
        }

        if (frames.size() == 1) {
            ArmAnimationPose.interpolate(base, frames.get(0).pose(), frames.get(0).pose(), 0.0F, out);
            return out;
        }

        ArmAnimationKeyframe first = frames.get(0);
        ArmAnimationKeyframe last = frames.get(frames.size() - 1);

        if (time <= first.tick()) {
            ArmAnimationPose.interpolate(base, first.pose(), first.pose(), 0.0F, out);
            return out;
        }

        if (time >= last.tick()) {
            ArmAnimationPose.interpolate(base, last.pose(), last.pose(), 0.0F, out);
            return out;
        }

        ArmAnimationKeyframe prev = first;
        ArmAnimationKeyframe next = last;

        for (int i = 1; i < frames.size(); i++) {
            ArmAnimationKeyframe kf = frames.get(i);
            if (time <= kf.tick()) {
                next = kf;
                break;
            }
            prev = kf;
        }

        float denom = Math.max(1.0F, next.tick() - prev.tick());
        float alpha = (time - prev.tick()) / denom;
        ArmAnimationPose.interpolate(base, prev.pose(), next.pose(), alpha, out);
        return out;
    }

    public static ArmAnimation fromJson(ResourceLocation id, JsonObject json) {
        Hands hands = Hands.fromString(getString(json, "hands", "both"));
        Mode mode = Mode.fromString(getString(json, "mode", "oneshot"));
        int duration = getInt(json, "duration", 0);
        boolean mirror = getBoolean(json, "mirror", false);

        List<ArmAnimationKeyframe> rootFrames = parseKeyframes(json.getAsJsonArray("keyframes"));
        ArmAnimationView first = parseViewFlexible(json.get("first_person"));
        ArmAnimationView third = parseViewFlexible(json.get("third_person"));

        int maxTick = Math.max(
                maxTick(rootFrames),
                Math.max(maxTick(first.keyframes()), maxTick(third.keyframes()))
        );

        if (duration <= 0) {
            duration = Math.max(1, maxTick);
        }

        return new ArmAnimation(
                id,
                hands,
                mode,
                duration,
                mirror,
                first,
                third,
                rootFrames
        );
    }

    private static ArmAnimationView parseView(JsonObject obj) {
        if (obj == null) {
            return ArmAnimationView.empty();
        }

        ArmAnimationPose base = parsePose(obj);
        List<ArmAnimationKeyframe> frames = parseKeyframes(obj.getAsJsonArray("keyframes"));
        return new ArmAnimationView(base, frames);
    }

    private static ArmAnimationPose parsePose(JsonObject obj) {
        ArmAnimationPose pose = new ArmAnimationPose();
        if (obj == null) return pose;

        applyPart(obj.getAsJsonObject("right_arm"), pose.rightArm);
        applyPart(obj.getAsJsonObject("left_arm"), pose.leftArm);
        applyPart(obj.getAsJsonObject("body"), pose.body);
        applyPart(obj.getAsJsonObject("head"), pose.head);
        return pose;
    }

    private static void applyPart(JsonObject obj, ArmAnimationPose.PosePart part) {
        if (obj == null) return;

        if (obj.has("x_rot")) part.xRot = obj.get("x_rot").getAsFloat() * Mth.DEG_TO_RAD;
        if (obj.has("y_rot")) part.yRot = obj.get("y_rot").getAsFloat() * Mth.DEG_TO_RAD;
        if (obj.has("z_rot")) part.zRot = obj.get("z_rot").getAsFloat() * Mth.DEG_TO_RAD;

        if (obj.has("x_offset")) part.xOffset = obj.get("x_offset").getAsFloat();
        if (obj.has("y_offset")) part.yOffset = obj.get("y_offset").getAsFloat();
        if (obj.has("z_offset")) part.zOffset = obj.get("z_offset").getAsFloat();
    }

    private static ArmAnimationView parseViewFlexible(JsonElement element) {
        if (element == null || element.isJsonNull()) {
            return ArmAnimationView.empty();
        }

        if (element.isJsonObject()) {
            return parseView(element.getAsJsonObject());
        }

        if (element.isJsonArray()) {
            List<ArmAnimationKeyframe> frames = parseKeyframes(element.getAsJsonArray());
            return new ArmAnimationView(new ArmAnimationPose(), frames);
        }

        return ArmAnimationView.empty();
    }

    private static List<ArmAnimationKeyframe> parseKeyframes(JsonArray array) {
        if (array == null || array.isEmpty()) return List.of();

        List<ArmAnimationKeyframe> frames = new ArrayList<>();
        for (JsonElement el : array) {
            if (!el.isJsonObject()) continue;
            JsonObject obj = el.getAsJsonObject();
            int tick = getInt(obj, "tick", 0);
            ArmAnimationPose pose = parsePose(obj);
            frames.add(new ArmAnimationKeyframe(tick, pose));
        }

        frames.sort(Comparator.comparingInt(ArmAnimationKeyframe::tick));
        return frames;
    }

    private static int maxTick(List<ArmAnimationKeyframe> frames) {
        if (frames == null || frames.isEmpty()) return 0;
        return frames.get(frames.size() - 1).tick();
    }

    private static String getString(JsonObject obj, String key, String def) {
        return obj != null && obj.has(key) ? obj.get(key).getAsString() : def;
    }

    private static int getInt(JsonObject obj, String key, int def) {
        return obj != null && obj.has(key) ? obj.get(key).getAsInt() : def;
    }

    private static boolean getBoolean(JsonObject obj, String key, boolean def) {
        return obj != null && obj.has(key) ? obj.get(key).getAsBoolean() : def;
    }
}
