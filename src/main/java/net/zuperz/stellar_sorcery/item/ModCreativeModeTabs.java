package net.zuperz.stellar_sorcery.item;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.component.EssenceBottleData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.fluid.ModFluids;
import net.zuperz.stellar_sorcery.item.custom.EssenceBottleItem;

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

                        pOutput.accept(ModItems.SOFT_CLAY_JAR.get());
                        pOutput.accept(ModItems.CLAY_JAR.get());
                        pOutput.accept(ModItems.FIRE_CLAY_JAR.get());
                        pOutput.accept(ModItems.TWIG_CLAY_JAR.get());
                        pOutput.accept(ModItems.MAGIC_CLAY_JAR.get());
                        pOutput.accept(ModItems.EXTRACTER_CLAY_JAR.get());

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

                        // bottle_essence_of_nature
                        ItemStack bottle_essence_of_nature = new ItemStack(ModItems.ESSENCE_BOTTLE.get());
                        EssenceBottleData data_nature = new EssenceBottleData(
                                new ItemStack(ModItems.FRITILLARIA_MELEAGRIS.get()),
                                new ItemStack(ModBlocks.CALENDULA.get()),
                                new ItemStack(Items.STICK)
                        );
                        bottle_essence_of_nature.set(ModDataComponentTypes.ESSENCE_BOTTLE, data_nature);
                        pOutput.accept(bottle_essence_of_nature);

                        // bottle_essence_of_night_bloom
                        ItemStack bottle_essence_of_night_bloom = new ItemStack(ModItems.ESSENCE_BOTTLE.get());
                        EssenceBottleData data_night = new EssenceBottleData(
                                new ItemStack(Items.FERMENTED_SPIDER_EYE),
                                new ItemStack(Items.GLOW_BERRIES),
                                new ItemStack(ModBlocks.NIGELLA_DAMASCENA)
                        );
                        bottle_essence_of_night_bloom.set(ModDataComponentTypes.ESSENCE_BOTTLE, data_night);
                        pOutput.accept(bottle_essence_of_night_bloom);

                        // bottle_essence_of_chaos
                        ItemStack bottle_essence_of_chaos = new ItemStack(ModItems.ESSENCE_BOTTLE.get());
                        EssenceBottleData data_chaos = new EssenceBottleData(
                                new ItemStack(Items.GUNPOWDER),
                                new ItemStack(Items.REDSTONE),
                                new ItemStack(Items.TNT)
                        );
                        bottle_essence_of_chaos.set(ModDataComponentTypes.ESSENCE_BOTTLE, data_chaos);
                        pOutput.accept(bottle_essence_of_chaos);

                        // bottle_essence_of_knowledge
                        ItemStack bottle_essence_of_knowledge = new ItemStack(ModItems.ESSENCE_BOTTLE.get());
                        EssenceBottleData data_knowledge = new EssenceBottleData(
                                new ItemStack(Items.LAPIS_LAZULI),
                                new ItemStack(Items.BOOK),
                                new ItemStack(Items.EXPERIENCE_BOTTLE)
                        );
                        bottle_essence_of_knowledge.set(ModDataComponentTypes.ESSENCE_BOTTLE, data_knowledge);
                        pOutput.accept(bottle_essence_of_knowledge);

                        // bottle_essence_diamond_power
                        ItemStack bottle_essence_diamond_power = new ItemStack(ModItems.ESSENCE_BOTTLE.get());
                        EssenceBottleData data_diamond = new EssenceBottleData(
                                new ItemStack(Items.BLAZE_ROD),
                                new ItemStack(Items.DIAMOND),
                                new ItemStack(Items.ENDER_PEARL)
                        );
                        bottle_essence_diamond_power.set(ModDataComponentTypes.ESSENCE_BOTTLE, data_diamond);
                        pOutput.accept(bottle_essence_diamond_power);

                        // bottle_essence_of_lingering_myst
                        ItemStack bottle_essence_lingering = new ItemStack(ModItems.ESSENCE_BOTTLE.get());
                        EssenceBottleData data_lingering = new EssenceBottleData(
                                new ItemStack(Items.AMETHYST_SHARD),
                                new ItemStack(Items.PHANTOM_MEMBRANE),
                                new ItemStack(Items.POTION)
                        );
                        bottle_essence_lingering.set(ModDataComponentTypes.ESSENCE_BOTTLE, data_lingering);
                        pOutput.accept(bottle_essence_lingering);

                        // bottle_essence_of_wrath
                        ItemStack bottle_essence_wrath = new ItemStack(ModItems.ESSENCE_BOTTLE.get());
                        EssenceBottleData data_wrath = new EssenceBottleData(
                                new ItemStack(Items.BLAZE_POWDER),
                                new ItemStack(Items.GHAST_TEAR),
                                new ItemStack(Items.NETHER_STAR)
                        );
                        bottle_essence_wrath.set(ModDataComponentTypes.ESSENCE_BOTTLE, data_wrath);
                        pOutput.accept(bottle_essence_wrath);

                        // bottle_essence_of_radiance
                        ItemStack bottle_essence_radiance = new ItemStack(ModItems.ESSENCE_BOTTLE.get());
                        EssenceBottleData data_radiance = new EssenceBottleData(
                                new ItemStack(Items.DRAGON_BREATH),
                                new ItemStack(Items.GLASS_BOTTLE),
                                new ItemStack(Items.GLOWSTONE_DUST)
                        );
                        bottle_essence_radiance.set(ModDataComponentTypes.ESSENCE_BOTTLE, data_radiance);
                        pOutput.accept(bottle_essence_radiance);

                        /*
                        // bottle_essence //
                        ItemStack bottle_essence = new ItemStack(ModItems.ESSENCE_BOTTLE.get());
                        EssenceBottleData _data = new EssenceBottleData(
                                new ItemStack(ModItems.FRITILLARIA_MELEAGRIS.get()),
                                new ItemStack(ModBlocks.CALENDULA.get()),
                                new ItemStack(Items.STICK)
                        );
                        bottle_essence.set(ModDataComponentTypes.ESSENCE_BOTTLE, _data);
                        pOutput.accept(bottle_essence);
                         */

                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}