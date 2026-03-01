package net.zuperz.stellar_sorcery.client.rendering;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.zuperz.stellar_sorcery.StellarSorcery;
import org.jetbrains.annotations.NotNull;

public class GazeLayer<T extends Player, M extends PlayerModel<T>> extends RenderLayer<T, M> {

    private static final ResourceLocation GAZE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/models/player/gaze/grow_gaze_left.png");

    public GazeLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(@NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer,
                       int packedLight,
                       @NotNull T player,
                       float limbSwing,
                       float limbSwingAmount,
                       float partialTicks,
                       float ageInTicks,
                       float netHeadYaw,
                       float headPitch) {

        float scale = 0.95f;
        poseStack.pushPose();
        poseStack.scale(scale, scale, scale);
        poseStack.translate(0.0, (1.0 - scale) * 0.5, 0.0);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(GAZE_TEXTURE));
        this.getParentModel().renderToBuffer(
                poseStack,
                vertexConsumer,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }
}