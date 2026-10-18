package net.mcreator.minepiece.client.hud;

import net.mcreator.minepiece.client.DevilFruitClientState;
import net.mcreator.minepiece.client.DevilFruitControls;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Top-left power panel, outside chat/hotbar; scaled coordinates and live bindings. */
public final class DevilFruitHud implements HudElement {
    private static final int GOLD=0xFFE9BC67,TEXT=0xFFF0EDE6,DIM=0xFFA8B4BA;
    public static void register() {HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("minepiece","devil_fruit_hud"),new DevilFruitHud());}

    @Override public void extractRenderState(GuiGraphicsExtractor g,DeltaTracker delta) {
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null || mc.font==null || mc.player.isSpectator() || mc.gui.hud.isHidden()
                || mc.gui.screen()!=null || !DevilFruitControls.hudVisible || DevilFruitClientState.fruitId.isEmpty()) return;
        int width=Math.min(224,(g.guiWidth()-24)/2);
        if(width<80) return;
        int content=width-16;
        var move=selectedMove();
        Component technique=move!=null?move.name():DevilFruitClientState.techniqueId.isEmpty()?Component.translatable("minepiece.hud.none")
                :Component.translatableWithFallback("minepiece.technique."+DevilFruitClientState.techniqueId,DevilFruitClientState.techniqueId);
        var lines=mc.font.split(technique,content);
        int lineCount=Math.min(2,Math.max(1,lines.size()));
        boolean weight=Math.abs(DevilFruitClientState.weight-1)>1.0E-9;
        int height=130+lineCount*10+(weight?11:0);
        int x=8,y=8,cx=x+8,cy=y+7;
        g.fill(x,y,x+width,y+height,0xD3111A22);
        g.fill(x,y,x+3,y+height,GOLD);
        g.fill(x+3,y,x+width,y+1,0xFF58616B);
        text(g,mc,Component.translatable("minepiece.hud.title"),cx,cy,content,GOLD);cy+=12;
        text(g,mc,fruitName(),cx,cy,content,TEXT);cy+=14;
        int energy=finiteInt(DevilFruitClientState.energy),max=finiteInt(DevilFruitClientState.energyMax);
        text(g,mc,Component.translatable("minepiece.hud.energy").append("  "+energy+" / "+max),cx,cy,content,DIM);cy+=11;
        g.fill(cx,cy,cx+content,cy+4,0xFF283641);
        double ratio=max>0?Math.clamp((double)energy/max,0,1):0;
        int filled=(int)Math.round(content*ratio);
        if(filled>0)g.fill(cx,cy,cx+filled,cy+4,ratio<=.2?0xFFEC7865:0xFF4DD8BC);
        cy+=11;
        String index=DevilFruitClientState.techniqueCount>0?(DevilFruitClientState.technique+1)+" / "+DevilFruitClientState.techniqueCount:"—";
        text(g,mc,Component.translatable("minepiece.hud.technique").append("  "+index),cx,cy,content,GOLD);cy+=11;
        for(int n=0;n<lineCount;n++) {
            if(n<lines.size()) g.text(mc.font,lines.get(n),cx,cy,TEXT);
            cy+=10;
        }
        if(lines.size()>2)g.text(mc.font,"…",x+width-16,cy-10,TEXT);
        cy+=3;
        text(g,mc,Component.translatable("minepiece.hud.form").append(": ").append(formName()),cx,cy,content,DIM);cy+=11;
        if(weight) {text(g,mc,Component.translatable("minepiece.hud.weight").append(": "+String.format(java.util.Locale.ROOT,"%.1f×",DevilFruitClientState.weight)),cx,cy,content,DIM);cy+=11;}
        g.fill(cx,cy+1,cx+content,cy+2,0xFF33434E);cy+=7;
        hint(g,mc,DevilFruitControls.SKILL,DevilFruitControls.reloadHasPriority(mc)?"minepiece.hud.reload_priority":"minepiece.hud.use",cx,cy,content);cy+=11;
        hint(g,mc,DevilFruitControls.CYCLE,"minepiece.hud.change",cx,cy,content);cy+=11;
        hint(g,mc,DevilFruitControls.TRANSFORM,"minepiece.hud.transform",cx,cy,content);
    }
    private static int finiteInt(double value) {return Double.isFinite(value)?(int)Math.clamp(Math.round(value),0,Integer.MAX_VALUE):0;}
    private static void hint(GuiGraphicsExtractor g,Minecraft mc,KeyMapping key,String action,int x,int y,int width) {
        text(g,mc,Component.literal("[").append(key.getTranslatedKeyMessage()).append("] ").append(Component.translatable(action)),x,y,width,0xFFB9C6CD);
    }
    private static void text(GuiGraphicsExtractor g,Minecraft mc,Component text,int x,int y,int width,int color) {
        String value=text.getString();
        if(mc.font.width(value)>width)value=mc.font.plainSubstrByWidth(value,Math.max(0,width-mc.font.width("…")))+"…";
        g.text(mc.font,value,x,y,color);
    }
    private static net.mcreator.minepiece.procedures.DevilFruitMove selectedMove() {
        Identifier id=Identifier.tryParse(DevilFruitClientState.fruitId);
        if(id==null)return null;
        var moves=net.mcreator.minepiece.procedures.DevilFruitRegistry.movesFor(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(id));
        int i=DevilFruitClientState.technique;
        return i>=0 && i<moves.size() && moves.get(i).id().equals(DevilFruitClientState.techniqueId)?moves.get(i):null;
    }
    private static Component formName() {
        double form=DevilFruitClientState.form;
        if(form<=0)return Component.translatable("minepiece.hud.form.base");
        String fruit=DevilFruitClientState.fruitId;
        if(fruit.equals(net.mcreator.minepiece.procedures.GoroAmaru.FRUIT))return Component.literal("Amaru");
        if(fruit.equals(net.mcreator.minepiece.procedures.GomuGears.FRUIT))return Component.literal("Gear "+(int)form);
        return Component.translatable(form==2?"minepiece.hud.form.full":"minepiece.hud.form.hybrid");
    }
    private static Component fruitName() {
        String id=DevilFruitClientState.fruitId;int colon=id.indexOf(':');
        return colon<=0?Component.literal(id):Component.translatableWithFallback("item."+id.substring(0,colon)+"."+id.substring(colon+1),id.substring(colon+1));
    }
}
