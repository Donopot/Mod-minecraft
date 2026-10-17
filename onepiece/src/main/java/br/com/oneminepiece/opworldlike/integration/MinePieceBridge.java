package br.com.oneminepiece.opworldlike.integration;

import br.com.oneminepiece.opworldlike.OmpWorld;
import br.com.oneminepiece.opworldlike.claim.IslandDominionBonusData;
import br.com.oneminepiece.opworldlike.crew.CrewMemberSnapshot;
import br.com.oneminepiece.opworldlike.crew.CrewPersistenceStore;
import br.com.oneminepiece.opworldlike.crew.CrewSnapshot;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.mcreator.minepiece.init.MinepieceModAttributes;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.mcreator.minepiece.network.MinepieceModVariables.PlayerVariables;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;

public final class MinePieceBridge {
   public static final int BASE_DORIKI_LIMIT = 100000;
   private static final double POWER_PER_LEVEL = 1000.0;
   private static final double ADEPT_HAKI_LEVEL = 8.0;
   private static final double MAX_HAKI_LEVEL = 10.0;

   private MinePieceBridge() {
   }

   public static Optional<CrewInfo> crewOf(ServerPlayer player) {
      if (player == null) {
         return Optional.empty();
      }

      Optional<CrewSnapshot> persisted = CrewPersistenceStore.crewForMember(player.getUUID());
      return persisted.isPresent()
         ? Optional.of(crewInfo(persisted.get()))
         : variables(player).map(vars -> vars.pirates_crew_name).filter(name -> !name.isBlank()).map(name -> new CrewInfo(name, null, 1));
   }

   public static boolean crewApiAvailable() {
      return true;
   }

   public static Optional<CrewInfo> crewByName(String name) {
      return snapshotByName(name).map(MinePieceBridge::crewInfo);
   }

   public static List<CrewInfo> crews() {
      return CrewPersistenceStore.snapshots()
         .stream()
         .map(MinePieceBridge::crewInfo)
         .sorted(Comparator.comparing(CrewInfo::name, String.CASE_INSENSITIVE_ORDER))
         .toList();
   }

   public static List<CrewSnapshot> crewSnapshots() {
      return CrewPersistenceStore.snapshots();
   }

   public static int restoreMissingCrews(List<CrewSnapshot> snapshots) {
      if (snapshots != null && !snapshots.isEmpty()) {
         Map<String, CrewSnapshot> merged = snapshotsByName(CrewPersistenceStore.snapshots());
         int changed = 0;

         for (CrewSnapshot snapshot : snapshots) {
            if (snapshot.name() != null && !snapshot.name().isBlank() && !snapshot.members().isEmpty()) {
               String key = normalizeCrew(snapshot.name());
               if (!merged.containsKey(key)) {
                  merged.put(key, snapshot);
                  changed++;
               }
            }
         }

         if (changed > 0) {
            CrewPersistenceStore.saveSnapshots(new ArrayList<>(merged.values()), "Mine Piece crew restore");
         }

         return changed;
      } else {
         return 0;
      }
   }

   public static boolean createCrew(ServerPlayer captain, String name) {
      if (captain != null && name != null && !name.isBlank() && !crewOf(captain).isPresent() && !snapshotByName(name).isPresent()) {
         CrewSnapshot crew = new CrewSnapshot(name.trim(), System.currentTimeMillis() / 1000L, List.of(member(captain, true)));
         List<CrewSnapshot> crews = new ArrayList<>(CrewPersistenceStore.snapshots());
         crews.add(crew);
         CrewPersistenceStore.saveSnapshots(crews, "Mine Piece crew created");
         applyCrewVariables(captain, crew.name(), captain.getGameProfile().name(), true);
         return true;
      } else {
         return false;
      }
   }

   public static boolean addPlayerToCrew(ServerPlayer target, String crewName) {
      if (target != null && crewName != null && !crewOf(target).isPresent()) {
         List<CrewSnapshot> crews = new ArrayList<>(CrewPersistenceStore.snapshots());
         int index = indexByName(crews, crewName);
         if (index < 0) {
            return false;
         }

         CrewSnapshot crew = crews.get(index);
         List<CrewMemberSnapshot> members = new ArrayList<>(crew.members());
         members.add(member(target, false));
         CrewSnapshot updated = new CrewSnapshot(crew.name(), crew.creationDate(), members);
         crews.set(index, updated);
         CrewPersistenceStore.saveSnapshots(crews, "Mine Piece crew member added");
         applyCrewVariables(target, updated.name(), captainName(updated), false);
         return true;
      } else {
         return false;
      }
   }

   public static boolean removePlayerFromCrew(ServerPlayer target, boolean kicked) {
      if (target == null) {
         return false;
      }

      List<CrewSnapshot> crews = new ArrayList<>(CrewPersistenceStore.snapshots());

      for (int i = 0; i < crews.size(); i++) {
         CrewSnapshot crew = crews.get(i);
         if (!crew.members().stream().noneMatch(member -> target.getUUID().equals(member.uuid()))) {
            List<CrewMemberSnapshot> members = crew.members()
               .stream()
               .filter(member -> !target.getUUID().equals(member.uuid()))
               .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);
            clearCrewVariables(target);
            if (members.isEmpty()) {
               crews.remove(i);
            } else {
               if (members.stream().noneMatch(CrewMemberSnapshot::captain)) {
                  CrewMemberSnapshot first = members.get(0);
                  members.set(0, new CrewMemberSnapshot(first.uuid(), first.username(), true));
               }

               crews.set(i, new CrewSnapshot(crew.name(), crew.creationDate(), members));
            }

            CrewPersistenceStore.saveSnapshots(crews, kicked ? "Mine Piece crew member kicked" : "Mine Piece crew member left");
            syncOnlineCrewMembers(crews);
            return true;
         }
      }

      return false;
   }

   public static boolean renameCrew(String oldName, String newName) {
      if (oldName != null && newName != null && !newName.isBlank() && !snapshotByName(newName).isPresent()) {
         List<CrewSnapshot> crews = new ArrayList<>(CrewPersistenceStore.snapshots());
         int index = indexByName(crews, oldName);
         if (index < 0) {
            return false;
         }

         CrewSnapshot crew = crews.get(index);
         crews.set(index, new CrewSnapshot(newName.trim(), crew.creationDate(), crew.members()));
         CrewPersistenceStore.saveSnapshots(crews, "Mine Piece crew renamed");
         syncOnlineCrewMembers(crews);
         return true;
      } else {
         return false;
      }
   }

   public static boolean disbandCrew(String crewName) {
      List<CrewSnapshot> crews = new ArrayList<>(CrewPersistenceStore.snapshots());
      int index = indexByName(crews, crewName);
      if (index < 0) {
         return false;
      }

      CrewSnapshot removed = crews.remove(index);

      for (CrewMemberSnapshot member : removed.members()) {
         onlinePlayer(member.uuid()).ifPresent(MinePieceBridge::clearCrewVariables);
      }

      CrewPersistenceStore.saveSnapshots(crews, "Mine Piece crew disbanded");
      return true;
   }

   public static double reputationBonus(ServerPlayer player) {
      PlayerVariables vars = variables(player).orElse(null);
      if (vars == null) {
         return 0.0;
      }

      double bountyScore = vars.PlayerBounty / 1000000.0;
      double levelScore = vars.PlayerLevel / 25.0;
      double hakiScore = Math.max(vars.ExpArmament, vars.ExpObservation) / 1000.0;
      return Math.min(3.0, Math.max(0.0, bountyScore + levelScore + hakiScore));
   }

   public static boolean hasAdeptBusoshoku(ServerPlayer player) {
      return armamentLevel(player) >= 8.0;
   }

   public static double dorikiProgress(ServerPlayer player) {
      double limit = effectiveDorikiLimit(player);
      return limit <= 0.0 ? 0.0 : Math.max(0.0, Math.min(1.0, currentDoriki(player) / limit));
   }

   public static int baseDorikiLimit() {
      return 100000;
   }

   public static int effectiveDorikiLimit(ServerPlayer player) {
      int base = baseDorikiLimit();
      return player == null ? base : base + IslandDominionBonusData.get((ServerLevel) player.level()).dorikiLimitBonus(player.getUUID());
   }

   public static double currentDoriki(ServerPlayer player) {
      return variables(player).map(vars -> Math.max(0.0, vars.PlayerLevel * 1000.0)).orElse(0.0);
   }

   public static boolean applyDorikiDelta(ServerPlayer player, double delta) {
      if (player != null && delta != 0.0) {
         PlayerVariables vars = variables(player).orElse(null);
         if (vars == null) {
            return false;
         }

         double maxLevel = effectiveDorikiLimit(player) / 1000.0;
         vars.PlayerLevel = Math.max(0.0, Math.min(maxLevel, vars.PlayerLevel + delta / 1000.0));
         vars.syncPlayerVariables(player);
         return true;
      } else {
         return true;
      }
   }

   public static boolean hasMaxBusoshoku(ServerPlayer player) {
      return armamentLevel(player) >= 10.0;
   }

   public static boolean hasMaxKenbunshoku(ServerPlayer player) {
      return observationLevel(player) >= 10.0;
   }

   public static void resyncCharacterData(ServerPlayer player) {
      if (player != null) {
         Optional<CrewSnapshot> crew = CrewPersistenceStore.crewForMember(player.getUUID());
         if (crew.isEmpty()) {
            clearCrewVariables(player);
         } else {
            boolean captain = crew.get().members().stream().anyMatch(member -> player.getUUID().equals(member.uuid()) && member.captain());
            applyCrewVariables(player, crew.get().name(), captainName(crew.get()), captain);
         }
      }
   }

   public static void logPotentialGriefSources() {
      List<String> markers = List.of("fire", "flame", "lava", "magma", "explosion", "bomb", "meteor", "ice", "poison", "gas", "room", "dark", "dyna");
      String entities = BuiltInRegistries.ENTITY_TYPE
         .keySet()
         .stream()
         .filter(id -> "minepiece".equals(id.getNamespace()))
         .filter(id -> containsAny(id, markers))
         .<String>map(Identifier::toString)
         .sorted()
         .limit(80L)
         .reduce((left, right) -> left + ", " + right)
         .orElse("nenhuma entidade suspeita registrada");
      String items = BuiltInRegistries.ITEM
         .keySet()
         .stream()
         .filter(id -> "minepiece".equals(id.getNamespace()))
         .filter(id -> containsAny(id, markers))
         .<String>map(Identifier::toString)
         .sorted()
         .limit(80L)
         .reduce((left, right) -> left + ", " + right)
         .orElse("nenhum item suspeito registrado");
      OmpWorld.LOGGER.info("Mine Piece grief scan - entidades: {}", entities);
      OmpWorld.LOGGER.info("Mine Piece grief scan - itens: {}", items);
   }

   private static CrewMemberSnapshot member(ServerPlayer player, boolean captain) {
      return new CrewMemberSnapshot(player.getUUID(), player.getGameProfile().name(), captain);
   }

   private static CrewInfo crewInfo(CrewSnapshot snapshot) {
      UUID captainId = null;
      int members = 0;

      for (CrewMemberSnapshot member : snapshot.members()) {
         if (member.uuid() != null) {
            members++;
            if (member.captain()) {
               captainId = member.uuid();
            }
         }
      }

      if (captainId == null && !snapshot.members().isEmpty()) {
         captainId = snapshot.members().get(0).uuid();
      }

      return new CrewInfo(snapshot.name(), captainId, members);
   }

   private static Optional<CrewSnapshot> snapshotByName(String name) {
      String normalized = normalizeCrew(name);
      return CrewPersistenceStore.snapshots().stream().filter(crew -> normalizeCrew(crew.name()).equals(normalized)).findFirst();
   }

   private static int indexByName(List<CrewSnapshot> crews, String name) {
      String normalized = normalizeCrew(name);

      for (int i = 0; i < crews.size(); i++) {
         if (normalizeCrew(crews.get(i).name()).equals(normalized)) {
            return i;
         }
      }

      return -1;
   }

   private static Map<String, CrewSnapshot> snapshotsByName(List<CrewSnapshot> snapshots) {
      Map<String, CrewSnapshot> result = new LinkedHashMap<>();

      for (CrewSnapshot snapshot : snapshots) {
         if (snapshot.name() != null && !snapshot.name().isBlank()) {
            result.put(normalizeCrew(snapshot.name()), snapshot);
         }
      }

      return result;
   }

   private static Optional<PlayerVariables> variables(ServerPlayer player) {
      return player == null ? Optional.empty() : Optional.of(MinepieceModVariables.get(player));
   }

   private static void applyCrewVariables(ServerPlayer player, String crewName, String captainName, boolean captain) {
      variables(player).ifPresent(vars -> {
         vars.pirates_crew_name = crewName == null ? "" : crewName;
         vars.team_captain = captain;
         vars.team_captain_name = captainName == null ? "" : captainName;
         vars.syncPlayerVariables(player);
      });
   }

   private static void clearCrewVariables(ServerPlayer player) {
      variables(player).ifPresent(vars -> {
         vars.pirates_crew_name = "";
         vars.team_captain = false;
         vars.team_captain_name = "";
         vars.syncPlayerVariables(player);
      });
   }

   private static String captainName(CrewSnapshot crew) {
      return crew.members().stream().filter(CrewMemberSnapshot::captain).findFirst().map(CrewMemberSnapshot::username).orElse("");
   }

   private static void syncOnlineCrewMembers(List<CrewSnapshot> crews) {
      for (CrewSnapshot crew : crews) {
         String captainName = captainName(crew);

         for (CrewMemberSnapshot member : crew.members()) {
            onlinePlayer(member.uuid()).ifPresent(player -> applyCrewVariables(player, crew.name(), captainName, member.captain()));
         }
      }
   }

   private static Optional<ServerPlayer> onlinePlayer(UUID uuid) {
      return uuid != null && OmpWorld.currentServer != null
         ? Optional.ofNullable(OmpWorld.currentServer.getPlayerList().getPlayer(uuid))
         : Optional.empty();
   }

   private static double armamentLevel(ServerPlayer player) {
      AttributeInstance attribute = player == null ? null : player.getAttribute(MinepieceModAttributes.ARMAMENT_HAKI_LEVEL);
      return attribute == null ? 0.0 : attribute.getValue();
   }

   private static double observationLevel(ServerPlayer player) {
      AttributeInstance attribute = player == null ? null : player.getAttribute(MinepieceModAttributes.OBSERVATION_HAKI_LEVEL);
      return attribute == null ? 0.0 : attribute.getValue();
   }

   private static boolean containsAny(Identifier id, List<String> markers) {
      String path = id.getPath();

      for (String marker : markers) {
         if (path.contains(marker)) {
            return true;
         }
      }

      return false;
   }

   private static String normalizeCrew(String crew) {
      return crew == null ? "" : crew.toLowerCase(Locale.ROOT).replace(' ', '_');
   }
}
