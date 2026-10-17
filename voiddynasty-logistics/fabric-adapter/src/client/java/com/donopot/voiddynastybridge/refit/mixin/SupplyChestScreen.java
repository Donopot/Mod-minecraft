package com.donopot.voiddynastybridge.refit.mixin;
import com.donopot.voiddynasty.menu.SuperChestMenu;
import com.donopot.voiddynasty.network.ScrollContainerPayload;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=com.donopot.voiddynasty.client.SuperChestScreen.class,remap=false)
public abstract class SupplyChestScreen extends AbstractContainerScreen<SuperChestMenu> {
    @Unique private Button up,down;
    @Unique private int requested=-1;
    @Unique private long retry;
    protected SupplyChestScreen(SuperChestMenu menu,Inventory inventory,Component title){super(menu,inventory,title,176,222);}
    @Inject(method="<init>",at=@At("RETURN"))
    private void vd$init(SuperChestMenu menu,Inventory inventory,Component title,CallbackInfo ci){requested=-1;}
    @ModifyArg(method="<init>",at=@At(value="INVOKE",target="Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;<init>(Lnet/minecraft/world/inventory/AbstractContainerMenu;Lnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/network/chat/Component;II)V"),index=3)
    private static int vd$width(int original){return 200;}
    @Overwrite public void extractRenderState(GuiGraphicsExtractor g,int x,int y,float delta){super.extractRenderState(g,x,y,delta);}
    @Override protected void init(){super.init();
        up=addRenderableWidget(Button.builder(Component.literal("^"),b->scroll(-6)).bounds(leftPos+178,topPos+22,16,20).build());
        down=addRenderableWidget(Button.builder(Component.literal("v"),b->scroll(6)).bounds(leftPos+178,topPos+99,16,20).build());
    }
    private boolean waiting(){return requested>=0;}
    private void scroll(int delta){
        if(waiting()||!menu.getCarried().isEmpty()||isQuickCrafting)return;
        int row=Math.clamp(menu.getScrollRow()+delta,0,menu.getMaxScrollRow());if(row==menu.getScrollRow())return;
        requested=row;retry=System.currentTimeMillis()+2000;ClientPlayNetworking.send(new ScrollContainerPayload(menu.containerId,row));
    }
    @Override protected void containerTick(){
        super.containerTick();if(requested==menu.getScrollRow())requested=-1;
        if(waiting()&&System.currentTimeMillis()>retry){retry=System.currentTimeMillis()+2000;ClientPlayNetworking.send(new ScrollContainerPayload(menu.containerId,requested));}
        up.active=!waiting()&&menu.getScrollRow()>0&&menu.getCarried().isEmpty();down.active=!waiting()&&menu.getScrollRow()<menu.getMaxScrollRow()&&menu.getCarried().isEmpty();
    }
    @Overwrite public boolean mouseScrolled(double x,double y,double dx,double dy){
        if(x>=leftPos&&x<leftPos+imageWidth&&y>=topPos+18&&y<topPos+126&&dy!=0){scroll(dy>0?-1:1);return true;}return super.mouseScrolled(x,y,dx,dy);
    }
    @Override public boolean mouseClicked(MouseButtonEvent event,boolean doubleClick){return waiting()||super.mouseClicked(event,doubleClick);}
    @Override public boolean mouseDragged(MouseButtonEvent event,double x,double y){return waiting()||super.mouseDragged(event,x,y);}
    @Override public boolean mouseReleased(MouseButtonEvent event){return waiting()||super.mouseReleased(event);}
    @Override public boolean keyPressed(KeyEvent event){return waiting()&&event.key()!=256||super.keyPressed(event);}
    @Override public void extractContents(GuiGraphicsExtractor g,int mx,int my,float delta){
        g.fill(leftPos-2,topPos-2,leftPos+imageWidth+2,topPos+imageHeight+2,0xFFF0D796);
        g.fill(leftPos,topPos,leftPos+imageWidth,topPos+imageHeight,0xFF192330);
        for(var slot:menu.slots){int x=leftPos+slot.x,y=topPos+slot.y;g.fill(x-1,y-1,x+17,y+17,0xFF516070);g.fill(x,y,x+16,y+16,0xFF0D131B);}
        g.fill(leftPos+181,topPos+48,leftPos+191,topPos+93,0xFF0D131B);
        int y=topPos+48+(menu.getMaxScrollRow()==0?0:35*menu.getScrollRow()/menu.getMaxScrollRow());g.fill(leftPos+181,y,leftPos+191,y+10,0xFFF0D796);
        super.extractContents(g,mx,my,delta);
    }
    @Override protected void extractLabels(GuiGraphicsExtractor g,int mx,int my){
        g.text(font,font.plainSubstrByWidth(title.getString(),184),8,6,0xFFF0D796);
        g.text(font,waiting()?"Synchronisation...":"Lignes "+(menu.getScrollRow()+1)+" à "+(menu.getScrollRow()+6)+" / "+(menu.getMaxScrollRow()+6),8,128,0xFFBBC6D2);
    }
}
