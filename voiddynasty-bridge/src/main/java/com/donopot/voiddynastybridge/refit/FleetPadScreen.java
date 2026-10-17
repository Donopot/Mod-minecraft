package com.donopot.voiddynastybridge.refit;

import com.donopot.voiddynasty.ControlPadScreen;
import com.donopot.voiddynasty.ShipType;
import com.donopot.voiddynasty.network.FleetListRequestPayload;
import com.donopot.voiddynasty.network.ShipInfo;
import java.util.List;
import java.util.Locale;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class FleetPadScreen extends Screen {
   private List<ShipInfo> fleet = List.of();
   private String selected = "";
   private String status = "Lecture de la flotte...";
   private String pending = "";
   private String draft = "";
   private int tab;
   private int page;
   private int type;
   private boolean listRequested;
   private EditBox name;
   private PadLayout layout;
   private static final ShipType[] SHIPS = ShipType.values();

   private static String id(ShipType var0) {
      return var0.name().toLowerCase(Locale.ROOT);
   }

   private static String label(ShipType var0) {
      return var0.displayName;
   }

   public FleetPadScreen() {
      super(Component.literal("VOID DYNASTY | Flotte"));
   }

   private Button button(String var1, int var2, int var3, int var4, Runnable var5) {
      return (Button)this.addRenderableWidget(Button.builder(Component.literal(var1), var1x -> var5.run()).bounds(var2, var3, var4, 20).build());
   }

   private void rebuild() {
      if (this.name != null) {
         this.draft = this.name.getValue();
      }

      this.name = null;
      this.clearWidgets();
      this.init();
   }

   private ShipInfo selected() {
      return this.fleet.stream().filter(var1 -> var1.name().equals(this.selected)).findFirst().orElse(null);
   }

   private void request() {
      if (Minecraft.getInstance().player != null) {
         ClientPlayNetworking.send(new FleetListRequestPayload());
      }
   }

   protected void init() {
      this.layout = PadLayout.of(this.width, this.height);
      int var1 = this.layout.left();
      int var2 = this.layout.width();
      String[] var3 = new String[]{"Flotte", "Commandes", "Construction"};

      for (int var4 = 0; var4 < 3; var4++) {
         final int var7 = var4;
         Button var6 = this.button(var3[var7], var1 + var7 * (var2 / 3), 34, var2 / 3 - 4, () -> {
            this.tab = var7;
            this.pending = "";
            this.rebuild();
         });
         var6.active = var4 != this.tab;
      }

      if (this.tab == 0) {
         int var10 = this.page * this.layout.rows();

         for (int var5 = 0; var5 < this.layout.rows() && var10 + var5 < this.fleet.size(); var5++) {
            ShipInfo var15 = this.fleet.get(var10 + var5);
            String var7 = (var15.name().equals(this.selected) ? "> " : "")
               + var15.name()
               + " | "
               + var15.typeDisplay()
               + " | "
               + (var15.deployed() ? "Déployé" : "Rangé");
            this.button(this.font.plainSubstrByWidth(var7, var2 - 16), var1, 66 + var5 * 22, var2, () -> {
               this.selected = var15.name();
               this.tab = 1;
               this.pending = "";
               this.rebuild();
            });
         }

         int var13 = this.layout.footer();
         this.button("< Précédente", var1, var13, this.layout.columnWidth(), () -> {
            this.page = Math.max(0, this.page - 1);
            this.rebuild();
         }).active = this.page > 0;
         this.button("Suivante >", var1 + this.layout.columnWidth() + 12, var13, this.layout.columnWidth(), () -> {
            this.page++;
            this.rebuild();
         }).active = (this.page + 1) * this.layout.rows() < this.fleet.size();
      } else if (this.tab == 1) {
         ShipInfo var11 = this.selected();
         int var14 = this.layout.columnWidth();
         Button var16 = this.button("Déployer", var1, 96, var14, () -> this.command("deploy", false));
         var16.active = var11 != null && !var11.deployed();
         Button var17 = this.button(
            this.pending.equals("stow") ? "Confirmer le rangement" : "Ranger", var1 + var14 + 12, 96, var14, () -> this.command("stow", true)
         );
         var17.active = var11 != null && var11.deployed();
         Button var8 = this.button(
            this.pending.equals("teleport") ? "Confirmer le déplacement" : "Déplacer ici (X/Z)", var1, 120, var14, () -> this.command("teleport", true)
         );
         var8.active = var11 != null && var11.deployed();
         Button var9 = this.button(
            this.pending.equals("delete") ? "Confirmer la suppression" : "Supprimer", var1 + var14 + 12, 120, var14, () -> this.command("delete", true)
         );
         var9.active = var11 != null;
         this.name = new EditBox(this.font, var1, 146, var14, 20, Component.literal("Nouveau nom"));
         this.name.setMaxLength(32);
         this.name.setValue(this.draft);
         this.addRenderableWidget(this.name);
         this.button("Renommer", var1 + var14 + 12, 146, var14, () -> this.command("rename", false)).active = var11 != null;
      } else {
         int var12 = this.layout.columnWidth();
         this.name = new EditBox(this.font, var1, 96, var2, 20, Component.literal("Nom du vaisseau"));
         this.name.setMaxLength(32);
         this.name.setValue(this.draft);
         this.addRenderableWidget(this.name);
         this.button("Classe : " + label(SHIPS[this.type]), var1, 124, var12, () -> {
            this.type = (this.type + 1) % SHIPS.length;
            this.rebuild();
         });
         this.button("Ajouter à la flotte", var1 + var12 + 12, 124, var12, () -> this.command("add", false));
      }

      this.button("Actualiser", var1, this.height - 26, this.layout.columnWidth(), this::request);
      this.button("Fermer", var1 + this.layout.columnWidth() + 12, this.height - 26, this.layout.columnWidth(), this::onClose);
      if (!this.listRequested) {
         this.listRequested = true;
         this.request();
      }
   }

   public void updateShips(List<ShipInfo> var1) {
      Minecraft.getInstance().execute(() -> {
         this.fleet = List.copyOf(var1);
         if (this.selected() == null) {
            this.selected = "";
            this.pending = "";
         }

         if (this.selected.isEmpty() && !this.fleet.isEmpty()) {
            this.selected = this.fleet.getFirst().name();
         }

         this.page = Math.min(this.page, Math.max(0, (this.fleet.size() - 1) / Math.max(1, PadLayout.of(this.width, this.height).rows())));
         this.status = this.fleet.size() + " vaisseau(x) — sélection et commandes propres à ce joueur";
         this.rebuild();
      });
   }

   public void onClose() {
      ControlPadScreen var1 = ControlPadScreen.getInstance();
      if (var1 != null) {
         var1.onClose();
      } else {
         super.onClose();
      }
   }

   private void command(String var1, boolean var2) {
      Minecraft var3 = Minecraft.getInstance();
      if (var3.player != null) {
         ShipInfo var4 = this.selected();
         if (var1.equals("add") || var4 != null && PadLayout.name(var4.name())) {
            if (var2 && !this.pending.equals(var1)) {
               this.pending = var1;
               this.status = "Vérifier le vaisseau sélectionné, puis confirmer l'action.";
               this.rebuild();
            } else {
               String var5 = this.name == null ? this.draft : this.name.getValue().trim().replace(' ', '_');
               if ((var1.equals("add") || var1.equals("rename")) && !PadLayout.name(var5)) {
                  this.status = "Nom : 1 à 32 lettres, chiffres, _ ou -.";
               } else {
                  String var6 = switch (var1) {
                     case "add" -> "fleet add " + id(SHIPS[this.type]) + " " + var5;
                     case "rename" -> "fleet rename " + var4.name() + " " + var5;
                     case "teleport" -> "fleet teleport " + var4.name() + " " + var3.player.getBlockX() + " " + var3.player.getBlockZ();
                     default -> "fleet " + var1 + " " + var4.name();
                  };
                  var3.player.connection.sendCommand(var6);
                  if (var1.equals("rename") || var1.equals("add")) {
                     this.selected = var5;
                  }

                  this.pending = "";
                  this.status = "Commande envoyée : " + var1 + ". Le résultat est affiché dans le chat.";
                  this.request();
                  this.rebuild();
               }
            }
         } else {
            this.status = "Sélectionner un vaisseau au nom valide.";
         }
      }
   }

   public void extractRenderState(GuiGraphicsExtractor var1, int var2, int var3, float var4) {
      var1.fill(0, 0, this.width, this.height, -267381473);
      var1.fill(this.layout.left() - 8, 28, this.layout.left() + this.layout.width() + 8, this.height - 30, -15129808);
      var1.centeredText(this.font, "VOID DYNASTY | Commandement de flotte", this.width / 2, 13, -993386);
      int var5 = this.layout.left();
      ShipInfo var6 = this.selected();
      if (this.tab == 0 && this.fleet.isEmpty()) {
         var1.text(this.font, "Aucun vaisseau — ouvrir Construction pour en ajouter.", var5, 70, -1906962);
      }

      if (this.tab == 0) {
         var1.text(
            this.font,
            "Page " + (this.page + 1) + " / " + Math.max(1, (this.fleet.size() + this.layout.rows() - 1) / this.layout.rows()),
            var5,
            this.height - 68,
            -4471086
         );
      }

      if (this.tab == 1) {
         var1.text(
            this.font,
            this.font.plainSubstrByWidth(var6 == null ? "Choisir un vaisseau dans Flotte" : var6.name() + " | " + var6.typeDisplay(), this.layout.width()),
            var5,
            64,
            -993386
         );
         if (var6 != null) {
            var1.text(this.font, var6.deployed() ? "Position : " + var6.x() + ", " + var6.y() + ", " + var6.z() : "Vaisseau rangé", var5, 78, -4471086);
         }
      }

      if (this.tab == 2) {
         var1.text(this.font, "Nom et classe du nouveau vaisseau", var5, 76, -4471086);
         var1.text(this.font, this.font.plainSubstrByWidth(label(SHIPS[this.type]) + " — " + SHIPS[this.type].nx + "x" + SHIPS[this.type].ny + "x" + SHIPS[this.type].nz + " blocs", this.layout.width()), var5, 172, -4471086);
      }

      if (this.tab != 0) {
         var1.text(this.font, this.font.plainSubstrByWidth(this.status, this.layout.width()), var5, this.height - 64, -993386);
      }

      super.extractRenderState(var1, var2, var3, var4);
   }
}
