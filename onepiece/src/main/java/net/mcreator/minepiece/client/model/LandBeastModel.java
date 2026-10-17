package net.mcreator.minepiece.client.model;

import net.mcreator.minepiece.client.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

/** Full animal rigs. Native reference quadrupeds retain their rest pivots; player poses add rotation only. */
public final class LandBeastModel extends EntityModel<EntityRenderState> {
 public static final ModelLayerLocation WOLF=layer("wolf"),LEOPARD=layer("leopard"),SABER_FANGS=layer("saber_fangs"),SABER_MARKS=layer("saber_marks"),GIRAFFE=layer("giraffe"),BAT=layer("bat"),WOLF_FACE=layer("wolf_face");
 private final ModelPart rig;private final int kind;
 private static ModelLayerLocation layer(String id){return new ModelLayerLocation(Identifier.fromNamespaceAndPath("minepiece","b06_beast_"+id),"main");}
 public LandBeastModel(ModelPart root,int kind){super(root);rig=root;this.kind=kind;}
 private static void part(PartDefinition root,String name,float x,float y,float z,float dx,float dy,float dz,float px,float py,float pz){root.addOrReplaceChild(name,CubeListBuilder.create().texOffs(0,0).addBox(x,y,z,dx,dy,dz),PartPose.offset(px,py,pz));}
 public static LayerDefinition wolf(){var mesh=new MeshDefinition();var r=mesh.getRoot();part(r,"body",-3,0,-6,6,7,13,0,11,0);part(r,"head",-3,-3,-5,6,6,6,0,10,-6);var h=r.getChild("head");part(h,"muzzle",-2,-.5F,-8,4,3,4,0,0,0);part(h,"ear_l",1,-6,-2,2,4,2,0,0,0);part(h,"ear_r",-3,-6,-2,2,4,2,0,0,0);for(String name:new String[]{"left_front_leg","right_front_leg","left_hind_leg","right_hind_leg"})part(r,name,-1.25F,0,-1.25F,2.5F,7,2.5F,name.startsWith("left")?2:-2,17,name.contains("front")?-4:5);part(r,"tail",-1,0,-1,2,9,2,0,11,7);return LayerDefinition.create(mesh,32,32);}
 public static LayerDefinition wolfFace(){var mesh=new MeshDefinition();var r=mesh.getRoot();part(r,"head",-1.25F,-.5F,-8.1F,2.5F,1.5F,1,0,10,-6);var h=r.getChild("head");part(h,"eye_l",2,-1.5F,-5.1F,1,1,.2F,0,0,0);part(h,"eye_r",-3,-1.5F,-5.1F,1,1,.2F,0,0,0);return LayerDefinition.create(mesh,16,16);}
 public static LayerDefinition fangs(){var mesh=new MeshDefinition();var r=mesh.getRoot();r.addOrReplaceChild("head",CubeListBuilder.create().addBox(-2.2F,.2F,-7.5F,1.2F,5,1.5F).addBox(1,.2F,-7.5F,1.2F,5,1.5F).addBox(-1.6F,1,-7.9F,3.2F,2,1),PartPose.offsetAndRotation(-.125F,13.3127F,-5.4117F,.1309F,0,0));return LayerDefinition.create(mesh,32,32);}
 public static LayerDefinition saberMarks(){var mesh=new MeshDefinition();var r=mesh.getRoot();var stripes=CubeListBuilder.create();for(int i=0;i<5;i++){float z=-5+i*2.4F;stripes.addBox(-3.05F,-3.5F,z,6.1F,.2F,.75F).addBox(-3.15F,-3.4F,z,.2F,4,.75F).addBox(2.95F,-3.4F,z,.2F,4,.75F);}r.addOrReplaceChild("body",stripes,PartPose.offset(-.0625F,14.4688F,1.75F));r.addOrReplaceChild("head",CubeListBuilder.create().addBox(-.9F,.2F,-8,1.8F,1,1).addBox(-2.5F,-.8F,-5.4F,1.5F,.6F,.3F).addBox(1,-.8F,-5.4F,1.5F,.6F,.3F).addBox(-.4F,-2.15F,-5.3F,.8F,1.4F,5.5F),PartPose.offsetAndRotation(-.125F,13.3127F,-5.4117F,.1309F,0,0));return LayerDefinition.create(mesh,32,32);}
 public static LayerDefinition bat(){var mesh=new MeshDefinition();var r=mesh.getRoot();part(r,"body",-2,-2,-1.5F,4,7,3,0,15,0);part(r,"head",-2.5F,-3,-3,5,5,4,0,13,0);var h=r.getChild("head");part(h,"ear_l",1,-7,-1,1.5F,5,1,0,0,0);part(h,"ear_r",-2.5F,-7,-1,1.5F,5,1,0,0,0);
  for(int side:new int[]{-1,1}){String name=side<0?"right_wing":"left_wing";part(r,name,side<0?-12:0,0,0,12,.5F,2,side*2,14,0);var wing=r.getChild(name);for(int i=0;i<3;i++)part(wing,"membrane"+i,side<0?-12+i*2:i*2,0,1,8-i*2,.2F,6-i,0,0,0);part(wing,"finger",side<0?-13:0,-.2F,-.2F,13,1,1,0,0,0);}
  part(r,"left_hind_leg",-1,0,-1,1.5F,3,2,1.5F,21,0);part(r,"right_hind_leg",-1,0,-1,1.5F,3,2,-1,21,0);return LayerDefinition.create(mesh,32,32);
 }
 private void rotate(String name,float x,float y,float z){if(rig.hasChild(name)){var p=rig.getChild(name);p.xRot+=x;p.yRot+=y;p.zRot+=z;}}
 @Override public void setupAnim(EntityRenderState state){rig.getAllParts().forEach(ModelPart::resetPose);if(state instanceof ParameciaRenderData stone&&stone.minepiece$stone())return;var poses=TransformPoses.forState(state);if(poses==null)return;
  rotate("head",poses.head().xRot(),poses.head().yRot()*.65F,poses.head().zRot());float left=poses.leftLeg().xRot()*.65F,right=poses.rightLeg().xRot()*.65F;
  rotate("left_hind_leg",left,0,0);rotate("right_front_leg",left+poses.rightArm().xRot()*.45F,0,poses.rightArm().zRot()*.3F);rotate("right_hind_leg",right,0,0);rotate("left_front_leg",right+poses.leftArm().xRot()*.45F,0,poses.leftArm().zRot()*.3F);if(kind==0)rotate("tail",-.9F, (float)Math.sin(state.ageInTicks*.1)*.12F,0);
  if(kind==4){boolean flying=state instanceof TransformRenderData d&&d.minepiece$airborne();float flap=flying?(float)Math.sin(state.ageInTicks*.5)*.65F:.9F;rotate("left_wing",0,0,flap);rotate("right_wing",0,0,-flap);rotate("body",poses.body().xRot()*.4F,0,0);}
  net.mcreator.minepiece.client.Batch06VisualLab.beastPose(kind,rig.getChild("head").xRot,rig.hasChild("right_front_leg")?rig.getChild("right_front_leg").xRot:poses.rightArm().xRot(),rig.hasChild("left_front_leg")?rig.getChild("left_front_leg").xRot:poses.leftArm().xRot());
 }
}
