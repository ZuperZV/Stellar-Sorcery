package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.zuperz.stellar_sorcery.component.CelestialData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.component.StarDustData;

import java.util.List;

public class CelestialSwordItem extends SwordItem {

    public CelestialSwordItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        CelestialData data = stack.get(ModDataComponentTypes.CELESTIAL.get());

        if (data == null) {
            ItemStack toStore = player.getOffhandItem();

            if (!toStore.isEmpty()) {
                ItemStack stored = toStore.copy();
                stored.setCount(1);

                stack.set(ModDataComponentTypes.CELESTIAL.get(), new CelestialData(stored));

                toStore.shrink(1);

                return InteractionResultHolder.success(stack);
            }
        } else {
            ItemStack embedded = data.getEmbeddedItem();

            if (!player.getInventory().add(embedded.copy())) {
                player.drop(embedded.copy(), false);
            }

            stack.remove(ModDataComponentTypes.CELESTIAL.get());

            return InteractionResultHolder.success(stack);
        }

        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
        CelestialData data = pStack.get(ModDataComponentTypes.CELESTIAL.get());
        if (data != null) {
            ItemStack embedded = data.getEmbeddedItem();

            pTooltipComponents.add(CommonComponents.EMPTY);
            pTooltipComponents.add(Component.translatable("tooltip.stellar_sorcery.runic_channels").append(": ").withStyle(style -> style.withColor(ChatFormatting.GRAY)));
            pTooltipComponents.add(Component.literal(" ").append(Component.translatable(embedded.getDescriptionId())).append(" ").append(Component.translatable("tooltip.stellar_sorcery.embedded")).withStyle(style -> style.withColor(ChatFormatting.DARK_GREEN)));

            super.appendHoverText(pStack, (TooltipContext) pContext, pTooltipComponents, pTooltipFlag);
        }
    }
}
