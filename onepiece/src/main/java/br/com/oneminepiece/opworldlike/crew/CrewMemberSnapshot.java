package br.com.oneminepiece.opworldlike.crew;

import java.util.UUID;

public record CrewMemberSnapshot(UUID uuid, String username, boolean captain) {
}
