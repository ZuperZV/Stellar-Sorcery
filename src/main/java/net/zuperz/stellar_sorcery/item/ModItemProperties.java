package net.zuperz.stellar_sorcery.item;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.data.SigilDataLoader;
import net.zuperz.stellar_sorcery.item.custom.SigilItem;

import java.util.Locale;

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

        ItemProperties.register(ModItems.CODEX_ARCANUM.get(), ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "tier"),
                (ItemStack stack, ClientLevel level, LivingEntity entity, int seed) -> {
                    var data = stack.get(ModDataComponentTypes.CODEX_TIER);
                    return data != null ? (float) data.getTier() : 0f;
                }
        );

        ItemProperties.register(ModItems.SIGIL.get(),
                ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "tier"),
                (ItemStack stack, ClientLevel level, LivingEntity entity, int seed) -> {

                    var data = stack.get(ModDataComponentTypes.SIGIL_NAME);
                    if (data == null) {
                        return 0.0f;
                    }

                    String armorType = SigilDataLoader.getArmorByName(data.name());

                    if (armorType == null) {
                        return 1.0f;
                    }

                    armorType = armorType.toLowerCase(Locale.ROOT);

                    return switch (armorType) {
                        case "helmet" -> 2.0f;
                        case "chestplate" -> 3.0f;
                        case "leggings" -> 4.0f;
                        case "boots" -> 5.0f;
                        default -> 1.0f;
                    };
                }
        );
    }
}