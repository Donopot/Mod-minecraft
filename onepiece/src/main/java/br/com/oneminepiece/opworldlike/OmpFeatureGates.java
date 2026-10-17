package br.com.oneminepiece.opworldlike;

public final class OmpFeatureGates {
   private OmpFeatureGates() {
   }

   public static boolean worldGameplay() {
      return true;
   }

   public static boolean serverCompetition() {
      return false;
   }

   public static boolean minePieceServerRules() {
      return false;
   }

   public static boolean mmnmServerRules() {
      return false;
   }
}
