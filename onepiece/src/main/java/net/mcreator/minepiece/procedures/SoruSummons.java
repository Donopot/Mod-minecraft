package net.mcreator.minepiece.procedures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

import net.mcreator.minepiece.entity.SummonEntity;
import net.mcreator.minepiece.init.MinepieceModEntities;

/**
 * <b>SORU SORU — les trois mécaniques propres du Fruit (deux invocations, une ressource, des ordres).</b>
 *
 * <h2>Ce que dit la source</h2>
 * <ul>
 *   <li>{@code RaiteiProcedure} lit une clé {@code ZEUS_UUID} et {@code HebunriFireProcedure} une clé
 *       {@code PROMETHEUS_UUID} : les deux invocations sont <b>Zeus</b> (la foudre) et <b>Prometheus</b>
 *       (le feu), chacune suivie par l'UUID de son propriétaire.</li>
 *   <li>{@code IkokuProcedure} lit en plus une {@code PAIR_UUID} : les invocations se <b>pairent</b> à un
 *       partenaire. D'où l'importance de savoir <b>qui</b> commande.</li>
 *   <li>{@code SetSummonedEntityProcedure} / {@code StartTechniqueEntityProcedure} : la source
 *       <b>mémorise</b> l'invocation au lieu de la compter sur place.</li>
 * </ul>
 *
 * <h2>Le défaut que ce service corrige</h2>
 * <p>Le lancement générique du port comptait les invocations <b>par proximité</b> (128 blocs). Une
 * invocation laissée loin — après une reconnexion, un rappel, un vol en bateau — échappait donc au
 * plafond, et l'on pouvait en empiler une seconde. Le registre de ce service compte <b>par
 * propriétaire et par genre</b>, sans dépendre de la distance : un rappel <b>remplace</b>, il n'ajoute
 * pas.</p>
 *
 * <h2>Les trois règles tenues</h2>
 * <ol>
 *   <li><b>Deux invocations distinctes, qui finissent.</b> Zeus attaque à vue, Prometheus garde son
 *       maître, les deux modèles diffèrent, et les deux <b>expirent</b> — la source leur donne 200 ticks.</li>
 *   <li><b>Une seule ressource.</b> L'invocation n'a pas de réserve propre : c'est celle du porteur qui
 *       paie, et un rappel remplace au lieu de dupliquer.</li>
 *   <li><b>Seul le propriétaire commande, et les alliés ne sont pas visés.</b> Les deux règles sont
 *       séparées : bien obéir ne dispense pas de bien choisir ses cibles.</li>
 * </ol>
 */
public final class SoruSummons {

   /** Plafond du contrat : deux invocations, celles de la source. */
   public static final int MAX = 2;

   /** Genre d'invocation, avec ce qui le rend distinct de l'autre. */
   public enum Kind {
      /** Zeus — la foudre. Il attaque à vue, et il frappe fort. */
      RAITEI("raitei", "zeus", "zeus", SummonEntity.Order.ATTAQUER, 12.0),
      /** Prometheus — le feu. Il garde son maître, et il frappe moins fort mais de près. */
      HEBUNRI("hebunrifire", "prometheus", "prometheus", SummonEntity.Order.DEFENDRE, 8.0);

      private final String moveId;
      private final String model;
      private final String texture;
      private final SummonEntity.Order order;
      private final double damage;

      Kind(String moveId, String model, String texture, SummonEntity.Order order, double damage) {
         this.moveId = moveId;
         this.model = model;
         this.texture = texture;
         this.order = order;
         this.damage = damage;
      }

      public String moveId() {
         return moveId;
      }

      public SummonEntity.Order order() {
         return order;
      }

      public String model() {
         return model;
      }

      public String texture() {
         return texture;
      }

      public double damage() {
         return damage;
      }
   }

   private static final Map<UUID, Map<Kind, SummonEntity>> ACTIVES = new HashMap<>();

   private static int remplacements;
   private static int creations;
   private static int expirations;

   private SoruSummons() {
   }

   public static int remplacements() {
      return remplacements;
   }

   public static int creations() {
      return creations;
   }

   public static int expirations() {
      return expirations;
   }

   public static void resetCompteurs() {
      remplacements = 0;
      creations = 0;
      expirations = 0;
      for(var list:new ArrayList<>(ACTIVES.values()))for(var e:list.values())e.discard();
      ACTIVES.clear();
   }

   /**
    * Invoque (ou <b>remplace</b>) l'invocation de ce genre pour ce porteur.
    *
    * <p>Une seconde demande du même genre <b>écarte la première</b> : c'est le sens de « aucune
    * duplication après reconnexion ou rappel ». Le plafond global reste {@value #MAX}, mais il ne
    * bloque pas un remplacement légitime — un rappel n'est pas une invocation supplémentaire.</p>
    */
   public static SummonEntity summon(ServerPlayer player, Kind kind) {
      if (!(player.level() instanceof ServerLevel level)) {
         return null;
      }
      Map<Kind, SummonEntity> parGenre = ACTIVES.computeIfAbsent(player.getUUID(), k -> new LinkedHashMap<>());
      SummonEntity ancienne = parGenre.get(kind);
      if (ancienne != null) {
         ancienne.discard();
         remplacements++;
      } else if (parGenre.size() >= MAX) {
         return null;
      }
      SummonEntity e = MinepieceModEntities.SUMMON.spawn(level, player.blockPosition(),
         EntitySpawnReason.MOB_SUMMONED);
      if (e == null) {
         return null;
      }
      e.setOwnerEntity(player);
      e.setDamage(kind.damage());
      e.setModel(kind.model());
      e.setTexture(kind.texture());
      e.setOrder(kind.order());
      Vec3 look = player.getLookAngle();
      e.setPos(player.getX() + look.x * 1.5, player.getY(), player.getZ() + look.z * 1.5);
      e.setYRot(player.getYRot());
      e.lifetime(DevilFruitRegistry.summonLifetime(kind.moveId()));
      parGenre.put(kind, e);
      creations++;
      return e;
   }

   /** L'invocation active de ce genre, ou {@code null}. */
   public static SummonEntity active(ServerPlayer player, Kind kind) {
      Map<Kind, SummonEntity> parGenre = ACTIVES.get(player.getUUID());
      return parGenre == null ? null : parGenre.get(kind);
   }

   /**
    * Nombre d'invocations <b>enregistrées</b> pour ce porteur.
    *
    * <p>Compté sur le registre, pas sur la proximité : c'est ce qui empêche la duplication après une
    * reconnexion, quand l'invocation n'est plus dans le champ de vision.</p>
    */
   public static int count(ServerPlayer player) {
      Map<Kind, SummonEntity> parGenre = ACTIVES.get(player.getUUID());
      if (parGenre == null) {
         return 0;
      }
      parGenre.entrySet().removeIf(e -> !e.getValue().isAlive());
      return parGenre.size();
   }

   /** Retire une invocation, en nommant la cause. */
   public static void release(ServerPlayer player, Kind kind, String cause) {
      Map<Kind, SummonEntity> parGenre = ACTIVES.get(player.getUUID());
      if (parGenre == null) {
         return;
      }
      SummonEntity e = parGenre.remove(kind);
      if (e != null) {
         e.discard();
      }
   }

   /** Vide le registre d'un porteur — sans toucher aux entités des autres. */
   public static void releaseAll(ServerPlayer player, String cause) {
      Map<Kind, SummonEntity> parGenre = ACTIVES.remove(player.getUUID());
      if (parGenre == null) {
         return;
      }
      for (SummonEntity e : new ArrayList<>(parGenre.values())) {
         e.discard();
      }
   }

   /**
    * Seul le propriétaire commande.
    *
    * <p>Séparé de la règle de ciblage : un allié qui donne un ordre échoue <b>ici</b>, et cela n'a rien
    * à voir avec le fait que l'invocation doive ou non l'attaquer.</p>
    */
   public static boolean ownerOnly(ServerPlayer demandeur, SummonEntity invocation) {
      LivingEntity proprietaire = invocation.getOwner();
      return proprietaire != null && demandeur.getUUID().equals(proprietaire.getUUID());
   }

   /** L'invocation doit-elle épargner cette cible ? (propriétaire et ses alliés) */
   public static boolean mustSpare(SummonEntity invocation, LivingEntity cible) {
      LivingEntity proprietaire = invocation.getOwner();
      if (proprietaire == null) {
         return false;
      }
      if (cible.getUUID().equals(proprietaire.getUUID())) {
         return true;
      }
      return !CombatSkillLogic.isValidTarget(proprietaire, cible);
   }

   /** Entretien : expiration naturelle, mort du propriétaire, déconnexion. */
   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (Map.Entry<UUID, Map<Kind, SummonEntity>> entree : new ArrayList<>(ACTIVES.entrySet())) {
            Map<Kind, SummonEntity> parGenre = entree.getValue();
            ServerPlayer porteur = null;
            for (SummonEntity e : parGenre.values()) {
               if (e.getOwner() instanceof ServerPlayer p) {
                  porteur = p;
                  break;
               }
            }
            if (porteur == null) {
               continue;
            }
            for (Kind kind : Kind.values()) {
               SummonEntity e = parGenre.get(kind);
               if (e != null && !e.isAlive()) {
                  parGenre.remove(kind);
                  expirations++;
               }
            }
            if (!BatchRules.usable(porteur,(ServerLevel)parGenre.values().stream().findFirst().map(e->e.level()).orElse(porteur.level()),net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_SORU_SORU)) {
               releaseAll(porteur, "mort");
            }
         }
      });
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
         ServerPlayer joueur = handler.getPlayer();
         if (joueur != null) {
            releaseAll(joueur, "deconnexion");
         }
      });
   }
}
