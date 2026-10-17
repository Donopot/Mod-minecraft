using System;
using System.IO;
using System.Collections.Generic;
using System.Security.Cryptography;
using System.Text;

namespace VoidDynasty.RogueTrader {
    // Append-only bounded receipts. A returned change must be saved by Unity
    // before acknowledgment. No old receipt is evicted or rewarded again.
    public static class DeliveryLedger {
        public const string MissionHash=CargoCatalog.Hash;
        public const int MaxReceipts=256;
        public static bool Uuid(string value){Guid parsed;return Guid.TryParseExact(value,"D",out parsed);}
        public static void Require(bool value,string code){if(!value)throw new InvalidDataException(code);}
        public static BridgeSaveState Copy(BridgeSaveState s){return PerSaveState.Decode(DiagnosticJson.Serialize(s));}
        private static bool Hash(string v){if(v==null||v.Length!=64)return false;foreach(char c in v)if(!((c>='0'&&c<='9')||(c>='a'&&c<='f')))return false;return true;}
        public static string Last(BridgeSaveState s){return s.receipts.Count==0?"":s.receipts[s.receipts.Count-1].delivery_id;}
        public static BridgeSaveState Normalize(BridgeSaveState current){
            Require(current!=null&&current.persistence_verified,"PERSISTENCE_NOT_VERIFIED");
            var s=Copy(current);
            if(s.ledger_version==0){
                Require(s.receipts==null||s.receipts.Count==0,"RECOVERY_REQUIRED");
                s.receipts=new List<DeliveryReceipt>();
                if(s.logistics_reserve==10){
                    Require(s.delivery_state=="REWARD_COMMITTED"&&Uuid(s.delivery_id)&&Hash(s.payload_hash),"RECOVERY_REQUIRED");
                    s.receipts.Add(new DeliveryReceipt{delivery_id=s.delivery_id,payload_hash=s.payload_hash,sequence=1,mission_id=CargoCatalog.Default,reward_kind="logistics"});
                }else Require(s.logistics_reserve==0&&String.IsNullOrEmpty(s.delivery_id),"RECOVERY_REQUIRED");
                s.ledger_version=2;
            }
            Require(s.ledger_version==2&&s.receipts!=null&&s.receipts.Count<=MaxReceipts,"RECOVERY_REQUIRED");
            var ids=new HashSet<string>();int legacyPoints=0;
            if(String.IsNullOrEmpty(s.mission_id))s.mission_id=CargoCatalog.Default;
            for(int i=0;i<s.receipts.Count;i++){
                var r=s.receipts[i];
                Require(r!=null,"RECOVERY_REQUIRED");
                if(String.IsNullOrEmpty(r.mission_id))r.mission_id=CargoCatalog.Default;
                if(String.IsNullOrEmpty(r.reward_kind))r.reward_kind="logistics";
                if(r.reward_kind=="logistics"){Require(r.mission_id==CargoCatalog.Default,"RECOVERY_REQUIRED");legacyPoints+=10;}
                else {Require(r.reward_kind=="native_cargo"&&EntityId(r.native_cargo_id)&&r.native_blueprint_id==CargoCatalog.Get(r.mission_id).Blueprint,"RECOVERY_REQUIRED");}
                Require(r!=null&&Uuid(r.delivery_id)&&Hash(r.payload_hash)&&r.sequence==i+1&&ids.Add(r.delivery_id),"RECOVERY_REQUIRED");
            }
            Require(s.logistics_reserve==legacyPoints,"RECOVERY_REQUIRED");
            if(String.IsNullOrEmpty(s.pair_id)){
                Require(s.receipts.Count==0&&String.IsNullOrEmpty(s.delivery_id),"RECOVERY_REQUIRED");return s;
            }
            Require(Uuid(s.pair_id)&&Uuid(s.world_id)&&Uuid(s.checkpoint_id),"RECOVERY_REQUIRED");
            if(String.IsNullOrEmpty(s.delivery_id))Require(s.receipts.Count==0,"RECOVERY_REQUIRED");
            else if(s.delivery_state=="PREPARED"||s.delivery_state=="REWARD_PENDING")Require(Uuid(s.delivery_id)&&Hash(s.payload_hash)&&!ids.Contains(s.delivery_id),"RECOVERY_REQUIRED");
            else Require(s.delivery_state=="REWARD_COMMITTED"&&s.delivery_id==Last(s)&&s.payload_hash==s.receipts[s.receipts.Count-1].payload_hash,"RECOVERY_REQUIRED");
            return s;
        }
        public static void Validate(BridgeSaveState s){Normalize(s);}
        public static BridgeSaveState Pair(BridgeSaveState current,Dictionary<string,string> m){
            var s=Normalize(current);Require(String.IsNullOrEmpty(s.pair_id),"ALREADY_PAIRED");
            Require(Uuid(m["world_id"])&&Uuid(m["pair_id"])&&Uuid(m["checkpoint_id"]),"INVALID_IDENTITY");
            s.world_id=m["world_id"];s.pair_id=m["pair_id"];s.checkpoint_id=m["checkpoint_id"];return s;
        }
        public static void Identity(BridgeSaveState s,Dictionary<string,string> m){
            Require(s.bridge_campaign_id==m["campaign_id"]&&s.world_id==m["world_id"]&&s.pair_id==m["pair_id"]&&s.checkpoint_id==m["checkpoint_id"],"RECOVERY_REQUIRED");
            var mission=CargoCatalog.Get(m["mission_id"]);
            Require(m["mission_hash"]==MissionHash&&m["item_id"]==mission.Item&&m["count"]==mission.Count.ToString(System.Globalization.CultureInfo.InvariantCulture),"MISSION_MISMATCH");
        }
        public static int Known(Dictionary<string,string> m){
            int n;Require(Int32.TryParse(m["completed_count"],out n)&&n>=0&&n<=MaxReceipts&&n.ToString(System.Globalization.CultureInfo.InvariantCulture)==m["completed_count"],"INVALID_STATE");return n;
        }
        public static bool EntityId(string id){Guid parsed;return id!=null&&(id.Length==32||id.Length==36)&&Guid.TryParse(id,out parsed);}
        public static int NativeCount(BridgeSaveState s){int count=0;foreach(var r in s.receipts)if(r.reward_kind=="native_cargo")count++;return count;}
        public static string Fingerprint(BridgeSaveState s,int completed,string previous){return Fingerprint(s,completed,previous,CargoCatalog.Default);}
        public static string Fingerprint(BridgeSaveState s,int completed,string previous,string missionId){
            var m=CargoCatalog.Get(missionId);
            string json="{\"campaign_id\":\""+s.bridge_campaign_id+"\",\"cargo\":[{\"count\":"+m.Count+",\"item_id\":\""+m.Item+"\"}],\"mission_id\":\""+m.Id+"\",\"mission_version\":3,\"native_cargo_blueprint\":\""+m.Blueprint+"\",\"pair_id\":\""+s.pair_id+"\",\"previous_delivery_id\":\""+previous+"\",\"sequence\":"+(completed+1).ToString(System.Globalization.CultureInfo.InvariantCulture)+",\"world_id\":\""+s.world_id+"\"}";
            using(var sha=SHA256.Create())return BitConverter.ToString(sha.ComputeHash(Encoding.UTF8.GetBytes(json))).Replace("-","").ToLowerInvariant();
        }
        public static void Reconcile(BridgeSaveState s,Dictionary<string,string> m){
            Identity(s,m);Require(s.delivery_state!="REWARD_PENDING","RECOVERY_REQUIRED");int known=Known(m);string local=m["known_state"];
            Require(known<=s.receipts.Count&&(known==0?m["last_receipt_id"]=="":m["last_receipt_id"]==s.receipts[known-1].delivery_id),"RECOVERY_REQUIRED");
            if(local=="SETTLED"){
                Require(known==s.receipts.Count&&known>0&&s.mission_id==m["mission_id"]&&s.delivery_state=="REWARD_COMMITTED"&&m["delivery_id"]==Last(s)&&m["payload_hash"]==s.receipts[known-1].payload_hash,"RECOVERY_REQUIRED");return;
            }
            if(local=="IDLE"){
                Require(known==0&&s.receipts.Count==0&&String.IsNullOrEmpty(s.delivery_id),"RECOVERY_REQUIRED");return;
            }
            Require(local=="DRAFT"||local=="RESERVED","INVALID_STATE");
            Require(Uuid(m["delivery_id"])&&m["payload_hash"]==Fingerprint(s,known,m["last_receipt_id"],m["mission_id"]),"PAYLOAD_MISMATCH");
            if(local=="DRAFT"){
                Require(known==s.receipts.Count,"RECOVERY_REQUIRED");
                foreach(var r in s.receipts)Require(r.delivery_id!=m["delivery_id"],"RECOVERY_REQUIRED");
                if(s.delivery_state=="PREPARED")Require(s.delivery_id==m["delivery_id"]&&s.payload_hash==m["payload_hash"]&&s.mission_id==m["mission_id"],"PAYLOAD_MISMATCH");
            }else{
                Require(s.delivery_id==m["delivery_id"]&&s.payload_hash==m["payload_hash"]&&s.mission_id==m["mission_id"],"RECOVERY_REQUIRED");
                Require((s.receipts.Count==known&&s.delivery_state=="PREPARED")||(s.receipts.Count==known+1&&s.delivery_state=="REWARD_COMMITTED"),"RECOVERY_REQUIRED");
            }
        }
        public static BridgeSaveState Change(BridgeSaveState current,Dictionary<string,string> m){
            var s=Normalize(current);Reconcile(s,m);int known=Known(m);
            if(m["type"]=="prepare"){
                Require(m["known_state"]=="DRAFT","INVALID_STATE");Require(s.receipts.Count<MaxReceipts,"LEDGER_FULL");
                s.delivery_id=m["delivery_id"];s.payload_hash=m["payload_hash"];s.delivery_state="PREPARED";s.mission_id=m["mission_id"];
            }else if(m["type"]=="commit"){
                Require(m["known_state"]=="RESERVED","NOT_PREPARED");
                Require(s.delivery_state=="REWARD_COMMITTED","NATIVE_REWARD_INTENT_REQUIRED");
            }else throw new InvalidDataException("UNKNOWN_MESSAGE");
            return s;
        }
        public static BridgeSaveState MarkPending(BridgeSaveState current,Dictionary<string,string> m){
            var s=Normalize(current);Reconcile(s,m);Require(m["type"]=="commit"&&m["known_state"]=="RESERVED"&&s.delivery_state=="PREPARED","NOT_PREPARED");
            Require(s.receipts.Count<MaxReceipts,"LEDGER_FULL");s.delivery_state="REWARD_PENDING";return s;
        }
        public static BridgeSaveState CompletePending(BridgeSaveState current,Dictionary<string,string> m,string cargoId,string blueprint){
            var s=Normalize(current);Identity(s,m);int known=Known(m);
            Require(s.delivery_state=="REWARD_PENDING"&&s.delivery_id==m["delivery_id"]&&s.payload_hash==m["payload_hash"]
                &&s.mission_id==m["mission_id"]&&known==s.receipts.Count&&m["last_receipt_id"]==Last(s),"RECOVERY_REQUIRED");
            Require(EntityId(cargoId)&&blueprint==CargoCatalog.Get(s.mission_id).Blueprint,"NATIVE_CARGO_INVALID_ID");
            foreach(var r in s.receipts)Require(r.native_cargo_id!=cargoId,"RECOVERY_REQUIRED");
            s.receipts.Add(new DeliveryReceipt{delivery_id=s.delivery_id,payload_hash=s.payload_hash,sequence=known+1,mission_id=s.mission_id,
                reward_kind="native_cargo",native_cargo_id=cargoId,native_blueprint_id=blueprint});
            s.delivery_state="REWARD_COMMITTED";return s;
        }
    }
}
