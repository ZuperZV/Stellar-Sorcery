package net.zuperz.stellar_sorcery.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockElement;
import net.minecraft.client.renderer.block.model.ItemModelGenerator;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.*;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.client.SigilOverlayModel;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.item.ModItems;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static net.zuperz.stellar_sorcery.StellarSorcery.MOD_ID;

@Mixin(ItemRenderer.class)
public abstract class ItemRendererMixin {

    @Inject(method = "render", at = @At("RETURN"))
    private void stellar_sorcery$renderOverlay(
            ItemStack stack,
            ItemDisplayContext displayContext,
            boolean leftHanded,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay,
            BakedModel originalModel,
            CallbackInfo ci
    ) {
        if (stack.isEmpty()) return;
        if (!shouldRenderOverlay(stack)) return;

        ItemRenderer renderer = (ItemRenderer) (Object) this;

        ResourceLocation rl = getSigilTexture(stack);
        ModelResourceLocation mrl = new ModelResourceLocation(rl, "inventory");
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(mrl);

        BakedModel overlayModel = new SigilOverlayModel(model);

        RandomSource rand = RandomSource.create();
        VertexConsumer consumer = buffer.getBuffer(RenderType.cutout());

        if (displayContext == ItemDisplayContext.GROUND) {
            poseStack.scale(0.501F, 0.501F, 0.501F);
            poseStack.translate(-0.5F, -0.25F, -0.5F);
        } else if (displayContext == ItemDisplayContext.GUI) {
            poseStack.scale(1f, 1F, 1f);
            poseStack.translate(-0.5F, -0.5F, 0.5F);
        } else if (displayContext == ItemDisplayContext.FIXED) {
            poseStack.scale(0.5009F, 0.5009F, 0.5009F);
            poseStack.translate(-0.5F, -0.5F, -0.5F);
        } else if (displayContext == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND) {
            poseStack.scale(0.6809F, 0.6809F, 0.6809F);
            poseStack.translate(-0.39645F, 0.054F, 0.771F);

            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZN.rotationDegrees(25.0F));
        } else if (displayContext == ItemDisplayContext.FIRST_PERSON_LEFT_HAND) {
            poseStack.scale(0.6809F, 0.6809F, 0.6809F);
            poseStack.translate(-0.59645F, 0.054F, 0.781F);

            poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
            poseStack.mulPose(Axis.ZN.rotationDegrees(25.0F));
        }

        for (var quad : overlayModel.getQuads(null, null, rand)) {
            renderer.renderQuadList(poseStack, consumer, java.util.List.of(quad), stack, packedLight, packedOverlay);
        }
    }

    private static ResourceLocation getSigilTexture(ItemStack stack) {
        Item item = stack.getItem();

        String type = "generic";

        if (item instanceof ArmorItem armor) {
            type = switch (armor.getType()) {
                case HELMET -> "helmet";
                case CHESTPLATE -> "chestplate";
                case LEGGINGS -> "leggings";
                case BOOTS -> "boots";
                case BODY -> "body";
            };
        }

        return ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "sigil_overlay_" + type
        );
    }

    private boolean shouldRenderOverlay(ItemStack stack) {
        if (stack.getItem() instanceof ArmorItem armor) {
            return stack.getComponents().get(ModDataComponentTypes.SIGIL.get()) != null &&
                    !stack.getComponents().get(ModDataComponentTypes.SIGIL.get()).getSigils().isEmpty();
        }
        return false;
    }
}