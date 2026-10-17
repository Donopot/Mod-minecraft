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

public class ModelnekonekoLeopardHumanbeast2 extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath("minepiece", "neko_neko_leopard_humanbeast2"), "main"
   );
   public final ModelPart Head;
   public final ModelPart Body;
   public final ModelPart RightArm;
   public final ModelPart right_arm3;
   public final ModelPart LeftArm;
   public final ModelPart left_arm3;
   public final ModelPart RightLeg;
   public final ModelPart LeftLeg;

   public ModelnekonekoLeopardHumanbeast2(ModelPart root) {
      super(root);
      this.Head = root.getChild("Head");
      this.Body = root.getChild("Body");
      this.RightArm = root.getChild("RightArm");
      this.right_arm3 = this.RightArm.getChild("right_arm3");
      this.LeftArm = root.getChild("LeftArm");
      this.left_arm3 = this.LeftArm.getChild("left_arm3");
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
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-0.1F))
            .texOffs(32, 0)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.1F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition Body = partdefinition.addOrReplaceChild(
         "Body",
         CubeListBuilder.create()
            .texOffs(16, 16)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.45F))
            .texOffs(16, 32)
            .addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.55F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
      PartDefinition right_arm_r1 = RightArm.addOrReplaceChild(
         "right_arm_r1",
         CubeListBuilder.create()
            .texOffs(40, 32)
            .addBox(-2.0F, -4.5F, -1.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.1F))
            .texOffs(40, 16)
            .addBox(-2.0F, -4.5F, -1.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(-0.1F)),
         PartPose.offsetAndRotation(-1.0F, 2.0F, 0.0F, 0.1745F, 0.0F, 0.3927F)
      );
      PartDefinition right_arm3 = RightArm.addOrReplaceChild("right_arm3", CubeListBuilder.create(), PartPose.offset(-1.9526F, 4.124F, -0.4074F));
      PartDefinition right_arm_r2 = right_arm3.addOrReplaceChild(
         "right_arm_r2",
         CubeListBuilder.create()
            .texOffs(40, 38)
            .addBox(-2.5199F, -6.3062F, -1.368F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-0.2F))
            .texOffs(41, 25)
            .addBox(-2.5199F, -1.3062F, -1.368F, 3.0F, 3.0F, 4.0F, new CubeDeformation(-0.3F))
            .texOffs(40, 22)
            .addBox(-2.5199F, -6.3062F, -1.368F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-0.4F)),
         PartPose.offsetAndRotation(0.3763F, 5.3476F, -0.7862F, -0.3927F, 0.0F, 0.0F)
      );
      PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
      PartDefinition left_arm_r1 = LeftArm.addOrReplaceChild(
         "left_arm_r1",
         CubeListBuilder.create()
            .texOffs(48, 48)
            .mirror()
            .addBox(-2.0F, -4.5F, -1.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.1F))
            .mirror(false)
            .texOffs(32, 48)
            .mirror()
            .addBox(-2.0F, -4.5F, -1.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(-0.1F))
            .mirror(false),
         PartPose.offsetAndRotation(1.0F, 2.0F, 0.0F, 0.1745F, 0.0F, -0.3927F)
      );
      PartDefinition left_arm3 = LeftArm.addOrReplaceChild("left_arm3", CubeListBuilder.create(), PartPose.offset(1.9526F, 4.124F, -0.4074F));
      PartDefinition left_arm_r2 = left_arm3.addOrReplaceChild(
         "left_arm_r2",
         CubeListBuilder.create()
            .texOffs(48, 54)
            .mirror()
            .addBox(-1.4801F, -6.3062F, -1.368F, 4.0F, 5.0F, 4.0F, new CubeDeformation(-0.2F))
            .mirror(false)
            .texOffs(33, 57)
            .mirror()
            .addBox(-0.4801F, -1.3062F, -1.368F, 3.0F, 3.0F, 4.0F, new CubeDeformation(-0.3F))
            .mirror(false)
            .texOffs(32, 54)
            .mirror()
            .addBox(-1.4801F, -6.3062F, -1.368F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-0.4F))
            .mirror(false),
         PartPose.offsetAndRotation(-0.3763F, 5.3476F, -0.7862F, -0.3927F, 0.0F, 0.0F)
      );
      PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create(), PartPose.offset(-2.0F, 12.0F, 0.0F));
      PartDefinition right_leg_r1 = RightLeg.addOrReplaceChild(
         "right_leg_r1",
         CubeListBuilder.create().texOffs(0, 36).addBox(-2.996F, -3.5898F, -1.9995F, 4.0F, 8.0F, 4.0F, new CubeDeformation(-0.15F)),
         PartPose.offsetAndRotation(-0.7099F, 5.6518F, -2.1601F, 1.3963F, 0.0F, 0.0F)
      );
      PartDefinition right_leg_r2 = RightLeg.addOrReplaceChild(
         "right_leg_r2",
         CubeListBuilder.create().texOffs(0, 32).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(-0.6851F, 2.217F, -1.6383F, -0.7854F, 0.1745F, 0.0F)
      );
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild("LeftLeg", CubeListBuilder.create(), PartPose.offset(2.0F, 12.0F, 0.0F));
      PartDefinition left_leg_r1 = LeftLeg.addOrReplaceChild(
         "left_leg_r1",
         CubeListBuilder.create().texOffs(0, 52).addBox(-1.004F, -3.5898F, -1.9995F, 4.0F, 8.0F, 4.0F, new CubeDeformation(-0.15F)),
         PartPose.offsetAndRotation(0.7099F, 5.6518F, -2.1601F, 1.3963F, 0.0F, 0.0F)
      );
      PartDefinition left_leg_r2 = LeftLeg.addOrReplaceChild(
         "left_leg_r2",
         CubeListBuilder.create().texOffs(0, 48).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(0.6851F, 2.217F, -1.6383F, -0.7854F, -0.1745F, 0.0F)
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
