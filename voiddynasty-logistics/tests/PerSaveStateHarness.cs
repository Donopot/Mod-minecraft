using System;
using System.Collections;
using System.IO;
using VoidDynasty.RogueTrader;

internal static class PerSaveStateHarness {
    public static void Main(string[] args) {
        try { Run(args); }
        catch (Exception failure) { Console.Error.WriteLine(failure.GetType().Name); Environment.ExitCode = 1; }
    }
    private static void Run(string[] args) {
        if (args[0] == "read") { Console.WriteLine(PerSaveState.ReadArchive(args[1]) ?? "<MISSING>"); return; }
        var state = new BridgeSaveState { bridge_campaign_id = "00000000-0000-4000-8000-000000000001",
            native_campaign_id = "TEST-CAMPAIGN", probe_receipt = "00000000-0000-4000-8000-000000000002",
            persistence_verified = false, logistics_reserve = 0 };
        if (args[0] == "emit") { Console.WriteLine(DiagnosticJson.Serialize(state)); return; }
        IDictionary settings = new Hashtable(); settings["native_setting"] = 42;
        PerSaveState.Put(settings, state);
        var restored = PerSaveState.Get(settings);
        if (restored.bridge_campaign_id != state.bridge_campaign_id || restored.probe_receipt != state.probe_receipt
                || restored.logistics_reserve != 0 || restored.persistence_verified || (int)settings["native_setting"] != 42 || settings.Count != 2)
            throw new Exception("State round trip changed data");
        state.logistics_reserve = -1;
        bool rejected = false;
        try { PerSaveState.Decode(DiagnosticJson.Serialize(state)); } catch (InvalidDataException) { rejected = true; }
        if (!rejected) throw new Exception("Invalid reserve accepted");
        Console.WriteLine("PASS");
    }
}
