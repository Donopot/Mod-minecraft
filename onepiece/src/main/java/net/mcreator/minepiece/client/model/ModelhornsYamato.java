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

public class ModelhornsYamato extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("minepiece", "horns_yamato"), "main");
   public final ModelPart Body;
   public final ModelPart Head;
   public final ModelPart RightArm;
   public final ModelPart LeftArm;
   public final ModelPart RightLeg;
   public final ModelPart LeftLeg;

   public ModelhornsYamato(ModelPart root) {
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
      PartDefinition right_horn_r1 = Head.addOrReplaceChild(
         "right_horn_r1",
         CubeListBuilder.create()
            .texOffs(44, 56)
            .mirror()
            .addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.7F))
            .mirror(false),
         PartPose.offsetAndRotation(-3.4108F, -12.5641F, -2.7354F, -0.0093F, 0.0336F, 1.8412F)
      );
      PartDefinition right_horn_r2 = Head.addOrReplaceChild(
         "right_horn_r2",
         CubeListBuilder.create()
            .texOffs(44, 56)
            .mirror()
            .addBox(-0.8248F, -1.5418F, -1.0015F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.5F))
            .mirror(false),
         PartPose.offsetAndRotation(-4.1163F, -12.0454F, -2.7609F, -0.0093F, 0.0336F, 1.8412F)
      );
      PartDefinition right_horn_r3 = Head.addOrReplaceChild(
         "right_horn_r3",
         CubeListBuilder.create().texOffs(44, 56).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(-4.6746F, -9.8866F, -2.5896F, -0.0169F, -0.1913F, 2.2445F)
      );
      PartDefinition right_horn_r4 = Head.addOrReplaceChild(
         "right_horn_r4",
         CubeListBuilder.create().texOffs(46, 56).addBox(0.2632F, 0.2554F, -1.3673F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.15F)),
         PartPose.offsetAndRotation(-3.9099F, -9.7026F, -2.2622F, 0.085F, -0.1724F, 1.6944F)
      );
      PartDefinition right_horn_r5 = Head.addOrReplaceChild(
         "right_horn_r5",
         CubeListBuilder.create().texOffs(44, 56).addBox(0.2669F, 1.3639F, -1.4171F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.05F)),
         PartPose.offsetAndRotation(-3.8926F, -9.801F, -2.2622F, 0.1758F, -0.0777F, 0.9968F)
      );
      PartDefinition right_horn_r6 = Head.addOrReplaceChild(
         "right_horn_r6",
         CubeListBuilder.create().texOffs(46, 60).addBox(1.395F, 2.1702F, -1.4249F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-3.8926F, -9.801F, -2.2622F, 0.191F, -0.0199F, 0.6875F)
      );
      PartDefinition left_horn_r1 = Head.addOrReplaceChild(
         "left_horn_r1",
         CubeListBuilder.create().texOffs(44, 56).addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.7F)),
         PartPose.offsetAndRotation(3.4108F, -12.5641F, -2.7354F, -0.0093F, -0.0336F, -1.8412F)
      );
      PartDefinition left_horn_r2 = Head.addOrReplaceChild(
         "left_horn_r2",
         CubeListBuilder.create().texOffs(44, 56).addBox(-2.1752F, -1.5418F, -1.0015F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.5F)),
         PartPose.offsetAndRotation(4.1163F, -12.0454F, -2.7609F, -0.0093F, -0.0336F, -1.8412F)
      );
      PartDefinition left_horn_r3 = Head.addOrReplaceChild(
         "left_horn_r3",
         CubeListBuilder.create()
            .texOffs(44, 56)
            .mirror()
            .addBox(-1.5F, -1.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(4.6746F, -9.8866F, -2.5896F, -0.0169F, 0.1913F, -2.2445F)
      );
      PartDefinition left_horn_r4 = Head.addOrReplaceChild(
         "left_horn_r4",
         CubeListBuilder.create()
            .texOffs(44, 56)
            .mirror()
            .addBox(-3.2669F, 1.3639F, -1.4171F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.05F))
            .mirror(false),
         PartPose.offsetAndRotation(3.8926F, -9.801F, -2.2622F, 0.1758F, 0.0777F, -0.9968F)
      );
      PartDefinition left_horn_r5 = Head.addOrReplaceChild(
         "left_horn_r5",
         CubeListBuilder.create()
            .texOffs(46, 56)
            .mirror()
            .addBox(-2.2632F, 0.2554F, -1.3673F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.15F))
            .mirror(false),
         PartPose.offsetAndRotation(3.9099F, -9.7026F, -2.2622F, 0.085F, 0.1724F, -1.6944F)
      );
      PartDefinition left_horn_r6 = Head.addOrReplaceChild(
         "left_horn_r6",
         CubeListBuilder.create()
            .texOffs(46, 60)
            .mirror()
            .addBox(-3.395F, 2.1702F, -1.4249F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(3.8926F, -9.801F, -2.2622F, 0.191F, 0.0199F, -0.6875F)
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
