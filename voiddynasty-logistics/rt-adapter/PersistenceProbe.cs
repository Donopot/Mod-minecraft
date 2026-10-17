using System;
using System.Collections;
using System.IO;
using System.Reflection;
using System.Threading.Tasks;
using UnityEngine;

namespace VoidDynasty.RogueTrader {
    // User-triggered proof. Only named NEW manual saves and a namespaced settings
    // entry are created. No native economy, inventory or quest field is mutated.
    public sealed class PersistenceProbe : MonoBehaviour {
        public string Status { get; private set; }
        public bool Busy { get; private set; }
        public bool Passed { get; private set; }
        private Action<string> log;
        private object game;
        private object manager;
        private string nativeCampaign;
        private BridgeSaveState expected;

        public void Configure(Action<string> logger) { log = logger; Status = "Persistance non verifiee"; }

        internal static object Read(object target, string name) {
            if (target == null) return null;
            var type = target as Type ?? target.GetType();
            const BindingFlags flags = BindingFlags.Public | BindingFlags.Instance | BindingFlags.Static;
            var property = type.GetProperty(name, flags);
            if (property != null) return property.GetValue(target is Type ? null : target, null);
            var field = type.GetField(name, flags);
            return field == null ? null : field.GetValue(target is Type ? null : target);
        }
        private static object Invoke(object target, string name, params object[] arguments) {
            var method = target.GetType().GetMethod(name, BindingFlags.Public | BindingFlags.Instance);
            if (method == null || method.GetParameters().Length != arguments.Length) throw new MissingMethodException(name);
            return method.Invoke(target, arguments);
        }
        private IDictionary Settings() {
            return Read(Read(Read(game, "State"), "InGameSettings"), "List") as IDictionary;
        }
        private bool Allowed() {
            return game != null && String.Equals(Convert.ToString(Read(game, "CurrentMode")), "Default", StringComparison.Ordinal)
                && !Equals(Read(game, "IsInMainMenu"), true) && !Equals(Read(game, "IsUnloading"), true)
                && !Equals(Read(Read(game, "Player"), "IsInCombat"), true)
                && String.Equals(Convert.ToString(Read(manager, "CurrentState")), "None", StringComparison.Ordinal)
                && !Equals(Read(manager, "CommitInProgress"), true);
        }
        public void Begin(object currentGame) {
            if (Busy) return;
            game = currentGame;
            manager = Read(game, "SaveManager");
            try {
                if (!Allowed()) throw new InvalidOperationException("Charger une partie hors combat et dialogue");
                if (PerSaveState.Get(Settings()) != null) throw new InvalidOperationException("Etat du pont deja present; charger une copie sans test");
                nativeCampaign = Convert.ToString(Read(Read(game, "Player"), "GameId"));
                if (String.IsNullOrEmpty(nativeCampaign)) throw new InvalidOperationException("Campagne inconnue");
                expected = new BridgeSaveState { bridge_campaign_id = Guid.NewGuid().ToString(), native_campaign_id = nativeCampaign,
                    probe_receipt = Guid.NewGuid().ToString(), logistics_reserve = 0, persistence_verified = false };
                Busy = true; Passed = false;
                StartCoroutine(Guard(Run()));
            } catch (Exception failure) { Fail(failure); }
        }
        private void UpdateStatus(string value) { Status = value; if (log != null) log("PERSISTENCE_PROBE " + value); }
        private void Fail(Exception failure) {
            while (failure is TargetInvocationException && failure.InnerException != null) failure = failure.InnerException;
            Busy = false; Passed = false;
            UpdateStatus("ECHEC " + failure.GetType().Name + ": " + failure.Message);
        }
        private IEnumerator Guard(IEnumerator routine) {
            var stack = new System.Collections.Generic.Stack<IEnumerator>(); stack.Push(routine);
            while (stack.Count > 0) {
                object current = null; bool moved = false; Exception failure = null;
                try { moved = stack.Peek().MoveNext(); if (moved) current = stack.Peek().Current; }
                catch (Exception error) { failure = error; }
                if (failure != null) { Fail(failure); yield break; }
                if (!moved) { var disposable = stack.Pop() as IDisposable; if (disposable != null) disposable.Dispose(); continue; }
                var nested = current as IEnumerator;
                if (nested != null) stack.Push(nested); else yield return current;
            }
        }
        private void RequireCampaign() {
            if (!String.Equals(nativeCampaign, Convert.ToString(Read(Read(game, "Player"), "GameId")), StringComparison.Ordinal))
                throw new InvalidOperationException("Campagne changee pendant le test");
        }
        private string PathFor(object save) {
            string root = Path.GetFullPath(Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile),
                "AppData", "LocalLow", "Owlcat Games", "Warhammer 40000 Rogue Trader", "Saved Games"));
            string name = Convert.ToString(Read(save, "FileName"));
            if (String.IsNullOrEmpty(name) || Path.GetFileName(name) != name) throw new InvalidDataException("Nom de sauvegarde invalide");
            return Path.Combine(root, name);
        }
        private IEnumerator Save(object save, string expectedJson) {
            yield return new GameSaveOperations(game, EngineReady).Save(save);
            if (!String.Equals(Convert.ToString(Read(manager, "CurrentState")), "None", StringComparison.Ordinal)
                    || Equals(Read(manager, "CommitInProgress"), true)) throw new InvalidOperationException("Sauvegarde non terminee");
            string path = PathFor(save);
            var check = Task.Factory.StartNew(() => PerSaveState.ReadArchive(path));
            while (!check.IsCompleted) yield return null;
            if (check.IsFaulted || check.IsCanceled) throw new IOException("Lecture de la sauvegarde de test impossible");
            if (check.Result != expectedJson) throw new InvalidDataException("Le recu ecrit dans la sauvegarde ne correspond pas");
            RequireCampaign();
        }
        private IEnumerator Load(object save, string expectedJson) {
            object previousPlayer = Read(game, "Player");
            object previousSettings = Settings();
            yield return new GameSaveOperations(game, EngineReady).Load(save);
            RequireCampaign();
            if (System.Object.ReferenceEquals(previousPlayer, Read(game, "Player")) || System.Object.ReferenceEquals(previousSettings, Settings()))
                throw new InvalidOperationException("Le moteur n'a pas recharge les objets de sauvegarde");
            string actual = Settings().Contains(PerSaveState.Key) ? Settings()[PerSaveState.Key] as string : null;
            if (actual != expectedJson) throw new InvalidDataException("Isolation ou rechargement du recu en echec");
        }
        private bool EngineReady() {
            game = Read(game.GetType(), "Instance") ?? game;
            manager = Read(game, "SaveManager");
            var loadingType = game.GetType().Assembly.GetType("Kingmaker.EntitySystem.Persistence.LoadingProcess", true);
            var loading = Read(loadingType, "Instance");
            return Equals(Read(loading, "IsLoadingInProcess"), false) && Allowed();
        }
        private IEnumerator Run() {
            UpdateStatus("1/6 Creation d'une copie de securite");
            object baseline = Invoke(manager, "CreateNewSave", "VOID DYNASTY - AVANT TEST " + DateTime.Now.ToString("HHmmss"), false);
            yield return Save(baseline, null);
            UpdateStatus("2/6 Ecriture du recu de test (reserve 0)");
            RequireCampaign(); PerSaveState.Put(Settings(), expected);
            string first = DiagnosticJson.Serialize(expected);
            object test = Invoke(manager, "CreateNewSave", "VOID DYNASTY - TEST PERSISTANCE", false);
            yield return Save(test, first);
            UpdateStatus("3/6 Verification de l'isolation: copie avant test");
            yield return Load(baseline, null);
            UpdateStatus("4/6 Rechargement du recu depuis la copie de test");
            yield return Load(test, first);
            UpdateStatus("5/6 Sauvegarde de la preuve validee");
            expected.persistence_verified = true; PerSaveState.Put(Settings(), expected);
            string verified = DiagnosticJson.Serialize(expected);
            object ready = Invoke(manager, "CreateNewSave", "VOID DYNASTY - PERSISTANCE VALIDEE", false);
            yield return Save(ready, verified);
            UpdateStatus("6/6 Rechargement final de la preuve");
            yield return Load(ready, verified);
            Passed = true; Busy = false;
            UpdateStatus("PASS - recu persistant et sauvegardes isolees; reserve 0");
        }
    }
}
