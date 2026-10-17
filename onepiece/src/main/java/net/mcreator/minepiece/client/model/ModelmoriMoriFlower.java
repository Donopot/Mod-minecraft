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

public class ModelmoriMoriFlower extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("minepiece", "mori_mori_flower"), "main");
   public final ModelPart Head;
   public final ModelPart Body;
   public final ModelPart bone;
   public final ModelPart flower;
   public final ModelPart RightArm;
   public final ModelPart LeftArm;
   public final ModelPart RightLeg;
   public final ModelPart LeftLeg;

   public ModelmoriMoriFlower(ModelPart root) {
      super(root);
      this.Head = root.getChild("Head");
      this.Body = root.getChild("Body");
      this.bone = this.Body.getChild("bone");
      this.flower = this.bone.getChild("flower");
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
      PartDefinition jacket_r1 = Body.addOrReplaceChild(
         "jacket_r1",
         CubeListBuilder.create().texOffs(0, 55).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, -5.189F, 9.249F, -0.5236F, 0.0F, 0.0F)
      );
      PartDefinition jacket_r2 = Body.addOrReplaceChild(
         "jacket_r2",
         CubeListBuilder.create().texOffs(0, 55).addBox(-0.5F, -4.0F, -0.5F, 1.0F, 8.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 1.0F, 4.5F, -0.7854F, 0.0F, 0.0F)
      );
      PartDefinition bone = Body.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -8.5953F, 11.2156F, -0.5236F, 0.0F, 0.0F));
      PartDefinition flower = bone.addOrReplaceChild(
         "flower",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-8.0F, -0.1778F, -8.0417F, 16.0F, 1.0F, 16.0F, new CubeDeformation(0.0F))
            .texOffs(6, 19)
            .addBox(-6.0F, -0.6778F, -6.0417F, 12.0F, 1.0F, 12.0F, new CubeDeformation(0.0F))
            .texOffs(12, 38)
            .addBox(-4.0F, -1.1778F, -4.0417F, 8.0F, 2.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
      PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
      PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create(), PartPose.offset(-2.0F, 12.0F, 0.0F));
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild("LeftLeg", CubeListBuilder.create(), PartPose.offset(2.0F, 12.0F, 0.0F));
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
