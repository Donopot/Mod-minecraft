package br.com.oneminepiece.opworldlike.crew;

import br.com.oneminepiece.opworldlike.OmpWorld;
import br.com.oneminepiece.opworldlike.integration.MinePieceBridge;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public final class CrewPersistenceStore {
   private static final Path STORE_PATH = Path.of("config", "opworldlike", "crews.json");
   private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

   private CrewPersistenceStore() {
   }

   public static void saveCurrent(String reason) {
      saveCurrent(reason, false);
   }

   public static void saveCurrentExact(String reason) {
      saveCurrent(reason, true);
   }

   private static void saveCurrent(String reason, boolean exact) {
      if (!MinePieceBridge.crewApiAvailable()) {
         OmpWorld.LOGGER.warn("Skipping crew snapshot save for {}; Mine Piece crew API unavailable.", reason);
      } else {
         List<CrewSnapshot> crews = MinePieceBridge.crewSnapshots();
         save(exact ? crews : mergeWithStored(crews), reason);
      }
   }

   public static int restoreMissingCrews() {
      List<CrewSnapshot> snapshots = load();
      if (snapshots.isEmpty()) {
         return 0;
      }

      int restored = MinePieceBridge.restoreMissingCrews(snapshots);
      if (restored > 0) {
         OmpWorld.LOGGER.info("Restored {} Mine Piece crew(s) from {}.", restored, STORE_PATH);
      }

      return restored;
   }

   public static Optional<CrewSnapshot> crewForMember(UUID playerId) {
      if (playerId == null) {
         return Optional.empty();
      }

      for (CrewSnapshot crew : load()) {
         for (CrewMemberSnapshot member : crew.members()) {
            if (playerId.equals(member.uuid())) {
               return Optional.of(crew);
            }
         }
      }

      return Optional.empty();
   }

   public static List<CrewSnapshot> snapshots() {
      return load();
   }

   public static void saveSnapshots(List<CrewSnapshot> crews, String reason) {
      save(crews == null ? List.of() : crews, reason);
   }

   private static void save(List<CrewSnapshot> crews, String reason) {
      try {
         Files.createDirectories(STORE_PATH.getParent());

         try (Writer writer = Files.newBufferedWriter(STORE_PATH, StandardCharsets.UTF_8)) {
            GSON.toJson(toJson(crews), writer);
         }

         OmpWorld.LOGGER.debug("Saved {} crew snapshot(s) to {} after {}.", new Object[]{crews.size(), STORE_PATH, reason});
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not save crew snapshots to {} after {}.", new Object[]{STORE_PATH, reason, ex});
      }
   }

   private static List<CrewSnapshot> mergeWithStored(List<CrewSnapshot> current) {
      List<CrewSnapshot> stored = load();
      if (stored.isEmpty()) {
         return current;
      }

      if (current.isEmpty()) {
         return stored;
      }

      Map<String, CrewSnapshot> merged = new LinkedHashMap<>();

      for (CrewSnapshot crew : stored) {
         if (crew.name() != null && !crew.name().isBlank()) {
            merged.put(normalizeCrew(crew.name()), crew);
         }
      }

      for (CrewSnapshot crew : current) {
         if (crew.name() != null && !crew.name().isBlank()) {
            String key = normalizeCrew(crew.name());
            CrewSnapshot previous = merged.get(key);
            merged.put(key, previous == null ? crew : mergeCrew(previous, crew));
         }
      }

      return new ArrayList<>(merged.values());
   }

   private static CrewSnapshot mergeCrew(CrewSnapshot stored, CrewSnapshot current) {
      Map<UUID, CrewMemberSnapshot> members = new LinkedHashMap<>();
      UUID currentCaptain = captainUuid(current);

      for (CrewMemberSnapshot member : stored.members()) {
         if (member.uuid() != null) {
            boolean captain = currentCaptain == null ? member.captain() : member.uuid().equals(currentCaptain);
            members.put(member.uuid(), new CrewMemberSnapshot(member.uuid(), member.username(), captain));
         }
      }

      for (CrewMemberSnapshot member : current.members()) {
         if (member.uuid() != null) {
            CrewMemberSnapshot previous = members.get(member.uuid());
            boolean captain = member.captain() || currentCaptain == null && previous != null && previous.captain();
            String username = member.username() != null && !member.username().isBlank()
               ? member.username()
               : (previous == null ? member.uuid().toString() : previous.username());
            members.put(member.uuid(), new CrewMemberSnapshot(member.uuid(), username, captain));
         }
      }

      long creationDate = current.creationDate() > 0L ? current.creationDate() : stored.creationDate();
      return new CrewSnapshot(current.name(), creationDate, new ArrayList<>(members.values()));
   }

   private static UUID captainUuid(CrewSnapshot crew) {
      for (CrewMemberSnapshot member : crew.members()) {
         if (member.captain()) {
            return member.uuid();
         }
      }

      return null;
   }

   private static List<CrewSnapshot> load() {
      if (Files.notExists(STORE_PATH)) {
         return List.of();
      }

      try (Reader reader = Files.newBufferedReader(STORE_PATH, StandardCharsets.UTF_8)) {
         JsonElement root = JsonParser.parseReader(reader);
         if (!root.isJsonArray()) {
            return List.of();
         }

         List<CrewSnapshot> crews = new ArrayList<>();

         for (JsonElement crewElement : root.getAsJsonArray()) {
            if (crewElement.isJsonObject()) {
               CrewSnapshot crew = readCrew(crewElement.getAsJsonObject());
               if (crew != null && !crew.members().isEmpty()) {
                  crews.add(crew);
               }
            }
         }

         return crews;
      } catch (IOException | RuntimeException ex) {
         OmpWorld.LOGGER.warn("Could not load crew snapshots from {}.", STORE_PATH, ex);
         return List.of();
      }
   }

   private static JsonArray toJson(List<CrewSnapshot> crews) {
      JsonArray root = new JsonArray();

      for (CrewSnapshot crew : crews) {
         JsonObject crewObject = new JsonObject();
         crewObject.addProperty("name", crew.name());
         crewObject.addProperty("creationDate", crew.creationDate());
         JsonArray members = new JsonArray();

         for (CrewMemberSnapshot member : crew.members()) {
            JsonObject memberObject = new JsonObject();
            memberObject.addProperty("uuid", member.uuid().toString());
            memberObject.addProperty("username", member.username());
            memberObject.addProperty("captain", member.captain());
            members.add(memberObject);
         }

         crewObject.add("members", members);
         root.add(crewObject);
      }

      return root;
   }

   private static CrewSnapshot readCrew(JsonObject object) {
      String name = stringValue(object, "name");
      if (name.isBlank()) {
         return null;
      }

      long creationDate = object.has("creationDate") ? object.get("creationDate").getAsLong() : 0L;
      List<CrewMemberSnapshot> members = new ArrayList<>();
      if (object.has("members") && object.get("members").isJsonArray()) {
         for (JsonElement memberElement : object.getAsJsonArray("members")) {
            if (memberElement.isJsonObject()) {
               JsonObject memberObject = memberElement.getAsJsonObject();
               String uuidText = stringValue(memberObject, "uuid");
               if (!uuidText.isBlank()) {
                  UUID uuid = UUID.fromString(uuidText);
                  String username = stringValue(memberObject, "username");
                  boolean captain = memberObject.has("captain") && memberObject.get("captain").getAsBoolean();
                  members.add(new CrewMemberSnapshot(uuid, username.isBlank() ? uuidText : username, captain));
               }
            }
         }
      }

      return new CrewSnapshot(name, creationDate, members);
   }

   private static String stringValue(JsonObject object, String key) {
      return object.has(key) && !object.get(key).isJsonNull() ? object.get(key).getAsString() : "";
   }

   private static String normalizeCrew(String crew) {
      return crew.toLowerCase(Locale.ROOT).replace(' ', '_');
   }
}
