package br.com.oneminepiece.opworldlike.loot;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

/**
 * Catalogue des items Mine Piece ver15 dropés comme équipement rare.
 * (Roster 1.5.2 remappé vers les IDs ver15 ; les ~117 items supprimés en ver15 sont abandonnés.)
 */
public final class EquipmentLootCatalog {
   private static final String[] IDS = new String[]{
      "minepiece:ace",
      "minepiece:cat_claws",
      "minepiece:chakram",
      "minepiece:clothes_ace_helmet",
      "minepiece:clothes_crocodile_chestplate",
      "minepiece:clothes_franky_chestplate",
      "minepiece:clothes_luffy_boots",
      "minepiece:clothes_luffy_chestplate",
      "minepiece:clothes_luffy_helmet",
      "minepiece:clothes_luffy_leggings",
      "minepiece:clothes_mihawk_helmet",
      "minepiece:clothes_sabo_helmet",
      "minepiece:clothes_zoro_boots",
      "minepiece:clothes_zoro_chestplate",
      "minepiece:clothes_zoro_leggings",
      "minepiece:cutlass",
      "minepiece:daisenso",
      "minepiece:enma",
      "minepiece:hassaikai",
      "minepiece:hook",
      "minepiece:item_bullet",
      "minepiece:jitte",
      "minepiece:kikoku",
      "minepiece:kiribachi",
      "minepiece:meat_on_the_bone",
      "minepiece:mogura",
      "minepiece:murakumogiri",
      "minepiece:pipe",
      "minepiece:senriku",
      "minepiece:shusui",
      "minepiece:spear",
      "minepiece:tonfa",
      "minepiece:treasure_chest",
      "minepiece:treasure_chest_locked",
      "minepiece:uniform_marine_1_boots",
      "minepiece:uniform_marine_1_chestplate",
      "minepiece:uniform_marine_1_helmet",
      "minepiece:uniform_marine_1_leggings",
      "minepiece:wado_ichimonji",
      "minepiece:yoru",
   };
   private static List<Item> cachedItems;

   private EquipmentLootCatalog() {
   }

   public static List<Item> items() {
      if (cachedItems != null) {
         return cachedItems;
      }

      List<Item> resolved = new ArrayList<>();

      for (String id : IDS) {
         Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
         if (item != null && item != Items.AIR) {
            resolved.add(item);
         }
      }

      cachedItems = List.copyOf(resolved);
      return cachedItems;
   }
}
