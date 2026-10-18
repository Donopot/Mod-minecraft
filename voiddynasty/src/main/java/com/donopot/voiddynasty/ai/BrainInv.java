package com.donopot.voiddynasty.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Sac du Servitor.
 *
 * <p>Un mob n'a pas d'inventaire : le servitor ramasse donc les objets au sol lui-même et les
 * garde ici. C'est volatil (pas encore persisté) et volontairement plafonné : 27 piles, comme
 * un coffre. Au-delà, il ne ramasse plus et le dit.</p>
 */
public final class BrainInv {

    public static final int SLOTS = 27;

    private static final Map<UUID, List<ItemStack>> SACKS = new LinkedHashMap<>();

    private BrainInv() {}

    private static List<ItemStack> sack(UUID id) {
        return SACKS.computeIfAbsent(id, k -> new ArrayList<>());
    }

    public static List<ItemStack> of(UUID id) {
        return sack(id);
    }

    /** @return vrai si l'objet a été rangé (ou fusionné), faux si le sac est plein. */
    public static boolean add(net.minecraft.world.entity.LivingEntity bot, ItemStack stack) {
        if (bot == null || stack == null || stack.isEmpty()) return false;
        List<ItemStack> sack = sack(bot.getUUID());
        for (ItemStack slot : sack) {
            if (ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
                int free = slot.getMaxStackSize() - slot.getCount();
                int moved = Math.min(free, stack.getCount());
                slot.grow(moved);
                stack.shrink(moved);
                if (stack.isEmpty()) return true;
            }
        }
        if (sack.size() >= SLOTS) return false;
        sack.add(stack.copy());
        stack.setCount(0);
        return true;
    }

    public static int count(net.minecraft.world.entity.LivingEntity bot) {
        int n = 0;
        for (ItemStack s : of(bot.getUUID())) n += s.getCount();
        return n;
    }

    public static void dropAll(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity bot) {
        if (bot == null) return;
        for (ItemStack s : sack(bot.getUUID())) {
            if (!s.isEmpty()) bot.spawnAtLocation(level, s.copy());
        }
        SACKS.remove(bot.getUUID());
    }

    public static void clear(net.minecraft.world.entity.LivingEntity bot) {
        if (bot != null) SACKS.remove(bot.getUUID());
    }

    /** Résumé lisible : "iron_ore x7, cobblestone x32". */
    public static String summary(net.minecraft.world.entity.LivingEntity bot) {
        List<ItemStack> sack = of(bot.getUUID());
        if (sack.isEmpty()) return "vide";
        StringBuilder sb = new StringBuilder();
        int shown = 0;
        for (ItemStack s : sack) {
            if (s.isEmpty()) continue;
            if (shown++ > 0) sb.append(", ");
            sb.append(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(s.getItem()).getPath())
                    .append(" x").append(s.getCount());
            if (shown >= 8) break;
        }
        return sb.toString();
    }

    /** Utilisé par les tâches qui déposent dans un coffre (phase suivante). */
    public static List<ItemStack> takeAll(net.minecraft.world.entity.LivingEntity bot) {
        List<ItemStack> copy = new ArrayList<>(of(bot.getUUID()));
        SACKS.remove(bot.getUUID());
        return copy;
    }

    /** Position d'un joueur pour les tâches de suivi. */
    public static Player player(net.minecraft.server.level.ServerLevel level, String name) {
        return BrainTools.playerByName(level, name);
    }
}
