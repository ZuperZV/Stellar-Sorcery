package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.zuperz.stellar_sorcery.component.ActiveData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;

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
}
