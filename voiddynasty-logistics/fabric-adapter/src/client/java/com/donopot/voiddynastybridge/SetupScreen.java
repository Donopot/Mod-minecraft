package com.donopot.voiddynastybridge;

import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** A guide over the existing server-validated actions; never changes a pair or receipt itself. */
public final class SetupScreen extends Screen {
    private Button create,pair,open;
    public SetupScreen(){super(Component.literal("VOID DYNASTY | Assistant"));}
    @Override public boolean isPauseScreen(){return false;}
    private void action(String name){
        var mc=Minecraft.getInstance();var server=mc.getSingleplayerServer();
        if(server==null||mc.player==null)return;UUID player=mc.player.getUUID();
        server.execute(()->{var service=BridgeMod.service;if(service!=null&&service.server==server)service.action(player,name);});
    }
    private Button button(String text,int col,int y,Runnable action){return addRenderableWidget(Button.builder(Component.literal(text),b->action.run()).bounds(width/2-150+col*154,y,146,20).build());}
    @Override protected void init(){
        int y=height-74;
        create=button("Creer le depot",0,y,()->action("create"));
        pair=button("Demander le lien RT",1,y,()->action("pair"));
        open=button("Deposer les materiaux",0,y+24,()->{action("open");onClose();});
        button("Ouvrir le terminal",1,y+24,()->Minecraft.getInstance().setScreenAndShow(new DeliveryScreen()));
        button("Fermer",1,y+48,this::onClose);
    }
    @Override public void tick(){
        var service=BridgeMod.service;
        if(service==null){create.active=pair.active=open.active=false;return;}
        var v=service.view();boolean safe=!v.phase().equals("RECOVERY_REQUIRED");
        create.active=safe&&!v.hasCrate();pair.active=safe&&v.hasCrate()&&!v.paired()&&v.connected();
        open.active=safe&&v.hasCrate()&&(v.phase().equals("IDLE")||v.phase().equals("SETTLED"));
    }
    @Override public void extractRenderState(GuiGraphicsExtractor g,int mouseX,int mouseY,float delta){
        g.fill(0,0,width,height,0xF010151F);g.centeredText(font,title,width/2,12,0xFFF0D796);
        var service=BridgeMod.service;int x=Math.max(12,width/2-220),w=Math.min(440,width-24);
        String heading="Charger un monde solo local",body="Le pont fonctionne avec un monde solo sans ouverture LAN et Rogue Trader sur le meme PC.";
        String detail="";
        if(service!=null){var v=service.view();detail=v.availability()+" | "+v.status();
            if(v.phase().equals("RECOVERY_REQUIRED")){heading="Reprise necessaire";body="Conserver le depot et les sauvegardes. Recharger un checkpoint monde / campagne correspondant. Ne pas refaire l'association pour contourner ce blocage.";}
            else if(!v.connected()){heading="1 / 4 - Charger la campagne RT";body="Dans Rogue Trader : Ctrl+F10, panneau VOID DYNASTY. Premiere utilisation : Preparer cette campagne. Pour une partie deja liee, charger sa derniere sauvegarde REWARD_COMMITTED.";}
            else if(!v.hasCrate()){heading="2 / 4 - Installer le depot";body="Se placer devant un espace libre puis cliquer Creer le depot. Ce depot reste fixe dans ce monde. Votre campagne RT doit etre preparee avant de demander le lien.";}
            else if(!v.paired()){heading="3 / 4 - Associer ce monde";body="Cliquer Demander le lien RT. Dans le panneau VOID DYNASTY de Rogue Trader, verifier le monde demandeur et approuver. Attendre la confirmation de sauvegarde. Monde : "+v.world();}
            else if(v.nativeCount()==0){heading="4 / 4 - Faire une livraison";body="Ouvrir le terminal, choisir une mission, puis deposer les materiaux demandes. Cliquer Expedier puis Confirmer. La cargaison native sera sauvegardee dans RT avant la confirmation.";}
            else {heading="Le pont est pret";body="Choisir une mission dans le terminal pour continuer. Pour reprendre plus tard, charger ce monde et la derniere sauvegarde RT REWARD_COMMITTED correspondante. Cargaisons natives livrees : "+v.nativeCount();}
        }
        g.text(font,heading,x,34,0xFFE2E6EE);
        g.textWithWordWrap(font,Component.literal(body),x,52,w,0xFFE2E6EE);
        if(height>=290)g.textWithWordWrap(font,Component.literal(detail),x,118,w,0xFFF0D796);
        super.extractRenderState(g,mouseX,mouseY,delta);
    }
}
