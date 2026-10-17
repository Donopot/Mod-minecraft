using System;
using System.Collections;
using System.IO;
using System.IO.Compression;
using System.Linq;
using System.Reflection;
using System.Runtime.Serialization.Json;
using System.Xml;

namespace VoidDynasty.RogueTrader {
    public sealed class NativeCargoGrant {
        public string Id,Blueprint;
    }
    // Uses the same CargoState.Create(BlueprintCargo) path as native quest rewards.
    // Called only on Unity's thread, after a durable REWARD_PENDING intent.
    public static class NativeCargo {
        public static bool EntityId(string id){Guid parsed;return id!=null&&(id.Length==32||id.Length==36)&&Guid.TryParse(id,out parsed);}
        private static object Resolve(object game,CargoMission mission){
            var player=PersistenceProbe.Read(game,"Player");
            DeliveryLedger.Require(PersistenceProbe.Read(player,"PlayerShip")!=null,"NATIVE_CARGO_UNAVAILABLE");
            var library=AppDomain.CurrentDomain.GetAssemblies().Select(a=>a.GetType("Kingmaker.Blueprints.ResourcesLibrary",false)).FirstOrDefault(t=>t!=null);
            if(library==null)throw new InvalidDataException("NATIVE_CARGO_UNAVAILABLE");
            var load=library.GetMethods(BindingFlags.Static|BindingFlags.Public).Single(m=>m.Name=="TryGetBlueprint"&&!m.IsGenericMethod&&m.GetParameters().Length==1&&m.GetParameters()[0].ParameterType==typeof(string));
            var blueprint=load.Invoke(null,new object[]{mission.Blueprint});
            DeliveryLedger.Require(blueprint!=null&&Convert.ToString(PersistenceProbe.Read(blueprint,"OriginType"))==mission.Origin,"NATIVE_CARGO_UNAVAILABLE");
            return blueprint;
        }
        public static void CheckAvailable(object game,CargoMission mission){Resolve(game,mission);}
        public static NativeCargoGrant Create(object game,CargoMission mission){
            var blueprint=Resolve(game,mission);var player=PersistenceProbe.Read(game,"Player");
            var state=PersistenceProbe.Read(player,"CargoState");
            DeliveryLedger.Require(state!=null,"NATIVE_CARGO_UNAVAILABLE");
            var create=state.GetType().GetMethods(BindingFlags.Public|BindingFlags.Instance).Single(m=>m.Name=="Create"&&m.GetParameters().Length==1&&m.GetParameters()[0].ParameterType.FullName=="Kingmaker.Blueprints.Cargo.BlueprintCargo");
            var cargo=create.Invoke(state,new object[]{blueprint});
            DeliveryLedger.Require(cargo!=null&&Equals(PersistenceProbe.Read(cargo,"IsFull"),true)
                &&Convert.ToInt32(PersistenceProbe.Read(cargo,"FilledVolumePercent"))>=100,"NATIVE_CARGO_NOT_FULL");
            string id=Convert.ToString(PersistenceProbe.Read(cargo,"UniqueId"));
            DeliveryLedger.Require(EntityId(id),"NATIVE_CARGO_INVALID_ID");
            return new NativeCargoGrant{Id=id,Blueprint=mission.Blueprint};
        }
    }

}
