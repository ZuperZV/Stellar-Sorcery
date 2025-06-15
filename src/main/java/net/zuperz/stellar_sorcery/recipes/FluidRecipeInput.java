package net.zuperz.stellar_sorcery.recipes;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;
import net.neoforged.neoforge.fluids.FluidStack;

public class FluidRecipeInput implements RecipeInput {
    private final FluidStack fluidType;
    private int amount;

    public FluidRecipeInput(FluidStack fluidType) {
        this.fluidType = fluidType;
        this.amount = amount;
    }

    @Override
    public ItemStack getItem(int p_346128_) {
        return null;
    }

    public FluidStack getFluidType() {
        return fluidType;
    }

    public int getAmount() {
        return amount;
    }

    @Override
    public int size() {
        return 0;
    }
}