package br.com.oneminepiece.opworldlike.integration;

import java.util.UUID;

public record CrewInfo(String name, UUID captain, int members) {
   public CrewInfo(String name, UUID captain) {
      this(name, captain, 0);
   }

   public String key() {
      return this.name == null ? "" : this.name.toLowerCase().replace(' ', '_');
   }
}
