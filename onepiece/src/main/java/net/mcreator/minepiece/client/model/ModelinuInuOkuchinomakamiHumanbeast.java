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

public class ModelinuInuOkuchinomakamiHumanbeast extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath("minepiece", "inu_inu_okuchi_no_makami_humanbeast"), "main"
   );
   public final ModelPart Head;
   public final ModelPart hair;
   public final ModelPart Body;
   public final ModelPart bone;
   public final ModelPart RightArm;
   public final ModelPart LeftArm;
   public final ModelPart LeftLeg;
   public final ModelPart RightLeg;

   public ModelinuInuOkuchinomakamiHumanbeast(ModelPart root) {
      super(root);
      this.Head = root.getChild("Head");
      this.hair = this.Head.getChild("hair");
      this.Body = root.getChild("Body");
      this.bone = this.Body.getChild("bone");
      this.RightArm = root.getChild("RightArm");
      this.LeftArm = root.getChild("LeftArm");
      this.LeftLeg = root.getChild("LeftLeg");
      this.RightLeg = root.getChild("RightLeg");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition Head = partdefinition.addOrReplaceChild(
         "Head",
         CubeListBuilder.create()
            .texOffs(0, 48)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.05F))
            .texOffs(0, 32)
            .addBox(-6.0F, -9.0F, -6.3F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-2.0F))
            .texOffs(0, 32)
            .mirror()
            .addBox(-2.0F, -9.0F, -6.3F, 8.0F, 8.0F, 8.0F, new CubeDeformation(-2.0F))
            .mirror(false),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition head_r1 = Head.addOrReplaceChild(
         "head_r1",
         CubeListBuilder.create().texOffs(45, 33).mirror().addBox(-2.1F, -2.0F, 0.0F, 9.0F, 4.0F, 0.0F, new CubeDeformation(0.1F)).mirror(false),
         PartPose.offsetAndRotation(4.2298F, -3.3125F, -2.1F, 0.0215F, -0.1732F, -0.124F)
      );
      PartDefinition head_r2 = Head.addOrReplaceChild(
         "head_r2",
         CubeListBuilder.create().texOffs(45, 33).addBox(-6.9F, -2.0F, 0.0F, 9.0F, 4.0F, 0.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(-4.2298F, -3.3125F, -2.1F, 0.0215F, 0.1732F, 0.124F)
      );
      PartDefinition head_r3 = Head.addOrReplaceChild(
         "head_r3",
         CubeListBuilder.create().texOffs(0, 48).addBox(-4.0F, 1.3F, -2.2F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(3.5F, -4.0F, -1.0F, -0.48F, 0.0F, 0.0F)
      );
      PartDefinition hair = Head.addOrReplaceChild(
         "hair",
         CubeListBuilder.create().texOffs(0, 0).addBox(-3.0258F, -4.6526F, -6.4025F, 6.24F, 6.24F, 8.02F, new CubeDeformation(0.0F)),
         PartPose.offset(0.0F, -3.0F, 6.0F)
      );
      PartDefinition torso_r1 = hair.addOrReplaceChild(
         "torso_r1",
         CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -5.0F, -7.0F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-1.0675F, 2.6303F, 1.265F, -0.4279F, -0.0018F, 1.9987F)
      );
      PartDefinition torso_r2 = hair.addOrReplaceChild(
         "torso_r2",
         CubeListBuilder.create().texOffs(0, 6).mirror().addBox(0.0F, -5.0F, -7.0F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(1.0675F, 2.6303F, 1.265F, -0.4279F, 0.0018F, -1.9987F)
      );
      PartDefinition torso_r3 = hair.addOrReplaceChild(
         "torso_r3",
         CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -5.0F, -7.0F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-1.0675F, -6.3697F, 1.265F, -0.3107F, -0.2991F, 1.2601F)
      );
      PartDefinition torso_r4 = hair.addOrReplaceChild(
         "torso_r4",
         CubeListBuilder.create().texOffs(0, 6).mirror().addBox(0.0F, -5.0F, -7.0F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(1.0675F, -6.3697F, 1.265F, -0.3107F, 0.2991F, -1.2601F)
      );
      PartDefinition torso_r5 = hair.addOrReplaceChild(
         "torso_r5",
         CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -6.0F, -7.0F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-1.8559F, -1.5551F, 2.7341F, 0.0391F, -0.3407F, -0.4479F)
      );
      PartDefinition torso_r6 = hair.addOrReplaceChild(
         "torso_r6",
         CubeListBuilder.create().texOffs(0, 6).mirror().addBox(0.0F, -6.0F, -7.0F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(1.8559F, -1.5551F, 2.7341F, 0.0391F, 0.3407F, 0.4479F)
      );
      PartDefinition torso_r7 = hair.addOrReplaceChild(
         "torso_r7",
         CubeListBuilder.create()
            .texOffs(0, 6)
            .mirror()
            .addBox(0.0F, -5.76F, -7.13F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(4.774F, -1.7806F, 0.1093F, -0.0335F, 0.0806F, -0.394F)
      );
      PartDefinition torso_r8 = hair.addOrReplaceChild(
         "torso_r8",
         CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -5.76F, -7.13F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-4.774F, -1.7806F, 0.1093F, -0.0335F, -0.0806F, 0.394F)
      );
      PartDefinition torso_r9 = hair.addOrReplaceChild(
         "torso_r9",
         CubeListBuilder.create()
            .texOffs(48, 0)
            .addBox(-2.768F, -2.768F, 2.261F, 5.536F, 5.0F, 2.0F, new CubeDeformation(1.6F))
            .texOffs(30, 0)
            .mirror()
            .addBox(-0.2885F, -2.9F, -1.593F, 3.0F, 5.8F, 4.0F, new CubeDeformation(1.05F))
            .mirror(false)
            .texOffs(48, 0)
            .addBox(-2.768F, -2.768F, 2.261F, 5.536F, 5.0F, 2.0F, new CubeDeformation(1.6F))
            .texOffs(30, 0)
            .mirror()
            .addBox(-0.2885F, -2.9F, -1.593F, 3.0F, 5.8F, 4.0F, new CubeDeformation(1.05F))
            .mirror(false),
         PartPose.offsetAndRotation(0.0942F, -1.5326F, 0.1105F, 0.1309F, 0.0F, 0.0F)
      );
      PartDefinition torso_r10 = hair.addOrReplaceChild(
         "torso_r10",
         CubeListBuilder.create()
            .texOffs(30, 0)
            .addBox(-2.9F, -2.9F, -2.593F, 3.0F, 5.8F, 5.0F, new CubeDeformation(1.0F))
            .texOffs(31, 0)
            .addBox(-2.9F, -2.9F, -2.593F, 3.0F, 5.8F, 5.0F, new CubeDeformation(1.0F)),
         PartPose.offsetAndRotation(0.0942F, -1.5326F, 1.1105F, 0.1309F, 0.0F, 0.0F)
      );
      PartDefinition Body = partdefinition.addOrReplaceChild(
         "Body",
         CubeListBuilder.create().texOffs(40, 48).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.05F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition bone = Body.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition torso_r11 = bone.addOrReplaceChild(
         "torso_r11",
         CubeListBuilder.create()
            .texOffs(0, 6)
            .mirror()
            .addBox(0.0F, -11.86F, -1.97F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(4.5858F, 13.5037F, 4.4459F, -0.4011F, 0.2047F, -1.6387F)
      );
      PartDefinition torso_r12 = bone.addOrReplaceChild(
         "torso_r12",
         CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -11.86F, -1.97F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-4.5858F, 13.5037F, 4.4459F, -0.4011F, -0.2047F, 1.6387F)
      );
      PartDefinition torso_r13 = bone.addOrReplaceChild(
         "torso_r13",
         CubeListBuilder.create()
            .texOffs(0, 6)
            .mirror()
            .addBox(0.0F, -11.86F, -1.97F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(4.5858F, 2.5037F, 4.4459F, -0.4011F, 0.2047F, -1.6387F)
      );
      PartDefinition torso_r14 = bone.addOrReplaceChild(
         "torso_r14",
         CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -11.86F, -1.97F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-4.5858F, 2.5037F, 4.4459F, -0.4011F, -0.2047F, 1.6387F)
      );
      PartDefinition torso_r15 = bone.addOrReplaceChild(
         "torso_r15",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-2.5F, -2.986F, -0.6764F, 5.0F, 6.24F, 7.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(31, 0)
            .mirror()
            .addBox(-2.0F, -2.766F, 4.7639F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F))
            .mirror(false)
            .texOffs(50, 0)
            .mirror()
            .addBox(-2.768F, -2.634F, 8.6764F, 5.536F, 5.0F, 1.0F, new CubeDeformation(1.6F))
            .mirror(false),
         PartPose.offsetAndRotation(0.0F, 8.7083F, 1.3342F, 0.2182F, -0.3927F, -0.1309F)
      );
      PartDefinition torso_r16 = bone.addOrReplaceChild(
         "torso_r16",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-2.5F, -2.986F, -0.6764F, 5.0F, 6.24F, 7.0F, new CubeDeformation(0.0F))
            .texOffs(31, 0)
            .addBox(-2.0F, -2.766F, 4.7639F, 4.0F, 5.0F, 4.0F, new CubeDeformation(1.0F))
            .texOffs(50, 0)
            .addBox(-2.768F, -2.634F, 8.6764F, 5.536F, 5.0F, 1.0F, new CubeDeformation(1.6F)),
         PartPose.offsetAndRotation(0.0F, 8.7083F, 1.3342F, 0.2182F, 0.3927F, 0.1309F)
      );
      PartDefinition torso_r17 = bone.addOrReplaceChild(
         "torso_r17",
         CubeListBuilder.create()
            .texOffs(0, 6)
            .mirror()
            .addBox(-1.5F, -7.76F, -0.97F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 6)
            .mirror()
            .addBox(-1.5F, -7.76F, -0.97F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(5.8974F, 9.2972F, 0.5678F, 0.0072F, 0.4961F, 0.0217F)
      );
      PartDefinition torso_r18 = bone.addOrReplaceChild(
         "torso_r18",
         CubeListBuilder.create()
            .texOffs(0, 6)
            .mirror()
            .addBox(0.0F, -5.76F, -7.13F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(4.9824F, 7.2178F, 10.3491F, -0.2956F, 0.4045F, -0.652F)
      );
      PartDefinition torso_r19 = bone.addOrReplaceChild(
         "torso_r19",
         CubeListBuilder.create()
            .texOffs(0, 6)
            .mirror()
            .addBox(0.0F, -5.76F, -7.13F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(2.4358F, 6.1478F, 14.0056F, 0.3064F, 0.3966F, 0.6928F)
      );
      PartDefinition torso_r20 = bone.addOrReplaceChild(
         "torso_r20",
         CubeListBuilder.create().texOffs(0, 6).addBox(6.5F, -9.76F, 9.03F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-5.8974F, 9.2972F, 0.5678F, -0.0045F, 0.0F, -0.0191F)
      );
      PartDefinition torso_r21 = bone.addOrReplaceChild(
         "torso_r21",
         CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -5.76F, -7.13F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-2.4358F, 6.1478F, 14.0056F, 0.3064F, -0.3966F, -0.6928F)
      );
      PartDefinition torso_r22 = bone.addOrReplaceChild(
         "torso_r22",
         CubeListBuilder.create().texOffs(0, 6).addBox(0.0F, -5.76F, -7.13F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-4.9824F, 7.2178F, 10.3491F, -0.2956F, -0.4045F, 0.652F)
      );
      PartDefinition torso_r23 = bone.addOrReplaceChild(
         "torso_r23",
         CubeListBuilder.create().texOffs(0, 6).addBox(1.5F, -7.76F, -0.97F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-5.8974F, 9.2972F, 0.5678F, 0.0072F, -0.4961F, -0.0217F)
      );
      PartDefinition torso_r24 = bone.addOrReplaceChild(
         "torso_r24",
         CubeListBuilder.create().texOffs(0, 6).addBox(-0.69F, -4.4244F, -12.9905F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-9.26F, 11.3311F, 12.6526F, -1.113F, -0.22F, 1.7259F)
      );
      PartDefinition torso_r25 = bone.addOrReplaceChild(
         "torso_r25",
         CubeListBuilder.create().texOffs(0, 6).addBox(-0.69F, -4.4244F, -5.9906F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-2.9684F, 2.7834F, 9.6335F, -1.113F, -0.22F, 1.3768F)
      );
      PartDefinition torso_r26 = bone.addOrReplaceChild(
         "torso_r26",
         CubeListBuilder.create()
            .texOffs(0, 6)
            .mirror()
            .addBox(0.69F, -4.4244F, -12.9905F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(9.26F, 11.3311F, 12.6526F, -1.113F, 0.22F, -1.7259F)
      );
      PartDefinition torso_r27 = bone.addOrReplaceChild(
         "torso_r27",
         CubeListBuilder.create()
            .texOffs(0, 6)
            .mirror()
            .addBox(0.69F, -4.4244F, -5.9906F, 0.0F, 12.0F, 14.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(2.9684F, 2.7834F, 9.6335F, -1.113F, 0.22F, -1.3768F)
      );
      PartDefinition RightArm = partdefinition.addOrReplaceChild(
         "RightArm",
         CubeListBuilder.create()
            .texOffs(49, 30)
            .mirror()
            .addBox(-1.5F, 2.5191F, 1.3516F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(48, 48)
            .addBox(-3.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.05F)),
         PartPose.offset(-5.0F, 2.0F, 0.0F)
      );
      PartDefinition LeftArm = partdefinition.addOrReplaceChild(
         "LeftArm",
         CubeListBuilder.create()
            .texOffs(49, 30)
            .addBox(1.5F, 2.5191F, 1.3516F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
            .texOffs(48, 48)
            .addBox(-1.0F, -2.0F, -2.0F, 4.0F, 12.0F, 4.0F, new CubeDeformation(0.05F)),
         PartPose.offset(5.0F, 2.0F, 0.0F)
      );
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild(
         "LeftLeg",
         CubeListBuilder.create()
            .texOffs(36, 53)
            .addBox(-1.5F, 10.3102F, -1.5776F, 4.0F, 2.0F, 4.0F, new CubeDeformation(-0.3F))
            .texOffs(49, 30)
            .addBox(0.5F, 2.5191F, 1.3516F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      PartDefinition left_leg_r1 = LeftLeg.addOrReplaceChild(
         "left_leg_r1",
         CubeListBuilder.create().texOffs(36, 53).addBox(0.5F, -1.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F)
      );
      PartDefinition left_leg_r2 = LeftLeg.addOrReplaceChild(
         "left_leg_r2",
         CubeListBuilder.create().texOffs(37, 54).addBox(1.0F, -0.4809F, -6.1484F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, 1.1345F, 0.0F, 0.0F)
      );
      PartDefinition left_leg_r3 = LeftLeg.addOrReplaceChild(
         "left_leg_r3",
         CubeListBuilder.create().texOffs(38, 55).addBox(1.0F, -4.0F, -0.5F, 3.0F, 7.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(-2.0F, 8.5039F, 1.3272F, -0.2618F, 0.0F, 0.0F)
      );
      PartDefinition right_leg_r1 = LeftLeg.addOrReplaceChild(
         "right_leg_r1",
         CubeListBuilder.create()
            .texOffs(37, 54)
            .mirror()
            .addBox(-1.2F, -1.0F, -2.4F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.3F))
            .mirror(false),
         PartPose.offsetAndRotation(2.1F, 11.4102F, -0.0776F, 0.0F, -0.2705F, 0.0F)
      );
      PartDefinition right_leg_r2 = LeftLeg.addOrReplaceChild(
         "right_leg_r2",
         CubeListBuilder.create()
            .texOffs(37, 54)
            .mirror()
            .addBox(-1.0F, -1.0F, -2.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(0.9F, 11.3102F, -0.6776F, 0.0F, -0.1309F, 0.0F)
      );
      PartDefinition left_leg_r4 = LeftLeg.addOrReplaceChild(
         "left_leg_r4",
         CubeListBuilder.create().texOffs(37, 54).addBox(-1.0F, -1.0F, -2.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.2F)),
         PartPose.offsetAndRotation(0.3F, 11.3102F, -0.6776F, 0.0F, 0.1309F, 0.0F)
      );
      PartDefinition left_leg_r5 = LeftLeg.addOrReplaceChild(
         "left_leg_r5",
         CubeListBuilder.create().texOffs(37, 54).addBox(-1.0F, -1.0F, -2.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.3F)),
         PartPose.offsetAndRotation(-0.9F, 11.4102F, -0.0776F, 0.0F, 0.1309F, 0.0F)
      );
      PartDefinition RightLeg = partdefinition.addOrReplaceChild(
         "RightLeg",
         CubeListBuilder.create()
            .texOffs(36, 53)
            .mirror()
            .addBox(-2.5F, 10.3102F, -1.5776F, 4.0F, 2.0F, 4.0F, new CubeDeformation(-0.3F))
            .mirror(false)
            .texOffs(49, 30)
            .mirror()
            .addBox(-0.5F, 2.5191F, 1.3516F, 0.0F, 4.0F, 7.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition right_leg_r3 = RightLeg.addOrReplaceChild(
         "right_leg_r3",
         CubeListBuilder.create().texOffs(36, 53).mirror().addBox(-4.5F, -1.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F)
      );
      PartDefinition right_leg_r4 = RightLeg.addOrReplaceChild(
         "right_leg_r4",
         CubeListBuilder.create()
            .texOffs(37, 54)
            .mirror()
            .addBox(-4.0F, -0.4809F, -6.1484F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, 1.1345F, 0.0F, 0.0F)
      );
      PartDefinition right_leg_r5 = RightLeg.addOrReplaceChild(
         "right_leg_r5",
         CubeListBuilder.create()
            .texOffs(38, 55)
            .mirror()
            .addBox(-4.0F, -4.0F, -0.5F, 3.0F, 7.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(2.0F, 8.5039F, 1.3272F, -0.2618F, 0.0F, 0.0F)
      );
      PartDefinition left_leg_r6 = RightLeg.addOrReplaceChild(
         "left_leg_r6",
         CubeListBuilder.create().texOffs(37, 54).addBox(-0.8F, -1.0F, -2.4F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.3F)),
         PartPose.offsetAndRotation(-2.1F, 11.4102F, -0.0776F, 0.0F, 0.2705F, 0.0F)
      );
      PartDefinition left_leg_r7 = RightLeg.addOrReplaceChild(
         "left_leg_r7",
         CubeListBuilder.create().texOffs(37, 54).addBox(-1.0F, -1.0F, -2.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(-0.9F, 11.3102F, -0.6776F, 0.0F, 0.1309F, 0.0F)
      );
      PartDefinition right_leg_r6 = RightLeg.addOrReplaceChild(
         "right_leg_r6",
         CubeListBuilder.create()
            .texOffs(37, 54)
            .mirror()
            .addBox(-1.0F, -1.0F, -2.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.2F))
            .mirror(false),
         PartPose.offsetAndRotation(-0.3F, 11.3102F, -0.6776F, 0.0F, -0.1309F, 0.0F)
      );
      PartDefinition right_leg_r7 = RightLeg.addOrReplaceChild(
         "right_leg_r7",
         CubeListBuilder.create()
            .texOffs(37, 54)
            .mirror()
            .addBox(-1.0F, -1.0F, -2.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(-0.3F))
            .mirror(false),
         PartPose.offsetAndRotation(0.9F, 11.4102F, -0.0776F, 0.0F, -0.1309F, 0.0F)
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
