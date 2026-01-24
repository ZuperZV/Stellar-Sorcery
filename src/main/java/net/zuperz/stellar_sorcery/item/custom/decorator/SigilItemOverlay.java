package net.zuperz.stellar_sorcery.item.custom.decorator;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.IItemDecorator;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;

public class SigilItemOverlay implements IItemDecorator {

    @Override
    public boolean render(
            GuiGraphics guiGraphics,
            Font font,
            ItemStack stack,
            int x,
            int y
    ) {
        if (stack.getComponents().get(ModDataComponentTypes.SIGIL.get()) == null || stack.getComponents().get(ModDataComponentTypes.SIGIL.get()).getSigils().isEmpty()) {
            return false;
        }

        String stackArmorType = "";

        if (isArmor(stack.getItem(), ArmorItem.Type.HELMET)) {
            stackArmorType = "helmet";
        } else if (isArmor(stack.getItem(), ArmorItem.Type.CHESTPLATE)) {
            stackArmorType = "chestplate";
        } else if (isArmor(stack.getItem(), ArmorItem.Type.LEGGINGS)) {
            stackArmorType = "leggings";
        } else if (isArmor(stack.getItem(), ArmorItem.Type.BOOTS)) {
            stackArmorType = "boots";
        }

        if (stackArmorType.isEmpty()) {
            return false;
        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 200);

        /*guiGraphics.blit(
                ResourceLocation.fromNamespaceAndPath(
                        StellarSorcery.MOD_ID,
                        "textures/item/sigil_overlay_" + stackArmorType + ".png"
                ),
                x,
                y,
                0,
                0,
                16,
                16,
                16,
                16
        );
         */

        guiGraphics.pose().popPose();

        return true;
    }

    public boolean isArmor(Item item, ArmorItem.Type type) {
        if (item instanceof ArmorItem) {
            ArmorItem armor = (ArmorItem) item;
            return armor.getType() == type;
        }
        return false;
    }
}