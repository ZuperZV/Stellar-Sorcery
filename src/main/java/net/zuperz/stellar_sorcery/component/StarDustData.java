package net.zuperz.stellar_sorcery.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

public class StarDustData {

    private int value1;

    public StarDustData(int value1) {
        this.value1 = value1;
    }

    public int getValue1() {
        return value1;
    }

    public void decrease(int amount) {
        this.value1 = Math.max(0, this.value1 - amount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else {
            return obj instanceof StarDustData ex
                    && this.value1 == ex.value1;
        }
    }

    public static final Codec<StarDustData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("value1").forGetter(StarDustData::getValue1)
            ).apply(instance, StarDustData::new)
    );
}
