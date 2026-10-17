package br.com.oneminepiece.opworldlike.crew;

import java.util.List;

public record CrewSnapshot(String name, long creationDate, List<CrewMemberSnapshot> members) {
   public CrewSnapshot {
      members = members == null ? List.of() : List.copyOf(members);
   }
}
