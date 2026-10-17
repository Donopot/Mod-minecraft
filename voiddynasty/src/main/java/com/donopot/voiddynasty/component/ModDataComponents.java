package com.donopot.voiddynasty.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;

/**
 * Data Components du mod. Le chargeur du bolter est persisté sur chaque ItemStack.
 */
public final class ModDataComponents {

    /** Chargeur du bolter (0..30), persisté + synchronisé client/serveur. */
    public static final DataComponentType<Integer> AMMO = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath("voiddynasty", "ammo"),
            DataComponentType.<Integer>builder()
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.INT)
                    .cacheEncoding()
                    .build()
    );

    /**
     * Recul en cours : nombre de ticks restants (phase D). Volontairement **non persisté** — c'est un
     * état d'animation transitoire, il ne doit pas survivre à une sauvegarde du monde. Sa seule
     * présence suffit au client, qui choisit alors la variante de modèle `_recoil`.
     */
    public static final DataComponentType<Integer> RECOIL = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath("voiddynasty", "recoil"),
            DataComponentType.<Integer>builder()
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );

    /** Rechargement en cours : ticks restants. Même logique que {@link #RECOIL} (non persisté). */
    public static final DataComponentType<Integer> RELOADING = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath("voiddynasty", "reloading"),
            DataComponentType.<Integer>builder()
                    .networkSynchronized(ByteBufCodecs.VAR_INT)
                    .build()
    );

    private ModDataComponents() {}

    public static void init() {
    }
}
