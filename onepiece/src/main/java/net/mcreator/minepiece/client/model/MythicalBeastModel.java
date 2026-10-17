package net.mcreator.minepiece.client.model;

import net.mcreator.minepiece.client.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

/** Complete B07 forms. Reserved reference geometry is visual only; Makami and dragon are authored rigs. */
public final class MythicalBeastModel extends EntityModel<EntityRenderState> {
 public static final ModelLayerLocation MAKAMI=layer("makami"),MAKAMI_MANE=layer("makami_mane"),MAKAMI_FACE=layer("makami_face"),DAIBUTSU=layer("daibutsu"),PHOENIX=layer("phoenix"),PHOENIX_WINGS=layer("phoenix_wings"),PTERO=layer("ptero"),DRAGON=layer("dragon"),DRAGON_TRIM=layer("dragon_trim");
 private final ModelPart rig;private final int kind;
 private static ModelLayerLocation layer(String id){return new ModelLayerLocation(Identifier.fromNamespaceAndPath("minepiece","b07_beast_"+id),"main");}
 public MythicalBeastModel(ModelPart root,int kind){super(root);rig=root;this.kind=kind;}
 private static PartDefinition part(PartDefinition r,String n,float x,float y,float z,float dx,float dy,float dz,float px,float py,float pz){return r.addOrReplaceChild(n,CubeListBuilder.create().texOffs(0,0).addBox(x,y,z,dx,dy,dz),PartPose.offset(px,py,pz));}
 public static LayerDefinition makami(){var m=new MeshDefinition();var r=m.getRoot();part(r,"body",-3.5F,0,-7,7,7,15,0,10,0);var h=part(r,"head",-3.5F,-3.5F,-5,7,7,6,0,8,-7);part(h,"muzzle",-2,-1,-9,4,3,5,0,0,0);part(h,"ear_l",2,-8,-1,2,5,2,0,0,0);part(h,"ear_r",-4,-8,-1,2,5,2,0,0,0);
  for(int s:new int[]{-1,1}){var horn=part(h,"horn"+s,-.6F,-5,-.6F,1.2F,5,1.2F,s*3,-4,0);horn=part(horn,"tip",-.5F,-3,-.5F,1,3,1,s*.5F,-4,0);}
  for(String n:new String[]{"left_front_leg","right_front_leg","left_hind_leg","right_hind_leg"})part(r,n,-1.25F,0,-1.25F,2.5F,8,2.5F,n.startsWith("left")?2.5F:-2.5F,16,n.contains("front")?-5:6);
  var tail=part(r,"tail",-1.5F,0,-1.5F,3,7,3,0,9,8);tail=part(tail,"curl",-1.5F,0,-1.5F,3,6,3,0,-5,2);return LayerDefinition.create(m,32,32);
 }
 public static LayerDefinition makamiMane(){var m=new MeshDefinition();var r=m.getRoot();var h=part(r,"head",-4,-1,-3,8,5,7,0,8,-7);for(int s:new int[]{-1,1})for(int i=0;i<4;i++)part(h,"flame"+s+"_"+i,-1,-4,-1,2,7-i,2,s*(3+i*.8F),-i*.5F,i*2);part(r,"body",-4,0,-6,8,2,13,0,9,0);return LayerDefinition.create(m,32,32);}
 public static LayerDefinition makamiFace(){var m=new MeshDefinition();var r=m.getRoot();var h=part(r,"head",-1.2F,-.6F,-9.1F,2.4F,1.5F,.8F,0,8,-7);for(int s:new int[]{-1,1}){part(h,"eye"+s,-.7F,-.4F,-.2F,1.4F,.8F,.4F,s*2.2F,-1.7F,-5);part(h,"eyebrow"+s,-.9F,-.2F,-.2F,1.8F,.4F,.4F,s*2.2F,-2.5F,-5);part(h,"mouth"+s,-.1F,-.2F,-.1F,.2F,.4F,3,s*1.6F,1.5F,-8.5F);}return LayerDefinition.create(m,16,16);}
 public static LayerDefinition phoenixWings(){var m=new MeshDefinition();var r=m.getRoot();r.addOrReplaceChild("head",CubeListBuilder.create(),PartPose.ZERO);for(int s:new int[]{-1,1}){var wing=r.addOrReplaceChild(s<0?"right_front_leg":"left_front_leg",CubeListBuilder.create(),PartPose.offset(s*3.75F,13.5842F,2.295F));for(int i=0;i<9;i++){float x=s<0?-(i+1)*4:i*4;part(wing,"feather"+i,x,-.15F,i*.65F,5,.3F,15-i,0,0,0);}part(wing,"leading",s<0?-37:0,-.4F,-.6F,37,.8F,1.2F,0,0,0);}return LayerDefinition.create(m,64,64);}
 public static LayerDefinition dragon(){return dragon(false);}
 public static LayerDefinition dragonTrim(){return dragon(true);}
 private static LayerDefinition dragon(boolean trim){var m=new MeshDefinition();var r=m.getRoot();
  var head=part(r,"head",trim?-6:-5,trim?-4:-5,-8,trim?12:10,trim?2:9,trim?8:12,0,-6,-13);
  if(!trim){part(head,"muzzle",-4,-1,-15,8,5,10,0,0,0);part(head,"jaw",-3,3,-14,6,2,10,0,0,0);}
  else{for(int s:new int[]{-1,1}){var horn=part(head,"horn"+s,-.6F,-9,-.6F,1.2F,9,1.2F,s*4,-2,0);part(horn,"fork",-.5F,-4,-.5F,1,5,1,s*2,-5,0);part(head,"whisker"+s,-.4F,-.4F,-.4F,1,1,12,s*4,1,-12);part(head,"eye"+s,-.7F,-.7F,-.7F,1.4F,1.4F,1.4F,s*4.7F,-2,-7);}}
  var body=part(r,"body",trim?-4:-5,trim?-5:-5,-6,trim?8:10,trim?2:10,12,0,2,-3);
  for(int i=0;i<5;i++){body=part(body,"segment"+i,trim?-3:-4,-4,0,trim?6:8,trim?2:8,10,0,i==0?5:1,i==0?6:8);if(trim)part(body,"spine",-.5F,-8,1,1,5,6,0,0,0);}
  if(!trim)for(int s:new int[]{-1,1})for(int i=0;i<2;i++){var leg=part(r,(s<0?"right":"left")+(i==0?"_front_leg":"_hind_leg"),-1,0,-1,3,8,3,s*5,7+i*3,-2+i*17);part(leg,"claw",-2,6,-5,5,2,7,0,0,0);}
  return LayerDefinition.create(m,64,64);
 }
 private void rot(String name,float x,float y,float z){if(rig.hasChild(name)){var p=rig.getChild(name);p.xRot+=x;p.yRot+=y;p.zRot+=z;}}
 @Override public void setupAnim(EntityRenderState state){rig.getAllParts().forEach(p->{p.resetPose();p.visible=true;});if(state instanceof ParameciaRenderData stone&&stone.minepiece$stone())return;var pose=TransformPoses.forState(state);if(pose==null)return;boolean flying=state instanceof TransformRenderData d&&d.minepiece$airborne();
  rot("head",pose.head().xRot()*.6F,pose.head().yRot()*.65F,pose.head().zRot()*.5F);
  if(kind==1){rot("headwear",pose.head().xRot()*.6F,pose.head().yRot()*.65F,0);for(String n:new String[]{"right_arm","right_sleeve"})rot(n,pose.rightArm().xRot(),pose.rightArm().yRot(),pose.rightArm().zRot());for(String n:new String[]{"left_arm","left_sleeve"})rot(n,pose.leftArm().xRot(),pose.leftArm().yRot(),pose.leftArm().zRot());rot("left_leg",pose.leftLeg().xRot(),0,0);rot("right_leg",pose.rightLeg().xRot(),0,0);}
  else if(kind==2||kind==3||kind==5||kind==8){float flap=flying?(float)Math.sin(state.ageInTicks*.3)*.4F:.1F;rot("left_front_leg",0,flap*.7F,flap);rot("right_front_leg",0,-flap*.7F,-flap);rot("left_hind_leg",flying?-.4F:pose.leftLeg().xRot(),0,0);rot("right_hind_leg",flying?-.4F:pose.rightLeg().xRot(),0,0);
   if(kind==8&&!flying){rot("left_front_leg",0,0,1.0F);rot("right_front_leg",0,0,-1.0F);}
   if(kind==2){rig.getChild("left_front_leg").getAllParts().forEach(p->p.visible=false);rig.getChild("right_front_leg").getAllParts().forEach(p->p.visible=false);}
   if(kind==5){for(String name:new String[]{"left_hind_leg","right_hind_leg","left_front_leg","right_front_leg"})rig.getChild(name).getAllParts().forEach(p->p.visible=false);}
  }else{rot("right_front_leg",pose.leftLeg().xRot()*.65F+pose.rightArm().xRot()*.4F,0,0);rot("left_front_leg",pose.rightLeg().xRot()*.65F+pose.leftArm().xRot()*.4F,0,0);rot("left_hind_leg",pose.leftLeg().xRot()*.65F,0,0);rot("right_hind_leg",pose.rightLeg().xRot()*.65F,0,0);rot("tail",-.9F,(float)Math.sin(state.ageInTicks*.1)*.2F,0);
   if(kind==4||kind==7){var b=rig.getChild("body");for(int i=0;i<5;i++){b=b.getChild("segment"+i);b.yRot+=(float)Math.sin(state.ageInTicks*.07-i*.8)*.18F;b.xRot+=(float)Math.sin(state.ageInTicks*.06-i*.6)*.08F;}}
  }
  if(kind<5){int slot=kind==4?3:kind==3?4:kind;Batch07VisualLab.beastPose(slot,rig.getChild("head").xRot,rig.hasChild("right_front_leg")?rig.getChild("right_front_leg").xRot:rig.getChild("right_arm").xRot,rig.hasChild("left_front_leg")?rig.getChild("left_front_leg").xRot:rig.getChild("left_arm").xRot);}
 }
}
