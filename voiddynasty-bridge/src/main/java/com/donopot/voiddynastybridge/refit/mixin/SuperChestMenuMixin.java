package com.donopot.voiddynastybridge.refit.mixin;

import com.donopot.voiddynasty.menu.SuperChestMenu;
import com.donopot.voiddynastybridge.refit.PageSlot;
import com.donopot.voiddynastybridge.refit.PageWord;
import com.donopot.voiddynastybridge.refit.PagedContainer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {SuperChestMenu.class},
   remap = false
)
public abstract class SuperChestMenuMixin extends AbstractContainerMenu {
   @Shadow
   @Final
   @Mutable
   private Container container;
   @Unique
   private PagedContainer vd$window;

   protected SuperChestMenuMixin(MenuType<?> var1, int var2) {
      super(var1, var2);
   }

   @Inject(
      method = {"<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/Container;)V"},
      at = {@At("RETURN")}
   )
   private void vd$window(int var1, Inventory var2, Container var3, CallbackInfo var4) {
      this.vd$window = new PagedContainer(var3, var2.player.level().isClientSide());
      this.container = this.vd$window;

      for (int var5 = 0; var5 < 54; var5++) {
         Slot var6 = (Slot)this.slots.get(var5);
         PageSlot var7 = new PageSlot(this.vd$window, var5, var6.x, var6.y);
         var7.index = var5;
         this.slots.set(var5, var7);
      }

      for (int var8 = 0; var8 < 4; var8++) {
         this.addDataSlot(new PageWord(this.vd$window, var8));
      }
   }

   @Inject(
      method = {"getMaxScrollRow"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void vd$max(CallbackInfoReturnable<Integer> var1) {
      if (this.vd$window != null) {
         var1.setReturnValue(this.vd$window.maxRow());
      }
   }

   @Inject(
      method = {"getScrollRow"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void vd$row(CallbackInfoReturnable<Integer> var1) {
      if (this.vd$window != null) {
         var1.setReturnValue(this.vd$window.row());
      }
   }

   @Inject(
      method = {"setScrollRow"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void vd$scroll(int var1, CallbackInfo var2) {
      if (this.vd$window != null) {
         this.vd$window.row(var1);
         var2.cancel();
      }
   }
}
