package com.donopot.voiddynastybridge.refit;

public record PadLayout(int left, int width, int rows, int footer, int contentBottom) {
   public static PadLayout of(int var0, int var1) {
      int var2 = Math.max(280, Math.min(540, var0 - 24));
      return new PadLayout((var0 - var2) / 2, var2, Math.max(1, (var1 - 142) / 22), var1 - 54, var1 - 62);
   }

   public int columnWidth() {
      return (this.width - 12) / 2;
   }

   public static boolean name(String var0) {
      return var0 != null && var0.matches("[\\p{L}\\p{N}_-]{1,32}");
   }
}
