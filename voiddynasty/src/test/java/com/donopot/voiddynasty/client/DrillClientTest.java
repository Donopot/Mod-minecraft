package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.registry.ModItems;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.world.InteractionHand;

/** Captures real rendering and drives the actual client/server hold-use path. */
public final class DrillClientTest implements ClientModInitializer {
    private static int tick;
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger("DRILLTEST");
    public void onInitializeClient(){ClientTickEvents.END_CLIENT_TICK.register(mc->{
        if(mc.player==null || mc.level==null)return;
        try{run(mc,++tick);}catch(Throwable error){LOG.error("DRILL_CLIENT FAIL",error);mc.stop();}
    });}
    private static void check(boolean yes,String what){if(!yes)throw new AssertionError(what);LOG.info("DRILL_CLIENT PASS {}",what);}
    private static void command(Minecraft mc,String phase){mc.player.connection.sendCommand("drillfixture "+phase);}
    private static void capture(Minecraft mc,String name){Screenshot.grab(mc.gameDirectory,name,mc.gameRenderer.mainRenderTarget(),1,m->LOG.info("DRILL_CAPTURE {} {}",name,m.getString()));}
    private static void start(Minecraft mc){mc.options.keyUse.setDown(true);mc.gameMode.useItem(mc.player,InteractionHand.MAIN_HAND);}
    private static void stop(Minecraft mc){mc.options.keyUse.setDown(false);mc.gameMode.releaseUsingItem(mc.player);}
    private static void run(Minecraft mc,int t){
        if(t==20)command(mc,"setup");
        t-=160;
        if(t<=20)return;
        if(t==50){check(mc.player.getMainHandItem().is(ModItems.LASER_DRILL),"real drill equipped");capture(mc,"01-drill-idle.png");}
        if(t==60)start(mc);
        if(t==63){check(mc.player.isUsingItem(),"hold use active");capture(mc,"02-drill-charge.png");}
        if(t==75)capture(mc,"03-drill-beam.png");
        if(t==90)mc.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
        if(t==95)capture(mc,"04-drill-third-person.png");
        if(t==100)command(mc,"mined");
        if(t==105){stop(mc);mc.options.setCameraType(CameraType.FIRST_PERSON);}
        if(t==115){check(!mc.player.isUsingItem(),"release stops local beam");command(mc,"released");capture(mc,"05-drill-released.png");}
        if(t==125)command(mc,"container");
        if(t==140)start(mc);
        if(t==165){stop(mc);command(mc,"containerCheck");}
        if(t==175)command(mc,"protected");
        if(t==190)start(mc);
        if(t==215){stop(mc);command(mc,"protectedCheck");}
        if(t==230){LOG.info("DRILL_CLIENT COMPLETE");mc.stop();}
    }
}
