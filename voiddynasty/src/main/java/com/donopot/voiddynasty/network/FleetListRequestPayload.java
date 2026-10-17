package com.donopot.voiddynasty.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Paquet C2S : le client (pad) demande la liste de sa flotte au serveur.
 * Le serveur répond par un FleetListPayload.
 */
public record FleetListRequestPayload() implements CustomPacketPayload {

    public static final Type<FleetListRequestPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("voiddynasty", "fleet_list_request"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FleetListRequestPayload> STREAM_CODEC =
            StreamCodec.unit(new FleetListRequestPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
