package net.zuperz.stellar_sorcery.gaze;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.zuperz.stellar_sorcery.data.gaze.GazeDefinition;

public record GazeSpellContext(
        Player player,
        Level level,
        ItemStack gazeStack,
        GazeDefinition definition,
        GazeMutableStats stats,
        GazeTarget target
) {
}
