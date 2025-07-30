package net.zuperz.stellar_sorcery.block;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.zuperz.stellar_sorcery.StellarSorcery;

@EventBusSubscriber(modid = StellarSorcery.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ModItemBlockRenderTypes {
    @SubscribeEvent
    public static void registerItemModelProperties(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ASTRAL_ALTAR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ASTRAL_NEXUS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ESSENCE_BOILER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.STAR_LIGHT.get(), RenderType.cutout());
        });
    }
}