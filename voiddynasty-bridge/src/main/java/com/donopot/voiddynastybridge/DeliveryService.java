package com.donopot.voiddynastybridge;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;

public final class DeliveryService implements AutoCloseable {
   public static final String MISSION = "9fe289e9bb643972571c685e165cea73ffba139bae3392292b7496b8066b19cf";
   public final MinecraftServer server;
   private final Path file;
   private BridgeWorld s;
   private final ExecutorService worker = Executors.newSingleThreadExecutor(var0 -> {
      Thread var1x = new Thread(var0, "VoidDynasty delivery");
      var1x.setDaemon(true);
      return var1x;
   });
   private volatile Socket socket;
   private volatile boolean closed;
   private boolean inFlight;
   private boolean connected;
   private boolean sending;
   private final DeliveryInteraction interaction = new DeliveryInteraction();
   private String availability = "Connexion en cours";
   private long nextPoll;
   private long received;
   private JsonObject remote;
   private String message = "Connexion a Rogue Trader...";
   private volatile DeliveryService.View view = new DeliveryService.View(
      "Chargement",
      "Connexion en cours",
      "",
      "",
      "",
      "IDLE",
      "",
      0,
      0,
      0,
      0,
      false,
      false,
      false,
      false,
      "Composants de vaisseau",
      64,
      0,
      false,
      "logistics_first_delivery"
   );

   public DeliveryService.View view() {
      return this.view;
   }

   public DeliveryService(MinecraftServer var1) {
      this.server = var1;
      this.file = var1.getWorldPath(LevelResource.ROOT).resolve("data/voiddynasty-bridge.json");

      try {
         this.s = BridgeWorld.read(this.file);
         this.s.save(this.file);
         if (Set.of("RESERVING", "SETTLING").contains(this.s.phase)) {
            this.recover("Ecriture interrompue: checkpoint requis");
         }
      } catch (Exception var3) {
         this.s = new BridgeWorld();
         this.s.phase = "RECOVERY_REQUIRED";
         this.message = "Journal illisible; conserver les fichiers et restaurer un checkpoint";
      }
   }

   public boolean locked() {
      return !Set.of("IDLE", "DRAFT", "SETTLED").contains(this.s.phase) || this.closed;
   }

   public boolean matches(CargoBlockEntity var1) {
      return var1.world.equals(this.s.world)
         && var1.crate.equals(this.s.crate)
         && var1.getBlockPos().equals(new BlockPos(this.s.x, this.s.y, this.s.z))
         && var1.getLevel() != null
         && var1.getLevel().dimension().identifier().toString().equals(this.s.dimension);
   }

   private CargoBlockEntity cargo() {
      if (this.s.crate.isEmpty()) {
         return null;
      } else {
         ServerLevel var1 = this.server.getLevel(ResourceKey.create(Registries.DIMENSION, Identifier.parse(this.s.dimension)));
         if (var1 == null) {
            throw new IllegalStateException("Dimension introuvable");
         } else {
            BlockPos var2 = new BlockPos(this.s.x, this.s.y, this.s.z);
            var1.getChunkAt(var2);
            if (var1.getBlockEntity(var2) instanceof CargoBlockEntity var3 && this.matches(var3)) {
               return var3;
            }

            throw new IllegalStateException("Conteneur absent, copie ou deplace");
         }
      }
   }

   private CargoCatalog.Mission activeMission() {
      return CargoCatalog.get(this.s.delivery_mission_id);
   }

   private void verifyCargo(CargoBlockEntity var1) {
      if (var1 == null) {
         if (!this.s.delivery.isEmpty()) {
            throw new IllegalStateException("Conteneur manquant");
         }
      } else {
         String var3 = this.s.phase;

         boolean var2 = switch (var3) {
            case "IDLE" -> var1.reserved == 0 && var1.phase.equals("IDLE") && var1.delivery.isEmpty();
            case "DRAFT" -> var1.reserved == 0
            && (
               this.s.completed_count == 0
                  ? var1.phase.equals("IDLE") && var1.delivery.isEmpty()
                  : var1.phase.equals("SETTLED") && var1.delivery.equals(this.s.last_receipt_id)
            );
            case "RESERVED" -> var1.reserved == this.activeMission().count()
            && var1.reservedItem.equals(this.activeMission().item())
            && var1.mission.equals(this.s.delivery_mission_id)
            && var1.phase.equals("RESERVED")
            && var1.delivery.equals(this.s.delivery);
            case "SETTLED" -> var1.reserved == 0 && var1.phase.equals("SETTLED") && var1.delivery.equals(this.s.delivery);
            default -> false;
         };
         if (!var2) {
            throw new IllegalStateException("Divergence conteneur / registre: checkpoint requis");
         }
      }
   }

   private void persist() throws IOException {
      this.s.save(this.file);
   }

   private void recover(String var1) {
      this.sending = false;
      this.connected = false;
      this.s.phase = "RECOVERY_REQUIRED";
      this.message = var1;

      try {
         this.persist();
      } catch (Exception var3) {
      }
   }

   private boolean safe() {
      return !this.closed && this.server.isSingleplayer() && !this.server.isPublished() && !this.s.phase.equals("RECOVERY_REQUIRED");
   }

   public void tick() {
      if (!this.safe()) {
         this.connected = false;
         this.publish();
      } else {
         try {
            CargoBlockEntity var1 = this.cargo();
            this.verifyCargo(var1);
            if (System.currentTimeMillis() - this.received > 6000L) {
               this.connected = false;
            }

            if (!this.inFlight && System.currentTimeMillis() >= this.nextPoll) {
               this.nextPoll = System.currentTimeMillis() + 2000L;
               this.request("inspect");
            }
         } catch (Exception var2) {
            this.recover(var2.getMessage());
         }

         this.publish();
      }
   }

   private void publish() {
      CargoBlockEntity var1 = null;

      try {
         var1 = this.cargo();
      } catch (Exception var8) {
      }

      boolean var2 = this.connected && System.currentTimeMillis() - this.received < 6000L;
      CargoCatalog.Mission var3 = CargoCatalog.get(this.s.selected_mission_id);
      int var4 = 0;

      try {
         if (var1 != null) {
            var4 = var1.count(var3.item());
         }
      } catch (Exception var7) {
      }

      boolean var5 = this.remote != null && text(this.remote, "code").equals("OK") && this.remote.get("available").getAsBoolean();
      if (this.s.phase.equals("SETTLED")) {
         this.message = "Derniere livraison confirmee (" + this.s.completed_count + " au total)";
      }

      String var6 = this.availability;
      if (var5) {
         if (this.s.completed_count >= 256) {
            var6 = "Registre plein : nouvelle version necessaire";
         } else if (this.interaction.mutationBusy()) {
            var6 = "Enregistrement de la livraison en cours";
         } else if (!this.s.paired) {
            var6 = "Lier le monde a la campagne";
         } else if (!this.s.phase.equals("IDLE") && !this.s.phase.equals("SETTLED")) {
            var6 = "Livraison en cours; Reprendre si necessaire";
         } else {
            var6 = var4 >= var3.count() ? "Pret : une cargaison native " + var3.label() : "Deposer encore " + (var3.count() - var4) + " unites";
         }
      }

      this.view = new DeliveryService.View(
         this.message,
         var2 ? var6 : "Rogue Trader deconnecte ou sans reponse recente",
         this.s.world,
         this.remote == null ? "" : text(this.remote, "campaign_name"),
         this.remote == null ? "" : text(this.remote, "zone"),
         this.s.phase,
         this.s.delivery,
         var4,
         var1 == null ? 0 : var1.reserved,
         this.remote != null && !text(this.remote, "campaign_id").isEmpty() ? this.remote.get("reserve").getAsInt() : -1,
         this.s.completed_count,
         this.s.paired,
         var2,
         this.safe()
            && this.s.completed_count < 256
            && DeliveryInteraction.canShip(var2, var5, this.s.paired, this.s.phase, var4, var3.count(), this.interaction.mutationBusy()),
         var1 != null,
         var3.label() + " : " + var3.count() + " " + var3.material(),
         var3.count(),
         this.remote == null ? 0 : this.remote.get("native_count").getAsInt(),
         this.safe() && !this.interaction.mutationBusy() && (this.s.phase.equals("IDLE") || this.s.phase.equals("SETTLED")),
         var3.id()
      );
   }

   public void action(UUID var1, String var2) {
      if (!this.server.isSameThread()) {
         throw new IllegalStateException("Server thread required");
      } else {
         try {
            if (!this.safe()) {
               throw new IllegalStateException("Pont indisponible");
            }

            if (this.inFlight) {
               if (this.interaction.defer(var1, var2)) {
                  return;
               }

               throw new IllegalStateException("Operation en cours");
            }

            if (var2.startsWith("ship:")) {
               if (!var2.substring(5).equals(this.s.selected_mission_id)) {
                  throw new IllegalStateException("Mission modifiee : confirmer a nouveau");
               }

               var2 = "ship";
            }

            ServerPlayer var3 = this.server.getPlayerList().getPlayer(var1);
            if (var3 == null) {
               throw new IllegalStateException("Joueur absent");
            }

            if (var2.equals("previous_mission") || var2.equals("next_mission")) {
               if (!this.s.phase.equals("IDLE") && !this.s.phase.equals("SETTLED")) {
                  throw new IllegalStateException("Terminer la livraison avant de changer de mission");
               }

               int var10 = 0;

               while (!CargoCatalog.ALL.get(var10).id().equals(this.s.selected_mission_id)) {
                  var10++;
               }

               var10 = Math.floorMod(var10 + (var2.equals("next_mission") ? 1 : -1), CargoCatalog.ALL.size());
               this.s.selected_mission_id = CargoCatalog.ALL.get(var10).id();
               this.persist();
               this.publish();
               return;
            }

            if (var2.equals("create")) {
               if (!this.s.crate.isEmpty()) {
                  throw new IllegalStateException("Un depot existe deja dans ce monde");
               }

               BlockPos var9 = var3.blockPosition().relative(var3.getDirection(), 2);
               ServerLevel var5 = var3.level();
               if (!var5.isEmptyBlock(var9) || !var5.isInWorldBounds(var9)) {
                  throw new IllegalStateException("Il faut une case libre deux blocs devant toi");
               }

               this.s.crate = UUID.randomUUID().toString();
               this.s.dimension = var5.dimension().identifier().toString();
               this.s.x = var9.getX();
               this.s.y = var9.getY();
               this.s.z = var9.getZ();
               this.persist();
               if (!var5.setBlockAndUpdate(var9, BridgeMod.CARGO.defaultBlockState())) {
                  throw new IOException("Pose impossible");
               }

               CargoBlockEntity var6 = (CargoBlockEntity)var5.getBlockEntity(var9);
               var6.world = this.s.world;
               var6.crate = this.s.crate;
               var6.setChanged();
               this.server.saveEverything(false, true, true);
               this.persist();
               this.message = "Depot cree: clic droit pour deposer le fer";
            } else {
               label159: {
                  CargoBlockEntity var4 = this.cargo();
                  this.verifyCargo(var4);
                  if (var4 != null
                     && var3.level() == var4.getLevel()
                     && !(var3.distanceToSqr((double)this.s.x + 0.5, (double)this.s.y + 0.5, (double)this.s.z + 0.5) > 64.0)) {
                     if (var2.equals("open")) {
                        var3.openMenu(var4);
                        break label159;
                     }

                     if (var2.equals("pair")) {
                        if (this.connected && this.remote != null) {
                           if (this.s.paired) {
                              throw new IllegalStateException("Monde deja lie");
                           }

                           if (this.s.campaign.isEmpty()) {
                              this.s.campaign = text(this.remote, "campaign_id");
                              this.s.pair = UUID.randomUUID().toString();
                              this.s.checkpoint = UUID.randomUUID().toString();
                              this.persist();
                           }

                           this.request("propose_pair");
                           break label159;
                        }

                        throw new IllegalStateException("Rogue Trader indisponible");
                     }

                     if (var2.equals("ship")) {
                        if (!this.view.canShip()) {
                           throw new IllegalStateException("Materiaux, lien et campagne disponibles requis");
                        }

                        var3.closeContainer();
                        this.s.delivery_mission_id = this.s.selected_mission_id;
                        this.s.delivery = UUID.randomUUID().toString();
                        this.s.hash = fingerprint(this.s);
                        this.s.phase = "DRAFT";
                        this.persist();
                        this.sending = true;
                        this.request("prepare");
                        break label159;
                     }

                     if (!var2.equals("resume")) {
                        break label159;
                     }

                     if (this.s.paired && (this.s.phase.equals("DRAFT") || this.s.phase.equals("RESERVED"))) {
                        this.sending = true;
                        this.request("status");
                        break label159;
                     }

                     throw new IllegalStateException("Aucune livraison reprenable");
                  }

                  throw new IllegalStateException("Approche-toi du depot (8 blocs maximum)");
               }
            }
         } catch (IOException var7) {
            this.recover("Echec de sauvegarde: cargaison verrouillee");
         } catch (Exception var8) {
            this.message = var8.getMessage();
         }

         this.publish();
      }
   }

   private JsonObject envelope(String var1) {
      JsonObject var2 = new JsonObject();
      var2.addProperty("protocol_version", 4);
      var2.addProperty("type", var1);
      var2.addProperty("message_id", UUID.randomUUID().toString());
      var2.addProperty("session_id", this.remote == null ? "" : text(this.remote, "session_id"));
      var2.addProperty("campaign_id", this.s.campaign);
      var2.addProperty("world_id", this.s.world);
      var2.addProperty("pair_id", this.s.pair);
      var2.addProperty("checkpoint_id", this.s.checkpoint);
      var2.addProperty("delivery_id", this.s.delivery);
      var2.addProperty("known_state", this.s.phase);
      var2.addProperty("mission_hash", "9fe289e9bb643972571c685e165cea73ffba139bae3392292b7496b8066b19cf");
      var2.addProperty("payload_hash", this.s.hash);
      CargoCatalog.Mission var3 = this.s.phase.equals("IDLE") ? CargoCatalog.get(this.s.selected_mission_id) : this.activeMission();
      var2.addProperty("mission_id", var3.id());
      var2.addProperty("item_id", var3.item());
      var2.addProperty("count", var3.count());
      var2.addProperty("completed_count", this.s.completed_count);
      var2.addProperty("last_receipt_id", this.s.last_receipt_id);
      return var2;
   }

   private void request(String var1) {
      JsonObject var2 = this.envelope(var1);
      this.inFlight = true;
      this.interaction.started(var1);
      if (!var1.equals("inspect")) {
         this.message = switch (var1) {
            case "prepare" -> "Preparation...";
            case "commit" -> "Livraison en cours; sauvegarde Rogue Trader...";
            case "propose_pair" -> "Valide le monde affiche dans Rogue Trader (Ctrl+F10)";
            default -> "Rapprochement des recus...";
         };
      }

      this.worker.submit(() -> {
         JsonObject var3 = null;
         String var4 = null;

         try {
            var3 = this.rpc(var2);
         } catch (Exception var7) {
            var4 = "Connexion interrompue; utiliser Reprendre avec le meme recu";
         }

         JsonObject var5 = var3;
         String var6 = var4;
         if (!this.closed) {
            this.server.execute(() -> {
               if (!this.closed) {
                  this.inFlight = false;
                  DeliveryInteraction.Click var4x = this.interaction.completed();
                  if (var6 != null) {
                     this.connected = false;
                     this.availability = var6;
                     this.message = var6;
                     this.sending = false;
                     this.publish();
                  } else {
                     try {
                        this.accept(var1, var5);
                     } catch (Exception var6x) {
                        this.recover(var6x.getMessage());
                     }

                     this.publish();
                     if (var4x != null && !this.inFlight && this.safe()) {
                        this.action(var4x.player(), var4x.action());
                     }
                  }
               }
            });
         }
      });
   }

   private void accept(String var1, JsonObject var2) throws Exception {
      this.remote = var2;
      this.received = System.currentTimeMillis();
      String var3 = text(var2, "code");
      this.connected = true;
      this.availability = DeliveryInteraction.pauseMessage(var3);
      boolean var4 = var3.equals("PAUSED_MODE") || var3.equals("PAUSED_COMBAT");
      if (var3.equals("OK")) {
         this.availability = var2.get("available").getAsBoolean() ? "Envoi disponible" : "Envoi temporairement suspendu";
      }

      if (var3.equals("PAIR_APPROVAL_REQUIRED")) {
         this.message = "Dans Rogue Trader: Ctrl+F10 puis Lier cette campagne au monde affiche";
      } else if (!var3.equals("OK") && !var4) {
         if (Set.of("RECOVERY_REQUIRED", "PAYLOAD_MISMATCH", "PERSISTENCE_FAILED").contains(var3)) {
            this.recover(var3 + ": restaurer un checkpoint appaire");
         } else {
            this.message = this.availability.isEmpty() ? "Rogue Trader: " + var3 : this.availability;
            this.sending = false;
         }
      } else if (!this.s.campaign.isEmpty() && !this.s.campaign.equals(text(var2, "campaign_id"))) {
         throw new IOException("Campagne differente");
      } else if (this.s.pair.isEmpty()) {
         if (!text(var2, "pair_id").isEmpty()) {
            this.connected = false;
            this.message = "Cette campagne est deja liee a un monde; charger le monde correspondant";
         } else {
            this.message = "Connexion etablie; creer le depot puis lier la campagne";
         }
      } else if (this.s.pair.equals(text(var2, "pair_id"))
         && this.s.world.equals(text(var2, "world_id"))
         && this.s.checkpoint.equals(text(var2, "checkpoint_id"))) {
         if (!this.s.paired) {
            this.s.paired = true;
            this.persist();
            this.message = "Campagne et monde lies; choisir une mission";
         }

         DeliveryReconciliation.Result var5 = DeliveryReconciliation.inspect(this.s, var2);
         if (var5 == DeliveryReconciliation.Result.COMMITTED) {
            CargoBlockEntity var9 = this.cargo();
            this.verifyCargo(var9);
            this.s.phase = "SETTLING";
            this.persist();
            var9.settle(this.s.delivery);
            this.server.saveEverything(false, true, true);
            this.s.completed_count++;
            this.s.last_receipt_id = this.s.delivery;
            this.s.phase = "SETTLED";
            this.persist();
            this.sending = false;
            this.message = "Livraison confirmee : une cargaison native " + this.activeMission().label();
         } else if (var4) {
            this.sending = false;
         } else {
            if (this.sending && this.s.phase.equals("DRAFT")) {
               if (var5 == DeliveryReconciliation.Result.PREPARED) {
                  CargoBlockEntity var6 = this.cargo();
                  this.verifyCargo(var6);
                  if (var6.count(this.activeMission().item()) < this.activeMission().count()) {
                     this.sending = false;
                     this.message = "Preparation conservee; completer les materiaux puis Reprendre";
                     return;
                  }

                  for (ServerPlayer var8 : this.server.getPlayerList().getPlayers()) {
                     var8.closeContainer();
                  }

                  this.s.phase = "RESERVING";
                  this.persist();
                  var6.reserve(this.s.delivery, this.activeMission());
                  this.server.saveEverything(false, true, true);
                  this.s.phase = "RESERVED";
                  this.persist();
                  this.request("commit");
               } else {
                  if (var5 != DeliveryReconciliation.Result.NEED_PREPARE) {
                     throw new IOException("Etat distant incompatible");
                  }

                  this.request("prepare");
               }
            } else if (this.sending && this.s.phase.equals("RESERVED") && var5 == DeliveryReconciliation.Result.PREPARED) {
               this.request("commit");
            } else if (!this.s.phase.equals("SETTLED")) {
               this.message = this.s.phase.equals("IDLE")
                  ? "Mission repetable : une cargaison native par envoi"
                  : "Cargaison conservee; Reprendre pour poursuivre";
            }
         }
      } else if (!this.s.paired && text(var2, "pair_id").isEmpty()) {
         this.message = "Appairage a confirmer dans Rogue Trader";
      } else {
         throw new IOException("Divergence de paire/checkpoint");
      }
   }

   private JsonObject rpc(JsonObject var1) throws Exception {
      Path var2 = Path.of(System.getenv("LOCALAPPDATA"), "VoidDynastyBridge", "diagnostic.local.json");
      if (Files.size(var2) > 4096L) {
         throw new IOException();
      } else {
         JsonObject var3 = JsonParser.parseString(Files.readString(var2)).getAsJsonObject();
         var1.addProperty("token", text(var3, "token"));

         JsonObject var7;
         try (Socket var4 = new Socket()) {
            this.socket = var4;
            var4.connect(new InetSocketAddress("127.0.0.1", 28765), 3000);
            var4.setSoTimeout(130000);
            var4.getOutputStream().write((var1 + "\n").getBytes(StandardCharsets.UTF_8));
            ByteArrayOutputStream var5 = new ByteArrayOutputStream();

            int var6;
            while ((var6 = var4.getInputStream().read()) != 10) {
               if (var6 < 0 || var5.size() >= 4095) {
                  throw new IOException();
               }

               var5.write(var6);
            }

            var7 = DeliveryWire.reply(var5.toString(StandardCharsets.UTF_8), text(var1, "message_id"));
         } finally {
            this.socket = null;
         }

         return var7;
      }
   }

   public static String fingerprint(BridgeWorld var0) throws Exception {
      CargoCatalog.Mission var1 = CargoCatalog.get(var0.delivery_mission_id);
      String var2 = "{\"campaign_id\":\""
         + var0.campaign
         + "\",\"cargo\":[{\"count\":"
         + var1.count()
         + ",\"item_id\":\""
         + var1.item()
         + "\"}],\"mission_id\":\""
         + var1.id()
         + "\",\"mission_version\":3,\"native_cargo_blueprint\":\""
         + var1.blueprint()
         + "\",\"pair_id\":\""
         + var0.pair
         + "\",\"previous_delivery_id\":\""
         + var0.last_receipt_id
         + "\",\"sequence\":"
         + (var0.completed_count + 1)
         + ",\"world_id\":\""
         + var0.world
         + "\"}";
      return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(var2.getBytes(StandardCharsets.UTF_8)));
   }

   private static String text(JsonObject var0, String var1) {
      return var0.has(var1) && !var0.get(var1).isJsonNull() ? var0.get(var1).getAsString() : "";
   }

   @Override
   public void close() {
      this.closed = true;
      this.worker.shutdownNow();

      try {
         if (this.socket != null) {
            this.socket.close();
         }
      } catch (IOException var2) {
      }
   }

   public static record View(
      String status,
      String availability,
      String world,
      String campaign,
      String zone,
      String phase,
      String delivery,
      int iron,
      int reserved,
      int reserve,
      int completed,
      boolean paired,
      boolean connected,
      boolean canShip,
      boolean hasCrate,
      String mission,
      int required,
      int nativeCount,
      boolean canSelect,
      String missionId
   ) {
   }
}
