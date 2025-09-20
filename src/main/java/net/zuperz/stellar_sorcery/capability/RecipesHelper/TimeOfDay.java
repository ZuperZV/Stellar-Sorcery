package net.zuperz.stellar_sorcery.capability.RecipesHelper;

import com.mojang.serialization.Codec;

public enum TimeOfDay {
    DAY,
    NIGHT,
    BOTH;

    public static final Codec<TimeOfDay> CODEC = Codec.STRING.xmap(
            s -> TimeOfDay.valueOf(s.toUpperCase()),
            TimeOfDay::name
    );
}