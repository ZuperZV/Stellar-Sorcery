package net.zuperz.stellar_sorcery.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public class CelestialData {
    private final ItemStack embeddedItem;

    public CelestialData(ItemStack embeddedItem) {
        this.embeddedItem = embeddedItem;
    }

    public ItemStack getEmbeddedItem() {
        return embeddedItem;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CelestialData that)) return false;
        return ItemStack.isSameItemSameComponents(this.embeddedItem, that.embeddedItem);
    }

    @Override
    public int hashCode() {
        return Objects.hash(embeddedItem);
    }

    public static final Codec<CelestialData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.fieldOf("embedded_item").forGetter(CelestialData::getEmbeddedItem)
    ).apply(instance, CelestialData::new));
}