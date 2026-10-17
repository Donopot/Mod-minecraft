package net.mcreator.minepiece.client.model;

import net.mcreator.minepiece.client.TransformPoses;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;

/** Private adaptation of the user-provided Daibutsu/Volt Amaru silhouette to 26.3 render states. */
public final class AmaruModel extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION=new ModelLayerLocation(Identifier.fromNamespaceAndPath("minepiece","amaru"),"main");
   public final ModelPart head,body,rightArm,leftArm,rightLeg,leftLeg;
   public AmaruModel(ModelPart root){super(root);head=root.getChild("head");body=root.getChild("body");rightArm=root.getChild("right_arm");leftArm=root.getChild("left_arm");rightLeg=root.getChild("right_leg");leftLeg=root.getChild("left_leg");}
   public static LayerDefinition createBodyLayer(){
      var mesh=new MeshDefinition();var r=mesh.getRoot();
      part(r,"head",0,0,-4,-8,-4,8,8,8,0,0,0,0);
      var face=r.getChild("head");
      face.addOrReplaceChild("eyes",CubeListBuilder.create().texOffs(60,60)
         .addBox(-2.9F,-5.3F,-4.4F,1.8F,.75F,.3F).texOffs(60,60).addBox(1.1F,-5.3F,-4.4F,1.8F,.75F,.3F),PartPose.ZERO);
      face.addOrReplaceChild("ears",CubeListBuilder.create().texOffs(4,4)
         .addBox(-5.25F,-4.5F,-.5F,1.2F,9,1).texOffs(4,4).addBox(4.05F,-4.5F,-.5F,1.2F,9,1),PartPose.ZERO);
      part(r,"body",16,16,-4,0,-2,8,12,4,3,0,0,0);
      part(r,"right_arm",40,16,-3,-2,-2,4,12,4,2,-5,2,0);
      part(r,"left_arm",32,48,-1,-2,-2,4,12,4,2,5,2,0);
      part(r,"right_leg",0,16,-2,0,-2,4,12,4,1,-2,12,0);
      part(r,"left_leg",16,48,-2,0,-2,4,12,4,1,2,12,0);
      return LayerDefinition.create(mesh,64,64);
   }
   private static void part(PartDefinition root,String name,int u,int v,float x,float y,float z,float w,float h,float depth,float inflate,float px,float py,float pz){
      root.addOrReplaceChild(name,CubeListBuilder.create().texOffs(u,v).addBox(x,y,z,w,h,depth,new CubeDeformation(inflate))
         .texOffs(u,v).addBox(x,y,z,w,h,depth,new CubeDeformation(inflate+.25F)),PartPose.offset(px,py,pz));
   }
   @Override public void setupAnim(EntityRenderState state){
      var p=TransformPoses.forState(state);if(p==null)return;
      head.loadPose(p.head());body.loadPose(p.body());rightArm.loadPose(p.rightArm());leftArm.loadPose(p.leftArm());rightLeg.loadPose(p.rightLeg());leftLeg.loadPose(p.leftLeg());
      net.mcreator.minepiece.client.AmaruRenderProbe.evaluated(state,this);
   }
}
