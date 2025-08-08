package net.zuperz.stellar_sorcery.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.fluid.ModFluids;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, StellarSorcery.MOD_ID);

    public static final Supplier<CreativeModeTab> STELLAR_SORCERY_TAB =
            CREATIVE_MODE_TABS.register("stellar_sorcery_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("creativetab.stellar_sorcery.stellar_sorcery_tab"))
                    .icon(() -> new ItemStack(ModItems.CELESTIAL_BLADE.get()))
                    .displayItems((pParameters, pOutput) -> {
                        pOutput.accept(ModItems.CELESTIAL_BLADE.get());

                        pOutput.accept(ModBlocks.ASTRAL_ALTAR.get());
                        pOutput.accept(ModBlocks.ASTRAL_NEXUS.get());

                        pOutput.accept(ModBlocks.VITAL_STUMP.get());
                        pOutput.accept(ModBlocks.STUMP.get());

                        pOutput.accept(ModBlocks.ESSENCE_BOILER.get());
                        pOutput.accept(ModItems.EMPTY_ESSENCE_BOTTLE.get());

                        pOutput.accept(ModItems.ROOT.get());

                        pOutput.accept(ModItems.FRITILLARIA_MELEAGRIS.get());
                        pOutput.accept(ModItems.FRITILLARIA_MELEAGRIS_SEEDS.get());

                        pOutput.accept(ModBlocks.RED_CAMPION.get());
                        pOutput.accept(ModBlocks.CALENDULA.get());
                        pOutput.accept(ModBlocks.NIGELLA_DAMASCENA.get());

                        pOutput.accept(ModBlocks.BUDDING_MOONSHINE.get());
                        pOutput.accept(ModBlocks.MOONSHINE_CLUSTER.get());
                        pOutput.accept(ModItems.MOONSHINE_SHARD.get());
                        pOutput.accept(ModItems.MOONSHINE_CATALYST.get());

                        pOutput.accept(ModFluids.NOCTILUME_BUCKET.get());
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}