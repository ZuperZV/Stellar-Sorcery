package net.zuperz.stellar_sorcery.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.zuperz.stellar_sorcery.animation.ArmAnimation;
import net.zuperz.stellar_sorcery.animation.ArmAnimationPose;
import net.zuperz.stellar_sorcery.animation.ArmAnimationRegistry;
import net.zuperz.stellar_sorcery.client.animation.ArmAnimationApplier;
import net.zuperz.stellar_sorcery.client.animation.ArmAnimationController;
import net.zuperz.stellar_sorcery.client.animation.ArmAnimationInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererArmAnimationMixin {

    @Shadow
    public abstract ResourceLocation getTextureLocation(AbstractClientPlayer p_117783_);

    @Unique public ModelPart ss$fakeSleeve;

    @Unique private boolean ss$hasSaved;
    @Unique private HumanoidArm ss$currentArm;
    @Unique private ModelPart ss$arm;
    @Unique private ModelPart ss$sleeve;
    @Unique private float ss$armXRot;
    @Unique private float ss$armYRot;
    @Unique private float ss$armZRot;
    @Unique private float ss$armX;
    @Unique private float ss$armY;
    @Unique private float ss$armZ;
    @Unique private float ss$sleeveXRot;
    @Unique private float ss$sleeveYRot;
    @Unique private float ss$sleeveZRot;
    @Unique private float ss$sleeveX;
    @Unique private float ss$sleeveY;
    @Unique private float ss$sleeveZ;

    @Inject(method = "renderRightHand", at = @At("HEAD"))
    private void markRightHand(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, CallbackInfo ci) {
        ss$currentArm = HumanoidArm.RIGHT;
    }

    @Inject(method = "renderLeftHand", at = @At("HEAD"))
    private void markLeftHand(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, CallbackInfo ci) {
        ss$currentArm = HumanoidArm.LEFT;
    }

    @Inject(
            method = "renderHand",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/model/geom/ModelPart;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;II)V",
                    ordinal = 0
            )
    )
    private void applyFirstPersonArmAnimation(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer player,
            ModelPart arm,
            ModelPart sleeve,
            CallbackInfo ci
    ) {
        ArmAnimationInstance inst = ArmAnimationController.get(player.getId());
        //System.out.println("Test 1 Checking animation for " + player.getId() + "  " + inst);
        if (inst == null) return;

        ArmAnimation anim = ArmAnimationRegistry.get(inst.animationId());
        //System.out.println("Test 2 Checking animation = " + anim);
        if (anim == null) return;

        if (ss$currentArm == null) return;
        boolean right = ss$currentArm == HumanoidArm.RIGHT;
        boolean apply = switch (anim.hands()) {
            case BOTH -> true;
            case MAIN_HAND -> (player.getMainArm() == HumanoidArm.RIGHT) == right;
            case OFF_HAND -> (player.getMainArm() == HumanoidArm.LEFT) == right;
        };
        //System.out.println("Test 3 ss$currentArm = " + ss$currentArm);

        if (!apply) return;
        //System.out.println("Test 4 !apply = " + !apply);


        float partial = Minecraft.getInstance().getTimer().getGameTimeDeltaPartialTick(false);
        float time = inst.sampleTime(player.level().getGameTime(), partial);
        ArmAnimationPose pose = anim.samplePose(ArmAnimation.View.FIRST_PERSON, time, inst.poseBuffer());

        ArmAnimationPose.PosePart part = ArmAnimationApplier.resolveArmPose(pose, right, anim.mirror());
        if (part == null || part.isEmpty()) return;

        ss$hasSaved = true;
        ss$arm = arm;
        ss$sleeve = sleeve;
        ss$armXRot = arm.xRot;
        ss$armYRot = arm.yRot;
        ss$armZRot = arm.zRot;
        ss$armX = arm.x;
        ss$armY = arm.y;
        ss$armZ = arm.z;
        ss$sleeveXRot = sleeve.xRot;
        ss$sleeveYRot = sleeve.yRot;
        ss$sleeveZRot = sleeve.zRot;
        ss$sleeveX = sleeve.x;
        ss$sleeveY = sleeve.y;
        ss$sleeveZ = sleeve.z;

        ArmAnimationApplier.applyToArm(arm, sleeve, part);
    }

    @Inject(method = "renderHand", at = @At("TAIL"))
    private void restoreFirstPersonArmAnimation(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            AbstractClientPlayer player,
            ModelPart arm,
            ModelPart sleeve,
            CallbackInfo ci
    ) {
        if (!ss$hasSaved) return;
        if (arm != ss$arm || sleeve != ss$sleeve) return;

        arm.xRot = ss$armXRot;
        arm.yRot = ss$armYRot;
        arm.zRot = ss$armZRot;
        arm.x = ss$armX;
        arm.y = ss$armY;
        arm.z = ss$armZ;

        sleeve.xRot = ss$sleeveXRot;
        sleeve.yRot = ss$sleeveYRot;
        sleeve.zRot = ss$sleeveZRot;
        sleeve.x = ss$sleeveX;
        sleeve.y = ss$sleeveY;
        sleeve.z = ss$sleeveZ;

        ss$hasSaved = false;
        ss$arm = null;
        ss$sleeve = null;
    }

    @Inject(method = "renderRightHand", at = @At("TAIL"))
    private void clearRightHand(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, CallbackInfo ci) {
        ss$currentArm = null;
    }

    @Inject(method = "renderLeftHand", at = @At("TAIL"))
    private void clearLeftHand(PoseStack poseStack, MultiBufferSource buffer, int packedLight, AbstractClientPlayer player, CallbackInfo ci) {
        ss$currentArm = null;
    }

    @Inject(method = "renderHand", at = @At("TAIL"))
    private void renderFakeSleeve(
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int packedLight,
            AbstractClientPlayer player,
            ModelPart arm,
            ModelPart sleeve,
            CallbackInfo ci
    ) {
        if (arm == null) return;

        Map<String, ModelPart> children = ((ModelPartAccessor)(Object)arm).stellar_sorcery$getChildren();
        System.out.println("testing for fakeSleeve i children: " + children);
        ModelPart fakeSleeve = children.get("stellar_sleeve");
        if (fakeSleeve == null) {
            System.out.println("NEJJ");
            return;
        }

        fakeSleeve.visible = true;
        VertexConsumer cutoutBuffer = bufferSource.getBuffer(RenderType.cutout());
        fakeSleeve.render(poseStack, cutoutBuffer, packedLight, OverlayTexture.NO_OVERLAY);
    }
}
