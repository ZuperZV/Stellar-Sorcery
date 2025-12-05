package net.zuperz.stellar_sorcery.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Objects;

public class SpellData {

    private final String area;
    private final List<String> runes;
    private final List<String> modifiers;
    private final int healthCost;

    public SpellData(String area, List<String> runes, List<String> modifiers, int healthCost) {
        this.area = area;
        this.runes = List.copyOf(runes);
        this.modifiers = List.copyOf(modifiers);
        this.healthCost = healthCost;
    }

    public String getArea() { return area; }
    public List<String> getRunes() { return runes; }
    public List<String> getModifiers() { return modifiers; }
    public int getHealthCost() { return healthCost; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SpellData)) return false;
        SpellData that = (SpellData) o;
        return healthCost == that.healthCost &&
                Objects.equals(area, that.area) &&
                Objects.equals(runes, that.runes) &&
                Objects.equals(modifiers, that.modifiers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(area, runes, modifiers, healthCost);
    }

    public static final Codec<SpellData> CODEC =
            RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("area").forGetter(SpellData::getArea),
                    Codec.STRING.listOf().fieldOf("runes").forGetter(SpellData::getRunes),
                    Codec.STRING.listOf().fieldOf("modifiers").forGetter(SpellData::getModifiers),
                    Codec.INT.fieldOf("health_cost").forGetter(SpellData::getHealthCost)
            ).apply(instance, SpellData::new));

    public static SpellData from(ItemStack stack) {
        return stack.get(ModDataComponentTypes.SPELL_DATA);
    }
}
