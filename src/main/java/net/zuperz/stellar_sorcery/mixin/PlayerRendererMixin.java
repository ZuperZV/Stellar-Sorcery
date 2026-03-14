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
import net.minecraft.world.item.ItemStack;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.item.custom.GazeItem;
import net.zuperz.stellar_sorcery.screen.Helpers.IExtraSlotsProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {

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
                if (!(player instanceof IExtraSlotsProvider provider)) return;

                var extraInventory = provider.getExtraSlots();

                float scale = 1.01f;
                poseStack.pushPose();
                poseStack.scale(scale, scale, scale);
                poseStack.translate(0.0, (1.0 - scale) * 0.5, 0.0);

                for (int i = 0; i < extraInventory.getContainerSize(); i++) {
                    ItemStack stack = extraInventory.getItem(i);
                    if (stack.isEmpty() || !(stack.getItem() instanceof GazeItem gaze)) continue;

                    // slot0 = right, slot1 = left
                    boolean isRight = i == 0;
                    String armSuffix = isRight ? "_right.png" : "_left.png";

                    ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                            gaze.getGazeTexture().getNamespace(),
                            gaze.getGazeTexture().getPath() + armSuffix
                    );

                    VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(texture));
                    this.getParentModel().renderToBuffer(poseStack, vc, packedLight, OverlayTexture.NO_OVERLAY);
                }

                poseStack.popPose();
            }
        });
    }
}