package net.zuperz.stellar_sorcery.item.custom;

import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zuperz.stellar_sorcery.screen.CodexArcanumMenu;

public class CodexArcanumItem extends Item {

    public CodexArcanumItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide) {
            player.openMenu(this.getMenuProvider(new ItemStack(this)));
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }

    private MenuProvider getMenuProvider(ItemStack itemStack) {
        return new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return itemStack.getHoverName();
            }

            @Override
            public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
                return new CodexArcanumMenu(i, inventory.player);
            }
        };
    }
}