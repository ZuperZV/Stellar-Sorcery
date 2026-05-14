package net.zuperz.stellar_sorcery.particle;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public class ColorBubbleType extends ParticleType<ColorBubbleData> {

    public ColorBubbleType() {
        super(false);
    }

    @Override
    public MapCodec<ColorBubbleData> codec() {
        return ColorBubbleData.CODEC;
    }

    @Override
    public StreamCodec<? super RegistryFriendlyByteBuf, ColorBubbleData> streamCodec() {
        return ColorBubbleData.STREAM_CODEC;
    }
}