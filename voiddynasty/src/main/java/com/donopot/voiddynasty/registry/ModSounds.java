package com.donopot.voiddynasty.registry;

import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/**
 * Registre des sons du mod : un son de tir distinct par arme + rechargement + mêlée.
 */
public final class ModSounds {

    public static final SoundEvent BOLTGUN_FIRE = fire("item.bolter.fire");
    public static final SoundEvent BOLT_PISTOL_FIRE = fire("item.bolt_pistol.fire");
    public static final SoundEvent HEAVY_BOLTER_FIRE = fire("item.heavy_bolter.fire");
    public static final SoundEvent LAS_GUN_FIRE = fire("item.las_gun.fire");
    public static final SoundEvent PLASMA_PISTOL_FIRE = fire("item.plasma_pistol.fire");
    public static final SoundEvent PLASMA_INCINERATOR_FIRE = fire("item.plasma_incinerator.fire");
    public static final SoundEvent SHOTGUN_FIRE = fire("item.shotgun.fire");
    public static final SoundEvent FLAMETHROWER_FIRE = fire("item.flamethrower.fire");
    public static final SoundEvent MELTA_FIRE = fire("item.melta.fire");
    public static final SoundEvent MISSILE_FIRE = fire("item.missile.fire");
    public static final SoundEvent MELEE_SWING = fire("item.melee.swing");
    public static final SoundEvent DRY_FIRE = fire("item.dry_fire");
    public static final SoundEvent RELOAD = fire("item.reload");

    private ModSounds() {
    }

    private static SoundEvent fire(String path) {
        Identifier id = Identifier.fromNamespaceAndPath("voiddynasty", path);
        return Registry.register(
                BuiltInRegistries.SOUND_EVENT,
                id,
                SoundEvent.createVariableRangeEvent(id)
        );
    }

    public static void init() {
    }
}
