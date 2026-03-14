package net.zuperz.stellar_sorcery.screen.Helpers;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.zuperz.stellar_sorcery.item.custom.GazeItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public interface IExtraSlotsProvider {
    Container getExtraSlots();

    public static List<GazeItem> getActiveGazes(Player player) {
        if (!(player instanceof IExtraSlotsProvider provider)) return Collections.emptyList();

        Container extra = provider.getExtraSlots();
        List<GazeItem> gazes = new ArrayList<>();

        for (int i = 0; i < extra.getContainerSize(); i++) {
            ItemStack stack = extra.getItem(i);
            if (stack.getItem() instanceof GazeItem gaze) {
                gazes.add(gaze);
            }
        }

        return gazes;
    }
}