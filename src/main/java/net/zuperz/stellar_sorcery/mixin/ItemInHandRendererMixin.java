package net.zuperz.stellar_sorcery.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.item.custom.GazeItem;
import net.zuperz.stellar_sorcery.screen.Helpers.IExtraSlotsProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {

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
        if (!(player instanceof IExtraSlotsProvider provider)) return;

        Container extraInventory = provider.getExtraSlots();

        // slot0 = right, slot1 = left
        ItemStack gazeStack = arm == HumanoidArm.RIGHT
                ? extraInventory.getItem(IExtraSlotsProvider.GAZE_SLOT_RIGHT)
                : extraInventory.getItem(IExtraSlotsProvider.GAZE_SLOT_LEFT);
        if (gazeStack.isEmpty() || !(gazeStack.getItem() instanceof GazeItem gaze)) return;

        ResourceLocation baseTexture = gaze.getGazeTexture();
        if (baseTexture == null) return;

        String armSuffix = arm == HumanoidArm.RIGHT ? "_right.png" : "_left.png";
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(
                baseTexture.getNamespace(),
                baseTexture.getPath() + armSuffix
        );

        poseStack.pushPose();

        float scale = 1.01f;
        poseStack.scale(scale, scale, scale);

        PlayerRenderer renderer = (PlayerRenderer)
                Minecraft.getInstance()
                        .getEntityRenderDispatcher()
                        .getRenderer(player);

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(texture));

        ModelPart arm2 = arm == HumanoidArm.RIGHT
                ? renderer.getModel().rightArm
                : renderer.getModel().leftArm;

        poseStack.pushPose();

        arm2.translateAndRotate(poseStack);

        VertexConsumer vc = buffer.getBuffer(RenderType.entityTranslucent(texture));

        renderer.getModel().renderToBuffer(
                poseStack,
                vc,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }
}
