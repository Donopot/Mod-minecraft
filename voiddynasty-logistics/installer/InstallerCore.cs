using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.IO.Compression;
using System.Linq;
using System.Reflection;
using System.Security.Cryptography;
using System.Text;
using System.Web.Script.Serialization;

namespace VoidDynasty.Setup {
    public sealed class Options {
        public string Minecraft = "", Game = "", RtData = "", Original = "", World = "", Save = "";
        public bool BackupSaves = true;
    }
    public sealed class Bundle {
        public string Version, OriginalHash, GameHash;
        public byte[] Minecraft, Dll, Info;
        public static Bundle Load() {
            var a = Assembly.GetExecutingAssembly();
            Func<string, byte[]> read = name => { using (var s = a.GetManifestResourceStream(name)) {
                if (s == null) throw new InvalidDataException("Paquet incomplet : " + name);
                using (var m = new MemoryStream()) { s.CopyTo(m); return m.ToArray(); }
            }};
            var meta = Json.Object(Encoding.UTF8.GetString(read("payload.manifest")));
            var b = new Bundle { Version=Json.Text(meta,"version"), OriginalHash=Json.Text(meta,"original_sha256"),
                GameHash=Json.Text(meta,"game_code_sha256"), Minecraft=read("payload.minecraft"), Dll=read("payload.dll"), Info=read("payload.info") };
            if (Hash.Bytes(b.Minecraft)!=Json.Text(meta,"minecraft_sha256") || Hash.Bytes(b.Dll)!=Json.Text(meta,"dll_sha256") || Hash.Bytes(b.Info)!=Json.Text(meta,"info_sha256"))
                throw new InvalidDataException("Les fichiers embarques ne correspondent pas au manifeste.");
            return b;
        }
    }
    public static class Json {
        private static JavaScriptSerializer Serializer() { return new JavaScriptSerializer { MaxJsonLength=4*1024*1024, RecursionLimit=64 }; }
        public static Dictionary<string,object> Object(string text) { return Serializer().Deserialize<Dictionary<string,object>>(text.TrimStart('\ufeff')); }
        public static string Text(Dictionary<string,object> o,string key) { object v; return o!=null && o.TryGetValue(key,out v) && v!=null ? Convert.ToString(v) : ""; }
        public static T Read<T>(string path) { return Serializer().Deserialize<T>(File.ReadAllText(path,Encoding.UTF8).TrimStart('\ufeff')); }
        public static void Write(string path,object data) { Files.Atomic(path,Encoding.UTF8.GetBytes(Serializer().Serialize(data))); }
    }
    public static class Hash {
        public static string Bytes(byte[] data) { using(var h=SHA256.Create()) return BitConverter.ToString(h.ComputeHash(data)).Replace("-", "").ToLowerInvariant(); }
        public static string FileHash(string file) { using(var s=File.OpenRead(file)) using(var h=SHA256.Create()) return BitConverter.ToString(h.ComputeHash(s)).Replace("-", "").ToLowerInvariant(); }
    }
    public static class Files {
        public static string Full(string path) {
            if(string.IsNullOrWhiteSpace(path)) throw new IOException("Un dossier ou fichier requis n'est pas renseigne.");
            return Path.GetFullPath(path).TrimEnd(Path.DirectorySeparatorChar);
        }
        public static bool Same(string a,string b) { return string.Equals(Full(a),Full(b),StringComparison.OrdinalIgnoreCase); }
        public static string Within(string root,string path) {
            string r=Full(root),p=Full(path);
            if(!p.StartsWith(r+Path.DirectorySeparatorChar,StringComparison.OrdinalIgnoreCase)) throw new IOException("Chemin hors du dossier autorise : "+p);
            NoLinks(p); return p;
        }
        public static void NoLinks(string path) {
            for(string p=Full(path); !string.IsNullOrEmpty(p); p=Path.GetDirectoryName(p)) {
                if((File.Exists(p)||Directory.Exists(p)) && (File.GetAttributes(p)&FileAttributes.ReparsePoint)!=0)
                    throw new IOException("Choisir un chemin direct, sans lien ou jonction : "+p);
            }
        }
        public static void Atomic(string path,byte[] data) {
            NoLinks(path); Directory.CreateDirectory(Path.GetDirectoryName(path));
            string temp=path+".voiddynasty-"+Guid.NewGuid().ToString("N")+".tmp";
            try {
                using(var s=new FileStream(temp,FileMode.CreateNew,FileAccess.Write,FileShare.None)) { s.Write(data,0,data.Length); s.Flush(true); }
                if(File.Exists(path)) File.Replace(temp,path,null); else File.Move(temp,path);
            } finally { if(File.Exists(temp)) File.Delete(temp); }
        }
        public static IEnumerable<string> Tree(string root) {
            NoLinks(root);
            foreach(string f in Directory.GetFiles(root)) { NoLinks(f); yield return f; }
            foreach(string d in Directory.GetDirectories(root)) foreach(string f in Tree(d)) yield return f;
        }
        public static void ZipWorld(string source,string output,Action<string> log) {
            NoLinks(source); string prefix=Full(source)+Path.DirectorySeparatorChar;
            var files=Tree(source).ToArray();
            var expected=new Dictionary<string,string>();
            using(var zip=ZipFile.Open(output,ZipArchiveMode.Create)) foreach(string f in files) {
                string name=f.Substring(prefix.Length).Replace('\\','/');expected[name]=Hash.FileHash(f);
                zip.CreateEntryFromFile(f,name,CompressionLevel.Optimal);
            }
            using(var zip=ZipFile.OpenRead(output)) foreach(var entry in zip.Entries) using(var input=entry.Open()) using(var hash=SHA256.Create()) {
                string actual=BitConverter.ToString(hash.ComputeHash(input)).Replace("-", "").ToLowerInvariant();
                if(actual!=expected[entry.FullName])throw new IOException("Contenu du monde sauvegarde non verifie : "+entry.FullName);
            }
            log("Sauvegarde du monde terminee : "+files.Length+" fichiers.");
        }
    }
    public sealed class Check {
        public List<string> Errors=new List<string>(), Notes=new List<string>();
        public string Profile="";
        public string Display() { return string.Join(Environment.NewLine,Errors.Select(x=>"BLOQUANT : "+x).Concat(Notes.Select(x=>"• "+x))); }
    }
    public sealed class Change {
        public string Target, SourceName;
        public byte[] Data;
        public bool Managed;
    }
    public sealed class SavedFile {
        public string Target, Before, After, Backup;
        public bool Managed;
    }
    public sealed class Record {
        public string Id, Version, Status, Created, Minecraft, RtData, Operation;
        public List<SavedFile> Files=new List<SavedFile>();
        public List<string> SaveBackups=new List<string>();
        public Dictionary<string,string> SaveHashes=new Dictionary<string,string>();
    }
    public sealed class Engine {
        public readonly Bundle Bundle;
        public readonly string State;
        private readonly Func<bool> running;
        // Test seam exercises copy failure and process races without game processes or user paths.
        public Action<int> BeforeWrite;
        public Engine(Bundle bundle,string state,Func<bool> gamesRunning) { Bundle=bundle;State=Files.Full(state);running=gamesRunning;Files.NoLinks(State); }
        public static bool GamesRunning() {
            foreach(string name in new[]{"WH40KRT","javaw","java"}) {
                var processes=Process.GetProcessesByName(name);bool found=processes.Length>0;
                foreach(var process in processes)process.Dispose();if(found)return true;
            }
            return false;
        }
        public static Dictionary<string,object> Mod(string path) {
            using(var z=ZipFile.OpenRead(path)) {
                var e=z.GetEntry("fabric.mod.json"); if(e==null)return null;
                if(e.Length>262144)throw new InvalidDataException("fabric.mod.json trop grand : "+Path.GetFileName(path));
                using(var r=new StreamReader(e.Open(),Encoding.UTF8))return Json.Object(r.ReadToEnd());
            }
        }
        private static Dictionary<string,List<string>> Mods(string root) {
            var mods=new Dictionary<string,List<string>>();
            if(!Directory.Exists(root)) return mods;
            Files.NoLinks(root);
            foreach(string f in Directory.GetFiles(root,"*.jar")) {
                Files.NoLinks(f);var m=Mod(f);if(m==null)continue;
                string id=Json.Text(m,"id");if(!mods.ContainsKey(id))mods[id]=new List<string>();mods[id].Add(f);
            }
            return mods;
        }
        private static string Destination(Dictionary<string,List<string>> mods,string id,string root,string name) {
            List<string> paths;
            if(mods.TryGetValue(id,out paths)) { if(paths.Count!=1)throw new IOException("Plusieurs exemplaires du mod "+id+" : retirer le doublon avant de continuer.");return paths[0]; }
            return Path.Combine(root,name);
        }
        public Check Inspect(Options o) {
            var c=new Check();
            try {
                if(string.IsNullOrWhiteSpace(o.Minecraft))throw new IOException("Choisir le dossier de l'instance Minecraft contenant mods et versions.");
                if(string.IsNullOrWhiteSpace(o.Game))throw new IOException("Choisir le dossier d'installation Rogue Trader contenant WH40KRT.exe.");
                if(string.IsNullOrWhiteSpace(o.RtData))throw new IOException("Choisir le dossier de donnees Rogue Trader contenant Saved Games et UnityModManager.");
                Files.NoLinks(o.Minecraft);Files.NoLinks(o.Game);Files.NoLinks(o.RtData);
                if(!Directory.Exists(o.Minecraft))throw new IOException("Instance Minecraft introuvable.");
                if(!Directory.Exists(o.RtData))c.Errors.Add("Dossier de donnees Rogue Trader absent. Lancer le jeu une premiere fois, puis choisir son dossier contenant Saved Games.");
                string versions=Path.Combine(o.Minecraft,"versions");
                if(Directory.Exists(versions)) foreach(string dir in Directory.GetDirectories(versions)) {
                    string file=Path.Combine(dir,Path.GetFileName(dir)+".json");if(!File.Exists(file))continue;
                    Files.NoLinks(file);var p=Json.Object(File.ReadAllText(file,Encoding.UTF8));
                    object libs;
                    bool fabric=p.TryGetValue("libraries",out libs) && libs is System.Collections.IEnumerable && ((System.Collections.IEnumerable)libs).Cast<object>().OfType<Dictionary<string,object>>().Any(x=>Json.Text(x,"name")=="net.fabricmc:fabric-loader:0.19.5");
                    if(fabric&&Json.Text(p,"inheritsFrom")=="26.3") { c.Profile=Json.Text(p,"id");break; }
                }
                if(c.Profile=="")c.Errors.Add("Profil Minecraft 26.3 / Fabric 0.19.5 absent de cette instance. Installer ce profil dans le lanceur puis revenir.");
                else c.Notes.Add("Profil a choisir dans le lanceur : "+c.Profile+". Java 25 requis par ce profil.");
                var mods=Mods(Path.Combine(o.Minecraft,"mods"));
                foreach(var expected in new[]{new[]{"fabric-api","0.162.0+26.3"},new[]{"geckolib","5.5.7"}}) {
                    List<string> paths;
                    if(!mods.TryGetValue(expected[0],out paths))c.Errors.Add("Dependance absente : "+expected[0]+" "+expected[1]+".");
                    else if(paths.Count!=1)c.Errors.Add("Dependance en double : "+expected[0]+".");
                    else if(Json.Text(Mod(paths[0]),"version")!=expected[1])c.Errors.Add("Version non validee : "+expected[0]+". Cette alpha demande "+expected[1]+".");
                    else c.Notes.Add(expected[0]+" "+expected[1]+" present.");
                }
                string managed=Path.Combine(o.Game,"WH40KRT_Data","Managed","Code.dll");
                foreach(string required in new[]{Path.Combine(o.Game,"WH40KRT.exe"),managed,Path.Combine(o.Game,"WH40KRT_Data","StreamingAssets","OwlcatUnityModManager.zip")}) {
                    Files.NoLinks(required);if(!File.Exists(required))c.Errors.Add("Installation Rogue Trader incomplete : "+Path.GetFileName(required));
                }
                if(File.Exists(managed)&&Hash.FileHash(managed)!=Bundle.GameHash)c.Errors.Add("Cette version de Rogue Trader differe de la version validee 1.6.1.514. Installation bloquee pour eviter un module incompatible.");
                else if(File.Exists(managed))c.Notes.Add("Assemblage Rogue Trader identique a la version cible 1.6.1.514.");
                string umm=Path.Combine(o.Game,"WH40KRT_Data","StreamingAssets","OwlcatUnityModManager.zip");
                if(File.Exists(umm))using(var z=ZipFile.OpenRead(umm)) {
                    if(!z.Entries.Any(e=>e.Name=="UnityModManager.dll"))c.Errors.Add("Le chargeur UMM fourni avec Rogue Trader n'est pas reconnu.");
                }
                if(!File.Exists(o.Original))c.Errors.Add("Selectionner le JAR original VOID DYNASTY 1.0.0, ou son exemplaire deja installe.");
                else {
                    Files.NoLinks(o.Original);var m=Mod(o.Original);
                    if(Json.Text(m,"id")!="voiddynasty"||Hash.FileHash(o.Original)!=Bundle.OriginalHash)c.Errors.Add("Le JAR original differe de VOID DYNASTY 1.0.0 valide. Aucun original n'est fourni dans ce paquet.");
                    else c.Notes.Add("VOID DYNASTY original reconnu ; il reste un mod separe.");
                }
                Plan(o); // Also rejects a duplicate mod ID or a colliding destination.
                if(o.BackupSaves) {
                    if(o.World!="") {
                        Files.Within(Path.Combine(o.Minecraft,"saves"),o.World);
                        if(!File.Exists(Path.Combine(o.World,"level.dat")))c.Errors.Add("Le monde a sauvegarder ne contient pas level.dat.");
                    }
                    if(o.Save!="") {
                        Files.Within(Path.Combine(o.RtData,"Saved Games"),o.Save);
                        if(!File.Exists(o.Save)||!o.Save.EndsWith(".zks",StringComparison.OrdinalIgnoreCase))c.Errors.Add("Choisir une sauvegarde Rogue Trader .zks existante.");
                    }
                    c.Notes.Add(o.World==""&&o.Save==""?"Aucune partie selectionnee. Les fichiers de mods seront sauvegardes ; aucune sauvegarde de partie n'est incluse.":"Copie du monde et/ou de la sauvegarde selectionnes avant installation. Leur association n'est pas verifiee par l'installeur.");
                } else c.Notes.Add("Copie des parties desactivee. Les fichiers de mods remplaces seront toujours sauvegardes.");
                if(running())c.Errors.Add("Fermer Minecraft et Rogue Trader avant toute installation ou restauration (un processus Java ou WH40KRT est actif).");
                c.Notes.Add("Mode pris en charge : Windows, solo local, deux jeux sur le meme compte Windows. Aucun chargeur ni dependance ne sera telecharge automatiquement.");
            } catch(Exception e) { c.Errors.Add(e.Message); }
            return c;
        }
        public List<Change> Plan(Options o) {
            string mc=Path.Combine(Files.Full(o.Minecraft),"mods"),rt=Path.Combine(Files.Full(o.RtData),"UnityModManager","VoidDynastyBridge");
            var mods=Mods(mc);
            var changes=new List<Change>{
                new Change{Target=Destination(mods,"voiddynasty",mc,"voiddynasty-1.0.0.jar"),SourceName="Original VOID DYNASTY (conserve a la desinstallation)",Data=File.Exists(o.Original)?File.ReadAllBytes(o.Original):new byte[0],Managed=false},
                new Change{Target=Destination(mods,"voiddynasty_bridge",mc,"voiddynasty-bridge-"+Bundle.Version+".jar"),SourceName="Compagnon Minecraft "+Bundle.Version,Data=Bundle.Minecraft,Managed=true},
                new Change{Target=Path.Combine(rt,"VoidDynastyBridge.dll"),SourceName="Module Rogue Trader "+Bundle.Version,Data=Bundle.Dll,Managed=true},
                new Change{Target=Path.Combine(rt,"Info.json"),SourceName="Description du module Rogue Trader",Data=Bundle.Info,Managed=true}
            };
            foreach(var f in changes) {
                Files.Within(f.Target.EndsWith(".jar",StringComparison.OrdinalIgnoreCase)?mc:rt,f.Target);
                if(Directory.Exists(f.Target))throw new IOException("Un dossier occupe le chemin du mod : "+f.Target);
                if(f.Target.EndsWith(".jar",StringComparison.OrdinalIgnoreCase)&&File.Exists(f.Target)) {
                    string expected=f.Managed?"voiddynasty_bridge":"voiddynasty";
                    if(Json.Text(Mod(f.Target),"id")!=expected)throw new IOException("Un autre fichier occupe la destination : "+f.Target);
                }
            }
            if(changes[0].Data.Length>0&&Hash.Bytes(changes[0].Data)!=Bundle.OriginalHash)throw new IOException("Le JAR original a change ou n'est pas reconnu.");
            return changes;
        }
        public string Preview(Options o) { return string.Join(Environment.NewLine+Environment.NewLine,Plan(o).Select(f=>f.SourceName+Environment.NewLine+"  → "+f.Target)); }
        private void Closed() { if(running())throw new IOException("Les jeux doivent rester fermes pendant cette operation."); }
        public string Install(Options o,Action<string> log) {
            var check=Inspect(o);if(check.Errors.Count>0)throw new IOException(check.Display());
            foreach(var pending in Records()) { var record=Json.Read<Record>(pending);if(record.Status=="prepared")throw new IOException("Installation interrompue a restaurer avant de continuer : "+pending); }
            Closed(); var changes=Plan(o);
            string dir=NewDirectory();
            var r=NewRecord(o,"install");
            // Save copies never overwrite or restore a live save. A backup failure prevents all mod writes.
            if(o.BackupSaves) {
                long size=0;
                if(o.World!="")size+=Files.Tree(o.World).Sum(x=>new FileInfo(x).Length);
                if(o.Save!="")size+=new FileInfo(o.Save).Length;
                var disk=new DriveInfo(Path.GetPathRoot(State));
                if(disk.AvailableFreeSpace<size+128L*1024*1024)throw new IOException("Espace disque insuffisant pour les sauvegardes.");
                if(o.World!="") { log("Sauvegarde du monde en cours...");string f=Path.Combine(dir,"monde.zip");Files.ZipWorld(o.World,f,log);r.SaveBackups.Add(f); }
                if(o.Save!="") { string f=Path.Combine(dir,Path.GetFileName(o.Save));File.Copy(o.Save,f);if(Hash.FileHash(f)!=Hash.FileHash(o.Save))throw new IOException("Copie de la sauvegarde RT non verifiee.");r.SaveBackups.Add(f);log("Sauvegarde RT copiee et verifiee."); }
                foreach(string f in r.SaveBackups)r.SaveHashes[f]=Hash.FileHash(f);
            }
            Closed(); return Apply(changes,r,dir,log);
        }
        private string NewDirectory() { string d=Path.Combine(State,"backups",DateTime.UtcNow.ToString("yyyyMMdd-HHmmss-fffffff")+"-"+Guid.NewGuid().ToString("N"));Files.NoLinks(d);Directory.CreateDirectory(d);return d; }
        private Record NewRecord(Options o,string op) { return new Record{Id=Guid.NewGuid().ToString("N"),Version=Bundle.Version,Created=DateTime.UtcNow.ToString("o"),Minecraft=Files.Full(o.Minecraft),RtData=Files.Full(o.RtData),Operation=op,Status="prepared"}; }
        private string Apply(List<Change> changes,Record r,string dir,Action<string> log) {
            var manifest=Path.Combine(dir,"installation.json");int index=0;
            foreach(var f in changes) {
                Files.NoLinks(f.Target);string before=File.Exists(f.Target)?Hash.FileHash(f.Target):"";
                string backup="";
                if(before!="") { backup=Path.Combine(dir,"mod-"+index+".bak");File.Copy(f.Target,backup);if(Hash.FileHash(backup)!=before)throw new IOException("Ancien fichier non sauvegarde correctement."); }
                r.Files.Add(new SavedFile{Target=Files.Full(f.Target),Before=before,After=f.Data==null?"":Hash.Bytes(f.Data),Backup=backup,Managed=f.Managed});index++;
            }
            Json.Write(manifest,r); // Durable rollback journal precedes the first mutation.
            var written=new List<SavedFile>();
            try {
                for(int i=0;i<changes.Count;i++) {
                    Closed();var f=changes[i];var saved=r.Files[i];
                    if(BeforeWrite!=null)BeforeWrite(i);
                    Closed();
                    string current=File.Exists(f.Target)?Hash.FileHash(f.Target):"";
                    if(current!=saved.Before)throw new IOException("Le fichier a change pendant l'installation : "+f.Target);
                    if(saved.Before==saved.After)continue;
                    written.Add(saved);
                    if(f.Data==null) { if(File.Exists(f.Target))File.Delete(f.Target); }
                    else Files.Atomic(f.Target,f.Data);
                    current=File.Exists(f.Target)?Hash.FileHash(f.Target):"";
                    if(current!=saved.After)throw new IOException("Verification apres copie echouee : "+f.Target);
                    log(f.SourceName+" : verifie.");
                }
                r.Status="completed";Json.Write(manifest,r);log("Operation terminee. Journal : "+manifest);return manifest;
            } catch(Exception failure) {
                bool restored=true;
                foreach(var f in written.AsEnumerable().Reverse())try {
                    // Only undo this operation's own bytes, never an intervening third-party modification.
                    string current=File.Exists(f.Target)?Hash.FileHash(f.Target):"";
                    if(current!=f.After&&current!=f.Before)throw new IOException("Fichier modifie par un autre programme.");
                    if(current==f.Before)continue;
                    if(f.Before=="") { if(File.Exists(f.Target))File.Delete(f.Target); }
                    else Files.Atomic(f.Target,File.ReadAllBytes(f.Backup));
                } catch { restored=false; }
                r.Status=restored?"rolled_back":"prepared";Json.Write(manifest,r);
                throw new IOException(failure.Message+Environment.NewLine+(restored?"Les fichiers deja modifies ont ete remis dans leur etat precedent.":"Restauration incomplete. Conserver le journal et les sauvegardes : "+manifest),failure);
            }
        }
        public string[] Records() {
            string root=Path.Combine(State,"backups");if(!Directory.Exists(root))return new string[0];
            Files.NoLinks(root);
            return Directory.GetDirectories(root).Select(d=>Files.Within(root,Path.Combine(d,"installation.json"))).Where(File.Exists).OrderByDescending(x=>x,StringComparer.Ordinal).ToArray();
        }
        public Record ValidRecord(string manifest) {
            Files.Within(Path.Combine(State,"backups"),manifest);
            var r=Json.Read<Record>(manifest);
            if(r==null||r.Files==null||r.Files.Count!=4||!new[]{"prepared","completed","rolled_back","restored"}.Contains(r.Status))throw new IOException("Journal d'installation invalide.");
            var seen=new HashSet<string>(StringComparer.OrdinalIgnoreCase);
            foreach(var f in r.Files) {
                string target=Files.Full(f.Target), name=Path.GetFileName(target);
                string mc=Path.Combine(Files.Full(r.Minecraft),"mods"),rt=Path.Combine(Files.Full(r.RtData),"UnityModManager","VoidDynastyBridge");
                if(target.EndsWith(".jar",StringComparison.OrdinalIgnoreCase)) {
                    Files.Within(mc,target);if(!Files.Same(Path.GetDirectoryName(target),mc))throw new IOException("Destination Minecraft invalide.");
                } else if((name!="Info.json"&&name!="VoidDynastyBridge.dll")||!Files.Same(Path.GetDirectoryName(target),rt))throw new IOException("Destination Rogue Trader invalide.");
                else Files.Within(rt,target);
                if(!seen.Add(target))throw new IOException("Destination en double dans le journal.");
                if(!ValidHash(f.Before)||!ValidHash(f.After))throw new IOException("Empreinte du journal invalide.");
                if(f.Before!="") {
                    Files.Within(Path.GetDirectoryName(manifest),f.Backup);
                    if(!File.Exists(f.Backup)||Hash.FileHash(f.Backup)!=f.Before)throw new IOException("Sauvegarde de mod manquante ou modifiee.");
                }
                // A forged journal must not make an unrelated JAR an uninstall target.
                if(target.EndsWith(".jar",StringComparison.OrdinalIgnoreCase)) {
                    var data=f.Before!=""?Mod(f.Backup):null;
                    if(File.Exists(target))data=Mod(target);
                    if(data!=null && Json.Text(data,"id")!=(f.Managed?"voiddynasty_bridge":"voiddynasty"))throw new IOException("Le journal cible un autre mod.");
                    if(data==null&&(File.Exists(target)||f.Before!=""))throw new IOException("Identite du mod non verifiable.");
                } else if(!f.Managed)throw new IOException("Fichier Rogue Trader non gere dans le journal.");
            }
            if(r.Files.Count(f=>!f.Managed)!=1||r.Files.Count(f=>f.Target.EndsWith(".jar",StringComparison.OrdinalIgnoreCase))!=2)throw new IOException("Liste de fichiers geree invalide.");
            return r;
        }
        public string Maintain(string manifest,bool uninstall,Action<string> log) {
            Closed();var r=ValidRecord(manifest);
            if(r.Status=="rolled_back"||r.Status=="restored")throw new IOException("Cette operation a deja ete annulee.");
            if(uninstall&&r.Operation!="install")throw new IOException("Choisir un journal d'installation pour desinstaller.");
            var changes=new List<Change>();
            foreach(var f in r.Files) {
                string current=File.Exists(f.Target)?Hash.FileHash(f.Target):"";
                if(current!=f.After && !(r.Status=="prepared"&&current==f.Before))throw new IOException("Le fichier a change depuis cette operation. Aucune modification : "+f.Target);
                byte[] data=uninstall?(f.Managed?null:(File.Exists(f.Target)?File.ReadAllBytes(f.Target):null)):(f.Before==""?null:File.ReadAllBytes(f.Backup));
                changes.Add(new Change{Target=f.Target,Data=data,Managed=f.Managed,SourceName=uninstall?"Retrait du pont (original conserve)":"Restauration du fichier precedent"});
            }
            string dir=NewDirectory();var next=NewRecord(new Options{Minecraft=r.Minecraft,RtData=r.RtData},uninstall?"uninstall":"restore");
            string result=Apply(changes,next,dir,log);
            r.Status="restored";Json.Write(manifest,r);return result;
        }
        private static bool ValidHash(string h) { return h!=null&&(h==""||(h.Length==64&&h.All(c=>"0123456789abcdef".Contains(c)))); }
    }
}
