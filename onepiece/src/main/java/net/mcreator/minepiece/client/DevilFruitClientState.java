package net.mcreator.minepiece.client;

/**
 * État client reflété par le serveur (CORE-004) : fruit, énergie, forme et sélection.
 * Alimenté par le paquet {@code sync_state} ; sert de base au futur HUD (énergie/technique).
 */
public final class DevilFruitClientState {
   public static String fruitId = "";
   public static boolean flameThrust;
   public static double energy = 0.0;
   public static double energyMax = 0.0;
   public static double form = 0.0;
   public static int technique = 0;
   /** CORE-028 : id de la technique courante + nombre de techniques du Fruit (affichage HUD localisé). */
   public static String techniqueId = "";
   public static int techniqueCount = 0;
   /** FRUIT-KIRO_KIRO-03 : poids CONFIRME par le serveur (jamais la demande du client). */
   public static double weight = 1.0;

   public static void reset() {
      flameThrust=false;
      fruitId = ""; energy = 0; energyMax = 0; form = 0; technique = 0;
      techniqueId = ""; techniqueCount = 0; weight = 1;
   }

   private DevilFruitClientState() {
   }
}
