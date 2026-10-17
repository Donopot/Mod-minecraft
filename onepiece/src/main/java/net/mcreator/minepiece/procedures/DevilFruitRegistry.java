package net.mcreator.minepiece.procedures;

import br.com.oneminepiece.opworldlike.balance.DevilFruitBalance;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.mcreator.minepiece.init.MinepieceModItems;
import net.mcreator.minepiece.init.MinepieceModMobEffects;
import net.mcreator.minepiece.init.MinepieceModParticles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registre déclaratif des Fruits du Démon → leurs moves (techniques).
 * Les valeurs (dégâts, portée, noms) sont extraites fidèlement des procédures MCreator ver15
 * (Skill&lt;Fruit&gt;Procedure et leurs sous-procédures) — extraction automatisée par 6 sous-agents
 * sur /tmp/df-power-decomp/, règle « zéro invention ». L'exécution est gérée par
 * {@link DevilFruitSkillDispatcher} via les primitives de {@link CombatSkillLogic}.
 *
 * <p>Conventions d'intégration :</p>
 * <ul>
 *   <li>dmg/range/kb = coefficient de base (le multiplicateur de charge CNT6 = 1+cnt6*0.1 est omis) ;</li>
 *   <li>dmg {@code 0} = move « entité » (dégâts portés par l'entité skill, portage à venir) ;</li>
 *   <li>effets on-hit root/gel/paralysie → {@link MobEffects#SLOWNESS} ; self-buffs → {@link MoveType#BUFF} ;</li>
 *   <li>feu/brûlure (pas un MobEffect) → non appliqué, dégâts conservés.</li>
 * </ul>
 */
public final class DevilFruitRegistry {

   private static final Logger LOGGER = LoggerFactory.getLogger("MinePieceDevilFruit");

   private static final Map<Item, List<DevilFruitMove>> MOVES = new HashMap<>();

   /**
    * Techniques qui <b>invoquent une créature</b> (CORE-021) au lieu de produire un projectile ou un
    * visuel éphémère — valeur = durée de vie en ticks.
    *
    * <p>Porté par les données et non par une liste d'identifiants codée en dur dans le lanceur : c'est
    * ce qui permet à un Fruit d'être converti en invocation (Bisu Bisu, FRUIT-BISU_BISU-03/05) sans
    * toucher au moteur. Les 5 invocations d'origine conservent 200 ticks (10 s) ; les soldats-biscuits,
    * qui doivent tenir un combat, ont une durée plus longue.</p>
    */
   private static final Map<String, Integer> SUMMON_MOVES = new HashMap<>();

   static {
      SUMMON_MOVES.put("doppelman", 200);
      SUMMON_MOVES.put("raitei", 200);
      SUMMON_MOVES.put("hebunrifire", 200);
      SUMMON_MOVES.put("notewarrior", 200);
      SUMMON_MOVES.put("summontotmusica", 200);
      // FRUIT-BISU_BISU-03/05 : Biscuit Warrior I et II deviennent des soldats-biscuits.
      SUMMON_MOVES.put("biscuitwarrior", 240);
      SUMMON_MOVES.put("biscuitwarrior2", 320);
   }

   /** Vrai si la technique invoque une créature plutôt que de projeter un effet. */
   public static boolean isSummon(String moveId) {
      return SUMMON_MOVES.containsKey(moveId);
   }

   /** Durée de vie de l'invocation, en ticks. */
   public static int summonLifetime(String moveId) {
      return SUMMON_MOVES.getOrDefault(moveId, 200);
   }

   private DevilFruitRegistry() {
   }

   static {
      // Les effets MinePiece sont référencés par le catalogue (ex. MAMARAGAN) : leur enregistrement
      // doit être effectif AVANT la construction des techniques, sinon le Holder est nul et la
      // première technique concernée lève une NullPointerException au lancement. `register()` est
      // idempotent, l'initialiseur du mod peut donc l'avoir déjà appelé.
      net.mcreator.minepiece.init.MinepieceModMobEffects.register();

      // ===================== LOGIA =====================

      // Mera Mera no Mi (feu) — 7 techniques
      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA, List.of(
         DevilFruitMove.projectile("hiken", "Fire Fist", 30.0, 20, 1.0, 50.0, 100, MinepieceModParticles.FLAME),
         DevilFruitMove.projectile("higan", "Fire Gun", 15.0, 80, 0.5, 50.0, 100, MinepieceModParticles.FLAME),
         DevilFruitMove.melee("hibashira", "Fire Pillar", 24.0, 8.0, 1.0, 50.0, 120, MinepieceModParticles.FLAME),
         DevilFruitMove.melee("kagero", "Heat Haze", 15.0, 4.0, 0.5, 40.0, 80, MinepieceModParticles.FLAME),
         DevilFruitMove.projectile("hotarubi", "Firefly", 15.0, 60, 0.3, 50.0, 100, MinepieceModParticles.FLAME),
         DevilFruitMove.melee("kaenryuo", "Flame Dragon King", 26.0, 6.0, 2.0, 60.0, 140, MinepieceModParticles.FLAME),
         DevilFruitMove.melee("daienkai", "Great Flame Commandment", 200.0, 12.0, 3.0, 100.0, 300, MinepieceModParticles.FLAME)
      ));

      // Magu Magu no Mi (magma) — 5 techniques
      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_MAGU_MAGU, List.of(
         DevilFruitMove.projectile("daifunka", "Dai Funka", 30.0, 80, 1.0, 60.0, 140, MinepieceModParticles.MAGMA),
         DevilFruitMove.projectile("inugamiguren", "Inugami Guren", 30.0, 80, 1.0, 60.0, 140, MinepieceModParticles.MAGMA),
         DevilFruitMove.melee("kageromagumagu", "Kagero", 20.0, 3.0, 0.0, 40.0, 80, MinepieceModParticles.MAGMA),
         DevilFruitMove.melee("volcanicexplosion", "Volcanic Explosion", 24.0, 8.0, 1.0, 50.0, 120, MinepieceModParticles.MAGMA),
         DevilFruitMove.projectile("ryuseikazan", "Ryusei Kazan", 40.0, 80, 1.0, 70.0, 160, MinepieceModParticles.MAGMA)
      ));

      // Goro Goro no Mi (foudre) — 7 techniques
      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO, List.of(
         DevilFruitMove.melee("vari", "Vari", 14.0, 24.0, 0.1, MobEffects.SLOWNESS, 20, 1, 50.0, 100, MinepieceModParticles.THUNDER_BLUE),
         DevilFruitMove.melee("elthor", "El Thor", 24.0, 8.0, 0.5, MobEffects.SLOWNESS, 20, 1, 60.0, 140, MinepieceModParticles.THUNDER_BLUE),
         DevilFruitMove.melee("sango", "Sango", 18.0, 10.0, 0.1, MobEffects.SLOWNESS, 20, 1, 50.0, 120, MinepieceModParticles.THUNDER_BLUE),
         DevilFruitMove.buff("mamaragan", "Mamaragan", MinepieceModMobEffects.MAMARAGAN, 60, 0, 50.0, 120, MinepieceModParticles.THUNDER_BLUE),
         DevilFruitMove.melee("karikrash", "Kari Krash", 18.0, 12.0, 1.5, MobEffects.SLOWNESS, 20, 1, 50.0, 120, MinepieceModParticles.THUNDER_BLUE),
         DevilFruitMove.buff("passive", "Goro Goro Passive", MobEffects.SPEED, 60, 0, 40.0, 100, MinepieceModParticles.THUNDER_BLUE),
         DevilFruitMove.projectile("raigo", "Raigo", 800.0, 80, 1.0, 150.0, 400, MinepieceModParticles.THUNDER_BLUE, 4.0)
      ));

      // Hie Hie no Mi (glace) — 5 techniques
      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_HIE_HIE, List.of(
         DevilFruitMove.projectile("icepartisan", "Ice Block: Partisan", 40.0, 80, 1.0, 60.0, 140, MinepieceModParticles.ICE),
         DevilFruitMove.projectile("pheasantbeak", "Ice Block: Pheasant Beak", 60.0, 80, 1.0, 70.0, 160, MinepieceModParticles.ICE),
         DevilFruitMove.projectile("icespikes", "Ice Spikes", 20.0, 60, 0.5, 50.0, 100, MinepieceModParticles.ICE),
         DevilFruitMove.melee("iceball", "Ice Ball", 15.0, 16.0, 0.01, MobEffects.SLOWNESS, 40, 2, 50.0, 120, MinepieceModParticles.ICE),
         DevilFruitMove.melee("iceage", "Ice Age", 20.0, 16.0, 0.1, MobEffects.SLOWNESS, 60, 3, 70.0, 180, MinepieceModParticles.ICE)
      ));

      // Pika Pika no Mi (lumière) — 8 techniques
      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_PIKA_PIKA, List.of(
         // Rayons : particules de lumière du JAR source (particle_light_large.json), vitesses ver15
         // (CORE-016) inchangées.
         DevilFruitMove.projectile("laser1", "Laser", 17.0, 80, 1.0, 50.0, 100, MinepieceModParticles.LIGHT_LARGE, 4.0),
         DevilFruitMove.projectile("laser2", "Laser Barrage", 24.0, 80, 1.0, 60.0, 120, MinepieceModParticles.LIGHT_LARGE, 4.0),
         DevilFruitMove.melee("kick", "Light Speed Kick", 17.0, 6.0, 2.0, 50.0, 120, MinepieceModParticles.LIGHT_LARGE),
         DevilFruitMove.buff("yatanokagami", "Yata no Kagami", MobEffects.SPEED, 40, 2, 40.0, 80, MinepieceModParticles.LIGHT_LARGE),
         DevilFruitMove.projectile("magatama", "Yasakani no Magatama", 16.0, 60, 0.5, 50.0, 100, MinepieceModParticles.LIGHT_SMALL, 3.5),
         DevilFruitMove.buff("amanomurakumo", "Ama no Murakumo", MobEffects.STRENGTH, 60, 1, 50.0, 120, MinepieceModParticles.LIGHT_LARGE),
         DevilFruitMove.melee("speedispower", "Speed is Power", 15.0, 6.0, 1.5, 50.0, 120, MinepieceModParticles.LIGHT_LARGE),
         DevilFruitMove.buff("passive", "Pika Pika Passive", MobEffects.SPEED, 60, 0, 40.0, 100, MinepieceModParticles.LIGHT_LARGE)
      ));

      // Yami Yami no Mi (ténèbres) — 4 techniques
      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_YAMI_YAMI, List.of(
         // Particules : celles du JAR source (assets/minepiece/particles/particle_dark.json), reprises
         // telles quelles. Les valeurs ver15 (dégâts, portée, recul, énergie, recharge) sont inchangées.
         DevilFruitMove.melee("kurouzu", "Kurouzu", 6.0, 10.0, 1.0, 50.0, 100, MinepieceModParticles.DARK),
         DevilFruitMove.melee("blackhole", "Black Hole", 24.0, 48.0, 2.0, 80.0, 200, MinepieceModParticles.DARK),
         DevilFruitMove.projectile("darkball", "Dark Ball", 40.0, 80, 1.0, 70.0, 160, MinepieceModParticles.DARK),
         DevilFruitMove.projectile("darkmatter", "Dark Matter", 200.0, 80, 2.0, 150.0, 400, MinepieceModParticles.DARK)
      ));

      // Suna Suna no Mi (sable) — 5 techniques
      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_SUNA_SUNA, List.of(
         DevilFruitMove.projectile("desertspada", "Desert Spada", 12.0, 80, 0.5, 50.0, 100, MinepieceModParticles.SAND),
         DevilFruitMove.projectile("barjan", "Barjan", 12.0, 80, 1.0, MobEffects.SLOWNESS, 20, 1, 50.0, 120, MinepieceModParticles.SAND),
         DevilFruitMove.projectile("sables", "Sables", 50.0, 80, 1.0, 70.0, 160, MinepieceModParticles.SAND),
         DevilFruitMove.projectile("desertlaspada", "Desert La Spada", 15.0, 80, 0.5, 50.0, 120, MinepieceModParticles.SAND),
         DevilFruitMove.melee("grounddeath", "Ground Death", 20.0, 16.0, 0.5, MobEffects.SLOWNESS, 30, 1, 60.0, 160, MinepieceModParticles.SAND)
      ));

      // Moku Moku no Mi (fumée) — 4 techniques
      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_MOKU_MOKU, List.of(
         DevilFruitMove.projectile("whiteblow", "White Blow", 50.0, 80, 1.0, 70.0, 160, MinepieceModParticles.BIG_SMOKE),
         DevilFruitMove.projectile("whitevine", "White Vine", 15.0, 80, 0.5, 50.0, 100, MinepieceModParticles.BIG_SMOKE),
         DevilFruitMove.melee("whitelauncher", "White Launcher", 11.0, 4.0, 1.0, 50.0, 100, MinepieceModParticles.BIG_SMOKE),
         DevilFruitMove.melee("whiteout", "White Out", 18.0, 32.0, 2.0, 60.0, 160, MinepieceModParticles.BIG_SMOKE)
      ));

      // Mori Mori no Mi (forêt) — 4 techniques
      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_MORI_MORI, List.of(
         DevilFruitMove.projectile("root", "Mori Mori Root", 9.0, 80, 0.5, 50.0, 100, MinepieceModParticles.FLOWER),
         DevilFruitMove.projectile("rootgiant", "Giant Root", 20.0, 80, 1.0, 60.0, 140, MinepieceModParticles.FLOWER),
         DevilFruitMove.melee("assault", "Mori Mori Assault", 15.0, 6.0, 1.0, 50.0, 120, MinepieceModParticles.FLOWER),
         DevilFruitMove.projectile("rootsurround", "Root Surroundings", 15.0, 80, 1.0, 70.0, 180, MinepieceModParticles.FLOWER)
      ));

      // ===================== ZOAN =====================

      // Gomu Gomu no Mi (caoutchouc) — 4 techniques (source ver15 : pistol/rocket/gatling/gear change)
      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_GOMU_GOMU, List.of(
         new DevilFruitMove("gomu_pistol", "Gomu Gomu no Pistol", MoveType.MELEE, 11.0, 6.0, 1.5, null, 0, 0,
            50.0, 100, MinepieceModParticles.SHOCKWAVE, "entity_gomu_arm", 0, 0.0),
         DevilFruitMove.melee("gomu_bazooka", "Gomu Gomu no Bazooka", 20.0, 5.0, 3.0, 60.0, 140, MinepieceModParticles.SHOCKWAVE),
         DevilFruitMove.melee("gomu_gatling", "Gomu Gomu no Gatling", 6.0, 5.0, 1.0, 50.0, 100, MinepieceModParticles.SHOCKWAVE),
         DevilFruitMove.buff("gear_change", "Gear Change", MobEffects.SPEED, 60, 2, 40.0, 80, MinepieceModParticles.SHOCKWAVE)
      ));

      // Rokushiki (techniques partagées par les Zoan) — valeurs de référence
      putZoan(MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_WOLF, List.of(
         DevilFruitMove.melee("shigan", "Shigan", 10.0, 2.0, 0.25, 40.0, 80, MinepieceModParticles.CLAWS),
         DevilFruitMove.buff("soru", "Soru", MobEffects.SPEED, 40, 2, 40.0, 80, MinepieceModParticles.CLAWS),
         DevilFruitMove.projectile("rankyaku", "Rankyaku", 30.0, 80, 0.5, 50.0, 100, MinepieceModParticles.CLAWS),
         DevilFruitMove.projectile("lupusfall", "Rankyaku: Lupus Fall", 30.0, 80, 0.5, 50.0, 120, MinepieceModParticles.CLAWS),
         DevilFruitMove.melee("okamihajiki", "Okami Hajiki", 12.0, 4.0, 2.0, 50.0, 100, MinepieceModParticles.CLAWS),
         DevilFruitMove.melee("jushigan", "Jushigan", 12.0, 4.0, 1.0, 50.0, 100, MinepieceModParticles.CLAWS),
         DevilFruitMove.melee("localareanetwork", "Local Area Network", 15.0, 6.0, 1.0, MobEffects.SLOWNESS, 20, 1, 60.0, 140, MinepieceModParticles.CLAWS),
         // FRUIT-INU_INU_WOLF-04 : le plan demande d'« AJOUTER … une poursuite avec endurance ».
         // L'ajout EST l'instruction. Coût 60 É, recharge 160 ticks, portée 24 blocs (cf. contrat §4).
         DevilFruitMove.melee("poursuite", "Poursuite", 0.0, 24.0, 0.0, 60.0, 160, MinepieceModParticles.CLAWS)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_USHI_USHI_GIRAFFE, List.of(
         DevilFruitMove.melee("shigan", "Shigan", 10.0, 2.0, 0.25, 40.0, 80, MinepieceModParticles.CLAWS),
         DevilFruitMove.buff("soru", "Soru", MobEffects.SPEED, 40, 2, 40.0, 80, MinepieceModParticles.CLAWS),
         DevilFruitMove.projectile("rankyaku", "Rankyaku", 30.0, 80, 0.5, 50.0, 100, MinepieceModParticles.CLAWS),
         DevilFruitMove.melee("rankyakuran", "Rankyaku Ran", 15.0, 6.0, 1.0, 50.0, 100, MinepieceModParticles.CLAWS),
         DevilFruitMove.melee("amanedachi", "Rankyaku Amane Dachi", 15.0, 6.0, 1.0, 50.0, 120, MinepieceModParticles.CLAWS)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_NEKO_NEKO_LEOPARD, List.of(
         DevilFruitMove.melee("shigan", "Shigan", 10.0, 2.0, 0.25, 40.0, 80, MinepieceModParticles.CLAWS),
         DevilFruitMove.buff("soru", "Soru", MobEffects.SPEED, 40, 2, 40.0, 80, MinepieceModParticles.CLAWS),
         DevilFruitMove.projectile("rankyaku", "Rankyaku", 30.0, 80, 0.5, 50.0, 100, MinepieceModParticles.CLAWS),
         DevilFruitMove.projectile("gaicho", "Rankyaku Gaicho", 30.0, 80, 0.5, 50.0, 120, MinepieceModParticles.CLAWS),
         DevilFruitMove.melee("attackjump", "Attack Jump", 15.0, 4.0, 1.0, 40.0, 80, MinepieceModParticles.CLAWS),
         DevilFruitMove.melee("shugan", "Shugan", 18.0, 5.0, 0.1, 50.0, 120, MinepieceModParticles.CLAWS),
         DevilFruitMove.melee("rokuogan", "Rokuogan", 35.0, 12.0, 1.5, 80.0, 200, MinepieceModParticles.CLAWS)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_BATTO_BATTO, List.of(
         DevilFruitMove.melee("shigan", "Shigan", 10.0, 2.0, 0.25, 40.0, 80, MinepieceModParticles.BAT),
         DevilFruitMove.buff("soru", "Soru", MobEffects.SPEED, 40, 2, 40.0, 80, MinepieceModParticles.BAT),
         DevilFruitMove.projectile("rankyaku", "Rankyaku", 30.0, 80, 0.5, 50.0, 100, MinepieceModParticles.BAT),
         DevilFruitMove.projectile("tobushigan", "Tobu Shigan", 2.2, 45, 0.5, 40.0, 80, MinepieceModParticles.BAT),
         DevilFruitMove.areaEffect("kamiezanshin", "Kamie Zanshin", null, 60, 0, 4.0, 100.0, 300, MinepieceModParticles.BAT),
         DevilFruitMove.melee("yurushitene", "Yurushitene", 18.0, 5.0, 0.1, 50.0, 120, MinepieceModParticles.BAT)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_HITO_HITO_DAIBUTSU, List.of(
         DevilFruitMove.melee("attackoverhead", "Attack Overhead", 15.0, 12.0, 1.0, 50.0, 120, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.melee("brilliantpunk", "Brilliant Punk", 15.0, 6.0, 1.0, 50.0, 100, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.projectile("impactball", "Impact Ball", 15.0, 80, 0.5, 50.0, 120, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.melee("overheadsuper", "Attack Overhead Super", 25.0, 10.0, 2.0, 60.0, 140, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.melee("impactwave", "Impact Wave", 28.0, 12.0, 1.5, 60.0, 160, MinepieceModParticles.SHOCK_HIT)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_RYU_RYU_PACHYCEPHALOSAURUS, List.of(
         DevilFruitMove.melee("attackjump", "Attack Jump", 15.0, 4.0, 1.0, 40.0, 80, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.melee("attacksweep", "Attack Sweep", 12.0, 6.0, 1.0, 50.0, 100, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.melee("headbutt", "Headbutt", 15.0, 4.0, 1.0, 50.0, 100, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.melee("ulzugan", "Ul Zugan", 18.0, 6.0, 1.5, 50.0, 120, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.melee("ulmeteor", "Ul Meteor", 24.0, 12.0, 1.5, 60.0, 160, MinepieceModParticles.SHOCK_HIT)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_OKUCHI_NO_MAKAMI, List.of(
         DevilFruitMove.melee("attackoverhead", "Attack Overhead", 15.0, 12.0, 1.0, 50.0, 120, MinepieceModParticles.CLAWS),
         DevilFruitMove.melee("attacksweep", "Attack Sweep", 12.0, 6.0, 1.0, MobEffects.SLOWNESS, 20, 1,
            50.0, 100, MinepieceModParticles.CLAWS),
         DevilFruitMove.melee("raimeihakke", "Raimei Hakke", 18.0, 4.0, 1.0, 50.0, 120, MinepieceModParticles.CLAWS),
         DevilFruitMove.projectile("narikabura", "Narikabura", 15.0, 80, 0.5, MobEffects.SLOWNESS, 20, 1,
            50.0, 120, MinepieceModParticles.CLAWS),
         DevilFruitMove.melee("namujihyoga", "Namuji Hyoga", 15.0, 2.0, 0.75, MobEffects.SLOWNESS, 20, 1,
            50.0, 100, MinepieceModParticles.CLAWS),
         DevilFruitMove.melee("himorogiri", "Himorogiri", 30.0, 10.0, 1.0, MobEffects.SLOWNESS, 30, 1,
            70.0, 180, MinepieceModParticles.CLAWS),
         // Défense de glace temporaire : coût 60, recharge 160 (décision, cf. contrat §4).
         DevilFruitMove.buff("hyogaheki", "Hyoga Heki", MobEffects.RESISTANCE, 1, 0, 60.0, 160,
            MinepieceModParticles.CLAWS)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_TORI_TORI_PHOENIX, List.of(
         DevilFruitMove.melee("attackspeed", "Attack Speed", 11.0, 4.0, 1.0, 40.0, 80, MinepieceModParticles.FLAME_BLUE),
         DevilFruitMove.melee("ongle", "Ongle", 15.0, 4.0, 1.0, 40.0, 80, MinepieceModParticles.FLAME_BLUE),
         DevilFruitMove.projectile("bluebird", "Blue Bird", 15.0, 80, 0.5, MobEffects.SLOWNESS, 20, 1, 50.0, 120, MinepieceModParticles.FLAME_BLUE),
         DevilFruitMove.melee("marcotornado", "Marco Tornado", 16.0, 24.0, 1.0, 50.0, 120, MinepieceModParticles.FLAME_BLUE),
         DevilFruitMove.melee("hooin", "Hoo In", 24.0, 4.0, 2.5, MobEffects.SLOW_FALLING, 20, 0, 60.0, 140, MinepieceModParticles.FLAME_BLUE),
         DevilFruitMove.melee("fujiazami", "Fujiazami", 15.0, 12.0, 1.5, MobEffects.SLOWNESS, 20, 1, 50.0, 120, MinepieceModParticles.FLAME_BLUE)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_UO_UO_SEIRYU, List.of(
         DevilFruitMove.melee("attackoverhead", "Attack Overhead", 15.0, 12.0, 1.0, 50.0, 120, MinepieceModParticles.WATER),
         DevilFruitMove.melee("attacksweep", "Attack Sweep", 12.0, 6.0, 1.0, MobEffects.SLOWNESS, 20, 1, 50.0, 100, MinepieceModParticles.WATER),
         DevilFruitMove.melee("raimeihakke", "Raimei Hakke", 18.0, 4.0, 1.0, 50.0, 120, MinepieceModParticles.WATER),
         DevilFruitMove.projectile("kongokabura", "Kongo Kabura", 18.0, 80, 0.5, MobEffects.SLOWNESS, 20, 1, 50.0, 120, MinepieceModParticles.WATER),
         DevilFruitMove.projectile("kaifu", "Kaifu", 30.0, 80, 0.5, MobEffects.SLOWNESS, 20, 1, 50.0, 120, MinepieceModParticles.WATER),
         DevilFruitMove.projectile("borobreath", "Boro Breath", 18.0, 80, 0.75, MobEffects.SLOWNESS, 20, 1, 60.0, 140, MinepieceModParticles.WATER),
         DevilFruitMove.melee("kosanze", "Kosanze Ragnaraku", 30.0, 10.0, 2.0, MobEffects.SLOWNESS, 20, 1, 70.0, 180, MinepieceModParticles.WATER),
         DevilFruitMove.melee("ikoku", "Ikoku", 30.0, 20.0, 2.0, 70.0, 180, MinepieceModParticles.WATER)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_RYU_RYU_PTERANODON, List.of(
         DevilFruitMove.melee("attackspeed", "Attack Speed", 11.0, 4.0, 1.0, 40.0, 80, MinepieceModParticles.SLASH),
         DevilFruitMove.melee("tankyudon", "Tankyudon", 15.0, 3.0, 0.6, MobEffects.SLOWNESS, 20, 1, 50.0, 100, MinepieceModParticles.SLASH),
         DevilFruitMove.projectile("barizodon", "Barizodon", 30.0, 80, 0.5, MobEffects.SLOWNESS, 20, 1, 50.0, 120, MinepieceModParticles.SLASH)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_RYU_RYU_BRACHIOSAURUS, List.of(
         DevilFruitMove.melee("attackoverhead", "Attack Overhead", 15.0, 12.0, 1.0, 50.0, 120, MinepieceModParticles.MAGMA),
         DevilFruitMove.melee("brachiobober", "Brachio Bober", 25.0, 10.0, 1.0, MobEffects.SLOWNESS, 20, 1, 60.0, 140, MinepieceModParticles.MAGMA),
         DevilFruitMove.projectile("blackcofee", "Black Cofee", 17.0, 80, 1.0, MobEffects.SLOWNESS, 20, 1, 50.0, 120, MinepieceModParticles.MAGMA),
         DevilFruitMove.projectile("brachiolauncher", "Brachio Launcher", 2.2, 80, 0.5, 40.0, 80, MinepieceModParticles.MAGMA),
         DevilFruitMove.projectile("sparkingqueen", "Sparking Queen", 16.0, 80, 0.5, 50.0, 120, MinepieceModParticles.MAGMA)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_NEKO_NEKO_SABERTIGER, List.of(
         DevilFruitMove.projectile("shigan", "Shigan", 10.0, 80, 0.25, 40.0, 80, MinepieceModParticles.CLAWS),
         DevilFruitMove.melee("attackspeed", "Attack Speed", 11.0, 4.0, 1.0, 40.0, 80, MinepieceModParticles.CLAWS),
         DevilFruitMove.melee("shiganmadara", "Shigan Madara", 12.0, 6.0, 0.1, 50.0, 100, MinepieceModParticles.CLAWS),
         DevilFruitMove.projectile("gagan", "Gagan", 15.0, 80, 0.5, MobEffects.SLOWNESS, 20, 1, 50.0, 120, MinepieceModParticles.CLAWS),
         DevilFruitMove.melee("kibasen", "Kibasen", 16.0, 6.0, 1.0, 50.0, 120, MinepieceModParticles.CLAWS)
      ));

      // ===================== PARAMECIA =====================

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_GURA_GURA, List.of(
         DevilFruitMove.melee("attacksweep", "Attack Sweep", 12.0, 6.0, 1.0, 50.0, 100, MinepieceModParticles.SHOCKWAVE),
         DevilFruitMove.melee("overheadsuper", "Attack Overhead Super", 25.0, 10.0, 2.0, 60.0, 140, MinepieceModParticles.SHOCKWAVE),
         DevilFruitMove.melee("gekishin", "Gekishin", 16.0, 12.0, 1.0, 60.0, 140, MinepieceModParticles.SHOCKWAVE),
         DevilFruitMove.melee("kabutowari", "Kabutowari", 15.0, 6.0, 1.5, 50.0, 120, MinepieceModParticles.SHOCKWAVE),
         DevilFruitMove.melee("tenchimeido", "Tenchi Meido", 18.0, 48.0, 1.0, 80.0, 200, MinepieceModParticles.SHOCKWAVE),
         DevilFruitMove.melee("kaishin", "Kaishin", 24.0, 6.0, 1.0, 70.0, 160, MinepieceModParticles.SHOCKWAVE)
      ));

      // FRUIT-INU_INU_OKUCHI_NO_MAKAMI-05 : la défense de glace est une technique ACTIVÉE — le plan
      // demande explicitement une défense « avec coût ». Ajout déclaré (l'audit le signale).
      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_HORO_HORO, List.of(
         DevilFruitMove.projectile("negativehollow", "Negative Hollow", 10.0, 80, 0.5, MobEffects.SLOWNESS, 40, 2, 50.0, 120, MinepieceModParticles.NEGATIVE),
         DevilFruitMove.projectile("minihollow", "Mini Hollow", 10.0, 80, 0.5, MobEffects.SLOWNESS, 30, 1, 50.0, 100, MinepieceModParticles.NEGATIVE),
         DevilFruitMove.projectile("tokuhoro", "Toku Horo", 10.0, 80, 0.5, MobEffects.SLOWNESS, 30, 1, 50.0, 100, MinepieceModParticles.NEGATIVE),
         DevilFruitMove.buff("rap", "Rap", MobEffects.SPEED, 40, 1, 40.0, 80, MinepieceModParticles.NEGATIVE),
         DevilFruitMove.projectile("kumac", "Kumac", 15.0, 80, 0.5, 50.0, 120, MinepieceModParticles.NEGATIVE)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_NIKYU_NIKYU, List.of(
         DevilFruitMove.projectile("padho", "Pad Ho", 10.0, 80, 2.0, 50.0, 120, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.projectile("ursusshock", "Ursus Shock", 25.0, 80, 2.0, 60.0, 160, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.melee("assault", "Assault", 15.0, 4.0, 1.0, 50.0, 100, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.melee("kumapunch", "Kuma Punch", 24.0, 8.0, 1.5, 60.0, 140, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.buff("passive", "Nikyu Passive", MobEffects.RESISTANCE, 60, 1, 50.0, 120, MinepieceModParticles.SHOCK_HIT)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_ZUSHI_ZUSHI, List.of(
         DevilFruitMove.melee("attackspeed", "Attack Speed", 11.0, 6.0, 1.0, 40.0, 80, MinepieceModParticles.GRAVITY),
         DevilFruitMove.melee("gravito", "Gravito", 11.0, 6.0, 1.0, MobEffects.SLOWNESS, 30, 1, 50.0, 120, MinepieceModParticles.GRAVITY),
         DevilFruitMove.projectile("meteorsmall", "Meteor (small)", 20.0, 80, 0.1, 50.0, 120, MinepieceModParticles.GRAVITY, 1.5),
         DevilFruitMove.melee("moko", "Moko", 26.0, 16.0, 2.0, 70.0, 180, MinepieceModParticles.GRAVITY),
         DevilFruitMove.projectile("rubble", "Rubble", 15.0, 80, 0.5, 50.0, 100, MinepieceModParticles.GRAVITY, 1.5),
         DevilFruitMove.projectile("meteor", "Meteor", 40.0, 80, 0.1, 70.0, 180, MinepieceModParticles.GRAVITY, 1.5)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_BISU_BISU, List.of(
         DevilFruitMove.melee("crashpretzel", "Crash Pretzel", 15.0, 12.0, 1.5, 50.0, 120, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.projectile("biscuitwarrior", "Biscuit Warrior", 15.0, 80, 0.5, 60.0, 140, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.projectile("biscuitwarrior2", "Biscuit Warrior II", 20.0, 80, 0.5, 60.0, 140, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.buff("biscuit", "Biscuit", MobEffects.RESISTANCE, 60, 1, 50.0, 120, MinepieceModParticles.SHOCK_HIT)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_MOCHI_MOCHI, List.of(
         new DevilFruitMove("mochipistol", "Mochi Pistol", MoveType.MELEE, 11.0, 6.0, 1.5, null, 0, 0,
            50.0, 100, MinepieceModParticles.SHOCK_HIT, "entity_gomu_arm", 0, 0.0),
         new DevilFruitMove("mochigatling", "Mochi Gatling", MoveType.MELEE, 4.0, 5.0, 0.5, null, 0, 0,
            50.0, 100, MinepieceModParticles.SHOCK_HIT, "entity_gomu_arm", 0, 0.0),
         DevilFruitMove.projectile("yakimochi", "Yaki Mochi", 15.0, 80, 0.5, 50.0, 120, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.melee("zangirimochi", "Zan Giri Mochi", 6.0, 6.0, 1.0, 50.0, 100, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.projectile("musodonatsu", "Muso Donatsu", 20.0, 80, 0.5, 60.0, 140, MinepieceModParticles.SHOCK_HIT),
         // FRUIT-MOCHI_MOCHI-05 : le plan demande de « CRÉER des constructions temporaires de mochi » —
         // aucune des 5 techniques de ver15 n'en pose. L'ajout est donc l'instruction elle-même.
         DevilFruitMove.buff("mochibuild", "Mochi Build", MobEffects.RESISTANCE, 200, 1, 50.0, 120,
            MinepieceModParticles.SHOCK_HIT)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_UTA_UTA, List.of(
         DevilFruitMove.projectile("noteattack", "Note Attack", 10.0, 80, 0.5, 50.0, 100, MinepieceModParticles.NOTE),
         DevilFruitMove.projectile("notelaser", "Note Laser", 10.0, 80, 0.5, 50.0, 100, MinepieceModParticles.NOTE),
         DevilFruitMove.melee("posing", "Posing", 20.0, 8.0, 1.0, 60.0, 140, MinepieceModParticles.NOTE),
         DevilFruitMove.melee("noteshield", "Note Shield", 20.0, 8.0, 1.0, 60.0, 140, MinepieceModParticles.NOTE),
         DevilFruitMove.melee("notewarrior", "Note Warrior", 15.0, 6.0, 1.0, 60.0, 140, MinepieceModParticles.NOTE),
         DevilFruitMove.melee("totmusicapunch", "Tot Musica Punch", 30.0, 6.0, 1.0, 60.0, 140, MinepieceModParticles.NOTE),
         DevilFruitMove.melee("summontotmusica", "Summon Tot Musica", 30.0, 8.0, 1.5, 100.0, 300, MinepieceModParticles.NOTE)
      ));

      // ⚠️ DÉCISION DE MOD (FRUIT-IMU-03/04) : l'identité de ce Fruit n'est PAS révélée dans l'œuvre.
      // Le plan demande de « définir explicitement la nature du pouvoir souhaité » et d'« éviter
      // d'inventer une fidélité au canon ». Le catalogue ci-dessous est donc un choix assumé et
      // documenté (docs/fruits/IMU.md), bâti autour de la seule technique réellement présente dans le
      // port d'origine. Aucune de ces lignes ne prétend reconstituer l'œuvre.
      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_IMU, List.of(
         new DevilFruitMove("imuarrow", "Imu Arrow", MoveType.MELEE, 25.0, 6.0, 1.0, null, 0, 0,
            60.0, 140, MinepieceModParticles.NEGATIVE, "entity_gomu_arm", 0, 0.0),
         // L'autorité suspend le geste : zone courte, ralentissement fort, coût et recharge propres.
         DevilFruitMove.areaEffect("silentcommand", "Silent Command", MobEffects.SLOWNESS, 60, 3,
            8.0, 50.0, 120, MinepieceModParticles.NEGATIVE),
         // La poigne du souverain : frappe lourde, portée courte, recul marqué.
         DevilFruitMove.melee("sovereigngrasp", "Sovereign Grasp", 30.0, 5.0, 2.5, 70.0, 180,
            MinepieceModParticles.NEGATIVE)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_KIRA_KIRA, List.of(
         // Attack Overhead reste : c'est l'ouvreur générique partagé par neuf fruits en ver15, et le plan
         // ne demande de remplacer que les JETS DE PIERRE.
         DevilFruitMove.melee("attackoverhead", "Attack Overhead", 15.0, 12.0, 1.0, 50.0, 120, MinepieceModParticles.LIGHT_LARGE),
         DevilFruitMove.melee("brilliantpunk", "Brilliant Punk", 15.0, 6.0, 1.0, 50.0, 100, MinepieceModParticles.LIGHT_LARGE),
         // FRUIT-KIRA_KIRA-05 : « remplacer les jets de pierre GÉNÉRIQUES par des attaques cohérentes avec
         // le contrat diamant ». Stone Throwing et Stone Throwing (large) étaient partagés avec Riki Riki
         // (d'où « génériques ») ; ils sont remplacés ici, et Riki Riki les garde.
         // ⚠️ PARAMÈTRES INCHANGÉS (15 et 200 dégâts) : la fidélité ver15 prime, et le remplacement porte
         // sur l'identité de l'attaque, pas sur sa puissance. Le plan NE demande PAS de rééquilibrage.
         DevilFruitMove.projectile("diamondshard", "Diamond Shard", 15.0, 80, 0.5, 50.0, 100, MinepieceModParticles.LIGHT_LARGE),
         DevilFruitMove.projectile("diamondavalanche", "Diamond Avalanche", 200.0, 80, 1.0, 70.0, 180, MinepieceModParticles.LIGHT_LARGE),
         // FRUIT-KIRA_KIRA-03/04 : le corps de diamant est une capacité ACTIVÉE (le plan demande une
         // réduction de dégâts « avec coût ou conditions », et une activation/désactivation observables).
         DevilFruitMove.buff("diamondbody", "Diamond Body", MobEffects.RESISTANCE, 200, 1, 60.0, 200,
            MinepieceModParticles.LIGHT_SMALL)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_SUPA_SUPA, List.of(
         DevilFruitMove.projectile("projectileslash", "Projectile Slash", 30.0, 80, 0.5, 50.0, 120, MinepieceModParticles.SLASH),
         DevilFruitMove.melee("atomicspurt", "Atomic Spurt", 16.0, 5.0, 1.0, 50.0, 120, MinepieceModParticles.SLASH),
         DevilFruitMove.melee("sparklingdaisy", "Sparkling Daisy", 18.0, 5.0, 1.0, 60.0, 140, MinepieceModParticles.SLASH),
         DevilFruitMove.melee("sparbreak", "Spar Break", 15.0, 5.0, 1.0, 60.0, 140, MinepieceModParticles.SLASH)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_RIKI_RIKI, List.of(
         DevilFruitMove.melee("attackoverhead", "Attack Overhead", 15.0, 12.0, 1.0, 50.0, 120, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.melee("hadoelbow", "Hado Elbow", 18.0, 8.0, 2.0, 50.0, 120, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.melee("galleonlariat", "Galleon Lariat", 18.0, 6.0, 1.5, 50.0, 120, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.projectile("stonethrowing", "Stone Throwing", 15.0, 80, 0.5, 50.0, 100, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.projectile("stonethrowinglarge", "Stone Throwing (large)", 200.0, 80, 1.0, 70.0, 180, MinepieceModParticles.SHOCK_HIT)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_MERO_MERO, List.of(
         DevilFruitMove.projectile("pistolkiss", "Pistol Kiss", 2.2, 80, 0.0, MobEffects.SLOWNESS, 40, 2, 50.0, 120, MinepieceModParticles.FLOWER_PINK),
         DevilFruitMove.melee("roundhousekick", "Brick Roundhouse Kick", 15.0, 6.0, 1.0, 50.0, 100, MinepieceModParticles.FLOWER_PINK),
         DevilFruitMove.melee("rotatekick", "Rotate Kick", 18.0, 6.0, 2.5, 50.0, 120, MinepieceModParticles.FLOWER_PINK),
         DevilFruitMove.melee("perfumefemur", "Perfume Femur Magna", 6.0, 4.0, 1.0, 50.0, 100, MinepieceModParticles.FLOWER_PINK)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_KIRO_KIRO, List.of(
         DevilFruitMove.melee("attackoverhead", "Attack Overhead", 15.0, 12.0, 1.0, 50.0, 120, MinepieceModParticles.GRAVITY),
         DevilFruitMove.buff("attackjump", "Attack Jump", MobEffects.SPEED, 60, 1, 40.0, 80, MinepieceModParticles.GRAVITY),
         DevilFruitMove.melee("kirooverhead", "Kiro Overhead", 25.0, 10.0, 1.0, 60.0, 140, MinepieceModParticles.GRAVITY),
         DevilFruitMove.buff("weightmanipulation", "Weight Manipulation", MobEffects.STRENGTH, 60, 1, 50.0, 120, MinepieceModParticles.GRAVITY)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_BOMU_BOMU, List.of(
         DevilFruitMove.projectile("nosefancycannon", "Nose Fancy Cannon", 13.0, 80, 0.5, 40.0, 80, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.melee("kickbomb", "Kick Bomb", 16.0, 12.0, 2.0, 50.0, 120, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.melee("punchbomb", "Punch Bomb", 15.0, 12.0, 1.5, 50.0, 120, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.melee("bomboverhead", "Bomb Overhead", 18.0, 16.0, 2.0, 60.0, 140, MinepieceModParticles.SHOCK_HIT),
         DevilFruitMove.melee("zenshinkibaku", "Zenshin Kibaku", 30.0, 24.0, 1.5, 70.0, 180, MinepieceModParticles.SHOCK_HIT)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_OPE_OPE, List.of(
         // VER15, à l'identique : mêlée générique 20 dégâts / portée 6 / recul 1 / 60 É / 140 ticks.
         // Le contrat TECH-01 demande d'en préserver la portée et le type de dégâts.
         DevilFruitMove.melee("stoneopeope", "Stone Ope Ope", 20.0, 6.0, 1.0, 60.0, 140, MinepieceModParticles.LIGHT_SMALL),
         // NOUVELLES capacités exigées par le plan 03/04/05 : la source ne contient AUCUNE ROOM,
         // aucun échange et aucune découpe. Les valeurs sont CHOISIES (voir DevilFruitOpeConfig).
         DevilFruitMove.buff("room", "Room", MinepieceModMobEffects.ROOM_BOUND, 200, 0, 80.0, 300,
            MinepieceModParticles.LIGHT_SMALL),
         DevilFruitMove.melee("shambles", "Shambles", 0.0, 8.0, 0.0, 40.0, 120, MinepieceModParticles.DARK),
         DevilFruitMove.melee("amputation", "Amputation", 12.0, 5.0, 0.5, 100.0, 200, MinepieceModParticles.LIGHT_SMALL)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_ARO_ARO, List.of(
         DevilFruitMove.projectile("arrow", "Aro Arrow", 15.0, 80, 0.5, 50.0, 100, MinepieceModParticles.SLASH),
         DevilFruitMove.melee("move", "Aro Move", 15.0, 6.0, 1.0, 50.0, 100, MinepieceModParticles.SLASH),
         DevilFruitMove.melee("combo", "Aro Combo", 18.0, 12.0, 2.0, 60.0, 140, MinepieceModParticles.SLASH),
         DevilFruitMove.buff("passive", "Aro Passive", MobEffects.STRENGTH, 60, 1, 40.0, 80, MinepieceModParticles.SLASH)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_KAGE_KAGE, List.of(
         DevilFruitMove.melee("attackoverhead", "Attack Overhead", 15.0, 12.0, 1.0, 50.0, 120, MinepieceModParticles.DARK),
         DevilFruitMove.projectile("brickbat", "Brick Bat", 15.0, 80, 0.25, 50.0, 100, MinepieceModParticles.DARK),
         DevilFruitMove.melee("tsunotokage", "Tsuno Tokage", 15.0, 4.0, 0.5, 50.0, 100, MinepieceModParticles.DARK),
         DevilFruitMove.melee("doppelman", "Doppelman", 15.0, 6.0, 1.0, 60.0, 140, MinepieceModParticles.DARK),
         DevilFruitMove.melee("islandshatterpunch", "Island Shatter Punch", 44.0, 24.0, 1.0, 80.0, 200, MinepieceModParticles.DARK),
         DevilFruitMove.buff("shadowasgard", "Shadow Asgard", MobEffects.STRENGTH, 100, 2, 80.0, 240, MinepieceModParticles.DARK)
      ));

      MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_SORU_SORU, List.of(
         DevilFruitMove.melee("attackoverhead", "Attack Overhead", 15.0, 12.0, 1.0, 50.0, 120, MinepieceModParticles.THUNDER_BLUE),
         DevilFruitMove.melee("attacksweep", "Attack Sweep", 12.0, 6.0, 1.0, 50.0, 100, MinepieceModParticles.THUNDER_BLUE),
         DevilFruitMove.melee("raitei", "Raitei", 15.0, 6.0, 1.0, MobEffects.SLOWNESS, 20, 1, 60.0, 140, MinepieceModParticles.THUNDER_BLUE),
         DevilFruitMove.melee("hebunrifire", "Hebunri Fire", 20.0, 6.0, 1.0, 60.0, 140, MinepieceModParticles.THUNDER_BLUE),
         DevilFruitMove.melee("furugora", "Furugora", 16.0, 8.0, 0.5, MobEffects.SLOWNESS, 20, 1, 50.0, 120, MinepieceModParticles.THUNDER_BLUE),
         DevilFruitMove.melee("mamaraid", "Mama Raid", 25.0, 12.0, 1.0, 70.0, 180, MinepieceModParticles.THUNDER_BLUE),
         DevilFruitMove.melee("ikoku", "Ikoku", 30.0, 20.0, 2.0, 70.0, 180, MinepieceModParticles.THUNDER_BLUE)
      ));
   }

   private static final Map<String, String[]> PROJECTILE_VISUALS = new HashMap<>();

   static {
      // Move id → {modèle GeoLib, texture}. Réutilise les assets ver15 (geo/animations/textures).
      // Magu (magma)
      v("daifunka", "fist_small", "tex_magma");
      v("inugamiguren", "inugami_guren", "inugami_guren");
      v("ryuseikazan", "fist_small", "tex_magma");
      // Mera (feu)
      v("hiken", "fist_small", "flame_tiger");
      v("higan", "fist_small", "flame_tiger");
      v("hotarubi", "ball_maximum", "flame_tiger");
      // Goro (foudre)
      v("raigo", "ball_maximum", "meteor");
      // Hie (glace)
      v("icepartisan", "partisan", "tex_ice");
      v("pheasantbeak", "phoenix", "tex_ice");
      v("icespikes", "partisan", "tex_ice");
      // Pika (lumière)
      v("laser1", "ball_maximum", "meteor");
      v("laser2", "ball_maximum", "meteor");
      v("magatama", "projectile_slash", "clear");
      // Yami (ténèbres)
      v("darkball", "rubble", "tex_black");
      v("darkmatter", "rubble", "tex_black");
      // Suna (sable)
      v("desertspada", "projectile_slash", "slash_sand");
      v("barjan", "entity_slash", "slash_sand");
      v("sables", "tornado", "slash_sand");
      v("desertlaspada", "projectile_slash", "slash_sand");
      // Moku (fumée)
      v("whiteblow", "fist_small", "clear");
      v("whitevine", "tornado", "slash_white");
      // Mori (forêt)
      v("root", "rock_fragment", "rock_fragment");
      v("rootgiant", "rock_fragment", "rock_fragment");
      v("rootsurround", "rock_fragment", "rock_fragment");
      // Zoan — Rankyaku / projectiles
      v("rankyaku", "projectile_slash", "projectile_slash");
      v("lupusfall", "jabra_wolf_projectile", "jabra_wolf_projectile");
      v("gaicho", "projectile_slash", "projectile_slash_gaicho");
      v("impactball", "rubble", "clear");
      v("narikabura", "projectile_tooth", "tex_ice");
      v("bluebird", "phoenix", "tex_ice");
      v("kongokabura", "projectile_tooth", "clear");
      v("kaifu", "projectile_slash", "projectile_slash");
      v("borobreath", "rubble", "clear");
      v("barizodon", "projectile_slash", "projectile_slash");
      v("blackcofee", "rubble", "clear");
      v("brachiolauncher", "ball_maximum", "meteor");
      v("sparkingqueen", "ball_maximum", "meteor");
      v("shigan", "projectile_slash", "projectile_slash");
      v("gagan", "projectile_tooth", "clear");
      // Horo (fantômes)
      v("negativehollow", "ghost1", "ghost1_1");
      v("minihollow", "ghost2", "clear");
      v("tokuhoro", "ghost2", "clear");
      v("kumac", "kumac", "kumac");
      // Nikyu (patte)
      v("padho", "pad", "clear");
      v("ursusshock", "pad", "clear");
      // Zushi (météores / gravité)
      v("meteorsmall", "ball_maximum", "meteor");
      v("rubble", "rubble", "clear");
      v("meteor", "ball_maximum", "meteor");
      // Bisu (biscuit)
      v("biscuitwarrior", "rock_fragment", "rock_fragment");
      v("biscuitwarrior2", "rock_fragment", "rock_fragment");
      // Mochi
      v("yakimochi", "fist_small", "flame_tiger");
      v("musodonatsu", "muso_donatsu", "tex_white3");
      // Uta (notes)
      v("noteattack", "note_3d", "clear");
      v("notelaser", "note_3d", "clear");
      // Kira / Riki (rochers)
      v("stonethrowing", "rock_fragment", "rock_fragment");
      // FRUIT-KIRA_KIRA-05 : le visuel suit l'identité. On RÉUTILISE l'actif cristallin existant
      // (ice_ball + tex_ice, aspect facetté) au lieu d'inventer un modèle : c'est le plus proche du
      // diamant dans le jeu d'assets. Un actif dédié serait un mieux (cf. §6 du contrat).
      v("diamondshard", "ice_ball", "tex_ice");
      v("diamondavalanche", "ice_ball", "tex_ice");
      v("stonethrowinglarge", "rock_fragment", "rock_fragment");
      // Supa (lames)
      v("projectileslash", "projectile_slash", "projectile_slash");
      // Mero (cœur)
      v("pistolkiss", "ball_maximum", "clear");
      // Bomu (explosion)
      v("nosefancycannon", "ball_maximum", "clear");
      // Aro (flèche)
      v("arrow", "aroaro_arrow", "aroaro_arrow");
      // Kage (ombres)
      v("brickbat", "rubble", "tex_black");
   }

   private static void v(String moveId, String model, String texture) {
      PROJECTILE_VISUALS.put(moveId, new String[] { model, texture });
   }

   /** Visuel (modèle + texture) du projectile d'un move, ou {@code null} si non défini. */
   public static String[] projectileVisual(String moveId) {
      return PROJECTILE_VISUALS.get(moveId);
   }

   private static final Map<String, String[]> MELEE_VISUALS = new HashMap<>();

   static {
      // Move mêlée/summon id → {modèle GeoLib, texture} (entité visuelle suit le propriétaire, one-shot).
      // Poings de feu / magma
      // FRUIT-MOCHI_MOCHI-03 : le bras du mochi a SA texture (mochi.png, créée), et non celle de Luffy.
      // La texture était écrite en dur dans la machinerie du bras : tout Fruit qui l'empruntait héritait
      // du visage de Luffy sans que personne ne l'ait décidé.
      mv("mochipistol", "entity_gomu_arm", "mochi");
      mv("mochigatling", "entity_gomu_arm", "mochi");
      mv("hiken", "fist_small", "flame_tiger");
      mv("volcanicexplosion", "fist_small", "tex_magma");
      mv("daienkai", "fist_small", "flame_tiger");
      // Glace
      mv("iceball", "ice_ball", "tex_ice");
      mv("iceage", "ice_ball", "tex_ice");
      // Foudre
      mv("elthor", "ball_maximum", "meteor");
      mv("sango", "ball_maximum", "meteor");
      // Ténèbres
      mv("kurouzu", "rubble", "tex_black");
      mv("blackhole", "rubble", "tex_black");
      // Fumée
      mv("whitelauncher", "fist_small", "clear");
      mv("whiteout", "tornado", "slash_blue");
      // Mera zone de feu
      mv("hibashira", "tornado", "slash_blue");
      mv("kaenryuo", "fist_small", "flame_tiger");
      // Invocations (statiques, durée allongée)
      mv("doppelman", "gecko_moria", "tex_black");
      mv("raitei", "zeus", "zeus");
      mv("hebunrifire", "prometheus", "prometheus");
      mv("notewarrior", "note_warrior1", "note_warrior1");
      mv("summontotmusica", "muso_donatsu", "tex_white3");
   }

   private static void mv(String moveId, String model, String texture) {
      MELEE_VISUALS.put(moveId, new String[] { model, texture });
   }

   /** Visuel (modèle + texture) de l'entité mêlée/summon d'un move, ou {@code null} si non défini. */
   public static String[] meleeVisual(String moveId) {
      return MELEE_VISUALS.get(moveId);
   }

   private static void putZoan(Item fruit, List<DevilFruitMove> moves) {
      MOVES.put(fruit, moves);
   }

   /**
    * Première technique portant cet identifiant, dans n'importe quel Fruit, ou {@code null}.
    *
    * <p>Sert au <b>Rokushiki</b> : les techniques de l'art martial sont déjà déclarées, Fruit par Fruit,
    * avec les valeurs de ver15 ; les redéclarer dans une seconde liste ferait deux sources de vérité
    * qui divergeraient au premier ajustement. On les <b>retrouve</b> donc ici — si une valeur change
    * dans un contrat de Fruit, l'art martial suit.</p>
    */
   public static DevilFruitMove moveById(String id) {
      for (List<DevilFruitMove> liste : MOVES.values()) {
         for (DevilFruitMove m : liste) {
            if (m.id().equals(id)) {
               return m;
            }
         }
      }
      return null;
   }

   public static List<DevilFruitMove> movesFor(Item fruit) {
      return MOVES.getOrDefault(fruit, List.of());
   }

   /** Autotest : résume l'état du registre + valide le catalogue (CORE-002 : doublons fruit+technique, valeurs invalides). */
   public static String selfTest() {
      int fruits = MOVES.size();
      int moves = MOVES.values().stream().mapToInt(List::size).sum();
      int visuals = PROJECTILE_VISUALS.size() + MELEE_VISUALS.size();

      // CORE-002 : l'identifiant est fruit+technique → un doublon n'existe que si un même fruit
      // déclare deux fois la même technique. Les valeurs doivent être cohérentes.
      List<String> issues = new ArrayList<>();
      // move.id partagés entre fruits (ambiguïté pour la résolution visuelle globale, cf CORE-017) :
      Set<String> globalIds = new HashSet<>();
      List<String> crossFruit = new ArrayList<>();

      for (Map.Entry<Item, List<DevilFruitMove>> entry : MOVES.entrySet()) {
         String fruitId = BuiltInRegistries.ITEM.getKey(entry.getKey()).toString();
         Set<String> localIds = new HashSet<>();
         for (DevilFruitMove m : entry.getValue()) {
            if (!localIds.add(m.id())) {
               issues.add("doublon technique \"" + m.id() + "\" dans " + fruitId);
            }
            if (!globalIds.add(m.id())) {
               crossFruit.add(fruitId + "/" + m.id());
            }
            if (m.damage() < 0) issues.add(fruitId + "/" + m.id() + " : dégâts négatifs");
            if (m.energyCost() < 0) issues.add(fruitId + "/" + m.id() + " : coût énergie négatif");
            if (m.cooldownTicks() < 0) issues.add(fruitId + "/" + m.id() + " : cooldown négatif");
            if (m.type() == MoveType.PROJECTILE) {
               if (m.lifetime() <= 0) issues.add(fruitId + "/" + m.id() + " : durée de vie projectile invalide");
               if (m.speed() <= 0) issues.add(fruitId + "/" + m.id() + " : vitesse projectile invalide");
            }
            if (m.type() == MoveType.MELEE && m.range() <= 0) {
               issues.add(fruitId + "/" + m.id() + " : portée mêlée invalide");
            }
         }
      }

      StringBuilder sb = new StringBuilder("Devil Fruit power layer: ")
         .append(fruits).append(" fruits, ").append(moves).append(" techniques, ").append(visuals).append(" visuels");
      if (issues.isEmpty()) {
         sb.append(" — catalogue valide (0 doublon fruit+technique, valeurs cohérentes)");
      } else {
         sb.append(" — ").append(issues.size()).append(" problème(s)");
         LOGGER.warn("[MinePiece shim] Validation catalogue : {} problème(s)", issues.size());
         for (String issue : issues) {
            LOGGER.warn("[MinePiece shim]   - {}", issue);
         }
      }
      if (!crossFruit.isEmpty()) {
         LOGGER.warn("[MinePiece shim] {} technique(s) partagée(s) entre fruits (ambiguïté visuelle, cf CORE-017).",
            crossFruit.size());
      }
      sb.append(" | ").append(DevilFruitBalance.summary());
      return sb.toString();
   }

   // ==================== Équilibrage (CORE-025) ====================

   /** Copie immuable du catalogue tel que déclaré (valeurs ver15) — base de recalcul idempotent. */
   private static final Map<Item, List<DevilFruitMove>> BASE_MOVES = new HashMap<>();

   /** Fige les valeurs d'origine. Appelé une fois, après l'initialisation statique du catalogue. */
   public static void snapshotBase() {
      if (!BASE_MOVES.isEmpty()) {
         return;
      }
      BASE_MOVES.putAll(MOVES);
   }

   /**
    * Applique les budgets d'équilibrage (CORE-025) et journalise chaque correction.
    *
    * <p>Idempotent : repart toujours de {@link #BASE_MOVES} (valeurs ver15), donc une modification de
    * la configuration d'équilibrage ne s'accumule jamais. Seuls les dégâts sont touchés ; les coûts,
    * recharges, effets et portées restent ceux de ver15.</p>
    */
   public static void applyBalanceBudgets() {
      snapshotBase();
      DevilFruitBalance.clear();
      for (Map.Entry<Item, List<DevilFruitMove>> entry : BASE_MOVES.entrySet()) {
         String fruitId = String.valueOf(BuiltInRegistries.ITEM.getKey(entry.getKey()));
         List<DevilFruitMove> adjusted = new ArrayList<>(entry.getValue().size());
         for (DevilFruitMove move : entry.getValue()) {
            String key = fruitId + "/" + move.id();
            double after = DevilFruitBalance.budgetedDamage(key, move.id(), move.energyCost(), move.damage());
            adjusted.add(after == move.damage() ? move : move.withDamage(after));
            if (after != move.damage()) {
               DevilFruitBalance.enrich(key, move.type().name(), move.cooldownTicks());
            }
         }
         MOVES.put(entry.getKey(), List.copyOf(adjusted));
      }
      LOGGER.info("[MinePiece shim] {}", DevilFruitBalance.summary());
   }

   /** Détail des corrections d'équilibrage appliquées (pour la commande de test). */
   public static Collection<DevilFruitBalance.Adjustment> balanceAdjustments() {
      return DevilFruitBalance.lastReport();
   }
}
