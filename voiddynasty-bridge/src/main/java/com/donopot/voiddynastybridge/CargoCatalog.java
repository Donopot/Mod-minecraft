package com.donopot.voiddynastybridge;

import java.util.List;

public final class CargoCatalog {
   public static final String HASH = "9fe289e9bb643972571c685e165cea73ffba139bae3392292b7496b8066b19cf";
   public static final String DEFAULT = "logistics_first_delivery";
   public static final List<CargoCatalog.Mission> ALL = List.of(
      new CargoCatalog.Mission(
         "logistics_first_delivery",
         "Composants de vaisseau",
         "ShipComponents",
         "minecraft:iron_ingot",
         64,
         "c493636cdec944c6a136a83342b6b94c",
         "lingots de fer"
      ),
      new CargoCatalog.Mission("fuel", "Carburant", "Fuel", "minecraft:coal", 128, "17b1c11724264e23a81fdae2ff3218a7", "charbons"),
      new CargoCatalog.Mission("provisions", "Provisions", "Provisions", "minecraft:bread", 128, "122948ed6e074bff95f704d985fbd424", "pains"),
      new CargoCatalog.Mission("textiles", "Textiles", "Textile", "minecraft:white_wool", 64, "07fef339f6d640509c55f1f2059ecc4f", "laines blanches"),
      new CargoCatalog.Mission("mechanisms", "Mecanismes", "Tech", "minecraft:redstone", 64, "515476f9cab441d7a12edcc56a1753f1", "redstones"),
      new CargoCatalog.Mission("ranged_weapons", "Armes a distance", "RangedWeaponry", "minecraft:arrow", 64, "5c5ed57dc30343299ab9d7f3b38d83fc", "fleches"),
      new CargoCatalog.Mission(
         "melee_weapons", "Armes de melee", "MeleeWeaponry", "minecraft:iron_sword", 8, "2c44bf9619e845648579b1c3fb89c16c", "epees en fer"
      ),
      new CargoCatalog.Mission("armours", "Armures", "Armours", "minecraft:iron_chestplate", 4, "da8e35396894491ca887c0d489b4b5b6", "plastrons en fer"),
      new CargoCatalog.Mission("holy_gifts", "Presents sacres", "Holy", "minecraft:gold_ingot", 32, "700d3574bc324f1d8dfd4ec25af01766", "lingots d'or"),
      new CargoCatalog.Mission("xeno_artefacts", "Xeno-artefacts", "Xeno", "minecraft:ender_pearl", 16, "b252589fd6dc4814a1af6384e03e452c", "perles de l'End"),
      new CargoCatalog.Mission(
         "chaos_goods", "Marchandises heretiques", "Chaos", "minecraft:crying_obsidian", 32, "a2c94df3003a4fc68918b075e5a4059f", "obsidiennes pleureuses"
      ),
      new CargoCatalog.Mission("jewellery", "Bijoux", "Jewelry", "minecraft:emerald", 16, "d6c9bc46fe764ac98bfd8a72dce7f130", "emeraudes"),
      new CargoCatalog.Mission("torpedoes", "Torpilles", "Torpedoes", "minecraft:gunpowder", 64, "bbc9a9ebcfc84dd5bde7d7cfb8a97931", "poudres a canon"),
      new CargoCatalog.Mission(
         "evacuation", "Evacuation de civils", "People", "minecraft:baked_potato", 128, "94ef8ca89e8d4c44b74de5a1c66c6ff0", "pommes de terre cuites"
      )
   );

   public static CargoCatalog.Mission get(String var0) {
      return ALL.stream().filter(var1 -> var1.id().equals(var0)).findFirst().orElseThrow(() -> new IllegalArgumentException("Mission inconnue"));
   }

   public static boolean accepts(String var0) {
      return ALL.stream().anyMatch(var1 -> var1.item().equals(var0));
   }

   public static record Mission(String id, String label, String origin, String item, int count, String blueprint, String material) {
   }
}
