package net.zuperz.stellar_sorcery.util;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.item.ModItems;

public class ModItemProperties {
    public static void addCustomItemProperties() {
        ItemProperties.register(ModItems.BLOOD_VIAL.get(), ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "on"),
                (pStack, pLevel, pEntity, pSeed) -> pStack.get(ModDataComponentTypes.PLAYER_DATA) != null ? 1f : 0f);

        ItemProperties.register(ModItems.WOODOO_DOLL.get(), ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "on"),
                (pStack, pLevel, pEntity, pSeed) -> pStack.get(ModDataComponentTypes.PLAYER_DATA) != null ? 1f : 0f);
    }
}