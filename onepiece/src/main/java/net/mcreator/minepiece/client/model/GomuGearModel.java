package net.mcreator.minepiece.client.model;
import net.mcreator.minepiece.client.*;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.resources.Identifier;
/** GearFourth humanoid proportions from the reserved reference; Fifth hair/clouds authored for this port. */
public final class GomuGearModel extends EntityModel<EntityRenderState> {
 public static final ModelLayerLocation BODY=new ModelLayerLocation(Identifier.fromNamespaceAndPath("minepiece","gomu_gears"),"main"),CLOUD=new ModelLayerLocation(Identifier.fromNamespaceAndPath("minepiece","gomu_clouds"),"main");
 public final ModelPart head,body,rightArm,leftArm,rightLeg,leftLeg;private final int mode;
 public GomuGearModel(ModelPart root,int mode){super(root);this.mode=mode;head=root.getChild("head");body=root.getChild("body");rightArm=root.getChild("right_arm");leftArm=root.getChild("left_arm");rightLeg=root.getChild("right_leg");leftLeg=root.getChild("left_leg");}
 private static void part(PartDefinition r,String name,int u,int v,float x,float y,float z,float w,float h,float d,float px,float py){r.addOrReplaceChild(name,CubeListBuilder.create().texOffs(u,v).addBox(x,y,z,w,h,d),PartPose.offset(px,py,0));}
 public static LayerDefinition bodyLayer(){var mesh=new MeshDefinition();var r=mesh.getRoot();part(r,"head",0,0,-4,-8,-4,8,8,8,0,0);part(r,"body",16,16,-4,0,-2,8,12,4,0,0);part(r,"right_arm",40,16,-3,-2,-2,4,12,4,-5,2);part(r,"left_arm",32,48,-1,-2,-2,4,12,4,5,2);part(r,"right_leg",0,16,-2,0,-2,4,12,4,-2,12);part(r,"left_leg",16,48,-2,0,-2,4,12,4,2,12);return LayerDefinition.create(mesh,64,64);}
 public static LayerDefinition cloudLayer(){var mesh=new MeshDefinition();var r=mesh.getRoot();var hair=CubeListBuilder.create();for(int i=0;i<9;i++){double a=i*2.399;hair.texOffs(0,0).addBox((float)Math.cos(a)*3-2,-10-(i%3),(float)Math.sin(a)*3-2,4,4,4);}r.addOrReplaceChild("head",hair,PartPose.ZERO);
  var cloth=CubeListBuilder.create().texOffs(0,0).addBox(-4.2F,0,-2.25F,2.5F,10,4.5F).addBox(1.7F,0,-2.25F,2.5F,10,4.5F);for(int i=0;i<12;i++){double a=i*Math.PI/6;cloth.addBox((float)Math.cos(a)*7-1.6F,-2+(float)Math.sin(a)*1.4F,(float)Math.sin(a)*3.5F-1.6F,3.2F,3.2F,3.2F);}r.addOrReplaceChild("body",cloth,PartPose.ZERO);
  part(r,"right_arm",0,0,-3.3F,-2.3F,-2.3F,4.6F,5,4.6F,-5,2);part(r,"left_arm",0,0,-1.3F,-2.3F,-2.3F,4.6F,5,4.6F,5,2);part(r,"right_leg",0,0,-2.25F,0,-2.25F,4.5F,9,4.5F,-2,12);part(r,"left_leg",0,0,-2.25F,0,-2.25F,4.5F,9,4.5F,2,12);return LayerDefinition.create(mesh,16,16);}
 @Override public void setupAnim(EntityRenderState state){var p=TransformPoses.forState(state);if(p==null)return;head.loadPose(p.head());body.loadPose(p.body());rightArm.loadPose(p.rightArm());leftArm.loadPose(p.leftArm());rightLeg.loadPose(p.rightLeg());leftLeg.loadPose(p.leftLeg());int gear=(int)((TransformRenderData)state).minepiece$form();for(var part:new ModelPart[]{head,body,rightArm,leftArm,rightLeg,leftLeg}){part.xScale=part.yScale=part.zScale=1;part.visible=true;}
  if(gear==4){body.xScale=2;body.yScale=1.7F;body.zScale=3;rightArm.xScale=rightArm.yScale=rightArm.zScale=1.75F;leftArm.xScale=leftArm.yScale=leftArm.zScale=1.75F;rightArm.x-=3;leftArm.x+=3;body.y-=8.4F;head.y-=8.4F;rightArm.y-=8.4F;leftArm.y-=8.4F;}
  if(mode==1){head.visible=body.visible=rightLeg.visible=leftLeg.visible=false;rightArm.visible=leftArm.visible=gear==4;}
  if(mode==2){boolean visible=gear==5;for(var part:new ModelPart[]{head,body,rightArm,leftArm,rightLeg,leftLeg})part.visible=visible;body.y+=(float)Math.sin(state.ageInTicks*.12)*.35F;}
  net.mcreator.minepiece.client.Batch05VisualLab.gearPose(gear,mode,head.xRot,rightArm.xRot,leftArm.xRot,body.xScale);
 }
}
