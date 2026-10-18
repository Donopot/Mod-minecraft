package net.mcreator.minepiece.client.model;

import com.google.gson.*;
import java.io.*;
import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.mcreator.minepiece.client.*;

/** Reserved Bedrock rigs: original cubes, pivots, box UVs and finite numeric animation tracks. */
public final class SourceRigModel extends EntityModel<EntityRenderState> {
   private record Loaded(ModelPart root,Map<String,ModelPart> bones,Map<String,RigAnimation> clips) {}
   private final Map<String,ModelPart> bones;
   private final Map<String,RigAnimation> clips;
   private final String key;
   private final ModelPart rig;
   public SourceRigModel(String key){this(key,load(key));}
   public SourceRigModel(String key,String bone,float x,float y,float z){this(key,load(key,bone,x,y,z));}
   private SourceRigModel(String key,Loaded data){super(data.root);this.key=key;rig=data.root;bones=data.bones;clips=data.clips;}
   private static JsonObject resource(String name){
      try(var reader=Minecraft.getInstance().getResourceManager().getResource(Identifier.fromNamespaceAndPath("minepiece","upgrade24/"+name)).orElseThrow().openAsReader()){
         return JsonParser.parseReader(reader).getAsJsonObject();
      }catch(IOException failure){throw new IllegalStateException("Missing reserved rig "+name,failure);}
   }
   private static Loaded load(String key){
      return load(key,null,0,0,0);
   }
   private static Loaded load(String key,String detached,float x,float y,float z){
      var geo=resource(key+".geo.json").getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();var description=geo.getAsJsonObject("description");
      int width=description.get("texture_width").getAsInt(),height=description.get("texture_height").getAsInt();
      Map<String,JsonObject> specs=new LinkedHashMap<>();for(var element:geo.getAsJsonArray("bones")){var bone=element.getAsJsonObject();if(specs.put(bone.get("name").getAsString(),bone)!=null)throw new IllegalArgumentException("Duplicate rig bone");}
      if(detached!=null){
         var selected=specs.get(detached).deepCopy();selected.remove("parent");selected.remove("rotation");selected.add("pivot",JsonParser.parseString("[0,0,0]"));
         for(var element:selected.getAsJsonArray("cubes")){var cube=element.getAsJsonObject();for(String field:List.of("origin","pivot"))if(cube.has(field)){var vector=cube.getAsJsonArray(field);vector.set(0,new JsonPrimitive(vector.get(0).getAsFloat()-x));vector.set(1,new JsonPrimitive(vector.get(1).getAsFloat()-y));vector.set(2,new JsonPrimitive(vector.get(2).getAsFloat()-z));}}
         specs.clear();specs.put(detached,selected);
      }
      Map<String,ModelPart> bones=new LinkedHashMap<>(),roots=new LinkedHashMap<>();
      for(var entry:specs.entrySet())if(!entry.getValue().has("parent"))roots.put(entry.getKey(),bone(entry.getValue(),specs,bones,width,height));
      if(bones.size()!=specs.size())throw new IllegalArgumentException("Orphan or cyclic rig hierarchy: "+key);
      var root=new ModelPart(List.of(),roots);root.setInitialPose(PartPose.offset(0,24,0));root.resetPose();
      var clips=detached==null?RigAnimation.read(resource(key+".animation.json")):Map.<String,RigAnimation>of();
      for(var clip:clips.values())if(!bones.keySet().containsAll(clip.bones()))throw new IllegalArgumentException("Animation references absent bone: "+key);
      return new Loaded(root,Map.copyOf(bones),clips);
   }
   private static float v(JsonObject object,String field,int index){return object.has(field)?object.getAsJsonArray(field).get(index).getAsFloat():0;}
   private static float rad(float degrees){return degrees*(float)Math.PI/180;}
   private static ModelPart bone(JsonObject spec,Map<String,JsonObject> specs,Map<String,ModelPart> bones,int width,int height){
      String name=spec.get("name").getAsString();float px=v(spec,"pivot",0),py=v(spec,"pivot",1),pz=v(spec,"pivot",2);Map<String,ModelPart> children=new LinkedHashMap<>();
      for(var entry:specs.entrySet())if(entry.getValue().has("parent")&&entry.getValue().get("parent").getAsString().equals(name))children.put(entry.getKey(),bone(entry.getValue(),specs,bones,width,height));
      int index=0;if(spec.has("cubes"))for(var element:spec.getAsJsonArray("cubes")){
         var cube=element.getAsJsonObject();float cx=cube.has("pivot")?v(cube,"pivot",0):px,cy=cube.has("pivot")?v(cube,"pivot",1):py,cz=cube.has("pivot")?v(cube,"pivot",2):pz;
         var uv=cube.getAsJsonArray("uv");float dx=v(cube,"size",0),dy=v(cube,"size",1),dz=v(cube,"size",2),inflate=cube.has("inflate")?cube.get("inflate").getAsFloat():0;
         var box=new ModelPart.Cube(uv.get(0).getAsInt(),uv.get(1).getAsInt(),v(cube,"origin",0)-cx,cy-v(cube,"origin",1)-dy,v(cube,"origin",2)-cz,dx,dy,dz,inflate,inflate,inflate,cube.has("mirror")&&cube.get("mirror").getAsBoolean(),width,height,EnumSet.allOf(Direction.class));
         var part=new ModelPart(List.of(box),Map.of());part.setInitialPose(PartPose.offsetAndRotation(cx-px,py-cy,cz-pz,rad(-v(cube,"rotation",0)),rad(-v(cube,"rotation",1)),rad(v(cube,"rotation",2))));part.resetPose();children.put("$cube_"+index++,part);
      }
      var parent=spec.has("parent")?specs.get(spec.get("parent").getAsString()):null;
      var part=new ModelPart(List.of(),children);part.setInitialPose(PartPose.offsetAndRotation(px-(parent==null?0:v(parent,"pivot",0)),(parent==null?0:v(parent,"pivot",1))-py,pz-(parent==null?0:v(parent,"pivot",2)),rad(-v(spec,"rotation",0)),rad(-v(spec,"rotation",1)),rad(v(spec,"rotation",2))));part.resetPose();bones.put(name,part);return part;
   }
   private String available(String shortName){if(clips.containsKey(shortName))return shortName;String wolf="animation.wolfbeast."+shortName;return clips.containsKey(wolf)?wolf:"";}
   private RigAnimation clip(String name){return clips.get(available(name));}
   private String action(String animation){
      if(key.equals("amaru"))return animation.contains("raigo")?"raigo":animation.contains("storm")?"mamaragan":animation.contains("blink")?"click":animation.contains("sango")||animation.contains("elthor")||animation.contains("vari")?"shoot":animation.contains("karikrash")?"attack":"";
      if(key.equals("torifullform"))return animation.contains("marcotornado")?"toritwirlbeast":animation.contains("bluebird")?"bluebirdbeast":animation.contains("fujiazami")?"torilift":animation.contains("hooin")?"toririptide":animation.contains("phoenix_")?"attack":"";
      if(key.equals("buddha"))return animation.contains("impactwave")?"rupture":animation.contains("overhead")?"stomp":animation.contains("impactball")?"shockwave":animation.contains("brilliant")?"palm":animation.contains("daibutsu_")?"attack":"";
      if(key.equals("wolfbeast"))return animation.contains("soru")||animation.contains("poursuite")?"dash":animation.contains("wolf_")?"attack":"";
      return animation.contains("leopard_")?"attack":"";
   }
   @Override public void setupAnim(EntityRenderState state){
      rig.getAllParts().forEach(ModelPart::resetPose);
      var input=state instanceof RigRenderData data?data.minepiece$rig():null;
      if(state instanceof SourceEffectState effect){
         var animation=clip(effect.clip);if(animation==null&&!effect.clip.isEmpty())throw new IllegalArgumentException("Missing source effect clip "+key+"/"+effect.clip);
         var sampled=animation==null?Map.<String,RigAnimation.Pose>of():animation.sample(effect.seconds);apply(sampled);
         // Keep parent transforms, hiding only geometry belonging to unselected branches.
         if(!effect.visibleBones.isEmpty())for(var entry:bones.entrySet()){
            boolean visible=effect.visibleBones.contains(entry.getKey());
            var parent=entry.getValue();
            for(int i=0;parent.hasChild("$cube_"+i);i++)parent.getChild("$cube_"+i).visible=visible;
         }
         PriorityRigProbe.evaluated(state,key,effect.clip,sampled,bones.size());return;
      }
      if(input==null)return;
      boolean stone=state instanceof ParameciaRenderData data&&data.minepiece$stone();double seconds=stone?0:state.ageInTicks/20;
      var idle=clip("idle");Map<String,RigAnimation.Pose> base=idle.sample(seconds);
      float locomotion=stone?0:key.equals("torifullform")&&input.airborne()?1:input.walking();
      if(locomotion>0)base=blend(base,clip("run").sample(seconds),locomotion);
      String selected=locomotion>.5F?"run":"idle";
      if(!stone&&key.equals("amaru")&&input.airborne()){
         base=blend(base,clip("fly").sample(input.airborneAge()),(float)Math.min(1,input.airborneAge()/.2));selected="fly";
      }
      String action=action(input.action());double actionTime=input.actionAge();
      if(!stone&&key.equals("torifullform")&&input.formAge()<2){action="toritransformation";actionTime=input.formAge();}
      var gesture=clip(action);
      if(!stone&&gesture!=null){
         boolean charge=input.action().endsWith("_charge");double duration=charge?5.2:gesture.length;
         if(actionTime<duration){float weight=(float)Math.min(1,Math.min(actionTime/.12,(duration-actionTime)/.12));base=blend(base,gesture.sample(Math.min(actionTime,gesture.length)),Math.max(0,weight));selected=action;}
      }
      apply(base);
      float a=0,b=0,c=0,d=0;for(var part:bones.values()){a+=part.xRot;b+=part.yRot;c+=part.zRot;d+=part.y*.01F;}
      if(key.equals("wolfbeast"))Batch06VisualLab.beastPose(0,a,b,c);
      if(key.equals("leopardbeastrework"))Batch06VisualLab.beastPose(1,a,b,c);
      if(key.equals("buddha"))Batch07VisualLab.beastPose(1,a,b,c);
      if(key.equals("torifullform"))Batch07VisualLab.beastPose(2,a,b,c);
      if(key.equals("amaru"))AmaruRenderProbe.evaluatedSource(state,a,b,c,d);
      PriorityRigProbe.evaluated(state,key,selected,base,bones.size());
   }
   private void apply(Map<String,RigAnimation.Pose> base){
      rig.getAllParts().forEach(p->p.visible=true);
      for(var entry:base.entrySet()){
         var part=bones.get(entry.getKey());var pose=entry.getValue();part.xRot+=rad(-pose.rotation().x());part.yRot+=rad(-pose.rotation().y());part.zRot+=rad(pose.rotation().z());part.x+=pose.position().x();part.y-=pose.position().y();part.z+=pose.position().z();part.xScale=pose.scale().x();part.yScale=pose.scale().y();part.zScale=pose.scale().z();
      }
   }
   private static Map<String,RigAnimation.Pose> blend(Map<String,RigAnimation.Pose> a,Map<String,RigAnimation.Pose> b,float weight){
      Map<String,RigAnimation.Pose> out=new LinkedHashMap<>(a);var neutral=new RigAnimation.Pose(RigAnimation.Vector.ZERO,RigAnimation.Vector.ZERO,RigAnimation.Vector.ONE);
      Set<String> names=new LinkedHashSet<>(a.keySet());names.addAll(b.keySet());
      for(String name:names){var left=a.getOrDefault(name,neutral);var right=b.getOrDefault(name,neutral);out.put(name,new RigAnimation.Pose(left.rotation().mix(right.rotation(),weight),left.position().mix(right.position(),weight),left.scale().mix(right.scale(),weight)));}
      return out;
   }
}
