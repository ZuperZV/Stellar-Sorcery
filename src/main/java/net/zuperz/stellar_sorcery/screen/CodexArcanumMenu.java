package net.zuperz.stellar_sorcery.screen;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class CodexArcanumMenu extends AbstractContainerMenu {

    public CodexArcanumMenu(int containerId, Player player) {
        super(ModMenuTypes.CODEX_ARCANUM_MENU.get(), containerId);
    }

    @Override
    public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
        return null;
    }

    @Override
    public boolean stillValid(Player p_38874_) {
        return true;
    }
}