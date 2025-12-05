package net.zuperz.stellar_sorcery.data.spell;

import net.zuperz.stellar_sorcery.data.spell.AreaFile;
import net.zuperz.stellar_sorcery.data.spell.RuneFile;
import net.zuperz.stellar_sorcery.data.spell.ModifierFile;

import java.util.List;

public class SpellBlueprint {

    public final String id;
    public final AreaFile area;
    public final List<RuneFile> runes;
    public final List<ModifierFile> modifiers;
    public final int healthCost;

    public SpellBlueprint(String id, AreaFile area, List<RuneFile> runes,
                          List<ModifierFile> modifiers, int healthCost) {
        this.id = id;
        this.area = area;
        this.runes = runes;
        this.modifiers = modifiers;
        this.healthCost = healthCost;
    }
}
