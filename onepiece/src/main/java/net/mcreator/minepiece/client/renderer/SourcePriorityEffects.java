package net.mcreator.minepiece.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.*;
import net.mcreator.minepiece.client.SourceEffectState;
import net.mcreator.minepiece.client.model.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** Original reserved meshes on existing server anchors. No hits, terrain edits or extra entities. */
public final class SourcePriorityEffects {
 private static final Set<String> SEEN=new HashSet<>();
 private final Map<String,SourceRigModel> rigs=new HashMap<>();
 private static final class Mesh extends EntityModel<EntityRenderState>{Mesh(ModelPart root){super(root);}}
 private final Mesh paw=new Mesh(SourcePriorityMeshes.paw().bakeRoot());
 private final Mesh hotFist=new Mesh(SourcePriorityMeshes.yakimochi().bakeRoot());
 private final Mesh mochiFist=new Mesh(SourcePriorityMeshes.mochifist().bakeRoot());
 public SourcePriorityEffects(){for(String key:List.of("guracrack","guracrackground","guracracksea","gurauppercut","takt1","takt2","takt3","takt4","takt5","takt6","ursusshock"))rigs.put(key,new SourceRigModel(key));rigs.put("buzzcut",new SourceRigModel("buzzcut","bone2",-355,3,0));}
 private static Identifier texture(String key){return Identifier.fromNamespaceAndPath("minepiece","upgrade24/"+key+".png");}
 public boolean submit(String id,float factor,Vec3 direction,int variant,EntityRenderState state,PoseStack pose,SubmitNodeCollector collector){
  String key=switch(id){
   case "b02_air_crack","b02_quake_front"->"guracrack";
   case "b02_quake_radial","b02_quake_sweep"->"guracrackground";
   case "b02_quake_overhead"->"gurauppercut";
   case "b02_tilt","b02_tilt_reverse"->"guracracksea";
   case "b02_stoneopeope"->"takt"+(Math.floorMod(variant,6)+1);
   case "b02_ursusshock","b02_paw_charge","b02_paw_blast"->"ursusshock";
   case "b03_mochi_zangirimochi","b03_mochi_impact"->"buzzcut";
   case "b02_padho","b02_paw_impact"->"paweffect";
   case "b03_mochi_yakimochi"->"yakimochi";
   case "b03_native_mochipistol","b03_native_mochigatling","b03_mochi_ringfist"->"mochifist";
   default->"";
  };
  if(key.isEmpty())return false;
  float age=state.ageInTicks/20F;float scale=1;String clip="idle";double time=age;
  EntityModel<EntityRenderState> model;EntityRenderState input=state;
  boolean emissive=key.startsWith("gura")||key.equals("ursusshock")||key.equals("paweffect");
  pose.pushPose();
  double yaw=Math.toDegrees(Math.atan2(-direction.x,direction.z)),pitch=-Math.toDegrees(Math.atan2(direction.y,Math.hypot(direction.x,direction.z)));
  pose.rotateDegrees(Axis.YP,(float)yaw+180);
  if(key.startsWith("gura")){
   scale=(id.equals("b02_air_crack")?.65F:Math.min(1.6F,Math.max(.4F,factor*.3F)));
   // Ground/overhead versions are exported as vertical sheets: lay them onto the ground.
   if(key.equals("guracrackground")||key.equals("gurauppercut")){pose.translate(0,-.65,0);pose.rotateDegrees(Axis.XP,90);}
   else if(key.equals("guracracksea"))pose.rotateDegrees(Axis.ZP,id.equals("b02_tilt")?18:-18);
   time=Math.min(age,.95); // one expansion per native impact, never restart the exported loop
  }else if(key.startsWith("takt")){scale=factor*.29F;pose.rotateDegrees(Axis.XP,(float)pitch);}
  else if(key.equals("ursusshock")){
   pose.rotateDegrees(Axis.XP,(float)pitch);
   if(id.equals("b02_paw_charge")){scale=.1F;time=Math.min(5.9,age*5);}
   else if(id.equals("b02_paw_blast")){clip="idle2";scale=.025F*Math.min(5,factor);time=Math.min(1.4,age*1.6);}
   else {scale=.34F*factor;time=.3;}
  }else if(key.equals("buzzcut")){
   // Only the source spiked head. Its 14s / 24-block arm animation is incompatible with this native dash.
   scale=Math.min(1.1F,Math.max(.4F,factor*.6F));time=0;clip="";
   pose.rotateDegrees(Axis.ZP,state.ageInTicks*27);
  }else if(key.equals("paweffect")){scale=id.equals("b02_padho")?1.7F:2.4F;pose.rotateDegrees(Axis.XP,(float)pitch);}
  else if(key.equals("yakimochi")){scale=3.2F*factor;pose.rotateDegrees(Axis.XP,90+(float)pitch);}
  else if(key.equals("mochifist")){
   // Source fist is at Y=24, the tail at Y=238. Stretch only the long axis to the server contact.
   float reach=id.equals("b03_mochi_ringfist")?1.7F:factor*(id.equals("b03_native_mochipistol")?12.25F:10.25F);
   pose.rotateDegrees(Axis.XP,(float)pitch);pose.translate(0,0,-reach);pose.scale(.22F,.22F,reach/13.375F);pose.rotateDegrees(Axis.XP,-90);scale=1;
  }
  pose.scale(scale,scale,scale);pose.translate(0,1.5,0);pose.scale(-1,-1,1);
  if(key.equals("paweffect"))model=paw;
  else if(key.equals("yakimochi")){model=hotFist;pose.translate(0,-.9375,0);}
  else if(key.equals("mochifist"))model=mochiFist;
  else {
   model=rigs.get(key);input=new SourceEffectState(clip,(float)time,key.equals("buzzcut")?Set.of("bone2"):Set.of());input.outlineColor=state.outlineColor;
  }
  collector.order(0).submitModel(model,input,pose,emissive?RenderTypes.eyes(texture(key)):RenderTypes.entityCutout(texture(key)),15728880,OverlayTexture.NO_OVERLAY,-1,null,state.outlineColor);
  pose.popPose();
  if(Boolean.getBoolean("opworldlike.visualCapture")&&SEEN.add(id))org.slf4j.LoggerFactory.getLogger("MinePieceAssets").info("[priority-effect-render] {} mesh={} native-anchor",id,key);
  return true;
 }
}
