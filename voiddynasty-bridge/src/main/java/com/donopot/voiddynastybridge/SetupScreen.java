package com.donopot.voiddynastybridge;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.network.chat.Component;

public final class SetupScreen extends Screen {
   private Button create;
   private Button pair;
   private Button open;

   public SetupScreen() {
      super(Component.literal("VOID DYNASTY | Assistant"));
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
      int var1 = this.height - 74;
      this.create = this.button("Creer le depot", 0, var1, () -> this.action("create"));
      this.pair = this.button("Demander le lien RT", 1, var1, () -> this.action("pair"));
      this.open = this.button("Deposer les materiaux", 0, var1 + 24, () -> {
         this.action("open");
         this.onClose();
      });
      this.button("Ouvrir le terminal", 1, var1 + 24, () -> Minecraft.getInstance().setScreenAndShow(new DeliveryScreen()));
      this.button("Fermer", 1, var1 + 48, this::onClose);
   }

   public void tick() {
      DeliveryService var1 = BridgeMod.service;
      if (var1 == null) {
         this.create.active = this.pair.active = this.open.active = false;
      } else {
         DeliveryService.View var2 = var1.view();
         boolean var3 = !var2.phase().equals("RECOVERY_REQUIRED");
         this.create.active = var3 && !var2.hasCrate();
         this.pair.active = var3 && var2.hasCrate() && !var2.paired() && var2.connected();
         this.open.active = var3 && var2.hasCrate() && (var2.phase().equals("IDLE") || var2.phase().equals("SETTLED"));
      }
   }

   public void extractRenderState(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      var1.fill(0, 0, this.width, this.height, -267381473);
      var1.centeredText(this.font, this.title, this.width / 2, 12, -993386);
      DeliveryService var5 = BridgeMod.service;
      int var6 = Math.max(12, this.width / 2 - 220);
      int var7 = Math.min(440, this.width - 24);
      String var8 = "Charger un monde solo local";
      String var9 = "Le pont fonctionne avec un monde solo sans ouverture LAN et Rogue Trader sur le meme PC.";
      String var10 = "";
      if (var5 != null) {
         DeliveryService.View var11 = var5.view();
         var10 = var11.availability() + " | " + var11.status();
         if (var11.phase().equals("RECOVERY_REQUIRED")) {
            var8 = "Reprise necessaire";
            var9 = "Conserver le depot et les sauvegardes. Recharger un checkpoint monde / campagne correspondant. Ne pas refaire l'association pour contourner ce blocage.";
         } else if (!var11.connected()) {
            var8 = "1 / 4 - Charger la campagne RT";
            var9 = "Dans Rogue Trader : Ctrl+F10, panneau VOID DYNASTY. Premiere utilisation : Preparer cette campagne. Pour une partie deja liee, charger sa derniere sauvegarde REWARD_COMMITTED.";
         } else if (!var11.hasCrate()) {
            var8 = "2 / 4 - Installer le depot";
            var9 = "Se placer devant un espace libre puis cliquer Creer le depot. Ce depot reste fixe dans ce monde. Votre campagne RT doit etre preparee avant de demander le lien.";
         } else if (!var11.paired()) {
            var8 = "3 / 4 - Associer ce monde";
            var9 = "Cliquer Demander le lien RT. Dans le panneau VOID DYNASTY de Rogue Trader, verifier le monde demandeur et approuver. Attendre la confirmation de sauvegarde. Monde : "
               + var11.world();
         } else if (var11.nativeCount() == 0) {
            var8 = "4 / 4 - Faire une livraison";
            var9 = "Ouvrir le terminal, choisir une mission, puis deposer les materiaux demandes. Cliquer Expedier puis Confirmer. La cargaison native sera sauvegardee dans RT avant la confirmation.";
         } else {
            var8 = "Le pont est pret";
            var9 = "Choisir une mission dans le terminal pour continuer. Pour reprendre plus tard, charger ce monde et la derniere sauvegarde RT REWARD_COMMITTED correspondante. Cargaisons natives livrees : "
               + var11.nativeCount();
         }
      }

      var1.text(this.font, var8, var6, 34, -1906962);
      var1.textWithWordWrap(this.font, Component.literal(var9), var6, 52, var7, -1906962);
      if (this.height >= 290) {
         var1.textWithWordWrap(this.font, Component.literal(var10), var6, 118, var7, -993386);
      }

      super.extractRenderState(var1, var2, var3, var4);
   }
}
