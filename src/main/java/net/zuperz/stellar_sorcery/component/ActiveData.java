package net.zuperz.stellar_sorcery.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.Objects;

public class ActiveData {

    private boolean active;

    public ActiveData(boolean active) {
        this.active = active;
    }

    public ActiveData() {
        this(false);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean getActive() {
        return active;
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.active);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ActiveData other) {
            return this.active == other.active;
        }
        return false;
    }

    public static final Codec<ActiveData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.BOOL.fieldOf("active").forGetter(ActiveData::getActive)
            ).apply(instance, ActiveData::new)
    );
}