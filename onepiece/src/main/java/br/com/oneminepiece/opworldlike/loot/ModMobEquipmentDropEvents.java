package br.com.oneminepiece.opworldlike.loot;

import java.util.List;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ModMobEquipmentDropEvents {
   private static final ModMobEquipmentDropEvents INSTANCE = new ModMobEquipmentDropEvents();

   private ModMobEquipmentDropEvents() {
   }

   public static void register() {
      ServerLivingEntityEvents.AFTER_DEATH.register(INSTANCE::onAfterDeath);
   }

   private void onAfterDeath(LivingEntity entity, DamageSource source) {
      if (EquipmentLootConfig.enabled() && source.getEntity() instanceof ServerPlayer) {
         if (!entity.level().isClientSide()) {
            Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
            if (id != null && "minepiece".equals(id.getNamespace())) {
               RandomSource random = entity.getRandom();
               this.rollConfiguredDrop(entity, random, EquipmentLootConfig.mmnmLeadDropChance(), EquipmentLootConfig.mmnmLeadDropItem());
               this.rollConfiguredDrop(entity, random, EquipmentLootConfig.mmnmCharacterCreatorDropChance(), EquipmentLootConfig.mmnmCharacterCreatorDropItem());
               double chance = EquipmentLootConfig.modMobDropChance();
               if (entity.level() instanceof ServerLevel level) {
                  chance *= RegionalLootMultipliers.multiplierAt(level, entity.getX(), entity.getZ());
               }

               if (!(random.nextDouble() > Math.min(1.0, chance))) {
                  List<Item> items = EquipmentLootCatalog.items();
                  if (!items.isEmpty()) {
                     Item item = items.get(random.nextInt(items.size()));
                     entity.level().addFreshEntity(new ItemEntity(entity.level(), entity.getX(), entity.getY() + 0.5, entity.getZ(), new ItemStack(item)));
                  }
               }
            }
         }
      }
   }

   private void rollConfiguredDrop(LivingEntity entity, RandomSource random, double chance, String itemId) {
      if (!(chance <= 0.0) && !(random.nextDouble() > chance)) {
         Item item = this.resolveItem(itemId);
         if (item != null) {
            entity.level().addFreshEntity(new ItemEntity(entity.level(), entity.getX(), entity.getY() + 0.5, entity.getZ(), new ItemStack(item)));
         }
      }
   }

   private Item resolveItem(String itemId) {
      try {
         Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(itemId));
         return item != null && item != Items.AIR ? item : null;
      } catch (RuntimeException ignored) {
         return null;
      }
   }
}
