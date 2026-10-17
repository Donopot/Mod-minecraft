package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import java.util.Set;
import net.mcreator.minepiece.init.MinepieceModItems;
import net.mcreator.minepiece.network.DevilFruitNetworking;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.mcreator.minepiece.network.MinepieceModVariables.PlayerVariables;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Transformation (forme hybride) des Fruits Zoan + Gomu.
 *
 * <p>Fidèle aux procédures ver15 {@code KeyTransformOnKeyPressedProcedure}/{@code ChangeGearProcedure} :
 * chaque fruit applique ses propres bonus (Force/Résistance/Vitesse, calibrés sur {@code attack_damage}/
 * {@code armor_toughness} du source) et ses propres particules de transformation (glace pour Okuchi, feu
 * pour Daibutsu, flammes bleue+jaune pour Phoenix). Le changement de modèle/skin du joueur (loup/girafe/…)
 * reste un suivi séparé client-only.</p>
 *
 * <p>CORE-013 : les bonus sont appliqués via des <b>modificateurs d'attributs identifiés</b> (UUID de
 * ressource stable), ajoutés/retirés de façon réversible. Fin de forme ne retire que les modificateurs
 * du fruit, sans toucher aux potions ni aux autres sources.</p>
 */
public final class DevilFruitTransformation {

   /** Identifiants des modificateurs d'attributs de transformation (réversibles, CORE-013). */
   private static final Identifier MOD_STRENGTH = Identifier.fromNamespaceAndPath("minepiece", "form_strength");
   private static final Identifier MOD_RESISTANCE = Identifier.fromNamespaceAndPath("minepiece", "form_resistance");
   private static final Identifier MOD_SPEED = Identifier.fromNamespaceAndPath("minepiece", "form_speed");
   private static final Identifier MOD_SCALE = Identifier.fromNamespaceAndPath("minepiece", "form_scale");

   /**
    * Tag des Fruits pouvant prendre une forme hybride (FRUIT-*-01/FRUIT-*-02).
    *
    * <p>La classification est portée par les <b>tags</b> et non par une liste codée en dur : c'est ce
    * qui rend la famille d'un Fruit observable et vérifiable. Le tag {@code minepiece:transformable}
    * remplace l'ancienne liste {@code TRANSFORMABLE} (13 Fruits) et couvre explicitement Gomu Gomu,
    * qui est un <b>Paramecia</b> canonique capable de transformation — ver15 le classait ZOAN parce
    * qu'il regroupait les transformables, ce qui mélangeait famille et capacité.</p>
    */
   public static final TagKey<Item> TAG_TRANSFORMABLE =
      TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("minepiece", "transformable"));

   private DevilFruitTransformation() {
   }

   /** Bascule la forme hybride. Retourne vrai si un changement a eu lieu. */
   public static boolean toggle(ServerPlayer player) {
      PlayerVariables vars = MinepieceModVariables.get(player);
      ItemStack fruit = vars.PlayerDevilFruit;
      if(ZoanForms.handles(fruit))return ZoanForms.cycle(player);
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_GOMU_GOMU)) return GomuGears.cycle(player,false);
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO) || GoroAmaru.has(player)) return GoroAmaru.toggle(player);
      if (fruit.isEmpty()) {
         player.sendSystemMessage(Component.literal("§c").append(Component.translatable("minepiece.message.no_fruit")), false);
         return false;
      }
      // CORE-007 : suppression par l'eau (mer) → pas de transformation.
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         player.sendSystemMessage(Component.literal("§c").append(Component.translatable("minepiece.message.sea_suppressed")), false);
         return false;
      }
      if(fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA)) return FirePropulsion.toggle(player);
      if (!fruit.is(TAG_TRANSFORMABLE)) {
         player.sendSystemMessage(Component.literal("§e").append(Component.translatable("minepiece.message.no_hybrid")), false);
         return false;
      }

      if (vars.player_form == 0.0) {
         // ESPACE NÉCESSAIRE (FRUIT-HITO_HITO_DAIBUTSU-03) : une forme géante ne se déploie pas
         // dans un couloir de deux blocs. Le refus est prononcé AVANT toute modification d'état,
         // et il est expliqué — pas subi.
         double formeScale = scaleMultiplier(fruit.getItem());
         if (formeScale > 1.5 && !GiantForm.hasRoom(player, GiantForm.clampedScale(formeScale))) {
            player.sendSystemMessage(Component.literal(
               "§cPas assez de place pour déployer la forme géante : il faut de l'air autour de vous."), false);
            return false;
         }
         if (formeScale > 1.5 && GiantForm.begin(player, GiantForm.clampedScale(formeScale)) == 0.0) return false;
         vars.player_form = 1.0;
         applyForm(player, fruit.getItem());
         transformationBurst(player, fruit.getItem());
         player.level().playSound(null, player.blockPosition(), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.8F, 1.2F);
         player.sendSystemMessage(Component.literal("§d§l").append(Component.translatable("minepiece.message.transformed")), false);
      } else {
         vars.player_form = 0.0;
         GiantForm.end(player);       // les modificateurs propres à la forme géante d'abord
         removeForm(player);
         player.level().playSound(null, player.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.8F, 0.8F);
         player.sendSystemMessage(Component.literal("§7").append(Component.translatable("minepiece.message.reverted")), false);
      }
      syncToClient(player, vars);
      return true;
   }

   /**
    * Révoque la forme hybride : attributs de forme + vol + état + sync.
    *
    * <p>Utilisé par le reset complet du Fruit (FRUIT-RESET-01). La révocation est
    * <b>inconditionnelle</b> : elle retire les modificateurs même si {@code player_form} vaut déjà 0,
    * ce qui rattrape un état désynchronisé. Elle ne touche ni aux potions ni aux autres sources
    * (les modificateurs sont identifiés par UUID de ressource, CORE-013).</p>
    */
   /**
    * Signature de forme d'un Fruit : ce qui, côté serveur, identifie la forme dans laquelle il
    * transforme (échelle, effets, particules). Sert à prouver que deux Fruits ne partagent pas
    * involontairement la même forme (FRUIT-INU_INU_OKUCHI_NO_MAKAMI-03).
    */
   public static String formSignatureFor(Item fruit) {
      if(fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO)return "Amaru:scale=2.5,body=2.4x6.5,eye=5.5,combatBonus=0,flight=owned";
      if (!fruit.builtInRegistryHolder().is(TAG_TRANSFORMABLE)) {
         return "";
      }
      boolean gomu = fruit == net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_GOMU_GOMU;
      return "echelle=" + scaleMultiplier(fruit)
         + ",force=" + strengthAmplifier(fruit, gomu)
         + ",resistance=" + resistanceAmplifier(fruit, gomu)
         + ",vol=" + isFlyingFruit(fruit);
   }

   public static void revertForm(ServerPlayer player) {
      if(ZoanForms.has(player)||MinepieceModVariables.get(player).zoanFlightGranted||ZoanForms.handles(DevilFruitPowerLogic.getDevilFruit(player))){ZoanForms.end(player);return;}
      GomuGears.end(player);
      if (MinepieceModVariables.get(player).amaruFlightGranted || GoroAmaru.has(player) || GoroAmaru.scaled(player) || MinepieceModVariables.get(player).PlayerDevilFruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO)) {GoroAmaru.end(player);return;}
      PlayerVariables vars = MinepieceModVariables.get(player);
      vars.player_form = 0.0;
      GiantForm.end(player);
      removeForm(player);
      syncToClient(player, vars);
   }

   /** Rebuild transient modifiers after login/respawn, without replaying the transformation burst. */
   public static void restorePersistedForm(ServerPlayer player) {
      if(MinepieceModVariables.get(player).zoanFlightGranted||ZoanForms.handles(DevilFruitPowerLogic.getDevilFruit(player))){ZoanForms.end(player);return;}
      if(MinepieceModVariables.get(player).gomuFlightGranted||DevilFruitPowerLogic.getDevilFruit(player).is(MinepieceModItems.ITEM_DEVIL_FRUIT_GOMU_GOMU)){GomuGears.end(player);MinepieceModVariables.get(player).player_form=0;return;}
      if (MinepieceModVariables.get(player).amaruFlightGranted || MinepieceModVariables.get(player).PlayerDevilFruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO) || GoroAmaru.scaled(player) || GoroAmaru.has(player)) {GoroAmaru.end(player);return;}
      PlayerVariables vars = MinepieceModVariables.get(player);
      int remaining = vars.formRemainingTicks;
      GiantForm.end(player);
      removeForm(player);
      if (vars.player_form != 1.0 || vars.PlayerDevilFruit.isEmpty()
            || !vars.PlayerDevilFruit.is(TAG_TRANSFORMABLE) || DevilFruitPowerLogic.isSuppressed(player)) {
         vars.player_form = 0.0;
         return;
      }
      double scale = GiantForm.clampedScale(scaleMultiplier(vars.PlayerDevilFruit.getItem()));
      if (scale > 1.5 && !GiantForm.restore(player, scale, remaining)) {
         vars.player_form = 0.0;
         return;
      }
      applyForm(player, vars.PlayerDevilFruit.getItem());
   }

   /** Synchronise l'état de transformation (forme + fruit) au client pour le rendu du modèle. */
   private static void syncToClient(ServerPlayer player, PlayerVariables vars) {
      String fruitId = vars.PlayerDevilFruit.isEmpty()
         ? ""
         : BuiltInRegistries.ITEM.getKey(vars.PlayerDevilFruit.getItem()).toString();
      DevilFruitNetworking.sendTransformState(player, vars.player_form, fruitId);
      DevilFruitNetworking.sendSyncState(player);
   }

   private static void applyForm(ServerPlayer player, Item fruit) {
      boolean gomu = fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_GOMU_GOMU;
      int strAmp = strengthAmplifier(fruit, gomu);
      int resAmp = resistanceAmplifier(fruit, gomu);
      // STRENGTH proxy : +3 dégâts par niveau (amp 0 → +3), fidèle à MobEffects.STRENGTH.
      addModifier(player, Attributes.ATTACK_DAMAGE, MOD_STRENGTH, 3.0 * (strAmp + 1));
      // RESISTANCE proxy : armure (+4 par niveau), approximation documentée de la réduction de dégâts.
      addModifier(player, Attributes.ARMOR, MOD_RESISTANCE, 4.0 * resAmp);
      if (!gomu) {
         // SPEED proxy : +20 % de vitesse.
         addModifier(player, Attributes.MOVEMENT_SPEED, MOD_SPEED, 0.2, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
      }
      // CORE-019 : dimensions + caméra — échelle via Attributes.SCALE (Mojang), fidèle à la table SIZE de ver15
      // (KeyTransformOnKeyPressedProcedure, taille ×0.5 pour les joueurs). getScale() ajuste hitbox, œil et caméra.
      // Le plafond de taille est appliqué ICI, au moment où l'échelle est réellement posée : la table
      // ci-dessus n'est qu'une intention, et une intention non bornée produirait un géant démesuré.
      double scale = GiantForm.clampedScale(scaleMultiplier(fruit));
      if (scale != 1.0) {
         addModifier(player, Attributes.SCALE, MOD_SCALE, scale - 1.0);
      }
      player.refreshDimensions();
      // CORE-019 : mobilité — vol pour les fruits ailés (Phénix, Ptéranodon, Chauve-souris).
      grantFlight(player, fruit);
   }

   /** Échelle du joueur (1.0 = normale) par fruit, fidèle à la table SIZE de ver15 (×0.5 pour les joueurs). */
   private static double scaleMultiplier(Item fruit) {
      if (fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_HITO_HITO_DAIBUTSU) {
         return 3.5; // 5.0 × 0.5 → +2.5
      }
      if (fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_USHI_USHI_GIRAFFE
          || fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_UO_UO_SEIRYU
          || fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_RYU_RYU_PACHYCEPHALOSAURUS
          || fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_RYU_RYU_BRACHIOSAURUS) {
         return 1.75; // 1.5 × 0.5 → +0.75
      }
      if (fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_NEKO_NEKO_LEOPARD
          || fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_WOLF
          || fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_OKUCHI_NO_MAKAMI) {
         return 1.5; // 1.0 × 0.5 → +0.5
      }
      return 1.0;
   }

   /** Fruits ailés (vol en forme hybride). */
   private static boolean isFlyingFruit(Item fruit) {
      // Mera Mera : vol par PROPULSION uniquement (FRUIT-MERA_MERA-04) — pas de vol stationnaire,
         // la poussée est fournie par FirePropulsion et s'arrête sur obstacle / suppression / énergie.
         return fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_TORI_TORI_PHOENIX
          || fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_RYU_RYU_PTERANODON
          || fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_BATTO_BATTO
          || fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA;
   }

   /** Accorde le vol (mayfly) sans toucher aux joueurs créatifs/spectateurs. */
   private static void grantFlight(ServerPlayer player, Item fruit) {
      if (!isFlyingFruit(fruit)) {
         return;
      }
      Abilities abilities = player.getAbilities();
      if (!abilities.instabuild && !player.isSpectator() && !abilities.mayfly) {
         abilities.mayfly = true;
         abilities.flying = true;
         player.onUpdateAbilities();
      }
   }

   /** Révoque le vol accordé par la transformation (préserve créatif/spectateur). */
   private static void revokeFlight(ServerPlayer player) {
      Abilities abilities = player.getAbilities();
      if (!abilities.instabuild && !player.isSpectator() && abilities.mayfly) {
         abilities.mayfly = false;
         abilities.flying = false;
         player.onUpdateAbilities();
      }
   }

   private static void addModifier(ServerPlayer player, Holder<Attribute> attr, Identifier id, double amount) {
      addModifier(player, attr, id, amount, AttributeModifier.Operation.ADD_VALUE);
   }

   private static void addModifier(ServerPlayer player, Holder<Attribute> attr, Identifier id, double amount,
                                   AttributeModifier.Operation operation) {
      AttributeInstance inst = player.getAttribute(attr);
      if (inst == null) {
         return;
      }
      inst.removeModifier(id); // idempotent
      inst.addTransientModifier(new AttributeModifier(id, amount, operation));
   }

   private static void removeForm(ServerPlayer player) {
      removeModifier(player, Attributes.ATTACK_DAMAGE, MOD_STRENGTH);
      removeModifier(player, Attributes.ARMOR, MOD_RESISTANCE);
      removeModifier(player, Attributes.MOVEMENT_SPEED, MOD_SPEED);
      removeModifier(player, Attributes.SCALE, MOD_SCALE);
      player.refreshDimensions();
      revokeFlight(player);
   }

   private static void removeModifier(ServerPlayer player, Holder<Attribute> attr, Identifier id) {
      AttributeInstance inst = player.getAttribute(attr);
      if (inst != null) {
         inst.removeModifier(id);
      }
   }

   /** Force (amplificateur STRENGTH) — calée sur {@code attack_damage} du source (0.1 par défaut, 0.2 fruits lourds). */
   private static int strengthAmplifier(Item fruit, boolean gomu) {
      if (gomu) return 0;
      if (fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_HITO_HITO_DAIBUTSU
          || fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_OKUCHI_NO_MAKAMI
          || fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_UO_UO_SEIRYU
          || fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_RYU_RYU_PACHYCEPHALOSAURUS
          || fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_RYU_RYU_BRACHIOSAURUS) {
         return 2;
      }
      return 1;
   }

   /** Résistance (amplificateur RESISTANCE) — calée sur {@code armor_toughness} du source (2.0 par défaut, 4.0 fruits lourds). */
   private static int resistanceAmplifier(Item fruit, boolean gomu) {
      if (gomu) return 2;
      if (fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_UO_UO_SEIRYU
          || fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_RYU_RYU_PACHYCEPHALOSAURUS
          || fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_RYU_RYU_BRACHIOSAURUS) {
         return 2;
      }
      return 1;
   }

   /** Particules de transformation propres au fruit (fidèle à KeyTransformOnKeyPressedProcedure). */
   private static void transformationBurst(ServerPlayer player, Item fruit) {
      ServerLevel level = (ServerLevel) player.level();
      double x = player.getX();
      double y = player.getY() + player.getBbHeight() * 0.5;
      double z = player.getZ();
      double w = player.getBbWidth();

      // Fruits avec effet de glace → Okuchi no Makami.
      if (fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_OKUCHI_NO_MAKAMI) {
         level.sendParticles(MinepieceModParticles.ICE, x, y, z, 30, w * 0.5, player.getBbHeight() * 0.5, w * 0.5, 0.1);
      } else if (fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_HITO_HITO_DAIBUTSU) {
         level.sendParticles(MinepieceModParticles.FLAME, x, y, z,30, w * 0.5, player.getBbHeight() * 0.5, w * 0.5, 0.0);
      } else if (fruit == MinepieceModItems.ITEM_DEVIL_FRUIT_TORI_TORI_PHOENIX) {
         level.sendParticles(MinepieceModParticles.FLAME_BLUE, x, y, z, 30, w, player.getBbHeight(), w, 0.5);
         level.sendParticles(MinepieceModParticles.FLAME, x, y, z, 15, w, player.getBbHeight(), w, 0.5);
      } else {
         level.sendParticles(MinepieceModParticles.BIG_SMOKE, x, y, z, 45, w * 0.5, player.getBbHeight() * 0.5, w * 0.5, 0.0);
      }
   }
}
