package net.zuperz.stellar_sorcery.util;

import net.minecraft.util.StringRepresentable;

public enum SunflowerEnum implements StringRepresentable {
    TOP("top"),
    BOTTOM("bottom"),
    GROUND_BOTTOM("ground_bottom"),;

    private final String name;

    SunflowerEnum(String name) {
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
