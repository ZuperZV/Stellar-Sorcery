package net.zuperz.stellar_sorcery.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

public class CodexTierData {

    private int tier;

    public CodexTierData(int tier) {
        this.tier = tier;
    }

    public void setTier(int tier) {
        this.tier = tier;
    }

    public int getTier() {
        return tier;
    }

    public void decrease(int amount) {
        this.tier = Math.max(0, this.tier - amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.tier);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof CodexTierData ex) {
            return this.tier == ex.tier;
        }
        return false;
    }

    public static final Codec<CodexTierData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("tier").forGetter(CodexTierData::getTier)
            ).apply(instance, CodexTierData::new)
    );
}