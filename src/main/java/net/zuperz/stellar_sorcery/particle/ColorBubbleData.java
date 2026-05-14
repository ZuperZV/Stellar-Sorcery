package net.zuperz.stellar_sorcery.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

public record ColorBubbleData(float red, float green, float blue) implements ParticleOptions {

    public static final MapCodec<ColorBubbleData> CODEC =
            RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("red").forGetter(ColorBubbleData::red),
                    Codec.FLOAT.fieldOf("green").forGetter(ColorBubbleData::green),
                    Codec.FLOAT.fieldOf("blue").forGetter(ColorBubbleData::blue)
            ).apply(instance, ColorBubbleData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, ColorBubbleData> STREAM_CODEC =
            StreamCodec.composite(
                    ByteBufCodecs.FLOAT, ColorBubbleData::red,
                    ByteBufCodecs.FLOAT, ColorBubbleData::green,
                    ByteBufCodecs.FLOAT, ColorBubbleData::blue,
                    ColorBubbleData::new
            );

    @Override
    public ParticleType<?> getType() {
        return ModParticleTypes.COLOR_BUBBLE.get();
    }
}