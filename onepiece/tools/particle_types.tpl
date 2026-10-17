package net.mcreator.minepiece.init;

import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;

/**
 * <b>Les §N§ particules du mod, reprises du JAR source</b> — définitions et textures copiées à
 * l'identique (tools/particle_port.py), aucune redessinée.
 *
 * <h2>Pourquoi elles existent toutes ici</h2>
 * <p>Le portage initial remplaçait les particules du mod par des particules vanilla, faute de les avoir
 * reprises : une perte de fidélité gratuite, les fichiers étant présents dans le jar source. Trois
 * seulement (dark, light_large, light_small) avaient été extraites au moment où Yami et Pika en avaient
 * besoin. L'ensemble est désormais là, sous les <b>NOMS D'ORIGINE</b>, pour que chaque technique puisse
 * employer la particule de la source au lieu d'un substitut.</p>
 *
 * <p>Les identifiants sont ceux des définitions du jar (minepiece:particle_dark), pas des noms
 * raccourcis : un nom inventé obligerait à tenir une table de correspondance, et c'est exactement le
 * genre d'écart qui fait diverger un portage.</p>
 *
 * <p>Enregistrement côté <b>serveur</b> (le type doit exister des deux côtés pour circuler dans un
 * paquet) ; l'apparence est enregistrée côté client dans client/MinepieceParticlesClient, avec les
 * paramètres LUS dans le bytecode des classes source.</p>
 */
public final class MinepieceModParticles {

   public static final String NAMESPACE = "minepiece";

§CHAMPS§

   private MinepieceModParticles() {
   }

   /** Force le chargement de la classe (donc l'enregistrement des types). */
   public static void register() {
      // Les champs s'enregistrent d'eux-mêmes à la charge de la classe.
   }

   /**
    * Le constructeur de SimpleParticleType est <b>protégé</b> : on l'atteint par une sous-classe
    * anonyme, idiome habituel, plutôt que de réimplémenter codec et streamCodec.
    */
   private static SimpleParticleType register(String chemin) {
      return Registry.register(BuiltInRegistries.PARTICLE_TYPE,
         Identifier.fromNamespaceAndPath(NAMESPACE, chemin), new SimpleParticleType(false) {
         });
   }

   /** Type brut par identifiant de définition, ou null si l'identifiant est inconnu. */
   public static ParticleType<?> byName(String nom) {
      return switch (nom) {
§SWITCH§
         default -> null;
      };
   }
}
