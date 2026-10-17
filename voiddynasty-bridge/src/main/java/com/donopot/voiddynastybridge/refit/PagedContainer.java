package com.donopot.voiddynastybridge.refit;

import net.minecraft.world.Container;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class PagedContainer implements Container {
   public static final int WINDOW = 54;
   public static final int COLUMNS = 9;
   private final Container backing;
   private final boolean client;
   private int capacity;
   private int row;

   public PagedContainer(Container var1, boolean var2) {
      this.backing = var1;
      this.client = var2;
      this.capacity = var1.getContainerSize();
   }

   public int capacity() {
      return this.capacity;
   }

   public int row() {
      return this.row;
   }

   public static int maxRow(int var0) {
      return (int)Math.max(0L, ((long)var0 + 8L) / 9L - 6L);
   }

   public int maxRow() {
      return maxRow(this.capacity);
   }

   public void row(int var1) {
      this.row = Math.clamp((long)var1, 0, this.maxRow());
   }

   public int word(int var1) {
      return switch (var1) {
         case 0 -> this.capacity & 65535;
         case 1 -> this.capacity >>> 16;
         case 2 -> this.row & 65535;
         case 3 -> this.row >>> 16;
         default -> throw new IllegalArgumentException();
      };
   }

   public void word(int var1, int var2) {
      if (this.client) {
         int var3 = var2 & 65535;
         switch (var1) {
            case 0:
               this.capacity = this.capacity & -65536 | var3;
               break;
            case 1:
               this.capacity = this.capacity & 65535 | var3 << 16;
               break;
            case 2:
               this.row = this.row & -65536 | var3;
               break;
            case 3:
               this.row = this.row & 65535 | var3 << 16;
               break;
            default:
               throw new IllegalArgumentException();
         }

         this.capacity = Math.max(0, this.capacity);
         this.row = Math.max(0, this.row);
      }
   }

   public boolean exists(int var1) {
      return var1 >= 0 && var1 < 54 && (long)this.row * 9L + (long)var1 < (long)this.capacity;
   }

   private int index(int var1) {
      return this.client ? var1 : this.row * 9 + var1;
   }

   public int getContainerSize() {
      return 54;
   }

   public boolean isEmpty() {
      for (int var1 = 0; var1 < 54; var1++) {
         if (!this.getItem(var1).isEmpty()) {
            return false;
         }
      }

      return true;
   }

   public ItemStack getItem(int var1) {
      return this.exists(var1) ? this.backing.getItem(this.index(var1)) : ItemStack.EMPTY;
   }

   public ItemStack removeItem(int var1, int var2) {
      if (!this.exists(var1)) {
         return ItemStack.EMPTY;
      } else {
         ItemStack var3 = this.backing.removeItem(this.index(var1), var2);
         this.setChanged();
         return var3;
      }
   }

   public ItemStack removeItemNoUpdate(int var1) {
      return this.exists(var1) ? this.backing.removeItemNoUpdate(this.index(var1)) : ItemStack.EMPTY;
   }

   public void setItem(int var1, ItemStack var2) {
      if (this.exists(var1)) {
         this.backing.setItem(this.index(var1), var2);
         this.setChanged();
      }
   }

   public void setChanged() {
      this.backing.setChanged();
   }

   public boolean stillValid(Player var1) {
      return this.backing.stillValid(var1);
   }

   public int getMaxStackSize() {
      return this.backing.getMaxStackSize();
   }

   public boolean canPlaceItem(int var1, ItemStack var2) {
      return this.exists(var1) && this.backing.canPlaceItem(this.index(var1), var2);
   }

   public boolean canTakeItem(Container var1, int var2, ItemStack var3) {
      return this.exists(var2) && this.backing.canTakeItem(var1, this.index(var2), var3);
   }

   public void startOpen(ContainerUser var1) {
      this.backing.startOpen(var1);
   }

   public void stopOpen(ContainerUser var1) {
      this.backing.stopOpen(var1);
   }

   public void clearContent() {
      for (int var1 = 0; var1 < 54; var1++) {
         if (this.exists(var1)) {
            this.backing.setItem(this.index(var1), ItemStack.EMPTY);
         }
      }

      this.setChanged();
   }
}
