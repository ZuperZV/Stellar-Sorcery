package net.zuperz.stellar_sorcery.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
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

    public static EssenceBottleData fromIds(String[] ids) {
        if (ids.length != 3) {
            throw new IllegalArgumentException("EssenceBottleData requires exactly 3 ids, got " + ids.length);
        }

        var id0 = ResourceLocation.tryParse(ids[0]);
        var id1 = ResourceLocation.tryParse(ids[1]);
        var id2 = ResourceLocation.tryParse(ids[2]);

        if (id0 == null || id1 == null || id2 == null) {
            throw new IllegalArgumentException("One of the EssenceBottleData ids could not be parsed: " + String.join(",", ids));
        }

        ItemStack stack0 = new ItemStack(BuiltInRegistries.ITEM.get(id0));
        ItemStack stack1 = new ItemStack(BuiltInRegistries.ITEM.get(id1));
        ItemStack stack2 = new ItemStack(BuiltInRegistries.ITEM.get(id2));

        return new EssenceBottleData(stack0, stack1, stack2);
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