package br.com.oneminepiece.opworldlike.balance;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Configuration d'équilibrage des techniques de Fruit du Démon (CORE-025).
 *
 * <p>Définit des <b>budgets de dégâts</b> (exprimés en points de vie ; un joueur en a 20) et la façon
 * dont ils sont appliqués. Deux niveaux, du plus conservateur au plus strict :</p>
 * <ul>
 *   <li>{@code absolute-max-damage} — plafond dur, appliqué dès que {@code enabled = true}. Il ne
 *       touche que les aberrations (ver15 déclarait 200, 800 et un 999 999 « one-shot »).</li>
 *   <li>{@code enforce-tier-budgets} — applique en plus un budget par palier déduit du coût en Curse
 *       Power (base / signature / ultime). Désactivé par défaut : il normalise aussi des techniques
 *       légitimes au-dessus du palier, donc c'est un choix de conception explicite.</li>
 * </ul>
 *
 * <p>Les valeurs ver15 sont conservées partout où elles restent dans le budget : l'équilibrage ne
 * réécrit pas le catalogue, il ne corrige que les écarts. Chaque correction est journalisée
 * ({@link DevilFruitBalance#lastReport()}) et affichée dans l'autotest serveur.</p>
 *
 * <p>Fichier : {@code config/opworldlike/devilfruit-balance.toml}. Clé {@code override-<id>} pour
 * forcer la valeur d'une technique précise (priorité maximale).</p>
 */
public final class DevilFruitBalanceConfig {

   private static final Path CONFIG_PATH = Path.of("config", "opworldlike", "devilfruit-balance.toml");

   private static boolean enabled = true;
   private static double absoluteMaxDamage = 60.0;
   private static boolean enforceTierBudgets = false;
   private static double tierBasicMax = 25.0;
   private static double tierSignatureMax = 40.0;
   private static double tierUltimateMax = 60.0;
   private static double signatureEnergyThreshold = 60.0;
   private static double ultimateEnergyThreshold = 110.0;
   private static final Map<String, Double> OVERRIDES = new LinkedHashMap<>();

   private DevilFruitBalanceConfig() {
   }

   public static void loadOrCreate() {
      OVERRIDES.clear();
      try {
         Files.createDirectories(CONFIG_PATH.getParent());
         if (Files.notExists(CONFIG_PATH)) {
            Files.writeString(CONFIG_PATH, defaultConfig(), StandardCharsets.UTF_8);
         }
         parse(Files.readAllLines(CONFIG_PATH, StandardCharsets.UTF_8));
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load devil fruit balance config {}; using defaults.", CONFIG_PATH, ex);
      }
   }

   public static boolean enabled() {
      return enabled;
   }

   public static double absoluteMaxDamage() {
      return absoluteMaxDamage;
   }

   public static boolean enforceTierBudgets() {
      return enforceTierBudgets;
   }

   public static double signatureEnergyThreshold() {
      return signatureEnergyThreshold;
   }

   public static double ultimateEnergyThreshold() {
      return ultimateEnergyThreshold;
   }

   /** Budget de dégâts du palier auquel appartient une technique, d'après son coût en Curse Power. */
   public static double tierBudget(double energyCost) {
      if (energyCost >= ultimateEnergyThreshold) {
         return tierUltimateMax;
      }
      if (energyCost >= signatureEnergyThreshold) {
         return tierSignatureMax;
      }
      return tierBasicMax;
   }

   public static double tierBasicMax() {
      return tierBasicMax;
   }

   public static double tierSignatureMax() {
      return tierSignatureMax;
   }

   public static double tierUltimateMax() {
      return tierUltimateMax;
   }

   /** Valeur forcée pour une technique, ou {@code null}. */
   public static Double override(String moveId) {
      return OVERRIDES.get(moveId);
   }

   public static int overrideCount() {
      return OVERRIDES.size();
   }

   private static void parse(List<String> lines) {
      for (String raw : lines) {
         String line = raw.split("#", 2)[0].trim();
         if (line.isBlank() || !line.contains("=")) {
            continue;
         }
         String[] parts = line.split("=", 2);
         String key = parts[0].trim();
         String value = parts[1].trim().replace("\"", "");
         try {
            if (key.startsWith("override-")) {
               OVERRIDES.put(key.substring("override-".length()), Double.parseDouble(value));
               continue;
            }
            switch (key) {
               case "enabled" -> enabled = Boolean.parseBoolean(value);
               case "absolute-max-damage" -> absoluteMaxDamage = clampDouble(value, 1.0, 100000.0);
               case "enforce-tier-budgets" -> enforceTierBudgets = Boolean.parseBoolean(value);
               case "tier-basic-max" -> tierBasicMax = clampDouble(value, 1.0, 100000.0);
               case "tier-signature-max" -> tierSignatureMax = clampDouble(value, 1.0, 100000.0);
               case "tier-ultimate-max" -> tierUltimateMax = clampDouble(value, 1.0, 100000.0);
               case "signature-energy-threshold" -> signatureEnergyThreshold = clampDouble(value, 0.0, 10000.0);
               case "ultimate-energy-threshold" -> ultimateEnergyThreshold = clampDouble(value, 0.0, 10000.0);
               default -> {
                  // clé inconnue ignorée
               }
            }
         } catch (NumberFormatException ex) {
            OmpWorld.LOGGER.warn("Balance config: valeur invalide pour '{}' ({}).", key, value);
         }
      }
   }

   private static double clampDouble(String raw, double min, double max) {
      return Math.max(min, Math.min(max, Double.parseDouble(raw)));
   }

   private static String defaultConfig() {
      return "# Equilibrage des techniques de Fruit du Demon (CORE-025).\n"
         + "# Les degats sont en points de vie ; un joueur de base en a 20.\n"
         + "# Les valeurs ver15 sont conservees partout ou elles restent dans le budget :\n"
         + "# seuls les ecarts sont corriges, et chaque correction est journalisee.\n"
         + "\n"
         + "enabled = true\n"
         + "\n"
         + "# Plafond dur (applique des que enabled = true).\n"
         + "# 60 = 3x les PV de base : au-dela, une technique n'est plus contrable ni en PvE ni en PvP.\n"
         + "# Corrige les aberrations ver15 : 999999 (Kamie Zanshin), 800 (Raigo), 200 x4.\n"
         + "absolute-max-damage = 60.0\n"
         + "\n"
         + "# Budgets par palier, deduits du cout en Curse Power. Desactive par defaut :\n"
         + "# l'activer normalise aussi des techniques legitimes au-dessus de leur palier.\n"
         + "enforce-tier-budgets = false\n"
         + "signature-energy-threshold = 60.0\n"
         + "ultimate-energy-threshold = 110.0\n"
         + "tier-basic-max = 25.0\n"
         + "tier-signature-max = 40.0\n"
         + "tier-ultimate-max = 60.0\n"
         + "\n"
         + "# Forcer la valeur d'une technique precise (priorite maximale).\n"
         + "# Exemple : override-kamiezanshin = 45.0\n";
   }
}
