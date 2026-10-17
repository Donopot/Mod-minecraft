using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Diagnostics;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Threading;
using System.Windows.Forms;

namespace VoidDynasty.Setup {
    internal static class Program {
        [STAThread] private static int Main(string[] args) {
            Application.EnableVisualStyles();Application.SetCompatibleTextRenderingDefault(false);
            bool created;
            using(var mutex=new Mutex(true,"Local\\VoidDynastySetup",out created)) {
                if(!created) { MessageBox.Show("L'assistant VOID DYNASTY est deja ouvert.");return 1; }
                try {
                    var bundle=Bundle.Load();
                    string state=Path.Combine(Environment.GetFolderPath(Environment.SpecialFolder.LocalApplicationData),"VoidDynastyBridge","Installer");
                    if(args.Length>0&&args[0]=="--inspect") {
                        var o=Wizard.Defaults(state);
                        for(int i=1;i+1<args.Length;i+=2) {
                            switch(args[i]) {case "--minecraft":o.Minecraft=args[i+1];break;case "--game":o.Game=args[i+1];break;case "--original":o.Original=args[i+1];break;case "--rt-data":o.RtData=args[i+1];break;default:throw new ArgumentException("Option inconnue.");}
                        }
                        var result=new Engine(bundle,state,Engine.GamesRunning).Inspect(o);
                        Console.WriteLine(result.Display());
                        if(args.Contains("--inspect"))Files.Atomic(Path.Combine(Path.GetTempPath(),"void-dynasty-setup-check.txt"),System.Text.Encoding.UTF8.GetBytes(result.Display()));
                        return result.Errors.Count==0?0:2;
                    }
                    using(var wizard=new Wizard(bundle,state)) {
                        if(args.Length==2&&args[0]=="--render-preview") { wizard.RenderPreview(args[1]);return 0; }
                        Application.Run(wizard);
                    }
                    return 0;
                } catch(Exception e) { MessageBox.Show(e.Message,"VOID DYNASTY — erreur",MessageBoxButtons.OK,MessageBoxIcon.Error);return 1; }
            }
        }
    }
    public sealed class Wizard : Form {
        private readonly Engine engine;
        private readonly string state;
        private readonly TabControl tabs=new TabControl();
        private readonly Button back=new Button(),next=new Button(),install=new Button(),check=new Button();
        private TextBox minecraft,game,rtData,original,checks,preview,output;
        private ComboBox worlds,saves,history;
        private CheckBox backup;
        private Label result;
        private bool busy;
        private bool rendering;
        private Options reviewed;
        private BackgroundWorker worker;
        public Wizard(Bundle bundle,string stateRoot) {
            state=stateRoot;engine=new Engine(bundle,state,Engine.GamesRunning);
            Text="VOID DYNASTY — Assistant "+bundle.Version;ClientSize=new Size(980,710);MinimumSize=new Size(930,720);
            StartPosition=FormStartPosition.CenterScreen;Font=new Font("Segoe UI",10);AutoScaleMode=AutoScaleMode.Dpi;
            var heading=new Label{Text="VOID DYNASTY",Font=new Font("Segoe UI",22,FontStyle.Bold),ForeColor=Color.FromArgb(218,190,126),Dock=DockStyle.Top,Height=62,Padding=new Padding(20,12,0,0),BackColor=Color.FromArgb(24,31,43)};
            tabs.Dock=DockStyle.Fill;tabs.Padding=new Point(12,8);
            foreach(string title in new[]{"1  Bienvenue","2  Dossiers et copies","3  Vérification","4  Installation","5  Jouer et entretien"})tabs.TabPages.Add(new TabPage(title){BackColor=Color.White,Padding=new Padding(20),AutoScroll=true});
            var footer=new FlowLayoutPanel{Dock=DockStyle.Bottom,Height=56,Padding=new Padding(18,7,18,7),FlowDirection=FlowDirection.RightToLeft,WrapContents=false};
            back.Text="Précédent";back.Size=new Size(115,34);back.Click+=(s,e)=>{if(tabs.SelectedIndex>0)tabs.SelectedIndex--;};
            next.Text="Suivant";next.Size=new Size(140,34);next.Click+=(s,e)=>{if(tabs.SelectedIndex<4)tabs.SelectedIndex++;};
            footer.Controls.AddRange(new Control[]{next,back});Controls.Add(tabs);Controls.Add(footer);Controls.Add(heading);
            BuildWelcome();BuildPaths();BuildCheck();BuildInstall();BuildPlay();
            tabs.SelectedIndexChanged+=(s,e)=> { back.Enabled=tabs.SelectedIndex>0&&!busy;next.Enabled=tabs.SelectedIndex<4&&!busy; if(tabs.SelectedIndex==2)ValidateInstall();if(tabs.SelectedIndex==4)RefreshHistory(); };
            var defaults=Defaults(state);minecraft.Text=defaults.Minecraft;game.Text=defaults.Game;rtData.Text=defaults.RtData;original.Text=defaults.Original;
            RefreshSaves();SelectPath(worlds,defaults.World);SelectPath(saves,defaults.Save);backup.Checked=defaults.BackupSaves;
            foreach(var path in new[]{minecraft,game,rtData,original})path.TextChanged+=(s,e)=>InvalidateReview();
            worlds.SelectedIndexChanged+=(s,e)=>InvalidateReview();saves.SelectedIndexChanged+=(s,e)=>InvalidateReview();backup.CheckedChanged+=(s,e)=>InvalidateReview();
            back.Enabled=false;
            FormClosing+=(s,e)=> { if(busy) { e.Cancel=true;MessageBox.Show(this,"L'operation est en cours. Attendre sa fin pour conserver les sauvegardes et le journal.",Text); } };
        }
        public static Options Defaults(string state) {
            string app=Environment.GetFolderPath(Environment.SpecialFolder.ApplicationData);
            string user=Environment.GetFolderPath(Environment.SpecialFolder.UserProfile);
            var o=new Options{Minecraft=Path.Combine(app,".minecraft"),RtData=Path.Combine(user,"AppData","LocalLow","Owlcat Games","Warhammer 40000 Rogue Trader")};
            string roamingSibling=Path.Combine(Path.GetDirectoryName(app),"LocalLow","Owlcat Games","Warhammer 40000 Rogue Trader");
            if(!Directory.Exists(o.RtData)&&Directory.Exists(roamingSibling))o.RtData=roamingSibling;
            string prefs=Path.Combine(state,"preferences.json");
            if(File.Exists(prefs))try { o=Json.Read<Options>(prefs); } catch { }
            if(o.Game=="") {
                var roots=new[]{Environment.GetFolderPath(Environment.SpecialFolder.ProgramFilesX86),Environment.GetFolderPath(Environment.SpecialFolder.ProgramFiles)};
                foreach(string root in roots) {
                    string p=Path.Combine(root,"Steam","steamapps","common","Warhammer 40000 Rogue Trader");if(File.Exists(Path.Combine(p,"WH40KRT.exe"))) { o.Game=p;break; }
                }
            }
            if(o.Original=="")try {
                string mods=Path.Combine(o.Minecraft,"mods");
                if(Directory.Exists(mods))foreach(string f in Directory.GetFiles(mods,"*.jar"))if(Json.Text(Engine.Mod(f),"id")=="voiddynasty") { o.Original=f;break; }
            } catch { }
            return o;
        }
        private static Label LabelAt(Control parent,string text,int x,int y,int w,int h,bool bold) {
            var l=new Label{Text=text,AutoSize=false,Location=new Point(x,y),Size=new Size(w,h),Font=new Font("Segoe UI",bold?12:10,bold?FontStyle.Bold:FontStyle.Regular)};parent.Controls.Add(l);return l;
        }
        private static TextBox TextAt(Control p,int y,int h) {
            var t=new TextBox{Multiline=true,ReadOnly=true,ScrollBars=ScrollBars.Vertical,BorderStyle=BorderStyle.FixedSingle,Location=new Point(20,y),Size=new Size(880,h),Anchor=AnchorStyles.Left|AnchorStyles.Top|AnchorStyles.Right,BackColor=Color.White};p.Controls.Add(t);p.SizeChanged+=(s,e)=>t.Width=Math.Max(200,p.ClientSize.Width-40);return t;
        }
        private void InvalidateReview() {
            reviewed=null;install.Enabled=false;preview.Text="Les choix ont change. Revenir a Vérification avant d'installer.";
        }
        private void BuildWelcome() {
            var p=tabs.TabPages[0];LabelAt(p,"Installez le pont entre vos deux jeux",20,22,860,40,true);
            var body=TextAt(p,78,350);body.Text="Cet assistant installe les deux modules du pont et vous accompagne jusqu'a la première livraison.\r\n\r\nAvant de commencer\r\n• Fermez Minecraft et Rogue Trader.\r\n• Preparez Minecraft 26.3 avec Fabric 0.19.5, Fabric API 0.162.0+26.3 et GeckoLib 5.5.7.\r\n• Conservez votre JAR VOID DYNASTY 1.0.0 original : il n'est pas fourni ici.\r\n• Rogue Trader cible : installation 1.6.1.514 validee, avec son chargeur UMM fourni par le jeu.\r\n\r\nLes anciens fichiers de mods sont sauvegardes automatiquement. Vous pouvez aussi selectionner un monde et une sauvegarde RT a copier. Les parties et leur association ne sont jamais modifiees par cet assistant.\r\n\r\nCette alpha fonctionne en solo local, avec les deux jeux sur le meme PC et le meme compte Windows. Les 14 recompenses natives restent en cours de validation en jeu.";
            LabelAt(p,"Pas de Python, de service externe ni de compte supplementaire.",20,450,880,42,true);
        }
        private TextBox PathRow(Control p,string title,int y,bool file) {
            LabelAt(p,title,20,y,880,24,false);
            var text=new TextBox{Location=new Point(20,y+26),Size=new Size(750,28),Anchor=AnchorStyles.Left|AnchorStyles.Top|AnchorStyles.Right};p.Controls.Add(text);
            var browse=new Button{Text="Parcourir…",Location=new Point(784,y+24),Size=new Size(116,32),Anchor=AnchorStyles.Right|AnchorStyles.Top};p.Controls.Add(browse);
            p.SizeChanged+=(s,e)=> { text.Width=Math.Max(200,p.ClientSize.Width-170);browse.Left=p.ClientSize.Width-136; };
            browse.Click+=(s,e)=> {
                if(file)using(var d=new OpenFileDialog{Title=title,Filter="Mod Minecraft (*.jar)|*.jar",CheckFileExists=true}) { if(d.ShowDialog(this)==DialogResult.OK)text.Text=d.FileName; }
                else using(var d=new FolderBrowserDialog{Description=title,SelectedPath=Directory.Exists(text.Text)?text.Text:"",ShowNewFolderButton=false}) { if(d.ShowDialog(this)==DialogResult.OK)text.Text=d.SelectedPath; }
                if(text==minecraft||text==rtData)RefreshSaves();
            };
            return text;
        }
        private void BuildPaths() {
            var p=tabs.TabPages[1];
            minecraft=PathRow(p,"Instance Minecraft (dossier contenant mods et versions)",12,false);
            game=PathRow(p,"Installation Rogue Trader (dossier contenant WH40KRT.exe)",78,false);
            rtData=PathRow(p,"Données Rogue Trader (dossier contenant Saved Games et UnityModManager)",144,false);
            original=PathRow(p,"Mod VOID DYNASTY original 1.0.0 (.jar)",210,true);
            backup=new CheckBox{Text="Copier les parties selectionnees avant l'installation",Checked=true,Location=new Point(20,282),Size=new Size(760,28)};p.Controls.Add(backup);
            LabelAt(p,"Monde Minecraft a copier",20,318,410,24,false);LabelAt(p,"Sauvegarde Rogue Trader a copier",465,318,435,24,false);
            worlds=new ComboBox{DropDownStyle=ComboBoxStyle.DropDownList,Location=new Point(20,345),Size=new Size(420,30)};
            saves=new ComboBox{DropDownStyle=ComboBoxStyle.DropDownList,Location=new Point(465,345),Size=new Size(435,30),Anchor=AnchorStyles.Left|AnchorStyles.Top|AnchorStyles.Right};p.Controls.AddRange(new Control[]{worlds,saves});
            p.SizeChanged+=(s,e)=>saves.Width=Math.Max(200,p.ClientSize.Width-485);
            var refresh=new Button{Text="Relire les parties",Location=new Point(20,390),Size=new Size(175,34)};refresh.Click+=(s,e)=>RefreshSaves();p.Controls.Add(refresh);
            LabelAt(p,"Choisissez une paire correspondante. L'installeur copie ces fichiers sans valider leur association.\r\nPour une nouvelle partie, laissez les deux selections vides.",20,440,880,64,false);
        }
        private sealed class Choice {
            public string Path,Title;
            public override string ToString(){return Title;}
        }
        private void RefreshSaves() {
            string oldWorld=Chosen(worlds),oldSave=Chosen(saves);
            worlds.Items.Clear();saves.Items.Clear();worlds.Items.Add(new Choice{Path="",Title="Aucun monde sélectionné"});saves.Items.Add(new Choice{Path="",Title="Aucune sauvegarde sélectionnée"});
            try {
                string root=Path.Combine(minecraft.Text,"saves");
                if(Directory.Exists(root))foreach(string d in Directory.GetDirectories(root).OrderBy(x=>x))if(File.Exists(Path.Combine(d,"level.dat")))worlds.Items.Add(new Choice{Path=d,Title=Path.GetFileName(d)});
                root=Path.Combine(rtData.Text,"Saved Games");
                if(Directory.Exists(root))foreach(string f in Directory.GetFiles(root,"*.zks").OrderByDescending(File.GetLastWriteTimeUtc))saves.Items.Add(new Choice{Path=f,Title=Path.GetFileNameWithoutExtension(f)});
            } catch { }
            SelectPath(worlds,oldWorld);SelectPath(saves,oldSave);
        }
        private static string Chosen(ComboBox box) { return box!=null&&box.SelectedItem is Choice?((Choice)box.SelectedItem).Path:""; }
        private static void SelectPath(ComboBox box,string path) { box.SelectedIndex=0;for(int i=1;i<box.Items.Count;i++)if(string.Equals(((Choice)box.Items[i]).Path,path,StringComparison.OrdinalIgnoreCase)) { box.SelectedIndex=i;break; } }
        private Options Options() { return new Options{Minecraft=minecraft.Text.Trim(),Game=game.Text.Trim(),RtData=rtData.Text.Trim(),Original=original.Text.Trim(),World=Chosen(worlds),Save=Chosen(saves),BackupSaves=backup.Checked}; }
        private void BuildCheck() {
            var p=tabs.TabPages[2];LabelAt(p,"Vérifier avant de copier",20,12,880,32,true);
            check.Text="Vérifier à nouveau";check.SetBounds(20,55,210,34);check.Click+=(s,e)=>ValidateInstall();p.Controls.Add(check);
            checks=TextAt(p,105,390);checks.Text="Renseignez les dossiers, puis lancez la verification.";
        }
        private void ValidateInstall() {
            if(busy)return;
            reviewed=null;install.Enabled=false;var o=Options();
            var c=engine.Inspect(o);checks.Text=c.Display();
            if(c.Errors.Count==0) {
                reviewed=o;preview.Text=engine.Preview(o)+"\r\n\r\nSauvegardes conservees dans :\r\n"+Path.Combine(state,"backups")+"\r\n\r\nLes deux modules seront installes ensemble. Aucun jeu n'est lance automatiquement.";
                install.Enabled=true;
            } else preview.Text="Revenez a Vérification et corrigez les points bloquants avant d'installer.";
        }
        private void BuildInstall() {
            var p=tabs.TabPages[3];LabelAt(p,"Installation / mise à jour / réparation",20,12,880,32,true);
            preview=TextAt(p,54,242);preview.Text="Verifier les dossiers a l'etape precedente.";
            install.Text="Sauvegarder et installer";install.SetBounds(20,310,260,38);install.Enabled=false;
            install.Click+=(s,e)=> {
                if(reviewed==null)return;var snapshot=reviewed;
                Run(log=> { var path=engine.Install(snapshot,log);Json.Write(Path.Combine(state,"preferences.json"),snapshot);return path; },"Installation terminée. Ouvrez Jouer et entretien pour la suite.");
            };p.Controls.Add(install);
            output=TextAt(p,365,105);result=LabelAt(p,"",20,480,880,50,true);
        }
        private void Run(Func<Action<string>,string> operation,string success) {
            if(busy)return;busy=true;tabs.Enabled=false;back.Enabled=next.Enabled=false;UseWaitCursor=true;result.Text="Opération en cours…";output.Clear();
            worker=new BackgroundWorker{WorkerReportsProgress=true};
            worker.ProgressChanged+=(s,e)=>output.AppendText(Convert.ToString(e.UserState)+Environment.NewLine);
            worker.DoWork+=(s,e)=> { e.Result=operation(line=>worker.ReportProgress(0,line)); };
            worker.RunWorkerCompleted+=(s,e)=> {
                busy=false;tabs.Enabled=true;UseWaitCursor=false;back.Enabled=tabs.SelectedIndex>0;next.Enabled=tabs.SelectedIndex<4;
                if(e.Error!=null) { result.Text="Opération arrêtée. Consultez le detail.";output.AppendText(e.Error.Message);MessageBox.Show(this,e.Error.Message,"VOID DYNASTY",MessageBoxButtons.OK,MessageBoxIcon.Warning); }
                else { result.Text=success;output.AppendText(Convert.ToString(e.Result));install.Enabled=false;reviewed=null; }
                RefreshHistory();worker.Dispose();worker=null;
            };worker.RunWorkerAsync();
        }
        private void BuildPlay() {
            var p=tabs.TabPages[4];LabelAt(p,"Votre première livraison",20,12,880,30,true);
            var guide=TextAt(p,52,238);guide.Text="1. Lancez Rogue Trader et chargez votre campagne. Ouvrez le panneau VOID DYNASTY avec Ctrl+F10.\r\n2. Premiere association : utilisez le bouton de préparation de campagne. Le test travaille sur des copies et recharge la partie. Une campagne deja associee n'a pas besoin de ce test.\r\n3. Dans le lanceur Minecraft, choisissez le profil Fabric valide puis chargez votre monde solo.\r\n4. Tapez /voidbridge assistant : le guide vous accompagne pour creer le depot, lier la campagne et deposer les materiaux. L'association doit etre approuvee dans RT.\r\n5. Ouvrez le terminal, choisissez une mission, expediez et confirmez. Verifiez la cargaison native dans RT.\r\n\r\nPartie deja associee : rechargez le meme monde et la derniere sauvegarde RT REWARD_COMMITTED correspondante. Gardez les deux modules et les sauvegardes appairees.\r\n\r\nPour retirer le pont d'un monde contenant son depot, conservez d'abord une copie complete : Minecraft peut supprimer les blocs d'un mod absent.";
            LabelAt(p,"Entretien — fichiers de mods uniquement",20,308,880,28,true);
            history=new ComboBox{DropDownStyle=ComboBoxStyle.DropDownList,Location=new Point(20,344),Size=new Size(880,30),Anchor=AnchorStyles.Left|AnchorStyles.Top|AnchorStyles.Right};p.Controls.Add(history);
            p.SizeChanged+=(s,e)=>history.Width=Math.Max(200,p.ClientSize.Width-40);
            var restore=new Button{Text="Restaurer l'état précédent",Location=new Point(20,391),Size=new Size(240,36)};
            var uninstall=new Button{Text="Retirer les deux modules du pont",Location=new Point(275,391),Size=new Size(285,36)};
            var folder=new Button{Text="Ouvrir les sauvegardes",Location=new Point(576,391),Size=new Size(230,36)};
            restore.Click+=(s,e)=>Maintenance(false);uninstall.Click+=(s,e)=>Maintenance(true);
            folder.Click+=(s,e)=> { string d=Path.Combine(state,"backups");if(Directory.Exists(d))Process.Start(new ProcessStartInfo(d){UseShellExecute=true});else MessageBox.Show(this,"Aucune sauvegarde creee par cet assistant."); };
            p.Controls.AddRange(new Control[]{restore,uninstall,folder});
            LabelAt(p,"L'original VOID DYNASTY reste installe lors du retrait du pont. Les parties ne sont jamais restaurees automatiquement. Pour reparer ou mettre a jour, repassez par les etapes 2 a 4.",20,447,880,65,false);
        }
        private void RefreshHistory() {
            if(history==null)return;history.Items.Clear();
            try { foreach(string f in engine.Records()) { var r=Json.Read<Record>(f);history.Items.Add(new Choice{Path=f,Title=r.Created+" | "+r.Operation+" | "+r.Status+" | "+r.Version}); } } catch(Exception e) { output.AppendText(Environment.NewLine+e.Message); }
            if(history.Items.Count>0)history.SelectedIndex=0;
        }
        private void Maintenance(bool uninstall) {
            if(history.SelectedItem==null)return;string path=((Choice)history.SelectedItem).Path;
            try {
                var r=engine.ValidRecord(path);
                string targets=string.Join(Environment.NewLine,r.Files.Where(f=>!uninstall||f.Managed).Select(f=>f.Target));
                string message=(uninstall?"Retirer les modules du pont ? Le mod original sera conserve.":"Restaurer les fichiers de mods tels qu'ils etaient avant cette operation ?")+"\r\n\r\n"+targets+"\r\n\r\nLes sauvegardes de partie restent en place. Les fichiers modifies seront sauvegardes avant l'operation.";
                if(MessageBox.Show(this,message,"VOID DYNASTY — verifier l'operation",MessageBoxButtons.OKCancel,MessageBoxIcon.Question)!=DialogResult.OK)return;
                tabs.SelectedIndex=3;Run(log=>engine.Maintain(path,uninstall,log),uninstall?"Les modules du pont ont ete retires.":"L'etat precedent des fichiers de mods a ete restaure.");
            } catch(Exception e) { MessageBox.Show(this,e.Message,Text,MessageBoxButtons.OK,MessageBoxIcon.Warning); }
        }
        public void RenderPreview(string directory) {
            rendering=true;ShowInTaskbar=false;Opacity=0;StartPosition=FormStartPosition.Manual;Location=new Point(-30000,-30000);
            Directory.CreateDirectory(directory);Show();Application.DoEvents();
            for(int i=0;i<tabs.TabPages.Count;i++) {
                tabs.SelectedIndex=i;PerformLayout();Application.DoEvents();
                foreach(var t in tabs.TabPages[i].Controls.OfType<TextBox>()) { t.SelectionStart=0;t.SelectionLength=0; }
                tabs.Focus();
                using(var bitmap=new Bitmap(Width,Height)) { DrawToBitmap(bitmap,new Rectangle(0,0,Width,Height));bitmap.Save(Path.Combine(directory,"assistant-"+(i+1)+".png")); }
            }
            Hide();
        }
        protected override bool ShowWithoutActivation { get { return rendering; } }
    }
}
