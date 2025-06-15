package net.zuperz.stellar_sorcery;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.block.entity.ModBlockEntities;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralAltarBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.custom.AstralNexusBlockEntity;
import net.zuperz.stellar_sorcery.block.entity.renderer.ArcForgeBlockEntityRenderer;
import net.zuperz.stellar_sorcery.block.entity.renderer.AstralAltarBlockEntityRenderer;
import net.zuperz.stellar_sorcery.block.entity.renderer.AstralNexusBlockEntityRenderer;
import net.zuperz.stellar_sorcery.entity.ModEntities;
import net.zuperz.stellar_sorcery.entity.client.ModModelLayers;
import net.zuperz.stellar_sorcery.entity.client.SigilOrbModel;
import net.zuperz.stellar_sorcery.entity.client.SigilOrbRenderer;
import net.zuperz.stellar_sorcery.entity.custom.SigilOrbEntity;
import net.zuperz.stellar_sorcery.item.ModCreativeModeTabs;
import net.zuperz.stellar_sorcery.item.ModItems;
import net.zuperz.stellar_sorcery.recipes.ModRecipes;
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

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(StellarSorcery.MOD_ID)
public class StellarSorcery
{
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "stellar_sorcery";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    private static boolean networkingRegistered = false;
    private static final Map<CustomPacketPayload.Type<?>, NetworkMessage<?>> MESSAGES = new HashMap<>();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public StellarSorcery(IEventBus modEventBus, ModContainer modContainer)
    {

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerNetworking);

        ModItems.register(modEventBus);
        ModBlocks.register(modEventBus);

        ModRecipes.register(modEventBus);

        ModCreativeModeTabs.register(modEventBus);
        //ModDataComponentTypes.register(modEventBus);

        //ModMenuTypes.register(modEventBus);
        ModBlockEntities.register(modEventBus);

        ModEntities.register(modEventBus);

        //ResourceEntityDataSerializers.register(modEventBus);
        //ResourceSensorTypes.register(modEventBus);

        //StellarSorceryRegistry registry = StellarSorceryRegistry.getInstance();
        //ModStellarSorcerys.registerAll(registry);
        //modEventBus.addListener(ModItems::onRegisterItems);

        NeoForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent

    @EventBusSubscriber(modid = MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        /*
        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.KILN_MENU.get(), KilnScreen::new);
            event.register(ModMenuTypes.FORGE_CORE_MENU.get(), ForgeCoreScreen::new);
            event.register(ModMenuTypes.WOODEN_WETTLE_MENU.get(), WoodenKettleScreen::new);
        }
         */

        @SubscribeEvent
        public static void registerBER(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.ASTRAL_ALTAR_BE.get(), AstralAltarBlockEntityRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.ASTRAL_NEXUS_BE.get(), AstralNexusBlockEntityRenderer::new);

            event.registerBlockEntityRenderer(ModBlockEntities.ARCFORGE_BE.get(), ArcForgeBlockEntityRenderer::new);
        }

        @SubscribeEvent
        public static void registerAttributes(EntityAttributeCreationEvent event) {
            event.put(ModEntities.SIGIL_ORB.get(), SigilOrbEntity.createAttributes().build());
        }

        @SubscribeEvent
        public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
            event.registerLayerDefinition(ModModelLayers.SIGIL_ORB, SigilOrbModel::createBodyLayer);
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(ModEntities.SIGIL_ORB.get(), SigilOrbRenderer::new);

            //event.enqueueWork(() -> {
            //    ItemBlockRenderTypes.setRenderLayer(ModBlocks.WOODEN_KETTLE.get(), RenderType.translucent());
            //});
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