package net.mcreator.minepiece.item;

import net.mcreator.minepiece.init.MinepieceModItems;
import net.mcreator.minepiece.init.MinepieceModMobEffects;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.mcreator.minepiece.network.MinepieceModVariables.PlayerVariables;
import net.mcreator.minepiece.procedures.DevilFruitPowerLogic;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shim Fabric 26.3 d'un Fruit du Démon Mine Piece (ver15).
 *
 * Fruit comestible (durée 16 ticks, nourriture 4/0.3, toujours comestible, rareté épique).
 * À la fin de la consommation, applique la règle One Piece :
 *   - fruit générique (`item_devil_fruit`) → réinitialise le pouvoir ;
 *   - 1er fruit spécifique → fixe `PlayerDevilFruit` + nausée + aide ;
 *   - 2e fruit (déjà un pouvoir) → mort.
 * Les pouvoirs eux-mêmes (skills/transformations) sont une couche séparée, non encore portée.
 */
public class DevilFruitItem extends Item {
   private static final Logger LOGGER = LoggerFactory.getLogger("MinePieceDevilFruit");

   public DevilFruitItem(Item.Properties props) {
      super(props.stacksTo(1).rarity(Rarity.EPIC).food(new FoodProperties(4, 0.3F, true)));
   }

   @Override
   public int getUseDuration(ItemStack stack, LivingEntity entity) {
      return 16;
   }

   @Override
   public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
      if (!level.isClientSide() && entity instanceof ServerPlayer player) {
         PlayerVariables vars = MinepieceModVariables.get(player);
         ItemStack current = vars.PlayerDevilFruit;
         boolean hasFruit = current != null && !current.isEmpty();

         if (stack.getItem() == MinepieceModItems.ITEM_DEVIL_FRUIT) {
            // fruit générique : reset complet (forme, attributs, sélection) — CORE-008 / FRUIT-RESET-01
            DevilFruitPowerLogic.resetDevilFruitPowers(player);
            player.sendSystemMessage(Component.literal("§7").append(
               Component.translatable("minepiece.message.power_reset")), false);
         } else if (!hasFruit) {
            // 1er fruit : fixe le pouvoir + malédiction de la mer + énergie pleine
            vars.PlayerDevilFruit = stack.copy();
            vars.UseDevilAbilities = true;
            vars.PlayerEnergy = vars.PlayerEnergyMAX;
            player.addEffect(new MobEffectInstance(MinepieceModMobEffects.DEVIL_FRUIT_USER, -1, 0, false, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 100, 0, true, true));
            LOGGER.info("{} a mangé le fruit {} — pouvoir accordé, énergie {}.", player.getName().getString(), stack.getItem(), (int) vars.PlayerEnergy);
            player.sendSystemMessage(Component.literal("§3§l").append(
               Component.translatable("minepiece.message.fruit_eaten")), false);
            vars.syncPlayerVariables(player);
         } else {
            // 2e fruit : mort (règle One Piece)
            DevilFruitPowerLogic.resetDevilFruitPowers(player);
            player.sendSystemMessage(Component.literal("§4§l").append(
               Component.translatable("minepiece.message.two_fruits")), false);
            player.hurt(player.damageSources().genericKill(), Float.MAX_VALUE);
         }
      }
      return super.finishUsingItem(stack, level, entity);
   }
}
