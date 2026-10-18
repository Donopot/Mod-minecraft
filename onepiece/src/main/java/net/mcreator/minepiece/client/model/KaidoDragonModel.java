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
   // Keep every original cube/pivot/UV, but let each section follow the travelled spine independently.
   for(String name:DragonSpineMath.BONES)specs.get(name).addProperty("parent","torso");
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
 @Override public void setupAnim(EntityRenderState state){rig.getAllParts().forEach(ModelPart::resetPose);seconds=state.ageInTicks/20;boolean stone=state instanceof ParameciaRenderData s&&s.minepiece$stone();if(stone)seconds=0;
  var pose=state instanceof DragonRenderData d?d.minepiece$dragonPose():null;
  if(pose!=null)for(int i=0;i<DragonSpineMath.BONES.length;i++){
   var joint=pose.joints().get(i);var part=bones.get(DragonSpineMath.BONES[i]);
   part.x=joint.x();part.y=joint.y();part.z=joint.z();part.xRot=joint.pitch();part.yRot=joint.yaw();part.zRot=0;
  }
  var head=bones.get("head");if(pose!=null){head.xRot=pose.headPitch();head.yRot=pose.headYaw();}
  rotate("arms",22.5F,-25,-20);rotate("right_arm2",-1.66812F,-7.90532F,-19.86983F);rotate("left_arm2",3.63648F,-6.34294F,31.7525F);
  rotate("ago",stone?10:10+(float)Math.sin(seconds*1.6)*2,0,0);
  rotate("right_beard",0,stone?10:10+(float)Math.cos(seconds*1.5)*5,0);rotate("left_beard",0,stone?-10:-10-(float)Math.cos(seconds*1.5)*5,0);
  Batch07VisualLab.beastPose(3,head.xRot,bones.get("torso2").xRot,bones.get("torso3").xRot);
 }
}
