package br.com.oneminepiece.opworldlike.loot;

import java.util.List;
import java.util.Set;
import net.fabricmc.fabric.api.loot.v3.LootTableEvents;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProvider;

/**
 * Port Fabric 26.3 de NaturalLootEvents (Forge 1.20.1).
 *
 * Ajoute du loot Mine Piece aux coffres de structures vanilla. LootTableLoadEvent → LootTableEvents.MODIFY.
 * NOTE : la condition custom `RegionalLootChanceCondition` (multiplicateur régional) est remplacée par la
 * condition vanilla `LootItemRandomChanceCondition` (chance plate) — le scaling régional sera rétabli quand
 * `RegionalLootChanceCondition` sera porté (MapCodec 26.3).
 */
public final class NaturalLootEvents {
   private static final Set<Identifier> TARGETS = Set.of(
      Identifier.parse("minecraft:chests/shipwreck_treasure"),
      Identifier.parse("minecraft:chests/shipwreck_supply"),
      Identifier.parse("minecraft:chests/buried_treasure"),
      Identifier.parse("minecraft:chests/village/village_weaponsmith"),
      Identifier.parse("minecraft:chests/village/village_armorer"),
      Identifier.parse("minecraft:chests/desert_pyramid"),
      Identifier.parse("minecraft:chests/simple_dungeon"),
      Identifier.parse("minecraft:chests/abandoned_mineshaft"),
      Identifier.parse("minecraft:chests/underwater_ruin_big"),
      Identifier.parse("minecraft:chests/underwater_ruin_small")
   );

   private NaturalLootEvents() {
   }

   public static void register() {
      LootTableEvents.MODIFY.register((key, tableBuilder, source, registries) -> {
         Identifier name = key.identifier();
         if (isStructureChestLootTable(name)) {
            addBoxPool(tableBuilder, "minepiece:treasure_chest", 0.08F);
            addBoxPool(tableBuilder, "minepiece:treasure_chest_locked", 0.02F);
            addEquipmentPool(tableBuilder);
         }

         if (TARGETS.contains(name)) {
            LootPool.Builder pool = LootPool.lootPool()
               .setRolls(Holder.direct((ContextIntProvider) new ConstantValue(1)))
               .when(Holder.direct(LootItemRandomChanceCondition.randomChance(0.22F).build()));
            add(pool, "minepiece:meat_on_the_bone", 55);
            add(pool, "minepiece:item_bullet", 35);
            add(pool, "minepiece:cutlass", 18);
            tableBuilder.withPool(pool);
         }
      });
   }

   private static boolean isStructureChestLootTable(Identifier name) {
      String path = name.getPath();
      return path.startsWith("chests/") || path.contains("/chests/");
   }

   private static void addBoxPool(net.minecraft.world.level.storage.loot.LootTable.Builder tableBuilder, String itemId, float chance) {
      Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
      if (item != null && item != Items.AIR) {
         LootPool.Builder pool = LootPool.lootPool()
            .setRolls(Holder.direct((ContextIntProvider) new ConstantValue(1)))
            .when(Holder.direct(LootItemRandomChanceCondition.randomChance(chance).build()))
            .add(LootItem.lootTableItem(item));
         tableBuilder.withPool(pool);
      }
   }

   private static void addEquipmentPool(net.minecraft.world.level.storage.loot.LootTable.Builder tableBuilder) {
      if (EquipmentLootConfig.enabled()) {
         List<Item> items = EquipmentLootCatalog.items();
         if (!items.isEmpty()) {
            LootPool.Builder pool = LootPool.lootPool()
               .setRolls(Holder.direct((ContextIntProvider) new ConstantValue(1)))
               .when(Holder.direct(LootItemRandomChanceCondition.randomChance((float) EquipmentLootConfig.chestDropChance()).build()));

            for (Item item : items) {
               pool.add(LootItem.lootTableItem(item));
            }

            tableBuilder.withPool(pool);
         }
      }
   }

   private static void add(LootPool.Builder pool, String itemId, int weight) {
      Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
      if (item != null && item != Items.AIR) {
         pool.add(LootItem.lootTableItem(item).setWeight(weight));
      }
   }
}
