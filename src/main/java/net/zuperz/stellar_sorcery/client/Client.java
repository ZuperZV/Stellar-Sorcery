package net.zuperz.stellar_sorcery.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderItemInFrameEvent;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;

import static net.zuperz.stellar_sorcery.StellarSorcery.MOD_ID;

@EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
public class Client {

    @SubscribeEvent
    public static void onRenderItemInFrame(RenderItemInFrameEvent  event) {
        ItemStack stack = event.getItemStack();

        var sigil = stack.get(ModDataComponentTypes.SIGIL.get());
        if (sigil == null || sigil.getSigils().isEmpty()) return;

        PoseStack pose = event.getPoseStack();
        MultiBufferSource buffer = event.getMultiBufferSource();

        pose.pushPose();
        pose.translate(0, 0, 0.001);

        Minecraft mc = Minecraft.getInstance();
        ItemRenderer itemRenderer = mc.getItemRenderer();

        BakedModel model = itemRenderer.getModel(
                stack,
                null,
                null,
                0
        );

        ResourceLocation texture = getSigilTexture(stack);

        VertexConsumer consumer =
                buffer.getBuffer(RenderType.entityTranslucent(texture));

        /*itemRenderer.renderQuadList(
                pose,
                consumer,
                model.getQuads(null, null, RandomSource.create()),
                stack,
                event.getPackedLight(),
                OverlayTexture.NO_OVERLAY
        );
         */

        pose.popPose();
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

        return ResourceLocation.fromNamespaceAndPath(
                MOD_ID,
                "textures/item/sigil_overlay_" + type + ".png"
        );
    }
}
