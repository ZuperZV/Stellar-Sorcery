package net.zuperz.stellar_sorcery.mixin;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.client.player.AbstractClientPlayer;
import net.zuperz.stellar_sorcery.animation.ArmAnimation;
import net.zuperz.stellar_sorcery.animation.ArmAnimationPose;
import net.zuperz.stellar_sorcery.animation.ArmAnimationRegistry;
import net.zuperz.stellar_sorcery.client.animation.ArmAnimationController;
import net.zuperz.stellar_sorcery.client.animation.ArmAnimationInstance;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class PlayerModelAnimationMixin {

    @Inject(method = "setupAnim", at = @At("RETURN"))
    private void stellarSorcery$anim(
            LivingEntity entity,
            float limbSwing,
            float limbSwingAmount,
            float ageInTicks,
            float netHeadYaw,
            float headPitch,
            CallbackInfo ci
    ) {

        if (!(entity instanceof AbstractClientPlayer player)) return;

        ArmAnimationInstance inst = ArmAnimationController.get(player.getId());
        if (inst == null) return;

        ArmAnimation anim = ArmAnimationRegistry.get(inst.animationId());
        if (anim == null) return;

        float time = inst.sampleTime(player.level().getGameTime(), 0);

        ArmAnimationPose pose =
                anim.samplePose(ArmAnimation.View.THIRD_PERSON, time, inst.poseBuffer());

        PlayerModel<?> model = (PlayerModel<?>) (Object) this;

        if (!pose.rightArm.isEmpty()) {
            model.rightArm.xRot = pose.rightArm.xRot;
            model.rightArm.yRot = pose.rightArm.yRot;
            model.rightArm.zRot = pose.rightArm.zRot;
        }

        if (!pose.leftArm.isEmpty()) {
            model.leftArm.xRot = pose.leftArm.xRot;
            model.leftArm.yRot = pose.leftArm.yRot;
            model.leftArm.zRot = pose.leftArm.zRot;
        }
    }
}