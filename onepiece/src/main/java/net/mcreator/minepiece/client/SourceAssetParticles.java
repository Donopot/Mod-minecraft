package net.mcreator.minepiece.client;

import java.util.*;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.mcreator.minepiece.entity.*;
import net.mcreator.minepiece.init.MinepieceModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

/** Original particle sprites for source attacks whose clear meshes were intentionally invisible. */
public final class SourceAssetParticles {
 private static final Set<String> SEEN=new HashSet<>();
 private SourceAssetParticles(){}
 public static void register(){ClientTickEvents.END_CLIENT_TICK.register(SourceAssetParticles::tick);}
 private static ParticleOptions type(String id){return switch(id){
  case "whiteblow","whitevine","whitelauncher","b01_smoke_finish","b01_smoke_wave","b01_smoke_dash"->MinepieceModParticles.BIG_SMOKE;
  case "desertspada","desertlaspada","b01_sand_slash","b01_sand_wave","b01_sand_dash","sables-native"->MinepieceModParticles.SAND;
  case "darkball","darkmatter","b01_dark_grab","b01_dark_field","b01_dark_impact"->MinepieceModParticles.DARK;
  case "goro_arc","goro_blink_trail","goro_charge","goro_impact","goro_raigo"->MinepieceModParticles.THUNDER_LIGHT_BLUE;
  case "b05_kage_brickbat"->MinepieceModParticles.BAT;
  case "b07_phoenix_bluebird","b07_phoenix_marcotornado"->MinepieceModParticles.FLAME_BLUE;
  case "b07_makami_narikabura"->MinepieceModParticles.BLUE;
  case "b07_daibutsu_impactball"->net.minecraft.core.particles.ColorParticleOption.create(net.minecraft.core.particles.ParticleTypes.FLASH,0xFFFFF6E4);
  default->null;};}
 private static void tick(Minecraft c){
  if(c.level==null||c.player==null||c.level.getGameTime()%2!=0)return;int budget=64;
  for(Entity e:c.level.entitiesForRendering()){
   if(budget<=0)break;if(e.isRemoved()||e.distanceToSqr(c.player)>64*64)continue;
   String id;double radius=1;Vec3 direction=e.getLookAngle();
   if(e instanceof SkillProjectileEntity p){if(!p.getEntityData().get(SkillProjectileEntity.VISUAL_READY))continue;id=p.getVisualProfile();radius=Math.clamp(p.getScale(),.2,2);direction=p.getDeltaMovement();}
   else if(e instanceof EntityGomuArmEntity fx){id=fx.getVisualProfile();radius=Math.clamp(fx.getStretch(),.3,4);}
   else continue;var type=type(id);if(type==null)continue;int emitted=0;
   boolean flash=id.equals("b07_daibutsu_impactball");if(flash&&c.level.getGameTime()%8!=0)continue;
   int count=flash?1:type==MinepieceModParticles.FLAME_BLUE||type==MinepieceModParticles.BAT?3:8;
   for(int i=0;i<count&&budget>0;i++){
    double angle=c.level.getRandom().nextDouble()*Math.PI*2,r=radius*Math.sqrt(c.level.getRandom().nextDouble());
    Vec3 at=e.position().add(Math.cos(angle)*r,c.level.getRandom().nextDouble()*radius*.7,Math.sin(angle)*r);
    if(e instanceof SkillProjectileEntity)at=at.subtract(direction.scale(c.level.getRandom().nextDouble()));
    if(c.options.getCameraType().isFirstPerson()&&at.distanceToSqr(c.player.getEyePosition())<1.2*1.2)continue;
    c.level.addParticle(type,at.x,at.y,at.z,0,.025,0);budget--;emitted++;
   }
   if(emitted>0&&Boolean.getBoolean("opworldlike.visualCapture")&&SEEN.add(id))org.slf4j.LoggerFactory.getLogger("MinePieceAssets").info("[source-particle-render] {} native-ver15 sprites={}",id,emitted);
  }
 }
}
