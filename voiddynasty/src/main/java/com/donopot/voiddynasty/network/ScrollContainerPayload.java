package com.donopot.voiddynasty.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Paquet C2S : le client demande au serveur de faire défiler un menu scrollable
 * (super coffre / super conteneur).
 */
public record ScrollContainerPayload(int syncId, int scrollRow) implements CustomPacketPayload {

    public static final Type<ScrollContainerPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("voiddynasty", "scroll_container"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ScrollContainerPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, payload) -> {
                        buf.writeVarInt(payload.syncId());
                        buf.writeVarInt(payload.scrollRow());
                    },
                    buf -> new ScrollContainerPayload(buf.readVarInt(), buf.readVarInt())
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
