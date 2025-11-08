package net.zuperz.stellar_sorcery.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class SigilData {

    private final List<ItemStack> sigils;

    private final int capacity;

    public SigilData(List<ItemStack> sigils, int capacity) {
        this.sigils = new ArrayList<>(sigils);
        this.capacity = capacity;
    }

    public List<ItemStack> getSigils() {
        return Collections.unmodifiableList(sigils);
    }

    public int getCapacity() {
        return capacity;
    }

    public boolean addSigil(ItemStack stack) {
        if (sigils.size() < capacity) {
            sigils.add(stack.copy());
            return true;
        }
        return false;
    }

    public ItemStack removeSigil(int index) {
        if (index >= 0 && index < sigils.size()) {
            return sigils.remove(index);
        }
        return ItemStack.EMPTY;
    }

    public boolean containsSigil(Item item) {
        return sigils.stream().anyMatch(s -> s.is(item));
    }

    public void clear() {
        sigils.clear();
    }

    public int getCount() {
        return sigils.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SigilData that)) return false;
        return capacity == that.capacity &&
                ItemStack.listMatches(this.sigils, that.sigils);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sigils, capacity);
    }

    public static SigilData fromIds(List<String> ids, int capacity) {
        List<ItemStack> stacks = new ArrayList<>();
        for (String idStr : ids) {
            ResourceLocation id = ResourceLocation.tryParse(idStr);
            if (id == null) continue;
            Item item = BuiltInRegistries.ITEM.get(id);
            stacks.add(new ItemStack(item));
        }
        return new SigilData(stacks, capacity);
    }

    public static SigilData from(ItemStack stack) {
        return stack.get(ModDataComponentTypes.SIGIL);
    }

    public static final Codec<SigilData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            ItemStack.CODEC.listOf().optionalFieldOf("sigils", Collections.emptyList()).forGetter(SigilData::getSigils),
            Codec.INT.optionalFieldOf("capacity", 3).forGetter(SigilData::getCapacity)
    ).apply(instance, SigilData::new));
}