package net.mcreator.minepiece.client;

import java.util.HashSet;
import java.util.Set;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.mcreator.minepiece.entity.EntityGomuArmEntity;
import net.mcreator.minepiece.entity.SkillProjectileEntity;
import net.mcreator.minepiece.init.MinepieceModParticles;
import net.mcreator.minepiece.procedures.MeraTechniques;

/** The same animated FLAME/FLAME_SMALL sprites as FirePropulsion, shaped around each finite attack. */
public final class MeraFlameVisuals {
 private static final Set<String> SEEN=new HashSet<>();
 private MeraFlameVisuals(){}
 public static void register(){ClientTickEvents.END_CLIENT_TICK.register(MeraFlameVisuals::tick);}
 private static void tick(Minecraft c){
  if(c.level==null||c.player==null||c.level.getGameTime()%2!=0)return;
  int budget=96;
  for(Entity e:c.level.entitiesForRendering()){
   if(budget<=0)break;if(e.isRemoved()||e.distanceToSqr(c.player)>64*64)continue;
   String id;float scale=1;boolean projectile=false;
   if(e instanceof SkillProjectileEntity shot){id=shot.getVisualProfile();scale=shot.getScale();projectile=true;if(!shot.getEntityData().get(SkillProjectileEntity.VISUAL_READY))continue;}
   else if(e instanceof EntityGomuArmEntity fx){id=fx.getVisualProfile();scale=fx.getStretch();}
   else continue;
   if(!MeraTechniques.ownsVisual(id))continue;
   int requested=switch(id){case "higan"->8;case "hiken"->16;case "hibashira","daienkai","mera_charge_ball"->24;case "kaenryuo"->20;case "mera_charge"->6;default->12;};
   Vec3 direction=projectile?e.getDeltaMovement():e.getLookAngle();if(direction.lengthSqr()<1e-6)direction=new Vec3(0,0,1);direction=direction.normalize();
   Vec3 u=direction.cross(Math.abs(direction.y)>.95?new Vec3(1,0,0):new Vec3(0,1,0)).normalize(),v=direction.cross(u);
   int emitted=0;
   for(int i=0;i<requested&&budget>0;i++){
    double f=c.level.getRandom().nextDouble(),angle=c.level.getRandom().nextDouble()*Math.PI*2;Vec3 at,velocity;boolean big=i%4==0;
    switch(id){
     case "hiken","higan"->{double radius=id.equals("higan")?.14:Math.clamp(scale*.35,.3,1.1);double trail=Math.min(4,e.getDeltaMovement().length());at=e.position().subtract(direction.scale(f*trail)).add(u.scale(Math.cos(angle)*radius)).add(v.scale(Math.sin(angle)*radius));velocity=direction.scale(-.035).add(0,.035,0);if(id.equals("higan"))big=false;}
     case "daienkai","mera_charge_ball"->{double r=id.equals("daienkai")?2.4*Math.clamp(scale,.5,2):.2+2*Math.clamp(scale,0,1);double y=c.level.getRandom().nextDouble()*2-1,side=Math.sqrt(1-y*y);Vec3 normal=new Vec3(Math.cos(angle)*side,y,Math.sin(angle)*side);at=e.position().add(normal.scale(r));velocity=normal.scale(.025).add(0,.045,0);}
     case "hibashira"->{boolean warning=e.tickCount<10;double radius=Math.clamp(scale,.5,12),height=warning?.15:e.tickCount<20?8:3;at=e.position().add(Math.cos(angle)*radius*Math.sqrt(f),c.level.getRandom().nextDouble()*height,Math.sin(angle)*radius*Math.sqrt(f));velocity=new Vec3(0,warning?.015:.1,0);if(warning)big=false;}
     case "kaenryuo"->{double along=.6+f*5.4,radius=.15+f*.9;at=e.position().add(direction.scale(along)).add(u.scale(Math.cos(angle)*radius)).add(v.scale(Math.sin(angle)*radius));velocity=direction.scale(.1).add(0,.04,0);}
     case "mera_fireflies","hotarubi"->{double radius=Math.clamp(scale,1,6);at=e.position().add(Math.cos(angle)*radius*Math.sqrt(f),.15+c.level.getRandom().nextDouble()*1.3,Math.sin(angle)*radius*Math.sqrt(f));velocity=new Vec3(0,.025,0);big=false;}
     case "mera_dash","kagero"->{at=e.position().subtract(direction.scale(f*1.8)).add(Math.cos(angle)*.3,.25+c.level.getRandom().nextDouble()*.45,Math.sin(angle)*.3);velocity=direction.scale(-.04).add(0,.05,0);}
     default->{at=e.position().add(Math.cos(angle)*.5,c.level.getRandom().nextDouble()*.55-.15,Math.sin(angle)*.5);velocity=new Vec3(0,.035,0);big=false;}
    }
    // Sprite rendering fades against each viewer's camera; retain the world trail in all views.
    c.level.addParticle(big?MinepieceModParticles.FLAME:MinepieceModParticles.FLAME_SMALL,at.x,at.y,at.z,velocity.x,velocity.y,velocity.z);emitted++;budget--;
   }
   if(emitted>0&&Boolean.getBoolean("opworldlike.visualCapture")&&SEEN.add(id))org.slf4j.LoggerFactory.getLogger("MeraFlames").info("[mera-flame] {} sprites={} native-propulsion-flames",id,emitted);
  }
 }
}
