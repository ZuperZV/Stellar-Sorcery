package net.zuperz.stellar_sorcery.data.spell;

import java.util.List;
import java.util.Map;

public class SpellModifierEngine {

    public static void applyModifiers(
            SpellBlueprint spell,
            List<RuneFile> runes,
            List<ModifierFile> modifiers
    ) {

        for (ModifierFile mod : modifiers) {

            for (Map.Entry<String, Object> e : mod.effects.entrySet()) {
                String key = e.getKey();
                Object val = e.getValue();

                switch (key) {

                    case "damage_multiplier" -> {
                        double mul = ((Number) val).doubleValue();
                        for (RuneFile rune : runes) {
                            if (rune.effect_type.equals("damage")) {
                                rune.amount *= mul;
                            }
                        }
                    }

                    case "self_damage_multiplier" -> {
                        double mul = ((Number) val).doubleValue();
                        for (RuneFile rune : runes) {
                            if (rune.effect_type.equals("self_damage")) {
                                rune.amount *= mul;
                            }
                        }
                    }

                    case "radius_boost" -> {
                        if (spell.area.radius > 0) {
                            spell.area.radius += ((Number) val).floatValue();
                        }
                    }

                    case "projectile_speed_up" -> {
                        //if (spell.area.projectile_speed > 0) {
                        //    spell.area.projectile_speed += ((Number) val).floatValue();
                        //}
                    }

                    case "add_status_effect" -> {
                        Map<String, Object> effectMap = (Map<String, Object>) val;

                        RuneFile added = new RuneFile();
                        added.id = "extra_status_from_modifier";
                        added.effect_type = "status_effect";
                        added.element = (String) effectMap.get("type");
                        added.duration = ((Number) effectMap.get("duration")).intValue();
                        added.amount = 0;

                        runes.add(added);
                    }
                }
            }
        }
    }
}
