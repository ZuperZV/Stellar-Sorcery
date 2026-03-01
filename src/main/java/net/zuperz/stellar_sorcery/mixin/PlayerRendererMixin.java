package net.zuperz.stellar_sorcery.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.zuperz.stellar_sorcery.StellarSorcery;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

    private static final ResourceLocation GAZE_TEXTURE_LEFT =
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/model/player/gaze/grow_gaze_left.png");
    private static final ResourceLocation GAZE_TEXTURE_RIGHT =
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/model/player/gaze/grow_gaze_right.png");

    @Inject(method = "<init>", at = @At("RETURN"))
    private void addGazeLayer(net.minecraft.client.renderer.entity.EntityRendererProvider.Context context,
                              boolean slim,
                              CallbackInfo ci) {

        PlayerRenderer renderer = (PlayerRenderer) (Object) this;

        renderer.addLayer(new RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>(
                (RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>>) renderer
        ) {

            @Override
            public void render(PoseStack poseStack,
                               MultiBufferSource buffer,
                               int packedLight,
                               AbstractClientPlayer player,
                               float limbSwing,
                               float limbSwingAmount,
                               float partialTicks,
                               float ageInTicks,
                               float netHeadYaw,
                               float headPitch) {

                if (player.isInvisible()) return;

                float scale = 1.01f;

                poseStack.pushPose();

                poseStack.scale(scale, scale, scale);

                poseStack.translate(0.0, (1.0 - scale) * 0.5, 0.0);

                VertexConsumer vertexConsumer =
                        buffer.getBuffer(RenderType.entityTranslucent(GAZE_TEXTURE_LEFT));

                this.getParentModel().renderToBuffer(
                        poseStack,
                        vertexConsumer,
                        packedLight,
                        OverlayTexture.NO_OVERLAY
                );

                VertexConsumer vertexConsumer3 =
                        buffer.getBuffer(RenderType.entityTranslucent(GAZE_TEXTURE_RIGHT));

                this.getParentModel().renderToBuffer(
                        poseStack,
                        vertexConsumer3,
                        packedLight,
                        OverlayTexture.NO_OVERLAY
                );

                poseStack.popPose();
            }
        });
    }
}