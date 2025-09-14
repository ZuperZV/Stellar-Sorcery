package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.component.PlayerData;

import java.util.List;

public class BloodVialItem extends Item {
    public BloodVialItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCraftingRemainingItem() {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        return stack.copy();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        PlayerData data = stack.get(ModDataComponentTypes.PLAYER_DATA);
        if (data != null) {
            tooltip.add(Component.literal("   ")
                    .append(Component.literal(data.getPlayerName()).withStyle(ChatFormatting.BLUE))
                    .append(Component.translatable("tooltip.stellar_sorcery.blood").withStyle(ChatFormatting.BLUE)));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
