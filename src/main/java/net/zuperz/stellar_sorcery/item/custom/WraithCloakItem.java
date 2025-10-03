package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.zuperz.stellar_sorcery.component.ActiveData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;

import java.util.List;
import java.util.Objects;

public class WraithCloakItem extends Item {

    public WraithCloakItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);

        if (level.isClientSide) return InteractionResultHolder.success(stack);

        ActiveData data = stack.get(ModDataComponentTypes.ACTIVE);

        if (data == null) {
            data = new ActiveData(false);
        }

        data = new ActiveData(!data.getActive());

        stack.set(ModDataComponentTypes.ACTIVE, data);

        return InteractionResultHolder.success(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slot, isSelected);

        if (!(entity instanceof Player player)) return;
        if (level.isClientSide) return;

        var activeComponent = stack.get(ModDataComponentTypes.ACTIVE);
        if (activeComponent != null && activeComponent.getActive()) {
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 50));
        }
    }
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext tooltipContext, List<Component> tooltip, TooltipFlag flag) {

        MobEffectInstance invisibility = new MobEffectInstance(MobEffects.INVISIBILITY, 50);

        MutableComponent line = Component.translatable(invisibility.getDescriptionId());

        if (invisibility.getAmplifier() > 0) {
            line = line.append(" ")
                    .append(Component.translatable("potion.potency." + invisibility.getAmplifier()));
        }

        tooltip.add(line.withStyle(invisibility.getEffect().value().getCategory().getTooltipFormatting()));
    }
}
