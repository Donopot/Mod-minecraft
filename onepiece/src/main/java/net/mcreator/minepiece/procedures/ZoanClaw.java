package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.zoan.DevilFruitZoanConfig;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;
import java.util.UUID;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/**
 * <b>Griffes et combos rapides des Fruits Zoan</b> — FRUIT-{NEKO_NEKO_*,RYU_RYU_*,TORI_TORI,UO_UO,
 * USHI_USHI}-05.
 *
 * <p>Le plan, pour chacun de ces Fruits : « Créer <b>griffes et combos rapides</b> distincts des
 * simples frappes », critère « le <b>nombre d'impacts et le coût</b> sont documentés et <b>ne se
 * multiplient pas avec le spam</b> ».</p>
 *
 * <h2>Un combo ne multiplie pas les dégâts : il les RÉPARTIT</h2>
 * <p>C'est le point délicat du critère. Une lecture naïve — « N impacts, donc N fois les dégâts » —
 * ferait d'une technique à 15 dégâts une technique à 45 dès que le combo passe à trois coups, et
 * « ne se multiplient pas » serait violé non par le spam mais par le <b>code</b>. Ici, les dégâts
 * <b>déclarés par ver15</b> sont <b>répartis</b> entre les impacts : trois coups de cinq valent la même
 * chose qu'un coup de quinze. Le total reste la valeur du catalogue, et un test le compare au chiffre
 * du registre.</p>
 *
 * <h2>Le coût et la recharge ne se multiplient pas non plus</h2>
 * <p>Le lancement paie <b>une fois</b> l'énergie et la recharge de la technique (c'est le
 * {@code DevilFruitSkillDispatcher} qui les facture, et il refuse pendant la recharge) : spammer ne
 * produit donc ni plus d'impacts ni plus de dégâts. La borne du nombre d'impacts est <b>dans le
 * code</b>, pas dans un fichier.</p>
 *
 * <h2>Table des techniques concernées</h2>
 * <p>Les techniques dont le <b>nom de la source désigne une rafale</b> : {@code shiganmadara}
 * (« Shigan Madara », la pluie de doigts-pistolets), {@code gagan} (le coup de griffes), {@code
 * rankyakuran} (« Rankyaku Ran »), {@code raimeihakke} (« Raimei Hakke », les huit directions),
 * {@code kongokabura} et {@code galleonlariat}. Les autres gardent la frappe simple du moteur.</p>
 */
public final class ZoanClaw {

   private static final Set<String> TECHNIQUES_DE_COMBO =
      Set.of("shiganmadara", "gagan", "rankyakuran", "raimeihakke", "kongokabura", "galleonlariat");

   private static int combos;
   private static int impactsTotaux;
   private static int ciblesTotales;
   private static final List<Combo> ACTIVE = new ArrayList<>();
   // Vanilla accepts an equal-strength hit once its 10-tick hurt-cooldown window has passed.
   private static final int HIT_INTERVAL = 10;
   private static final class Combo {
      final ServerPlayer owner;
      final ServerLevel level;
      final net.minecraft.world.item.Item fruit;
      final DevilFruitMove move;
      final List<UUID> targets;
      final double damage;
      int remaining;
      long next;
      Combo(ServerPlayer owner, ItemStack fruit, DevilFruitMove move, List<LivingEntity> targets, int remaining, double damage) {
         this.owner = owner; this.level = (ServerLevel) owner.level(); this.fruit = fruit.getItem(); this.move = move;
         this.targets = targets.stream().map(LivingEntity::getUUID).toList();
         this.remaining = remaining; this.damage = damage; this.next = level.getGameTime() + HIT_INTERVAL;
      }
   }

   private ZoanClaw() {
   }

   public static void register() {
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         for (var iterator = ACTIVE.iterator(); iterator.hasNext();) {
            Combo combo = iterator.next();
            if (combo.level.getServer() != server) continue;
            if (!combo.owner.isAlive() || combo.owner.isRemoved() || combo.owner.level() != combo.level
                  || !net.mcreator.minepiece.network.MinepieceModVariables.get(combo.owner).PlayerDevilFruit.is(combo.fruit)
                  || DevilFruitPowerLogic.isSuppressed(combo.owner)) {
               iterator.remove(); continue;
            }
            if (combo.level.getGameTime() < combo.next) continue;
            var source = CombatSkillLogic.elementalDamageSource(combo.level, DevilFruitElement.of(new ItemStack(combo.fruit)), combo.owner, null);
            for (UUID id : combo.targets) {
               if (!(combo.level.getEntity(id) instanceof LivingEntity target)
                     || !CombatSkillLogic.isValidTarget(combo.owner, target)
                     || !combo.owner.hasLineOfSight(target)
                     || combo.owner.distanceTo(target) > CombatSkillLogic.effectiveRange(combo.owner, combo.move.range()) + 1) continue;
               if (CombatSkillLogic.hurtAndConfirm(target, combo.level, source, (float) combo.damage)) {
                  impactsTotaux++;
                  burst(combo.level, combo.owner, List.of(target));
               }
            }
            combo.next += HIT_INTERVAL;
            if (--combo.remaining <= 0) iterator.remove();
         }
      });
      ServerLifecycleEvents.SERVER_STOPPED.register(server -> ACTIVE.clear());
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> cancel(handler.player));
   }

   public static void cancel(ServerPlayer player) { ACTIVE.removeIf(combo -> combo.owner.getUUID().equals(player.getUUID())); }
   public static boolean isActive(ServerPlayer player) { return ACTIVE.stream().anyMatch(combo -> combo.owner == player); }

   /** Cette technique est-elle un combo ? */
   public static boolean isComboMove(String moveId) {
      return moveId != null && TECHNIQUES_DE_COMBO.contains(moveId);
   }

   /** Table lisible, pour les contrats et les preuves. */
   public static List<String> comboMoves() {
      return List.copyOf(TECHNIQUES_DE_COMBO);
   }

   /** Nombre d'impacts d'un combo (configuration, plafonnée dans le code). */
   public static int impacts() {
      return DevilFruitZoanConfig.comboImpacts();
   }

   /** Dégâts par impact : les dégâts ver15 répartis, jamais multipliés. */
   public static double damagePerImpact(DevilFruitMove move) {
      return move.damage() / impacts();
   }

   /** Dégâts TOTAUX d'un combo — la valeur du catalogue, quelle que soit la découpe. */
   public static double totalDamage(DevilFruitMove move) {
      return damagePerImpact(move) * impacts();
   }

   /**
    * Applique le combo : la <b>sélection</b> des cibles est celle du moteur
    * ({@link CombatSkillLogic#strikeTargets}), et la seule différence est la <b>répartition</b> des
    * dégâts en impacts bornés.
    *
    * @return nombre d'impacts réellement appliqués
    */
   public static int strikeCombo(ServerPlayer porteur, ItemStack fruit, DevilFruitMove move) {
      if (porteur == null || move == null) {
         return 0;
      }
      if (isActive(porteur)) return 0;
      int impacts = impacts();
      double parImpact = damagePerImpact(move);
      // Premier impact : strikeTargets applique « dégâts + bonus ». On lui donne un bonus NÉGATIF pour
      // qu'il frappe la part d'un impact, puis on applique les impacts restants sur les mêmes cibles.
      List<LivingEntity> touchees = CombatSkillLogic.strikeTargets(porteur, fruit, move,
         parImpact - move.damage());
      if (touchees.isEmpty()) {
         return 0;
      }
      int appliques = touchees.size(); // un impact par cible, déjà compté
      if (porteur.level() instanceof ServerLevel level) {
         if (impacts > 1) ACTIVE.add(new Combo(porteur, fruit, move, touchees, impacts - 1, parImpact));
         level.playSound(null, porteur.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS,
            0.9F, 1.6F);
         burst(level, porteur, touchees);
      }
      combos++;
      impactsTotaux += appliques;
      ciblesTotales += touchees.size();
      return appliques;
   }

   private static void burst(ServerLevel level, ServerPlayer porteur, List<LivingEntity> cibles) {
      for (LivingEntity cible : cibles) {
         level.sendParticles(MinepieceModParticles.SLASH, cible.getX(), cible.getY() + 1.0, cible.getZ(),
            3, 0.3, 0.3, 0.3, 0.0);
      }
   }

   /** Rapport de diagnostic. */
   public static String comboReport() {
      return "combos=" + combos + " impacts=" + impactsTotaux + " cibles=" + ciblesTotales;
   }

   /** Remet les compteurs à zéro (tests répétables). */
   public static void resetReport() {
      combos = 0;
      impactsTotaux = 0;
      ciblesTotales = 0;
   }

   /** Nombre d'impacts cumulés. */
   public static int totalImpacts() {
      return impactsTotaux;
   }
}
