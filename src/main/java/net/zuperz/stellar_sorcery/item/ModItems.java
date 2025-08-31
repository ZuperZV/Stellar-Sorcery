package net.zuperz.stellar_sorcery.item;

import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zuperz.stellar_sorcery.potion.ModPotions;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.item.custom.*;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(StellarSorcery.MOD_ID);

    //properties -> new FuelItem(properties, 800)

    public static final DeferredItem<Item> CELESTIAL_BLADE = ITEMS.register("celestial_blade",
            () -> new CelestialSwordItem(ModToolTiers.CELESTIAL,
                    new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.CELESTIAL, 3, -2.4f))));

    public static final DeferredItem<Item> AURORA_SKULL = ITEMS.register("aurora_skull",
            () -> new AuroraSkullItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> ROOT = ITEMS.register("root",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> SOFT_CLAY_JAR = ITEMS.register("soft_clay_jar",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> CLAY_JAR = ITEMS.register("clay_jar",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> FIRE_CLAY_JAR = ITEMS.register("fire_clay_jar",
            () -> new JarPotionsItem(ModPotions.JAR_FIRE_RESISTANCE));

    public static final DeferredItem<Item> TWIG_CLAY_JAR = ITEMS.register("twig_clay_jar",
            () -> new JarPotionsItem(ModPotions.JAR_TWIG));

    public static final DeferredItem<Item> WIND_CLAY_JAR = ITEMS.register("wind_clay_jar",
            () -> new JarPotionsItem(ModPotions.JAR_WIND));

    public static final DeferredItem<Item> WATER_CLAY_JAR = ITEMS.register("water_clay_jar",
            () -> new JarPotionsItem(ModPotions.JAR_WATER));

    public static final DeferredItem<Item> SHADOW_CLAY_JAR = ITEMS.register("shadow_clay_jar",
            () -> new JarPotionsItem(ModPotions.JAR_SHADOW));

    public static final DeferredItem<Item> STONE_CLAY_JAR = ITEMS.register("stone_clay_jar",
            () -> new JarPotionsItem(ModPotions.JAR_STONE));

    public static final DeferredItem<Item> SUN_CLAY_JAR = ITEMS.register("sun_clay_jar",
            () -> new JarPotionsItem(ModPotions.JAR_SUN));

    public static final DeferredItem<Item> FROST_CLAY_JAR = ITEMS.register("frost_clay_jar",
            () -> new JarPotionsItem(ModPotions.JAR_FROST));

    public static final DeferredItem<Item> STORM_CLAY_JAR = ITEMS.register("storm_clay_jar",
            () -> new JarPotionsItem(ModPotions.JAR_STORM));

    public static final DeferredItem<Item> EXTRACTER_CLAY_JAR = ITEMS.register("extracter_clay_jar",
            () -> new JarPotionsItem(ModPotions.JAR_EXTRACTER));


    public static final DeferredItem<Item> ESSENCE_BOTTLE = ITEMS.register("essence_bottle",
            () -> new EssenceBottleItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> EMPTY_ESSENCE_BOTTLE = ITEMS.register("empty_essence_bottle",
            () -> new Item(new Item.Properties().stacksTo(16)));

    public static final DeferredItem<Item> FRITILLARIA_MELEAGRIS_SEEDS = ITEMS.register("fritillaria_meleagris_seeds",
            () -> new ItemNameBlockItem(ModBlocks.FRITILLARIA_MELEAGRIS_CROP.get(), new Item.Properties()));

    public static final DeferredItem<Item> FRITILLARIA_MELEAGRIS = ITEMS.registerItem("fritillaria_meleagris",
            Item::new, new Item.Properties().food(ModFoodProperties.FRITILLARIA_MELEAGRIS));

    public static final DeferredItem<Item> MOONSHINE_CATALYST = ITEMS.register("moonshine_catalyst",
            () -> new MoonshineCatalystItem(new Item.Properties()));

    public static final DeferredItem<Item> MOONSHINE_SHARD = ITEMS.register("moonshine_shard",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> WHISPERING_FRAGMENT = ITEMS.register("whispering_fragment",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> BLUESTONE_DUST = ITEMS.register("bluestone_dust",
            () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}