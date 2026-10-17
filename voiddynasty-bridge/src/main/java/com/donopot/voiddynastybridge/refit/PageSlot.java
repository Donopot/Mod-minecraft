package com.donopot.voiddynastybridge.refit;

import net.minecraft.world.inventory.Slot;

public final class PageSlot extends Slot {
   private final PagedContainer page;
   private final int logical;

   public PageSlot(PagedContainer var1, int var2, int var3, int var4) {
      super(var1, var2, var3, var4);
      this.page = var1;
      this.logical = var2;
   }

   public boolean isActive() {
      return this.page.exists(this.logical);
   }
}
