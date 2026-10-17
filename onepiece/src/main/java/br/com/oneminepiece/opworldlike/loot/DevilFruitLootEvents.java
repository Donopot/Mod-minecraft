package br.com.oneminepiece.opworldlike.loot;

import com.mojang.serialization.MapCodec;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

/**
 * Distribution des Fruits du Démon en survie (CORE-023).
 *
 * <p>Injecte un tirage de Fruit pondéré par rareté dans les coffres de structures vanilla ({@code chests/…}).
 * Deux garde-fous :
 * <ul>
 *   <li><b>Rareté</b> : poids par palier (légendaire / rare / commun), configurables ;</li>
 *   <li><b>Unicité</b> : une fonction de loot écarte un Fruit déjà possédé par un joueur — la
 *       <b>réincarnation</b> est donc automatique (dès que le propriétaire perd le Fruit, il redevient
 *       tirable). Dérivé des variables joueur persistées (CORE-003), donc conservé après mort/redémarrage.</li>
 * </ul>
 */
public final class DevilFruitLootEvents {

   private static final String FRUIT_PREFIX = "item_devil_fruit";

   // Paliers de rareté (suffixe d'ID de fruit).
   private static final Set<String> LEGENDARY = Set.of(
      "goro_goro", "gura_gura", "hie_hie", "hito_hito_daibutsu", "imu", "inu_inu_okuchi_no_makami",
      "kage_kage", "magu_magu", "mera_mera", "mochi_mochi", "mori_mori", "nikyu_nikyu", "ope_ope",
      "pika_pika", "suna_suna", "tori_tori_phoenix", "uo_uo_seiryu", "uta_uta", "yami_yami", "zushi_zushi"
   );
   private static final Set<String> RARE = Set.of(
      "aro_aro", "batto_batto", "bisu_bisu", "horo_horo", "inu_inu_wolf", "kira_kira", "kiro_kiro",
      "mero_mero", "moku_moku", "neko_neko_leopard", "neko_neko_sabertiger", "riki_riki",
      "ryu_ryu_brachiosaurus", "ryu_ryu_pachycephalosaurus", "ryu_ryu_pteranodon", "soru_soru",
      "supa_supa", "ushi_ushi_giraffe"
   );

   private DevilFruitLootEvents() {
   }

   public static void register() {
      // Type de fonction de loot (unicité) — enregistré pour la cohérence du codec.
      Registry.register(BuiltInRegistries.LOOT_FUNCTION_TYPE,
         Identifier.fromNamespaceAndPath("minepiece", "fruit_available"), FruitAvailableFunction.CODEC);

      LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
         if (!DevilFruitLootConfig.enabled()) {
            return;
         }
         if (isStructureChest(key.identifier())) {
            addFruitPool(tableBuilder);
         }
      });

      // Réincarnation (CORE-023) : la mort d'un utilisateur libère son Fruit → il redevient lootable.
      ServerLivingEntityEvents.AFTER_DEATH.register((entity, damageSource) -> {
         if (DevilFruitLootConfig.reincarnationOnDeath() && entity instanceof ServerPlayer player) {
            net.mcreator.minepiece.procedures.DevilFruitPowerLogic.clearDevilFruit(player);
         }
      });
   }

   private static boolean isStructureChest(Identifier name) {
      String path = name.getPath();
      return path.startsWith("chests/") || path.contains("/chests/");
   }

   private static void addFruitPool(LootTable.Builder tableBuilder) {
      List<Item> fruits = allFruits();
      if (fruits.isEmpty()) {
         return;
      }
      LootPool.Builder pool = LootPool.lootPool()
         .setRolls(Holder.direct((ContextIntProvider) new ConstantValue(1)))
         .when(Holder.direct(LootItemRandomChanceCondition.randomChance((float) DevilFruitLootConfig.chestDropChance()).build()));

      for (Item fruit : fruits) {
         pool.add(LootItem.lootTableItem(fruit)
            .setWeight(weightFor(BuiltInRegistries.ITEM.getKey(fruit).getPath()))
            .apply(Holder.direct(FruitAvailableFunction.INSTANCE)));
      }

      tableBuilder.withPool(pool);
   }

   /** Tous les items de Fruit du Démon enregistrés (préfixe {@code minepiece:item_devil_fruit}). */
   private static List<Item> allFruits() {
      List<Item> fruits = new ArrayList<>();
      for (Item item : BuiltInRegistries.ITEM) {
         Identifier id = BuiltInRegistries.ITEM.getKey(item);
         if (id != null && "minepiece".equals(id.getNamespace()) && id.getPath().startsWith(FRUIT_PREFIX)) {
            fruits.add(item);
         }
      }
      return fruits;
   }

   /** Poids de rareté selon le palier du fruit. */
   private static int weightFor(String path) {
      String suffix = path.substring(FRUIT_PREFIX.length());
      if (suffix.startsWith("_")) {
         suffix = suffix.substring(1);
      }
      if (LEGENDARY.contains(suffix)) {
         return DevilFruitLootConfig.weightLegendary();
      }
      if (RARE.contains(suffix)) {
         return DevilFruitLootConfig.weightRare();
      }
      return DevilFruitLootConfig.weightCommon();
   }

   /**
    * Fonction de loot (unicité) : écarte le Fruit si un joueur le possède déjà. Sans état — la disponibilité
    * est recalculée à chaque tirage, ce qui rend la réincarnation automatique.
    */
   public static final class FruitAvailableFunction implements LootItemFunction {
      public static final FruitAvailableFunction INSTANCE = new FruitAvailableFunction();
      public static final MapCodec<FruitAvailableFunction> CODEC = MapCodec.unit(INSTANCE);

      private FruitAvailableFunction() {
      }

      @Override
      public MapCodec<? extends LootItemFunction> codec() {
         return CODEC;
      }

      @Override
      public ItemStack apply(ItemStack stack, LootContext context) {
         if (stack.isEmpty() || !DevilFruitLootConfig.unique()) {
            return stack;
         }
         Set<Item> owned = MinepieceModVariables.ownedFruitItems();
         return owned.contains(stack.getItem()) ? ItemStack.EMPTY : stack;
      }
   }
}
