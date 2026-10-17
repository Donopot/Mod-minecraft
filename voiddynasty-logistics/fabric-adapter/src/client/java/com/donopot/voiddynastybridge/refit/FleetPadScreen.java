package com.donopot.voiddynastybridge.refit;
import java.util.*;
import com.donopot.voiddynasty.ControlPadScreen;
import com.donopot.voiddynasty.network.*;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Uses the original fleet receiver and commands; no fleet state is rewritten by this screen. */
public final class FleetPadScreen extends Screen {
    private List<ShipInfo> fleet=List.of();
    private String selected="",status="Lecture de la flotte...",pending="",draft="";
    private int tab,page,type;
    private boolean listRequested;
    private EditBox name;
    private PadLayout layout;
    private static final String[] TYPES={"frigate","emperor","ruin"};
    private static final String[] LABELS={"Frégate","Emperor","Ruin"};
    public FleetPadScreen(){super(Component.literal("VOID DYNASTY | Flotte"));}
    private Button button(String label,int x,int y,int w,Runnable action){return addRenderableWidget(Button.builder(Component.literal(label),b->action.run()).bounds(x,y,w,20).build());}
    private void rebuild(){if(name!=null)draft=name.getValue();name=null;clearWidgets();init();}
    private ShipInfo selected(){return fleet.stream().filter(s->s.name().equals(selected)).findFirst().orElse(null);}
    private void request(){if(Minecraft.getInstance().player!=null)ClientPlayNetworking.send(new FleetListRequestPayload());}
    @Override protected void init(){
        layout=PadLayout.of(width,height);int x=layout.left(),w=layout.width();
        String[] tabs={"Flotte","Commandes","Construction"};
        for(int i=0;i<3;i++){final int t=i;var b=button(tabs[i],x+i*(w/3),34,w/3-4,()->{tab=t;pending="";rebuild();});b.active=i!=tab;}
        if(tab==0){
            int start=page*layout.rows();
            for(int i=0;i<layout.rows()&&start+i<fleet.size();i++){
                var ship=fleet.get(start+i);String label=(ship.name().equals(selected)?"> ":"")+ship.name()+" | "+ship.typeDisplay()+" | "+(ship.deployed()?"Déployé":"Rangé");
                button(font.plainSubstrByWidth(label,w-16),x,66+i*22,w,()->{selected=ship.name();tab=1;pending="";rebuild();});
            }
            int y=layout.footer();button("< Précédente",x,y,layout.columnWidth(),()->{page=Math.max(0,page-1);rebuild();}).active=page>0;
            button("Suivante >",x+layout.columnWidth()+12,y,layout.columnWidth(),()->{page++;rebuild();}).active=(page+1)*layout.rows()<fleet.size();
        }else if(tab==1){
            var ship=selected();int cw=layout.columnWidth();
            var deploy=button("Déployer",x,96,cw,()->command("deploy",false));deploy.active=ship!=null&&!ship.deployed();
            var stow=button(pending.equals("stow")?"Confirmer le rangement":"Ranger",x+cw+12,96,cw,()->command("stow",true));stow.active=ship!=null&&ship.deployed();
            var teleport=button(pending.equals("teleport")?"Confirmer le déplacement":"Déplacer ici (X/Z)",x,120,cw,()->command("teleport",true));teleport.active=ship!=null&&ship.deployed();
            var remove=button(pending.equals("delete")?"Confirmer la suppression":"Supprimer",x+cw+12,120,cw,()->command("delete",true));remove.active=ship!=null;
            name=new EditBox(font,x,146,cw,20,Component.literal("Nouveau nom"));name.setMaxLength(32);name.setValue(draft);addRenderableWidget(name);
            button("Renommer",x+cw+12,146,cw,()->command("rename",false)).active=ship!=null;
        }else{
            int cw=layout.columnWidth();name=new EditBox(font,x,96,w,20,Component.literal("Nom du vaisseau"));name.setMaxLength(32);name.setValue(draft);addRenderableWidget(name);
            button("Type : "+LABELS[type],x,124,cw,()->{type=(type+1)%TYPES.length;rebuild();});
            button("Ajouter à la flotte",x+cw+12,124,cw,()->command("add",false));
        }
        button("Actualiser",x,height-26,layout.columnWidth(),this::request);
        button("Fermer",x+layout.columnWidth()+12,height-26,layout.columnWidth(),this::onClose);
        if(!listRequested){listRequested=true;request();}
    }
    public void updateShips(List<ShipInfo> ships){Minecraft.getInstance().execute(()->{
        fleet=List.copyOf(ships);if(selected()==null){selected="";pending="";}
        if(selected.isEmpty()&&!fleet.isEmpty())selected=fleet.getFirst().name();
        page=Math.min(page,Math.max(0,(fleet.size()-1)/Math.max(1,PadLayout.of(width,height).rows())));
        status=fleet.size()+" vaisseau(x) — sélection et commandes propres à ce joueur";rebuild();
    });}
    @Override public void onClose(){var old=ControlPadScreen.getInstance();if(old!=null)old.onClose();else super.onClose();}
    private void command(String verb,boolean confirm){
        var mc=Minecraft.getInstance();if(mc.player==null)return;var ship=selected();
        if(!verb.equals("add")&&(ship==null||!PadLayout.name(ship.name()))){status="Sélectionner un vaisseau au nom valide.";return;}
        if(confirm&&!pending.equals(verb)){pending=verb;status="Vérifier le vaisseau sélectionné, puis confirmer l'action.";rebuild();return;}
        String value=name==null?draft:name.getValue().trim().replace(' ','_');
        if((verb.equals("add")||verb.equals("rename"))&&!PadLayout.name(value)){status="Nom : 1 à 32 lettres, chiffres, _ ou -.";return;}
        String command=switch(verb){case "add"->"fleet add "+TYPES[type]+" "+value;case "rename"->"fleet rename "+ship.name()+" "+value;case "teleport"->"fleet teleport "+ship.name()+" "+mc.player.getBlockX()+" "+mc.player.getBlockZ();default->"fleet "+verb+" "+ship.name();};
        mc.player.connection.sendCommand(command);if(verb.equals("rename")||verb.equals("add"))selected=value;pending="";status="Commande envoyée : "+verb+". Le résultat est affiché dans le chat.";
        request();rebuild();
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mx,int my,float delta){
        g.fill(0,0,width,height,0xF010151F);g.fill(layout.left()-8,28,layout.left()+layout.width()+8,height-30,0xFF192330);
        g.centeredText(font,"VOID DYNASTY | Commandement de flotte",width/2,13,0xFFF0D796);
        int x=layout.left();var ship=selected();
        if(tab==0&&fleet.isEmpty())g.text(font,"Aucun vaisseau — ouvrir Construction pour en ajouter.",x,70,0xFFE2E6EE);
        if(tab==0)g.text(font,"Page "+(page+1)+" / "+Math.max(1,(fleet.size()+layout.rows()-1)/layout.rows()),x,height-68,0xFFBBC6D2);
        if(tab==1){g.text(font,font.plainSubstrByWidth(ship==null?"Choisir un vaisseau dans Flotte":ship.name()+" | "+ship.typeDisplay(),layout.width()),x,64,0xFFF0D796);
            if(ship!=null)g.text(font,ship.deployed()?"Position : "+ship.x()+", "+ship.y()+", "+ship.z():"Vaisseau rangé",x,78,0xFFBBC6D2);}
        if(tab==2)g.text(font,"Nom et classe du nouveau vaisseau",x,76,0xFFBBC6D2);
        if(tab!=0)g.text(font,font.plainSubstrByWidth(status,layout.width()),x,height-64,0xFFF0D796);
        super.extractRenderState(g,mx,my,delta);
    }
}
