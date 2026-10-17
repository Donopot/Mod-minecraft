using System;
using System.Collections;
using System.Diagnostics;
using System.Reflection;

namespace VoidDynasty.RogueTrader {
    // Use the same high-level entry points as the game's UI. The internal
    // SaveManager routines alone do not orchestrate unloading/loading an area.
    public sealed class GameSaveOperations {
        private readonly object game;
        private readonly Func<bool> ready;
        public GameSaveOperations(object game, Func<bool> ready) { this.game = game; this.ready = ready; }
        public IEnumerator Save(object info) { return Execute("SaveGame", info); }
        public IEnumerator Load(object info) { return Execute("LoadGame", info); }
        private IEnumerator Execute(string methodName, object info) {
            bool complete = false;
            var method = game.GetType().GetMethod(methodName, BindingFlags.Public | BindingFlags.Instance);
            if (method == null || method.GetParameters().Length != 2) throw new MissingMethodException(methodName);
            var timer = Stopwatch.StartNew();
            method.Invoke(game, new object[] { info, (Action)(() => complete = true) });
            do {
                if (timer.Elapsed.TotalSeconds > 120) throw new TimeoutException("Operation native non terminee: " + methodName);
                yield return null;
            } while (!complete || !ready());
        }
    }
}
