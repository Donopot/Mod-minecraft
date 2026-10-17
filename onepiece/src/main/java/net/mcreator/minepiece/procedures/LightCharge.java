package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.pika.DevilFruitPikaConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.init.MinepieceModMobEffects;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;

/**
 * <b>Avertissement visible et volée bornée</b> — Pika Pika no Mi (FRUIT-PIKA_PIKA-05).
 *
 * <p>Le plan : « Ajouter <b>charge ou avertissement visible</b> aux attaques rapides et barrages »,
 * critère : « un adversaire <b>voit le signal avant l'impact</b> et la rafale a un <b>nombre d'impacts
 * borné</b> ».</p>
 *
 * <h2>La seule façon honnête de « voir avant » est de séparer la charge de l'impact</h2>
 * <p>Une attaque instantanée ne peut pas être annoncée : il faut du <b>temps</b> entre le signal et le
 * coup. La charge dure {@code charge_ticks} ticks (20 par défaut, borné à 60), pendant lesquels elle est
 * <b>visible</b> — halo de particules au-dessus du porteur, marqueur d'effet, son de charge — puis la
 * volée part. L'adversaire a donc une fenêtre réelle pour se mettre à couvert, et cette fenêtre est
 * <b>mesurée</b> par un test plutôt que promis : à l'armement, les impacts valent 0 ; la volée n'existe
 * qu'à un <b>tick ultérieur</b>.</p>
 *
 * <h2>La rafale est bornée par construction</h2>
 * <p>Le nombre d'impacts est fixé par la configuration et <b>plafonné dans le code</b> : une volée n'est
 * pas « autant de projectiles que possible », c'est un nombre décidé. Le compteur d'impacts est
 * conservé, donc un test peut affirmer « borné » et non « probablement borné ».</p>
 */
public final class LightCharge {

   private static final class Charge {
      final TechniqueTerrain.Budget terrain;
      final UUID owner;
      final ServerPlayer reference;final ServerLevel world;
      int fired,interval;
      final ItemStack fruit;
      final DevilFruitMove move;
      int ttl;
      int derniersImpacts = -1;

      Charge(ServerPlayer reference,UUID owner, ItemStack fruit, DevilFruitMove move, int ttl) {
         this.reference=reference;this.world=(ServerLevel)reference.level();
         this.owner = owner;
         this.fruit = fruit;
         this.move = move;this.terrain=TechniqueTerrain.begin(reference,move);
         this.ttl = ttl;
      }
   }

   private static final Map<UUID, Charge> CHARGES = new HashMap<>();
   private static final Map<UUID, ServerPlayer> OWNER_REFS = new HashMap<>();
   private static final Map<UUID, String> LAST_END = new HashMap<>();

   /**
    * Derniers impacts par porteur.
    *
    * <p>Table <b>séparée</b> de la charge, et ce n'est pas un détail : la charge est <b>retirée</b> au
    * moment où la volée part, donc une lecture « via la charge » renvoyait « aucune volée » juste après
    * la volée. Le test lisait -1 pendant que le rapport comptait 6 impacts : une mesure qui interroge un
    * objet disparu ne mesure rien.</p>
    */
   private static final Map<UUID, Integer> LAST_IMPACTS = new HashMap<>();

   private static int armees;
   private static int voleesTirees;
   private static int impactsTires;
   private static int avortees;

   private LightCharge() {
   }

   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> tick());
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server->clearAll());
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
         ServerPlayer player = handler.getPlayer();
         if (player != null) {
            cancel(player, "deconnexion");
            OWNER_REFS.remove(player.getUUID());
         }
      });
   }

   /**
    * Arme la charge : le signal est posé MAINTENANT, l'impact viendra plus tard.
    *
    * @return vrai si la charge est armée
    */
   public static boolean begin(ServerPlayer player, ItemStack fruit, DevilFruitMove move) {
      if (player == null || move == null || !(player.level() instanceof ServerLevel level)) {
         return false;
      }
      if (!BatchRules.usable(player,level,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_PIKA_PIKA) || CHARGES.containsKey(player.getUUID())) {
         return false; // un pouvoir scellé n'annonce rien et ne frappe pas
      }
      int ttl = DevilFruitPikaConfig.chargeTicks();
      CHARGES.put(player.getUUID(), new Charge(player,player.getUUID(), fruit.copy(), move, ttl));
      OWNER_REFS.put(player.getUUID(), player);
      LAST_IMPACTS.remove(player.getUUID());LAST_END.remove(player.getUUID());
      // Signal VISIBLE (particules côté serveur → tous les observateurs les voient) et sonore.
      afficherSignal(level, player);
      level.playSound(null, player.blockPosition(), SoundEvents.RESPAWN_ANCHOR_CHARGE, SoundSource.PLAYERS,
         0.8F, 1.9F);
      armees++;
      return true;
   }

   /** Le porteur est-il en train de charger ? */
   public static boolean isCharging(ServerPlayer player) {
      return player != null && CHARGES.containsKey(player.getUUID());
   }

   /** Ticks restants avant l'impact, ou 0. */
   public static int remainingTicks(ServerPlayer player) {
      Charge c = player == null ? null : CHARGES.get(player.getUUID());
      return c == null ? 0 : c.ttl;
   }

   /** Nombre d'impacts de la dernière volée tirée par ce porteur (-1 = aucune). */
   public static int lastVolleyImpacts(ServerPlayer player) {
      if (player == null) {
         return -1;
      }
      return LAST_IMPACTS.getOrDefault(player.getUUID(), -1);
   }

   /** Raison de la dernière fin de charge (« volee », « annulee:… »). */
   public static String lastEndReason(ServerPlayer player) {
      return player == null ? "aucune" : LAST_END.getOrDefault(player.getUUID(), "aucune");
   }

   /** Interrompt une charge (suppression, mort, retrait du Fruit, déconnexion). */
   public static void cancel(ServerPlayer player, String raison) {
      if (player == null) {
         return;
      }
      Charge c = CHARGES.remove(player.getUUID());OWNER_REFS.remove(player.getUUID());
      if (c != null) {
         avortees++;
         LAST_END.put(player.getUUID(), "annulee:" + raison);
         player.removeEffect(MinepieceModMobEffects.LIGHT_CHARGE);
      }
   }

   /** Interrompt toutes les charges d'un porteur — utilisé par le reset de Fruit. */
   public static void cancelFrom(ServerPlayer player, String raison) {
      cancel(player, raison);
   }

   /** Interrompt toutes les charges — reset de Fruit et fin de test. */
   public static void clearAll() {
      for (UUID owner : new ArrayList<>(CHARGES.keySet())) {
         cancel(OWNER_REFS.get(owner), "reset");
      }
      CHARGES.clear();OWNER_REFS.clear();
   }

   /** Nombre de charges en cours (observable). */
   public static int activeCount() {
      return CHARGES.size();
   }

   private static void tick() {
      for (Map.Entry<UUID, Charge> entree : new ArrayList<>(CHARGES.entrySet())) {
         Charge c = entree.getValue();
         ServerPlayer porteur = c.reference;
         if (!BatchRules.usable(porteur,c.world,net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_PIKA_PIKA) || porteur.isRemoved() || !porteur.isAlive()
               || DevilFruitPowerLogic.isSuppressed(porteur)) {
            cancel(porteur, "suppression");
            continue;
         }
         if (!(porteur.level() instanceof ServerLevel level)) {
            cancel(porteur, "dimension");
            continue;
         }
         c.ttl--;
         if (c.ttl > 0) {
            // Le signal reste visible pendant TOUTE la charge : c'est la fenêtre donnée à l'adversaire.
            afficherSignal(level, porteur);
            continue;
         }
         if(c.interval++%2!=0)continue;
         if(c.fired==0)TechniqueAnimations.playNamed(porteur,"b01_laser2_release");
         tirerVolee(level,porteur,c);c.fired++;
         int impacts=c.fired;LAST_IMPACTS.put(entree.getKey(),impacts);
         if(c.fired<DevilFruitPikaConfig.barrageMaxImpacts())continue;
         porteur.removeEffect(MinepieceModMobEffects.LIGHT_CHARGE);OWNER_REFS.remove(c.owner);
         c.derniersImpacts = impacts;
         LAST_IMPACTS.put(entree.getKey(), impacts);
         CHARGES.remove(entree.getKey());
         LAST_END.put(entree.getKey(), "volee");
         voleesTirees++;
         impactsTires += impacts;
      }
   }

   /** La volée : un nombre d'impacts FIXÉ par la configuration et plafonné dans le code. */
   private static int tirerVolee(ServerLevel level, ServerPlayer porteur, Charge c) {
      int impacts = DevilFruitPikaConfig.barrageMaxImpacts();
      double portee = LightBeams.laserRange();
      double angleBase = Math.toRadians(-8.0);
      double pas = Math.toRadians(16.0) / Math.max(1, impacts - 1);
      int tires = 0;
      for (int i = c.fired; i < c.fired+1; i++) {
         double angle = angleBase + pas * i;
         var rayon = new net.mcreator.minepiece.entity.SkillProjectileEntity(
            net.mcreator.minepiece.init.MinepieceModEntities.SKILL_PROJECTILE,
            porteur.getEyePosition().x, porteur.getEyePosition().y, porteur.getEyePosition().z,
            porteur.getLookAngle().yRot((float) angle), level);
         String[] visuel = DevilFruitRegistry.projectileVisual(c.move.id());
         if (visuel == null) {
            visuel = DevilFruitElement.projectileVisualFallback(DevilFruitElement.of(c.fruit));
         }
         rayon.visualProfile(c.move.id()).batchOrigin(porteur).terrain(c.terrain);
         rayon.setOwner(porteur);
         rayon.damage(c.move.damage()).knockback(c.move.knockback()).lifetime(c.move.lifetime());
         rayon.element(DevilFruitElement.forTechnique(c.fruit, c.move.id()));
         rayon.scale("ball_maximum".equals(visuel[0]) ? 1.0F : 2.5F);
         rayon.model(visuel[0]);
         rayon.texture(visuel[1]);
         rayon.trail(ParticleTypes.END_ROD);
         rayon.range(portee);
         rayon.shoot(porteur.getLookAngle().yRot((float) angle).x,
            porteur.getLookAngle().yRot((float) angle).y,
            porteur.getLookAngle().yRot((float) angle).z, (float) c.move.speed(), 0.0F);
         level.addFreshEntity(rayon);
         tires++;
      }
      level.playSound(null, porteur.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS,
         0.9F, 1.4F);
      return tires;
   }

   /** Le signal : halo borné, visible de tous, pendant toute la charge. */
   private static void afficherSignal(ServerLevel level, ServerPlayer porteur) {
      double x = porteur.getX();
      double y = porteur.getY() + porteur.getBbHeight() + 0.6;
      double z = porteur.getZ();
      // Tiny vanilla sparks: the source sprites were several blocks wide in first person.
      level.sendParticles(ParticleTypes.END_ROD, x, y, z, 3, .25, .08, .25, .002);
      porteur.addEffect(new MobEffectInstance(MinepieceModMobEffects.LIGHT_CHARGE,
         DevilFruitPikaConfig.chargeTicks() + 5, 0, false, false));
   }

   /** Rapport de diagnostic : « ça n'a pas marché » doit être lisible sans reconstruire le mod. */
   public static String chargeReport() {
      return "arme\u00e9es=" + armees + " volee_tirees=" + voleesTirees + " impacts_tires=" + impactsTires
         + " avortees=" + avortees;
   }

   /** Remet les compteurs à zéro (tests répétables). */
   public static void resetReport() {
      armees = 0;
      voleesTirees = 0;
      impactsTires = 0;
      avortees = 0;
   }
}
