package net.zuperz.stellar_sorcery.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.entity.custom.SigilOrbEntity;

public class SigilOrbRenderer extends MobRenderer<SigilOrbEntity, SigilOrbModel> {
    public SigilOrbRenderer(EntityRendererProvider.Context pContext) {
        super(pContext, new SigilOrbModel(pContext.bakeLayer(ModModelLayers.SIGIL_ORB)), 0.75f);
    }

    
    @Override
    public ResourceLocation getTextureLocation(SigilOrbEntity entity) {
        return ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "textures/entity/sigil_orb.png");
    }

    @Override
    public void render(SigilOrbEntity pEntity, float pEntityYaw, float pPartialTicks, PoseStack pPoseStack,
                       MultiBufferSource pBuffer, int pPackedLight) {
        super.render(pEntity, pEntityYaw, pPartialTicks, pPoseStack, pBuffer, pPackedLight);
    }
}