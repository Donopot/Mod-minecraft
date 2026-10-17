package net.mcreator.minepiece.procedures;

import java.util.ArrayList;
import java.util.List;
import net.mcreator.minepiece.init.MinepieceModItems;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/**
 * <b>Rokushiki — l'art martial, apprenable HORS des Fruits du Démon.</b>
 *
 * <h2>Ce que dit la source, et qui l'impose</h2>
 * <p>Le jar ver15 contient {@code SkillRokushikiProcedure} : un <b>dispatcher</b> qui lit une variable
 * de compétence et exécute {@code Shigan}, {@code Rankyaku}, {@code TekkaiRin}, {@code IronBody},
 * {@code AttackSpeed}, {@code Rokuogan} ou {@code SoruPassive} selon la valeur. Il contient aussi
 * {@code SelectRokushikiProcedure}, {@code AIRokushikiProcedure} (les PNJ l'emploient : tags
 * {@code can_use_shave}, {@code can_use_moonwalk}, {@code can_use_iron_body}), l'advancement
 * {@code skill_rokushiki_advancement} et l'objet {@code minepiece:skill_selector}.</p>
 *
 * <p>Autrement dit : <b>le Rokushiki n'appartient à aucun Fruit.</b> C'est un art martial qu'on
 * apprend, et que certains Fruits emploient en plus. L'implémenter comme un jeu de techniques
 * accrochées à Ushi Ushi ou Inu Inu Wolf — ce que faisait le port — revient à confondre l'outil et
 * son utilisateur.</p>
 *
 * <h2>Aucune valeur n'est recopiée</h2>
 * <p>Les techniques sont <b>déjà déclarées</b>, Fruit par Fruit, avec les valeurs de ver15. Les
 * redéclarer ici créerait deux sources de vérité qui divergeraient : on les <b>retrouve</b> par leur
 * identifiant ({@link DevilFruitRegistry#moveById}). Un ajustement de valeur dans un contrat de Fruit
 * se propage donc à l'art martial, et un identifiant introuvable est <b>signalé</b>, jamais inventé.</p>
 *
 * <h2>Coût et recharge</h2>
 * <p>Les mêmes règles que les Fruits : l'Énergie et la recharge sont validées <b>avant</b> tout coût,
 * un refus ne consomme rien, et la recharge est portée par l'objet d'apprentissage — c'est lui qu'on
 * tient quand on s'en sert, donc c'est lui qui refroidit.</p>
 */
public final class Rokushiki {

   /**
    * Le répertoire, dans l'ordre de la source. Les identifiants sont ceux des déclarations existantes.
    *
    * <p>{@code gagan} et {@code kibasen} (propres au Neko Neko Sabertiger) et {@code localareanetwork}
    * (propre à l'Inu Inu Wolf) n'y sont <b>pas</b> : ce sont des techniques de Fruit, pas de l'art
    * martial, et les y mettre aurait inventé un répertoire.</p>
    */
   private static final List<String> IDS = List.of(
      "shigan",         // doigt-pistolet
      "shiganmadara",   // la pluie de doigts-pistolets
      "jushigan",       // les dix doigts
      "tobushigan",     // le doigt volant
      "rankyaku",       // la jambe-tempête
      "rankyakuran",    // la jambe-tempête déchaînée
      "lupusfall",      // Rankyaku: Lupus Fall
      "amanedachi",     // Rankyaku: Amane Dachi
      "kamiezanshin",   // Kami-e : l'esquive du roseau
      "okamihajiki",    // la riposte du loup
      "yurushitene",    // la souplesse
      "rokuogan",       // le canon des six royaumes
      "soru",           // le pas de côté (Shave)
      "attackspeed"     // l'accélération, appelée par le dispatcher de la source
   );

   private static List<DevilFruitMove> cache;

   private Rokushiki() {
   }

   /**
    * Le répertoire effectivement disponible, dans l'ordre.
    *
    * <p>Un identifiant sans déclaration est <b>écarté</b> et signalé une fois : mieux vaut un
    * répertoire de quatorze techniques réelles qu'une quinzième qui ne fait rien.</p>
    */
   public static List<DevilFruitMove> moves() {
      if (cache == null) {
         List<DevilFruitMove> trouves = new ArrayList<>();
         StringBuilder absents = new StringBuilder();
         for (String id : IDS) {
            DevilFruitMove m = DevilFruitRegistry.moveById(id);
            if (m == null) {
               absents.append(id).append(' ');
            } else {
               trouves.add(m);
            }
         }
         if (absents.length() > 0) {
            br.com.oneminepiece.opworldlike.OmpWorld.LOGGER.warn(
               "Rokushiki : identifiants sans déclaration dans le registre : {}", absents.toString().trim());
         }
         cache = List.copyOf(trouves);
      }
      return cache;
   }

   /** Le porteur a-t-il appris l'art martial ? */
   public static boolean isLearned(ServerPlayer player) {
      return MinepieceModVariables.get(player).rokushikiLearned > 0.5;
   }

   /**
    * Apprend le Rokushiki — l'acquisition se fait HORS Fruit (objet d'apprentissage ou commande).
    *
    * @return vrai si l'apprentissage vient d'avoir lieu (faux s'il était déjà acquis)
    */
   public static boolean learn(ServerPlayer player) {
      MinepieceModVariables.PlayerVariables vars = MinepieceModVariables.get(player);
      if (vars.rokushikiLearned > 0.5) {
         return false;
      }
      vars.rokushikiLearned = 1.0;
      vars.syncPlayerVariables(player);
      player.sendSystemMessage(Component.literal(
         "§b§lRokushiki appris. §r§7Six arts, un corps : choisissez votre technique avec la touche de "
            + "cycle, puis déclenchez-la comme un pouvoir."), false);
      return true;
   }

   /** Oublie l'art martial (retrait, remise à zéro). */
   public static void forget(ServerPlayer player) {
      MinepieceModVariables.PlayerVariables vars = MinepieceModVariables.get(player);
      vars.rokushikiLearned = 0.0;
      vars.rokushikiSelected = 0.0;
      vars.syncPlayerVariables(player);
   }

   /** La technique actuellement sélectionnée, ou {@code null}. */
   public static DevilFruitMove selected(ServerPlayer player) {
      List<DevilFruitMove> liste = moves();
      if (liste.isEmpty()) {
         return null;
      }
      int index = Math.floorMod((int) MinepieceModVariables.get(player).rokushikiSelected, liste.size());
      return liste.get(index);
   }

   /** Passe à la technique suivante du répertoire. */
   public static void cycle(ServerPlayer player) {
      List<DevilFruitMove> liste = moves();
      if (liste.isEmpty()) {
         return;
      }
      MinepieceModVariables.PlayerVariables vars = MinepieceModVariables.get(player);
      vars.rokushikiSelected = (vars.rokushikiSelected + 1) % liste.size();
      DevilFruitMove suivant = selected(player);
      // `DevilFruitMove.name()` renvoie un Component (nom traduisible), pas une String : on l'ajoute
      // tel quel. Le passer à Component.literal ne compile pas — et traduire un nom déjà translatable
      // aurait de toute façon figé la langue.
      Component message = Component.literal("§bRokushiki §7» §f");
      player.sendSystemMessage(suivant == null ? message
         : message.copy().append(suivant.name()), false);
      vars.syncPlayerVariables(player);
   }

   /** L'objet qui porte la recharge : le manuel, celui qu'on tient pour s'en servir. */
   public static ItemStack cooldownCarrier() {
      return new ItemStack(MinepieceModItems.ITEM_SKILL_SELECTOR);
   }

   /**
    * Déclenche la technique sélectionnée — <b>sans Fruit du Démon</b>.
    *
    * <p>Mêmes règles que pour un Fruit : scellement, recharge puis Énergie, <b>validés avant tout
    * coût</b>, et un refus ne consomme rien.</p>
    *
    * @return vrai si la technique a été exécutée
    */
   public static boolean cast(ServerPlayer player) {
      MinepieceModVariables.PlayerVariables vars = MinepieceModVariables.get(player);
      if (vars.rokushikiLearned <= 0.5) {
         player.sendSystemMessage(Component.literal("§c").append(
            Component.translatable("minepiece.message.rokushiki_unknown")), false);
         return false;
      }
      DevilFruitMove move = selected(player);
      if (move == null) {
         return false;
      }
      if (DevilFruitPowerLogic.isSuppressed(player)) {
         player.sendSystemMessage(Component.literal("§c").append(
            Component.translatable("yami".equals(DevilFruitPowerLogic.suppressionCause(player))
               ? "minepiece.message.yami_sealed" : "minepiece.message.sea_suppressed")), false);
         return false;
      }
      ItemStack porteur = cooldownCarrier();
      if (player.getCooldowns().isOnCooldown(porteur)) {
         player.sendSystemMessage(Component.literal("§c").append(
            Component.translatable("minepiece.message.cooldown")), false);
         return false;
      }
      if (vars.PlayerEnergy < move.energyCost()) {
         player.sendSystemMessage(Component.literal("§c").append(
            Component.translatable("minepiece.message.no_energy", (int) move.energyCost(),
               (int) vars.PlayerEnergy)), false);
         return false;
      }
      vars.PlayerEnergy -= move.energyCost();
      boolean execute = DevilFruitSkillDispatcher.executeMove(player, porteur, move);
      if (execute) {
         player.getCooldowns().addCooldown(porteur, move.cooldownTicks());
      } else {
         vars.PlayerEnergy += move.energyCost(); // un refus ne consomme rien
      }
      vars.syncPlayerVariables(player);
      return execute;
   }
}
