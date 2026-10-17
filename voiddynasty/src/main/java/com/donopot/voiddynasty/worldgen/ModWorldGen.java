package com.donopot.voiddynasty.worldgen;

import net.fabricmc.fabric.api.biome.v1.BiomeModifications;
import net.fabricmc.fabric.api.biome.v1.BiomeSelectors;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

/**
 * La génération du minerai de céramite — phase H.
 *
 * ⚠️ C'est de la CONCEPTION, pas du portage : la source est une carte-campagne qui DONNE ses objets et
 * n'a aucune donnée de génération à recopier. Les valeurs ci-dessous sont donc un point de départ assumé
 * (4 filons de 8 blocs par chunk, de la couche -64 à 48, dans tous les biomes de surface), à régler par
 * Donopot — c'est lui qui juge l'équilibrage.
 *
 * Sans cette classe, le minerai n'existe nulle part dans le monde : le titane ne vient que de lui, donc
 * la progression d'une partie en survie libre était bloquée avant la première arme.
 */
public final class ModWorldGen {

    public static final ResourceKey<PlacedFeature> CERAMITE_ORE = ResourceKey.create(
            Registries.PLACED_FEATURE, Identifier.fromNamespaceAndPath("voiddynasty", "ceramite_ore"));

    private ModWorldGen() {}

    /** Appelé à l'initialisation du mod : attache le filon à tous les biomes de surface. */
    public static void enregistrer() {
        BiomeModifications.addFeature(BiomeSelectors.foundInOverworld(),
                GenerationStep.Decoration.UNDERGROUND_ORES, CERAMITE_ORE);
    }
}
