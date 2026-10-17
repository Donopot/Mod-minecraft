using System;
using System.Collections.Generic;
using System.IO;
using VoidDynasty.RogueTrader;

internal static class DeliveryLedgerHarness {
    private static void Check(bool v,string why){if(!v)throw new Exception(why);}
    private static void Reject(Action action,string code){try{action();throw new Exception("Accepted "+code);}catch(InvalidDataException e){Check(e.Message==code,e.Message);}}
    private static BridgeSaveState Initial(){return new BridgeSaveState{bridge_campaign_id="00000000-0000-4000-8000-000000000002",native_campaign_id="native",probe_receipt=Guid.NewGuid().ToString(),persistence_verified=true};}
    private static Dictionary<string,string> Request(BridgeSaveState s,string missionId=CargoCatalog.Default){var mission=CargoCatalog.Get(missionId);return new Dictionary<string,string>{
        {"world_id",s.world_id},{"pair_id",s.pair_id},{"checkpoint_id",s.checkpoint_id},{"campaign_id",s.bridge_campaign_id},
        {"mission_hash",DeliveryLedger.MissionHash},{"mission_id",missionId},{"item_id",mission.Item},{"count",mission.Count.ToString()},{"delivery_id",Guid.NewGuid().ToString()},
        {"known_state","DRAFT"},{"type","prepare"},{"completed_count",s.receipts.Count.ToString()},{"last_receipt_id",DeliveryLedger.Last(s)},
        {"payload_hash",DeliveryLedger.Fingerprint(s,s.receipts.Count,DeliveryLedger.Last(s),missionId)}};}
    public static void Main(string[] args){try{
        if(args.Length==4){Console.WriteLine(NativeCargoArchive.Contains(args[1],args[2],args[3])?"FOUND":"ABSENT");return;}
        if(args.Length==1){
            var before=PerSaveState.Decode(PerSaveState.ReadArchive(args[0]));var migrated=DeliveryLedger.Normalize(before);
            Check(migrated.logistics_reserve==before.logistics_reserve&&migrated.delivery_id==before.delivery_id
                &&migrated.bridge_campaign_id==before.bridge_campaign_id&&migrated.pair_id==before.pair_id,"Real migration changed identity/reward");
            Check(migrated.receipts.Count==1&&migrated.logistics_reserve==10,"Real receipt missing");
            Console.WriteLine("PASS real saved receipt migrated, reserve=10, completed=1");return;
        }
        Check(DeliveryAvailability.CanRead(true,true,true,false),"Menu must allow reading");
        Check(!DeliveryAvailability.CanMutate(true,"Inventory",false,false),"Menu allowed mutation");
        Check(!DeliveryAvailability.CanMutate(true,"UnknownMode",false,false),"Unknown mode allowed mutation");
        Check(!DeliveryAvailability.CanRead(true,false,true,false)&&!DeliveryAvailability.CanRead(false,true,true,false),"Unloaded read");
        Check(!DeliveryAvailability.CanRead(true,true,true,true),"Probe allowed read");
        Check(DeliveryAvailability.CanMutate(true,"Default",false,false)&&!DeliveryAvailability.CanMutate(true,"Default",true,false),"Default/combat policy");
        var raw=Initial();var pair=new Dictionary<string,string>{{"world_id","00000000-0000-4000-8000-000000000001"},{"pair_id","00000000-0000-4000-8000-000000000003"},{"checkpoint_id",Guid.NewGuid().ToString()}};
        var unverified=DeliveryLedger.Copy(raw);unverified.persistence_verified=false;Reject(()=>DeliveryLedger.Pair(unverified,pair),"PERSISTENCE_NOT_VERIFIED");
        var empty=DeliveryLedger.Pair(raw,pair);Check(empty.receipts.Count==0&&empty.logistics_reserve==0,"Pair rewarded");
        Console.WriteLine(DeliveryLedger.Fingerprint(empty,0,""));
        foreach(var mission in CargoCatalog.All)Console.WriteLine("HASH "+mission.Id+" "+DeliveryLedger.Fingerprint(empty,0,"",mission.Id));
        // Migrate the actual old single-delivery shape without resetting reward or identities.
        var legacy=DeliveryLedger.Copy(empty);legacy.ledger_version=0;legacy.receipts=null;legacy.logistics_reserve=10;
        legacy.delivery_state="REWARD_COMMITTED";legacy.delivery_id=Guid.NewGuid().ToString();legacy.payload_hash=new string('a',64);
        var s=DeliveryLedger.Normalize(legacy);Check(s.receipts.Count==1&&s.receipts[0].delivery_id==legacy.delivery_id&&s.logistics_reserve==10,"Legacy receipt lost");
        var invalidLegacy=DeliveryLedger.Copy(legacy);invalidLegacy.delivery_state="PREPARED";invalidLegacy.logistics_reserve=0;Reject(()=>DeliveryLedger.Normalize(invalidLegacy),"RECOVERY_REQUIRED");
        var m=Request(s);var prepared=DeliveryLedger.Change(legacy,m);Check(prepared.logistics_reserve==10&&prepared.receipts.Count==1&&legacy.receipts==null,"Prepare/migration mutated source");
        var duplicate=DeliveryLedger.Change(prepared,m);Check(duplicate.delivery_id==prepared.delivery_id,"Repeated prepare changed id");
        var tampered=new Dictionary<string,string>(m);tampered["payload_hash"]=new string('b',64);Reject(()=>DeliveryLedger.Change(prepared,tampered),"PAYLOAD_MISMATCH");
        m["known_state"]="RESERVED";m["type"]="commit";
        Reject(()=>DeliveryLedger.Change(prepared,m),"NATIVE_REWARD_INTENT_REQUIRED");
        var intent=DeliveryLedger.MarkPending(prepared,m);Reject(()=>DeliveryLedger.Reconcile(intent,m),"RECOVERY_REQUIRED");
        Reject(()=>DeliveryLedger.CompletePending(intent,m,Guid.NewGuid().ToString("N"),new string('a',32)),"NATIVE_CARGO_INVALID_ID");
        s=DeliveryLedger.CompletePending(intent,m,Guid.NewGuid().ToString("N"),CargoCatalog.Get(m["mission_id"]).Blueprint);
        for(int i=0;i<100;i++)s=DeliveryLedger.Change(s,m);
        Check(s.logistics_reserve==10&&s.receipts.Count==2&&DeliveryLedger.NativeCount(s)==1,"Duplicate reward");
        s=PerSaveState.Decode(DiagnosticJson.Serialize(s));DeliveryLedger.Validate(s);
        var settled=new Dictionary<string,string>(m);settled["known_state"]="SETTLED";settled["completed_count"]="2";settled["last_receipt_id"]=s.delivery_id;
        DeliveryLedger.Reconcile(s,settled);Reject(()=>DeliveryLedger.Reconcile(prepared,settled),"RECOVERY_REQUIRED");
        var oldWorld=new Dictionary<string,string>(settled);oldWorld["completed_count"]="1";oldWorld["last_receipt_id"]=legacy.delivery_id;oldWorld["delivery_id"]=legacy.delivery_id;oldWorld["payload_hash"]=legacy.payload_hash;
        Reject(()=>DeliveryLedger.Reconcile(s,oldWorld),"RECOVERY_REQUIRED");
        var oldCommit=new Dictionary<string,string>(m);
        var reused=Request(s);reused["delivery_id"]=legacy.delivery_id;Reject(()=>DeliveryLedger.Change(s,reused),"RECOVERY_REQUIRED");
        for(int n=3;n<=DeliveryLedger.MaxReceipts;n++){
            var next=Request(s,CargoCatalog.All[(n-1)%CargoCatalog.All.Length].Id);var p=DeliveryLedger.Change(s,next);next["known_state"]="RESERVED";next["type"]="commit";var pending=DeliveryLedger.MarkPending(p,next);s=DeliveryLedger.CompletePending(pending,next,Guid.NewGuid().ToString("N"),CargoCatalog.Get(next["mission_id"]).Blueprint);
            s=DeliveryLedger.Change(s,next);Check(s.receipts.Count==n&&s.logistics_reserve==10&&DeliveryLedger.NativeCount(s)==n-1,"Repeat total incorrect");
        }
        Reject(()=>DeliveryLedger.Change(s,oldCommit),"RECOVERY_REQUIRED");
        var full=Request(s);Reject(()=>DeliveryLedger.Change(s,full),"LEDGER_FULL");
        Check(s.receipts[0].delivery_id==legacy.delivery_id,"Evicted old receipt");
        var roundTrip=PerSaveState.Decode(DiagnosticJson.Serialize(s));DeliveryLedger.Validate(roundTrip);
        var corrupt=DeliveryLedger.Copy(s);corrupt.receipts[1].sequence=1;Reject(()=>DeliveryLedger.Normalize(corrupt),"RECOVERY_REQUIRED");
        Console.WriteLine("PASS legacy +10 retained, native missions through 256, replayed commits, pending intents and rollback rejected");
    }catch(Exception e){Console.Error.WriteLine(e);Environment.ExitCode=1;}}
}
