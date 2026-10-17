package com.donopot.voiddynasty.armor;

import com.donopot.voiddynasty.registry.ModArmor;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ARGB;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Bonus de stats des armures, proportionnels aux pièces portées.
 * Space Marine (Mark X) : taille ~2.45 m.
 * Terminator : taille ~3.50 m + bouclier d'énergie visuel (particules orbitantes).
 */
public final class ArmorEffects {
    private static int tickCounter = 0;

    private static final Identifier SM_SCALE_ID =
            Identifier.fromNamespaceAndPath("voiddynasty", "space_marine_scale");
    private static final AttributeModifier SM_SCALE =
            new AttributeModifier(SM_SCALE_ID, 0.36, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

    private static final Identifier TERM_SCALE_ID =
            Identifier.fromNamespaceAndPath("voiddynasty", "terminator_scale");
    private static final AttributeModifier TERM_SCALE =
            new AttributeModifier(TERM_SCALE_ID, 0.9444, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);

    /** Particule d'énergie du bouclier (cyan). */
    private static final DustParticleOptions ENERGY_SHIELD =
            new DustParticleOptions(ARGB.color(255, 100, 220, 255), 1.6f);

    public static void tick(MinecraftServer server) {
        if (++tickCounter % 10 != 0) return;
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            int sm = countPieces(player, ModArmor.HELMET, ModArmor.CHESTPLATE, ModArmor.LEGGINGS, ModArmor.BOOTS);
            int term = countPieces(player, ModArmor.TERMINATOR_HELMET, ModArmor.TERMINATOR_CHESTPLATE,
                    ModArmor.TERMINATOR_LEGGINGS, ModArmor.TERMINATOR_BOOTS);

            // Retire les effets d'armure (re-appliqués ci-dessous si l'armure est encore portée).
            clearArmorEffects(player);

            if (sm > 0) applySpaceMarine(player, sm);
            if (term > 0) applyTerminator(player, term);

            // Taille selon l'armure complète (retirée sinon).
            applyScale(player, SM_SCALE_ID, SM_SCALE, sm == 4);
            applyScale(player, TERM_SCALE_ID, TERM_SCALE, term == 4);

            // Bouclier d'énergie (Terminator complète).
            if (term == 4) spawnEnergyShield(player);
        }
    }

    private static void applySpaceMarine(ServerPlayer player, int pieces) {
        int amp = pieces - 1; // 0..3
        apply(player, MobEffects.SPEED, amp);
        apply(player, MobEffects.STRENGTH, amp);
        apply(player, MobEffects.JUMP_BOOST, Math.min(1, amp));
        apply(player, MobEffects.RESISTANCE, Math.min(1, amp));
        apply(player, MobEffects.HEALTH_BOOST, Math.min(2, amp));
        apply(player, MobEffects.FIRE_RESISTANCE, 0);
        if (isArmor(player.getItemBySlot(EquipmentSlot.HEAD), ModArmor.HELMET)) {
            apply(player, MobEffects.NIGHT_VISION, 0);
            apply(player, MobEffects.WATER_BREATHING, 0);
        }
        if (pieces == 4) {
            apply(player, MobEffects.REGENERATION, 0);
            apply(player, MobEffects.SATURATION, 0);
        }
    }

    private static void applyTerminator(ServerPlayer player, int pieces) {
        // Élite : stats supérieures, bouclier énergétique (absorption).
        int amp = pieces == 4 ? 2 : Math.max(0, pieces - 1);
        apply(player, MobEffects.SPEED, amp);
        apply(player, MobEffects.STRENGTH, amp);
        apply(player, MobEffects.JUMP_BOOST, Math.min(1, amp));
        apply(player, MobEffects.RESISTANCE, amp);
        apply(player, MobEffects.HEALTH_BOOST, Math.min(2, amp));
        apply(player, MobEffects.FIRE_RESISTANCE, 0);
        if (isArmor(player.getItemBySlot(EquipmentSlot.HEAD), ModArmor.TERMINATOR_HELMET)) {
            apply(player, MobEffects.NIGHT_VISION, 0);
            apply(player, MobEffects.WATER_BREATHING, 0);
        }
        if (pieces == 4) {
            apply(player, MobEffects.REGENERATION, 0);
            apply(player, MobEffects.SATURATION, 0);
            apply(player, MobEffects.ABSORPTION, 1);   // bouclier : absorbe les dégâts
        }
    }

    /** Retire tous les effets d'armure (re-appliqués au tick suivant si l'armure est portée). */
    private static void clearArmorEffects(ServerPlayer player) {
        player.removeEffect(MobEffects.SPEED);
        player.removeEffect(MobEffects.STRENGTH);
        player.removeEffect(MobEffects.JUMP_BOOST);
        player.removeEffect(MobEffects.RESISTANCE);
        player.removeEffect(MobEffects.HEALTH_BOOST);
        player.removeEffect(MobEffects.FIRE_RESISTANCE);
        player.removeEffect(MobEffects.NIGHT_VISION);
        player.removeEffect(MobEffects.WATER_BREATHING);
        player.removeEffect(MobEffects.REGENERATION);
        player.removeEffect(MobEffects.SATURATION);
        player.removeEffect(MobEffects.ABSORPTION);
    }

    /** Bulle d'énergie : sphère de particules réparties uniformément (Fibonacci), en rotation lente. */
    private static void spawnEnergyShield(ServerPlayer player) {
        ServerLevel level = player.level();
        double cx = player.getX();
        double cy = player.getY() + 1.75;
        double cz = player.getZ();
        double radius = 1.9;
        double rotation = level.getGameTime() * 0.05;
        int count = 72;
        double goldenAngle = Math.PI * (3.0 - Math.sqrt(5.0));   // ~2.3999 rad (angle d'or)
        for (int i = 0; i < count; i++) {
            double yy = 1.0 - (i / (double) (count - 1)) * 2.0;   // -1..1 (hauteur normalisée)
            double r = Math.sqrt(1.0 - yy * yy);                  // rayon horizontal normalisé
            double theta = goldenAngle * i + rotation;
            double x = Math.cos(theta) * r;
            double z = Math.sin(theta) * r;
            level.sendParticles(ENERGY_SHIELD, cx + x * radius, cy + yy * radius, cz + z * radius, 1, 0, 0, 0, 0);
        }
    }

    private static void applyScale(ServerPlayer player, Identifier id, AttributeModifier mod, boolean active) {
        AttributeInstance attr = player.getAttribute(Attributes.SCALE);
        if (attr == null) return;
        if (active) {
            attr.addOrUpdateTransientModifier(mod);
        } else {
            attr.removeModifier(id);
        }
    }

    private static void apply(ServerPlayer player, Holder<MobEffect> effect, int amplifier) {
        player.addEffect(new MobEffectInstance(effect, 220, amplifier, true, false, true));
    }

    private static int countPieces(ServerPlayer player, Item helmet, Item chest, Item legs, Item boots) {
        int n = 0;
        if (isArmor(player.getItemBySlot(EquipmentSlot.HEAD), helmet)) n++;
        if (isArmor(player.getItemBySlot(EquipmentSlot.CHEST), chest)) n++;
        if (isArmor(player.getItemBySlot(EquipmentSlot.LEGS), legs)) n++;
        if (isArmor(player.getItemBySlot(EquipmentSlot.FEET), boots)) n++;
        return n;
    }

    private static boolean isArmor(ItemStack stack, Item expected) {
        return !stack.isEmpty() && stack.getItem() == expected;
    }

    private ArmorEffects() {}
}
