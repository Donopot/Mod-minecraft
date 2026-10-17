package net.mcreator.minepiece.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class ModelwingsBatBatFly extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("minepiece", "modelwings_bat_bat_fly"), "main");
   public final ModelPart Head;
   public final ModelPart Body;
   public final ModelPart wing_right;
   public final ModelPart wing_right2;
   public final ModelPart wing_left;
   public final ModelPart wing_left2;
   public final ModelPart RightArm;
   public final ModelPart LeftArm;
   public final ModelPart RightLeg;
   public final ModelPart LeftLeg;

   public ModelwingsBatBatFly(ModelPart root) {
      super(root);
      this.Head = root.getChild("Head");
      this.Body = root.getChild("Body");
      this.wing_right = this.Body.getChild("wing_right");
      this.wing_right2 = this.wing_right.getChild("wing_right2");
      this.wing_left = this.Body.getChild("wing_left");
      this.wing_left2 = this.wing_left.getChild("wing_left2");
      this.RightArm = root.getChild("RightArm");
      this.LeftArm = root.getChild("LeftArm");
      this.RightLeg = root.getChild("RightLeg");
      this.LeftLeg = root.getChild("LeftLeg");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition Head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition wing_right = Body.addOrReplaceChild("wing_right", CubeListBuilder.create(), PartPose.offset(-1.0F, 2.0F, 2.0F));
      PartDefinition jacket_r1 = wing_right.addOrReplaceChild(
         "jacket_r1",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-9.8018F, -10.0F, 0.0202F, 10.0F, 36.0F, 0.0F, new CubeDeformation(0.2F))
            .mirror(false),
         PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.0F, 0.7854F, 0.0F)
      );
      PartDefinition wing_right2 = wing_right.addOrReplaceChild("wing_right2", CubeListBuilder.create(), PartPose.offset(-6.8018F, -9.0F, 7.0202F));
      PartDefinition jacket_r2 = wing_right2.addOrReplaceChild(
         "jacket_r2",
         CubeListBuilder.create()
            .texOffs(32, 0)
            .mirror()
            .addBox(-22.0018F, -10.0F, 0.0202F, 12.0F, 36.0F, 0.0F, new CubeDeformation(0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(6.8018F, 8.0F, -7.0202F, 0.0F, 0.7854F, 0.0F)
      );
      PartDefinition wing_left = Body.addOrReplaceChild("wing_left", CubeListBuilder.create(), PartPose.offset(1.0F, 2.0F, 2.0F));
      PartDefinition jacket_r3 = wing_left.addOrReplaceChild(
         "jacket_r3",
         CubeListBuilder.create().texOffs(0, 0).addBox(-0.1982F, -10.0F, 0.0202F, 10.0F, 36.0F, 0.0F, new CubeDeformation(0.2F)),
         PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.0F, -0.7854F, 0.0F)
      );
      PartDefinition wing_left2 = wing_left.addOrReplaceChild("wing_left2", CubeListBuilder.create(), PartPose.offset(6.8018F, -9.0F, 7.0202F));
      PartDefinition jacket_r4 = wing_left2.addOrReplaceChild(
         "jacket_r4",
         CubeListBuilder.create().texOffs(32, 0).addBox(10.0018F, -10.0F, 0.0202F, 12.0F, 36.0F, 0.0F, new CubeDeformation(0.25F)),
         PartPose.offsetAndRotation(-6.8018F, 8.0F, -7.0202F, 0.0F, -0.7854F, 0.0F)
      );
      PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
      PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
      PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create(), PartPose.offset(-2.0F, 12.0F, 0.0F));
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild("LeftLeg", CubeListBuilder.create(), PartPose.offset(2.0F, 12.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 64, 64);
   }


   @Override
   public void setupAnim(EntityRenderState state) {
      net.mcreator.minepiece.client.TransformPoses.Snapshot parent = net.mcreator.minepiece.client.TransformPoses.forState(state);
      if (parent != null) {
         Head.loadPose(parent.head());
         Body.loadPose(parent.body());
         RightArm.loadPose(parent.rightArm());
         LeftArm.loadPose(parent.leftArm());
         RightLeg.loadPose(parent.rightLeg());
         LeftLeg.loadPose(parent.leftLeg());
      }
      float age = state.ageInTicks;
      wing_left2.zRot = Mth.cos(age * 0.6F + 3.0F) * 0.6F;
      wing_left.zRot = Mth.cos(age * 0.6F + 3.0F) * 0.6F;
      wing_right.zRot = Mth.cos(age * 0.6F) * 0.6F;
      wing_right2.zRot = Mth.cos(age * 0.6F) * 0.6F;
   }
}
