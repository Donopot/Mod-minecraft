using System;
using VoidDynasty.RogueTrader;

internal static class DiagnosticServerHarness {
    public static void Main(string[] args) {
        using (var server = new DiagnosticServer(args[0], int.Parse(args[1]))) {
            var first = Console.ReadLine();
            server.Publish(first == "SERIALIZE" ? DiagnosticJson.Serialize(new DiagnosticSnapshot {
                session_id = "00000000-0000-4000-8000-000000000001", sequence = 1, campaign_loaded = true,
                native_campaign_id = "TEST-CAMPAIGN", zone = "TEST-ZONE", game_mode = "TEST", save_state = "TEST", status = "TEST"
            }) : first);
            server.Start();
            Console.WriteLine("READY");
            Console.Out.Flush();
            string line;
            while ((line = Console.ReadLine()) != null && line != "STOP") server.Publish(line);
        }
    }
}
