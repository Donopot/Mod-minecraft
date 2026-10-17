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

public class ModelryuRyuPachycephalosaurusHumanbeast2 extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath("minepiece", "ryu_ryu_pachycephalosaurus_humanbeast2"), "main"
   );
   public final ModelPart Head;
   public final ModelPart Body;
   public final ModelPart RightArm;
   public final ModelPart LeftArm;
   public final ModelPart RightLeg;
   public final ModelPart LeftLeg;

   public ModelryuRyuPachycephalosaurusHumanbeast2(ModelPart root) {
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
      PartDefinition Head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition headwear_r1 = Head.addOrReplaceChild(
         "headwear_r1",
         CubeListBuilder.create()
            .texOffs(8, 10)
            .addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 10)
            .addBox(-2.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F)),
         PartPose.offsetAndRotation(-1.3F, -4.5F, -4.0F, -0.7854F, -0.3927F, 0.7854F)
      );
      PartDefinition headwear_r2 = Head.addOrReplaceChild(
         "headwear_r2",
         CubeListBuilder.create()
            .texOffs(8, 10)
            .addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(8, 10)
            .addBox(-2.5F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F)),
         PartPose.offsetAndRotation(-2.5266F, -4.6266F, -3.9913F, -0.7854F, -0.3927F, 0.3927F)
      );
      PartDefinition headwear_r3 = Head.addOrReplaceChild(
         "headwear_r3",
         CubeListBuilder.create()
            .texOffs(10, 10)
            .addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(10, 10)
            .addBox(0.5F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F)),
         PartPose.offsetAndRotation(2.5266F, -4.6266F, -3.9913F, -0.7854F, 0.3927F, -0.3927F)
      );
      PartDefinition headwear_r4 = Head.addOrReplaceChild(
         "headwear_r4",
         CubeListBuilder.create()
            .texOffs(10, 10)
            .addBox(0.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(-0.2F))
            .texOffs(10, 10)
            .addBox(-1.0F, -0.5F, -0.5F, 2.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(1.3F, -4.5F, -4.0F, -0.7854F, 0.3927F, -0.7854F)
      );
      PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
      PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
      PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create(), PartPose.offset(-2.0F, 12.0F, 0.0F));
      PartDefinition right_leg_r1 = RightLeg.addOrReplaceChild(
         "right_leg_r1",
         CubeListBuilder.create().texOffs(0, 16).addBox(-4.5F, -1.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F)
      );
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild("LeftLeg", CubeListBuilder.create(), PartPose.offset(2.0F, 12.0F, 0.0F));
      PartDefinition left_leg_r1 = LeftLeg.addOrReplaceChild(
         "left_leg_r1",
         CubeListBuilder.create().texOffs(16, 48).addBox(0.5F, -1.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F)
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
