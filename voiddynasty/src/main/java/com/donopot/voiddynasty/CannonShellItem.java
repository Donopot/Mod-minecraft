package com.donopot.voiddynasty;

import com.donopot.voiddynasty.entity.LemanRussEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

/**
 * Obus de canon — la munition du Leman Russ.
 *
 * <p>Utilisé assis dans le char, il tire. Ailleurs, c'est un simple bloc décoratif : on ne fait pas
 * partir un obus en le posant par terre.
 *
 * <p>La source fournit l'objet (`cannonshell`) et son modèle, mais aucun chiffre et aucun
 * comportement : le fait qu'un obus SE TIRE est une conception, et c'est écrit ici.
 */
public class CannonShellItem extends BlockItem {

    public CannonShellItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (player.getVehicle() instanceof LemanRussEntity char1 && level instanceof ServerLevel serveur) {
            boolean parti = char1.tirer(serveur, char1.boucheDuCanon(), player.getLookAngle(), player);
            if (parti) {
                ItemStack pile = player.getItemInHand(hand);
                if (!player.getAbilities().instabuild) {
                    pile.shrink(1);          // l'obus est consommé
                }
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.FAIL;   // le canon recharge
        }
        return super.use(level, player, hand);
    }
}
