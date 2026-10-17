package com.donopot.voiddynastybridge.refit.mixin;

import com.donopot.voiddynasty.client.SuperChestScreen;
import com.donopot.voiddynasty.menu.SuperChestMenu;
import com.donopot.voiddynasty.network.ScrollContainerPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(
   value = {SuperChestScreen.class},
   remap = false
)
public abstract class SupplyChestScreen extends AbstractContainerScreen<SuperChestMenu> {
   @Unique
   private Button up;
   @Unique
   private Button down;
   @Unique
   private int requested = -1;
   @Unique
   private long retry;

   protected SupplyChestScreen(SuperChestMenu var1, Inventory var2, Component var3) {
      super(var1, var2, var3, 176, 222);
   }

   @Inject(
      method = {"<init>"},
      at = {@At("RETURN")}
   )
   private void vd$init(SuperChestMenu var1, Inventory var2, Component var3, CallbackInfo var4) {
      this.requested = -1;
   }

   @ModifyArg(
      method = {"<init>"},
      at = {@At(
         value = "INVOKE",
         target = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;<init>(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;II)V"
      )},
      index = 3
   )
   private static int vd$width(int var0) {
      return 200;
   }

   @Overwrite
   public void extractRenderState(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      super.extractRenderState(var1, var2, var3, var4);
   }

   protected void init() {
      super.init();
      this.up = (Button)this.addRenderableWidget(
         Button.builder(Component.literal("^"), var1 -> this.scroll(-6)).bounds(this.leftPos + 178, this.topPos + 22, 16, 20).build()
      );
      this.down = (Button)this.addRenderableWidget(
         Button.builder(Component.literal("v"), var1 -> this.scroll(6)).bounds(this.leftPos + 178, this.topPos + 99, 16, 20).build()
      );
   }

   private boolean waiting() {
      return this.requested >= 0;
   }

   private void scroll(int var1) {
      if (!this.waiting() && ((SuperChestMenu)this.menu).getCarried().isEmpty() && !this.isQuickCrafting) {
         int var2 = Math.clamp((long)(((SuperChestMenu)this.menu).getScrollRow() + var1), 0, ((SuperChestMenu)this.menu).getMaxScrollRow());
         if (var2 != ((SuperChestMenu)this.menu).getScrollRow()) {
            this.requested = var2;
            this.retry = System.currentTimeMillis() + 2000L;
            ClientPlayNetworking.send(new ScrollContainerPayload(((SuperChestMenu)this.menu).containerId, var2));
         }
      }
   }

   protected void containerTick() {
      super.containerTick();
      if (this.requested == ((SuperChestMenu)this.menu).getScrollRow()) {
         this.requested = -1;
      }

      if (this.waiting() && System.currentTimeMillis() > this.retry) {
         this.retry = System.currentTimeMillis() + 2000L;
         ClientPlayNetworking.send(new ScrollContainerPayload(((SuperChestMenu)this.menu).containerId, this.requested));
      }

      this.up.active = !this.waiting() && ((SuperChestMenu)this.menu).getScrollRow() > 0 && ((SuperChestMenu)this.menu).getCarried().isEmpty();
      this.down.active = !this.waiting()
         && ((SuperChestMenu)this.menu).getScrollRow() < ((SuperChestMenu)this.menu).getMaxScrollRow()
         && ((SuperChestMenu)this.menu).getCarried().isEmpty();
   }

   @Overwrite
   public boolean mouseScrolled(double var1, double var3, double var5, double var7) {
      if (var1 >= (double)this.leftPos
         && var1 < (double)(this.leftPos + this.imageWidth)
         && var3 >= (double)(this.topPos + 18)
         && var3 < (double)(this.topPos + 126)
         && var7 != 0.0) {
         this.scroll(var7 > 0.0 ? -1 : 1);
         return true;
      } else {
         return super.mouseScrolled(var1, var3, var5, var7);
      }
   }

   public boolean mouseClicked(MouseButtonEvent var1, boolean var2) {
      return this.waiting() || super.mouseClicked(var1, var2);
   }

   public boolean mouseDragged(MouseButtonEvent var1, double var2, double var4) {
      return this.waiting() || super.mouseDragged(var1, var2, var4);
   }

   public boolean mouseReleased(MouseButtonEvent var1) {
      return this.waiting() || super.mouseReleased(var1);
   }

   public boolean keyPressed(KeyEvent var1) {
      return this.waiting() && var1.key() != 256 || super.keyPressed(var1);
   }

   public void extractContents(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      var1.fill(this.leftPos - 2, this.topPos - 2, this.leftPos + this.imageWidth + 2, this.topPos + this.imageHeight + 2, -993386);
      var1.fill(this.leftPos, this.topPos, this.leftPos + this.imageWidth, this.topPos + this.imageHeight, -15129808);

      for (Slot var6 : ((SuperChestMenu)this.menu).slots) {
         int var7 = this.leftPos + var6.x;
         int var8 = this.topPos + var6.y;
         var1.fill(var7 - 1, var8 - 1, var7 + 17, var8 + 17, -11444112);
         var1.fill(var7, var8, var7 + 16, var8 + 16, -15920357);
      }

      var1.fill(this.leftPos + 181, this.topPos + 48, this.leftPos + 191, this.topPos + 93, -15920357);
      int var9 = this.topPos
         + 48
         + (
            ((SuperChestMenu)this.menu).getMaxScrollRow() == 0
               ? 0
               : 35 * ((SuperChestMenu)this.menu).getScrollRow() / ((SuperChestMenu)this.menu).getMaxScrollRow()
         );
      var1.fill(this.leftPos + 181, var9, this.leftPos + 191, var9 + 10, -993386);
      super.extractContents(var1, var2, var3, var4);
   }

   protected void extractLabels(GuiGraphicsExtractor var1, int var2, int var3) {
      var1.text(this.font, this.font.plainSubstrByWidth(this.title.getString(), 184), 8, 6, -993386);
      var1.text(
         this.font,
         this.waiting()
            ? "Synchronisation..."
            : "Lignes "
               + (((SuperChestMenu)this.menu).getScrollRow() + 1)
               + " à "
               + (((SuperChestMenu)this.menu).getScrollRow() + 6)
               + " / "
               + (((SuperChestMenu)this.menu).getMaxScrollRow() + 6),
         8,
         128,
         -4471086
      );
   }
}
