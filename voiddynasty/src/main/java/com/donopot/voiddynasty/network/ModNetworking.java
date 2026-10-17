package com.donopot.voiddynasty.network;

import com.donopot.voiddynasty.FleetManager;
import com.donopot.voiddynasty.ShipData;
import com.donopot.voiddynasty.menu.SuperChestMenu;
import com.donopot.voiddynasty.weapon.WeaponService;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

/**
 * Enregistrement des paquets C2S (rechargement, scroll des super conteneurs, requête de flotte).
 */
public final class ModNetworking {

    private ModNetworking() {}

    public static void init() {
        PayloadTypeRegistry.serverboundPlay().register(ReloadPayload.TYPE, ReloadPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ReloadPayload.TYPE,
                (payload, context) -> WeaponService.startReload(context.player()));

        PayloadTypeRegistry.serverboundPlay().register(ScrollContainerPayload.TYPE, ScrollContainerPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(ScrollContainerPayload.TYPE,
                (payload, context) -> {
                    if (context.player().containerMenu instanceof SuperChestMenu menu
                            && menu.containerId == payload.syncId()) {
                        menu.setScrollRow(payload.scrollRow());
                        menu.broadcastChanges();
                    }
                });

        // Flotte : le pad demande la liste de ses vaisseaux
        PayloadTypeRegistry.clientboundPlay().register(FleetListPayload.TYPE, FleetListPayload.STREAM_CODEC);
        PayloadTypeRegistry.serverboundPlay().register(FleetListRequestPayload.TYPE, FleetListRequestPayload.STREAM_CODEC);
        ServerPlayNetworking.registerGlobalReceiver(FleetListRequestPayload.TYPE,
                (payload, context) -> {
                    ServerPlayer player = context.player();
                    List<ShipData> ships = FleetManager.list(player);
                    List<ShipInfo> infos = new ArrayList<>();
                    for (ShipData s : ships) {
                        int x = s.base != null ? s.base.getX() : 0;
                        int y = s.base != null ? s.base.getY() : 0;
                        int z = s.base != null ? s.base.getZ() : 0;
                        infos.add(new ShipInfo(s.name, s.type.displayName, s.isDeployed(), x, y, z));
                    }
                    ServerPlayNetworking.send(player, new FleetListPayload(infos));
                });
    }
}
