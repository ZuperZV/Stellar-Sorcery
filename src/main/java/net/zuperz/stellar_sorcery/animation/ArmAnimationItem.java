package net.zuperz.stellar_sorcery.animation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public interface ArmAnimationItem {
    @Nullable
    ResourceLocation getArmAnimationId(ItemStack stack);
}
