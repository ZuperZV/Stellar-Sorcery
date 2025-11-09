package net.zuperz.stellar_sorcery.component;

import com.mojang.serialization.Codec;

public record SigilNameData(String name) {
    public static final Codec<SigilNameData> CODEC = Codec.STRING
            .xmap(SigilNameData::new, SigilNameData::name);

    public static final SigilNameData DEFAULT = new SigilNameData("swiftness");
}
