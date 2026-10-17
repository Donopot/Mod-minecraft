package com.donopot.voiddynastybridge.refit;

import net.minecraft.world.inventory.DataSlot;

public final class PageWord extends DataSlot {
   private final PagedContainer page;
   private final int part;

   public PageWord(PagedContainer var1, int var2) {
      this.page = var1;
      this.part = var2;
   }

   public int get() {
      return this.page.word(this.part);
   }

   public void set(int var1) {
      this.page.word(this.part, var1);
   }
}
