package net.mcreator.minepiece.client.hud;

import net.mcreator.minepiece.client.DevilFruitClientState;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * HUD de la couche Fruits du Démon (CORE-028).
 *
 * <p>Affiche, uniquement quand le joueur a mangé un Fruit : le nom du Fruit, la barre d'Énergie,
 * la technique sélectionnée (« n/total » + nom) et la forme courante. Tout le texte passe par des
 * clés de traduction ({@code minepiece.hud.*}, {@code minepiece.overlay.*}, {@code minepiece.technique.*},
 * {@code item.minepiece.*}) : l'affichage suit la langue du client sans texte en dur.</p>
 *
 * <p>Les données viennent de {@link DevilFruitClientState}, synchronisé par le serveur (CORE-004),
 * étendu en CORE-028 avec l'id de la technique et le nombre de techniques.</p>
 */
public final class DevilFruitHud implements HudElement {

   private static final Identifier ID = Identifier.fromNamespaceAndPath("minepiece", "devil_fruit_hud");

   private static final int PANEL_BG = 0xB0101010;
   private static final int BORDER = 0xFF3A3A3A;
   private static final int BAR_BG = 0xFF202020;
   private static final int BAR_FILL = 0xFF35C46A;
   private static final int BAR_LOW = 0xFFD8523A;
   private static final int TITLE = 0xFFE0B040;
   private static final int TEXT = 0xFFE8E8E8;
   private static final int DIM = 0xFF9A9A9A;

   private static final int WIDTH = 168;
   private static final int HEIGHT = 56;
   private static final int MARGIN = 6;

   public static void register() {
      HudElementRegistry.addLast(ID, new DevilFruitHud());
   }

   @Override
   public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker delta) {
      Minecraft client = Minecraft.getInstance();
      if (client.player == null || client.font == null) {
         return;
      }
      if (DevilFruitClientState.fruitId.isEmpty()) {
         return;                                        // aucun Fruit → pas de HUD
      }

      int x = MARGIN;
      int y = graphics.guiHeight() - HEIGHT - MARGIN - 30;   // au-dessus de la barre rapide
      graphics.fill(x - 1, y - 1, x + WIDTH + 1, y + HEIGHT + 1, BORDER);
      graphics.fill(x, y, x + WIDTH, y + HEIGHT, PANEL_BG);

      int cx = x + 5;
      int cy = y + 5;

      // Nom du Fruit — traduit via la clé de l'item (minepiece:item_devil_fruit_x → item.minepiece....)
      graphics.text(client.font, Component.translatable("minepiece.hud.title"), cx, cy, TITLE);
      cy += 11;
      graphics.text(client.font, fruitName(), cx + 4, cy, TEXT);
      cy += 12;

      // Énergie : libellé + barre
      int energy = (int) Math.round(DevilFruitClientState.energy);
      int energyMax = (int) Math.round(DevilFruitClientState.energyMax);
      Component energyLabel = Component.translatable("minepiece.overlay.energy");
      graphics.text(client.font, energyLabel, cx + 4, cy, DIM);
      graphics.text(client.font, Component.literal(energy + "/" + energyMax),
         cx + 56, cy, TEXT);
      cy += 10;
      int barWidth = WIDTH - 16;
      graphics.fill(cx + 4, cy, cx + 4 + barWidth, cy + 4, BAR_BG);
      if (energyMax > 0) {
         int filled = Math.max(0, Math.min(barWidth, (int) Math.round(barWidth * energy / energyMax)));
         int color = energy * 100 / energyMax <= 20 ? BAR_LOW : BAR_FILL;
         if (filled > 0) {
            graphics.fill(cx + 4, cy, cx + 4 + filled, cy + 4, color);
         }
      }
      cy += 7;

      // Technique courante : « n/total » + nom localisé
      Component techniqueName = DevilFruitClientState.techniqueId.isEmpty()
         ? Component.translatable("minepiece.hud.none")
         : Component.translatableWithFallback(
            "minepiece.technique." + (DevilFruitClientState.techniqueId.equals("passive") && DevilFruitClientState.fruitId.endsWith("item_devil_fruit_aro_aro") ? "aro_passive" : DevilFruitClientState.techniqueId.equals("passive") && DevilFruitClientState.fruitId.endsWith("item_devil_fruit_nikyu_nikyu") ? "nikyu_passive" : DevilFruitClientState.techniqueId.equals("passive") && DevilFruitClientState.fruitId.endsWith("item_devil_fruit_pika_pika") ? "pika_passive" : DevilFruitClientState.techniqueId),
            DevilFruitClientState.techniqueId);
      graphics.text(client.font, Component.translatable("minepiece.hud.technique"), cx + 4, cy, DIM);
      String index = DevilFruitClientState.techniqueCount > 0
         ? (DevilFruitClientState.technique + 1) + "/" + DevilFruitClientState.techniqueCount + " "
         : "";
      graphics.text(client.font, Component.literal(index).append(techniqueName), cx + 56, cy, TEXT);
      cy += 11;

      // Forme
      Component form = Component.translatable("minepiece.hud.form");
      Component formValue = DevilFruitClientState.form > 0.0
         ? (DevilFruitClientState.fruitId.equals(net.mcreator.minepiece.procedures.GoroAmaru.FRUIT) ? Component.literal("Amaru") : Component.translatable("minepiece.hud.form.hybrid"))
         : Component.translatable("minepiece.hud.form.base");
      if(DevilFruitClientState.form>0&&DevilFruitClientState.fruitId.equals(net.mcreator.minepiece.procedures.GomuGears.FRUIT))formValue=Component.literal("Gear "+(int)DevilFruitClientState.form);
      if(DevilFruitClientState.form==2&&!DevilFruitClientState.fruitId.equals(net.mcreator.minepiece.procedures.GomuGears.FRUIT))formValue=Component.literal("Animale");
      graphics.text(client.font, form, cx + 4, cy, DIM);
      graphics.text(client.font, formValue, cx + 56, cy, TEXT);
      cy += 11;

      // FRUIT-KIRO_KIRO-03 : le poids, affiché SEULEMENT quand il n'est pas normal, et lu depuis l'état
      // CONFIRMÉ par le serveur (DevilFruitClientState.weight). Si le serveur a borné la demande du
      // joueur, l'interface montre la valeur bornée : elle suit l'état confirmé, jamais la demande.
      if (Math.abs(DevilFruitClientState.weight - 1.0) > 1.0E-9) {
         Component weightLabel = Component.translatable("minepiece.hud.weight");
         String poids = (DevilFruitClientState.weight == Math.floor(DevilFruitClientState.weight)
            ? String.valueOf((long) DevilFruitClientState.weight)
            : String.format(java.util.Locale.ROOT, "%.1f", DevilFruitClientState.weight)) + "×";
         graphics.text(client.font, weightLabel, cx + 4, cy, DIM);
         graphics.text(client.font, Component.literal(poids), cx + 56, cy, TEXT);
      }
   }

   /** Nom localisé du Fruit courant, dérivé de l'id d'item synchronisé. */
   private static Component fruitName() {
      String id = DevilFruitClientState.fruitId;              // ex. minepiece:item_devil_fruit_mera_mera
      int colon = id.indexOf(':');
      if (colon <= 0) {
         return Component.literal(id);
      }
      String key = "item." + id.substring(0, colon) + "." + id.substring(colon + 1);
      return Component.translatableWithFallback(key, id.substring(colon + 1));
   }
}
