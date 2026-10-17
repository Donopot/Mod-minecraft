using System;
using System.Linq;
using System.Reflection;
using UnityEngine;
using UnityModManagerNet;

namespace VoidDynasty.RogueTrader {
    // P1 diagnostic only. Every read is made from UMM's Unity-thread callbacks.
    // Only a read-only diagnostic stream is enabled before save persistence is proved.
    public static class DiagnosticMod {
        private static string status = "Waiting for first game update";
        private static string campaign = "-";
        private static string zone = "-";
        private static string mode = "-";
        private static string saveState = "-";
        private static string storage = "Not inspected";
        private static float elapsed = 1f;
        private static Type gameType;
        private static DiagnosticServer server;
        private static object lastPlayer;
        private static string session = Guid.NewGuid().ToString();
        private static bool campaignLoaded;
        private static long sequence;
        private static PersistenceProbe probe;
        private static GameObject probeObject;
        private static DeliveryBridge delivery;
        private static bool showProbe;
        private static bool showSetup=true;

        public static bool Load(UnityModManager.ModEntry entry) {
            entry.OnGUI = Draw;
            entry.OnUpdate = Update;
            probeObject = new GameObject("VoidDynastyPersistenceProbe");
            UnityEngine.Object.DontDestroyOnLoad(probeObject);
            probe = probeObject.AddComponent<PersistenceProbe>();
            probe.Configure(message => entry.Logger.Log(message));
            delivery = probeObject.AddComponent<DeliveryBridge>();
            entry.OnUnload = delegate {
                if ((probe != null && probe.Busy) || (delivery != null && delivery.Busy)) return false;
                if (server != null) server.Dispose();
                if (delivery != null) delivery.Stop();
                if (probeObject != null) UnityEngine.Object.Destroy(probeObject);
                return true;
            };
            try {
                string token = DiagnosticServer.LoadOrCreateToken();
                server = new DiagnosticServer(token);
                server.Start();
                delivery.Configure(token, message => entry.Logger.Log(message), () => probe != null && probe.Busy);
            } catch (Exception ex) {
                status = "Diagnostic server unavailable: " + ex.GetType().Name;
                entry.Logger.Log(status);
                server = null;
            }
            entry.Logger.Log("VOID DYNASTY 0.7.0 delivery build loaded; requires verified per-save state and explicit world pairing.");
            return true;
        }

        private static object Read(object target, string name) {
            if (target == null) return null;
            Type type = target as Type ?? target.GetType();
            object instance = target is Type ? null : target;
            const BindingFlags flags = BindingFlags.Public | BindingFlags.Instance | BindingFlags.Static;
            var property = type.GetProperty(name, flags);
            if (property != null) return property.GetValue(instance, null);
            var field = type.GetField(name, flags);
            return field == null ? null : field.GetValue(instance);
        }

        private static string Text(object value) { return value == null ? "-" : value.ToString(); }

        private static void Update(UnityModManager.ModEntry entry, float delta) {
            elapsed += delta;
            if (elapsed < 1f) return;
            elapsed = 0f;
            // Clear each snapshot before reading: never retain a previous campaign on failure.
            campaign = zone = mode = saveState = "-";
            campaignLoaded = false;
            try {
                if (gameType == null) {
                    gameType = AppDomain.CurrentDomain.GetAssemblies()
                        .Select(a => a.GetType("Kingmaker.Game", false)).FirstOrDefault(t => t != null);
                }
                if (gameType == null) { status = "Game API not loaded"; return; }
                var storageType = gameType.Assembly.GetType("Kingmaker.EntitySystem.EntityPartKeyValueStorage", false);
                storage = "Save-scoped InGameSettings.List / settings.json; " + (probe == null ? "not initialized" : probe.Status);
                if (!Equals(Read(gameType, "HasInstance"), true)) { status = "No game instance"; return; }
                var game = Read(gameType, "Instance");
                mode = Text(Read(game, "CurrentMode"));
                if (Equals(Read(game, "IsInMainMenu"), true) || Equals(Read(game, "IsUnloading"), true)) {
                    status = "Main menu or unloading; delivery unavailable";
                    return;
                }
                var player = Read(game, "Player");
                var area = Read(game, "CurrentlyLoadedArea");
                if (player == null || area == null) { status = "No campaign area loaded"; return; }
                if (!System.Object.ReferenceEquals(lastPlayer, player)) { session = Guid.NewGuid().ToString(); lastPlayer = player; }
                campaign = Text(Read(player, "GameId"));
                zone = Text(Read(area, "AreaDisplayName"));
                saveState = Text(Read(Read(game, "SaveManager"), "CurrentState"));
                campaignLoaded = true;
                var bridgeState = PerSaveState.Get(Read(Read(Read(game, "State"), "InGameSettings"), "List") as System.Collections.IDictionary);
                status = "Campaign detected; " + (probe.Busy ? probe.Status : bridgeState != null && bridgeState.persistence_verified ? "persistance validee" : "charger PERSISTANCE VALIDEE ou REWARD_COMMITTED");
                storage = "Save-scoped InGameSettings.List / settings.json; " + (bridgeState != null && bridgeState.persistence_verified ? "validee" : "non verifiee");
            } catch (Exception ex) {
                campaign = zone = saveState = "-";
                status = "API unavailable: " + ex.GetType().Name;
            } finally {
                if (!campaignLoaded && lastPlayer != null) { lastPlayer = null; session = Guid.NewGuid().ToString(); }
                if (server != null) server.Publish(DiagnosticJson.Serialize(new DiagnosticSnapshot {
                    session_id = session, sequence = ++sequence, campaign_loaded = campaignLoaded,
                    native_campaign_id = campaignLoaded ? campaign : null, zone = campaignLoaded ? zone : null,
                    game_mode = mode, save_state = saveState, status = status
                }));
                if (delivery != null && gameType != null) {
                    var current = Read(gameType, "Instance");
                    delivery.Observe(current, session);
                }
            }
        }

        private static void Draw(UnityModManager.ModEntry entry) {
            GUILayout.Label("VOID DYNASTY | Rogue Trader bridge | 0.7.0 livraison", new GUILayoutOption[0]);
            GUILayout.Label(status, new GUILayoutOption[0]);
            GUILayout.Label("Native campaign GameId (diagnostic): " + campaign, new GUILayoutOption[0]);
            GUILayout.Label("Zone: " + zone, new GUILayoutOption[0]);
            GUILayout.Label("Game mode: " + mode, new GUILayoutOption[0]);
            GUILayout.Label("Save manager: " + saveState, new GUILayoutOption[0]);
            GUILayout.Label("Persistence: " + storage, new GUILayoutOption[0]);
            GUILayout.Label("Read-only link: " + (server == null ? "Unavailable" : server.Status), new GUILayoutOption[0]);
            if (delivery != null) delivery.Draw();
            showSetup=GUILayout.Toggle(showSetup,"Assistant de premiere utilisation",new GUILayoutOption[0]);
            if(showSetup) {
                object currentGame=null;BridgeSaveState currentState=null;
                try {
                    currentGame=gameType==null||!Equals(Read(gameType,"HasInstance"),true)?null:Read(gameType,"Instance");
                    currentState=PerSaveState.Get(Read(Read(Read(currentGame,"State"),"InGameSettings"),"List") as System.Collections.IDictionary);
                } catch(Exception) { GUILayout.Label("Etat de campagne indisponible ; charger une partie stable avant la preparation.",new GUILayoutOption[0]);return; }
                if(currentState!=null) {
                    GUILayout.Label("Etat du pont deja present. Ne pas refaire la preparation : garder ce monde et cette sauvegarde ensemble.",new GUILayoutOption[0]);
                    GUILayout.Label("Dans Minecraft : /voidbridge assistant. Demander le lien puis l'approuver ici, ou ouvrir le terminal si le monde est deja lie.",new GUILayoutOption[0]);
                } else {
                    GUILayout.Label("1. Charger une campagne hors combat. 2. Preparer cette campagne : des copies seront creees puis rechargees. 3. Attendre PASS avant l'association Minecraft.",new GUILayoutOption[0]);
                    bool enabled=GUI.enabled;
                    GUI.enabled=enabled&&campaignLoaded&&probe!=null&&!probe.Busy&&(delivery==null||!delivery.Busy)&&mode=="Default";
                    if(GUILayout.Button("Preparer cette campagne (copies et rechargements)",new GUILayoutOption[0]))probe.Begin(currentGame);
                    GUI.enabled=enabled;
                }
                string preparation=probe==null?"Preparation indisponible":probe.Status;
                if(currentState!=null&&(probe==null||!probe.Busy))preparation=currentState.persistence_verified?"Preparation deja validee dans cette sauvegarde.":"Etat du pont present ; conserver les copies et reprendre depuis un checkpoint correspondant.";
                GUILayout.Label(preparation,new GUILayoutOption[0]);
            }
            showProbe=GUILayout.Toggle(showProbe,"Diagnostic avance : test de persistance",new GUILayoutOption[0]);
            if(!showProbe)return;
            GUILayout.Label("A utiliser seulement sur une campagne sans etat du pont. Une livraison recue ne necessite aucun nouveau test.", new GUILayoutOption[0]);
            bool previousEnabled = GUI.enabled;
            GUI.enabled = previousEnabled && probe != null && !probe.Busy && campaignLoaded && (delivery == null || !delivery.Busy);
            if (GUILayout.Button("Tester persistance sur copies (rechargements)", new GUILayoutOption[0]))
                probe.Begin(Read(gameType, "Instance"));
            GUI.enabled = previousEnabled;
        }
    }
}
