package net.mcreator.minepiece.procedures;

/**
 * Type élémentaire d'une technique de Fruit du Démon (CORE-012). Détermine les effets
 * additionnels appliqués aux cibles (brûlure, gel, …) au-delà des dégâts bruts.
 */
public enum ElementType {
   /** Attaque physique (mêlée standard, Zoans, la plupart des Paramecia). */
   PHYSICAL,
   /** Feu (Mera Mera, Phoenix) — embrase la cible. */
   FIRE,
   /** Magma (Magu Magu) — embrase intensément. */
   MAGMA,
   /** Glace (Hie Hie, Okuchi no Makami) — ralentit/gèle la cible et éteint le feu. */
   ICE,
   /** Foudre (Goro Goro). */
   LIGHTNING,
   /** Light/laser energy, distinct from a physical punch and without automatic ignition. */
   LIGHT,
   /** Explosion (Bomu Bomu, Gura Gura) — recul accentué. */
   EXPLOSIVE,
   /**
    * Fantôme (Horo Horo) — les projectiles <b>traversent les surfaces</b> et l'impact plonge la cible
    * dans l'<b>état négatif</b>. L'élément porte les deux règles, donc toutes les techniques du fruit
    * se comportent identiquement.
    */
   GHOST,
   /**
    * Charme (Mero Mero) — ne brûle ni ne gèle : <b>pétrifie</b> les cibles admissibles. L'élément porte
    * la règle, donc le projectile, la mêlée et la zone se comportent identiquement.
    */
   CHARM,
   /** Magique/autre (effet non-élémentaire, comportement neutre). */
   DEFAULT
}
