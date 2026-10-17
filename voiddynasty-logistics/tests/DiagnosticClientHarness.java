package com.donopot.voiddynastybridge;

import java.nio.file.Path;

/** Exercise the actual worker and parser without booting Minecraft. */
public final class DiagnosticClientHarness {
    public static void main(String[] args) throws Exception {
        try (DiagnosticConnection connection = new DiagnosticConnection(Path.of(args[0]), Integer.parseInt(args[1]))) {
            connection.start();
            if (args.length > 2 && "without-rt".equals(args[2])) {
                long deadline = System.nanoTime() + 4_000_000_000L;
                while (!connection.status().contains("unavailable") && System.nanoTime() < deadline) Thread.sleep(25);
                if (connection.snapshot() != null || !connection.status().contains("unavailable"))
                    throw new AssertionError("Missing Rogue Trader must leave the client available with no campaign");
                System.out.println("PASS WITHOUT RT: missing bridge configuration handled by background worker");
                return;
            }
            long deadline = System.nanoTime() + 12_000_000_000L;
            boolean observed = false;
            while (System.nanoTime() < deadline) {
                DiagnosticConnection.Snapshot state = connection.snapshot();
                if (state != null && !observed) {
                    if (!state.campaignLoaded() || !"TEST-CAMPAIGN".equals(state.campaignId()) || !"TEST-ZONE".equals(state.zone()))
                        throw new AssertionError("Unexpected diagnostic state");
                    System.out.println("LIVE " + state.campaignId() + " " + state.zone());
                    observed = true;
                    if (args.length < 3) return;
                }
                if (observed && state == null) { System.out.println("CLEARED"); return; }
                if (observed && state != null && !state.campaignLoaded() && args.length > 2 && "await-menu".equals(args[2])) {
                    if (state.campaignId() != null || state.zone() != null) throw new AssertionError("Stale campaign survived menu");
                    System.out.println("CLEARED_CAMPAIGN");
                    return;
                }
                Thread.sleep(25);
            }
            throw new AssertionError("Expected live state then expiry/disconnect was not observed");
        }
    }
}
