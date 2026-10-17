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

/** MinePiece ver15 Kaidou beast, preserving all bones, cube pivots and signed per-face UVs. */
public final class KaidoDragonModel extends EntityModel<EntityRenderState> {
 private final Map<String,ModelPart> bones=new LinkedHashMap<>();
 private final ModelPart rig;float seconds;
 public KaidoDragonModel(){this(load());}
 private KaidoDragonModel(ModelPart root){super(root);rig=root;collect(root);}
 private void collect(ModelPart root){for(ModelPart p:root.getAllParts())for(String name:NAMES)if(p.hasChild(name))bones.put(name,p.getChild(name));}
 private static final Set<String> NAMES=new LinkedHashSet<>();
 private static float v(JsonObject o,String field,int i){return o.has(field)?o.getAsJsonArray(field).get(i).getAsFloat():0;}
 private static ModelPart load(){
  try(var in=Minecraft.getInstance().getResourceManager().getResource(Identifier.fromNamespaceAndPath("minepiece","geo/kaidou_beast.geo.json")).orElseThrow().openAsReader()){
   JsonObject g=JsonParser.parseReader(in).getAsJsonObject().getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
   Map<String,JsonObject> specs=new LinkedHashMap<>();for(JsonElement e:g.getAsJsonArray("bones")){var b=e.getAsJsonObject();specs.put(b.get("name").getAsString(),b);}NAMES.addAll(specs.keySet());
   Map<String,ModelPart> roots=new LinkedHashMap<>();for(var e:specs.entrySet())if(!e.getValue().has("parent"))roots.put(e.getKey(),bone(e.getValue(),specs));
   var root=new ModelPart(List.of(),roots);root.setInitialPose(PartPose.offset(0,24,0));root.resetPose();return root;
  }catch(IOException e){throw new IllegalStateException("Cannot load original Kaido dragon",e);}
 }
 private static ModelPart bone(JsonObject b,Map<String,JsonObject> specs){
  String name=b.get("name").getAsString();float px=v(b,"pivot",0),py=v(b,"pivot",1),pz=v(b,"pivot",2);Map<String,ModelPart> children=new LinkedHashMap<>();
  for(var e:specs.entrySet())if(e.getValue().has("parent")&&e.getValue().get("parent").getAsString().equals(name))children.put(e.getKey(),bone(e.getValue(),specs));
  int index=0;if(b.has("cubes"))for(JsonElement e:b.getAsJsonArray("cubes")){var c=e.getAsJsonObject();float cx=c.has("pivot")?v(c,"pivot",0):px,cy=c.has("pivot")?v(c,"pivot",1):py,cz=c.has("pivot")?v(c,"pivot",2):pz;
   var part=new ModelPart(List.of(cube(c,cx,cy,cz)),Map.of());part.setInitialPose(PartPose.offsetAndRotation(cx-px,py-cy,cz-pz,rad(-v(c,"rotation",0)),rad(-v(c,"rotation",1)),rad(v(c,"rotation",2))));part.resetPose();children.put("cube_"+index++,part);
  }
  var parent=b.has("parent")?specs.get(b.get("parent").getAsString()):null;float x=px-(parent==null?0:v(parent,"pivot",0)),y=(parent==null?0:v(parent,"pivot",1))-py,z=pz-(parent==null?0:v(parent,"pivot",2));
  var part=new ModelPart(List.of(),children);part.setInitialPose(PartPose.offsetAndRotation(x,y,z,rad(-v(b,"rotation",0)),rad(-v(b,"rotation",1)),rad(v(b,"rotation",2))));part.resetPose();return part;
 }
 private static float rad(float d){return d*(float)Math.PI/180;}
 private static Direction verticalFlip(Direction side){return side==Direction.UP?Direction.DOWN:side==Direction.DOWN?Direction.UP:side;}
 private static ModelPart.Cube cube(JsonObject c,float px,float py,float pz){
  float dx=v(c,"size",0),dy=v(c,"size",1),dz=v(c,"size",2),inflate=c.has("inflate")?c.get("inflate").getAsFloat():0;
  JsonObject uv=c.getAsJsonObject("uv");Set<Direction> sides=EnumSet.noneOf(Direction.class);for(String face:uv.keySet())sides.add(verticalFlip(Direction.valueOf(face.toUpperCase(Locale.ROOT))));
  var cube=new ModelPart.Cube(0,0,v(c,"origin",0)-px,py-v(c,"origin",1)-dy,v(c,"origin",2)-pz,dx,dy,dz,inflate,inflate,inflate,c.has("mirror")&&c.get("mirror").getAsBoolean(),256,256,sides);
  for(int i=0;i<cube.polygons.length;i++){var p=cube.polygons[i];var normal=p.normal();Direction side=Math.abs(normal.x())>.5?(normal.x()>0?Direction.EAST:Direction.WEST):Math.abs(normal.y())>.5?(normal.y()>0?Direction.UP:Direction.DOWN):(normal.z()>0?Direction.SOUTH:Direction.NORTH);
   var f=uv.getAsJsonObject(verticalFlip(side).name().toLowerCase(Locale.ROOT));float u=v(f,"uv",0),vv=v(f,"uv",1),w=v(f,"uv_size",0),h=v(f,"uv_size",1);
   cube.polygons[i]=new ModelPart.Polygon(p.vertices().clone(),u,vv,u+w,vv+h,256,256,false,side);
  }return cube;
 }
 void rotate(String n,float x,float y,float z){var p=bones.get(n);if(p!=null){p.xRot+=rad(-x);p.yRot+=rad(-y);p.zRot+=rad(z);}}
 void position(String n,float x,float y,float z){var p=bones.get(n);if(p!=null){p.x+=x;p.y+=y;p.z+=z;}}
 void idle(){   rotate("body",-2.5F,0.0F,0.0F);
  position("body",0.0F,58.0F,0.0F);
  rotate("torso1",7.5F,0.0F,0.0F);
  rotate("torso2",(-40.4374983796F+(float)Math.cos(seconds*Math.PI/2)*(2.5F)),3.89285F,1.20314F);
  rotate("torso3",(-40.4374983796F-(float)Math.sin(seconds*Math.PI/2)*(2.5F)),17.32866F,10.98395F);
  rotate("torso4",(41.7727309529F-(float)Math.cos(seconds*Math.PI/2)*(2.5F)),9.33515F,34.52075F);
  rotate("torso5",(52.3613333115F+(float)Math.sin(seconds*Math.PI/2)*(-2.5F)),31.79886F,41.11172F);
  rotate("torso6",(25.7340664341F+(float)Math.cos(seconds*Math.PI/2)*(2.5F)),-49.54569F,4.07195F);
  rotate("torso7",-25.8843F,-54.30026F,47.08018F);
  rotate("torso8",(-2.1767871622F-(float)Math.cos(seconds*Math.PI/2)*(2.5F)),-53.10479F,21.13216F);
  rotate("torso9",(-37.1143166542F+(float)Math.sin(seconds*Math.PI/2)*(2.5F)),-41.92831F,24.75948F);
  rotate("torso10",(-20.6066035443F-(float)Math.cos(seconds*Math.PI/2)*(2.5F)),15.90042F,19.48905F);
  rotate("torso14",(-3.519558361F-(float)Math.sin(seconds*Math.PI/2)*(2.5F)),-1.40628F,0.37253F);
  rotate("torso15",(21.5415723501F-(float)Math.cos(seconds*Math.PI/2)*(2.5F)),20.92393F,1.91403F);
  rotate("torso16",(13.7131523224F+(float)Math.sin(seconds*Math.PI/2)*(2.5F)),47.90258F,-13.30163F);
  rotate("torso17",(-6.4800876872F-(float)Math.sin(seconds*Math.PI/2)*(2.5F)),52.21677F,-8.17831F);
  rotate("torso18",(-19.2929873631F+(float)Math.cos(seconds*Math.PI/2)*(2.5F)),52.05719F,-28.51962F);
  rotate("torso11",(-35.0849650105F+(float)Math.sin(seconds*Math.PI/2)*(2.5F)),30.00652F,-11.66133F);
  rotate("torso12",(-32.5F+(float)Math.cos(seconds*Math.PI/2)*(2.5F)),0.0F,0.0F);
  rotate("torso13",(4.6394523092F-(float)Math.sin(seconds*Math.PI/2)*(5F)),-16.52171F,18.04186F);
  rotate("torso22",(36.6404423935F+(float)Math.cos(seconds*Math.PI/2)*(2.5F)),-45.51554F,-30.3434F);
  rotate("torso23",(51.0870164467F-(float)Math.sin(seconds*Math.PI/2)*(2.5F)),-61.1599F,-36.88404F);
  rotate("torso24",(-19.9716767519F+(float)Math.cos(seconds*Math.PI/2)*(2.5F)),-53.69375F,63.93404F);
  rotate("torso25",(21.8779996941F-(float)Math.sin(seconds*Math.PI/2)*(2.5F)),-24.65008F,9.9663F);
  rotate("torso26",(-25.6928423153F-(float)Math.cos(seconds*Math.PI/2)*(2.5F)),16.88547F,-4.6653F);
  rotate("torso27",(-37.226435214F+(float)Math.cos(seconds*Math.PI/2)*(2.5F)),22.52101F,-11.14843F);
  rotate("head",(15F+(float)Math.sin(seconds*Math.PI/2)*(2.5F)),0.0F,0.0F);
  rotate("arms",22.5F,-25.0F,-20.0F);
  rotate("right_arm",0.0F,7.5F,0.0F);
  rotate("right_arm2",-1.66812F,-7.90532F,-19.86983F);
  rotate("right_hand",10.0F,20.0F,0.0F);
  rotate("left_arm",-2.04897F,-7.35637F,-4.38994F);
  rotate("left_arm2",3.63648F,-6.34294F,31.7525F);
  rotate("finger6",35.0F,0.0F,0.0F);
  rotate("finger8",50.0F,0.0F,0.0F);
  rotate("finger9",27.5F,0.0F,0.0F);
  rotate("mant",3.55798F,-27.26692F,-18.43593F);
  rotate("ago",(10F+(float)Math.sin(seconds*Math.PI/2)*(5F)),0.0F,0.0F);
  rotate("right_beard",0.0F,(10F+(float)Math.cos(seconds*Math.PI/2)*(10F)),0.0F);
  rotate("left_beard",0.0F,(-10F-(float)Math.cos(seconds*Math.PI/2)*(10F)),0.0F); }
 @Override public void setupAnim(EntityRenderState state){rig.getAllParts().forEach(ModelPart::resetPose);seconds=state.ageInTicks/20;boolean stone=state instanceof ParameciaRenderData s&&s.minepiece$stone();if(stone)seconds=0;idle();
  var pose=TransformPoses.forState(state);var head=bones.get("head");if(!stone&&pose!=null){head.xRot+=pose.head().xRot()*.45F;head.yRot+=pose.head().yRot()*.45F;}
  Batch07VisualLab.beastPose(3,head.xRot,bones.get("torso2").xRot,bones.get("torso3").xRot);
 }
}
