package com.donopot.voiddynastybridge;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

public final class DiagnosticConnection implements AutoCloseable {
   private static final int PORT = 28766;
   private static final Set<String> SNAPSHOT_FIELDS = Set.of(
      "protocol_version",
      "type",
      "diagnostic_only",
      "delivery_enabled",
      "session_id",
      "sequence",
      "campaign_loaded",
      "native_campaign_id",
      "zone",
      "game_mode",
      "save_state",
      "status"
   );
   private volatile boolean closed;
   private volatile Socket socket;
   private volatile DiagnosticConnection.Snapshot snapshot;
   private volatile String connectionStatus = "Waiting for Rogue Trader";
   private Thread worker;
   private final Path configPath;
   private final int port;

   public DiagnosticConnection() {
      this(Path.of(System.getenv("LOCALAPPDATA") == null ? "." : System.getenv("LOCALAPPDATA"), "VoidDynastyBridge", "diagnostic.local.json"), 28766);
   }

   DiagnosticConnection(Path var1, int var2) {
      this.configPath = var1;
      this.port = var2;
   }

   public synchronized void start() {
      if (this.worker != null) {
         throw new IllegalStateException("Already started");
      } else {
         this.worker = new Thread(this::run, "VoidDynasty diagnostic reader");
         this.worker.setDaemon(true);
         this.worker.start();
      }
   }

   public DiagnosticConnection.Snapshot snapshot() {
      DiagnosticConnection.Snapshot var1 = this.snapshot;
      return var1 != null && var1.fresh() ? var1 : null;
   }

   public String status() {
      DiagnosticConnection.Snapshot var1 = this.snapshot;
      return var1 != null && !var1.fresh() ? "Rogue Trader state expired" : this.connectionStatus;
   }

   private String token() throws IOException {
      Path var1 = this.configPath;
      if (Files.isRegularFile(var1) && Files.size(var1) <= 4096L) {
         JsonObject var2 = JsonParser.parseString(Files.readString(var1, StandardCharsets.UTF_8)).getAsJsonObject();
         String var3 = var2.get("token").getAsString();
         if ("1".equals(var2.get("protocol_version").toString()) && var2.get("port").getAsInt() == this.port && var3.matches("[0-9a-f]{64}")) {
            return var3;
         } else {
            throw new IOException("Invalid diagnostic configuration");
         }
      } else {
         throw new IOException("Start the Rogue Trader bridge first");
      }
   }

   private static String readFrame(BufferedInputStream var0, Socket var1, long var2) throws IOException {
      ByteArrayOutputStream var4 = new ByteArrayOutputStream(1024);

      while (var4.size() < 65536) {
         long var5 = (var2 - System.nanoTime()) / 1000000L;
         if (var5 <= 0L) {
            throw new IOException("Diagnostic heartbeat expired");
         }

         var1.setSoTimeout((int)Math.max(1L, Math.min(var5, 6000L)));
         int var7 = var0.read();
         if (var7 == -1) {
            throw new IOException("Bridge disconnected");
         }

         if (var7 == 10) {
            return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(var4.toByteArray())).toString();
         }

         var4.write(var7);
      }

      throw new IOException("Snapshot exceeds frame limit");
   }

   static DiagnosticConnection.Snapshot parse(String var0) throws IOException {
      try {
         JsonObject var1 = JsonParser.parseString(var0).getAsJsonObject();
         if (var1.keySet().equals(SNAPSHOT_FIELDS)
            && "1".equals(var1.get("protocol_version").toString())
            && "diagnostic_snapshot".equals(var1.get("type").getAsString())
            && "true".equals(var1.get("diagnostic_only").toString())
            && "false".equals(var1.get("delivery_enabled").toString())
            && ("true".equals(var1.get("campaign_loaded").toString()) || "false".equals(var1.get("campaign_loaded").toString()))) {
            String var2 = var1.get("session_id").getAsString();
            if (!UUID.fromString(var2).toString().equals(var2)) {
               throw new IOException("Invalid session");
            } else {
               long var3 = var1.get("sequence").getAsLong();
               if (var3 >= 1L && Long.toString(var3).equals(var1.get("sequence").toString())) {
                  boolean var5 = var1.get("campaign_loaded").getAsBoolean();
                  String var6 = var5 ? var1.get("native_campaign_id").getAsString() : null;
                  String var7 = var5 ? var1.get("zone").getAsString() : null;
                  String var8 = var1.get("game_mode").getAsString();
                  String var9 = var1.get("status").getAsString();
                  if ((var6 == null || var6.length() <= 256) && (var7 == null || var7.length() <= 1024) && var8.length() <= 128 && var9.length() <= 1024) {
                     return new DiagnosticConnection.Snapshot(var6, var7, var8, var9, var5, var2, var3, System.nanoTime());
                  } else {
                     throw new IOException("Snapshot field too large");
                  }
               } else {
                  throw new IOException("Invalid sequence");
               }
            }
         } else {
            throw new IOException("Invalid diagnostic snapshot");
         }
      } catch (RuntimeException var10) {
         throw new IOException("Invalid diagnostic snapshot", var10);
      }
   }

   private void run() {
      int[] var1 = new int[]{1, 2, 5, 10};
      int var2 = 0;

      while (!this.closed) {
         try {
            String var3 = this.token();

            try (Socket var4 = new Socket()) {
               label146: {
                  this.socket = var4;
                  if (!this.closed) {
                     var4.connect(new InetSocketAddress("127.0.0.1", this.port), 2000);
                     var4.setSoTimeout(6000);
                     var4.setTcpNoDelay(true);
                     JsonObject var5 = new JsonObject();
                     var5.addProperty("protocol_version", 1);
                     var5.addProperty("type", "diagnostic_hello");
                     var5.addProperty("token", var3);
                     var4.getOutputStream().write((var5 + "\n").getBytes(StandardCharsets.UTF_8));
                     BufferedInputStream var6 = new BufferedInputStream(var4.getInputStream());
                     DiagnosticConnection.Snapshot var7 = null;
                     long var8 = System.nanoTime();

                     while (true) {
                        if (this.closed) {
                           break label146;
                        }

                        DiagnosticConnection.Snapshot var10 = parse(readFrame(var6, var4, var8 + 6000000000L));
                        if (var7 != null && var10.sessionId().equals(var7.sessionId()) && var10.sequence() <= var7.sequence()) {
                           if (System.nanoTime() - var8 >= 6000000000L) {
                              throw new IOException("Game updates stopped");
                           }
                        } else {
                           var7 = var10;
                           this.snapshot = var10;
                           var8 = System.nanoTime();
                           var2 = 0;
                           this.connectionStatus = "Connected (read-only diagnostic)";
                        }
                     }
                  }

                  return;
               }
            }
         } catch (RuntimeException | IOException var20) {
            this.snapshot = null;
            this.connectionStatus = "Rogue Trader unavailable; start its bridge diagnostic";
         } finally {
            this.socket = null;
         }

         if (!this.closed) {
            try {
               Thread.sleep((long)var1[Math.min(var2++, var1.length - 1)] * 1000L);
            } catch (InterruptedException var18) {
               if (this.closed) {
                  return;
               }
            }
         }
      }
   }

   @Override
   public void close() {
      this.closed = true;
      this.snapshot = null;
      Socket var1 = this.socket;
      if (var1 != null) {
         try {
            var1.close();
         } catch (IOException var3) {
         }
      }

      if (this.worker != null) {
         this.worker.interrupt();
      }
   }

   public static record Snapshot(
      String campaignId, String zone, String mode, String status, boolean campaignLoaded, String sessionId, long sequence, long receivedAt
   ) {
      public boolean fresh() {
         return System.nanoTime() - this.receivedAt < 6000000000L;
      }
   }
}
