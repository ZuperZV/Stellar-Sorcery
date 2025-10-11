package net.zuperz.stellar_sorcery.item;

import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;

public class ModItemProperties {
    public static void addCustomItemProperties() {
        ItemProperties.register(ModItems.BLOOD_VIAL.get(), ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "on"),
                (pStack, pLevel, pEntity, pSeed) -> pStack.get(ModDataComponentTypes.PLAYER_DATA) != null ? 1f : 0f);

        ItemProperties.register(ModItems.WOODOO_DOLL.get(), ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "on"),
                (pStack, pLevel, pEntity, pSeed) -> pStack.get(ModDataComponentTypes.PLAYER_DATA) != null ? 1f : 0f);

        ItemProperties.register(ModItems.CHALK_CANISTER.get(), ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "on"),
                (pStack, pLevel, pEntity, pSeed) -> pStack.get(ModDataComponentTypes.BLOCK_STORAGE_DATA) != null ? 1f : 0f);

        ItemProperties.register(ModItems.WRAITH_CLOAK.get(), ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "on"),
                (pStack, pLevel, pEntity, pSeed) -> pStack.get(ModDataComponentTypes.ACTIVE) != null && pStack.get(ModDataComponentTypes.ACTIVE).getActive() ? 1f : 0f);
    }
}