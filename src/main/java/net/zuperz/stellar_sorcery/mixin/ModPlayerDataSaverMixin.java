package net.zuperz.stellar_sorcery.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.zuperz.stellar_sorcery.data.IModPlayerData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;

@Mixin(ServerPlayer.class)
public abstract class ModPlayerDataSaverMixin implements IModPlayerData {
    private ArrayList<String> stellarSorceryBookmarks = new ArrayList<>();

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void injectSave(CompoundTag tag, CallbackInfo ci) {
        if (stellarSorceryBookmarks.isEmpty()) return;

        CompoundTag stellar = new CompoundTag();
        for (int i = 0; i < stellarSorceryBookmarks.size(); i++) {
            stellar.putString("bookmark_" + i, stellarSorceryBookmarks.get(i));
        }
        stellar.putInt("bookmark_count", stellarSorceryBookmarks.size());
        tag.put("stellar_sorcery", stellar);
    }

    @Inject(method = "readAdditionalSaveData", at = @At("HEAD"))
    private void injectLoad(CompoundTag tag, CallbackInfo ci) {
        stellarSorceryBookmarks.clear();

        if (tag.contains("stellar_sorcery")) {
            CompoundTag stellar = tag.getCompound("stellar_sorcery");
            int count = stellar.getInt("bookmark_count");
            for (int i = 0; i < count; i++) {
                stellarSorceryBookmarks.add(stellar.getString("bookmark_" + i));
            }
        }
    }

    @Override
    public ArrayList<String> stellarSorceryGetBookmarks() {
        return stellarSorceryBookmarks;
    }

    @Override
    public void stellarSorcerySetBookmarks(ArrayList<String> list) {
        this.stellarSorceryBookmarks = list;
    }
}