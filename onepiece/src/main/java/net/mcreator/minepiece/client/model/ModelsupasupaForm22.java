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

public class ModelsupasupaForm22 extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("minepiece", "supasupa_form2_2"), "main");
   public final ModelPart Head;
   public final ModelPart Body;
   public final ModelPart RightArm;
   public final ModelPart LeftArm;
   public final ModelPart RightLeg;
   public final ModelPart LeftLeg;

   public ModelsupasupaForm22(ModelPart root) {
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
      PartDefinition RightArm = partdefinition.addOrReplaceChild(
         "RightArm",
         CubeListBuilder.create()
            .texOffs(42, 32)
            .addBox(-1.0F, -2.0F, -2.0F, 2.0F, 12.0F, 4.0F, new CubeDeformation(0.25F))
            .texOffs(42, 16)
            .addBox(-1.0F, -2.0F, -2.0F, 2.0F, 12.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offset(-5.0F, 2.0F, 0.0F)
      );
      PartDefinition LeftArm = partdefinition.addOrReplaceChild(
         "LeftArm",
         CubeListBuilder.create()
            .texOffs(34, 48)
            .addBox(-1.0F, -2.0F, -2.0F, 2.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(50, 48)
            .addBox(-1.0F, -2.0F, -2.0F, 2.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
         PartPose.offset(5.0F, 2.0F, 0.0F)
      );
      PartDefinition RightLeg = partdefinition.addOrReplaceChild(
         "RightLeg",
         CubeListBuilder.create()
            .texOffs(2, 16)
            .addBox(0.0F, 0.0F, -2.0F, 2.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(2, 32)
            .addBox(0.0F, 0.0F, -2.0F, 2.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild(
         "LeftLeg",
         CubeListBuilder.create()
            .texOffs(18, 48)
            .addBox(-2.0F, 0.0F, -2.0F, 2.0F, 12.0F, 4.0F, new CubeDeformation(0.0F))
            .texOffs(2, 48)
            .addBox(-2.0F, 0.0F, -2.0F, 2.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
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
