package net.mcreator.minepiece.client.model;

import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.Identifier;

public class ModelsupasupaForm2 extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("minepiece", "supasupa_form2"), "main");
   public final ModelPart Head;
   public final ModelPart Body;
   public final ModelPart RightArm;
   public final ModelPart LeftArm;
   public final ModelPart RightLeg;
   public final ModelPart LeftLeg;

   public ModelsupasupaForm2(ModelPart root) {
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
      PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
      PartDefinition right_arm_r1 = RightArm.addOrReplaceChild(
         "right_arm_r1",
         CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -6.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(-0.1F)),
         PartPose.offsetAndRotation(-1.0F, 4.0F, 0.0F, 0.0F, -0.7854F, 0.0F)
      );
      PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
      PartDefinition left_arm_r1 = LeftArm.addOrReplaceChild(
         "left_arm_r1",
         CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -6.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(-0.1F)),
         PartPose.offsetAndRotation(1.0F, 4.0F, 0.0F, 0.0F, 0.7854F, 0.0F)
      );
      PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create(), PartPose.offset(-2.0F, 12.0F, 0.0F));
      PartDefinition right_arm_r2 = RightLeg.addOrReplaceChild(
         "right_arm_r2",
         CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -6.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(-0.1F)),
         PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 0.0F, -0.7854F, 0.0F)
      );
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild("LeftLeg", CubeListBuilder.create(), PartPose.offset(2.0F, 12.0F, 0.0F));
      PartDefinition left_arm_r2 = LeftLeg.addOrReplaceChild(
         "left_arm_r2",
         CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -6.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(-0.1F)),
         PartPose.offsetAndRotation(0.0F, 6.0F, 0.0F, 0.0F, 0.7854F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 64, 64);
   }

   @Override
   public void setupAnim(EntityRenderState state) {
      // Synchronisation des os avec le modèle joueur (remplace copyFrom, supprimé en 26.3).
      net.mcreator.minepiece.client.TransformPoses.Snapshot parent =
         net.mcreator.minepiece.client.TransformPoses.forState(state);
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
