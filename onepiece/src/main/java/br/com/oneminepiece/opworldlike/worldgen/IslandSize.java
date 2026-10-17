package br.com.oneminepiece.opworldlike.worldgen;

public enum IslandSize {
   TINY(140, 260, 14),
   SMALL(320, 460, 26),
   MEDIUM(520, 720, 42),
   LARGE(820, 1050, 62),
   HUGE(1150, 1450, 86);

   public final int minRadius;
   public final int maxRadius;
   public final int heightBoost;

   IslandSize(int minRadius, int maxRadius, int heightBoost) {
      this.minRadius = minRadius;
      this.maxRadius = maxRadius;
      this.heightBoost = heightBoost;
   }
}
