package net.zuperz.stellar_sorcery;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import net.zuperz.stellar_sorcery.block.entity.renderer.*;
import net.zuperz.stellar_sorcery.fluid.BaseFluidType;
import net.zuperz.stellar_sorcery.fluid.ModFluidTypes;
import net.zuperz.stellar_sorcery.fluid.ModFluids;
import net.zuperz.stellar_sorcery.item.custom.EssenceAmuletItem;
import net.zuperz.stellar_sorcery.item.custom.EssenceDataLoader;
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
import net.zuperz.stellar_sorcery.potion.ModPotions;
import net.zuperz.stellar_sorcery.recipes.ModRecipes;
import net.zuperz.stellar_sorcery.util.ModItemProperties;
import org.slf4j.Logger;import com.mojang.logging.LogUtils;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import net.neoforged.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

@Mod(StellarSorcery.MOD_ID)
public class StellarSorcery
{
    public static final String MOD_ID = "stellar_sorcery";
    public static final Logger LOGGER = LogUtils.getLogger();

    private static boolean networkingRegistered = false;
    private static final Map<CustomPacketPayload.Type<?>, NetworkMessage<?>> MESSAGES = new HashMap<>();

    public StellarSorcery(IEventBus modEventBus, ModContainer modContainer)
    {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerNetworking);

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
    public void onServerStarting(ServerStartingEvent event) {
    }

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {

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
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            ModItemProperties.addCustomItemProperties();
        }
    }

    private static record NetworkMessage<T extends CustomPacketPayload>(
            StreamCodec<? extends FriendlyByteBuf, T> reader,
            IPayloadHandler<T> handler) {
    }

    public static <T extends CustomPacketPayload> void addNetworkMessage(CustomPacketPayload.Type<T> id,
                                                                         StreamCodec<? extends FriendlyByteBuf, T> reader,
                                                                         IPayloadHandler<T> handler) {
        if (networkingRegistered) {
            throw new IllegalStateException("Cannot register new network messages after networking has been registered");
        }
        MESSAGES.put(id, new NetworkMessage<>(reader, handler));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void registerNetworking(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar(MOD_ID);
        MESSAGES.forEach((id, networkMessage) -> registrar.playBidirectional(id, ((NetworkMessage) networkMessage).reader(), ((NetworkMessage) networkMessage).handler()));
        networkingRegistered = true;
    }
}