package com.donopot.voiddynasty.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import com.mojang.blaze3d.platform.InputConstants;
import net.mcreator.minepiece.client.DevilFruitControls;

/** Real client render/click/rebind tests in an owned disposable profile, no OS input. */
public final class HudClientTest implements ClientModInitializer {
    private static int tick,checks;private static boolean failed;
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger("HUDTEST");
    public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client->{
            if(client.player==null || client.level==null)return;
            try {run(client,++tick);} catch(Throwable error) {failed=true;LOG.error("HUD_CLIENT FAIL tick={}",tick,error);client.stop();}
        });
    }
    private static void check(boolean value,String message) {if(!value)throw new AssertionError(message);checks++;LOG.info("HUD_CLIENT PASS {}",message);}
    private static void click(KeyMapping key) {KeyMapping.click(KeyMappingHelper.getBoundKeyOf(key));}
    private static void bind(KeyMapping key,int code) {key.setKey(InputConstants.Type.KEYBOARD.getOrCreate(code));KeyMapping.resetMapping();}
    private static void command(Minecraft client,String phase) {client.player.connection.sendCommand("hudfixture "+phase);}
    private static void capture(Minecraft client,String name) {Screenshot.grab(client.gameDirectory,name,client.gameRenderer.mainRenderTarget(),1,message->LOG.info("HUD_CAPTURE {} {}",name,message.getString()));}
    private static void run(Minecraft mc,int t) throws Exception {
        boolean reopen=Boolean.getBoolean("hudtest.reopen");
        if(reopen) {
            if(t==25) {
                check(KeyMappingHelper.getBoundKeyOf(DevilFruitControls.SKILL).getValue()==InputConstants.KEY_K,"saved One Piece rebind reloaded");
                check(KeyMappingHelper.getBoundKeyOf(BolterKeybindings.RELOAD_KEY).getValue()==InputConstants.KEY_U,"saved reload rebind reloaded");
                check(!DevilFruitControls.hudVisible && !BolterKeybindings.hudVisible,"HUD preferences survive client restart");
                click(DevilFruitControls.HUD);click(BolterKeybindings.HUD_KEY);
            }
            if(t==35) {check(DevilFruitControls.hudVisible && BolterKeybindings.hudVisible,"both HUD toggles restored independently");capture(mc,"08-after-restart.png");}
            if(t==45){LOG.info("HUD_CLIENT COMPLETE checks={} failed={}",checks,failed);mc.stop();}return;
        }
        if(t==25) {
            var keys=java.util.Arrays.asList(mc.options.keyMappings);
            for(var key:new KeyMapping[]{DevilFruitControls.SKILL,DevilFruitControls.TRANSFORM,DevilFruitControls.CYCLE,DevilFruitControls.HUD,BolterKeybindings.RELOAD_KEY,BolterKeybindings.HUD_KEY})
                check(keys.contains(key),"registered "+key.getName());
            check(!DevilFruitControls.SKILL.same(BolterKeybindings.RELOAD_KEY),"new defaults do not share an action key");
            check(!DevilFruitControls.CATEGORY.label().getString().startsWith("key.category"),"One Piece category localized");
            check(!BolterKeybindings.CATEGORY.label().getString().startsWith("key.category"),"VOID category localized");
        }
        if(t==35)capture(mc,"01-default.png");
        if(t==40){bind(DevilFruitControls.SKILL,InputConstants.KEY_K);bind(BolterKeybindings.RELOAD_KEY,InputConstants.KEY_U);mc.options.save();}
        if(t==50)capture(mc,"02-remapped.png");
        if(t==55) {
            mc.gui.setScreen(new Screen(Component.literal("HUD test menu")){});
            click(DevilFruitControls.SKILL);click(DevilFruitControls.TRANSFORM);click(DevilFruitControls.CYCLE);click(BolterKeybindings.RELOAD_KEY);
        }
        if(t==65){command(mc,"menu");capture(mc,"03-menu.png");}
        if(t==70)mc.gui.setScreen(null);
        if(t==80){DevilFruitControls.SKILL.setDown(true);click(DevilFruitControls.SKILL);}
        if(t==82)DevilFruitControls.SKILL.setDown(false);
        if(t==90)command(mc,"cast");
        if(t==95)click(BolterKeybindings.RELOAD_KEY);
        if(t==102){command(mc,"reload");capture(mc,"04-reloading.png");}
        if(t==108){command(mc,"prepareLegacy");bind(DevilFruitControls.SKILL,InputConstants.KEY_R);bind(BolterKeybindings.RELOAD_KEY,InputConstants.KEY_R);}
        if(t==115){check(DevilFruitControls.reloadHasPriority(mc),"legacy shared R correctly favors held firearm");click(DevilFruitControls.SKILL);}
        if(t==125){command(mc,"legacy");capture(mc,"05-legacy-shared.png");}
        if(t==130){bind(DevilFruitControls.SKILL,InputConstants.KEY_K);bind(BolterKeybindings.RELOAD_KEY,InputConstants.KEY_U);mc.options.save();mc.gui.hud.toggle();}
        if(t==138){check(mc.gui.hud.isHidden(),"F1 hidden HUD state");capture(mc,"06-f1-hidden.png");}
        if(t==145){mc.gui.hud.toggle();mc.options.guiScale().set(3);mc.resizeGui();}
        if(t>=146 && t<=158) {
            // Rendering stress fixture: a real long localized label plus the optional weight row.
            net.mcreator.minepiece.client.DevilFruitClientState.techniqueId="mera_daienkai_entei";
            net.mcreator.minepiece.client.DevilFruitClientState.weight=10000;
        }
        if(t==155){check(mc.getWindow().getGuiScaledWidth()<400,"narrow scaled GUI actually applied");capture(mc,"07-narrow.png");}
        if(t==160){click(DevilFruitControls.HUD);click(BolterKeybindings.HUD_KEY);}
        if(t==170)check(!DevilFruitControls.hudVisible && !BolterKeybindings.hudVisible,"independent HUD visibility toggles");
        if(t==185){LOG.info("HUD_CLIENT COMPLETE checks={} failed={}",checks,failed);mc.stop();}
    }
}
