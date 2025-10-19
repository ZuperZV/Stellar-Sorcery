package net.zuperz.stellar_sorcery.block;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.fluid.ModFluidTypes;
import net.zuperz.stellar_sorcery.fluid.ModFluids;

@EventBusSubscriber(modid = StellarSorcery.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class ModItemBlockRenderTypes {
    @SubscribeEvent
    public static void registerItemModelProperties(final FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ASTRAL_ALTAR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ASTRAL_NEXUS.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.WHITE_CHALK.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SOUL_CANDLE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ESSENCE_BOILER.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.STAR_LIGHT.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.LUNAR_JAR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.LUNAR_INFUSER.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.LIGHT_JAR.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.LIGHT_INFUSER.get(), RenderType.cutout());

            ItemBlockRenderTypes.setRenderLayer(ModBlocks.ELDRITE.get(), RenderType.cutout());

            ItemBlockRenderTypes.setRenderLayer(ModFluids.SOURCE_NOCTILUME.get(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(ModFluids.FLOWING_NOCTILUME.get(), RenderType.translucent());
        });
    }
}