package net.zuperz.stellar_sorcery.spell;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public interface ISpell {
    void cast(Level level, Player player);
    String getName();
}
