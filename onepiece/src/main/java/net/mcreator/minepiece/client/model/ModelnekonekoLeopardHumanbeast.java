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

public class ModelnekonekoLeopardHumanbeast extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("minepiece", "neko_neko_leopard_humanbeast"), "main");
   public final ModelPart Head;
   public final ModelPart Body;
   public final ModelPart tail1;
   public final ModelPart tail2;
   public final ModelPart tail3;
   public final ModelPart tail4;
   public final ModelPart RightArm;
   public final ModelPart right_arm3;
   public final ModelPart LeftArm;
   public final ModelPart left_arm3;
   public final ModelPart RightLeg;
   public final ModelPart right_leg3;
   public final ModelPart right_leg4;
   public final ModelPart LeftLeg;
   public final ModelPart left_leg3;
   public final ModelPart left_leg4;

   public ModelnekonekoLeopardHumanbeast(ModelPart root) {
      super(root);
      this.Head = root.getChild("Head");
      this.Body = root.getChild("Body");
      this.tail1 = this.Body.getChild("tail1");
      this.tail2 = this.tail1.getChild("tail2");
      this.tail3 = this.tail2.getChild("tail3");
      this.tail4 = this.tail3.getChild("tail4");
      this.RightArm = root.getChild("RightArm");
      this.right_arm3 = this.RightArm.getChild("right_arm3");
      this.LeftArm = root.getChild("LeftArm");
      this.left_arm3 = this.LeftArm.getChild("left_arm3");
      this.RightLeg = root.getChild("RightLeg");
      this.right_leg3 = this.RightLeg.getChild("right_leg3");
      this.right_leg4 = this.right_leg3.getChild("right_leg4");
      this.LeftLeg = root.getChild("LeftLeg");
      this.left_leg3 = this.LeftLeg.getChild("left_leg3");
      this.left_leg4 = this.left_leg3.getChild("left_leg4");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition Head = partdefinition.addOrReplaceChild(
         "Head",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
            .texOffs(32, 0)
            .addBox(-4.5F, -9.5F, -2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(32, 0)
            .mirror()
            .addBox(2.5F, -9.5F, -2.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition Body = partdefinition.addOrReplaceChild(
         "Body",
         CubeListBuilder.create().texOffs(16, 16).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.5F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition tail1 = Body.addOrReplaceChild("tail1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 11.1075F, 2.2965F, 0.2618F, 0.0F, 0.0F));
      PartDefinition torso_r1 = tail1.addOrReplaceChild(
         "torso_r1",
         CubeListBuilder.create().texOffs(32, 22).addBox(-1.0F, 3.5542F, -8.5287F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.2F)),
         PartPose.offsetAndRotation(0.0F, 3.8861F, 7.8739F, -1.0472F, 0.0F, 0.0F)
      );
      PartDefinition tail2 = tail1.addOrReplaceChild("tail2", CubeListBuilder.create(), PartPose.offset(0.0F, 3.0F, 2.0F));
      PartDefinition torso_r2 = tail2.addOrReplaceChild(
         "torso_r2",
         CubeListBuilder.create().texOffs(32, 22).addBox(-1.0F, 1.4931F, -5.8813F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.21F)),
         PartPose.offsetAndRotation(0.0F, 0.8861F, 5.8739F, -0.6109F, 0.0F, 0.0F)
      );
      PartDefinition tail3 = tail2.addOrReplaceChild("tail3", CubeListBuilder.create(), PartPose.offset(0.0F, 2.0F, 3.5F));
      PartDefinition torso_r3 = tail3.addOrReplaceChild(
         "torso_r3",
         CubeListBuilder.create().texOffs(32, 22).addBox(-1.0F, 0.6909F, -2.6227F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.2F)),
         PartPose.offsetAndRotation(0.0F, -1.1139F, 2.3739F, -0.1745F, 0.0F, 0.0F)
      );
      PartDefinition tail4 = tail3.addOrReplaceChild("tail4", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 4.0F));
      PartDefinition torso_r4 = tail4.addOrReplaceChild(
         "torso_r4",
         CubeListBuilder.create()
            .texOffs(1, 1)
            .addBox(0.0F, 4.1458F, 9.2682F, 0.0F, 0.0F, 1.0F, new CubeDeformation(0.9F))
            .texOffs(32, 22)
            .addBox(-1.0F, 3.1458F, 3.7682F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.2F)),
         PartPose.offsetAndRotation(0.0F, -2.1139F, -1.6261F, 0.6109F, 0.0F, 0.0F)
      );
      PartDefinition torso_r5 = tail4.addOrReplaceChild(
         "torso_r5",
         CubeListBuilder.create().texOffs(32, 22).addBox(-1.0F, 1.3184F, 0.8188F, 2.0F, 2.0F, 5.0F, new CubeDeformation(-0.21F)),
         PartPose.offsetAndRotation(0.0F, -2.1139F, -1.6261F, 0.2182F, 0.0F, 0.0F)
      );
      PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
      PartDefinition right_arm_r1 = RightArm.addOrReplaceChild(
         "right_arm_r1",
         CubeListBuilder.create().texOffs(40, 16).addBox(-2.0F, -4.5F, -1.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-1.0F, 2.0F, 0.0F, 0.1745F, 0.0F, 0.3927F)
      );
      PartDefinition right_arm3 = RightArm.addOrReplaceChild("right_arm3", CubeListBuilder.create(), PartPose.offset(-1.9526F, 4.124F, -0.4074F));
      PartDefinition right_arm_r2 = right_arm3.addOrReplaceChild(
         "right_arm_r2",
         CubeListBuilder.create()
            .texOffs(41, 16)
            .addBox(-2.0199F, -1.3062F, -1.368F, 3.0F, 3.0F, 4.0F, new CubeDeformation(-0.2F))
            .texOffs(40, 16)
            .addBox(-2.0199F, -6.3062F, -1.368F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-0.3F)),
         PartPose.offsetAndRotation(-0.1237F, 5.3476F, -0.7862F, -0.3927F, 0.0F, 0.0F)
      );
      PartDefinition right_arm_r3 = right_arm3.addOrReplaceChild(
         "right_arm_r3",
         CubeListBuilder.create()
            .texOffs(0, 21)
            .addBox(-2.1746F, -0.0732F, 0.2497F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(0, 21)
            .addBox(-2.1473F, -0.1512F, 1.1633F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-0.1237F, 5.3476F, -0.7862F, -0.3054F, -0.0873F, -0.6981F)
      );
      PartDefinition right_arm_r4 = right_arm3.addOrReplaceChild(
         "right_arm_r4",
         CubeListBuilder.create()
            .texOffs(0, 21)
            .addBox(-2.5909F, -0.798F, 0.5683F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(0, 21)
            .addBox(-2.4451F, -0.8499F, 1.4704F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-0.1237F, 5.3476F, -0.7862F, -0.0436F, -0.4363F, -1.7017F)
      );
      PartDefinition right_arm_r5 = right_arm3.addOrReplaceChild(
         "right_arm_r5",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-2.8462F, -0.4898F, 0.5112F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
            .texOffs(0, 0)
            .addBox(-2.7364F, -0.5913F, 1.4115F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F)),
         PartPose.offsetAndRotation(-0.1237F, 5.3476F, -0.7862F, 0.0873F, -0.3927F, -2.2689F)
      );
      PartDefinition right_arm_r6 = right_arm3.addOrReplaceChild(
         "right_arm_r6",
         CubeListBuilder.create()
            .texOffs(0, 21)
            .addBox(-2.0851F, -0.1581F, -0.373F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(0, 21)
            .addBox(-1.9537F, -0.2323F, -1.3614F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-0.1237F, 5.3476F, -0.7862F, -0.3927F, -0.1309F, -0.6981F)
      );
      PartDefinition right_arm_r7 = right_arm3.addOrReplaceChild(
         "right_arm_r7",
         CubeListBuilder.create()
            .texOffs(0, 21)
            .addBox(-2.5938F, -0.8451F, -0.1151F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(0, 21)
            .addBox(-2.5396F, -0.7474F, -1.1087F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-0.1237F, 5.3476F, -0.7862F, -0.1309F, -0.48F, -1.7017F)
      );
      PartDefinition right_arm_r8 = right_arm3.addOrReplaceChild(
         "right_arm_r8",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-2.8571F, -0.4552F, -0.2506F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
            .texOffs(0, 0)
            .addBox(-2.8156F, -0.2095F, -1.2188F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F)),
         PartPose.offsetAndRotation(-0.1237F, 5.3476F, -0.7862F, 0.0F, -0.4363F, -2.2689F)
      );
      PartDefinition right_arm_r9 = right_arm3.addOrReplaceChild(
         "right_arm_r9",
         CubeListBuilder.create().texOffs(0, 0).addBox(-2.2961F, -0.842F, -0.6023F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F)),
         PartPose.offsetAndRotation(-0.1237F, 5.3476F, -0.7862F, 1.5218F, -1.0515F, -2.9513F)
      );
      PartDefinition right_arm_r10 = right_arm3.addOrReplaceChild(
         "right_arm_r10",
         CubeListBuilder.create().texOffs(0, 21).addBox(-2.3558F, -0.5414F, -0.4768F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-0.1237F, 5.3476F, -0.7862F, 3.0022F, -1.469F, 1.9506F)
      );
      PartDefinition right_arm_r11 = right_arm3.addOrReplaceChild(
         "right_arm_r11",
         CubeListBuilder.create().texOffs(0, 21).addBox(-1.6967F, -0.2221F, -0.6843F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-0.1237F, 5.3476F, -0.7862F, -1.6408F, -0.5707F, 0.3249F)
      );
      PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
      PartDefinition left_arm_r1 = LeftArm.addOrReplaceChild(
         "left_arm_r1",
         CubeListBuilder.create().texOffs(40, 16).mirror().addBox(-2.0F, -4.5F, -1.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(1.0F, 2.0F, 0.0F, 0.1745F, 0.0F, -0.3927F)
      );
      PartDefinition left_arm3 = LeftArm.addOrReplaceChild("left_arm3", CubeListBuilder.create(), PartPose.offset(1.9526F, 4.124F, -0.4074F));
      PartDefinition left_arm_r2 = left_arm3.addOrReplaceChild(
         "left_arm_r2",
         CubeListBuilder.create()
            .texOffs(41, 16)
            .mirror()
            .addBox(-0.9801F, -1.3062F, -1.368F, 3.0F, 3.0F, 4.0F, new CubeDeformation(-0.2F))
            .mirror(false)
            .texOffs(40, 16)
            .mirror()
            .addBox(-1.9801F, -6.3062F, -1.368F, 4.0F, 6.0F, 4.0F, new CubeDeformation(-0.3F))
            .mirror(false),
         PartPose.offsetAndRotation(0.1237F, 5.3476F, -0.7862F, -0.3927F, 0.0F, 0.0F)
      );
      PartDefinition left_arm_r3 = left_arm3.addOrReplaceChild(
         "left_arm_r3",
         CubeListBuilder.create()
            .texOffs(0, 21)
            .mirror()
            .addBox(1.1746F, -0.0732F, 0.2497F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 21)
            .mirror()
            .addBox(1.1473F, -0.1512F, 1.1633F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(0.1237F, 5.3476F, -0.7862F, -0.3054F, 0.0873F, 0.6981F)
      );
      PartDefinition left_arm_r4 = left_arm3.addOrReplaceChild(
         "left_arm_r4",
         CubeListBuilder.create()
            .texOffs(0, 21)
            .mirror()
            .addBox(1.5909F, -0.798F, 0.5683F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 21)
            .mirror()
            .addBox(1.4451F, -0.8499F, 1.4704F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(0.1237F, 5.3476F, -0.7862F, -0.0436F, 0.4363F, 1.7017F)
      );
      PartDefinition left_arm_r5 = left_arm3.addOrReplaceChild(
         "left_arm_r5",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(1.8462F, -0.4898F, 0.5112F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(1.7364F, -0.5913F, 1.4115F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
            .mirror(false),
         PartPose.offsetAndRotation(0.1237F, 5.3476F, -0.7862F, 0.0873F, 0.3927F, 2.2689F)
      );
      PartDefinition left_arm_r6 = left_arm3.addOrReplaceChild(
         "left_arm_r6",
         CubeListBuilder.create()
            .texOffs(0, 21)
            .mirror()
            .addBox(1.0851F, -0.1581F, -0.373F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 21)
            .mirror()
            .addBox(0.9537F, -0.2323F, -1.3614F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(0.1237F, 5.3476F, -0.7862F, -0.3927F, 0.1309F, 0.6981F)
      );
      PartDefinition left_arm_r7 = left_arm3.addOrReplaceChild(
         "left_arm_r7",
         CubeListBuilder.create()
            .texOffs(0, 21)
            .mirror()
            .addBox(1.5938F, -0.8451F, -0.1151F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(0, 21)
            .mirror()
            .addBox(1.5396F, -0.7474F, -1.1087F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(0.1237F, 5.3476F, -0.7862F, -0.1309F, 0.48F, 1.7017F)
      );
      PartDefinition left_arm_r8 = left_arm3.addOrReplaceChild(
         "left_arm_r8",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(1.8571F, -0.4552F, -0.2506F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(1.8156F, -0.2095F, -1.2188F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
            .mirror(false),
         PartPose.offsetAndRotation(0.1237F, 5.3476F, -0.7862F, 0.0F, 0.4363F, 2.2689F)
      );
      PartDefinition left_arm_r9 = left_arm3.addOrReplaceChild(
         "left_arm_r9",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(1.2961F, -0.842F, -0.6023F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F))
            .mirror(false),
         PartPose.offsetAndRotation(0.1237F, 5.3476F, -0.7862F, 1.5218F, 1.0515F, 2.9513F)
      );
      PartDefinition left_arm_r10 = left_arm3.addOrReplaceChild(
         "left_arm_r10",
         CubeListBuilder.create()
            .texOffs(0, 21)
            .mirror()
            .addBox(1.3558F, -0.5414F, -0.4768F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(0.1237F, 5.3476F, -0.7862F, 3.0022F, 1.469F, -1.9506F)
      );
      PartDefinition left_arm_r11 = left_arm3.addOrReplaceChild(
         "left_arm_r11",
         CubeListBuilder.create()
            .texOffs(0, 21)
            .mirror()
            .addBox(0.6967F, -0.2221F, -0.6843F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(0.1237F, 5.3476F, -0.7862F, -1.6408F, 0.5707F, -0.3249F)
      );
      PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create(), PartPose.offset(-2.0F, 12.0F, 0.0F));
      PartDefinition right_leg_r1 = RightLeg.addOrReplaceChild(
         "right_leg_r1",
         CubeListBuilder.create().texOffs(0, 16).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-1.1851F, 2.217F, -1.6383F, -0.7854F, 0.1745F, 0.0F)
      );
      PartDefinition right_leg3 = RightLeg.addOrReplaceChild(
         "right_leg3", CubeListBuilder.create(), PartPose.offsetAndRotation(-1.7824F, 5.4136F, -4.3441F, 0.011F, 0.0886F, -0.0151F)
      );
      PartDefinition right_leg_r2 = right_leg3.addOrReplaceChild(
         "right_leg_r2",
         CubeListBuilder.create().texOffs(0, 16).addBox(-2.996F, -4.5898F, -1.9995F, 4.0F, 9.0F, 4.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(1.0725F, 0.2382F, 2.1841F, 1.3963F, 0.0F, 0.0F)
      );
      PartDefinition right_leg4 = right_leg3.addOrReplaceChild("right_leg4", CubeListBuilder.create(), PartPose.offset(-0.177F, 0.0219F, 4.992F));
      PartDefinition right_leg_r3 = right_leg4.addOrReplaceChild(
         "right_leg_r3",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-3.1555F, -0.9334F, -1.1776F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.6F))
            .texOffs(0, 0)
            .addBox(-1.8555F, -0.9334F, -1.1776F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.6F))
            .texOffs(0, 0)
            .addBox(-0.5555F, -0.9334F, -1.1776F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.6F)),
         PartPose.offsetAndRotation(0.7352F, 5.8703F, -1.6282F, -1.309F, 0.0F, 0.0F)
      );
      PartDefinition right_leg_r4 = right_leg4.addOrReplaceChild(
         "right_leg_r4",
         CubeListBuilder.create()
            .texOffs(4, 18)
            .addBox(-3.1555F, -1.5075F, -1.0187F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.4F))
            .texOffs(4, 18)
            .addBox(-1.8555F, -1.5075F, -1.0187F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.4F))
            .texOffs(4, 18)
            .addBox(-0.5555F, -1.5075F, -1.0187F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.4F))
            .texOffs(2, 18)
            .addBox(-2.8555F, -2.9075F, -1.0187F, 4.0F, 3.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(0.7352F, 5.8703F, -1.6282F, -1.4835F, 0.0F, 0.0F)
      );
      PartDefinition right_leg_r5 = right_leg4.addOrReplaceChild(
         "right_leg_r5",
         CubeListBuilder.create()
            .texOffs(2, 18)
            .mirror()
            .addBox(-2.996F, -3.5044F, -1.0897F, 4.0F, 7.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(0.9126F, 2.396F, 0.6523F, -0.1309F, 0.0F, 0.0F)
      );
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild("LeftLeg", CubeListBuilder.create(), PartPose.offset(2.0F, 12.0F, 0.0F));
      PartDefinition left_leg_r1 = LeftLeg.addOrReplaceChild(
         "left_leg_r1",
         CubeListBuilder.create().texOffs(0, 16).mirror().addBox(-2.0F, -4.0F, -2.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(1.1851F, 2.217F, -1.6383F, -0.7854F, -0.1745F, 0.0F)
      );
      PartDefinition left_leg3 = LeftLeg.addOrReplaceChild(
         "left_leg3", CubeListBuilder.create(), PartPose.offsetAndRotation(1.7824F, 5.4136F, -4.3441F, 0.011F, -0.0886F, 0.0151F)
      );
      PartDefinition left_leg_r2 = left_leg3.addOrReplaceChild(
         "left_leg_r2",
         CubeListBuilder.create()
            .texOffs(0, 16)
            .mirror()
            .addBox(-1.004F, -4.5898F, -1.9995F, 4.0F, 9.0F, 4.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(-1.0725F, 0.2382F, 2.1841F, 1.3963F, 0.0F, 0.0F)
      );
      PartDefinition left_leg4 = left_leg3.addOrReplaceChild("left_leg4", CubeListBuilder.create(), PartPose.offset(0.177F, 0.0219F, 4.992F));
      PartDefinition left_leg_r3 = left_leg4.addOrReplaceChild(
         "left_leg_r3",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(1.1555F, -0.9334F, -1.1776F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.6F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.1445F, -0.9334F, -1.1776F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.6F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4445F, -0.9334F, -1.1776F, 2.0F, 4.0F, 2.0F, new CubeDeformation(-0.6F))
            .mirror(false),
         PartPose.offsetAndRotation(-0.7352F, 5.8703F, -1.6282F, -1.309F, 0.0F, 0.0F)
      );
      PartDefinition left_leg_r4 = left_leg4.addOrReplaceChild(
         "left_leg_r4",
         CubeListBuilder.create()
            .texOffs(4, 18)
            .mirror()
            .addBox(1.1555F, -1.5075F, -1.0187F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.4F))
            .mirror(false)
            .texOffs(4, 18)
            .mirror()
            .addBox(-0.1445F, -1.5075F, -1.0187F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.4F))
            .mirror(false)
            .texOffs(4, 18)
            .mirror()
            .addBox(-1.4445F, -1.5075F, -1.0187F, 2.0F, 3.0F, 2.0F, new CubeDeformation(-0.4F))
            .mirror(false)
            .texOffs(2, 18)
            .mirror()
            .addBox(-1.1445F, -2.9075F, -1.0187F, 4.0F, 3.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(-0.7352F, 5.8703F, -1.6282F, -1.4835F, 0.0F, 0.0F)
      );
      PartDefinition left_leg_r5 = left_leg4.addOrReplaceChild(
         "left_leg_r5",
         CubeListBuilder.create().texOffs(2, 18).addBox(-1.004F, -3.5044F, -1.0897F, 4.0F, 7.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(-0.9126F, 2.396F, 0.6523F, -0.1309F, 0.0F, 0.0F)
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
