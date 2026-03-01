package net.zuperz.stellar_sorcery.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import net.zuperz.stellar_sorcery.StellarSorcery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

    private static final ResourceLocation GAZE_TEXTURE_LEFT =
            ResourceLocation.fromNamespaceAndPath(
                    StellarSorcery.MOD_ID,
                    "textures/model/player/gaze/grow_gaze_left.png"
            );

    private static final ResourceLocation GAZE_TEXTURE_RIGHT =
            ResourceLocation.fromNamespaceAndPath(
                    StellarSorcery.MOD_ID,
                    "textures/model/player/gaze/grow_gaze_right.png"
            );


    @Inject(method = "renderPlayerArm", at = @At("TAIL"))
    private void renderGazeFirstPerson(
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            float equippedProgress,
            float swingProgress,
            HumanoidArm arm,
            CallbackInfo ci
    ) {

        AbstractClientPlayer player = Minecraft.getInstance().player;
        if (player == null || player.isInvisible()) return;

        poseStack.pushPose();

        float scale = 1.01f;
        poseStack.scale(scale, scale, scale);

        PlayerRenderer renderer = (PlayerRenderer)
                Minecraft.getInstance()
                        .getEntityRenderDispatcher()
                        .getRenderer(player);

        ResourceLocation texture =
                arm == HumanoidArm.LEFT ? GAZE_TEXTURE_LEFT : GAZE_TEXTURE_RIGHT;

        VertexConsumer vertexConsumer =
                buffer.getBuffer(RenderType.entityTranslucent(texture));

        renderer.getModel().renderToBuffer(
                poseStack,
                vertexConsumer,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }
}