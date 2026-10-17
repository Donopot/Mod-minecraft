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

public class ModelenelDrum extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("minepiece", "enel_drum"), "main");
   public final ModelPart Body;
   public final ModelPart drum;
   public final ModelPart stick;
   public final ModelPart drum1;
   public final ModelPart drum2;
   public final ModelPart drum3;
   public final ModelPart drum4;
   public final ModelPart Head;
   public final ModelPart RightArm;
   public final ModelPart LeftArm;
   public final ModelPart RightLeg;
   public final ModelPart LeftLeg;

   public ModelenelDrum(ModelPart root) {
      super(root);
      this.Body = root.getChild("Body");
      this.drum = this.Body.getChild("drum");
      this.stick = this.drum.getChild("stick");
      this.drum1 = this.drum.getChild("drum1");
      this.drum2 = this.drum.getChild("drum2");
      this.drum3 = this.drum.getChild("drum3");
      this.drum4 = this.drum.getChild("drum4");
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
      PartDefinition drum = Body.addOrReplaceChild("drum", CubeListBuilder.create(), PartPose.offset(9.45F, 9.0F, -5.0F));
      PartDefinition stick = drum.addOrReplaceChild(
         "stick",
         CubeListBuilder.create().texOffs(48, 53).addBox(-9.7183F, -10.5962F, -0.5F, 7.418F, 0.9925F, 1.0F, new CubeDeformation(-0.01F)),
         PartPose.offset(-3.4407F, -13.9417F, 8.1669F)
      );
      PartDefinition stick_r1 = stick.addOrReplaceChild(
         "stick_r1",
         CubeListBuilder.create()
            .texOffs(49, 53)
            .mirror()
            .addBox(-2.5F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(-0.01F))
            .mirror(false),
         PartPose.offsetAndRotation(-8.0639F, 8.0696F, -1.6158F, 0.3655F, 0.7119F, 0.5299F)
      );
      PartDefinition stick_r2 = stick.addOrReplaceChild(
         "stick_r2",
         CubeListBuilder.create().texOffs(49, 53).mirror().addBox(1.5F, 0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(-0.01F)).mirror(false),
         PartPose.offsetAndRotation(-14.9024F, 3.7092F, 0.0F, 0.0F, 0.0F, 0.3927F)
      );
      PartDefinition stick_r3 = stick.addOrReplaceChild(
         "stick_r3",
         CubeListBuilder.create()
            .texOffs(49, 53)
            .mirror()
            .addBox(-2.5F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(-0.01F))
            .mirror(false),
         PartPose.offsetAndRotation(-14.5778F, 3.1757F, 0.0F, 0.0F, 0.0F, 1.1781F)
      );
      PartDefinition stick_r4 = stick.addOrReplaceChild(
         "stick_r4",
         CubeListBuilder.create().texOffs(49, 53).addBox(-2.5F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(-0.01F)),
         PartPose.offsetAndRotation(-3.9547F, 8.0696F, -1.6158F, 0.3655F, -0.7119F, -0.5299F)
      );
      PartDefinition stick_r5 = stick.addOrReplaceChild(
         "stick_r5",
         CubeListBuilder.create().texOffs(49, 53).addBox(-6.5F, 0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(-0.01F)),
         PartPose.offsetAndRotation(2.8839F, 3.7092F, 0.0F, 0.0F, 0.0F, -0.3927F)
      );
      PartDefinition stick_r6 = stick.addOrReplaceChild(
         "stick_r6",
         CubeListBuilder.create().texOffs(49, 53).addBox(-2.5F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(-0.01F)),
         PartPose.offsetAndRotation(2.5592F, 3.1757F, 0.0F, 0.0F, 0.0F, -1.1781F)
      );
      PartDefinition stick_r7 = stick.addOrReplaceChild(
         "stick_r7",
         CubeListBuilder.create().texOffs(49, 53).addBox(-2.5F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(-0.01F)),
         PartPose.offsetAndRotation(2.5592F, -5.2923F, 0.0F, 0.0F, 0.0F, 1.1781F)
      );
      PartDefinition stick_r8 = stick.addOrReplaceChild(
         "stick_r8",
         CubeListBuilder.create()
            .texOffs(49, 53)
            .mirror()
            .addBox(-2.5F, -0.5F, -0.5F, 5.0F, 1.0F, 1.0F, new CubeDeformation(-0.01F))
            .mirror(false),
         PartPose.offsetAndRotation(-14.5778F, -5.2923F, 0.0F, 0.0F, 0.0F, -1.1781F)
      );
      PartDefinition drum1 = drum.addOrReplaceChild(
         "drum1",
         CubeListBuilder.create().texOffs(50, 57).addBox(-5.7114F, 0.8198F, -1.0269F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.04F)),
         PartPose.offset(-15.4593F, -17.9417F, 8.1669F)
      );
      PartDefinition drum2 = drum.addOrReplaceChild(
         "drum2",
         CubeListBuilder.create().texOffs(50, 57).addBox(-5.7114F, -0.1802F, -1.0269F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.04F)),
         PartPose.offset(-11.6593F, -26.0417F, 8.1669F)
      );
      PartDefinition drum3 = drum.addOrReplaceChild(
         "drum3",
         CubeListBuilder.create().texOffs(50, 57).addBox(0.7231F, -0.1802F, -1.0269F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.04F)),
         PartPose.offset(-7.2407F, -26.0417F, 8.1669F)
      );
      PartDefinition drum4 = drum.addOrReplaceChild(
         "drum4",
         CubeListBuilder.create().texOffs(50, 57).addBox(0.7231F, 0.8198F, -1.0269F, 5.0F, 5.0F, 2.0F, new CubeDeformation(0.04F)),
         PartPose.offset(-3.4407F, -17.9417F, 8.1669F)
      );
      PartDefinition Head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
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
