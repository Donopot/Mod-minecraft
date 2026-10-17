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

public class ModelhornsKaido extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(Identifier.fromNamespaceAndPath("minepiece", "horns_kaido"), "main");
   public final ModelPart Body;
   public final ModelPart Head;
   public final ModelPart RightArm;
   public final ModelPart LeftArm;
   public final ModelPart RightLeg;
   public final ModelPart LeftLeg;

   public ModelhornsKaido(ModelPart root) {
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
      PartDefinition head_r1 = Head.addOrReplaceChild(
         "head_r1",
         CubeListBuilder.create()
            .texOffs(3, 2)
            .mirror()
            .addBox(-2.2632F, 0.2555F, -1.3673F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.16F))
            .mirror(false),
         PartPose.offsetAndRotation(7.2051F, -8.7158F, -4.2296F, -0.4991F, 0.2384F, -1.0848F)
      );
      PartDefinition head_r2 = Head.addOrReplaceChild(
         "head_r2",
         CubeListBuilder.create()
            .texOffs(3, 2)
            .mirror()
            .addBox(-4.395F, 2.1702F, -1.4249F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.05F))
            .mirror(false),
         PartPose.offsetAndRotation(6.2739F, -8.7309F, -4.1989F, 0.1652F, 0.674F, -0.0423F)
      );
      PartDefinition head_r3 = Head.addOrReplaceChild(
         "head_r3",
         CubeListBuilder.create()
            .texOffs(3, 2)
            .mirror()
            .addBox(-3.4097F, 0.9496F, -1.4171F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.15F))
            .mirror(false),
         PartPose.offsetAndRotation(7.0739F, -8.7309F, -4.7989F, -0.2548F, 0.6495F, -0.7101F)
      );
      PartDefinition head_r4 = Head.addOrReplaceChild(
         "head_r4",
         CubeListBuilder.create()
            .texOffs(4, 2)
            .mirror()
            .addBox(-1.7649F, -0.2811F, -1.3726F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.26F))
            .mirror(false),
         PartPose.offsetAndRotation(7.2051F, -8.7158F, -4.2296F, -0.5476F, -0.0372F, -1.5544F)
      );
      PartDefinition head_r5 = Head.addOrReplaceChild(
         "head_r5",
         CubeListBuilder.create()
            .texOffs(3, 2)
            .mirror()
            .addBox(-0.145F, -0.9239F, -1.0017F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.45F))
            .mirror(false),
         PartPose.offsetAndRotation(7.5181F, -9.6791F, -4.4752F, 0.4687F, 0.3921F, 1.4011F)
      );
      PartDefinition head_r6 = Head.addOrReplaceChild(
         "head_r6",
         CubeListBuilder.create()
            .texOffs(3, 2)
            .mirror()
            .addBox(-1.055F, -0.924F, -1.0608F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.65F))
            .mirror(false),
         PartPose.offsetAndRotation(7.5181F, -9.3791F, -4.4752F, 0.5066F, -0.0083F, 1.4138F)
      );
      PartDefinition head_r7 = Head.addOrReplaceChild(
         "head_r7",
         CubeListBuilder.create()
            .texOffs(3, 2)
            .mirror()
            .addBox(-1.755F, -0.924F, -1.0608F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.76F))
            .mirror(false),
         PartPose.offsetAndRotation(7.5181F, -9.6791F, -4.4752F, 0.5066F, -0.0083F, 1.4138F)
      );
      PartDefinition head_r8 = Head.addOrReplaceChild(
         "head_r8",
         CubeListBuilder.create().texOffs(3, 2).addBox(0.2632F, 0.2555F, -1.3673F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.16F)),
         PartPose.offsetAndRotation(-7.2051F, -8.7158F, -4.2296F, -0.4991F, -0.2384F, 1.0848F)
      );
      PartDefinition head_r9 = Head.addOrReplaceChild(
         "head_r9",
         CubeListBuilder.create().texOffs(3, 2).addBox(0.395F, 2.1702F, -1.4249F, 4.0F, 2.0F, 2.0F, new CubeDeformation(0.05F)),
         PartPose.offsetAndRotation(-6.2739F, -8.7309F, -4.1989F, 0.1652F, -0.674F, 0.0423F)
      );
      PartDefinition head_r10 = Head.addOrReplaceChild(
         "head_r10",
         CubeListBuilder.create().texOffs(3, 2).addBox(0.4097F, 0.9496F, -1.4171F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.15F)),
         PartPose.offsetAndRotation(-7.0739F, -8.7309F, -4.7989F, -0.2548F, -0.6495F, 0.7101F)
      );
      PartDefinition head_r11 = Head.addOrReplaceChild(
         "head_r11",
         CubeListBuilder.create().texOffs(4, 2).addBox(-0.2351F, -0.2811F, -1.3726F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.26F)),
         PartPose.offsetAndRotation(-7.2051F, -8.7158F, -4.2296F, -0.5476F, 0.0372F, 1.5544F)
      );
      PartDefinition head_r12 = Head.addOrReplaceChild(
         "head_r12",
         CubeListBuilder.create().texOffs(3, 2).addBox(-1.855F, -0.9239F, -1.0017F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.45F)),
         PartPose.offsetAndRotation(-7.5181F, -9.6791F, -4.4752F, 0.4687F, -0.3921F, -1.4011F)
      );
      PartDefinition head_r13 = Head.addOrReplaceChild(
         "head_r13",
         CubeListBuilder.create().texOffs(3, 2).addBox(-0.945F, -0.924F, -1.0608F, 2.0F, 2.0F, 2.0F, new CubeDeformation(-0.65F)),
         PartPose.offsetAndRotation(-7.5181F, -9.3791F, -4.4752F, 0.5066F, 0.0083F, -1.4138F)
      );
      PartDefinition head_r14 = Head.addOrReplaceChild(
         "head_r14",
         CubeListBuilder.create().texOffs(3, 2).addBox(-1.245F, -0.924F, -1.0608F, 3.0F, 2.0F, 2.0F, new CubeDeformation(-0.76F)),
         PartPose.offsetAndRotation(-7.5181F, -9.6791F, -4.4752F, 0.5066F, 0.0083F, -1.4138F)
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
