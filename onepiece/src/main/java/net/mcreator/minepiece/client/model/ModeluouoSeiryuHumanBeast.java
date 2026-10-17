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

public class ModeluouoSeiryuHumanBeast extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("minepiece", "uo_uo_seiryu_humanbeast"), "main");
   public final ModelPart Body;
   public final ModelPart Head;
   public final ModelPart RightArm;
   public final ModelPart LeftArm;
   public final ModelPart LeftLeg;
   public final ModelPart RightLeg;

   public ModeluouoSeiryuHumanBeast(ModelPart root) {
      super(root);
      this.Body = root.getChild("Body");
      this.Head = root.getChild("Head");
      this.RightArm = root.getChild("RightArm");
      this.LeftArm = root.getChild("LeftArm");
      this.LeftLeg = root.getChild("LeftLeg");
      this.RightLeg = root.getChild("RightLeg");
   }

   public static LayerDefinition createBodyLayer() {
      MeshDefinition meshdefinition = new MeshDefinition();
      PartDefinition partdefinition = meshdefinition.getRoot();
      PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition torso_r1 = Body.addOrReplaceChild(
         "torso_r1",
         CubeListBuilder.create()
            .texOffs(48, 2)
            .addBox(-2.0F, -7.0F, -1.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(-0.45F))
            .texOffs(1, 8)
            .addBox(0.0F, -17.0305F, -3.6993F, 0.0F, 11.0F, 10.0F, new CubeDeformation(0.05F))
            .texOffs(24, 13)
            .addBox(0.0F, -7.1F, -4.0F, 0.0F, 8.0F, 4.0F, new CubeDeformation(0.05F))
            .texOffs(37, 0)
            .addBox(-1.5F, -7.0F, 2.0F, 3.0F, 8.0F, 2.0F, new CubeDeformation(-0.45F)),
         PartPose.offsetAndRotation(0.0F, 12.0513F, 17.3429F, -1.5272F, 0.0F, 0.0F)
      );
      PartDefinition torso_r2 = Body.addOrReplaceChild(
         "torso_r2",
         CubeListBuilder.create().texOffs(1, 8).addBox(0.6993F, -5.5305F, -5.0F, 0.0F, 11.0F, 10.0F, new CubeDeformation(0.05F)),
         PartPose.offsetAndRotation(0.0F, 13.5478F, 28.9192F, -1.5708F, 0.0436F, -1.5708F)
      );
      PartDefinition torso_r3 = Body.addOrReplaceChild(
         "torso_r3",
         CubeListBuilder.create().texOffs(1, 8).addBox(0.5022F, -5.5653F, -4.5168F, 0.0F, 11.0F, 10.0F, new CubeDeformation(0.05F)),
         PartPose.offsetAndRotation(0.0F, 13.5478F, 28.9192F, -1.7245F, -0.0188F, -2.3413F)
      );
      PartDefinition torso_r4 = Body.addOrReplaceChild(
         "torso_r4",
         CubeListBuilder.create().texOffs(1, 8).addBox(0.4832F, -5.5653F, -5.5022F, 0.0F, 11.0F, 10.0F, new CubeDeformation(0.05F)),
         PartPose.offsetAndRotation(0.0F, 13.5478F, 28.9192F, -1.5898F, 0.1537F, -0.7734F)
      );
      PartDefinition torso_r5 = Body.addOrReplaceChild(
         "torso_r5",
         CubeListBuilder.create()
            .texOffs(48, 2)
            .addBox(-2.0F, -8.0F, -1.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(-0.15F))
            .texOffs(24, 13)
            .addBox(0.0F, -8.0F, -4.6F, 0.0F, 8.0F, 4.0F, new CubeDeformation(0.05F))
            .texOffs(37, 0)
            .addBox(-1.5F, -8.0F, 2.0F, 3.0F, 9.0F, 2.0F, new CubeDeformation(-0.15F)),
         PartPose.offsetAndRotation(0.0F, 11.3581F, 9.5615F, -1.6581F, 0.0F, 0.0F)
      );
      PartDefinition torso_r6 = Body.addOrReplaceChild(
         "torso_r6",
         CubeListBuilder.create()
            .texOffs(48, 2)
            .addBox(-2.0F, -8.0F, -1.0F, 4.0F, 8.0F, 4.0F, new CubeDeformation(0.05F))
            .texOffs(24, 13)
            .addBox(0.0F, -8.0F, -5.0F, 0.0F, 8.0F, 4.0F, new CubeDeformation(0.05F))
            .texOffs(40, 0)
            .addBox(-1.5F, -8.0F, 3.0F, 3.0F, 8.0F, 1.0F, new CubeDeformation(0.05F)),
         PartPose.offsetAndRotation(0.0F, 10.0F, 2.0F, -1.7453F, 0.0F, 0.0F)
      );
      PartDefinition Head = partdefinition.addOrReplaceChild("Head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition head_r1 = Head.addOrReplaceChild(
         "head_r1",
         CubeListBuilder.create().texOffs(7, 37).addBox(-0.9844F, 0.0F, -0.9688F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-1.5926F, -7.6019F, -3.7109F, 0.0693F, 0.2732F, -0.0759F)
      );
      PartDefinition head_r2 = Head.addOrReplaceChild(
         "head_r2",
         CubeListBuilder.create().texOffs(1, 0).addBox(-2.0F, -0.8F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F)),
         PartPose.offsetAndRotation(-1.3838F, -7.5935F, -3.7482F, 0.2592F, -0.0688F, 1.4787F)
      );
      PartDefinition head_r3 = Head.addOrReplaceChild(
         "head_r3",
         CubeListBuilder.create()
            .texOffs(-1, 0)
            .addBox(-1.0F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F))
            .texOffs(0, 11)
            .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.19F)),
         PartPose.offsetAndRotation(-1.773F, -12.1319F, -4.0791F, 0.2483F, -0.1017F, 1.6059F)
      );
      PartDefinition head_r4 = Head.addOrReplaceChild(
         "head_r4",
         CubeListBuilder.create().texOffs(16, 0).addBox(-3.5F, -1.2F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.4F)),
         PartPose.offsetAndRotation(-2.038F, -12.1621F, -4.0135F, 0.2299F, -0.1389F, 1.7596F)
      );
      PartDefinition head_r5 = Head.addOrReplaceChild(
         "head_r5",
         CubeListBuilder.create().texOffs(17, 0).addBox(-0.2351F, -0.2811F, -1.3726F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F)),
         PartPose.offsetAndRotation(-7.2051F, -8.6158F, -4.5296F, -0.5476F, 0.0372F, 1.5544F)
      );
      PartDefinition head_r6 = Head.addOrReplaceChild(
         "head_r6",
         CubeListBuilder.create().texOffs(16, 0).addBox(-1.855F, -0.924F, -1.0017F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.4F)),
         PartPose.offsetAndRotation(-7.5181F, -9.679F, -4.7752F, 0.4687F, -0.3921F, -1.4011F)
      );
      PartDefinition head_r7 = Head.addOrReplaceChild(
         "head_r7",
         CubeListBuilder.create().texOffs(0, 11).addBox(0.2632F, 0.2555F, -1.3673F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.15F)),
         PartPose.offsetAndRotation(-7.2051F, -8.6158F, -4.5296F, -0.4991F, -0.2384F, 1.0848F)
      );
      PartDefinition head_r8 = Head.addOrReplaceChild(
         "head_r8",
         CubeListBuilder.create().texOffs(0, 0).addBox(0.4097F, 0.9496F, -1.4171F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F)),
         PartPose.offsetAndRotation(-7.0739F, -8.7308F, -4.7989F, -0.2548F, -0.6495F, 0.7101F)
      );
      PartDefinition head_r9 = Head.addOrReplaceChild(
         "head_r9",
         CubeListBuilder.create().texOffs(-1, 0).addBox(0.395F, 2.1702F, -1.4249F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-6.2739F, -8.7308F, -4.1989F, 0.1652F, -0.674F, 0.0423F)
      );
      PartDefinition head_r10 = Head.addOrReplaceChild(
         "head_r10",
         CubeListBuilder.create().texOffs(17, 0).addBox(-0.1352F, -0.2817F, -1.3685F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.4F)),
         PartPose.offsetAndRotation(-4.7712F, -7.9158F, 1.5515F, 0.6748F, 0.0281F, 1.6002F)
      );
      PartDefinition head_r11 = Head.addOrReplaceChild(
         "head_r11",
         CubeListBuilder.create().texOffs(16, 0).addBox(-2.6884F, -0.7851F, -1.336F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.6F)),
         PartPose.offsetAndRotation(-5.109F, -8.9791F, 1.7616F, -0.8036F, 0.1876F, -1.3206F)
      );
      PartDefinition head_r12 = Head.addOrReplaceChild(
         "head_r12",
         CubeListBuilder.create().texOffs(0, 11).addBox(0.2632F, 0.2554F, -1.3673F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.35F)),
         PartPose.offsetAndRotation(-4.7712F, -7.9158F, 1.5515F, 0.5884F, 0.3531F, 1.1572F)
      );
      PartDefinition head_r13 = Head.addOrReplaceChild(
         "head_r13",
         CubeListBuilder.create().texOffs(0, 0).addBox(0.4097F, 0.9496F, -1.4171F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.3F)),
         PartPose.offsetAndRotation(-4.9794F, -8.0309F, 1.3361F, 0.4617F, 0.369F, 0.5903F)
      );
      PartDefinition head_r14 = Head.addOrReplaceChild(
         "head_r14",
         CubeListBuilder.create()
            .texOffs(7, 37)
            .mirror()
            .addBox(-1.0156F, 0.0F, -0.9688F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(1.5926F, -7.6019F, -3.7109F, 0.0693F, -0.2732F, 0.0759F)
      );
      PartDefinition head_r15 = Head.addOrReplaceChild(
         "head_r15",
         CubeListBuilder.create().texOffs(1, 0).mirror().addBox(-1.0F, -0.8F, -1.0F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F)).mirror(false),
         PartPose.offsetAndRotation(1.3838F, -7.5935F, -3.7482F, 0.2592F, 0.0688F, -1.4787F)
      );
      PartDefinition head_r16 = Head.addOrReplaceChild(
         "head_r16",
         CubeListBuilder.create()
            .texOffs(-1, 0)
            .mirror()
            .addBox(-3.0F, -1.0F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F))
            .mirror(false)
            .texOffs(0, 11)
            .mirror()
            .addBox(-1.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.19F))
            .mirror(false),
         PartPose.offsetAndRotation(1.773F, -12.1319F, -4.0791F, 0.2483F, 0.1017F, -1.6059F)
      );
      PartDefinition head_r17 = Head.addOrReplaceChild(
         "head_r17",
         CubeListBuilder.create().texOffs(16, 0).mirror().addBox(-0.5F, -1.2F, -1.0F, 4.0F, 2.0F, 2.0F, new CubeDeformation(-0.4F)).mirror(false),
         PartPose.offsetAndRotation(2.038F, -12.1621F, -4.0135F, 0.2299F, 0.1389F, -1.7596F)
      );
      PartDefinition head_r18 = Head.addOrReplaceChild(
         "head_r18",
         CubeListBuilder.create()
            .texOffs(17, 0)
            .mirror()
            .addBox(-1.7649F, -0.2811F, -1.3726F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.2F))
            .mirror(false),
         PartPose.offsetAndRotation(7.2051F, -8.6158F, -4.5296F, -0.5476F, -0.0372F, -1.5544F)
      );
      PartDefinition head_r19 = Head.addOrReplaceChild(
         "head_r19",
         CubeListBuilder.create()
            .texOffs(16, 0)
            .mirror()
            .addBox(-0.145F, -0.924F, -1.0017F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.4F))
            .mirror(false),
         PartPose.offsetAndRotation(7.5181F, -9.679F, -4.7752F, 0.4687F, 0.3921F, 1.4011F)
      );
      PartDefinition head_r20 = Head.addOrReplaceChild(
         "head_r20",
         CubeListBuilder.create().texOffs(16, 0).addBox(-0.945F, -0.924F, -1.0609F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.6F)),
         PartPose.offsetAndRotation(-7.5181F, -9.679F, -4.7752F, 0.5066F, 0.0083F, -1.4138F)
      );
      PartDefinition head_r21 = Head.addOrReplaceChild(
         "head_r21",
         CubeListBuilder.create()
            .texOffs(16, 0)
            .mirror()
            .addBox(-2.055F, -0.924F, -1.0609F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.6F))
            .mirror(false),
         PartPose.offsetAndRotation(7.5181F, -9.679F, -4.7752F, 0.5066F, -0.0083F, 1.4138F)
      );
      PartDefinition head_r22 = Head.addOrReplaceChild(
         "head_r22",
         CubeListBuilder.create()
            .texOffs(0, 11)
            .mirror()
            .addBox(-2.2632F, 0.2555F, -1.3673F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.15F))
            .mirror(false),
         PartPose.offsetAndRotation(7.2051F, -8.6158F, -4.5296F, -0.4991F, 0.2384F, -1.0848F)
      );
      PartDefinition head_r23 = Head.addOrReplaceChild(
         "head_r23",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-3.4097F, 0.9496F, -1.4171F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.1F))
            .mirror(false),
         PartPose.offsetAndRotation(7.0739F, -8.7308F, -4.7989F, -0.2548F, 0.6495F, -0.7101F)
      );
      PartDefinition head_r24 = Head.addOrReplaceChild(
         "head_r24",
         CubeListBuilder.create()
            .texOffs(-1, 0)
            .mirror()
            .addBox(-4.395F, 2.1702F, -1.4249F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(6.2739F, -8.7308F, -4.1989F, 0.1652F, 0.674F, -0.0423F)
      );
      PartDefinition head_r25 = Head.addOrReplaceChild(
         "head_r25",
         CubeListBuilder.create()
            .texOffs(17, 0)
            .mirror()
            .addBox(-1.8648F, -0.2817F, -1.3685F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.4F))
            .mirror(false),
         PartPose.offsetAndRotation(4.7712F, -7.9158F, 1.5515F, 0.6748F, -0.0281F, -1.6002F)
      );
      PartDefinition head_r26 = Head.addOrReplaceChild(
         "head_r26",
         CubeListBuilder.create()
            .texOffs(16, 0)
            .mirror()
            .addBox(-0.3116F, -0.7851F, -1.336F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.6F))
            .mirror(false),
         PartPose.offsetAndRotation(5.109F, -8.9791F, 1.7616F, -0.8036F, -0.1876F, 1.3206F)
      );
      PartDefinition head_r27 = Head.addOrReplaceChild(
         "head_r27",
         CubeListBuilder.create()
            .texOffs(0, 11)
            .mirror()
            .addBox(-2.2632F, 0.2554F, -1.3673F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.35F))
            .mirror(false),
         PartPose.offsetAndRotation(4.7712F, -7.9158F, 1.5515F, 0.5884F, -0.3531F, -1.1572F)
      );
      PartDefinition head_r28 = Head.addOrReplaceChild(
         "head_r28",
         CubeListBuilder.create()
            .texOffs(0, 0)
            .mirror()
            .addBox(-3.4097F, 0.9496F, -1.4171F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.3F))
            .mirror(false),
         PartPose.offsetAndRotation(4.9794F, -8.0309F, 1.3361F, 0.4617F, -0.369F, -0.5903F)
      );
      PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
      PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild(
         "LeftLeg",
         CubeListBuilder.create()
            .texOffs(0, 38)
            .addBox(-1.0F, 10.9644F, -1.5936F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.1F))
            .texOffs(4, 40)
            .addBox(0.0F, 11.0F, -3.3F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offset(2.0F, 12.0F, 0.0F)
      );
      PartDefinition left_leg_r1 = LeftLeg.addOrReplaceChild(
         "left_leg_r1",
         CubeListBuilder.create().texOffs(24, 45).addBox(-0.5863F, -0.5863F, -1.4906F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(-1.1432F, 11.275F, -0.1894F, 0.8023F, -0.0857F, 0.7153F)
      );
      PartDefinition left_leg_r2 = LeftLeg.addOrReplaceChild(
         "left_leg_r2",
         CubeListBuilder.create().texOffs(4, 40).addBox(-0.5F, -0.575F, -0.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-1.1432F, 11.275F, -0.1894F, 0.2934F, 0.4623F, 0.1339F)
      );
      PartDefinition left_leg_r3 = LeftLeg.addOrReplaceChild(
         "left_leg_r3",
         CubeListBuilder.create().texOffs(24, 45).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(-0.6868F, 11.65F, -3.1875F, 0.3454F, -0.2095F, 0.7314F)
      );
      PartDefinition left_leg_r4 = LeftLeg.addOrReplaceChild(
         "left_leg_r4",
         CubeListBuilder.create().texOffs(4, 40).addBox(-0.6F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-0.5F, 11.5F, -2.2F, 0.0F, 0.0873F, 0.0F)
      );
      PartDefinition left_leg_r5 = LeftLeg.addOrReplaceChild(
         "left_leg_r5",
         CubeListBuilder.create().texOffs(24, 45).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(1.6868F, 11.65F, -3.1875F, 0.3454F, 0.2095F, -0.7314F)
      );
      PartDefinition left_leg_r6 = LeftLeg.addOrReplaceChild(
         "left_leg_r6",
         CubeListBuilder.create().texOffs(4, 40).addBox(-0.4F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(1.5F, 11.5F, -2.2F, 0.0F, -0.0873F, 0.0F)
      );
      PartDefinition left_leg_r7 = LeftLeg.addOrReplaceChild(
         "left_leg_r7",
         CubeListBuilder.create().texOffs(24, 45).addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(0.5F, 11.65F, -3.3F, 0.2849F, -0.274F, 0.7459F)
      );
      PartDefinition left_leg_r8 = LeftLeg.addOrReplaceChild(
         "left_leg_r8",
         CubeListBuilder.create().texOffs(0, 38).addBox(-1.5F, -0.8F, -1.5F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.5F, 11.2102F, -0.0776F, 0.2182F, 0.0F, 0.0F)
      );
      PartDefinition left_leg_r9 = LeftLeg.addOrReplaceChild(
         "left_leg_r9",
         CubeListBuilder.create().texOffs(24, 38).addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(0.5F, 11.4224F, 1.7875F, -0.3622F, -0.7519F, 0.2533F)
      );
      PartDefinition left_leg_r10 = LeftLeg.addOrReplaceChild(
         "left_leg_r10",
         CubeListBuilder.create().texOffs(54, 9).addBox(1.0F, -4.0F, -0.5F, 3.0F, 7.0F, 2.0F, new CubeDeformation(-0.25F)),
         PartPose.offsetAndRotation(-2.0F, 8.5039F, 1.3272F, -0.2618F, 0.0F, 0.0F)
      );
      PartDefinition left_leg_r11 = LeftLeg.addOrReplaceChild(
         "left_leg_r11",
         CubeListBuilder.create().texOffs(49, 4).addBox(1.0F, -0.4809F, -6.1484F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, 1.1345F, 0.0F, 0.0F)
      );
      PartDefinition left_leg_r12 = LeftLeg.addOrReplaceChild(
         "left_leg_r12",
         CubeListBuilder.create().texOffs(48, 3).addBox(0.5F, -1.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F)
      );
      PartDefinition RightLeg = partdefinition.addOrReplaceChild(
         "RightLeg",
         CubeListBuilder.create()
            .texOffs(0, 38)
            .mirror()
            .addBox(-2.0F, 10.9644F, -1.5936F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.1F))
            .mirror(false)
            .texOffs(4, 40)
            .mirror()
            .addBox(-1.0F, 11.0F, -3.3F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offset(-2.0F, 12.0F, 0.0F)
      );
      PartDefinition right_leg_r1 = RightLeg.addOrReplaceChild(
         "right_leg_r1",
         CubeListBuilder.create()
            .texOffs(24, 45)
            .mirror()
            .addBox(-0.4137F, -0.5863F, -1.4906F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(1.1432F, 11.275F, -0.1894F, 0.8023F, 0.0857F, -0.7153F)
      );
      PartDefinition right_leg_r2 = RightLeg.addOrReplaceChild(
         "right_leg_r2",
         CubeListBuilder.create()
            .texOffs(4, 40)
            .mirror()
            .addBox(-0.5F, -0.575F, -0.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(1.1432F, 11.275F, -0.1894F, 0.2934F, -0.4623F, -0.1339F)
      );
      PartDefinition right_leg_r3 = RightLeg.addOrReplaceChild(
         "right_leg_r3",
         CubeListBuilder.create()
            .texOffs(24, 45)
            .mirror()
            .addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(0.6868F, 11.65F, -3.1875F, 0.3454F, 0.2095F, -0.7314F)
      );
      PartDefinition right_leg_r4 = RightLeg.addOrReplaceChild(
         "right_leg_r4",
         CubeListBuilder.create().texOffs(4, 40).mirror().addBox(-0.4F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(0.5F, 11.5F, -2.2F, 0.0F, -0.0873F, 0.0F)
      );
      PartDefinition right_leg_r5 = RightLeg.addOrReplaceChild(
         "right_leg_r5",
         CubeListBuilder.create()
            .texOffs(24, 45)
            .mirror()
            .addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(-1.6868F, 11.65F, -3.1875F, 0.3454F, -0.2095F, 0.7314F)
      );
      PartDefinition right_leg_r6 = RightLeg.addOrReplaceChild(
         "right_leg_r6",
         CubeListBuilder.create().texOffs(4, 40).mirror().addBox(-0.6F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(-1.5F, 11.5F, -2.2F, 0.0F, 0.0873F, 0.0F)
      );
      PartDefinition right_leg_r7 = RightLeg.addOrReplaceChild(
         "right_leg_r7",
         CubeListBuilder.create()
            .texOffs(24, 45)
            .mirror()
            .addBox(-0.5F, -0.5F, -1.0F, 1.0F, 1.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(-0.5F, 11.65F, -3.3F, 0.2849F, 0.274F, -0.7459F)
      );
      PartDefinition right_leg_r8 = RightLeg.addOrReplaceChild(
         "right_leg_r8",
         CubeListBuilder.create().texOffs(0, 38).mirror().addBox(-1.5F, -0.8F, -1.5F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(-0.5F, 11.2102F, -0.0776F, 0.2182F, 0.0F, 0.0F)
      );
      PartDefinition right_leg_r9 = RightLeg.addOrReplaceChild(
         "right_leg_r9",
         CubeListBuilder.create()
            .texOffs(24, 38)
            .mirror()
            .addBox(-0.5F, -1.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(-0.5F, 11.4224F, 1.7875F, -0.3622F, 0.7519F, -0.2533F)
      );
      PartDefinition right_leg_r10 = RightLeg.addOrReplaceChild(
         "right_leg_r10",
         CubeListBuilder.create()
            .texOffs(54, 9)
            .mirror()
            .addBox(-4.0F, -4.0F, -0.5F, 3.0F, 7.0F, 2.0F, new CubeDeformation(-0.25F))
            .mirror(false),
         PartPose.offsetAndRotation(2.0F, 8.5039F, 1.3272F, -0.2618F, 0.0F, 0.0F)
      );
      PartDefinition right_leg_r11 = RightLeg.addOrReplaceChild(
         "right_leg_r11",
         CubeListBuilder.create()
            .texOffs(49, 4)
            .mirror()
            .addBox(-4.0F, -0.4809F, -6.1484F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F))
            .mirror(false),
         PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, 1.1345F, 0.0F, 0.0F)
      );
      PartDefinition right_leg_r12 = RightLeg.addOrReplaceChild(
         "right_leg_r12",
         CubeListBuilder.create().texOffs(48, 3).mirror().addBox(-4.5F, -1.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false),
         PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F)
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
