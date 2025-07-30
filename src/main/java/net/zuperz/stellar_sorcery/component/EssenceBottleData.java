package net.zuperz.stellar_sorcery.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;
import java.util.Optional;

public class EssenceBottleData {
    private final ItemStack embeddedItem;
    private final ItemStack embeddedItem1;
    private final ItemStack embeddedItem2;

    public EssenceBottleData(ItemStack embeddedItem, ItemStack embeddedItem1, ItemStack embeddedItem2) {
        this.embeddedItem = embeddedItem;
        this.embeddedItem1 = embeddedItem1;
        this.embeddedItem2 = embeddedItem2;
    }

    public ItemStack getEmbeddedItem() {
        return embeddedItem;
    }
    public ItemStack getEmbeddedItem1() {
        return embeddedItem1;
    }
    public ItemStack getEmbeddedItem2() {
        return embeddedItem2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EssenceBottleData that)) return false;
        boolean test = true;
        if (!ItemStack.isSameItemSameComponents(this.embeddedItem, that.embeddedItem)) {
            test = false;
        } else if (!ItemStack.isSameItemSameComponents(this.embeddedItem1, that.embeddedItem1)) {
            test = false;
        } else if (!ItemStack.isSameItemSameComponents(this.embeddedItem2, that.embeddedItem2)) {
            test = false;
        }
        return test;
    }

    @Override
    public int hashCode() {
        return Objects.hash(embeddedItem, embeddedItem1, embeddedItem2);
    }

    public static EssenceBottleData from(ItemStack stack) {
        return stack.get(ModDataComponentTypes.ESSENCE_BOTTLE);
    }


    public static final Codec<EssenceBottleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.optionalFieldOf("embedded_item").forGetter(d -> Optional.ofNullable(d.getEmbeddedItem())),
            ItemStack.CODEC.optionalFieldOf("embedded_item1").forGetter(d -> Optional.ofNullable(d.getEmbeddedItem1())),
            ItemStack.CODEC.optionalFieldOf("embedded_item2").forGetter(d -> Optional.ofNullable(d.getEmbeddedItem2()))
    ).apply(instance, (opt1, opt2, opt3) ->
            new EssenceBottleData(
                    opt1.orElse(null),
                    opt2.orElse(null),
                    opt3.orElse(null)
            )
    ));
}