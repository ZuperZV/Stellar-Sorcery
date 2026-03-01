package net.zuperz.stellar_sorcery.mixin;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.zuperz.stellar_sorcery.screen.Helpers.IExtraSlotsProvider;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerExtraSlotsMixin implements IExtraSlotsProvider {

    private Container extraSlots;

    @Override
    public Container getExtraSlots() {
        if (extraSlots == null) {
            extraSlots = new SimpleContainer(2);
        }
        return extraSlots;
    }
}