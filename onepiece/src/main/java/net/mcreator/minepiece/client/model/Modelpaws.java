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

public class Modelpaws extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("minepiece", "paws"), "main");
   public final ModelPart Body;
   public final ModelPart Head;
   public final ModelPart RightArm;
   public final ModelPart LeftArm;
   public final ModelPart RightLeg;
   public final ModelPart LeftLeg;

   public Modelpaws(ModelPart root) {
      super(root);
      this.Body = root.getChild("Body");
      this.Head = root.getChild("Head");
      this.RightArm = root.getChild("RightArm");
      this.LeftArm = root.getChild("LeftArm");
      this.RightLeg = root.getChild("RightLeg");
      this.LeftLeg = root.getChild("LeftLeg");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition Head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition RightArm = partdefinition.addOrReplaceChild(
         "RightArm",
         CubeListBuilder.create()
            .texOffs(0, 3)
            .addBox(-1.5F, 9.4F, -0.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(5, 0)
            .addBox(-1.0F, 9.3F, -1.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-2.7F, 9.4F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
            .texOffs(0, 0)
            .addBox(-2.7F, 9.4F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
            .texOffs(0, 0)
            .addBox(-2.7F, 9.4F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
            .texOffs(0, 0)
            .addBox(-2.7F, 9.4F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)),
         PartPose.offset(-5.0F, 2.0F, 0.0F)
      );
      PartDefinition LeftArm = partdefinition.addOrReplaceChild(
         "LeftArm",
         CubeListBuilder.create()
            .texOffs(5, 0)
            .mirror()
            .addBox(0.0F, 9.3F, -1.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(1.7F, 9.4F, -2.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(1.7F, 9.4F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(1.7F, 9.4F, 0.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(1.7F, 9.4F, 1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F))
            .mirror(false)
            .texOffs(0, 3)
            .mirror()
            .addBox(-0.5F, 9.4F, -0.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(5.0F, 2.0F, 0.0F)
      );
      PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create(), PartPose.offset(-2.0F, 12.0F, 0.0F));
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild("LeftLeg", CubeListBuilder.create(), PartPose.offset(2.0F, 12.0F, 0.0F));
      return LayerDefinition.create(meshdefinition, 16, 16);
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
