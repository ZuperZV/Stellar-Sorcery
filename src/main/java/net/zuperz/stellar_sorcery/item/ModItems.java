package net.zuperz.stellar_sorcery.item;

import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.block.ModBlocks;
import net.zuperz.stellar_sorcery.item.custom.AuroraSkullItem;
import net.zuperz.stellar_sorcery.item.custom.CelestialSwordItem;
import net.zuperz.stellar_sorcery.item.custom.EssenceBottleItem;

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

    public static final DeferredItem<Item> ESSENCE_BOTTLE = ITEMS.register("essence_bottle",
            () -> new EssenceBottleItem(new Item.Properties().stacksTo(1)));

    public static final DeferredItem<Item> FRITILLARIA_MELEAGRIS_SEEDS = ITEMS.register("fritillaria_meleagris_seeds",
            () -> new ItemNameBlockItem(ModBlocks.FRITILLARIA_MELEAGRIS_CROP.get(), new Item.Properties()));

    public static final DeferredItem<Item> FRITILLARIA_MELEAGRIS = ITEMS.registerItem("fritillaria_meleagris",
            Item::new, new Item.Properties().food(ModFoodProperties.FRITILLARIA_MELEAGRIS));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}