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

public class ModelryuRyuPachycephalosaurusHumanbeast extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath("minepiece", "ryu_ryu_pachycephalosaurus_humanbeast"), "main"
   );
   public final ModelPart Head;
   public final ModelPart Body;
   public final ModelPart RightArm;
   public final ModelPart LeftArm;
   public final ModelPart RightLeg;
   public final ModelPart LeftLeg;

   public ModelryuRyuPachycephalosaurusHumanbeast(ModelPart root) {
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
      PartDefinition head_r1 = Head.addOrReplaceChild(
         "head_r1",
         CubeListBuilder.create().texOffs(0, 0).addBox(1.395F, 2.1702F, -1.4249F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-6.8733F, -8.7309F, -2.2622F, 0.191F, -0.0199F, 0.033F)
      );
      PartDefinition head_r2 = Head.addOrReplaceChild(
         "head_r2",
         CubeListBuilder.create().texOffs(0, 0).addBox(0.6097F, 0.9496F, -1.4171F, 2.8F, 2.0F, 2.0F, new CubeDeformation(-0.1F)),
         PartPose.offsetAndRotation(-6.8733F, -8.7309F, -2.2622F, 0.156F, -0.1124F, 0.5497F)
      );
      PartDefinition head_r3 = Head.addOrReplaceChild(
         "head_r3",
         CubeListBuilder.create().texOffs(0, 0).addBox(-0.2368F, 0.2555F, -1.3673F, 2.5F, 2.0F, 2.0F, new CubeDeformation(-0.15F)),
         PartPose.offsetAndRotation(-6.8733F, -8.7309F, -2.2622F, 0.085F, -0.1724F, 1.0399F)
      );
      PartDefinition head_r4 = Head.addOrReplaceChild(
         "head_r4",
         CubeListBuilder.create().texOffs(0, 0).addBox(-1.1417F, -0.9229F, -0.8984F, 2.6F, 2.0F, 2.0F, new CubeDeformation(-0.6F)),
         PartPose.offsetAndRotation(-7.2529F, -9.759F, -2.5581F, 0.0114F, -0.033F, -1.4227F)
      );
      PartDefinition head_r5 = Head.addOrReplaceChild(
         "head_r5",
         CubeListBuilder.create().texOffs(0, 0).addBox(-2.8515F, -0.9235F, -0.9017F, 2.9F, 2.0F, 2.0F, new CubeDeformation(-0.4F)),
         PartPose.offsetAndRotation(-7.2529F, -9.759F, -2.5581F, 0.0046F, -0.0346F, -1.2219F)
      );
      PartDefinition head_r6 = Head.addOrReplaceChild(
         "head_r6",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-3.395F, 2.1702F, -1.4249F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(6.8733F, -8.7309F, -2.2622F, 0.191F, 0.0199F, -0.033F)
      );
      PartDefinition head_r7 = Head.addOrReplaceChild(
         "head_r7",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-3.4097F, 0.9496F, -1.4171F, 2.8F, 2.0F, 2.0F, new CubeDeformation(-0.1F))
            .mirror(false),
         PartPose.offsetAndRotation(6.8733F, -8.7309F, -2.2622F, 0.156F, 0.1124F, -0.5497F)
      );
      PartDefinition head_r8 = Head.addOrReplaceChild(
         "head_r8",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-2.2632F, 0.2555F, -1.3673F, 2.5F, 2.0F, 2.0F, new CubeDeformation(-0.15F))
            .mirror(false),
         PartPose.offsetAndRotation(6.8733F, -8.7309F, -2.2622F, 0.085F, 0.1724F, -1.0399F)
      );
      PartDefinition head_r9 = Head.addOrReplaceChild(
         "head_r9",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4583F, -0.9229F, -0.8984F, 2.6F, 2.0F, 2.0F, new CubeDeformation(-0.6F))
            .mirror(false),
         PartPose.offsetAndRotation(7.2529F, -9.759F, -2.5581F, 0.0114F, 0.033F, 1.4227F)
      );
      PartDefinition head_r10 = Head.addOrReplaceChild(
         "head_r10",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-0.0485F, -0.9235F, -0.9017F, 2.9F, 2.0F, 2.0F, new CubeDeformation(-0.4F))
            .mirror(false),
         PartPose.offsetAndRotation(7.2529F, -9.759F, -2.5581F, 0.0046F, 0.0346F, 1.2219F)
      );
      PartDefinition head_r11 = Head.addOrReplaceChild(
         "head_r11",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.0F, 0.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-3.0F, 0.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.5F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-2.5F, 0.3F, 0.8F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.7F))
            .mirror(false)
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.0F, 0.3F, 0.8F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.4F))
            .mirror(false),
         PartPose.offsetAndRotation(-4.5F, -6.5F, 0.0F, 0.0F, 0.0F, 0.1745F)
      );
      PartDefinition head_r12 = Head.addOrReplaceChild(
         "head_r12",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .addBox(-0.5F, 0.3F, 0.8F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.7F))
            .texOffs(0, 0)
            .addBox(-2.0F, 0.3F, 0.8F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.4F))
            .texOffs(0, 0)
            .addBox(0.0F, 0.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.5F))
            .texOffs(0, 0)
            .addBox(-2.0F, 0.0F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F)),
         PartPose.offsetAndRotation(4.5F, -6.5F, 0.0F, 0.0F, 0.0F, -0.1745F)
      );
      PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition torso_r1 = Body.addOrReplaceChild(
         "torso_r1",
         CubeListBuilder.create().texOffs(17, 0).addBox(-1.5F, 0.1F, -2.5F, 3.0F, 3.0F, 6.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(0.0F, 20.7957F, 14.5359F, -1.1781F, 0.0F, 0.0F)
      );
      PartDefinition torso_r2 = Body.addOrReplaceChild(
         "torso_r2",
         CubeListBuilder.create().texOffs(17, 0).addBox(-2.0F, -0.6F, -3.0F, 3.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.5F, 17.255F, 11.0248F, -0.7854F, 0.0F, 0.0F)
      );
      PartDefinition torso_r3 = Body.addOrReplaceChild(
         "torso_r3",
         CubeListBuilder.create().texOffs(16, 0).addBox(-2.0F, -2.4F, -2.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(0.0F, 15.3284F, 5.3284F, -0.3927F, 0.0F, 0.0F)
      );
      PartDefinition torso_r4 = Body.addOrReplaceChild(
         "torso_r4",
         CubeListBuilder.create().texOffs(16, 0).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, 12.5F, 2.5F, -0.7854F, 0.0F, 0.0F)
      );
      PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
      PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
      PartDefinition RightLeg = partdefinition.addOrReplaceChild(
         "RightLeg",
         CubeListBuilder.create().texOffs(40, 2).mirror().addBox(-1.0F, 11.0F, -3.3F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition right_leg_r1 = RightLeg.addOrReplaceChild(
         "right_leg_r1",
         CubeListBuilder.create()
            .texOffs(58, 0)
            .mirror()
            .addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(0.7868F, 11.65F, -3.1875F, 0.3454F, 0.2095F, -0.7314F)
      );
      PartDefinition right_leg_r2 = RightLeg.addOrReplaceChild(
         "right_leg_r2",
         CubeListBuilder.create().texOffs(40, 2).mirror().addBox(-0.4F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(0.6F, 11.5F, -2.2F, 0.0F, -0.0873F, 0.0F)
      );
      PartDefinition right_leg_r3 = RightLeg.addOrReplaceChild(
         "right_leg_r3",
         CubeListBuilder.create()
            .texOffs(58, 0)
            .mirror()
            .addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(-1.7868F, 11.65F, -3.1875F, 0.3454F, -0.2095F, 0.7314F)
      );
      PartDefinition right_leg_r4 = RightLeg.addOrReplaceChild(
         "right_leg_r4",
         CubeListBuilder.create().texOffs(40, 2).mirror().addBox(-0.6F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(-1.6F, 11.5F, -2.2F, 0.0F, 0.0873F, 0.0F)
      );
      PartDefinition right_leg_r5 = RightLeg.addOrReplaceChild(
         "right_leg_r5",
         CubeListBuilder.create()
            .texOffs(58, 0)
            .mirror()
            .addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(-0.5F, 11.65F, -3.3F, 0.2849F, 0.274F, -0.7459F)
      );
      PartDefinition right_leg_r6 = RightLeg.addOrReplaceChild(
         "right_leg_r6",
         CubeListBuilder.create().texOffs(36, 0).mirror().addBox(-1.5F, -0.7F, -1.5F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(-0.5F, 11.2102F, -0.0776F, 0.3927F, 0.0F, 0.0F)
      );
      PartDefinition right_leg_r7 = RightLeg.addOrReplaceChild(
         "right_leg_r7",
         CubeListBuilder.create().texOffs(42, 2).mirror().addBox(-0.5F, -2.3F, -0.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(-0.5F, 11.6605F, 1.5167F, -0.2618F, 0.0F, 0.0F)
      );
      PartDefinition right_leg_r8 = RightLeg.addOrReplaceChild(
         "right_leg_r8",
         CubeListBuilder.create()
            .texOffs(60, 0)
            .mirror()
            .addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(-0.5F, 11.4224F, 1.7875F, -0.3622F, 0.7519F, -0.2533F)
      );
      PartDefinition right_leg_r9 = RightLeg.addOrReplaceChild(
         "right_leg_r9",
         CubeListBuilder.create()
            .texOffs(38, 2)
            .mirror()
            .addBox(-4.0F, -4.0F, -0.5F, 3.0F, 6.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(2.0F, 8.5039F, 1.3272F, -0.2618F, 0.0F, 0.0F)
      );
      PartDefinition right_leg_r10 = RightLeg.addOrReplaceChild(
         "right_leg_r10",
         CubeListBuilder.create()
            .texOffs(37, 1)
            .mirror()
            .addBox(-4.0F, -0.4809F, -6.1484F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, 1.1345F, 0.0F, 0.0F)
      );
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild(
         "LeftLeg",
         CubeListBuilder.create().texOffs(40, 2).addBox(0.0F, 11.0F, -3.3F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      PartDefinition left_leg_r1 = LeftLeg.addOrReplaceChild(
         "left_leg_r1",
         CubeListBuilder.create().texOffs(58, 0).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(-0.7868F, 11.65F, -3.1875F, 0.3454F, -0.2095F, 0.7314F)
      );
      PartDefinition left_leg_r2 = LeftLeg.addOrReplaceChild(
         "left_leg_r2",
         CubeListBuilder.create().texOffs(40, 2).addBox(-0.6F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-0.6F, 11.5F, -2.2F, 0.0F, 0.0873F, 0.0F)
      );
      PartDefinition left_leg_r3 = LeftLeg.addOrReplaceChild(
         "left_leg_r3",
         CubeListBuilder.create().texOffs(58, 0).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(1.7868F, 11.65F, -3.1875F, 0.3454F, 0.2095F, -0.7314F)
      );
      PartDefinition left_leg_r4 = LeftLeg.addOrReplaceChild(
         "left_leg_r4",
         CubeListBuilder.create().texOffs(40, 2).addBox(-0.4F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(1.6F, 11.5F, -2.2F, 0.0F, -0.0873F, 0.0F)
      );
      PartDefinition left_leg_r5 = LeftLeg.addOrReplaceChild(
         "left_leg_r5",
         CubeListBuilder.create().texOffs(58, 0).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(0.5F, 11.65F, -3.3F, 0.2849F, -0.274F, 0.7459F)
      );
      PartDefinition left_leg_r6 = LeftLeg.addOrReplaceChild(
         "left_leg_r6",
         CubeListBuilder.create().texOffs(36, 0).addBox(-1.5F, -0.7F, -1.5F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.5F, 11.2102F, -0.0776F, 0.3927F, 0.0F, 0.0F)
      );
      PartDefinition left_leg_r7 = LeftLeg.addOrReplaceChild(
         "left_leg_r7",
         CubeListBuilder.create().texOffs(42, 2).addBox(-0.5F, -2.3F, -0.3F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.5F, 11.6605F, 1.5167F, -0.2618F, 0.0F, 0.0F)
      );
      PartDefinition left_leg_r8 = LeftLeg.addOrReplaceChild(
         "left_leg_r8",
         CubeListBuilder.create().texOffs(60, 0).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(0.5F, 11.4224F, 1.7875F, -0.3622F, -0.7519F, 0.2533F)
      );
      PartDefinition left_leg_r9 = LeftLeg.addOrReplaceChild(
         "left_leg_r9",
         CubeListBuilder.create().texOffs(38, 2).addBox(1.0F, -4.0F, -0.5F, 3.0F, 6.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(-2.0F, 8.5039F, 1.3272F, -0.2618F, 0.0F, 0.0F)
      );
      PartDefinition left_leg_r10 = LeftLeg.addOrReplaceChild(
         "left_leg_r10",
         CubeListBuilder.create().texOffs(37, 1).addBox(1.0F, -0.4809F, -6.1484F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, 1.1345F, 0.0F, 0.0F)
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
