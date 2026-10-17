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

public class ModelryuRyuBrachiosaurusHumanbeast extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath("minepiece", "ryu_ryu_brachiosaurus_humanbeast"), "main"
   );
   public final ModelPart Head;
   public final ModelPart Body;
   public final ModelPart RightArm;
   public final ModelPart LeftArm;
   public final ModelPart RightLeg;
   public final ModelPart LeftLeg;

   public ModelryuRyuBrachiosaurusHumanbeast(ModelPart root) {
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
            .texOffs(0, 0)
            .addBox(-4.0F, -18.0F, -4.0F, 8.0F, 7.0F, 5.0F, new CubeDeformation(0.2F))
            .texOffs(0, 0)
            .addBox(-4.0F, -20.0F, -7.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-4.0F, -16.0F, -10.0F, 8.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition cube_r1 = Head.addOrReplaceChild(
         "cube_r1",
         CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -17.0F, -4.0F, 6.0F, 12.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 5.2525F, 1.6476F, 0.1309F, 0.0F, 0.0F)
      );
      PartDefinition Body = partdefinition.addOrReplaceChild(
         "Body",
         CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.6F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition torso_r1 = Body.addOrReplaceChild(
         "torso_r1",
         CubeListBuilder.create()
            .texOffs(2, 0)
            .addBox(-2.0F, 3.8207F, -2.6348F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-0.9F))
            .texOffs(2, 0)
            .addBox(-2.0F, -0.6793F, -2.6348F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-0.4F)),
         PartPose.offsetAndRotation(0.0F, 15.0439F, 9.5657F, 1.5708F, 0.0F, 0.0F)
      );
      PartDefinition torso_r2 = Body.addOrReplaceChild(
         "torso_r2",
         CubeListBuilder.create().texOffs(2, 0).addBox(-2.0F, -5.387F, -2.3017F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(0.0F, 15.0439F, 9.5657F, 1.1781F, 0.0F, 0.0F)
      );
      PartDefinition torso_r3 = Body.addOrReplaceChild(
         "torso_r3",
         CubeListBuilder.create().texOffs(2, 0).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.6F)),
         PartPose.offsetAndRotation(0.0F, 11.0267F, 2.8219F, 0.7854F, 0.0F, 0.0F)
      );
      PartDefinition RightArm = partdefinition.addOrReplaceChild(
         "RightArm",
         CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.6F)),
         PartPose.offset(-5.0F, 2.0F, 0.0F)
      );
      PartDefinition LeftArm = partdefinition.addOrReplaceChild(
         "LeftArm",
         CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.6F)),
         PartPose.offset(5.0F, 2.0F, 0.0F)
      );
      PartDefinition RightLeg = partdefinition.addOrReplaceChild(
         "RightLeg",
         CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.6F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild(
         "LeftLeg",
         CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.6F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 32, 32);
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
