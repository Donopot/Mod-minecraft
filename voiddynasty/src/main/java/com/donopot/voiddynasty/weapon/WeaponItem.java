package com.donopot.voiddynasty.weapon;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Item d'arme générique : porte sa {@link WeaponDefinition} et gère les modes de tir.
 * Modes automatique/continu = maintien du clic (use -> CONSUME + releaseUsing).
 */
public class WeaponItem extends Item {

    private final WeaponDefinition definition;

    public WeaponItem(Properties properties, WeaponDefinition definition) {
        super(properties);
        this.definition = definition;
    }

    public WeaponDefinition definition() {
        return definition;
    }

    /**
     * Phase D : les marqueurs d'animation (recul, rechargement) vivent sur l'OBJET et non sur le
     * joueur. Ce hook est appelé par le jeu pour chaque copie de l'objet, où qu'elle soit — main,
     * sacoche, coffre — et en 26.3 il reçoit un {@link ServerLevel}, donc il ne s'exécute que côté
     * serveur : le serveur fait autorité et synchronise. Conséquence voulue : une arme rangée en plein
     * recul ne reste jamais figée dans sa pose.
     */
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity entity, EquipmentSlot slot) {
        WeaponService.tickMarqueurs(stack);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (definition.mode() == FireMode.AUTOMATIC || definition.mode() == FireMode.CONTINUOUS) {
            // 26.3 : le "using item" ne démarre plus tout seul via use(). On le lance des
            // DEUX côtés : côté serveur pour que releaseUsing() soit appelé au relâchement
            // (arrêt du tir auto), côté client pour que le client envoie bien le packet
            // RELEASE_USE_ITEM au relâchement.
            player.startUsingItem(hand);
            if (!level.isClientSide()) {
                WeaponService.startAutoFire((ServerPlayer) player);
            }
            return InteractionResult.CONSUME; // maintien du clic
        }

        if (level.isClientSide()) return InteractionResult.CONSUME;
        WeaponService.fire((ServerPlayer) player, stack);
        return InteractionResult.SUCCESS;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return (definition.mode() == FireMode.AUTOMATIC || definition.mode() == FireMode.CONTINUOUS)
                ? 72000 : 0;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof ServerPlayer sp) {
            WeaponService.stopAutoFire(sp);
        }
        return true;
    }
}
