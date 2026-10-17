package net.mcreator.minepiece.procedures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.phys.Vec3;

import net.mcreator.minepiece.entity.SummonEntity;
import net.mcreator.minepiece.init.MinepieceModEntities;

/**
 * <b>UTA UTA — les trois mécaniques propres du Fruit (le domaine, la sortie, Tot Musica).</b>
 *
 * <h2>Ce que dit la source</h2>
 * <ul>
 *   <li>{@code AITotMusicaProcedure} et {@code AITotMusicaEnergyProcedure} lisent {@code cnt1},
 *       {@code cnt_target}, {@code cnt_x} et une clé {@code OWNER_UUID}, et posent un drapeau
 *       {@code flag_die}. L'invocation <b>suit l'UUID de son maître</b> et sait mourir.</li>
 *   <li>{@code LogicOwnerExistProcedure} est appelé par l'IA : la source vérifie que le maître
 *       <b>existe encore</b> avant de continuer. C'est exactement la « sortie garantie ».</li>
 *   <li>{@code SkillUtaUtaProcedure} dispatche sur 21–26 et 29 : attaques de notes, laser, bouclier,
 *       pose, guerrier de notes, invocation de Tot Musica, coup de poing de Tot Musica.</li>
 * </ul>
 *
 * <h2>Le défaut que ce service évite</h2>
 * <p>Un domaine musical qui applique son effet <b>à chaque tick</b> transforme un concert en sirène
 * d'usine : l'effet est re-posé sans arrêt, les particules pleuvent, et le serveur paie pour rien. La
 * source compte par pas ({@code cnt}), pas par tick. Ici, le domaine applique son effet <b>une fois par
 * mesure</b> ({@value #RHYTHM} ticks), et la durée est bornée.</p>
 *
 * <h2>Les trois règles tenues</h2>
 * <ol>
 *   <li><b>Zones rythmées et bornées</b> : une application par mesure, et l'arrêt à l'échéance — le
 *       nombre d'applications ne peut pas dépasser ce que la durée autorise.</li>
 *   <li><b>Sortie garantie</b> : quatre causes sont traitées séparément et comptées — mort du porteur,
 *       déconnexion, scellement, expiration. Un domaine dont on ne sort pas est une prison, pas un
 *       pouvoir.</li>
 *   <li><b>Tot Musica fonctionne et expire</b> : c'est une invocation avec un propriétaire et une durée
 *       de vie, pas un décor de mêlée.</li>
 * </ol>
 */
public final class UtaDomain {

   /** Durée de vie d'un domaine, en ticks — bornée, jamais permanente. */
   public static final int ZONE_TICKS = 200;

   /** Une mesure : le domaine applique son effet une fois par {@value} ticks, pas à chaque tick. */
   public static final int RHYTHM = 20;

   /** Rayon du domaine. */
   public static final double RADIUS = 12.0;

   private static final class Zone {
      // La référence du porteur est GARDÉE : un joueur de test n'est pas dans la liste des joueurs du
      // serveur, donc un tick qui le chercherait par UUID ne le trouverait jamais et le domaine ne
      // battrait pas. Même pratique que le service d'amputation, qui garde ses références.
      final ServerPlayer porteur;
      final ServerLevel level;
      final Vec3 centre;
      final double rayon;
      final UUID auteur;
      int restant;
      int prochaineMesure;
      int applications;

      Zone(ServerPlayer porteur, ServerLevel level, Vec3 centre, double rayon, UUID auteur) {
         this.porteur = porteur;
         this.level = level;
         this.centre = centre;
         this.rayon = rayon;
         this.auteur = auteur;
         this.restant = ZONE_TICKS;
         this.prochaineMesure = 0;
      }
   }

   private static final Map<UUID, Zone> DOMAINES = new HashMap<>();
   private static final Map<UUID, SummonEntity> TOT_MUSICA = new HashMap<>();

   private static int sortiesMort;
   private static int sortiesDeconnexion;
   private static int sortiesScellement;
   private static int sortiesEcheance;
   private static int totMusicaInvoques;

   private UtaDomain() {
   }

   public static int sortiesMort() {
      return sortiesMort;
   }

   public static int sortiesDeconnexion() {
      return sortiesDeconnexion;
   }

   public static int sortiesScellement() {
      return sortiesScellement;
   }

   public static int sortiesEcheance() {
      return sortiesEcheance;
   }

   public static int totMusicaInvoques() {
      return totMusicaInvoques;
   }

   public static void resetCompteurs() {
      sortiesMort = 0;
      sortiesDeconnexion = 0;
      sortiesScellement = 0;
      sortiesEcheance = 0;
      totMusicaInvoques = 0;
      for(var z:new ArrayList<>(DOMAINES.values()))exit(z.porteur,"reset");
      for(var e:TOT_MUSICA.values())e.discard();
      DOMAINES.clear();
      TOT_MUSICA.clear();
   }

   // ============================================================================================
   // ① LE DOMAINE — rythmé et borné
   // ============================================================================================

   /** Ouvre (ou rouvre) le domaine musical du porteur. */
   public static boolean enter(ServerPlayer player, double rayon) {
      if (!(player.level() instanceof ServerLevel level)) {
         return false;
      }
      DOMAINES.put(player.getUUID(), new Zone(player, level, player.position(),
         Math.min(rayon <= 0.0 ? RADIUS : rayon, RADIUS), player.getUUID()));
      return true;
   }

   public static boolean inDomain(ServerPlayer player) {
      return DOMAINES.containsKey(player.getUUID());
   }

   /** Ticks restants du domaine (0 s'il n'y en a pas). */
   public static int domainRemaining(ServerPlayer player) {
      Zone z = DOMAINES.get(player.getUUID());
      return z == null ? 0 : z.restant;
   }

   /**
    * Nombre de mesures appliquées par ce domaine.
    *
    * <p>La borne est le cœur de la règle « sans répétition abusive » : ce nombre ne peut pas dépasser
    * {@code 1 + (ZONE_TICKS - restant) / RHYTHM}. Une application par tick le ferait exploser.</p>
    */
   public static int measuresOf(ServerPlayer player) {
      Zone z = DOMAINES.get(player.getUUID());
      return z == null ? 0 : z.applications;
   }

   /** Un porteur est-il dans le domaine de cet auteur ? */
   public static boolean isInside(ServerPlayer auteur, ServerPlayer cible) {
      Zone z = DOMAINES.get(auteur.getUUID());
      return z != null && cible.position().distanceTo(z.centre) <= z.rayon;
   }

   /** Ferme le domaine — la sortie est unique, quelle que soit la cause, et chaque cause est comptée. */
   public static void exit(ServerPlayer player, String cause) {
      Zone z = DOMAINES.remove(player.getUUID());

      switch (cause) {
         case "mort" -> sortiesMort++;
         case "deconnexion" -> sortiesDeconnexion++;
         case "scellement" -> sortiesScellement++;
         default -> sortiesEcheance++;
      }
      if (z != null) {
         SummonEntity tm = TOT_MUSICA.remove(player.getUUID());
         if (tm != null) {
            tm.discard();
         }
      }
   }

   // ============================================================================================
   // ③ TOT MUSICA — fonctionnel et borné
   // ============================================================================================

   /** Invoque Tot Musica, adossé à son maître et à une durée de vie. */
   public static SummonEntity summonTotMusica(ServerPlayer player) {
      if (!(player.level() instanceof ServerLevel level)) {
         return null;
      }
      SummonEntity ancien = TOT_MUSICA.get(player.getUUID());
      if (ancien != null && ancien.isAlive()) {
         ancien.discard();
      }
      SummonEntity e = MinepieceModEntities.SUMMON.spawn(level, player.blockPosition(),
         EntitySpawnReason.MOB_SUMMONED);
      if (e == null) {
         return null;
      }
      e.setOwnerEntity(player);
      e.setDamage(18.0);
      e.setModel("tot_musica_1");
      e.setTexture("tot_musica_1");
      e.setOrder(SummonEntity.Order.ATTAQUER);
      e.setPos(player.getX(), player.getY(), player.getZ() + 2.0);
      e.lifetime(DevilFruitRegistry.summonLifetime("summontotmusica"));
      TOT_MUSICA.put(player.getUUID(), e);
      totMusicaInvoques++;
      return e;
   }

   /** Tot Musica actif du porteur, ou {@code null}. */
   public static SummonEntity totMusica(ServerPlayer player) {
      return TOT_MUSICA.get(player.getUUID());
   }

   /**
    * Entretien du domaine : une mesure par {@value #RHYTHM} ticks, et les quatre sorties.
    *
    * <p>La cadence est tenue par un compteur de mesure, pas par une condition sur le tick courant : le
    * domaine n'applique pas son effet « quand ça tombe bien », il l'applique <b>en mesure</b>.</p>
    */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (Map.Entry<UUID, Zone> entree : new ArrayList<>(DOMAINES.entrySet())) {
            Zone z = entree.getValue();
            ServerPlayer porteur = z.porteur;
            if (porteur == null || porteur.hasDisconnected()) {
               continue; // la déconnexion s'en charge
            }
            if (!porteur.isAlive()) {
               exit(porteur, "mort");
               continue;
            }
            if (!BatchRules.usable(porteur,z.level,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_UTA_UTA)) {
               exit(porteur, "scellement");
               continue;
            }
            if (--z.restant <= 0) {
               exit(porteur, "echeance");
               continue;
            }
            // La mesure : une seule application par RHYTHM ticks, jamais une par tick.
            if (z.restant % RHYTHM == 0) {
               for (ServerPlayer auditeur : new ArrayList<>(server.getPlayerList().getPlayers())) {
                  if (auditeur.level()==porteur.level() && (auditeur==porteur||porteur.isAlliedTo(auditeur)) && auditeur.position().distanceTo(z.centre) <= z.rayon) {
                     auditeur.addEffect(new MobEffectInstance(MobEffects.SPEED, RHYTHM * 2, 0, false, false));
                  }
               }
               z.applications++;
            }
         }
      });
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
         ServerPlayer joueur = handler.getPlayer();
         if (joueur != null) {
            exit(joueur, "deconnexion");
         }
      });
   }
}
