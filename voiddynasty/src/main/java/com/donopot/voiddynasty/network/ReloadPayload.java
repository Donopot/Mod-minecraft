package com.donopot.voiddynasty.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

/**
 * Paquet C2S envoyé par le client quand le joueur presse R (recharger).
 * Ne transporte aucune donnée — sa présence déclenche le rechargement côté serveur.
 */
public record ReloadPayload() implements CustomPacketPayload {

    public static final Type<ReloadPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("voiddynasty", "reload"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ReloadPayload> STREAM_CODEC =
            StreamCodec.unit(new ReloadPayload());

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
