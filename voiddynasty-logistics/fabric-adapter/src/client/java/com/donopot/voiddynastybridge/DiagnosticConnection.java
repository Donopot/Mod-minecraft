package com.donopot.voiddynastybridge;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;

/** Loopback reader. No Minecraft or ship API is referenced by this worker. */
public final class DiagnosticConnection implements AutoCloseable {
    public record Snapshot(String campaignId, String zone, String mode, String status,
                           boolean campaignLoaded, String sessionId, long sequence, long receivedAt) {
        public boolean fresh() { return System.nanoTime() - receivedAt < 6_000_000_000L; }
    }

    private static final int PORT = 28766;
    private static final Set<String> SNAPSHOT_FIELDS = Set.of("protocol_version", "type", "diagnostic_only",
            "delivery_enabled", "session_id", "sequence", "campaign_loaded", "native_campaign_id", "zone",
            "game_mode", "save_state", "status");
    private volatile boolean closed;
    private volatile Socket socket;
    private volatile Snapshot snapshot;
    private volatile String connectionStatus = "Waiting for Rogue Trader";
    private Thread worker;
    private final Path configPath;
    private final int port;

    public DiagnosticConnection() {
        this(Path.of(System.getenv("LOCALAPPDATA") == null ? "." : System.getenv("LOCALAPPDATA"),
                "VoidDynastyBridge", "diagnostic.local.json"), PORT);
    }

    DiagnosticConnection(Path configPath, int port) {
        this.configPath = configPath;
        this.port = port;
    }

    public synchronized void start() {
        if (worker != null) throw new IllegalStateException("Already started");
        worker = new Thread(this::run, "VoidDynasty diagnostic reader");
        worker.setDaemon(true);
        worker.start();
    }

    public Snapshot snapshot() {
        Snapshot current = snapshot;
        return current != null && current.fresh() ? current : null;
    }

    public String status() {
        Snapshot current = snapshot;
        return current != null && !current.fresh() ? "Rogue Trader state expired" : connectionStatus;
    }

    private String token() throws IOException {
        Path path = configPath;
        if (!Files.isRegularFile(path) || Files.size(path) > 4096) throw new IOException("Start the Rogue Trader bridge first");
        JsonObject config = JsonParser.parseString(Files.readString(path, StandardCharsets.UTF_8)).getAsJsonObject();
        String token = config.get("token").getAsString();
        if (!"1".equals(config.get("protocol_version").toString()) || config.get("port").getAsInt() != port
                || !token.matches("[0-9a-f]{64}")) throw new IOException("Invalid diagnostic configuration");
        return token;
    }

    private static String readFrame(BufferedInputStream input, Socket connection, long deadline) throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream(1024);
        while (data.size() < 65536) {
            long remaining = (deadline - System.nanoTime()) / 1_000_000L;
            if (remaining <= 0) throw new IOException("Diagnostic heartbeat expired");
            connection.setSoTimeout((int)Math.max(1, Math.min(remaining, 6000)));
            int next = input.read();
            if (next == -1) throw new IOException("Bridge disconnected");
            if (next == '\n') return StandardCharsets.UTF_8.newDecoder().decode(java.nio.ByteBuffer.wrap(data.toByteArray())).toString();
            data.write(next);
        }
        throw new IOException("Snapshot exceeds frame limit");
    }

    static Snapshot parse(String frame) throws IOException {
        try {
            JsonObject value = JsonParser.parseString(frame).getAsJsonObject();
            if (!value.keySet().equals(SNAPSHOT_FIELDS) || !"1".equals(value.get("protocol_version").toString())
                    || !"diagnostic_snapshot".equals(value.get("type").getAsString())
                    || !"true".equals(value.get("diagnostic_only").toString())
                    || !"false".equals(value.get("delivery_enabled").toString())
                    || !("true".equals(value.get("campaign_loaded").toString()) || "false".equals(value.get("campaign_loaded").toString())))
                throw new IOException("Invalid diagnostic snapshot");
            String session = value.get("session_id").getAsString();
            if (!UUID.fromString(session).toString().equals(session)) throw new IOException("Invalid session");
            long sequence = value.get("sequence").getAsLong();
            if (sequence < 1 || !Long.toString(sequence).equals(value.get("sequence").toString())) throw new IOException("Invalid sequence");
            boolean loaded = value.get("campaign_loaded").getAsBoolean();
            String campaign = loaded ? value.get("native_campaign_id").getAsString() : null;
            String zone = loaded ? value.get("zone").getAsString() : null;
            String mode = value.get("game_mode").getAsString();
            String status = value.get("status").getAsString();
            if ((campaign != null && campaign.length() > 256) || (zone != null && zone.length() > 1024)
                    || mode.length() > 128 || status.length() > 1024) throw new IOException("Snapshot field too large");
            return new Snapshot(campaign, zone, mode, status, loaded, session, sequence, System.nanoTime());
        } catch (RuntimeException failure) {
            throw new IOException("Invalid diagnostic snapshot", failure);
        }
    }

    private void run() {
        int[] delays = {1, 2, 5, 10};
        int failures = 0;
        while (!closed) {
            try {
                String secret = token();
                try (Socket connection = new Socket()) {
                    socket = connection;
                    if (closed) return;
                    connection.connect(new InetSocketAddress("127.0.0.1", port), 2000);
                    connection.setSoTimeout(6000);
                    connection.setTcpNoDelay(true);
                    JsonObject hello = new JsonObject();
                    hello.addProperty("protocol_version", 1);
                    hello.addProperty("type", "diagnostic_hello");
                    hello.addProperty("token", secret);
                    connection.getOutputStream().write((hello + "\n").getBytes(StandardCharsets.UTF_8));
                    BufferedInputStream input = new BufferedInputStream(connection.getInputStream());
                    Snapshot previous = null;
                    long lastValid = System.nanoTime();
                    while (!closed) {
                        Snapshot next = parse(readFrame(input, connection, lastValid + 6_000_000_000L));
                        if (previous != null && next.sessionId().equals(previous.sessionId()) && next.sequence() <= previous.sequence()) {
                            if (System.nanoTime() - lastValid >= 6_000_000_000L) throw new IOException("Game updates stopped");
                            continue; // Repeated network output cannot refresh stale game state.
                        }
                        snapshot = previous = next;
                        lastValid = System.nanoTime();
                        failures = 0;
                        connectionStatus = "Connected (read-only diagnostic)";
                    }
                }
            } catch (IOException | RuntimeException failure) {
                snapshot = null; // No stale campaign survives disconnect or invalid data.
                connectionStatus = "Rogue Trader unavailable; start its bridge diagnostic";
            } finally { socket = null; }
            if (!closed) {
                try { Thread.sleep(delays[Math.min(failures++, delays.length - 1)] * 1000L); }
                catch (InterruptedException interrupted) { if (closed) return; }
            }
        }
    }

    @Override public void close() {
        closed = true;
        snapshot = null;
        Socket current = socket;
        if (current != null) try { current.close(); } catch (IOException ignored) { }
        if (worker != null) worker.interrupt();
    }
}
