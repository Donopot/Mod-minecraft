package br.com.oneminepiece.opworldlike.worldgen;

import java.util.List;
import net.minecraft.resources.Identifier;

public record OmpStructureDefinition(
   String id, OmpStructureKind kind, int centerX, int centerZ, int halfX, int halfZ, Identifier lootTable, List<Identifier> mobSpawns
) {
   public boolean intersects(int minX, int minZ, int maxX, int maxZ) {
      return this.centerX + this.halfX >= minX && this.centerX - this.halfX <= maxX && this.centerZ + this.halfZ >= minZ && this.centerZ - this.halfZ <= maxZ;
   }
}
