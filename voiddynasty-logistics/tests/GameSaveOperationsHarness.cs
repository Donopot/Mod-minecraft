using System;
using System.Collections;
using VoidDynasty.RogueTrader;

public sealed class FakeNativeGame {
    public string LastMethod;
    public Action Complete;
    public void SaveGame(object save, Action completed) { LastMethod = "SaveGame"; Complete = completed; }
    public void LoadGame(object save, Action completed) { LastMethod = "LoadGame"; Complete = completed; }
    public IEnumerator SaveRoutine(object save, bool force, bool show) { throw new Exception("Internal routine must not be used"); }
    public IEnumerator LoadRoutine(object save, bool flag) { throw new Exception("Internal routine must not be used"); }
}
internal static class GameSaveOperationsHarness {
    public static void Main() {
        try {
            foreach (bool load in new[] { false, true }) {
                bool ready = false;
                var game = new FakeNativeGame();
                var operations = new GameSaveOperations(game, () => ready);
                var routine = load ? operations.Load(new object()) : operations.Save(new object());
                if (!routine.MoveNext() || game.LastMethod != (load ? "LoadGame" : "SaveGame")) throw new Exception("Wrong entry point");
                ready = true;
                if (!routine.MoveNext()) throw new Exception("Completed without callback");
                ready = false; game.Complete();
                if (!routine.MoveNext()) throw new Exception("Completed while game still loading");
                ready = true;
                if (routine.MoveNext()) throw new Exception("Did not complete after callback and stable game");
            }
            Console.WriteLine("PASS");
        } catch (Exception failure) { Console.Error.WriteLine(failure.Message); Environment.ExitCode = 1; }
    }
}
