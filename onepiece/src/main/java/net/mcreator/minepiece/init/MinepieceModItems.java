package net.mcreator.minepiece.init;

import java.util.List;
import net.mcreator.minepiece.item.DevilFruitItem;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ToolMaterial;

/** Shim Mine Piece ver15 — items référencés par opworldlike + fruits du démon (généré). */
public final class MinepieceModItems {
   public static final String NAMESPACE = "minepiece";

   // Équipement (loot)
   /**
    * Manuel de Rokushiki — l'objet de la source ({@code minepiece:skill_selector}, texture
    * {@code icon_book.png}) par lequel l'art martial s'apprend <b>hors des Fruits du Démon</b>.
    * Première utilisation : apprentissage. Ensuite : choix de la technique.
    */
   public static final Item ITEM_SKILL_SELECTOR = registerManual("skill_selector");

   public static final Item BISCUIT = registerBiscuit();
   public static final Item AMANO_MURAKUMO = registerLightSword();
   public static final Item ACE = registerSword("ace", 3.0f, -2.4f);
   public static final Item CAT_CLAWS = registerSword("cat_claws", 3.0f, -2.0f);
   public static final Item CHAKRAM = registerSword("chakram", 3.0f, -2.0f);
   public static final Item CLOTHES_ACE_HELMET = register("clothes_ace_helmet");
   public static final Item CLOTHES_CROCODILE_CHESTPLATE = register("clothes_crocodile_chestplate");
   public static final Item CLOTHES_FRANKY_CHESTPLATE = register("clothes_franky_chestplate");
   public static final Item CLOTHES_LUFFY_BOOTS = register("clothes_luffy_boots");
   public static final Item CLOTHES_LUFFY_CHESTPLATE = register("clothes_luffy_chestplate");
   public static final Item CLOTHES_LUFFY_HELMET = register("clothes_luffy_helmet");
   public static final Item CLOTHES_LUFFY_LEGGINGS = register("clothes_luffy_leggings");
   public static final Item CLOTHES_MIHAWK_HELMET = register("clothes_mihawk_helmet");
   public static final Item CLOTHES_SABO_HELMET = register("clothes_sabo_helmet");
   public static final Item CLOTHES_ZORO_BOOTS = register("clothes_zoro_boots");
   public static final Item CLOTHES_ZORO_CHESTPLATE = register("clothes_zoro_chestplate");
   public static final Item CLOTHES_ZORO_LEGGINGS = register("clothes_zoro_leggings");
   public static final Item CUTLASS = registerSword("cutlass", 3.0f, -2.4f);
   public static final Item DAISENSO = registerSword("daisenso", 3.0f, -2.4f);
   public static final Item ENMA = registerSword("enma", 3.0f, -2.4f);
   public static final Item HASSAIKAI = registerSword("hassaikai", 3.0f, -2.4f);
   public static final Item HOOK = registerSword("hook", 3.0f, -2.4f);
   public static final Item ITEM_BULLET = register("item_bullet");
   public static final Item JITTE = registerSword("jitte", 3.0f, -2.4f);
   public static final Item KIKOKU = registerSword("kikoku", 3.0f, -2.4f);
   public static final Item KIRIBACHI = registerSword("kiribachi", 3.0f, -2.4f);
   public static final Item MEAT_ON_THE_BONE = register("meat_on_the_bone");
   public static final Item MOGURA = registerSword("mogura", 3.0f, -2.4f);
   public static final Item MURAKUMOGIRI = registerSword("murakumogiri", 3.0f, -2.4f);
   public static final Item PIPE = registerSword("pipe", 3.0f, -2.4f);
   public static final Item SENRIKU = registerSword("senriku", 3.0f, -2.4f);
   public static final Item SHUSUI = registerSword("shusui", 3.0f, -2.4f);
   public static final Item SPEAR = registerSword("spear", 3.0f, -2.4f);
   public static final Item TONFA = registerSword("tonfa", 3.0f, -2.4f);
   public static final Item TREASURE_CHEST = register("treasure_chest");
   public static final Item TREASURE_CHEST_LOCKED = register("treasure_chest_locked");
   public static final Item UNIFORM_MARINE_1_BOOTS = register("uniform_marine_1_boots");
   public static final Item UNIFORM_MARINE_1_CHESTPLATE = register("uniform_marine_1_chestplate");
   public static final Item UNIFORM_MARINE_1_HELMET = register("uniform_marine_1_helmet");
   public static final Item UNIFORM_MARINE_1_LEGGINGS = register("uniform_marine_1_leggings");
   public static final Item WADO_ICHIMONJI = registerSword("wado_ichimonji", 3.0f, -2.4f);
   public static final Item YORU = registerSword("yoru", 3.0f, -2.5f);

   // Fruits du démon
   public static final Item ITEM_DEVIL_FRUIT = registerFruit("item_devil_fruit");
   public static final Item ITEM_DEVIL_FRUIT_ARO_ARO = registerFruit("item_devil_fruit_aro_aro");
   public static final Item ITEM_DEVIL_FRUIT_BATTO_BATTO = registerFruit("item_devil_fruit_batto_batto");
   public static final Item ITEM_DEVIL_FRUIT_BISU_BISU = registerFruit("item_devil_fruit_bisu_bisu");
   public static final Item ITEM_DEVIL_FRUIT_BOMU_BOMU = registerFruit("item_devil_fruit_bomu_bomu");
   public static final Item ITEM_DEVIL_FRUIT_GOMU_GOMU = registerFruit("item_devil_fruit_gomu_gomu");
   public static final Item ITEM_DEVIL_FRUIT_GORO_GORO = registerFruit("item_devil_fruit_goro_goro");
   public static final Item ITEM_DEVIL_FRUIT_GURA_GURA = registerFruit("item_devil_fruit_gura_gura");
   public static final Item ITEM_DEVIL_FRUIT_HIE_HIE = registerFruit("item_devil_fruit_hie_hie");
   public static final Item ITEM_DEVIL_FRUIT_HITO_HITO_DAIBUTSU = registerFruit("item_devil_fruit_hito_hito_daibutsu");
   public static final Item ITEM_DEVIL_FRUIT_HORO_HORO = registerFruit("item_devil_fruit_horo_horo");
   public static final Item ITEM_DEVIL_FRUIT_IMU = registerFruit("item_devil_fruit_imu");
   public static final Item ITEM_DEVIL_FRUIT_INU_INU_OKUCHI_NO_MAKAMI = registerFruit("item_devil_fruit_inu_inu_okuchi_no_makami");
   public static final Item ITEM_DEVIL_FRUIT_INU_INU_WOLF = registerFruit("item_devil_fruit_inu_inu_wolf");
   public static final Item ITEM_DEVIL_FRUIT_KAGE_KAGE = registerFruit("item_devil_fruit_kage_kage");
   public static final Item ITEM_DEVIL_FRUIT_KIRA_KIRA = registerFruit("item_devil_fruit_kira_kira");
   public static final Item ITEM_DEVIL_FRUIT_KIRO_KIRO = registerFruit("item_devil_fruit_kiro_kiro");
   public static final Item ITEM_DEVIL_FRUIT_MAGU_MAGU = registerFruit("item_devil_fruit_magu_magu");
   public static final Item ITEM_DEVIL_FRUIT_MERA_MERA = registerFruit("item_devil_fruit_mera_mera");
   public static final Item ITEM_DEVIL_FRUIT_MERO_MERO = registerFruit("item_devil_fruit_mero_mero");
   public static final Item ITEM_DEVIL_FRUIT_MOCHI_MOCHI = registerFruit("item_devil_fruit_mochi_mochi");
   public static final Item ITEM_DEVIL_FRUIT_MOKU_MOKU = registerFruit("item_devil_fruit_moku_moku");
   public static final Item ITEM_DEVIL_FRUIT_MORI_MORI = registerFruit("item_devil_fruit_mori_mori");
   public static final Item ITEM_DEVIL_FRUIT_NEKO_NEKO_LEOPARD = registerFruit("item_devil_fruit_neko_neko_leopard");
   public static final Item ITEM_DEVIL_FRUIT_NEKO_NEKO_SABERTIGER = registerFruit("item_devil_fruit_neko_neko_sabertiger");
   public static final Item ITEM_DEVIL_FRUIT_NIKYU_NIKYU = registerFruit("item_devil_fruit_nikyu_nikyu");
   public static final Item ITEM_DEVIL_FRUIT_OPE_OPE = registerFruit("item_devil_fruit_ope_ope");
   public static final Item ITEM_DEVIL_FRUIT_PIKA_PIKA = registerFruit("item_devil_fruit_pika_pika");
   public static final Item ITEM_DEVIL_FRUIT_RIKI_RIKI = registerFruit("item_devil_fruit_riki_riki");
   public static final Item ITEM_DEVIL_FRUIT_RYU_RYU_BRACHIOSAURUS = registerFruit("item_devil_fruit_ryu_ryu_brachiosaurus");
   public static final Item ITEM_DEVIL_FRUIT_RYU_RYU_PACHYCEPHALOSAURUS = registerFruit("item_devil_fruit_ryu_ryu_pachycephalosaurus");
   public static final Item ITEM_DEVIL_FRUIT_RYU_RYU_PTERANODON = registerFruit("item_devil_fruit_ryu_ryu_pteranodon");
   public static final Item ITEM_DEVIL_FRUIT_SORU_SORU = registerFruit("item_devil_fruit_soru_soru");
   public static final Item ITEM_DEVIL_FRUIT_SUNA_SUNA = registerFruit("item_devil_fruit_suna_suna");
   public static final Item ITEM_DEVIL_FRUIT_SUPA_SUPA = registerFruit("item_devil_fruit_supa_supa");
   public static final Item ITEM_DEVIL_FRUIT_TORI_TORI_PHOENIX = registerFruit("item_devil_fruit_tori_tori_phoenix");
   public static final Item ITEM_DEVIL_FRUIT_UO_UO_SEIRYU = registerFruit("item_devil_fruit_uo_uo_seiryu");
   public static final Item ITEM_DEVIL_FRUIT_USHI_USHI_GIRAFFE = registerFruit("item_devil_fruit_ushi_ushi_giraffe");
   public static final Item ITEM_DEVIL_FRUIT_UTA_UTA = registerFruit("item_devil_fruit_uta_uta");
   public static final Item ITEM_DEVIL_FRUIT_YAMI_YAMI = registerFruit("item_devil_fruit_yami_yami");
   public static final Item ITEM_DEVIL_FRUIT_ZUSHI_ZUSHI = registerFruit("item_devil_fruit_zushi_zushi");

   private MinepieceModItems() {}

   private static Item register(String name) {
      Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
      return Registry.register(BuiltInRegistries.ITEM, id,
         new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).stacksTo(64)));
   }

   private static Item registerManual(String name) {
      Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
      return Registry.register(BuiltInRegistries.ITEM, id,
         new net.mcreator.minepiece.item.RokushikiManualItem(
            new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).stacksTo(1)));
   }

   private static Item registerFruit(String name) {
      Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
      return Registry.register(BuiltInRegistries.ITEM, id,
         new DevilFruitItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id))));
   }


   private static Item registerBiscuit(){Identifier id=Identifier.fromNamespaceAndPath(NAMESPACE,"biscuit");return Registry.register(BuiltInRegistries.ITEM,id,new Item(new Item.Properties().setId(ResourceKey.create(Registries.ITEM,id)).food(new net.minecraft.world.food.FoodProperties.Builder().nutrition(2).saturationModifier(.1F).build())));}
   private static Item registerLightSword(){Identifier id=Identifier.fromNamespaceAndPath(NAMESPACE,"amano_murakumo");return Registry.register(BuiltInRegistries.ITEM,id,new net.mcreator.minepiece.item.LightSwordItem(new Item.Properties().setId(ResourceKey.create(Registries.ITEM,id))));}
   private static Item registerSword(String name, float damage, float speed) {
      Identifier id = Identifier.fromNamespaceAndPath(NAMESPACE, name);
      return Registry.register(BuiltInRegistries.ITEM, id,
         new Item(ToolMaterial.DIAMOND.applySwordProperties(
            new Item.Properties().setId(ResourceKey.create(Registries.ITEM, id)).stacksTo(1),
            damage, speed)));
   }

   /** Le simple chargement de la classe enregistre tous les items. */
   public static void init() {
      registerTab();
   }

   /** Onglet créatif « Mine Piece » qui expose tous les équipements + fruits du démon. */
   private static void registerTab() {
      List<Item> items = List.of(
         // Équipement
         ACE, CAT_CLAWS, CHAKRAM, CLOTHES_ACE_HELMET, CLOTHES_CROCODILE_CHESTPLATE, CLOTHES_FRANKY_CHESTPLATE,
         CLOTHES_LUFFY_BOOTS, CLOTHES_LUFFY_CHESTPLATE, CLOTHES_LUFFY_HELMET, CLOTHES_LUFFY_LEGGINGS,
         CLOTHES_MIHAWK_HELMET, CLOTHES_SABO_HELMET, CLOTHES_ZORO_BOOTS, CLOTHES_ZORO_CHESTPLATE,
         CLOTHES_ZORO_LEGGINGS, CUTLASS, DAISENSO, ENMA, HASSAIKAI, HOOK, ITEM_BULLET, JITTE, KIKOKU,
         KIRIBACHI, MEAT_ON_THE_BONE, MOGURA, MURAKUMOGIRI, PIPE, SENRIKU, SHUSUI, SPEAR, TONFA,
         TREASURE_CHEST, TREASURE_CHEST_LOCKED, UNIFORM_MARINE_1_BOOTS, UNIFORM_MARINE_1_CHESTPLATE,
         UNIFORM_MARINE_1_HELMET, UNIFORM_MARINE_1_LEGGINGS, WADO_ICHIMONJI, YORU,
         // Fruits du démon
         ITEM_DEVIL_FRUIT, ITEM_DEVIL_FRUIT_ARO_ARO, ITEM_DEVIL_FRUIT_BATTO_BATTO, ITEM_DEVIL_FRUIT_BISU_BISU,
         ITEM_DEVIL_FRUIT_BOMU_BOMU, ITEM_DEVIL_FRUIT_GOMU_GOMU, ITEM_DEVIL_FRUIT_GORO_GORO, ITEM_DEVIL_FRUIT_GURA_GURA,
         ITEM_DEVIL_FRUIT_HIE_HIE, ITEM_DEVIL_FRUIT_HITO_HITO_DAIBUTSU, ITEM_DEVIL_FRUIT_HORO_HORO, ITEM_DEVIL_FRUIT_IMU,
         ITEM_DEVIL_FRUIT_INU_INU_OKUCHI_NO_MAKAMI, ITEM_DEVIL_FRUIT_INU_INU_WOLF, ITEM_DEVIL_FRUIT_KAGE_KAGE,
         ITEM_DEVIL_FRUIT_KIRA_KIRA, ITEM_DEVIL_FRUIT_KIRO_KIRO, ITEM_DEVIL_FRUIT_MAGU_MAGU, ITEM_DEVIL_FRUIT_MERA_MERA,
         ITEM_DEVIL_FRUIT_MERO_MERO, ITEM_DEVIL_FRUIT_MOCHI_MOCHI, ITEM_DEVIL_FRUIT_MOKU_MOKU, ITEM_DEVIL_FRUIT_MORI_MORI,
         ITEM_DEVIL_FRUIT_NEKO_NEKO_LEOPARD, ITEM_DEVIL_FRUIT_NEKO_NEKO_SABERTIGER, ITEM_DEVIL_FRUIT_NIKYU_NIKYU,
         ITEM_DEVIL_FRUIT_OPE_OPE, ITEM_DEVIL_FRUIT_PIKA_PIKA, ITEM_DEVIL_FRUIT_RIKI_RIKI, ITEM_DEVIL_FRUIT_RYU_RYU_BRACHIOSAURUS,
         ITEM_DEVIL_FRUIT_RYU_RYU_PACHYCEPHALOSAURUS, ITEM_DEVIL_FRUIT_RYU_RYU_PTERANODON, ITEM_DEVIL_FRUIT_SORU_SORU,
         ITEM_DEVIL_FRUIT_SUNA_SUNA, ITEM_DEVIL_FRUIT_SUPA_SUPA, ITEM_DEVIL_FRUIT_TORI_TORI_PHOENIX, ITEM_DEVIL_FRUIT_UO_UO_SEIRYU,
         ITEM_DEVIL_FRUIT_USHI_USHI_GIRAFFE, ITEM_DEVIL_FRUIT_UTA_UTA, ITEM_DEVIL_FRUIT_YAMI_YAMI, ITEM_DEVIL_FRUIT_ZUSHI_ZUSHI
      );
      Registry.register(
         BuiltInRegistries.CREATIVE_MODE_TAB,
         Identifier.fromNamespaceAndPath(NAMESPACE, "minepiece_tab"),
         CreativeModeTab.builder(CreativeModeTab.Row.TOP, 0)
            .title(Component.literal("Mine Piece"))
            .icon(() -> new ItemStack(ITEM_DEVIL_FRUIT_MERA_MERA))
            .displayItems((params, output) -> items.forEach(item -> output.accept(item)))
            .build()
      );
   }
}
