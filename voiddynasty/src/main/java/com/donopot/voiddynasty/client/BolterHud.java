package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.component.ModDataComponents;
import com.donopot.voiddynasty.weapon.WeaponItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/** Bottom-right magazine panel, separated from powers and vanilla hotbar/status bars. */
@Environment(EnvType.CLIENT)
public final class BolterHud {
    private BolterHud() {}
    public static void init() {HudElementRegistry.addLast(Identifier.fromNamespaceAndPath("voiddynasty","boltgun_ammo"),BolterHud::renderAmmoHud);}
    private static void renderAmmoHud(GuiGraphicsExtractor g,DeltaTracker delta) {
        Minecraft mc=Minecraft.getInstance();
        if(mc.player==null || mc.font==null || mc.player.isSpectator() || mc.gui.screen()!=null
                || mc.gui.hud.isHidden() || !BolterKeybindings.hudVisible) return;
        ItemStack stack=mc.player.getMainHandItem();
        if(!(stack.getItem() instanceof WeaponItem weapon) || weapon.definition().ammo()==null) return;
        var def=weapon.definition();
        int capacity=Math.max(1,def.magazineSize()),ammo=Math.clamp(stack.getOrDefault(ModDataComponents.AMMO,0),0,capacity);
        int reserve=0;for(ItemStack slot:mc.player.getInventory().getNonEquipmentItems()) if(slot.is(def.ammo()))reserve+=slot.getCount();
        int width=Math.min(224,(g.guiWidth()-24)/2),height=100;
        if(width<80)return;
        int x=g.guiWidth()-width-8,y=Math.max(8,g.guiHeight()-height-82),cx=x+8,w=width-16;
        int accent=ammo==0?0xFFF47B67:0xFF6ADACB;
        g.fill(x,y,x+width,y+height,0xD3111A22);g.fill(x+width-3,y,x+width,y+height,accent);
        g.fill(x,y,x+width-3,y+1,0xFF58616B);
        text(g,mc,"VOID DYNASTY",cx,y+7,w,accent);
        text(g,mc,stack.getHoverName().getString(),cx,y+20,w,0xFFF0EDE6);
        text(g,mc,ammo+" / "+capacity,cx,y+35,w/2,ammo==0?accent:0xFFFFFFFF);
        String stock=Component.translatable("hud.voiddynasty.reserve",reserve).getString();
        text(g,mc,stock,cx+w/2,y+35,w-w/2,0xFFA8B4BA);
        g.fill(cx,y+47,cx+w,y+51,0xFF283641);
        int filled=(int)Math.round((double)w*ammo/capacity);if(filled>0)g.fill(cx,y+47,cx+filled,y+51,accent);
        text(g,mc,new ItemStack(def.ammo()).getHoverName().getString(),cx,y+58,w,0xFFA8B4BA);
        boolean reloading=stack.getOrDefault(ModDataComponents.RELOADING,0)>0;
        text(g,mc,Component.translatable(reloading?"hud.voiddynasty.reloading":ammo==0?"hud.voiddynasty.empty":"hud.voiddynasty.ready").getString(),cx,y+71,w,reloading?0xFFE9BC67:accent);
        String hint=Component.literal("[").append(BolterKeybindings.RELOAD_KEY.getTranslatedKeyMessage()).append("] ").append(Component.translatable("key.voiddynasty.reload")).getString();
        text(g,mc,hint,cx,y+84,w,0xFFB9C6CD);
    }
    private static void text(GuiGraphicsExtractor g,Minecraft mc,String value,int x,int y,int width,int color) {
        if(mc.font.width(value)>width)value=mc.font.plainSubstrByWidth(value,Math.max(0,width-mc.font.width("…")))+"…";
        g.text(mc.font,value,x,y,color);
    }
}
