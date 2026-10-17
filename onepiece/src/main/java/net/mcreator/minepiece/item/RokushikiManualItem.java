package net.mcreator.minepiece.item;

import net.mcreator.minepiece.procedures.Rokushiki;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;

/**
 * <b>Manuel de Rokushiki</b> — l'objet par lequel l'art martial s'apprend, <b>hors des Fruits du
 * Démon</b>.
 *
 * <p>C'est l'objet de la source : {@code minepiece:skill_selector}, servi par la table de butin
 * {@code loot_book.json} et illustré par {@code icon_book.png}. Sa première utilisation
 * <b>apprend</b> le Rokushiki ; les suivantes font défiler les techniques, ce qui évite d'ouvrir un
 * écran pour six arts qu'on enchaîne en combat.</p>
 *
 * <p>Un porteur qui n'a rien appris et qui n'a pas de Fruit n'a donc plus une touche muette : il a un
 * objet à trouver, puis un art à pratiquer — exactement le rôle que lui donne la source.</p>
 */
public class RokushikiManualItem extends Item {

   public RokushikiManualItem(Item.Properties properties) {
      super(properties);
   }

   @Override
   public InteractionResult use(Level level, Player player, InteractionHand hand) {
      if (!(player instanceof ServerPlayer serveur)) {
         return InteractionResult.PASS;
      }
      if (!Rokushiki.isLearned(serveur)) {
         if (Rokushiki.learn(serveur)) {
            // Premier usage : l'art s'apprend. Le livre se tourne — c'est un manuel, pas une arme.
            level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS,
               1.0F, 1.0F);
            return InteractionResult.CONSUME;
         }
         return InteractionResult.PASS;
      }
      // Ensuite : choisir la technique, sans ouvrir d'écran — six arts s'enchaînent en combat.
      Rokushiki.cycle(serveur);
      level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS,
         0.9F, 1.2F);
      return InteractionResult.SUCCESS;
   }
}
