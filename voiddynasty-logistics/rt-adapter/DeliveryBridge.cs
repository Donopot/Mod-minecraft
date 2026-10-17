using System;
using System.Collections;
using System.Collections.Generic;
using System.IO;
using System.Reflection;
using System.Threading.Tasks;
using UnityEngine;

namespace VoidDynasty.RogueTrader {
    public sealed class DeliveryBridge : MonoBehaviour {
        private DeliveryServer server;
        private object game, lastPlayer;
        private string session="";
        private bool busy, failed, writing;
        private Func<bool> probeRunning;
        private DeliveryJob active;
        private Dictionary<string,string> pending;
        private Action<string> log;
        public string Status {get;private set;}
        public bool Busy {get{return busy;}}
        private static object Read(object target,string name) { return PersistenceProbe.Read(target,name); }
        private IDictionary Settings() { return Read(Read(Read(game,"State"),"InGameSettings"),"List") as IDictionary; }
        private BridgeSaveState State() { return PerSaveState.Get(Settings()); }
        public void Configure(string token,Action<string> logger,Func<bool> probeRunning) { this.probeRunning=probeRunning;log=logger;server=new DeliveryServer(token);server.Start();Status="Pret; charger PERSISTANCE VALIDEE"; }
        public void Stop() { if(server!=null)server.Dispose(); }
        public void Observe(object current,string currentSession) {
            game=current;session=currentSession;
            var player=Read(game,"Player");
            if(!System.Object.ReferenceEquals(lastPlayer,player)) { lastPlayer=player;pending=null;failed=false; }
            DeliveryJob next;
            if(!busy && server!=null && server.Take(out next)) { busy=true;active=next;StartCoroutine(Guard(Handle(next.Message))); }
        }
        private bool EngineReady() {
            var manager=Read(game,"SaveManager");
            var loadingType=game.GetType().Assembly.GetType("Kingmaker.EntitySystem.Persistence.LoadingProcess",true);
            return Equals(Read(Read(loadingType,"Instance"),"IsLoadingInProcess"),false)
                && Convert.ToString(Read(manager,"CurrentState"))=="None" && !Equals(Read(manager,"CommitInProgress"),true);
        }
        private bool SafeNow() {
            return DeliveryAvailability.CanMutate(ReadableNow(),Convert.ToString(Read(game,"CurrentMode")),
                Equals(Read(Read(game,"Player"),"IsInCombat"),true),failed);
        }
        private bool ReadableNow() {
            if(game==null)return false;
            return DeliveryAvailability.CanRead(EngineReady(),!Equals(Read(game,"IsInMainMenu"),true)
                && !Equals(Read(game,"IsUnloading"),true) && Read(game,"CurrentlyLoadedArea")!=null && Settings()!=null,
                System.Object.ReferenceEquals(lastPlayer,Read(game,"Player")),probeRunning!=null&&probeRunning());
        }
        private IEnumerator Save(BridgeSaveState value,NativeCargoGrant grant=null) {
            string identity=session;
            writing=true;
            PerSaveState.Put(Settings(),value);
            var manager=Read(game,"SaveManager");
            var save=manager.GetType().GetMethod("CreateNewSave").Invoke(manager,new object[]{"VOID DYNASTY - "+(value.delivery_state??"LIEN")+" "+DateTime.Now.ToString("HHmmss"),false});
            yield return new GameSaveOperations(game,EngineReady).Save(save);
            DeliveryLedger.Require(identity==session,"STALE_SESSION");
            DeliveryLedger.Require(value.native_campaign_id==Convert.ToString(Read(Read(game,"Player"),"GameId")),"RECOVERY_REQUIRED");
            var filename=Convert.ToString(Read(save,"FileName"));
            if(Path.GetFileName(filename)!=filename)throw new IOException("Invalid save path");
            string path=Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.UserProfile),"AppData","LocalLow","Owlcat Games","Warhammer 40000 Rogue Trader","Saved Games",filename);
            var check=Task.Factory.StartNew(()=>{
                if(grant!=null&&!NativeCargoArchive.Contains(path,grant.Id,grant.Blueprint))throw new IOException("NATIVE_CARGO_SAVE_INVALID");
                return PerSaveState.ReadArchive(path);
            });while(!check.IsCompleted)yield return null;
            if(check.IsFaulted || check.IsCanceled || check.Result!=DiagnosticJson.Serialize(value))throw new IOException("PERSISTENCE_FAILED");
            Status="Sauvegarde confirmee: "+filename;log("DELIVERY_SAVE "+(value.delivery_state??"PAIRED")+" reserve="+value.logistics_reserve);
            writing=false;
        }
        private DeliveryReply Reply(Dictionary<string,string> m,string code) {
            var r=new DeliveryReply {reply_to=m["message_id"],code=code,session_id=session,available=SafeNow()&&!failed,
                zone=Convert.ToString(Read(Read(game,"CurrentlyLoadedArea"),"AreaDisplayName")),campaign_name=Convert.ToString(Read(Read(game,"Player"),"GameId"))};
            var s=State();if(s!=null) {try{s=DeliveryLedger.Normalize(s);r.completed_count=s.receipts.Count;r.last_receipt_id=DeliveryLedger.Last(s);r.native_count=DeliveryLedger.NativeCount(s);}catch(System.IO.InvalidDataException){}
                r.mission_id=s.mission_id??CargoCatalog.Default;
                r.campaign_id=s.bridge_campaign_id;r.world_id=s.world_id??"";r.pair_id=s.pair_id??"";r.checkpoint_id=s.checkpoint_id??"";
                r.delivery_id=s.delivery_id??"";r.state=s.delivery_state??"IDLE";r.payload_hash=s.payload_hash??"";r.reserve=s.logistics_reserve;}
            return r;
        }
        private IEnumerator Handle(Dictionary<string,string> m) {
            bool readOnly=m["type"]=="inspect" || m["type"]=="status";
            DeliveryLedger.Require((readOnly?ReadableNow():SafeNow()) && !failed,"UNAVAILABLE");
            var s=DeliveryLedger.Normalize(State());
            DeliveryLedger.Require(s.native_campaign_id==Convert.ToString(Read(Read(game,"Player"),"GameId")),"RECOVERY_REQUIRED");
            DeliveryLedger.Require(m["mission_hash"]==DeliveryLedger.MissionHash,"MISSION_MISMATCH");
            if(m["type"]=="inspect") {
                if(!String.IsNullOrEmpty(m["campaign_id"])) DeliveryLedger.Require(m["campaign_id"]==s.bridge_campaign_id,"RECOVERY_REQUIRED");
                if(!String.IsNullOrEmpty(m["pair_id"]))DeliveryLedger.Reconcile(s,m);
            } else {
                DeliveryLedger.Require(m["session_id"]==session && m["campaign_id"]==s.bridge_campaign_id,"STALE_SESSION");
                if(m["type"]=="propose_pair") {
                    if(String.IsNullOrEmpty(s.pair_id)) { DeliveryLedger.Pair(s,m);pending=new Dictionary<string,string>(m);Status="Appairage demande par monde "+m["world_id"]; }
                    else DeliveryLedger.Identity(s,m);
                } else {
                    DeliveryLedger.Reconcile(s,m);
                    if(m["type"]=="prepare" || m["type"]=="commit") {
                        if(m["type"]=="prepare")NativeCargo.CheckAvailable(game,CargoCatalog.Get(m["mission_id"]));
                        if(m["type"]=="commit"&&s.delivery_state=="PREPARED"){
                            var intent=DeliveryLedger.MarkPending(s,m);yield return Save(intent);
                            DeliveryLedger.Require(SafeNow(),"UNAVAILABLE");
                            writing=true;var grant=NativeCargo.Create(game,CargoCatalog.Get(m["mission_id"]));
                            var committed=DeliveryLedger.CompletePending(intent,m,grant.Id,grant.Blueprint);
                            yield return Save(committed,grant);
                        }else{
                            var changed=DeliveryLedger.Change(s,m);
                            if(DiagnosticJson.Serialize(changed)!=DiagnosticJson.Serialize(s))yield return Save(changed);
                        }
                    } else DeliveryLedger.Require(m["type"]=="status","UNKNOWN_MESSAGE");
                }
            }
            string code=pending!=null?"PAIR_APPROVAL_REQUIRED":"OK";
            if(code=="OK"&&!SafeNow())code=Equals(Read(Read(game,"Player"),"IsInCombat"),true)?"PAUSED_COMBAT":"PAUSED_MODE";
            active.Reply.TrySetResult(DiagnosticJson.Serialize(Reply(m,code)));
            active=null;busy=false;
        }
        private IEnumerator Approve(Dictionary<string,string> m) {
            DeliveryLedger.Require(SafeNow() && m["session_id"]==session,"STALE_SESSION");
            var changed=DeliveryLedger.Pair(State(),m);pending=null;yield return Save(changed);busy=false;
        }
        private IEnumerator Guard(IEnumerator routine) {
            var stack=new Stack<IEnumerator>();stack.Push(routine);
            while(stack.Count>0) {
                object next=null;bool moved=false;Exception error=null;
                try {moved=stack.Peek().MoveNext();if(moved)next=stack.Peek().Current;}catch(Exception e){error=e;}
                if(error!=null) {
                    while(error is TargetInvocationException && error.InnerException!=null)error=error.InnerException;
                    var code=error is InvalidDataException ? error.Message : "PERSISTENCE_FAILED";
                    if(writing || !(error is InvalidDataException))failed=true;
                    writing=false;
                    if(Status!=code)log("DELIVERY_ERROR "+code);
                    Status=code;
                    if(active!=null) {try{active.Reply.TrySetResult(DiagnosticJson.Serialize(Reply(active.Message,code)));}catch{active.Reply.TrySetException(new IOException(code));}}
                    active=null;busy=false;yield break;
                }
                if(!moved){stack.Pop();continue;}var nested=next as IEnumerator;if(nested!=null)stack.Push(nested);else yield return next;
            }
        }
        public void Draw() {
            GUILayout.Label("Missions de ravitaillement : une cargaison native complete par envoi");
            if(busy)GUILayout.Label("Operation en cours; attendre la confirmation de sauvegarde.");
            try {var raw=State();var s=raw==null?null:DeliveryLedger.Normalize(raw);GUILayout.Label("Reserve: "+(s==null?"indisponible":s.logistics_reserve.ToString()));
                if(s!=null)GUILayout.Label("Livraisons confirmees : "+s.receipts.Count);
                if(s!=null)GUILayout.Label("Cargaisons natives livrees : "+DeliveryLedger.NativeCount(s));
                if(s!=null) {GUILayout.Label("Monde lie: "+(s.world_id??"aucun"));GUILayout.Label("Livraison: "+(s.delivery_id??"aucune")+" | "+(s.delivery_state??"IDLE"));}}
            catch {GUILayout.Label("Etat de sauvegarde indisponible");}
            GUILayout.Label(Status??"");
            if(pending!=null && !busy) {GUILayout.Label("Monde demandeur: "+pending["world_id"]);
                if(GUILayout.Button("Lier cette campagne au monde affiche")) {busy=true;StartCoroutine(Guard(Approve(pending)));}}
        }
    }
}
