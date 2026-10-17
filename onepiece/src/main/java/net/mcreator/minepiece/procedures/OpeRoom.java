package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.ope.DevilFruitOpeConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.init.MinepieceModMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * <b>ROOM et échange de positions</b> — Ope Ope no Mi (FRUIT-OPE_OPE-03/04).
 *
 * <p>Le plan demande une <b>ROOM</b> « avec centre, rayon, durée et <b>affichage pour les
 * observateurs</b> », et un <b>échange de positions</b> « à l'intérieur de ROOM avec <b>validation des
 * destinations</b> », les deux positions devant être « sûres, autorisées et appliquées
 * <b>atomiquement</b> ».</p>
 *
 * <h2>Ce que « atomiquement » veut dire ici</h2>
 * <p>Le piège est de déplacer la première entité, puis de découvrir que la seconde destination est
 * mauvaise : on a alors <b>déplacé quelqu'un pour rien</b>, et le joueur se retrouve dans un état qui
 * n'existe dans aucune règle du contrat. Les <b>deux</b> destinations sont donc validées <b>avant</b>
 * le premier déplacement, et la validation porte sur trois choses distinctes :</p>
 * <ol>
 *   <li><b>l'espace</b> — la boîte de collision de chaque entité doit tenir à sa destination
 *       ({@code level.noCollision}) et la position doit être chargée ;</li>
 *   <li><b>l'autorisation</b> — une destination située dans une <b>zone protégée</b> est refusée via le
 *       prédicat <b>déjà en service</b> ({@link DevilFruitExplosions#isProtectedPosition}), pour que
 *       « protégé » n'ait qu'une seule définition dans tout le mod ;</li>
 *   <li><b>la portée</b> — les deux entités doivent être dans la ROOM du lanceur et à portée l'une de
 *       l'autre.</li>
 * </ol>
 *
 * <h2>La ROOM se ferme toute seule</h2>
 * <p>Une aire persistante est un état qui survit à ce qui l'a créée : elle doit donc avoir <b>toutes</b>
 * ses fins prévues, comme les autres maintiens du mod. Fin par <b>échéance</b> (durée bornée dans le
 * code), par <b>suppression</b> (mer / scellement), par <b>mort</b> du porteur, par <b>déconnexion</b>
 * (sinon l'aire survivrait à son propriétaire) et par <b>changement de dimension</b> (une ROOM ne
 * s'exporte pas). La raison de fermeture est <b>lue</b> par les tests.</p>
 */
public final class OpeRoom {

   /** Une ROOM : une aire, un propriétaire, une durée. */
   private static final class Room {
      final ServerPlayer reference;final ServerLevel world;final java.util.List<net.mcreator.minepiece.entity.EntityGomuArmEntity> visuals=new java.util.ArrayList<>();
      final String dimension;
      final Vec3 centre;
      final double radius;
      final UUID owner;
      MobEffectInstance ownedMarker;
      int ttl;

      Room(ServerPlayer reference,String dimension, Vec3 centre, double radius, UUID owner, int ttl) {
         this.reference=reference;this.world=(ServerLevel)reference.level();
         this.dimension = dimension;
         this.centre = centre;
         this.radius = radius;
         this.owner = owner;
         this.ttl = ttl;
      }
   }

   /** Une ROOM par porteur au maximum — la clé est le porteur, pas la position. */
   private static final Map<UUID, Room> ROOMS = new HashMap<>();

   /** Raison de la dernière fermeture, par porteur. */
   private static final Map<UUID, String> LAST_CLOSE = new HashMap<>();

   /** Références revalidées des porteurs (les joueurs virtuels échappent aux recherches par UUID). */
   private static final Map<UUID, ServerPlayer> OWNER_REFS = new HashMap<>();

   /** Compteurs de diagnostic : un échec de test doit être diagnosticable sans reconstruire le mod. */
   private static int vues;
   private static int ouvertes;
   private static int fermees;
   private static int echangesReussis;
   private static int echangesRefuses;

   private OpeRoom() {
   }

   public static void register() {
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server->clearAll());
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         tick();
         for (ServerPlayer player : new ArrayList<>(server.getPlayerList().getPlayers())) {
            checkOwner(player);
         }
      });
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
         ServerPlayer player = handler.getPlayer();
         if (player != null) {
            close(player.getUUID(), "deconnexion");
            OWNER_REFS.remove(player.getUUID());
         }
      });
   }

   // ============================ ouverture / état ============================

   /**
    * Ouvre (ou réouvre) la ROOM du porteur, centrée sur lui.
    *
    * @return vrai si la ROOM est ouverte
    */
   public static boolean open(ServerPlayer owner) {
      if (owner == null || !(owner.level() instanceof ServerLevel level)) {
         return false;
      }
      if (!BatchRules.usable(owner,level,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_OPE_OPE) || ROOMS.containsKey(owner.getUUID())) {
         return false; // sous suppression, le pouvoir ne s'exprime pas
      }
      int ttl = DevilFruitOpeConfig.roomLifetimeTicks();
      ROOMS.put(owner.getUUID(), new Room(owner,level.dimension().identifier().toString(),
         owner.position(), DevilFruitOpeConfig.roomRadiusBlocks(), owner.getUUID(), ttl));
      OWNER_REFS.put(owner.getUUID(), owner);
      // Marqueur visible par le porteur : la ROOM a une durée, elle doit se voir.
      if(!owner.hasEffect(MinepieceModMobEffects.ROOM_BOUND)){owner.addEffect(new MobEffectInstance(MinepieceModMobEffects.ROOM_BOUND, ttl, 0, false, false));ROOMS.get(owner.getUUID()).ownedMarker=owner.getEffect(MinepieceModMobEffects.ROOM_BOUND);}
      ouvertes++;
      owner.sendSystemMessage(Component.literal("§bROOM §f: rayon " + fmt(DevilFruitOpeConfig.roomRadiusBlocks())
         + " §7pendant §f" + ttl + " §7ticks"), true);
      return true;
   }

   /** La ROOM du porteur est-elle ouverte ? */
   public static boolean isOpen(ServerPlayer owner) {
      return owner != null && ROOMS.containsKey(owner.getUUID()) && ROOMS.get(owner.getUUID()).reference==owner && BatchRules.usable(owner,ROOMS.get(owner.getUUID()).world,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_OPE_OPE);
   }

   /** Nombre de ROOM ouvertes (observable). */
   public static int activeCount() {
      return ROOMS.size();
   }

   /** Durée restante de la ROOM du porteur, ou 0. */
   public static int remainingTicks(ServerPlayer owner) {
      Room room = owner == null ? null : ROOMS.get(owner.getUUID());
      return room == null ? 0 : room.ttl;
   }

   /** Raison de la dernière fermeture pour ce porteur. */
   public static String lastCloseReason(ServerPlayer owner) {
      return owner == null ? "aucune" : LAST_CLOSE.getOrDefault(owner.getUUID(), "aucune");
   }

   /** L'entité est-elle dans la ROOM de ce porteur ? */
   public static boolean contains(ServerPlayer owner, LivingEntity cible) {
      Room room = owner == null ? null : ROOMS.get(owner.getUUID());
      if (room == null || room.reference!=owner || !BatchRules.usable(owner,room.world,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_OPE_OPE) || cible == null) {
         return false;
      }
      if (!(cible.level() instanceof ServerLevel level)) {
         return false;
      }
      if (!level.dimension().identifier().toString().equals(room.dimension)) {
         return false;
      }
      return cible.position().distanceTo(room.centre) <= room.radius;
   }

   /** Centre de la ROOM, ou {@code null}. */
   public static Vec3 centreOf(ServerPlayer owner) {
      Room room = owner == null ? null : ROOMS.get(owner.getUUID());
      return room == null ? null : room.centre;
   }

   // ============================ échange (Shambles) ============================

   /**
    * <b>Échange de positions à l'intérieur de la ROOM</b> (-04) : le porteur prend la place de sa cible,
    * qui prend la sienne.
    *
    * <p>Les deux destinations sont validées <b>avant</b> tout déplacement ; si l'une des deux échoue,
    * <b>rien</b> ne bouge et la méthode retourne {@code false} — un refus ne consomme donc rien, comme
    * l'exige la règle générale du mod.</p>
    *
    * @return vrai si les deux positions ont été échangées
    */
   public static boolean shambles(ServerPlayer caster) {
      if (!(caster.level() instanceof ServerLevel level)) {
         return false;
      }
      Room room = ROOMS.get(caster.getUUID());
      if (room == null || !contains(caster,caster)) {
         echangesRefuses++;
         return false; // hors ROOM : la technique est refusée
      }
      LivingEntity cible = lookedAt(level, caster, room);
      if (cible == null || cible == caster || !cible.isAlive()) {
         echangesRefuses++;
         return false;
      }
      if (cible.position().distanceTo(caster.position()) > DevilFruitOpeConfig.shamblesMaxDistanceBlocks()) {
         echangesRefuses++;
         return false;
      }
      Vec3 destinationCaster = cible.position();
      Vec3 destinationCible = caster.position();
      if (!destinationSure(level, caster, destinationCaster)
            || !destinationSure(level, cible, destinationCible)) {
         echangesRefuses++;
         return false; // UNE seule validation échoue → aucun des deux ne bouge
      }
      // Les deux destinations sont bonnes : on applique. `snapTo` plutôt que `teleportTo` pour que la
      // rotation « précédente » suive — une mesure de visée juste après un échange doit rester
      // déterministe.
      float rotCaster = caster.getYRot();
      float rotCible = cible.getYRot();
      caster.teleportTo(destinationCaster.x, destinationCaster.y, destinationCaster.z);
      caster.setOldPosAndRot();
      cible.teleportTo(destinationCible.x, destinationCible.y, destinationCible.z);
      cible.setOldPosAndRot();
      caster.setDeltaMovement(Vec3.ZERO);cible.setDeltaMovement(Vec3.ZERO);caster.syncVelocity=true;cible.syncVelocity=true;
      caster.setYRot(rotCaster);
      caster.setXRot(caster.getXRot());
      caster.setOldPosAndRot();
      echangesReussis++;
      caster.sendSystemMessage(Component.literal("§bShambles §f: échange avec §b"
         + (cible.hasCustomName() ? cible.getCustomName().getString() : cible.getName().getString())), true);
      vues++;
      return true;
   }

   public static boolean canShambles(ServerPlayer caster){
      if(caster==null || !contains(caster,caster))return false;LivingEntity target=targetInRoom(caster);
      return target!=null && target.position().distanceTo(caster.position())<=DevilFruitOpeConfig.shamblesMaxDistanceBlocks() && destinationSure((ServerLevel)caster.level(),caster,target.position()) && destinationSure((ServerLevel)caster.level(),target,caster.position());
   }
   /**
    * La cible visée par le porteur, à l'intérieur de la ROOM (ou {@code null}).
    *
    * <p>Exposée parce que la découpe (FRUIT-OPE_OPE-05) doit viser <b>exactement</b> la même cible que
    * l'échange : deux résolutions de cible différentes feraient diverger ce que le joueur voit de ce
    * que le pouvoir fait.</p>
    */
   public static LivingEntity targetInRoom(ServerPlayer caster) {
      if (caster == null || !(caster.level() instanceof ServerLevel level)) {
         return null;
      }
      Room room = ROOMS.get(caster.getUUID());
      return room == null ? null : lookedAt(level, caster, room);
   }

   /** La cible visée par le porteur, à l'intérieur de la ROOM (cône de mêlée réutilisé). */
   private static LivingEntity lookedAt(ServerLevel level, ServerPlayer caster, Room room) {
      if(!contains(caster,caster))return null;
      Vec3 regard = caster.getLookAngle();
      LivingEntity meilleure = null;
      double meilleureDistance = Double.MAX_VALUE;
      for (LivingEntity candidat : level.getEntitiesOfClass(LivingEntity.class,
            new AABB(room.centre, room.centre).inflate(room.radius))) {
         if (!BatchRules.target(caster,candidat) || candidat.isInvulnerable()) {
            continue;
         }
         if (!contains(caster, candidat)) {
            continue;
         }
         Vec3 vers = candidat.getBoundingBox().getCenter().subtract(caster.getEyePosition()).normalize();
         if (vers.dot(regard) < CombatSkillLogic.MELEE_CONE_COS) {
            continue;
         }
         double distance = candidat.position().distanceTo(caster.position());
         if (distance < meilleureDistance) {
            meilleure = candidat;
            meilleureDistance = distance;
         }
      }
      return meilleure;
   }

   /**
    * Une destination est-elle <b>sûre</b> et <b>autorisée</b> ?
    *
    * <p>Délégué à {@link SafeDestination} : la règle a été écrite ici pour l'échange, puis
    * <b>réclamée</b> par le déplacement lumineux de Pika Pika. Deux copies d'une règle de sûreté
    * divergeraient au premier ajustement — et l'une des deux finirait par téléporter dans un mur.
    * La factorisation a eu lieu <b>à la deuxième copie</b>, pas à la cinquième.</p>
    */
   private static boolean destinationSure(ServerLevel level, LivingEntity entite, Vec3 destination) {
      return SafeDestination.isSafe(level, entite, destination);
   }

   // ============================ entretien et fermeture ============================

   /** Ferme la ROOM d'un porteur : identifiant, jamais une référence — la clé est la source de vérité. */
   public static void close(UUID owner, String reason) {
      if (owner == null) {
         return;
      }
      Room room = ROOMS.remove(owner);
      if (room == null) {
         return;
      }
      for(var visual:room.visuals)visual.discard();
      Amputation.restoreAllFrom(room.reference,reason);
      LAST_CLOSE.put(owner, reason);
      fermees++;
      ServerPlayer caster = OWNER_REFS.get(owner);
      if (caster != null && !caster.isRemoved()) {
         var current=caster.getEffect(MinepieceModMobEffects.ROOM_BOUND);if(room.ownedMarker!=null && current==room.ownedMarker && current.getDuration()<=room.ttl+1 && current.getAmplifier()==0)caster.removeEffect(MinepieceModMobEffects.ROOM_BOUND);
      }
      OWNER_REFS.remove(owner);
   }

   /** Ferme la ROOM de ce porteur (appel nominatif). */
   public static void closeFrom(ServerPlayer owner, String reason) {
      if (owner != null) {
         close(owner.getUUID(), reason);
      }
   }

   /** Toutes les ROOM — utilisé par le reset de Fruit (FRUIT-RESET-01). */
   public static void clearAll() {
      for (UUID owner : new ArrayList<>(ROOMS.keySet())) {
         close(owner, "reset");
      }
   }

   private static void checkOwner(ServerPlayer player) {
      Room room = ROOMS.get(player.getUUID());
      if (room == null) {
         return;
      }
      if(room.reference!=player){close(player.getUUID(),"instance");return;}
      if (!BatchRules.usable(player,room.world,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_OPE_OPE)){close(player.getUUID(),"suppression");return;}
      if (!player.isAlive() || player.getHealth() <= 0.0F) {
         close(player.getUUID(), "mort");
         return;
      }
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         close(player.getUUID(), "suppression");
         return;
      }
      if (!(player.level() instanceof ServerLevel level)
            || !level.dimension().identifier().toString().equals(room.dimension)) {
         close(player.getUUID(), "dimension");
      }
   }

   private static void tick() {
      List<UUID> aFermer = new ArrayList<>();
      for (Map.Entry<UUID, Room> entree : ROOMS.entrySet()) {
         Room room = entree.getValue();
         room.ttl--;
         if (room.ttl <= 0) {
            aFermer.add(entree.getKey());
            continue;
         }
         ServerLevel level = levelOf(room);
         if (level == null) {
            aFermer.add(entree.getKey());
            continue;
         }
         if(!BatchRules.usable(room.reference,room.world,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_OPE_OPE)){aFermer.add(entree.getKey());continue;}
         room.visuals.removeIf(e->e.isRemoved());
         if(room.ttl%10==0)room.visuals.add(ControlledBatchTechniques.visual(room.reference,"b02_room",room.centre,12));
         for(var visual:room.visuals)visual.setStretch((float)room.radius);

      }
      for (UUID owner : aFermer) {
         close(owner, "echeance");
      }
   }

   private static ServerLevel levelOf(Room room) {
      ServerPlayer caster = OWNER_REFS.get(room.owner);
      if (caster != null && !caster.isRemoved() && caster.level() instanceof ServerLevel level
            && level.dimension().identifier().toString().equals(room.dimension)) {
         return level;
      }
      return null; // sans porteur résolvable, la ROOM n'a plus de centre : elle se ferme
   }

   // ============================ diagnostic ============================

   /**
    * Rapport de diagnostic : un échec doit être diagnosticable sans reconstruire le mod.
    *
    * <p>Compteurs : {@code vues} (entités examinées par un échange), {@code ouvertes}, {@code fermees},
    * {@code echangesReussis}, {@code echangesRefuses}.</p>
    */
   public static String roomReport() {
      return "vues=" + vues + " ouvertes=" + ouvertes + " fermees=" + fermees
         + " echanges_reussis=" + echangesReussis + " echanges_refuses=" + echangesRefuses;
   }

   /** Remet les compteurs à zéro (tests répétables). */
   public static void resetReport() {
      vues = 0;
      ouvertes = 0;
      fermees = 0;
      echangesReussis = 0;
      echangesRefuses = 0;
   }

   private static String fmt(double v) {
      return String.format(java.util.Locale.ROOT, "%.2f", v);
   }
}
