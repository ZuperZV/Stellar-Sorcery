package net.zuperz.stellar_sorcery.util;

import net.minecraft.util.StringRepresentable;

public enum FyldeEnum implements StringRepresentable {
    FULL("full"),
    HALF("half"),
    EMPTY("empty");

    private final String name;

    FyldeEnum(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
