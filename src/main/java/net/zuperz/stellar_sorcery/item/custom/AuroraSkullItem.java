package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import net.zuperz.stellar_sorcery.component.StarDustData;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AuroraSkullItem extends Item {
    public AuroraSkullItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        StarDustData starDust = stack.get(ModDataComponentTypes.STAR_DUST.get());

        if (starDust == null) {
            stack.set(ModDataComponentTypes.STAR_DUST.get(), new StarDustData(0));
        } else {
            int currentValue = starDust.getValue1();

            if (currentValue < 100) {
                stack.set(ModDataComponentTypes.STAR_DUST.get(), new StarDustData(currentValue + 1));
            }
        }

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {

        StarDustData starDust = pStack.get(ModDataComponentTypes.STAR_DUST.get());

        pTooltipComponents.add(CommonComponents.EMPTY);
        pTooltipComponents.add(Component.translatable("tooltip.stellar_sorcery.collected_star_dust").append(": ").withStyle(style -> style.withColor(ChatFormatting.GRAY)));
        pTooltipComponents.add(Component.translatable(" " + starDust.getValue1()).append(" ").append(Component.translatable("tooltip.stellar_sorcery.star_dust")).withStyle(style -> style.withColor(ChatFormatting.DARK_GREEN)));

        super.appendHoverText(pStack, (TooltipContext) pContext, pTooltipComponents, pTooltipFlag);
    }

    /*
    @Override
    public boolean isFoil(ItemStack stack) {
        StarDustData starDust = stack.get(ModDataComponentTypes.STAR_DUST.get());
        return starDust != null && starDust.getValue1() >= 100;
    }
     */

    public void decrease(ItemStack stack, int amount) {
        StarDustData starDust = stack.get(ModDataComponentTypes.STAR_DUST.get());

        starDust.decrease(amount);
    }

    public @Nullable StarDustData GetStarDust(ItemStack stack) {
        return stack.get(ModDataComponentTypes.STAR_DUST);
    }
}
