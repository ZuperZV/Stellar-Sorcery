package net.zuperz.stellar_sorcery.data.spell;

import java.util.ArrayList;
import java.util.List;

public class SpellResolver {

    public static SpellBlueprint resolve(SpellFile file) {

        var area = SpellRegistry.getArea(file.area);

        List<RuneFile> runes = new ArrayList<>();
        for (String id : file.runes) {
            var r = SpellRegistry.getRune(id);
            if (r != null) runes.add(r);
        }

        List<ModifierFile> modifiers = new ArrayList<>();
        for (String id : file.modifiers) {
            var m = SpellRegistry.getModifier(id);
            if (m != null) modifiers.add(m);
        }

        return new SpellBlueprint(
                file.id,
                area,
                runes,
                modifiers,
                file.health_cost
        );
    }
}
