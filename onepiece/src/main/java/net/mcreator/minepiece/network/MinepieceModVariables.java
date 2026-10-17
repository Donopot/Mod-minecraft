package net.mcreator.minepiece.network;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Shim Fabric 26.3 de l'addon Mine Piece (variables de joueur).
 * Remplace la capability Forge par une map UUID -> PlayerVariables.
 *
 * <p>CORE-003 : les variables sont persistées dans {@code <monde>/data/opworldlike/devilfruit-players.dat}
 * (NBT, une entrée par UUID) via {@link #loadAll()} / {@link #saveAll()}, branchés sur le cycle de vie
 * serveur dans {@link DevilFruitPersistence}. CORE-004 : {@code syncPlayerVariables} envoie l'état
 * complet (fruit, énergie, forme, sélection) au client via {@link DevilFruitNetworking#sendSyncState}.</p>
 */
public final class MinepieceModVariables {
   private static final Logger LOGGER = LoggerFactory.getLogger("MinePieceDevilFruit");
   private static final String FILE = "devilfruit-players.dat";
   private static final Map<UUID, PlayerVariables> VARIABLES = new ConcurrentHashMap<>();

   private MinepieceModVariables() {
   }

   public static PlayerVariables get(ServerPlayer player) {
      return VARIABLES.computeIfAbsent(player.getUUID(), u -> new PlayerVariables());
   }

   public static void clear(ServerPlayer player) {
      VARIABLES.remove(player.getUUID());
   }

   /**
    * Vide <b>tous</b> les porteurs connus — réservé au harnais de test.
    *
    * <p>Raison, et elle vient d'un échec réel : la table de loot <b>exclut les Fruits déjà
    * possédés</b> ({@link #ownedFruitItems()}). Les Fixtures du harnais donnent des Fruits à des faux
    * joueurs <b>persistants</b> : si le harnais ne les rend pas, le contrôle du loot en coffre
    * s'exécute sur un monde où presque tout est déjà possédé, retire chaque tirage, et la mesure
    * annonce « 0 fruit » — en accusant le loot alors que c'est le harnais qui a tout pris. Un harnais
    * qui emprunte doit rendre, et parfois en bloc.</p>
    */
   public static void clearAll() {
      VARIABLES.clear();
   }

   /** Items de Fruit du Démon actuellement possédés par un joueur (unicité du loot, CORE-023). */
   public static java.util.Set<Item> ownedFruitItems() {
      java.util.Set<Item> owned = new java.util.HashSet<>();
      for (PlayerVariables vars : VARIABLES.values()) {
         if (!vars.PlayerDevilFruit.isEmpty()) {
            owned.add(vars.PlayerDevilFruit.getItem());
         }
      }
      return owned;
   }

   /** Charge toutes les variables persistées depuis le disque (CORE-003) — appelé au démarrage serveur. */
   public static void loadAll() {
      VARIABLES.clear();
      try {
         CompoundTag root = FruitWorldStorage.read(FILE);
         ListTag list = root.getListOrEmpty("players");
         for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompoundOrEmpty(i);
            if (entry.contains("uuid")) {
               try {
                  UUID uuid = UUID.fromString(entry.getStringOr("uuid", ""));
                  PlayerVariables vars = new PlayerVariables();
                  vars.loadNbt(entry);
                  VARIABLES.put(uuid, vars);
               } catch (IllegalArgumentException ex) {
                  LOGGER.warn("Devil fruit player entry skipped (uuid invalide).", ex);
               }
            }
         }
         LOGGER.info("[MinePiece shim] Devil fruit player data loaded: {} entrée(s).", VARIABLES.size());
      } catch (IOException ex) {
         LOGGER.warn("Could not load devil fruit player data from {}.", FruitWorldStorage.path(FILE), ex);
      }
   }

   /** Sauvegarde toutes les variables sur disque (CORE-003) — appelé à l'arrêt/déconnexion. */
   public static void saveAll() {
      CompoundTag root = new CompoundTag();
      ListTag list = new ListTag();
      VARIABLES.forEach((uuid, vars) -> {
         CompoundTag entry = vars.writeNbt();
         entry.putString("uuid", uuid.toString());
         list.add(entry);
      });
      root.put("players", list);
      try {
         FruitWorldStorage.write(FILE, root);
      } catch (IOException ex) {
         LOGGER.warn("Could not save devil fruit player data to {}.", FruitWorldStorage.path(FILE), ex);
      }
   }

   public static class PlayerVariables {
      public String pirates_crew_name = "";
      public boolean team_captain = false;
      public String team_captain_name = "";
      public double PlayerBounty = 0.0;
      public double PlayerLevel = 0.0;
      public double ExpArmament = 0.0;
      public double ExpObservation = 0.0;
      // Haki (CORE-029) — niveaux persistés (0-10). Source de vérité de la couche Haki.
      public int hakiArmamentLevel = 0;
      public int hakiObservationLevel = 0;
      // Fruit du Démon
      public net.minecraft.world.item.ItemStack PlayerDevilFruit = net.minecraft.world.item.ItemStack.EMPTY;
      public boolean UseDevilAbilities = false;
      // Énergie ("Curse Power") + techniques
      public double PlayerEnergy = 200.0;
      public double PlayerEnergyMAX = 200.0;
      public double PlayerEnergyFormer = 200.0;
      public double PlayerSkill1 = 0.0;

      /**
       * <b>Rokushiki</b> — l'art martial, indépendant de tout Fruit du Démon.
       *
       * <p>La source le prouve : {@code SkillRokushikiProcedure} dispatche Shigan, Rankyaku, Tekkai,
       * Iron Body, Rokuogan et Attack Speed selon une <b>variable de compétence</b>, et
       * {@code AIRokushikiProcedure} le fait employer par les PNJ (tags {@code can_use_shave},
       * {@code can_use_moonwalk}, {@code can_use_iron_body}). L'apprentissage passe par
       * l'advancement {@code skill_rokushiki_advancement} et par l'objet {@code skill_selector} :
       * on l'acquiert donc <b>hors des Fruits</b>, ce que Donopot a rappelé.</p>
       */
      public double rokushikiLearned = 0.0;

      /** Technique Rokushiki sélectionnée (index dans {@code DevilFruitRegistry.rokushiki()}). */
      public double rokushikiSelected = 0.0;
      public double player_form = 0.0;
      public boolean gomuFlightGranted;
      public boolean zoanFlightGranted;
      /** Remaining maintained-form ticks; -1 denotes a legacy save without a timer. */
      public int formRemainingTicks = -1;
      public boolean meraFlightGranted;
      public boolean amaruFlightGranted;
      public double PlayerTechniqueUsedNumber = 0.0;
      public String p_skillName = "";
      public boolean PassiveTechnique = false;
      public boolean p_passive = false;
      public boolean noChangeTechnique = false;
      public boolean PhysicalAttack = false;

      /** Sérialise l'état persistant (fruit = id d'item, pas le stack complet). */
      public CompoundTag writeNbt() {
         CompoundTag tag = new CompoundTag();
         if (!PlayerDevilFruit.isEmpty()) {
            Identifier id = BuiltInRegistries.ITEM.getKey(PlayerDevilFruit.getItem());
            if (id != null) {
               tag.putString("fruit", id.toString());
            }
         }
         tag.putBoolean("useAbilities", UseDevilAbilities);
         tag.putDouble("energy", PlayerEnergy);
         tag.putDouble("energyMax", PlayerEnergyMAX);
         tag.putDouble("energyFormer", PlayerEnergyFormer);
         tag.putDouble("form", player_form);
         tag.putBoolean("gomuFlightGranted",gomuFlightGranted);
         tag.putBoolean("zoanFlightGranted",zoanFlightGranted);
         tag.putInt("formRemainingTicks", formRemainingTicks);
         tag.putBoolean("meraFlightGranted",meraFlightGranted);
         tag.putBoolean("amaruFlightGranted",amaruFlightGranted);
         tag.putDouble("technique", PlayerTechniqueUsedNumber);
         tag.putString("crewName", pirates_crew_name);
         tag.putBoolean("captain", team_captain);
         tag.putString("captainName", team_captain_name);
         tag.putDouble("bounty", PlayerBounty);
         tag.putDouble("level", PlayerLevel);
         tag.putDouble("expArmament", ExpArmament);
         tag.putDouble("expObservation", ExpObservation);
         tag.putDouble("rokushikiLearned", rokushikiLearned);
         tag.putDouble("rokushikiSelected", rokushikiSelected);
         tag.putInt("hakiArmament", hakiArmamentLevel);
         tag.putInt("hakiObservation", hakiObservationLevel);
         return tag;
      }

      /** Restaure l'état depuis le NBT ; les champs absents conservent leurs valeurs par défaut. */
      public void loadNbt(CompoundTag tag) {
         String fruitId = tag.getStringOr("fruit", "");
         if (!fruitId.isEmpty()) {
            Identifier id = Identifier.tryParse(fruitId);
            if (id != null && BuiltInRegistries.ITEM.containsKey(id)) {
               Item item = BuiltInRegistries.ITEM.getValue(id);
               if (item != null) {
                  PlayerDevilFruit = new ItemStack(item);
               }
            }
         }
         UseDevilAbilities = tag.getBooleanOr("useAbilities", false);
         PlayerEnergy = tag.getDoubleOr("energy", 200.0);
         PlayerEnergyMAX = tag.getDoubleOr("energyMax", 200.0);
         PlayerEnergyFormer = tag.getDoubleOr("energyFormer", 200.0);
         player_form = tag.getDoubleOr("form", 0.0);
         gomuFlightGranted=tag.getBooleanOr("gomuFlightGranted",false);
         zoanFlightGranted=tag.getBooleanOr("zoanFlightGranted",false);
         formRemainingTicks = tag.getIntOr("formRemainingTicks", -1);
         meraFlightGranted=tag.getBooleanOr("meraFlightGranted",false);
         amaruFlightGranted=tag.getBooleanOr("amaruFlightGranted",false);
         PlayerTechniqueUsedNumber = tag.getDoubleOr("technique", 0.0);
         pirates_crew_name = tag.getStringOr("crewName", "");
         team_captain = tag.getBooleanOr("captain", false);
         team_captain_name = tag.getStringOr("captainName", "");
         PlayerBounty = tag.getDoubleOr("bounty", 0.0);
         PlayerLevel = tag.getDoubleOr("level", 0.0);
         ExpArmament = tag.getDoubleOr("expArmament", 0.0);
         ExpObservation = tag.getDoubleOr("expObservation", 0.0);
         rokushikiLearned = tag.getDoubleOr("rokushikiLearned", 0.0);
         rokushikiSelected = tag.getDoubleOr("rokushikiSelected", 0.0);
         hakiArmamentLevel = tag.getIntOr("hakiArmament", 0);
         hakiObservationLevel = tag.getIntOr("hakiObservation", 0);
      }

      /** Synchronise l'état complet au client (CORE-004). */
      public void syncPlayerVariables(ServerPlayer player) {
         DevilFruitNetworking.sendSyncState(player);
      }
   }
}
