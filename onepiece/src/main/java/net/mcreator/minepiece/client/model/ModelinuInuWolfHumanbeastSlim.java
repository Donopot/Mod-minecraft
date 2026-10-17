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

public class ModelinuInuWolfHumanbeastSlim extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath("minepiece", "inu_inu_wolf_humanbeast_slim"), "main"
   );
   public final ModelPart Head;
   public final ModelPart Body;
   public final ModelPart RightArm;
   public final ModelPart LeftArm;
   public final ModelPart RightLeg;
   public final ModelPart LeftLeg;

   public ModelinuInuWolfHumanbeastSlim(ModelPart root) {
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
      PartDefinition Head = partdefinition.addOrReplaceChild(
         "Head",
         CubeListBuilder.create()
            .texOffs(19, 50)
            .mirror()
            .addBox(-2.0F, -3.0F, -6.0F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(19, 50)
            .mirror()
            .addBox(-2.0F, -3.0F, -9.0F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(9, 35)
            .mirror()
            .addBox(-0.5F, -3.0F, -9.0F, 1.0F, 1.0F, 1.0F, new CubeDeformation(0.1F))
            .mirror(false)
            .texOffs(18, 49)
            .mirror()
            .addBox(-2.0F, -2.5F, -8.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(-0.1F))
            .mirror(false),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition head_r1 = Head.addOrReplaceChild(
         "head_r1",
         CubeListBuilder.create().texOffs(16, 2).addBox(-1.5F, -1.0F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(4.0F, -9.5F, 0.0F, 0.0F, 0.0F, 0.1047F)
      );
      PartDefinition head_r2 = Head.addOrReplaceChild(
         "head_r2",
         CubeListBuilder.create().texOffs(35, 15).addBox(0.1F, -2.0F, -4.0F, 0.0F, 4.0F, 8.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(-5.5F, -4.0F, 4.0F, -1.5708F, -0.7854F, 0.0F)
      );
      PartDefinition head_r3 = Head.addOrReplaceChild(
         "head_r3",
         CubeListBuilder.create().texOffs(34, 14).addBox(0.0F, -2.0F, -4.0F, 0.0F, 4.0F, 9.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(-2.5F, -4.0F, 5.0F, -1.5708F, -0.7854F, 0.0F)
      );
      PartDefinition head_r4 = Head.addOrReplaceChild(
         "head_r4",
         CubeListBuilder.create().texOffs(34, 14).mirror().addBox(0.0F, -2.0F, -4.0F, 0.0F, 4.0F, 9.0F, new CubeDeformation(0.1F)).mirror(false),
         PartPose.offsetAndRotation(2.5F, -4.0F, 5.0F, -1.5708F, 0.7854F, 0.0F)
      );
      PartDefinition head_r5 = Head.addOrReplaceChild(
         "head_r5",
         CubeListBuilder.create().texOffs(35, 15).mirror().addBox(-0.1F, -2.0F, -4.0F, 0.0F, 4.0F, 8.0F, new CubeDeformation(0.1F)).mirror(false),
         PartPose.offsetAndRotation(5.5F, -4.0F, 4.0F, -1.5708F, 0.7854F, 0.0F)
      );
      PartDefinition head_r6 = Head.addOrReplaceChild(
         "head_r6",
         CubeListBuilder.create().texOffs(16, 2).mirror().addBox(-1.5F, -1.0F, 0.0F, 3.0F, 3.0F, 0.0F, new CubeDeformation(0.1F)).mirror(false),
         PartPose.offsetAndRotation(-4.0F, -9.5F, 0.0F, 0.0F, 0.0F, -0.1047F)
      );
      PartDefinition Body = partdefinition.addOrReplaceChild(
         "Body",
         CubeListBuilder.create()
            .texOffs(38, 48)
            .addBox(-2.0F, 9.1234F, 0.196F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(46, 0)
            .addBox(-2.0F, 7.5F, 10.0F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.5F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition head_r7 = Body.addOrReplaceChild(
         "head_r7",
         CubeListBuilder.create().texOffs(33, 17).mirror().addBox(0.0F, -2.0F, -4.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.1F)).mirror(false),
         PartPose.offsetAndRotation(1.5F, 3.0F, 3.0F, -1.5708F, 0.7854F, 0.0F)
      );
      PartDefinition head_r8 = Body.addOrReplaceChild(
         "head_r8",
         CubeListBuilder.create().texOffs(33, 17).addBox(0.0F, -2.0F, -4.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(-1.5F, 3.0F, 3.0F, -1.5708F, -0.7854F, 0.0F)
      );
      PartDefinition head_r9 = Body.addOrReplaceChild(
         "head_r9",
         CubeListBuilder.create().texOffs(33, 17).addBox(0.0F, -2.0F, -4.0F, 0.0F, 4.0F, 6.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(-4.5F, 3.0F, 2.0F, -1.5708F, -0.7854F, 0.0F)
      );
      PartDefinition head_r10 = Body.addOrReplaceChild(
         "head_r10",
         CubeListBuilder.create().texOffs(33, 17).mirror().addBox(0.0F, -2.0F, -4.0F, 0.0F, 4.0F, 6.0F, new CubeDeformation(0.1F)).mirror(false),
         PartPose.offsetAndRotation(4.5F, 3.0F, 2.0F, -1.5708F, 0.7854F, 0.0F)
      );
      PartDefinition torso_r1 = Body.addOrReplaceChild(
         "torso_r1",
         CubeListBuilder.create().texOffs(46, 0).addBox(-2.0F, -2.0F, -1.5F, 4.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 9.5F, 15.5F, -0.3927F, 0.0F, 0.0F)
      );
      PartDefinition torso_r2 = Body.addOrReplaceChild(
         "torso_r2",
         CubeListBuilder.create().texOffs(36, 50).addBox(-3.0F, -3.3F, -4.15F, 6.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 10.8F, 8.15F, 0.3927F, 0.0F, 0.0F)
      );
      PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
      PartDefinition right_arm_r1 = RightArm.addOrReplaceChild(
         "right_arm_r1",
         CubeListBuilder.create()
            .texOffs(54, 51)
            .mirror()
            .addBox(-0.2893F, -1.6F, -2.3036F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(54, 51)
            .mirror()
            .addBox(-0.2893F, -1.6F, 1.2964F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-1.3707F, 11.1178F, -0.0243F, 0.0F, 0.0F, -0.3927F)
      );
      PartDefinition right_arm_r2 = RightArm.addOrReplaceChild(
         "right_arm_r2",
         CubeListBuilder.create()
            .texOffs(59, 26)
            .mirror()
            .addBox(-1.9192F, -0.4F, -1.6213F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
            .mirror(false)
            .texOffs(59, 26)
            .mirror()
            .addBox(0.6264F, -0.4F, 0.9243F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
            .mirror(false),
         PartPose.offsetAndRotation(-1.3707F, 11.1178F, -0.0243F, 0.0F, -0.7854F, -0.3927F)
      );
      PartDefinition right_arm_r3 = RightArm.addOrReplaceChild(
         "right_arm_r3",
         CubeListBuilder.create()
            .texOffs(54, 51)
            .mirror()
            .addBox(-0.2893F, -1.6F, -1.1036F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(54, 51)
            .mirror()
            .addBox(-0.2893F, -1.6F, 0.0964F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-1.3707F, 11.3178F, -0.0243F, 0.0F, 0.0F, -0.3927F)
      );
      PartDefinition right_arm_r4 = RightArm.addOrReplaceChild(
         "right_arm_r4",
         CubeListBuilder.create()
            .texOffs(59, 26)
            .mirror()
            .addBox(-1.0707F, -0.4F, -0.7728F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
            .mirror(false)
            .texOffs(59, 26)
            .mirror()
            .addBox(-0.2222F, -0.4F, 0.0757F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
            .mirror(false),
         PartPose.offsetAndRotation(-1.3707F, 11.3178F, -0.0243F, 0.0F, -0.7854F, -0.3927F)
      );
      PartDefinition right_arm_r5 = RightArm.addOrReplaceChild(
         "right_arm_r5",
         CubeListBuilder.create()
            .texOffs(54, 51)
            .mirror()
            .addBox(-0.2893F, -1.6F, -0.5036F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.1F))
            .mirror(false),
         PartPose.offsetAndRotation(-0.8707F, 9.7178F, -2.6243F, -1.6428F, -1.3263F, 1.6105F)
      );
      PartDefinition right_arm_r6 = RightArm.addOrReplaceChild(
         "right_arm_r6",
         CubeListBuilder.create()
            .texOffs(59, 26)
            .mirror()
            .addBox(-0.6464F, -0.4F, -0.3485F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F))
            .mirror(false),
         PartPose.offsetAndRotation(-0.8707F, 9.7178F, -2.6243F, -2.8088F, -0.7393F, 2.8783F)
      );
      PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
      PartDefinition left_arm_r1 = LeftArm.addOrReplaceChild(
         "left_arm_r1",
         CubeListBuilder.create()
            .texOffs(54, 51)
            .addBox(-0.7107F, -1.6F, -2.3036F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(54, 51)
            .addBox(-0.7107F, -1.6F, 1.2964F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(1.3707F, 11.1178F, -0.0243F, 0.0F, 0.0F, 0.3927F)
      );
      PartDefinition left_arm_r2 = LeftArm.addOrReplaceChild(
         "left_arm_r2",
         CubeListBuilder.create()
            .texOffs(59, 26)
            .addBox(0.9192F, -0.4F, -1.6213F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
            .texOffs(59, 26)
            .addBox(-1.6264F, -0.4F, 0.9243F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F)),
         PartPose.offsetAndRotation(1.3707F, 11.1178F, -0.0243F, 0.0F, 0.7854F, 0.3927F)
      );
      PartDefinition left_arm_r3 = LeftArm.addOrReplaceChild(
         "left_arm_r3",
         CubeListBuilder.create()
            .texOffs(54, 51)
            .addBox(-0.7107F, -1.6F, -1.1036F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
            .texOffs(54, 51)
            .addBox(-0.7107F, -1.6F, 0.0964F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(1.3707F, 11.3178F, -0.0243F, 0.0F, 0.0F, 0.3927F)
      );
      PartDefinition left_arm_r4 = LeftArm.addOrReplaceChild(
         "left_arm_r4",
         CubeListBuilder.create()
            .texOffs(59, 26)
            .addBox(0.0707F, -0.4F, -0.7728F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F))
            .texOffs(59, 26)
            .addBox(-0.7778F, -0.4F, 0.0757F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.2F)),
         PartPose.offsetAndRotation(1.3707F, 11.3178F, -0.0243F, 0.0F, 0.7854F, 0.3927F)
      );
      PartDefinition left_arm_r5 = LeftArm.addOrReplaceChild(
         "left_arm_r5",
         CubeListBuilder.create().texOffs(54, 51).addBox(-0.7107F, -1.6F, -0.5036F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(0.8707F, 9.7178F, -2.6243F, -1.6428F, 1.3263F, -1.6105F)
      );
      PartDefinition left_arm_r6 = LeftArm.addOrReplaceChild(
         "left_arm_r6",
         CubeListBuilder.create().texOffs(59, 26).addBox(-0.3536F, -0.4F, -0.3485F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.1F)),
         PartPose.offsetAndRotation(0.8707F, 9.7178F, -2.6243F, -2.8088F, 0.7393F, -2.8783F)
      );
      PartDefinition RightLeg = partdefinition.addOrReplaceChild(
         "RightLeg",
         CubeListBuilder.create().texOffs(40, 55).mirror().addBox(-1.5F, 11.0F, -3.3F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition right_leg_r1 = RightLeg.addOrReplaceChild(
         "right_leg_r1",
         CubeListBuilder.create().texOffs(48, 54).addBox(-4.5F, -1.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F)
      );
      PartDefinition right_leg_r2 = RightLeg.addOrReplaceChild(
         "right_leg_r2",
         CubeListBuilder.create()
            .texOffs(58, 26)
            .mirror()
            .addBox(0.4069F, 0.2107F, -1.5309F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(-1.0F, 11.575F, -2.7456F, 0.3454F, 0.2095F, -0.7314F)
      );
      PartDefinition right_leg_r3 = RightLeg.addOrReplaceChild(
         "right_leg_r3",
         CubeListBuilder.create()
            .texOffs(40, 55)
            .mirror()
            .addBox(0.6438F, -0.575F, -0.5436F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-1.0F, 11.575F, -2.7456F, 0.0F, -0.0873F, 0.0F)
      );
      PartDefinition left_leg_r1 = RightLeg.addOrReplaceChild(
         "left_leg_r1",
         CubeListBuilder.create().texOffs(58, 26).addBox(-0.4137F, -0.5863F, -1.4906F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(1.1432F, 11.575F, -2.1894F, 0.4964F, 0.0389F, -0.7126F)
      );
      PartDefinition left_leg_r2 = RightLeg.addOrReplaceChild(
         "left_leg_r2",
         CubeListBuilder.create().texOffs(40, 55).addBox(-0.5F, -0.575F, -0.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(1.1432F, 11.575F, -2.1894F, 0.0F, -0.3142F, 0.0F)
      );
      PartDefinition right_leg_r4 = RightLeg.addOrReplaceChild(
         "right_leg_r4",
         CubeListBuilder.create()
            .texOffs(58, 26)
            .mirror()
            .addBox(-0.5863F, -0.5863F, -1.4906F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(-2.1432F, 11.575F, -2.1894F, 0.4964F, -0.0389F, 0.7126F)
      );
      PartDefinition right_leg_r5 = RightLeg.addOrReplaceChild(
         "right_leg_r5",
         CubeListBuilder.create()
            .texOffs(40, 55)
            .mirror()
            .addBox(-0.5F, -0.575F, -0.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-2.1432F, 11.575F, -2.1894F, 0.0F, 0.3142F, 0.0F)
      );
      PartDefinition right_leg_r6 = RightLeg.addOrReplaceChild(
         "right_leg_r6",
         CubeListBuilder.create()
            .texOffs(58, 26)
            .mirror()
            .addBox(-0.399F, -0.601F, -1.5409F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(-1.0F, 11.575F, -2.7456F, 0.2849F, 0.274F, -0.7459F)
      );
      PartDefinition right_leg_r7 = RightLeg.addOrReplaceChild(
         "right_leg_r7",
         CubeListBuilder.create().texOffs(36, 53).mirror().addBox(-1.5F, -0.7F, -1.5F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(-0.5F, 11.6745F, 0.0163F, 0.1833F, 0.0F, 0.0F)
      );
      PartDefinition right_leg_r8 = RightLeg.addOrReplaceChild(
         "right_leg_r8",
         CubeListBuilder.create().texOffs(36, 53).mirror().addBox(-1.5F, -0.7F, -1.5F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(-0.5F, 11.2102F, -0.0776F, 0.3927F, 0.0F, 0.0F)
      );
      PartDefinition right_leg_r9 = RightLeg.addOrReplaceChild(
         "right_leg_r9",
         CubeListBuilder.create()
            .texOffs(38, 55)
            .mirror()
            .addBox(-4.0F, -4.0F, -0.5F, 3.0F, 6.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(2.0F, 8.5039F, 1.3272F, -0.2618F, 0.0F, 0.0F)
      );
      PartDefinition right_leg_r10 = RightLeg.addOrReplaceChild(
         "right_leg_r10",
         CubeListBuilder.create()
            .texOffs(37, 54)
            .mirror()
            .addBox(-4.0F, -0.4809F, -6.1484F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, 1.1345F, 0.0F, 0.0F)
      );
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild(
         "LeftLeg",
         CubeListBuilder.create().texOffs(40, 55).addBox(0.5F, 11.0F, -3.3F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      PartDefinition left_leg_r3 = LeftLeg.addOrReplaceChild(
         "left_leg_r3",
         CubeListBuilder.create().texOffs(48, 54).mirror().addBox(0.5F, -1.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F)
      );
      PartDefinition left_leg_r4 = LeftLeg.addOrReplaceChild(
         "left_leg_r4",
         CubeListBuilder.create().texOffs(58, 26).addBox(-1.4069F, 0.2107F, -1.5309F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(1.0F, 11.575F, -2.7456F, 0.3454F, -0.2095F, 0.7314F)
      );
      PartDefinition left_leg_r5 = LeftLeg.addOrReplaceChild(
         "left_leg_r5",
         CubeListBuilder.create().texOffs(40, 55).addBox(-1.6438F, -0.575F, -0.5436F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(1.0F, 11.575F, -2.7456F, 0.0F, 0.0873F, 0.0F)
      );
      PartDefinition right_leg_r11 = LeftLeg.addOrReplaceChild(
         "right_leg_r11",
         CubeListBuilder.create()
            .texOffs(58, 26)
            .mirror()
            .addBox(-0.5863F, -0.5863F, -1.4906F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(-1.1432F, 11.575F, -2.1894F, 0.4964F, -0.0389F, 0.7126F)
      );
      PartDefinition right_leg_r12 = LeftLeg.addOrReplaceChild(
         "right_leg_r12",
         CubeListBuilder.create()
            .texOffs(40, 55)
            .mirror()
            .addBox(-0.5F, -0.575F, -0.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(-1.1432F, 11.575F, -2.1894F, 0.0F, 0.3142F, 0.0F)
      );
      PartDefinition left_leg_r6 = LeftLeg.addOrReplaceChild(
         "left_leg_r6",
         CubeListBuilder.create().texOffs(58, 26).addBox(-0.4137F, -0.5863F, -1.4906F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(2.1432F, 11.575F, -2.1894F, 0.4964F, 0.0389F, -0.7126F)
      );
      PartDefinition left_leg_r7 = LeftLeg.addOrReplaceChild(
         "left_leg_r7",
         CubeListBuilder.create().texOffs(40, 55).addBox(-0.5F, -0.575F, -0.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(2.1432F, 11.575F, -2.1894F, 0.0F, -0.3142F, 0.0F)
      );
      PartDefinition left_leg_r8 = LeftLeg.addOrReplaceChild(
         "left_leg_r8",
         CubeListBuilder.create().texOffs(58, 26).addBox(-0.601F, -0.601F, -1.5409F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(1.0F, 11.575F, -2.7456F, 0.2849F, -0.274F, 0.7459F)
      );
      PartDefinition left_leg_r9 = LeftLeg.addOrReplaceChild(
         "left_leg_r9",
         CubeListBuilder.create().texOffs(36, 53).addBox(-1.5F, -0.7F, -1.5F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.5F, 11.6745F, 0.0163F, 0.1833F, 0.0F, 0.0F)
      );
      PartDefinition left_leg_r10 = LeftLeg.addOrReplaceChild(
         "left_leg_r10",
         CubeListBuilder.create().texOffs(36, 53).addBox(-1.5F, -0.7F, -1.5F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.5F, 11.2102F, -0.0776F, 0.3927F, 0.0F, 0.0F)
      );
      PartDefinition left_leg_r11 = LeftLeg.addOrReplaceChild(
         "left_leg_r11",
         CubeListBuilder.create().texOffs(38, 55).addBox(1.0F, -4.0F, -0.5F, 3.0F, 6.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(-2.0F, 8.5039F, 1.3272F, -0.2618F, 0.0F, 0.0F)
      );
      PartDefinition left_leg_r12 = LeftLeg.addOrReplaceChild(
         "left_leg_r12",
         CubeListBuilder.create().texOffs(37, 54).addBox(1.0F, -0.4809F, -6.1484F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, 1.1345F, 0.0F, 0.0F)
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
