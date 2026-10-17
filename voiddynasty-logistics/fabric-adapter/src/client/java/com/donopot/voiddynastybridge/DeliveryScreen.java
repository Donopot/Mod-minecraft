package com.donopot.voiddynastybridge;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Presentation reads immutable views; buttons enqueue validated server actions. */
public final class DeliveryScreen extends Screen {
    private boolean confirm;
    private Button create,open,pair,ship,resume;
    private Button previousMission,nextMission;
    private String selected="";
    private String confirmedMission="";
    public DeliveryScreen(){super(Component.literal("VOID DYNASTY | Livraison"));}
    @Override public boolean isPauseScreen(){return false;}
    private void action(String name){
        var local=Minecraft.getInstance();var server=local.getSingleplayerServer();
        if(server==null||local.player==null)return;UUID player=local.player.getUUID();
        server.execute(()->{var service=BridgeMod.service;if(service!=null&&service.server==server)service.action(player,name);});
    }
    private Button button(String text,int column,int y,Runnable action){return addRenderableWidget(Button.builder(Component.literal(text),b->action.run()).bounds(width/2-150+column*154,y,146,20).build());}
    @Override protected void init(){
        int y=Math.max(144,height-98);
        previousMission=button("Mission precedente",0,y,()->{confirm=false;action("previous_mission");});
        nextMission=button("Mission suivante",1,y,()->{confirm=false;action("next_mission");});
        y+=24;
        create=button("Creer le depot",0,y,()->action("create"));
        open=button("Ouvrir le depot",1,y,()->{action("open");onClose();});
        pair=button("Lier la campagne",0,y+24,()->action("pair"));
        ship=button("Expedier",1,y+24,()->{if(!confirm){var service=BridgeMod.service;if(service==null)return;confirmedMission=service.view().missionId();confirm=true;ship.setMessage(Component.literal("Confirmer la livraison"));}else{confirm=false;ship.setMessage(Component.literal("Expedier"));action("ship:"+confirmedMission);}});
        resume=button("Reprendre / recu",0,y+48,()->action("resume"));
        button("Fermer",1,y+48,this::onClose);
        addRenderableWidget(Button.builder(Component.literal("?"),b->Minecraft.getInstance().setScreenAndShow(new SetupScreen())).bounds(width-30,8,22,20).build());
    }
    @Override public void tick(){
        var service=BridgeMod.service;if(service==null){create.active=open.active=pair.active=ship.active=resume.active=false;return;}
        var v=service.view();create.active=!v.hasCrate()&&!v.phase().equals("RECOVERY_REQUIRED");open.active=v.hasCrate()&&(v.phase().equals("IDLE")||v.phase().equals("DRAFT")||v.phase().equals("SETTLED"));
        previousMission.active=nextMission.active=v.canSelect();
        if(!selected.equals(v.mission())){selected=v.mission();confirm=false;}
        pair.active=v.hasCrate()&&!v.paired()&&v.connected();ship.active=v.canShip();resume.active=v.paired()&&(v.phase().equals("DRAFT")||v.phase().equals("RESERVED"));
        if(!v.canShip())confirm=false;
        if(!confirm)ship.setMessage(Component.literal("Expedier ("+(v.completed()+1)+")"));
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){
        g.fill(0,0,width,height,0xEE10151F);g.centeredText(font,title,width/2,18,0xFFF0D796);
        var service=BridgeMod.service;
        if(service==null)g.centeredText(font,"Charger un monde solo local, sans ouverture LAN",width/2,52,0xFFFFCC88);
        else{
            var v=service.view();int x=Math.max(12,width/2-220),y=40;
            String[] lines={"Mission : "+v.mission(),"Monde : "+v.world(),"Deposes : "+v.iron()+" / "+v.required()+" | Reserves : "+v.reserved(),"Cargaisons RT livrees : "+v.nativeCount()+" | Logistique : "+(v.reserve()<0?"--":v.reserve()),"Livraisons : "+v.completed()+" | "+v.phase()+" | "+(v.connected()?"Connecte":"Deconnecte"),v.availability(),"Campagne RT : "+(v.campaign().length()>8?v.campaign().substring(0,8):v.campaign())+" / "+v.zone()};
            for(String line:lines){g.text(font,line,x,y,0xFFE2E6EE);y+=12;}
            g.textWithWordWrap(font,Component.literal(v.status()),x,y,Math.min(440,width-24),0xFFF0D796);
        }
        super.extractRenderState(g,mouseX,mouseY,delta);
    }
}
