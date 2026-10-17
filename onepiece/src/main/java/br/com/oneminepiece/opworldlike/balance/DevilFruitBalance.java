package br.com.oneminepiece.opworldlike.balance;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Application des budgets d'équilibrage (CORE-025).
 *
 * <p>Fonction pure de calcul ({@link #budgetedDamage}) + journal des corrections réellement
 * appliquées, relu par l'autotest serveur et par la commande de test {@code /dftest balance}.
 * Aucune valeur n'est modifiée sans être tracée : c'est ce qui permet de prouver que l'équilibrage
 * ne réécrit <b>que</b> les écarts et laisse les valeurs ver15 intactes partout ailleurs.</p>
 */
public final class DevilFruitBalance {

   /** Une correction de dégâts : ancienne valeur, nouvelle valeur et motif. */
   public record Adjustment(String moveId, String type, double before, double after,
                            double energyCost, int cooldownTicks, String reason) {
   }

   private static final Map<String, Adjustment> LAST = new LinkedHashMap<>();
   private static int scanned;

   private DevilFruitBalance() {
   }

   /**
    * Dégâts retenus pour une technique : budget de palier, plafond dur et surcharge explicite.
    *
    * @param key         clé unique du porteur (fruit/technique) — un même id peut être partagé par
    *                    plusieurs fruits (cf CORE-017), donc l'id seul ne suffit pas à compter
    * @param moveId      identifiant de la technique (clé de {@code override-<id>})
    * @param energyCost  coût en Curse Power (détermine le palier)
    * @param damage      dégâts déclarés (ver15)
    * @return les dégâts effectivement appliqués
    */
   public static double budgetedDamage(String key, String moveId, double energyCost, double damage) {
      scanned++;
      if (damage <= 0.0) {
         return damage;
      }
      Double forced = DevilFruitBalanceConfig.override(moveId);
      if (forced != null) {
         record(key, moveId, energyCost, damage, forced, "surcharge explicite (override-" + moveId + ")");
         return forced;
      }
      if (!DevilFruitBalanceConfig.enabled()) {
         return damage;
      }
      double after = damage;
      String reason = null;
      if (damage > DevilFruitBalanceConfig.absoluteMaxDamage()) {
         after = DevilFruitBalanceConfig.absoluteMaxDamage();
         reason = "plafond dur (" + trim(after) + " = 3x PV de base)";
      }
      if (DevilFruitBalanceConfig.enforceTierBudgets()) {
         double tier = DevilFruitBalanceConfig.tierBudget(energyCost);
         if (after > tier) {
            after = tier;
            reason = "budget de palier (cout " + trim(energyCost) + " Curse Power)";
         }
      }
      if (reason == null) {
         return damage;
      }
      record(key, moveId, energyCost, damage, after, reason);
      return after;
   }

   private static void record(String key, String moveId, double energyCost, double before, double after, String reason) {
      LAST.put(key, new Adjustment(moveId, "", before, after, energyCost, 0, reason));
   }

   /** Enregistre le type et le cooldown d'une technique déjà corrigée (complété par le registre). */
   public static void enrich(String key, String type, int cooldownTicks) {
      Adjustment a = LAST.get(key);
      if (a != null) {
         LAST.put(key, new Adjustment(a.moveId(), type, a.before(), a.after(),
            a.energyCost(), cooldownTicks, a.reason()));
      }
   }

   public static void clear() {
      LAST.clear();
      scanned = 0;
   }

   public static Collection<Adjustment> lastReport() {
      return LAST.values();
   }

   public static int adjustmentCount() {
      return LAST.size();
   }

   public static int scannedCount() {
      return scanned;
   }

   /** Résumé d'une ligne pour l'autotest serveur. */
   public static String summary() {
      if (!DevilFruitBalanceConfig.enabled()) {
         return "équilibrage désactivé (valeurs ver15 brutes)";
      }
      if (LAST.isEmpty()) {
         return "équilibrage actif : 0 correction (toutes les techniques dans le budget)";
      }
      StringBuilder sb = new StringBuilder("équilibrage actif : ").append(LAST.size())
         .append(" correction(s) sur ").append(scanned).append(" techniques (plafond ")
         .append(trim(DevilFruitBalanceConfig.absoluteMaxDamage())).append(") →");
      for (Adjustment a : LAST.values()) {
         sb.append(" ").append(a.moveId()).append(' ').append(trim(a.before()))
           .append("→").append(trim(a.after()));
      }
      return sb.toString();
   }

   private static String trim(double v) {
      return v == Math.rint(v) ? String.valueOf((long) v) : String.valueOf(v);
   }
}
