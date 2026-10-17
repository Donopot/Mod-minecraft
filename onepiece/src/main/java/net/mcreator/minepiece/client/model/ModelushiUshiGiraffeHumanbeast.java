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
import net.minecraft.client.renderer.entity.state.EntityRenderState;

public class ModelushiUshiGiraffeHumanbeast extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath("minepiece", "ushi_ushi_giraffe_humanbeast"), "main"
   );
   public final ModelPart Head;
   public final ModelPart Body;
   public final ModelPart RightArm;
   public final ModelPart LeftArm;
   public final ModelPart RightLeg;
   public final ModelPart LeftLeg;

   public ModelushiUshiGiraffeHumanbeast(ModelPart root) {
      super(root);
      this.Head = root.getChild("Head");
      this.Body = root.getChild("Body");
      this.RightArm = root.getChild("RightArm");
      this.LeftArm = root.getChild("LeftArm");
      this.RightLeg = root.getChild("RightLeg");
      this.LeftLeg = root.getChild("LeftLeg");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition Head = partdefinition.addOrReplaceChild(
         "Head",
         CubeListBuilder.create()
            .texOffs(32, 0)
            .addBox(-3.0F, -22.0F, -4.0F, 6.0F, 6.0F, 5.0F, new CubeDeformation(0.1F))
            .texOffs(18, 13)
            .addBox(-3.0F, -22.0F, -10.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(26, 14)
            .addBox(-1.0F, -20.0F, -5.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(1.5F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition cube_r1 = Head.addOrReplaceChild(
         "cube_r1",
         CubeListBuilder.create()
            .texOffs(48, 54)
            .addBox(-1.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(12, 54)
            .addBox(-4.0F, -3.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(1.5F, -21.0F, -1.5F, -0.3491F, 0.0F, 0.0F)
      );
      PartDefinition cube_r2 = Head.addOrReplaceChild(
         "cube_r2",
         CubeListBuilder.create().texOffs(12, 51).addBox(-2.0F, -1.0F, -1.0F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(3.0F, -20.0F, -0.25F, -0.9616F, -0.4488F, 0.3894F)
      );
      PartDefinition cube_r3 = Head.addOrReplaceChild(
         "cube_r3",
         CubeListBuilder.create().texOffs(42, 28).addBox(-3.0F, -1.0F, -1.0F, 5.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-3.0F, -20.0F, -0.25F, -0.9616F, 0.4488F, -0.3894F)
      );
      PartDefinition cube_r4 = Head.addOrReplaceChild(
         "cube_r4",
         CubeListBuilder.create().texOffs(34, 33).addBox(0.0F, -19.0F, 2.0F, 1.0F, 18.0F, 2.0F, new CubeDeformation(-0.1F)),
         PartPose.offsetAndRotation(-0.5F, 0.2525F, 1.0476F, 0.1309F, 0.0F, 0.0F)
      );
      PartDefinition cube_r5 = Head.addOrReplaceChild(
         "cube_r5",
         CubeListBuilder.create().texOffs(0, 13).addBox(-2.0F, -17.0F, -3.0F, 4.0F, 16.0F, 5.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 0.2525F, 1.1476F, 0.1309F, 0.0F, 0.0F)
      );
      PartDefinition Body = partdefinition.addOrReplaceChild(
         "Body",
         CubeListBuilder.create()
            .texOffs(18, 23)
            .addBox(-4.0F, 6.0F, -2.0F, 8.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-5.0F, -1.0F, -3.0F, 10.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(40, 42)
            .addBox(-0.5F, -2.0F, 2.0F, 1.0F, 14.0F, 3.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition cube_r6 = Body.addOrReplaceChild(
         "cube_r6",
         CubeListBuilder.create()
            .texOffs(0, 57)
            .addBox(-2.0F, -2.0F, 8.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(25, 54)
            .addBox(-1.0F, -1.0F, -1.0F, 1.0F, 1.0F, 9.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.5F, 13.0F, 2.0F, -0.7418F, 0.0F, 0.0F)
      );
      PartDefinition RightArm = partdefinition.addOrReplaceChild(
         "RightArm",
         CubeListBuilder.create()
            .texOffs(42, 20)
            .addBox(-3.0F, 7.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(10, 22)
            .addBox(-2.0F, 2.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.5F)),
         PartPose.offset(-5.0F, 2.0F, 0.0F)
      );
      PartDefinition right_arm_r1 = RightArm.addOrReplaceChild(
         "right_arm_r1",
         CubeListBuilder.create().texOffs(40, 33).addBox(-2.0F, -2.5F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-1.0F, 0.5F, 0.0F, 0.0F, 0.0F, 0.2618F)
      );
      PartDefinition LeftArm = partdefinition.addOrReplaceChild(
         "LeftArm",
         CubeListBuilder.create()
            .texOffs(16, 43)
            .addBox(-1.0F, 7.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(10, 22)
            .mirror()
            .addBox(0.0F, 2.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.5F))
            .mirror(false),
         PartPose.offset(5.0F, 2.0F, 0.0F)
      );
      PartDefinition left_arm_r1 = LeftArm.addOrReplaceChild(
         "left_arm_r1",
         CubeListBuilder.create().texOffs(42, 11).addBox(-2.0F, -2.5F, -2.0F, 4.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(1.0F, 0.5F, 0.0F, 0.0F, 0.0F, -0.2618F)
      );
      PartDefinition RightLeg = partdefinition.addOrReplaceChild(
         "RightLeg",
         CubeListBuilder.create()
            .texOffs(0, 44)
            .addBox(-3.0F, 10.0F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(32, 11)
            .addBox(-3.0F, 11.0F, -3.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition right_leg_r1 = RightLeg.addOrReplaceChild(
         "right_leg_r1",
         CubeListBuilder.create().texOffs(26, 51).addBox(-1.0F, -2.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-1.366F, 8.7787F, 0.5913F, -0.2184F, 0.0425F, -0.0094F)
      );
      PartDefinition right_leg_r2 = RightLeg.addOrReplaceChild(
         "right_leg_r2",
         CubeListBuilder.create()
            .texOffs(48, 48)
            .addBox(-1.0F, 4.0F, 2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(18, 33)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0359F, -0.7263F, 0.1758F, -0.597F, 0.0884F, 0.2044F)
      );
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild(
         "LeftLeg",
         CubeListBuilder.create()
            .texOffs(48, 42)
            .addBox(0.0F, 10.0F, -2.0F, 3.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(42, 31)
            .addBox(0.0F, 11.0F, -3.0F, 3.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      PartDefinition left_leg_r1 = LeftLeg.addOrReplaceChild(
         "left_leg_r1",
         CubeListBuilder.create().texOffs(54, 0).addBox(-1.0F, -2.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(1.366F, 8.7787F, 0.5913F, -0.2184F, -0.0425F, 0.0094F)
      );
      PartDefinition left_leg_r2 = LeftLeg.addOrReplaceChild(
         "left_leg_r2",
         CubeListBuilder.create()
            .texOffs(0, 50)
            .addBox(-1.0F, 4.0F, 2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(0, 34)
            .addBox(-2.0F, 0.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-0.0359F, -0.7263F, 0.1758F, -0.597F, -0.0884F, -0.2044F)
      );
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
   }
}
