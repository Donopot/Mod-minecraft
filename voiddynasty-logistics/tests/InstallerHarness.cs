using System;
using System.Collections.Generic;
using System.IO;
using System.IO.Compression;
using System.Linq;
using System.Text;
using VoidDynasty.Setup;

internal static class InstallerHarness {
    private static int assertions;
    private static void Assert(bool value,string message) { assertions++;if(!value)throw new Exception(message); }
    private static byte[] Utf(string s) { return Encoding.UTF8.GetBytes(s); }
    private static byte[] Jar(string id,string version) {
        using(var m=new MemoryStream()) {
            using(var z=new ZipArchive(m,ZipArchiveMode.Create,true))using(var w=new StreamWriter(z.CreateEntry("fabric.mod.json").Open()))w.Write("{\"id\":\""+id+"\",\"version\":\""+version+"\"}");
            return m.ToArray();
        }
    }
    private sealed class Lab {
        public string Root,State;
        public Options O;
        public Bundle B;
        public Engine E;
        public bool Running;
        public Lab(string root,string name) {
            Root=Path.Combine(root,name);State=Path.Combine(Root,"state");
            O=new Options{Minecraft=Path.Combine(Root,"minecraft instance"),Game=Path.Combine(Root,"RT install"),RtData=Path.Combine(Root,"RT data"),Original=Path.Combine(Root,"original.jar")};
            byte[] original=Jar("voiddynasty","1.0.0"),code=Utf("fake compiled game reference");
            B=new Bundle{Version="0.6.1",OriginalHash=Hash.Bytes(original),GameHash=Hash.Bytes(code),Minecraft=Jar("voiddynasty_bridge","0.6.1"),Dll=Utf("our DLL"),Info=Utf("{\"Version\":\"0.6.1\"}")};
            Put(O.Original,original);
            Put(Path.Combine(O.Game,"WH40KRT.exe"),Utf("fake exe"));Put(Path.Combine(O.Game,"WH40KRT_Data","Managed","Code.dll"),code);
            using(var z=ZipFile.Open(MakeParent(Path.Combine(O.Game,"WH40KRT_Data","StreamingAssets","OwlcatUnityModManager.zip")),ZipArchiveMode.Create))z.CreateEntry("UnityModManager.dll");
            Put(Path.Combine(O.Minecraft,"versions","fabric-profile","fabric-profile.json"),Utf("{\"id\":\"fabric-profile\",\"inheritsFrom\":\"26.3\",\"libraries\":[{\"name\":\"net.fabricmc:fabric-loader:0.19.5\"}]}"));
            Put(Path.Combine(O.Minecraft,"mods","api.jar"),Jar("fabric-api","0.162.0+26.3"));
            Put(Path.Combine(O.Minecraft,"mods","gecko.jar"),Jar("geckolib","5.5.7"));
            O.World=Path.Combine(O.Minecraft,"saves","existing world");Put(Path.Combine(O.World,"level.dat"),Utf("unchanged world"));Put(Path.Combine(O.World,"region","r.0.0.mca"),Utf("world chunks"));
            O.Save=Path.Combine(O.RtData,"Saved Games","matching.zks");Put(O.Save,Utf("unchanged campaign"));
            E=new Engine(B,State,()=>Running);
        }
        public string Mc { get { return Path.Combine(O.Minecraft,"mods","voiddynasty-bridge-0.6.1.jar"); } }
        public string Dll { get { return Path.Combine(O.RtData,"UnityModManager","VoidDynastyBridge","VoidDynastyBridge.dll"); } }
    }
    private static string MakeParent(string p) { Directory.CreateDirectory(Path.GetDirectoryName(p));return p; }
    private static void Put(string p,byte[] b) { File.WriteAllBytes(MakeParent(p),b); }
    private static void Fails(Action op,string expected) { try { op();throw new Exception("Operation unexpectedly succeeded"); }catch(IOException e) { Assert(e.Message.Contains(expected),"Unexpected failure: "+e.Message); } }
    private static void Log(string s) { }
    private static void Roundtrip(string root) {
        var l=new Lab(root,"round trip espaces é");
        Assert(l.E.Inspect(l.O).Errors.Count==0,l.E.Inspect(l.O).Display());
        string manifest=l.E.Install(l.O,Log);var r=l.E.ValidRecord(manifest);
        Assert(r.Status=="completed"&&r.SaveBackups.Count==2,"Install journal and save copies");
        Assert(r.SaveHashes.Count==2&&r.SaveHashes.All(x=>Hash.FileHash(x.Key)==x.Value),"Save backup hashes retained");
        Assert(Hash.FileHash(l.Mc)==Hash.Bytes(l.B.Minecraft),"Minecraft payload hash");Assert(Hash.FileHash(l.Dll)==Hash.Bytes(l.B.Dll),"RT payload hash");
        Assert(File.ReadAllText(l.O.Save)=="unchanged campaign"&&File.ReadAllText(Path.Combine(l.O.World,"level.dat"))=="unchanged world","Saves untouched");
        using(var z=ZipFile.OpenRead(r.SaveBackups[0]))Assert(z.GetEntry("region/r.0.0.mca")!=null&&z.GetEntry("level.dat")!=null,"World ZIP hierarchy");
        string uninstall=l.E.Maintain(manifest,true,Log);
        Assert(!File.Exists(l.Mc)&&!File.Exists(l.Dll),"Uninstall both modules");
        Assert(File.Exists(Path.Combine(l.O.Minecraft,"mods","voiddynasty-1.0.0.jar")),"Original kept");
        Assert(File.Exists(l.O.Save)&&File.Exists(Path.Combine(l.O.World,"level.dat")),"Uninstall preserves saves");
        l.E.Maintain(uninstall,false,Log);Assert(File.Exists(l.Mc)&&File.Exists(l.Dll),"Undo uninstall");
        Console.WriteLine("PASS install, world/RT backup, uninstall, undo uninstall");
    }
    private static void Update(string root) {
        var l=new Lab(root,"upgrade");byte[] old=Jar("voiddynasty_bridge","0.5.0");
        string existing=Path.Combine(l.O.Minecraft,"mods","custom old name.jar");Put(existing,old);Put(l.Dll,Utf("old RT module"));
        string manifest=l.E.Install(l.O,Log);Assert(!File.Exists(l.Mc),"Retains existing companion filename");
        Assert(Hash.FileHash(existing)==Hash.Bytes(l.B.Minecraft),"Upgrades existing ID");
        l.E.Maintain(manifest,false,Log);Assert(Hash.FileHash(existing)==Hash.Bytes(old)&&File.ReadAllText(l.Dll)=="old RT module","Restore pre-upgrade modules");
        Console.WriteLine("PASS update and exact previous-file restoration");
    }
    private static void Protection(string root) {
        var l=new Lab(root,"guards");
        string game=l.O.Game;l.O.Game="";Assert(l.E.Inspect(l.O).Errors.Any(x=>x.Contains("WH40KRT.exe")),"Missing game folder has actionable guidance");l.O.Game=game;
        Put(Path.Combine(l.O.Minecraft,"mods","double.jar"),Jar("voiddynasty_bridge","0.4.0"));Put(l.Mc,Jar("voiddynasty_bridge","0.5.0"));
        Assert(l.E.Inspect(l.O).Errors.Any(x=>x.Contains("Plusieurs")),"Duplicate IDs blocked");
        File.Delete(Path.Combine(l.O.Minecraft,"mods","double.jar"));
        l.Running=true;Fails(()=>l.E.Install(l.O,Log),"Fermer Minecraft");Assert(l.E.Records().Length==0,"Running game causes no writes");l.Running=false;
        Put(Path.Combine(l.O.Minecraft,"mods","gecko.jar"),Jar("geckolib","wrong"));Assert(l.E.Inspect(l.O).Errors.Any(x=>x.Contains("Version non validee")),"Wrong dependency blocks");
        Put(Path.Combine(l.O.Minecraft,"mods","gecko.jar"),Jar("geckolib","5.5.7"));
        Put(Path.Combine(l.O.Game,"WH40KRT_Data","Managed","Code.dll"),Utf("different game"));Assert(l.E.Inspect(l.O).Errors.Any(x=>x.Contains("differe")),"Wrong game binary blocks");
        Put(Path.Combine(l.O.Game,"WH40KRT_Data","Managed","Code.dll"),Utf("fake compiled game reference"));
        l.O.World=l.Root;Assert(l.E.Inspect(l.O).Errors.Count>0,"Backup cannot traverse outside saves");
        Console.WriteLine("PASS duplicates, process, dependencies, game compatibility, path boundaries");
    }
    private static void Failure(string root) {
        var l=new Lab(root,"rollback");Put(l.Dll,Utf("previous RT"));
        l.E.BeforeWrite=i=>{if(i==3)throw new IOException("injected copy failure");};
        Fails(()=>l.E.Install(l.O,Log),"injected copy failure");
        Assert(!File.Exists(l.Mc)&&!File.Exists(Path.Combine(l.O.Minecraft,"mods","voiddynasty-1.0.0.jar")),"Created files rolled back");
        Assert(File.ReadAllText(l.Dll)=="previous RT","Old files restored after failure");
        Assert(Json.Read<Record>(l.E.Records()[0]).Status=="rolled_back","Failed journal reports rollback");
        var race=new Lab(root,"process race");race.E.BeforeWrite=i=>{if(i==1)race.Running=true;};
        Fails(()=>race.E.Install(race.O,Log),"doivent rester fermes");Assert(!File.Exists(race.Mc),"Game launch during operation blocks and rolls back");
        Console.WriteLine("PASS injected write failure and game-launch race rollback");
    }
    private static void Tamper(string root) {
        var l=new Lab(root,"tampering");Put(l.Dll,Utf("previous DLL"));string manifest=l.E.Install(l.O,Log);Put(l.Dll,Utf("changed by someone else"));
        Fails(()=>l.E.Maintain(manifest,true,Log),"change depuis");Assert(File.Exists(l.Mc),"All uninstall checks before any mutation");
        Put(l.Dll,l.B.Dll);var r=Json.Read<Record>(manifest);string originalTarget=r.Files[1].Target;r.Files[1].Target=Path.Combine(l.Root,"unrelated.jar");Json.Write(manifest,r);
        Fails(()=>l.E.Maintain(manifest,false,Log),"hors du dossier");Assert(File.Exists(originalTarget),"Forged target rejected");
        r.Files[1].Target=originalTarget;Json.Write(manifest,r);
        var original=r.Files.First(f=>f.Before!="");Put(original.Backup,Utf("damaged backup"));
        Fails(()=>l.E.Maintain(manifest,false,Log),"Sauvegarde de mod");
        Console.WriteLine("PASS modified target, forged journal and corrupted backup protection");
    }
    private static void Pending(string root) {
        var l=new Lab(root,"interrupted");string manifest=l.E.Install(l.O,Log);var r=Json.Read<Record>(manifest);r.Status="prepared";Json.Write(manifest,r);
        Fails(()=>l.E.Install(l.O,Log),"interrompue");l.E.Maintain(manifest,false,Log);Assert(!File.Exists(l.Mc),"Interrupted install recoverable");
        l.E.Install(l.O,Log);Console.WriteLine("PASS interrupted journal blocks new installs and supports recovery");
        var partial=new Lab(root,"interrupted before companion");string pm=partial.E.Install(partial.O,Log);var pr=Json.Read<Record>(pm);
        pr.Status="prepared";Json.Write(pm,pr);
        foreach(var f in pr.Files.Where(f=>f.Managed))File.Delete(f.Target);
        partial.E.Maintain(pm,false,Log);
        Assert(!File.Exists(partial.Mc)&&!File.Exists(partial.Dll),"Crash before new companion creation can roll back");
    }
    public static int Main(string[] args) {
        try {Roundtrip(args[0]);Update(args[0]);Protection(args[0]);Failure(args[0]);Tamper(args[0]);Pending(args[0]);Console.WriteLine("PASS "+assertions+" installer assertions");return 0;}
        catch(Exception e) { Console.Error.WriteLine(e.ToString());return 1; }
    }
}
