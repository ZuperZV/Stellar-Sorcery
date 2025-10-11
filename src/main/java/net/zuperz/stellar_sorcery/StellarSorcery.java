package net.zuperz.stellar_sorcery;

import net.minecraft.core.component.DataComponents;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import net.zuperz.stellar_sorcery.block.entity.renderer.*;
import net.zuperz.stellar_sorcery.data.CodexDataLoader;
import net.zuperz.stellar_sorcery.fluid.BaseFluidType;
import net.zuperz.stellar_sorcery.fluid.ModFluidTypes;
import net.zuperz.stellar_sorcery.fluid.ModFluids;
import net.zuperz.stellar_sorcery.item.ModItemProperties;
import net.zuperz.stellar_sorcery.item.custom.EssenceAmuletItem;
import net.zuperz.stellar_sorcery.data.EssenceDataLoader;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.entity.ModEntities;
import net.zuperz.stellar_sorcery.entity.client.ModModelLayers;
import net.zuperz.stellar_sorcery.entity.client.SigilOrbModel;
import net.zuperz.stellar_sorcery.entity.custom.SigilOrbEntity;
import net.zuperz.stellar_sorcery.item.ModCreativeModeTabs;
import net.zuperz.stellar_sorcery.item.ModItems;
import net.zuperz.stellar_sorcery.item.custom.EssenceBottleItem;
import net.zuperz.stellar_sorcery.item.custom.decorator.EssenceBottleClientTooltip;
import net.zuperz.stellar_sorcery.item.custom.decorator.EssenceBottleTooltip;
import net.zuperz.stellar_sorcery.item.custom.decorator.StarDustNumberBarDecorator;
import net.zuperz.stellar_sorcery.network.SyncBookmarksPacket;
import net.zuperz.stellar_sorcery.potion.ModPotions;
import net.zuperz.stellar_sorcery.recipes.ModRecipes;
import net.zuperz.stellar_sorcery.screen.CodexArcanumScreen;
import net.zuperz.stellar_sorcery.screen.ModMenuTypes;
import org.slf4j.Logger;import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.zuperz.stellar_sorcery.network.SetBookmarksPacket;

@Mod(StellarSorcery.MOD_ID)
public class StellarSorcery
{
    public static final String MOD_ID = "stellar_sorcery";
    public static final Logger LOGGER = LogUtils.getLogger();

    public StellarSorcery(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerNetworkHandlers);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        ModFluids.register(modEventBus);
        ModFluidTypes.register(modEventBus);

        ModCreativeModeTabs.register(modEventBus);
        ModPotions.register(modEventBus);

        ModDataComponentTypes.register(modEventBus);
        ModRecipes.register(modEventBus);

        ModBlockEntities.register(modEventBus);
        ModEntities.register(modEventBus);

        ModMenuTypes.register(modEventBus);

        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.RED_CAMPION.getId(), ModBlocks.POTTED_RED_CAMPION);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.CALENDULA.getId(), ModBlocks.POTTED_CALENDULA);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(ModBlocks.NIGELLA_DAMASCENA.getId(), ModBlocks.POTTED_NIGELLA_DAMASCENA);
        });

        ModFluids.registerFluidInteractions();

        EssenceDataLoader.load();
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        CodexDataLoader.load();
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.CODEX_ARCANUM_MENU.get(), CodexArcanumScreen::new);
        }

        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.ASTRAL_ALTAR_BE.get(), AstralAltarBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.ASTRAL_NEXUS_BE.get(), AstralNexusBlockEntityRenderer::new);

            event.registerBlockEntityRenderer(ModBlockEntities.VITAL_STUMP_BE.get(), VitalStumpBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.STUMP_BE.get(), StumpBlockEntityRenderer::new);

            event.registerBlockEntityRenderer(ModBlockEntities.ESSENCE_BOILER_BE.get(), EssenceBoilerBlockEntityRenderer::new);

            event.registerBlockEntityRenderer(ModBlockEntities.LUNAR_LIGHT_BEAM_EMITTER_BE.get(), lightBeamBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.LUNAR_INFUSER_BE.get(), LunarInfuserBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.LUNAR_JAR_BE.get(), LunarJarBlockEntityRenderer::new);

            event.registerBlockEntityRenderer(ModBlockEntities.LIGHT_BEAM_EMITTER_BE.get(), lightBeamBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.LIGHT_INFUSER_BE.get(), LunarInfuserBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.LIGHT_JAR_BE.get(), LunarJarBlockEntityRenderer::new);

            event.registerBlockEntityRenderer(ModBlockEntities.ALTER_BE.get(), SoulCandleBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.ARCFORGE_BE.get(), ArcForgeBlockEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerItemDecorators(RegisterItemDecorationsEvent event) {
            event.register(ModItems.AURORA_SKULL.get(), new StarDustNumberBarDecorator());
        }

        @SubscribeEvent
        public static void registerTooltipFactories(RegisterClientTooltipComponentFactoriesEvent event) {
            event.register(EssenceBottleTooltip.class, EssenceBottleClientTooltip::new);
        }

        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(ModEntities.SIGIL_ORB.get(), SigilOrbEntity.createAttributes().build());
        }

        @SubscribeEvent
        public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(ModModelLayers.SIGIL_ORB, SigilOrbModel::createBodyLayer);

            event.registerLayerDefinition(
                    AstralAltarBlockEntityRenderer.MAGIC_AURA_LAYER,
                    AstralAltarBlockEntityRenderer::createMagicAuraLayer
            );
        }

        @SubscribeEvent
        public static void onClientExtensions(RegisterClientExtensionsEvent event) {
            event.registerFluidType(((BaseFluidType) ModFluidTypes.NOCTILUME_FLUID_TYPE.get()).getClientFluidTypeExtensions(),
                    ModFluidTypes.NOCTILUME_FLUID_TYPE.get());
        }

        @SubscribeEvent
        public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
            event.register((stack, tintIndex) -> {
                if (stack.getItem() instanceof EssenceBottleItem bottleItem) {
                    return bottleItem.getColor(stack, tintIndex);
                }
                return -1;
            }, ModItems.ESSENCE_BOTTLE.get());


            event.register((stack, tintIndex) -> {
                if (stack.getItem() instanceof EssenceAmuletItem bottleItem) {
                    return bottleItem.getColor(stack, tintIndex);
                }
                return -1;
            }, ModItems.ESSENCE_AMULET.get());


            event.register(
                    (ItemStack stack, int tintIndex) -> {
                        if (tintIndex != 0) {
                            return -1;
                        }

                        DyedItemColor dyedColor = stack.get(DataComponents.DYED_COLOR);

                        if (dyedColor == null) {
                            return FastColor.ARGB32.opaque(0x4f4972);
                        }

                        int color = dyedColor.rgb();
                        return FastColor.ARGB32.opaque(color);
                    },
                    ModItems.CODEX_ARCANUM.get()
            );
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            ModItemProperties.addCustomItemProperties();
        }
    }

    public void registerNetworkHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID)
                .versioned("1.0")
                .optional();

        registrar.playToServer(
                SetBookmarksPacket.TYPE,
                SetBookmarksPacket.STREAM_CODEC,
                SetBookmarksPacket::handle
        );
        registrar.playToClient(
                SyncBookmarksPacket.TYPE,
                SyncBookmarksPacket.STREAM_CODEC,
                SyncBookmarksPacket::handle
        );

        LOGGER.info("[StellarSorcery] Network channel registered for BookmarkPacket!");
    }
}