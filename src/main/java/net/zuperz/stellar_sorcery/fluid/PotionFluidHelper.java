package net.zuperz.stellar_sorcery.fluid;

import net.minecraft.world.item.alchemy.PotionContents;
import net.neoforged.neoforge.fluids.FluidStack;
import net.zuperz.stellar_sorcery.component.ModDataComponentTypes;

public class PotionFluidHelper {

    public static final int MB_PER_POTION = 250;

    public static FluidStack toFluid(PotionContents contents, int amount) {
        FluidStack stack = new FluidStack(ModFluids.SOURCE_POTION.get(), amount);
        stack.set(ModDataComponentTypes.POTION_CONTENTS, contents);
        return stack;
    }

    public static PotionContents fromFluid(FluidStack stack) {
        return stack.getOrDefault(ModDataComponentTypes.POTION_CONTENTS, PotionContents.EMPTY);
    }
}