package com.donopot.voiddynasty;

import com.mojang.serialization.Codec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.saveddata.SavedDataType;

/**
 * État persistant de la mission, par monde.
 * - ruinPos : position de base de la ruine (null = pas encore placée).
 */
public final class MissionState extends SavedData {

    public BlockPos ruinPos;

    public static final Codec<MissionState> CODEC =
            CompoundTag.CODEC.xmap(MissionState::fromTag, MissionState::toTag);

    public static final SavedDataType<MissionState> TYPE = new SavedDataType<>(
            Identifier.fromNamespaceAndPath("voiddynasty", "mission_state"),
            MissionState::new,
            CODEC,
            DataFixTypes.SAVED_DATA_FORCED_CHUNKS);

    public static MissionState get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(TYPE);
    }

    private static CompoundTag toTag(MissionState s) {
        CompoundTag tag = new CompoundTag();
        if (s.ruinPos != null) tag.putLong("ruin", s.ruinPos.asLong());
        return tag;
    }

    private static MissionState fromTag(CompoundTag tag) {
        MissionState s = new MissionState();
        s.ruinPos = tag.getLong("ruin").map(BlockPos::of).orElse(null);
        return s;
    }

    /** Vrai si la position donnée est proche de la ruine (zone de mission). */
    public boolean isNear(BlockPos p) {
        if (ruinPos == null) return false;
        return Math.abs(p.getX() - ruinPos.getX()) <= 64 && Math.abs(p.getZ() - ruinPos.getZ()) <= 64;
    }

    public static Component missionMessage() {
        return Component.literal(
                        "MISSION — Le Monde Mortuaire : récupère le Fragment de champ de Gellar dans le coffre de l'autel de la ruine.")
                .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
    }
}
