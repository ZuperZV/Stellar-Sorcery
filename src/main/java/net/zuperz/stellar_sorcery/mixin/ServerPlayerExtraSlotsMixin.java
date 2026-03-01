package net.zuperz.stellar_sorcery.mixin;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.zuperz.stellar_sorcery.screen.Helpers.IExtraSlotsProvider;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerExtraSlotsMixin implements IExtraSlotsProvider {

    private Container extraSlots;

    @Override
    public Container getExtraSlots() {
        if (extraSlots == null) {
            extraSlots = new SimpleContainer(2);
        }
        return extraSlots;
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void saveExtraSlots(CompoundTag tag, CallbackInfo ci) {
        Container inv = getExtraSlots();

        ServerPlayer self = (ServerPlayer)(Object)this;
        HolderLookup.Provider provider = self.level().registryAccess();

        CompoundTag extra = new CompoundTag();

        for (int i = 0; i < inv.getContainerSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (!stack.isEmpty()) {
                extra.put("slot_" + i, stack.saveOptional(provider));
            }
        }

        tag.put("extra_slots", extra);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void loadExtraSlots(CompoundTag tag, CallbackInfo ci) {
        if (!tag.contains("extra_slots")) return;

        Container inv = getExtraSlots();

        ServerPlayer self = (ServerPlayer)(Object)this;
        HolderLookup.Provider provider = self.level().registryAccess();

        CompoundTag extra = tag.getCompound("extra_slots");

        for (int i = 0; i < inv.getContainerSize(); i++) {
            if (extra.contains("slot_" + i)) {
                inv.setItem(i, ItemStack.parseOptional(provider, extra.getCompound("slot_" + i)));
            } else {
                inv.setItem(i, ItemStack.EMPTY);
            }
        }
    }
}