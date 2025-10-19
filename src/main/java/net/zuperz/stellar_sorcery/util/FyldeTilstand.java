package net.zuperz.stellar_sorcery.util;

import net.minecraft.util.StringRepresentable;

public enum FyldeTilstand implements StringRepresentable {
    FULL("full"),
    HALF("half"),
    EMPTY("empty");

    private final String name;

    FyldeTilstand(String name) {
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
