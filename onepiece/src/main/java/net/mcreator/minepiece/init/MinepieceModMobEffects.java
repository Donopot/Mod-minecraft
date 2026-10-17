package net.mcreator.minepiece.init;

import net.mcreator.minepiece.potion.DevilFruitUserMobEffect;
import net.mcreator.minepiece.potion.SeaPowerMobEffect;
import net.mcreator.minepiece.potion.MamaraganMobEffect;
import net.mcreator.minepiece.potion.SimpleMarkerMobEffect;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Shim Fabric 26.3 de {@code MinepieceModMobEffects} (ver15) — effets référencés par la couche pouvoir.
 * Les effets de combat/cooldown/mobilité sont des marqueurs simples (leur logique dédiée — fly, double
 * jump — reste différée) ; {@code DEVIL_FRUIT_USER} et {@code SEA_POWER} ont leur vraie logique.
 */
public final class MinepieceModMobEffects {
   public static final String NAMESPACE = "minepiece";

   public static Holder<MobEffect> DEVIL_FRUIT_USER;
   public static Holder<MobEffect> SEA_POWER;
   public static Holder<MobEffect> SKILL;
   public static Holder<MobEffect> FLY_EFFECT;
   public static Holder<MobEffect> DOUBLE_JUMP_EFFECT;
   public static Holder<MobEffect> COOLDOWN_TIME;
   public static Holder<MobEffect> COOLDOWN_TIME_SECOND;
   public static Holder<MobEffect> COOLDOWN_TIME_FLY;
   public static Holder<MobEffect> COOLDOWN_TIME_BACK_STEP;
   public static Holder<MobEffect> COOLDOWN_TIME_COMBAT;
   public static Holder<MobEffect> ARMAMENT_HAKI;
   public static Holder<MobEffect> OBSERVATION_HAKI;
   public static Holder<MobEffect> SOVEREIGN_HAKI;
   public static Holder<MobEffect> GEAR;
   public static Holder<MobEffect> MAMARAGAN;
   /** Rendu de la pétrification (FRUIT-MERO_MERO-04) : couleur pierre, sans effet mécanique propre. */
   public static Holder<MobEffect> PETRIFICATION;
   public static Holder<MobEffect> B04_STONE;
   /** Rendu du voile souverain (FRUIT-IMU-06) : marqueur propre au passif, sans effet externe. */
   public static Holder<MobEffect> SOVEREIGN_VEIL;
   /** Rendu de la défense de glace (FRUIT-INU_INU_OKUCHI_NO_MAKAMI-05) : marqueur propre. */
   public static Holder<MobEffect> ICE_DEFENSE;
   /** Victime dont l'ombre a ete prise (FRUIT-KAGE_KAGE-03) : marqueur propre, jamais un effet vanille. */
   public static Holder<MobEffect> SHADOWLESS;
   /** Corps de diamant (FRUIT-KIRA_KIRA-03) : marqueur propre, jamais un effet vanille. */
   public static Holder<MobEffect> DIAMOND_BODY;
   /** Poids modifie (FRUIT-KIRO_KIRO-03) : marqueur propre. */
   public static Holder<MobEffect> WEIGHT_STATE;
   /** Cible collée par le mochi (FRUIT-MOCHI_MOCHI-04) : marqueur propre. */
   public static Holder<MobEffect> STICKY;
   /** Cible capturée dans la fumée (FRUIT-MOKU_MOKU-05) : marqueur propre. */
   public static Holder<MobEffect> SMOKE_CAPTURED;
   /** Cible enracinée par Mori Mori (FRUIT-MORI_MORI-03) : marqueur propre. */
   public static Holder<MobEffect> ROOTED;
   public static Holder<MobEffect> FROZEN;
   /** ROOM d'Ope Ope ouverte (FRUIT-OPE_OPE-03) : marqueur porté par le lanceur. */
   public static Holder<MobEffect> ROOM_BOUND;
   /** Cible temporairement sectionnée (FRUIT-OPE_OPE-05) : marqueur propre. */
   public static Holder<MobEffect> AMPUTATED;
   /** Charge lumineuse en cours (FRUIT-PIKA_PIKA-05) : marqueur visible du signal. */
   public static Holder<MobEffect> LIGHT_CHARGE;
   /** Pouvoir scellé par les ténèbres (FRUIT-YAMI_YAMI-05) : lu par isSuppressed, comme SEA_POWER. */
   public static Holder<MobEffect> YAMI_SEALED;
   /** Fenêtre d'absorption ouverte (FRUIT-YAMI_YAMI-04) : marqueur visible des ténèbres actives. */
   public static Holder<MobEffect> YAMI_ABSORBING;

   private MinepieceModMobEffects() {
   }

   /** Garde d'idempotence : l'enregistrement peut être demandé par plusieurs points d'entrée. */
   private static boolean registered;

   /**
    * Enregistre les effets. <b>Idempotent</b> — obligatoire car deux points d'entrée l'appellent :
    * l'initialiseur du mod et le bloc statique de {@code DevilFruitRegistry}, qui référence des
    * effets MinePiece (Mamaragan) et doit donc pouvoir garantir leur existence avant de les lire.
    */
   public static void register() {
      if (registered) {
         return;
      }
      registered = true;
      DEVIL_FRUIT_USER = register("devil_fruit_user", new DevilFruitUserMobEffect());
      SEA_POWER = register("sea_power", new SeaPowerMobEffect());
      SKILL = register("skill", new SimpleMarkerMobEffect(MobEffectCategory.NEUTRAL, -1));
      FLY_EFFECT = register("fly_effect", new SimpleMarkerMobEffect(MobEffectCategory.BENEFICIAL, -1));
      DOUBLE_JUMP_EFFECT = register("double_jump_effect", new SimpleMarkerMobEffect(MobEffectCategory.BENEFICIAL, -1));
      COOLDOWN_TIME = register("cooldown_time", new SimpleMarkerMobEffect(MobEffectCategory.NEUTRAL, -1));
      COOLDOWN_TIME_SECOND = register("cooldown_time_second", new SimpleMarkerMobEffect(MobEffectCategory.NEUTRAL, -1));
      COOLDOWN_TIME_FLY = register("cooldown_time_fly", new SimpleMarkerMobEffect(MobEffectCategory.NEUTRAL, -1));
      COOLDOWN_TIME_BACK_STEP = register("cooldown_time_back_step", new SimpleMarkerMobEffect(MobEffectCategory.NEUTRAL, -1));
      COOLDOWN_TIME_COMBAT = register("cooldown_time_combat", new SimpleMarkerMobEffect(MobEffectCategory.NEUTRAL, -1));
      ARMAMENT_HAKI = register("armament_haki", new SimpleMarkerMobEffect(MobEffectCategory.BENEFICIAL, -1));
      OBSERVATION_HAKI = register("observation_haki", new SimpleMarkerMobEffect(MobEffectCategory.BENEFICIAL, -1));
      SOVEREIGN_HAKI = register("sovereign_haki", new SimpleMarkerMobEffect(MobEffectCategory.BENEFICIAL, -1));
      GEAR = register("gear", new SimpleMarkerMobEffect(MobEffectCategory.BENEFICIAL, -1));
      ROOTED = register("rooted", new SimpleMarkerMobEffect(MobEffectCategory.HARMFUL, 0x4A5D23));
      SMOKE_CAPTURED = register("smoke_captured", new SimpleMarkerMobEffect(MobEffectCategory.HARMFUL, 0x8A8A8A));
      STICKY = register("sticky", new SimpleMarkerMobEffect(MobEffectCategory.HARMFUL, 0xF2E4CE));
      WEIGHT_STATE = register("weight_state", new SimpleMarkerMobEffect(MobEffectCategory.NEUTRAL, 0xC2B280));
      DIAMOND_BODY = register("diamond_body", new SimpleMarkerMobEffect(MobEffectCategory.BENEFICIAL, 0xB9F2FF));
      SHADOWLESS = register("shadowless", new SimpleMarkerMobEffect(MobEffectCategory.HARMFUL, 0x1A1A2E));
      ICE_DEFENSE = register("ice_defense", new SimpleMarkerMobEffect(MobEffectCategory.BENEFICIAL, 0x8FD8FF));
      SOVEREIGN_VEIL = register("sovereign_veil", new SimpleMarkerMobEffect(MobEffectCategory.BENEFICIAL, 0xC7A44B));
      B04_STONE = register("b04_stone",new SimpleMarkerMobEffect(MobEffectCategory.HARMFUL,0xA9ADB1));
      PETRIFICATION = register("petrification", new SimpleMarkerMobEffect(MobEffectCategory.HARMFUL, 0x8C8C8C));
MAMARAGAN = register("mamaragan", new MamaraganMobEffect(MobEffectCategory.BENEFICIAL, 0x5AC8FA));
      FROZEN = register("frozen", new SimpleMarkerMobEffect(MobEffectCategory.HARMFUL, 0x9BE7FF));
      // Ope Ope (FRUIT-OPE_OPE-03/05) : la ROOM a une durée qui doit se VOIR, et une cible
      // sectionnée doit pouvoir constater pourquoi ses coups ne portent plus.
      ROOM_BOUND = register("room_bound", new SimpleMarkerMobEffect(MobEffectCategory.BENEFICIAL, 0x66D9FF));
      AMPUTATED = register("amputated", new SimpleMarkerMobEffect(MobEffectCategory.HARMFUL, 0xD94F4F));
      LIGHT_CHARGE = register("light_charge", new SimpleMarkerMobEffect(MobEffectCategory.NEUTRAL, 0xFFF6C8));
      YAMI_SEALED = register("yami_sealed", new SimpleMarkerMobEffect(MobEffectCategory.HARMFUL, 0x1B1033));
      YAMI_ABSORBING = register("yami_absorbing", new SimpleMarkerMobEffect(MobEffectCategory.NEUTRAL, 0x2B1B4D));
   }

   private static Holder<MobEffect> register(String name, MobEffect effect) {
      return Registry.registerForHolder(
         BuiltInRegistries.MOB_EFFECT,
         Identifier.fromNamespaceAndPath(NAMESPACE, name),
         effect
      );
   }
}
