package com.donopot.voiddynasty;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Flotte persistante, par monde : Map<joueur (UUID), liste de vaisseaux>.
 * Survit au relog / redémarrage via SavedData.
 */
public final class FleetState extends SavedData {

    public static final int MAX_SHIPS_PER_PLAYER = 10;   // doit rester aligné sur FleetManager

    public final Map<UUID, List<ShipData>> fleets = new HashMap<>();

    public static final Codec<FleetState> CODEC =
            CompoundTag.CODEC.xmap(FleetState::fromTag, FleetState::toTag);

    public static final SavedDataType<FleetState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("voiddynasty", "fleet_state"),
            FleetState::new,
            CODEC,
            DataFixTypes.SAVED_DATA_FORCED_CHUNKS);

    public static FleetState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    public List<ShipData> shipsOf(UUID owner) {
        return fleets.computeIfAbsent(owner, k -> new ArrayList<>());
    }

    private static CompoundTag toTag(FleetState s) {
        CompoundTag root = new CompoundTag();
        ListTag fleetsTag = new ListTag();
        for (Map.Entry<UUID, List<ShipData>> e : s.fleets.entrySet()) {
            CompoundTag fleetTag = new CompoundTag();
            fleetTag.putLong("ownerMost", e.getKey().getMostSignificantBits());
            fleetTag.putLong("ownerLeast", e.getKey().getLeastSignificantBits());
            ListTag shipsTag = new ListTag();
            for (ShipData sh : e.getValue()) {
                CompoundTag shipTag = new CompoundTag();
                shipTag.putString("name", sh.name);
                shipTag.putString("type", sh.type.name());
                if (sh.base != null) shipTag.putLong("base", sh.base.asLong());
                if (sh.storedBytes != null) shipTag.putByteArray("saved", sh.storedBytes);
                shipsTag.add(shipsTag.size(), shipTag);
            }
            fleetTag.put("ships", shipsTag);
            fleetsTag.add(fleetsTag.size(), fleetTag);
        }
        root.put("fleets", fleetsTag);
        return root;
    }

    private static FleetState fromTag(CompoundTag root) {
        FleetState s = new FleetState();
        ListTag fleetsTag = root.getListOrEmpty("fleets");
        for (int i = 0; i < fleetsTag.size(); i++) {
            CompoundTag fleetTag = fleetsTag.getCompoundOrEmpty(i);
            UUID owner = new UUID(fleetTag.getLongOr("ownerMost", 0L), fleetTag.getLongOr("ownerLeast", 0L));
            List<ShipData> ships = new ArrayList<>();
            ListTag shipsTag = fleetTag.getListOrEmpty("ships");
            for (int j = 0; j < shipsTag.size(); j++) {
                CompoundTag shipTag = shipsTag.getCompoundOrEmpty(j);
                String name = shipTag.getStringOr("name", "");
                String typeStr = shipTag.getStringOr("type", ShipType.EMPEROR.name());
                ShipType type;
                try {
                    type = ShipType.valueOf(typeStr);
                } catch (IllegalArgumentException ex) {
                    type = ShipType.EMPEROR;
                }
                ShipData sh = new ShipData(name, type);
                sh.base = shipTag.getLong("base").map(BlockPos::of).orElse(null);
                sh.storedBytes = shipTag.getByteArray("saved").orElse(null);
                ships.add(sh);
            }
            s.fleets.put(owner, ships);
        }
        return s;
    }
}
