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

public class ModelfeatherRobe extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("minepiece", "feather_robe"), "main");
   public final ModelPart Head;
   public final ModelPart Body;
   public final ModelPart feather_robe;
   public final ModelPart RightArm;
   public final ModelPart LeftArm;
   public final ModelPart LeftLeg;
   public final ModelPart RightLeg;

   public ModelfeatherRobe(ModelPart root) {
      super(root);
      this.Head = root.getChild("Head");
      this.Body = root.getChild("Body");
      this.feather_robe = this.Body.getChild("feather_robe");
      this.RightArm = root.getChild("RightArm");
      this.LeftArm = root.getChild("LeftArm");
      this.LeftLeg = root.getChild("LeftLeg");
      this.RightLeg = root.getChild("RightLeg");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition Head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition feather_robe = Body.addOrReplaceChild(
         "feather_robe",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(4.0F, -23.5F, -4.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(5.0F, -24.5F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.2F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(3.5F, -21.5F, -4.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .addBox(-6.0F, -23.5F, -4.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-7.0F, -24.5F, -3.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.2F))
            .texOffs(0, 0)
            .addBox(-5.5F, -21.5F, -4.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, 24.0F, 0.0F)
      );
      PartDefinition deco_r1 = feather_robe.addOrReplaceChild(
         "deco_r1",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(2.0F, 1.0F, -2.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .mirror()
            .addBox(12.0F, 1.0F, -2.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-8.0F, -30.0F, 7.0F, -0.7854F, 0.0F, 0.0F)
      );
      PartDefinition deco_r2 = feather_robe.addOrReplaceChild(
         "deco_r2",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-2.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-1.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.1F))
            .texOffs(0, 0)
            .mirror()
            .addBox(10.0F, -1.0F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(9.0F, -1.5F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.1F))
            .mirror(false),
         PartPose.offsetAndRotation(-5.0F, -25.5F, 3.0F, -0.7854F, 0.0F, 0.0F)
      );
      PartDefinition deco_r3 = feather_robe.addOrReplaceChild(
         "deco_r3",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-1.5F, -2.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-2.0F, -3.5F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(-3.0F, -32.0F, 10.0F, 0.0F, 0.0F, -1.5708F)
      );
      PartDefinition deco_r4 = feather_robe.addOrReplaceChild(
         "deco_r4",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-1.0F, -2.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .mirror()
            .addBox(9.0F, -2.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-5.0F, -15.0F, 3.0F, 1.5708F, 0.0F, 0.0F)
      );
      PartDefinition deco_r5 = feather_robe.addOrReplaceChild(
         "deco_r5",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-1.0F, -3.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.2F))
            .texOffs(0, 0)
            .mirror()
            .addBox(9.0F, -3.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.2F))
            .mirror(false),
         PartPose.offsetAndRotation(-5.0F, -17.0F, -1.0F, 0.7854F, 0.0F, 0.0F)
      );
      PartDefinition deco_r6 = feather_robe.addOrReplaceChild(
         "deco_r6",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-1.0F, -2.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .mirror()
            .addBox(10.0F, -2.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-5.5F, -28.0F, 4.5F, -0.3927F, 0.0F, 0.0F)
      );
      PartDefinition deco_r7 = feather_robe.addOrReplaceChild(
         "deco_r7",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-1.0F, -2.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .mirror()
            .addBox(9.0F, -2.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-5.0F, -24.5F, 0.5F, 1.5708F, 0.0F, 0.0F)
      );
      PartDefinition deco_r8 = feather_robe.addOrReplaceChild(
         "deco_r8",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(0.0F, -1.5F, -0.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .mirror()
            .addBox(14.0F, -1.5F, -0.5F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-8.0F, -30.0F, 7.0F, -1.1781F, 0.0F, 0.0F)
      );
      PartDefinition deco_r9 = feather_robe.addOrReplaceChild(
         "deco_r9",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.2F))
            .texOffs(0, 0)
            .mirror()
            .addBox(11.0F, -1.0F, -1.0F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.2F))
            .mirror(false),
         PartPose.offsetAndRotation(-6.0F, -30.5F, 7.5F, -1.1781F, 0.0F, 0.0F)
      );
      PartDefinition deco_r10 = feather_robe.addOrReplaceChild(
         "deco_r10",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.0F, 1.0F, -0.5F, 2.0F, 4.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.5F, -2.0F, -1.0F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(0.0F, -3.5F, -2.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.1F))
            .mirror(false),
         PartPose.offsetAndRotation(3.0F, -32.0F, 10.0F, 0.0F, 0.0F, 1.5708F)
      );
      PartDefinition deco_r11 = feather_robe.addOrReplaceChild(
         "deco_r11",
         CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -3.5F, 1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F)),
         PartPose.offsetAndRotation(0.0F, -34.0F, 10.0F, 0.0F, 0.0F, 1.5708F)
      );
      PartDefinition deco_r12 = feather_robe.addOrReplaceChild(
         "deco_r12",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.5F, -2.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.5F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.5F, 2.0F, -1.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.5F))
            .mirror(false),
         PartPose.offsetAndRotation(0.0F, -32.0F, 11.5F, 0.0F, 0.0F, 1.5708F)
      );
      PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
      PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild("LeftLeg", CubeListBuilder.create(), PartPose.offset(2.0F, 12.0F, 0.0F));
      PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create(), PartPose.offset(-2.0F, 12.0F, 0.0F));
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
