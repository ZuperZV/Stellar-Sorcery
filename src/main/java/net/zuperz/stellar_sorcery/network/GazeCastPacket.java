package net.zuperz.stellar_sorcery.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.client.gaze.GazeClientEffects;

public record GazeCastPacket(
        int playerId,
        String handAnimation,
        int animationDuration,
        boolean hasEffect,
        String effectType,
        String effectColor,
        String effectParticle,
        int effectDuration
) implements CustomPacketPayload {
    public static final Type<GazeCastPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "gaze_cast"));

    public static final StreamCodec<RegistryFriendlyByteBuf, GazeCastPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeInt(pkt.playerId);
                        buf.writeUtf(pkt.handAnimation);
                        buf.writeInt(pkt.animationDuration);
                        buf.writeBoolean(pkt.hasEffect);
                        if (pkt.hasEffect) {
                            buf.writeUtf(pkt.effectType);
                            buf.writeUtf(pkt.effectColor);
                            buf.writeUtf(pkt.effectParticle);
                            buf.writeInt(pkt.effectDuration);
                        }
                    },
                    buf -> {
                        int playerId = buf.readInt();
                        String handAnimation = buf.readUtf();
                        int animationDuration = buf.readInt();
                        boolean hasEffect = buf.readBoolean();
                        String effectType = "";
                        String effectColor = "";
                        String effectParticle = "";
                        int effectDuration = 0;
                        if (hasEffect) {
                            effectType = buf.readUtf();
                            effectColor = buf.readUtf();
                            effectParticle = buf.readUtf();
                            effectDuration = buf.readInt();
                        }
                        return new GazeCastPacket(
                                playerId,
                                handAnimation,
                                animationDuration,
                                hasEffect,
                                effectType,
                                effectColor,
                                effectParticle,
                                effectDuration
                        );
                    }
            );

    @Override
    public Type<GazeCastPacket> type() { return TYPE; }

    public static void handle(GazeCastPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> GazeClientEffects.trigger(
                msg.playerId,
                msg.handAnimation,
                msg.animationDuration,
                msg.hasEffect,
                msg.effectType,
                msg.effectColor,
                msg.effectParticle,
                msg.effectDuration
        ));
    }
}
