package com.donopot.voiddynasty.network;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;

import java.util.ArrayList;
import java.util.List;

/**
 * Paquet S2C : le serveur envoie la liste des vaisseaux du joueur au pad.
 */
public record FleetListPayload(List<ShipInfo> ships) implements CustomPacketPayload {

    public static final Type<FleetListPayload> TYPE =
            new Type<>(Identifier.fromNamespaceAndPath("voiddynasty", "fleet_list"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FleetListPayload> STREAM_CODEC =
            StreamCodec.of(
                    (buf, p) -> {
                        buf.writeVarInt(p.ships().size());
                        for (ShipInfo s : p.ships()) {
                            buf.writeUtf(s.name());
                            buf.writeUtf(s.typeDisplay());
                            buf.writeBoolean(s.deployed());
                            buf.writeVarInt(s.x());
                            buf.writeVarInt(s.y());
                            buf.writeVarInt(s.z());
                        }
                    },
                    buf -> {
                        int n = buf.readVarInt();
                        List<ShipInfo> ships = new ArrayList<>();
                        for (int i = 0; i < n; i++) {
                            ships.add(new ShipInfo(buf.readUtf(), buf.readUtf(), buf.readBoolean(),
                                    buf.readVarInt(), buf.readVarInt(), buf.readVarInt()));
                        }
                        return new FleetListPayload(ships);
                    }
            );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
