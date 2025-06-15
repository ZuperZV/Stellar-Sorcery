package net.zuperz.stellar_sorcery.item;

import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.zuperz.stellar_sorcery.StellarSorcery;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(StellarSorcery.MOD_ID);

    //properties -> new FuelItem(properties, 800)

    public static final DeferredItem<Item> CELESTIAL_BLADE = ITEMS.register("celestial_blade",
            () -> new SwordItem(ModToolTiers.CELESTIAL,
                    new Item.Properties().attributes(SwordItem.createAttributes(ModToolTiers.CELESTIAL, 3, -2.4f))));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}

/*
if (level.isClientSide) {
    StellarSorceryClient.triggerAcceleratorShader();
}

 */