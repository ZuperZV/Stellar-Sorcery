package net.zuperz.stellar_sorcery.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.zuperz.stellar_sorcery.StellarSorcery;
import net.zuperz.stellar_sorcery.client.animation.ArmAnimationController;

public record ArmAnimationPacket(
        int playerId,
        ResourceLocation animationId,
        boolean stop
) implements CustomPacketPayload {

    public static final Type<ArmAnimationPacket> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(StellarSorcery.MOD_ID, "arm_animation"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ArmAnimationPacket> STREAM_CODEC =
            StreamCodec.of(
                    (buf, pkt) -> {
                        buf.writeInt(pkt.playerId);
                        buf.writeResourceLocation(pkt.animationId);
                        buf.writeBoolean(pkt.stop);
                    },
                    buf -> new ArmAnimationPacket(
                            buf.readInt(),
                            buf.readResourceLocation(),
                            buf.readBoolean()
                    )
            );

    @Override
    public Type<ArmAnimationPacket> type() { return TYPE; }

    public static void handle(ArmAnimationPacket msg, IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            if (msg.stop) {
                ArmAnimationController.stop(msg.playerId);
            } else {
                ArmAnimationController.play(msg.playerId, msg.animationId);
                System.out.println("Test hmm ArmAnimationController play: " + msg.playerId + " " + msg.animationId);
            }
        });
    }
}
