package net.mcreator.minepiece.client.model;
import net.mcreator.minepiece.client.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
/** Complete final dinosaurs: reserved Brachiosaurus geometry and an authored dome-headed biped. */
public final class AncientBeastModel extends EntityModel<EntityRenderState> {
 public static final ModelLayerLocation BRACHIO=layer("brachio"),PACHY=layer("pachy"),DOME=layer("dome");private final ModelPart rig;private final int kind;
 private static ModelLayerLocation layer(String n){return new ModelLayerLocation(Identifier.fromNamespaceAndPath("minepiece","b08_beast_"+n),"main");}
 public AncientBeastModel(ModelPart r,int k){super(r);rig=r;kind=k;}
 private static PartDefinition part(PartDefinition r,String n,float x,float y,float z,float dx,float dy,float dz,float px,float py,float pz){return r.addOrReplaceChild(n,CubeListBuilder.create().texOffs(0,0).addBox(x,y,z,dx,dy,dz),PartPose.offset(px,py,pz));}
 public static LayerDefinition pachy(){var m=new MeshDefinition();var r=m.getRoot();part(r,"body",-3.5F,-4,-3,7,10,8,0,9,1);var h=part(r,"head",-4,-5,-5,8,7,8,0,4,-3);part(h,"muzzle",-3,-1,-8,6,3,5,0,0,0);
  for(int s:new int[]{-1,1}){part(r,(s<0?"right":"left")+"_front_leg",-1,0,-1,2,5,2,s*4,9,-2);var leg=part(r,(s<0?"right":"left")+"_hind_leg",-2,0,-2,4,10,4,s*3,14,1);part(leg,"foot",-2,7,-5,4,3,6,0,0,0);}
  var tail=part(r,"tail",-2,-2,0,4,4,10,0,13,6);part(tail,"tip",-1.2F,-1.2F,0,2.4F,2.4F,10,0,0,8);return LayerDefinition.create(m,32,32);
 }
 public static LayerDefinition dome(){var m=new MeshDefinition();var r=m.getRoot();var h=part(r,"head",-3.7F,-7,-4,7.4F,3,6,0,4,-3);for(int s:new int[]{-1,1}){part(h,"horn"+s,-.6F,-4,-.6F,1.2F,4,1.2F,s*4,-2,-1);part(h,"eye"+s,-.6F,-.5F,-.1F,1.2F,1,.3F,s*2.5F,-2,-5);}return LayerDefinition.create(m,32,32);}
 private void rot(String n,float x,float y,float z){if(rig.hasChild(n)){var p=rig.getChild(n);p.xRot+=x;p.yRot+=y;p.zRot+=z;}}
 @Override public void setupAnim(EntityRenderState state){rig.getAllParts().forEach(ModelPart::resetPose);if(state instanceof ParameciaRenderData stone&&stone.minepiece$stone())return;var p=TransformPoses.forState(state);if(p==null)return;rot("head",p.head().xRot()*.55F,p.head().yRot()*.5F,0);rot("right_front_leg",p.leftLeg().xRot()*.5F+p.rightArm().xRot()*.4F,0,0);rot("left_front_leg",p.rightLeg().xRot()*.5F+p.leftArm().xRot()*.4F,0,0);rot("right_hind_leg",p.rightLeg().xRot()*.6F,0,0);rot("left_hind_leg",p.leftLeg().xRot()*.6F,0,0);rot("tail",0,(float)Math.sin(state.ageInTicks*.09)*.16F,0);
  Batch08VisualLab.beastPose(kind,rig.getChild("head").xRot,rig.hasChild("right_front_leg")?rig.getChild("right_front_leg").xRot:0,rig.hasChild("left_front_leg")?rig.getChild("left_front_leg").xRot:0);
 }
}
