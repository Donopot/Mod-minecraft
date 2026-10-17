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

public class ModeltoritoriPhoenixHumanbeastFly extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath("minepiece", "tori_tori_phoenix_humanbeast_fly"), "main"
   );
   public final ModelPart Head;
   public final ModelPart Body;
   public final ModelPart tail;
   public final ModelPart bone;
   public final ModelPart bone3;
   public final ModelPart bone2;
   public final ModelPart RightArm;
   public final ModelPart RightWing;
   public final ModelPart RightWing2;
   public final ModelPart LeftArm;
   public final ModelPart LeftWing;
   public final ModelPart LeftWing2;
   public final ModelPart RightLeg;
   public final ModelPart LeftLeg;

   public ModeltoritoriPhoenixHumanbeastFly(ModelPart root) {
      super(root);
      this.Head = root.getChild("Head");
      this.Body = root.getChild("Body");
      this.tail = this.Body.getChild("tail");
      this.bone = this.tail.getChild("bone");
      this.bone3 = this.bone.getChild("bone3");
      this.bone2 = this.bone3.getChild("bone2");
      this.RightArm = root.getChild("RightArm");
      this.RightWing = this.RightArm.getChild("RightWing");
      this.RightWing2 = this.RightWing.getChild("RightWing2");
      this.LeftArm = root.getChild("LeftArm");
      this.LeftWing = this.LeftArm.getChild("LeftWing");
      this.LeftWing2 = this.LeftWing.getChild("LeftWing2");
      this.RightLeg = root.getChild("RightLeg");
      this.LeftLeg = root.getChild("LeftLeg");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition Head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition Body = partdefinition.addOrReplaceChild(
         "Body",
         CubeListBuilder.create().texOffs(16, 32).addBox(-4.0F, 0.0F, -2.0F, 8.0F, 12.0F, 4.0F, new CubeDeformation(0.55F)),
         PartPose.offset(0.0F, 0.0F, 0.0F)
      );
      PartDefinition tail = Body.addOrReplaceChild(
         "tail",
         CubeListBuilder.create()
            .texOffs(40, 20)
            .addBox(-8.0F, 0.0F, 0.0F, 16.0F, 16.0F, 0.0F, new CubeDeformation(0.2F))
            .texOffs(54, 36)
            .addBox(-1.0F, 7.7274F, -2.0706F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, 0.2618F, 0.0F, 0.0F)
      );
      PartDefinition cube_r1 = tail.addOrReplaceChild(
         "cube_r1",
         CubeListBuilder.create().texOffs(54, 36).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(1.3892F, 7.61F, -2.0391F, 0.0039F, 0.0394F, -0.191F)
      );
      PartDefinition cube_r2 = tail.addOrReplaceChild(
         "cube_r2",
         CubeListBuilder.create().texOffs(54, 36).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2579F, 0.0394F, -0.191F)
      );
      PartDefinition cube_r3 = tail.addOrReplaceChild(
         "cube_r3",
         CubeListBuilder.create().texOffs(54, 36).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F)
      );
      PartDefinition cube_r4 = tail.addOrReplaceChild(
         "cube_r4",
         CubeListBuilder.create().texOffs(54, 36).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(-1.3892F, 7.61F, -2.0391F, 0.0039F, -0.0394F, 0.191F)
      );
      PartDefinition cube_r5 = tail.addOrReplaceChild(
         "cube_r5",
         CubeListBuilder.create().texOffs(54, 36).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2579F, -0.0394F, 0.191F)
      );
      PartDefinition bone = tail.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 15.2319F, -2.3351F, -0.3927F, 0.0F, 0.0F));
      PartDefinition cube_r6 = bone.addOrReplaceChild(
         "cube_r6",
         CubeListBuilder.create().texOffs(54, 36).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(0.0F, 0.4955F, 0.2646F, 0.2618F, 0.0F, 0.0F)
      );
      PartDefinition cube_r7 = bone.addOrReplaceChild(
         "cube_r7",
         CubeListBuilder.create().texOffs(54, 36).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(2.9088F, 0.2324F, 0.3269F, 0.2657F, 0.0394F, -0.191F)
      );
      PartDefinition cube_r8 = bone.addOrReplaceChild(
         "cube_r8",
         CubeListBuilder.create().texOffs(54, 36).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(-2.9088F, 0.2324F, 0.3269F, 0.2657F, -0.0394F, 0.191F)
      );
      PartDefinition bone3 = bone.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 8.2232F, 2.3352F, -0.3491F, 0.0F, 0.0F));
      PartDefinition cube_r9 = bone3.addOrReplaceChild(
         "cube_r9",
         CubeListBuilder.create().texOffs(54, 36).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5236F, 0.0F, 0.0F)
      );
      PartDefinition cube_r10 = bone3.addOrReplaceChild(
         "cube_r10",
         CubeListBuilder.create().texOffs(54, 36).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(4.4552F, -0.4272F, 0.0905F, 0.5275F, 0.0394F, -0.191F)
      );
      PartDefinition cube_r11 = bone3.addOrReplaceChild(
         "cube_r11",
         CubeListBuilder.create().texOffs(54, 36).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(-4.4552F, -0.4272F, 0.0905F, 0.5275F, -0.0394F, 0.191F)
      );
      PartDefinition bone2 = bone3.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 6.9284F, 4.0002F, -0.3491F, 0.0F, 0.0F));
      PartDefinition cube_r12 = bone2.addOrReplaceChild(
         "cube_r12",
         CubeListBuilder.create().texOffs(54, 36).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.7854F, 0.0F, 0.0F)
      );
      PartDefinition cube_r13 = bone2.addOrReplaceChild(
         "cube_r13",
         CubeListBuilder.create().texOffs(54, 36).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(5.9231F, -0.5985F, 0.1142F, 0.7893F, 0.0394F, -0.191F)
      );
      PartDefinition cube_r14 = bone2.addOrReplaceChild(
         "cube_r14",
         CubeListBuilder.create().texOffs(54, 36).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 8.0F, 0.0F, new CubeDeformation(0.1F)),
         PartPose.offsetAndRotation(-5.9231F, -0.5985F, 0.1142F, 0.7893F, -0.0394F, 0.191F)
      );
      PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
      PartDefinition RightWing = RightArm.addOrReplaceChild(
         "RightWing",
         CubeListBuilder.create().texOffs(48, 89).addBox(-15.0F, -10.0F, 0.0F, 16.0F, 24.0F, 0.0F, new CubeDeformation(0.2F)),
         PartPose.offsetAndRotation(1.0F, 0.0F, 0.0F, 0.0F, 0.1745F, 0.0F)
      );
      PartDefinition RightWing2 = RightWing.addOrReplaceChild(
         "RightWing2",
         CubeListBuilder.create().texOffs(36, 65).addBox(-28.0F, -8.0F, 0.0F, 28.0F, 24.0F, 0.0F, new CubeDeformation(0.2F)),
         PartPose.offsetAndRotation(-15.1414F, -2.0F, -0.1414F, 0.0F, -0.3491F, 0.0F)
      );
      PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
      PartDefinition LeftWing = LeftArm.addOrReplaceChild(
         "LeftWing",
         CubeListBuilder.create()
            .texOffs(48, 89)
            .mirror()
            .addBox(-1.0F, -10.0F, 0.0F, 16.0F, 24.0F, 0.0F, new CubeDeformation(0.2F))
            .mirror(false),
         PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.0F, -0.1745F, 0.0F)
      );
      PartDefinition LeftWing2 = LeftWing.addOrReplaceChild(
         "LeftWing2",
         CubeListBuilder.create().texOffs(36, 65).mirror().addBox(0.0F, -8.0F, 0.0F, 28.0F, 24.0F, 0.0F, new CubeDeformation(0.2F)).mirror(false),
         PartPose.offsetAndRotation(15.1414F, -2.0F, -0.1414F, 0.0F, 0.3491F, 0.0F)
      );
      PartDefinition RightLeg = partdefinition.addOrReplaceChild(
         "RightLeg",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-1.4F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F))
            .mirror(false)
            .texOffs(16, 0)
            .mirror()
            .addBox(-1.4F, 11.0F, -3.0F, 3.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition cube_r15 = RightLeg.addOrReplaceChild(
         "cube_r15",
         CubeListBuilder.create()
            .texOffs(34, 0)
            .mirror()
            .addBox(-0.25F, -1.75F, 1.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(2.0123F, 14.5881F, -7.3047F, 1.309F, -0.2618F, 0.0F)
      );
      PartDefinition cube_r16 = RightLeg.addOrReplaceChild(
         "cube_r16",
         CubeListBuilder.create().texOffs(26, 0).mirror().addBox(-0.5F, -1.75F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.01F)).mirror(false),
         PartPose.offsetAndRotation(2.2364F, 12.8881F, -7.1751F, 0.4363F, -0.2618F, 0.0F)
      );
      PartDefinition cube_r17 = RightLeg.addOrReplaceChild(
         "cube_r17",
         CubeListBuilder.create().texOffs(26, 0).mirror().addBox(-0.5F, -1.75F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(1.879F, 11.6787F, -5.8414F, -0.3491F, -0.2618F, 0.0F)
      );
      PartDefinition cube_r18 = RightLeg.addOrReplaceChild(
         "cube_r18",
         CubeListBuilder.create()
            .texOffs(34, 0)
            .mirror()
            .addBox(-0.25F, -1.75F, 1.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(-2.2952F, 14.5881F, -7.1753F, 1.309F, 0.2618F, 0.0F)
      );
      PartDefinition cube_r19 = RightLeg.addOrReplaceChild(
         "cube_r19",
         CubeListBuilder.create().texOffs(26, 0).mirror().addBox(-0.5F, -1.75F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.01F)).mirror(false),
         PartPose.offsetAndRotation(-2.0364F, 12.8881F, -7.1751F, 0.4363F, 0.2618F, 0.0F)
      );
      PartDefinition cube_r20 = RightLeg.addOrReplaceChild(
         "cube_r20",
         CubeListBuilder.create().texOffs(26, 0).mirror().addBox(-0.5F, -1.75F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(-1.679F, 11.6787F, -5.8414F, -0.3491F, 0.2618F, 0.0F)
      );
      PartDefinition cube_r21 = RightLeg.addOrReplaceChild(
         "cube_r21",
         CubeListBuilder.create()
            .texOffs(34, 0)
            .mirror()
            .addBox(-0.25F, -1.75F, 1.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(-0.15F, 14.5881F, -7.5235F, 1.309F, 0.0F, 0.0F)
      );
      PartDefinition cube_r22 = RightLeg.addOrReplaceChild(
         "cube_r22",
         CubeListBuilder.create().texOffs(26, 0).mirror().addBox(-0.5F, -1.75F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.01F)).mirror(false),
         PartPose.offsetAndRotation(0.1F, 12.8881F, -7.4564F, 0.4363F, 0.0F, 0.0F)
      );
      PartDefinition cube_r23 = RightLeg.addOrReplaceChild(
         "cube_r23",
         CubeListBuilder.create().texOffs(26, 0).mirror().addBox(-0.5F, -1.75F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(0.1F, 11.6787F, -6.0756F, -0.3491F, 0.0F, 0.0F)
      );
      PartDefinition cube_r24 = RightLeg.addOrReplaceChild(
         "cube_r24",
         CubeListBuilder.create()
            .texOffs(34, 0)
            .mirror()
            .addBox(-0.5F, -2.25F, -3.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(0.1F, 15.6833F, 4.5635F, -1.309F, 0.0F, 0.0F)
      );
      PartDefinition cube_r25 = RightLeg.addOrReplaceChild(
         "cube_r25",
         CubeListBuilder.create().texOffs(26, 0).mirror().addBox(-0.5F, -2.0F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(0.1F, 14.0805F, 4.8737F, -0.4363F, 0.0F, 0.0F)
      );
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild(
         "LeftLeg",
         CubeListBuilder.create()
            .texOffs(16, 0)
            .addBox(-1.6F, 11.0F, -3.0F, 3.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
            .texOffs(0, 0)
            .addBox(-1.6F, -1.0F, -1.5F, 3.0F, 12.0F, 3.0F, new CubeDeformation(0.0F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      PartDefinition cube_r26 = LeftLeg.addOrReplaceChild(
         "cube_r26",
         CubeListBuilder.create().texOffs(26, 0).addBox(-0.5F, -2.0F, -3.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-0.1F, 14.0805F, 4.8737F, -0.4363F, 0.0F, 0.0F)
      );
      PartDefinition cube_r27 = LeftLeg.addOrReplaceChild(
         "cube_r27",
         CubeListBuilder.create().texOffs(34, 0).addBox(-0.5F, -2.25F, -3.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(-0.1F, 15.6833F, 4.5635F, -1.309F, 0.0F, 0.0F)
      );
      PartDefinition cube_r28 = LeftLeg.addOrReplaceChild(
         "cube_r28",
         CubeListBuilder.create().texOffs(26, 0).addBox(-0.5F, -1.75F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-0.1F, 11.6787F, -6.0756F, -0.3491F, 0.0F, 0.0F)
      );
      PartDefinition cube_r29 = LeftLeg.addOrReplaceChild(
         "cube_r29",
         CubeListBuilder.create().texOffs(26, 0).addBox(-0.5F, -1.75F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.01F)),
         PartPose.offsetAndRotation(-0.1F, 12.8881F, -7.4564F, 0.4363F, 0.0F, 0.0F)
      );
      PartDefinition cube_r30 = LeftLeg.addOrReplaceChild(
         "cube_r30",
         CubeListBuilder.create().texOffs(34, 0).addBox(-0.75F, -1.75F, 1.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(0.15F, 14.5881F, -7.5235F, 1.309F, 0.0F, 0.0F)
      );
      PartDefinition cube_r31 = LeftLeg.addOrReplaceChild(
         "cube_r31",
         CubeListBuilder.create().texOffs(26, 0).addBox(-0.5F, -1.75F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(1.679F, 11.6787F, -5.8414F, -0.3491F, -0.2618F, 0.0F)
      );
      PartDefinition cube_r32 = LeftLeg.addOrReplaceChild(
         "cube_r32",
         CubeListBuilder.create().texOffs(26, 0).addBox(-0.5F, -1.75F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.01F)),
         PartPose.offsetAndRotation(2.0364F, 12.8881F, -7.1751F, 0.4363F, -0.2618F, 0.0F)
      );
      PartDefinition cube_r33 = LeftLeg.addOrReplaceChild(
         "cube_r33",
         CubeListBuilder.create().texOffs(34, 0).addBox(-0.75F, -1.75F, 1.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(2.2952F, 14.5881F, -7.1753F, 1.309F, -0.2618F, 0.0F)
      );
      PartDefinition cube_r34 = LeftLeg.addOrReplaceChild(
         "cube_r34",
         CubeListBuilder.create().texOffs(26, 0).addBox(-0.5F, -1.75F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-1.879F, 11.6787F, -5.8414F, -0.3491F, 0.2618F, 0.0F)
      );
      PartDefinition cube_r35 = LeftLeg.addOrReplaceChild(
         "cube_r35",
         CubeListBuilder.create().texOffs(26, 0).addBox(-0.5F, -1.75F, 0.0F, 1.0F, 1.0F, 3.0F, new CubeDeformation(0.01F)),
         PartPose.offsetAndRotation(-2.2364F, 12.8881F, -7.1751F, 0.4363F, 0.2618F, 0.0F)
      );
      PartDefinition cube_r36 = LeftLeg.addOrReplaceChild(
         "cube_r36",
         CubeListBuilder.create().texOffs(34, 0).addBox(-0.75F, -1.75F, 1.25F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(-2.0123F, 14.5881F, -7.3047F, 1.309F, 0.2618F, 0.0F)
      );
      return LayerDefinition.create(meshdefinition, 128, 128);
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
