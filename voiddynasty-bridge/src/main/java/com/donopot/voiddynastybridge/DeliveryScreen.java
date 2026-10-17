package com.donopot.voiddynastybridge;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;

public final class DeliveryScreen extends Screen {
   private boolean confirm;
   private Button create;
   private Button open;
   private Button pair;
   private Button ship;
   private Button resume;
   private Button previousMission;
   private Button nextMission;
   private String selected = "";
   private String confirmedMission = "";

   public DeliveryScreen() {
      super(Component.literal("VOID DYNASTY | Livraison"));
   }

   public boolean isPauseScreen() {
      return false;
   }

   private void action(String var1) {
      Minecraft var2 = Minecraft.getInstance();
      IntegratedServer var3 = var2.getSingleplayerServer();
      if (var3 != null && var2.player != null) {
         UUID var4 = var2.player.getUUID();
         var3.execute(() -> {
            DeliveryService var3x = BridgeMod.service;
            if (var3x != null && var3x.server == var3) {
               var3x.action(var4, var1);
            }
         });
      }
   }

   private Button button(String var1, int var2, int var3, Runnable var4) {
      return (Button)this.addRenderableWidget(
         Button.builder(Component.literal(var1), var1x -> var4.run()).bounds(this.width / 2 - 150 + var2 * 154, var3, 146, 20).build()
      );
   }

   protected void init() {
      int var1 = Math.max(144, this.height - 98);
      this.previousMission = this.button("Mission precedente", 0, var1, () -> {
         this.confirm = false;
         this.action("previous_mission");
      });
      this.nextMission = this.button("Mission suivante", 1, var1, () -> {
         this.confirm = false;
         this.action("next_mission");
      });
      var1 += 24;
      this.create = this.button("Creer le depot", 0, var1, () -> this.action("create"));
      this.open = this.button("Ouvrir le depot", 1, var1, () -> {
         this.action("open");
         this.onClose();
      });
      this.pair = this.button("Lier la campagne", 0, var1 + 24, () -> this.action("pair"));
      this.ship = this.button("Expedier", 1, var1 + 24, () -> {
         if (!this.confirm) {
            DeliveryService var1x = BridgeMod.service;
            if (var1x == null) {
               return;
            }

            this.confirmedMission = var1x.view().missionId();
            this.confirm = true;
            this.ship.setMessage(Component.literal("Confirmer la livraison"));
         } else {
            this.confirm = false;
            this.ship.setMessage(Component.literal("Expedier"));
            this.action("ship:" + this.confirmedMission);
         }
      });
      this.resume = this.button("Reprendre / recu", 0, var1 + 48, () -> this.action("resume"));
      this.button("Fermer", 1, var1 + 48, this::onClose);
      this.addRenderableWidget(
         Button.builder(Component.literal("?"), var0 -> Minecraft.getInstance().setScreenAndShow(new SetupScreen())).bounds(this.width - 30, 8, 22, 20).build()
      );
   }

   public void tick() {
      DeliveryService var1 = BridgeMod.service;
      if (var1 == null) {
         this.create.active = this.open.active = this.pair.active = this.ship.active = this.resume.active = false;
      } else {
         DeliveryService.View var2 = var1.view();
         this.create.active = !var2.hasCrate() && !var2.phase().equals("RECOVERY_REQUIRED");
         this.open.active = var2.hasCrate() && (var2.phase().equals("IDLE") || var2.phase().equals("DRAFT") || var2.phase().equals("SETTLED"));
         this.previousMission.active = this.nextMission.active = var2.canSelect();
         if (!this.selected.equals(var2.mission())) {
            this.selected = var2.mission();
            this.confirm = false;
         }

         this.pair.active = var2.hasCrate() && !var2.paired() && var2.connected();
         this.ship.active = var2.canShip();
         this.resume.active = var2.paired() && (var2.phase().equals("DRAFT") || var2.phase().equals("RESERVED"));
         if (!var2.canShip()) {
            this.confirm = false;
         }

         if (!this.confirm) {
            this.ship.setMessage(Component.literal("Expedier (" + (var2.completed() + 1) + ")"));
         }
      }
   }

   public void extractRenderState(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      var1.fill(0, 0, this.width, this.height, -300935905);
      var1.centeredText(this.font, this.title, this.width / 2, 18, -993386);
      DeliveryService var5 = BridgeMod.service;
      if (var5 == null) {
         var1.centeredText(this.font, "Charger un monde solo local, sans ouverture LAN", this.width / 2, 52, -13176);
      } else {
         DeliveryService.View var6 = var5.view();
         int var7 = Math.max(12, this.width / 2 - 220);
         byte var8 = 40;
         String[] var9 = new String[]{
            "Mission : " + var6.mission(),
            "Monde : " + var6.world(),
            "Deposes : " + var6.iron() + " / " + var6.required() + " | Reserves : " + var6.reserved(),
            "Cargaisons RT livrees : " + var6.nativeCount() + " | Logistique : " + (var6.reserve() < 0 ? "--" : var6.reserve()),
            "Livraisons : " + var6.completed() + " | " + var6.phase() + " | " + (var6.connected() ? "Connecte" : "Deconnecte"),
            var6.availability(),
            "Campagne RT : " + (var6.campaign().length() > 8 ? var6.campaign().substring(0, 8) : var6.campaign()) + " / " + var6.zone()
         };

         for (String var13 : var9) {
            var1.text(this.font, var13, var7, var8, -1906962);
            var8 += 12;
         }

         var1.textWithWordWrap(this.font, Component.literal(var6.status()), var7, var8, Math.min(440, this.width - 24), -993386);
      }

      super.extractRenderState(var1, var2, var3, var4);
   }
}
