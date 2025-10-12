package net.zuperz.stellar_sorcery.api.jei.subtypeInterpreter;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.zuperz.stellar_sorcery.component.CodexTierData;
import net.zuperz.stellar_sorcery.component.EssenceBottleData;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;

public class CodexArcanumSubtypeInterpreter implements ISubtypeInterpreter {

    @Override
    public Object getSubtypeData(Object ingredient, UidContext context) {
        if (!(ingredient instanceof ItemStack stack)) {
            return null;
        }

        CodexTierData data = stack.get(ModDataComponentTypes.CODEX_TIER);
        if (data == null) {
            return null;
        }

        return "tier:" + data.getTier();
    }

    @Override
    public String getLegacyStringSubtypeInfo(Object ingredient, UidContext context) {
        Object subtype = getSubtypeData(ingredient, context);
        return subtype == null ? "" : subtype.toString();
    }
}