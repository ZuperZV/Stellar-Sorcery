package net.zuperz.stellar_sorcery.api.jei.subtypeInterpreter;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.zuperz.stellar_sorcery.component.EssenceBottleData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;
import org.jetbrains.annotations.Nullable;

public class EssenceBottleSubtypeInterpreter implements ISubtypeInterpreter {

    @Override
    public Object getSubtypeData(Object ingredient, UidContext context) {
        if (!(ingredient instanceof ItemStack stack)) {
            return null;
        }

        EssenceBottleData data = stack.get(ModDataComponentTypes.ESSENCE_BOTTLE);
        if (data == null) {
            return null;
        }

        String id0 = idOrEmpty(data.getEmbeddedItem());
        String id1 = idOrEmpty(data.getEmbeddedItem1());
        String id2 = idOrEmpty(data.getEmbeddedItem2());

        return id0 + "|" + id1 + "|" + id2;
    }

    @Override
    public String getLegacyStringSubtypeInfo(Object ingredient, UidContext context) {
        Object subtype = getSubtypeData(ingredient, context);
        return subtype == null ? "" : subtype.toString();
    }

    private String idOrEmpty(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return "empty";
        }
        ResourceLocation id = stack.getItem().builtInRegistryHolder().key().location();
        return id.toString();
    }
}