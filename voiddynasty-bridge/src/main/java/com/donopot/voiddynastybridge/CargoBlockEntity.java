package com.donopot.voiddynastybridge;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public final class CargoBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
   private NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);
   public String world = "";
   public String crate = "";
   public String delivery = "";
   public String phase = "IDLE";
   public int reserved;
   public String reservedItem = "minecraft:iron_ingot";
   public String mission = "logistics_first_delivery";

   public CargoBlockEntity(BlockPos var1, BlockState var2) {
      super(BridgeMod.CARGO_ENTITY, var1, var2);
   }

   public int getContainerSize() {
      return 9;
   }

   protected NonNullList<ItemStack> getItems() {
      return this.items;
   }

   protected void setItems(NonNullList<ItemStack> var1) {
      this.items = var1;
   }

   protected Component getDefaultName() {
      return Component.literal("VOID DYNASTY | Depot de fer");
   }

   protected AbstractContainerMenu createMenu(int var1, Inventory var2) {
      return new ChestMenu(MenuType.GENERIC_9x1, var1, var2, this, 1);
   }

   public boolean locked() {
      return !this.phase.equals("IDLE") && !this.phase.equals("SETTLED")
         || BridgeMod.service == null
         || !BridgeMod.service.matches(this)
         || BridgeMod.service.locked();
   }

   public boolean canOpen(Player var1) {
      return !this.locked() && super.canOpen(var1);
   }

   public boolean stillValid(Player var1) {
      return !this.locked() && super.stillValid(var1);
   }

   public boolean canPlaceItem(int var1, ItemStack var2) {
      return !this.locked() && CargoCatalog.accepts(BuiltInRegistries.ITEM.getKey(var2.getItem()).toString());
   }

   public ItemStack removeItem(int var1, int var2) {
      return this.locked() ? ItemStack.EMPTY : super.removeItem(var1, var2);
   }

   public ItemStack removeItemNoUpdate(int var1) {
      return this.locked() ? ItemStack.EMPTY : super.removeItemNoUpdate(var1);
   }

   public void setItem(int var1, ItemStack var2) {
      if (!this.locked() && (var2.isEmpty() || CargoCatalog.accepts(BuiltInRegistries.ITEM.getKey(var2.getItem()).toString()))) {
         super.setItem(var1, var2);
      }
   }

   public void clearContent() {
      if (!this.locked()) {
         super.clearContent();
      }
   }

   public int[] getSlotsForFace(Direction var1) {
      return new int[0];
   }

   public boolean canPlaceItemThroughFace(int var1, ItemStack var2, Direction var3) {
      return false;
   }

   public boolean canTakeItemThroughFace(int var1, ItemStack var2, Direction var3) {
      return false;
   }

   private boolean matchesItem(ItemStack var1, String var2) {
      return !var1.isEmpty() && BuiltInRegistries.ITEM.getKey(var1.getItem()).toString().equals(var2);
   }

   public int count(String var1) {
      int var2 = 0;

      for (ItemStack var4 : this.items) {
         if (this.matchesItem(var4, var1)) {
            var2 += var4.getCount();
         }
      }

      return var2;
   }

   public void reserve(String var1, CargoCatalog.Mission var2) {
      if (this.count(var2.item()) >= var2.count() && this.reserved == 0 && (this.phase.equals("IDLE") || this.phase.equals("SETTLED"))) {
         int var3 = var2.count();

         for (ItemStack var5 : this.items) {
            if (this.matchesItem(var5, var2.item())) {
               int var6 = Math.min(var3, var5.getCount());
               var5.shrink(var6);
               var3 -= var6;
               if (var3 == 0) {
                  break;
               }
            }
         }

         this.reserved = var2.count();
         this.reservedItem = var2.item();
         this.mission = var2.id();
         this.delivery = var1;
         this.phase = "RESERVED";
         this.setChanged();
      } else {
         throw new IllegalStateException("Reservation invalide");
      }
   }

   public void settle(String var1) {
      if (this.delivery.equals(var1) && this.reserved == CargoCatalog.get(this.mission).count() && this.phase.equals("RESERVED")) {
         this.reserved = 0;
         this.phase = "SETTLED";
         this.setChanged();
      } else {
         throw new IllegalStateException("Recu invalide");
      }
   }

   protected void loadAdditional(ValueInput var1) {
      super.loadAdditional(var1);
      this.items = NonNullList.withSize(9, ItemStack.EMPTY);
      ContainerHelper.loadAllItems(var1, this.items);
      this.world = var1.getStringOr("VDWorld", "");
      this.crate = var1.getStringOr("VDCrate", "");
      this.delivery = var1.getStringOr("VDDelivery", "");
      this.phase = var1.getStringOr("VDPhase", "IDLE");
      this.reserved = var1.getIntOr("VDReserved", 0);
      this.reservedItem = var1.getStringOr("VDReservedItem", "minecraft:iron_ingot");
      this.mission = var1.getStringOr("VDMission", "logistics_first_delivery");
   }

   protected void saveAdditional(ValueOutput var1) {
      super.saveAdditional(var1);
      ContainerHelper.saveAllItems(var1, this.items);
      var1.putString("VDWorld", this.world);
      var1.putString("VDCrate", this.crate);
      var1.putString("VDDelivery", this.delivery);
      var1.putString("VDPhase", this.phase);
      var1.putInt("VDReserved", this.reserved);
      var1.putString("VDReservedItem", this.reservedItem);
      var1.putString("VDMission", this.mission);
   }
}
