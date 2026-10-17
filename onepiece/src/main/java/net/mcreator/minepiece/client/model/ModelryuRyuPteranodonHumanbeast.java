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

public class ModelryuRyuPteranodonHumanbeast extends EntityModel<EntityRenderState> {
   public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(
      Identifier.fromNamespaceAndPath("minepiece", "ryu_ryu_pteranodon_humanbeast"), "main"
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

   public ModelryuRyuPteranodonHumanbeast(ModelPart root) {
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
      PartDefinition head_r1 = Head.addOrReplaceChild(
         "head_r1",
         CubeListBuilder.create().texOffs(38, -5).addBox(0.0F, -5.6F, -4.1F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)),
         PartPose.offsetAndRotation(0.0F, -10.9393F, 1.0607F, -1.1781F, 0.0F, 0.0F)
      );
      PartDefinition Body = partdefinition.addOrReplaceChild("Body", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));
      PartDefinition tail = Body.addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 8.0F, 2.0F, 0.2618F, 0.0F, 0.0F));
      PartDefinition bone = tail.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 15.2319F, -2.3351F, -0.3927F, 0.0F, 0.0F));
      PartDefinition bone3 = bone.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 8.2232F, 2.3352F, -0.3491F, 0.0F, 0.0F));
      PartDefinition bone2 = bone3.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 6.9284F, 4.0002F, -0.3491F, 0.0F, 0.0F));
      PartDefinition RightArm = partdefinition.addOrReplaceChild("RightArm", CubeListBuilder.create(), PartPose.offset(-5.0F, 2.0F, 0.0F));
      PartDefinition RightWing = RightArm.addOrReplaceChild(
         "RightWing",
         CubeListBuilder.create()
            .texOffs(48, 89)
            .addBox(-15.0F, -10.0F, 0.0F, 16.0F, 24.0F, 0.0F, new CubeDeformation(0.2F))
            .texOffs(48, 25)
            .addBox(-15.0F, -10.0F, -0.4F, 16.0F, 24.0F, 0.0F, new CubeDeformation(0.2F)),
         PartPose.offsetAndRotation(1.0F, 0.0F, 0.0F, 0.0F, 0.1745F, 0.0F)
      );
      PartDefinition RightWing2 = RightWing.addOrReplaceChild(
         "RightWing2",
         CubeListBuilder.create()
            .texOffs(36, 65)
            .addBox(-28.0F, -8.0F, 0.0F, 28.0F, 24.0F, 0.0F, new CubeDeformation(0.2F))
            .texOffs(36, 1)
            .addBox(-28.0F, -8.0F, -0.4F, 28.0F, 24.0F, 0.0F, new CubeDeformation(0.2F)),
         PartPose.offsetAndRotation(-15.1414F, -2.0F, -0.1414F, 0.0F, -0.3491F, 0.0F)
      );
      PartDefinition LeftArm = partdefinition.addOrReplaceChild("LeftArm", CubeListBuilder.create(), PartPose.offset(5.0F, 2.0F, 0.0F));
      PartDefinition LeftWing = LeftArm.addOrReplaceChild(
         "LeftWing",
         CubeListBuilder.create()
            .texOffs(48, 89)
            .mirror()
            .addBox(-1.0F, -10.0F, 0.0F, 16.0F, 24.0F, 0.0F, new CubeDeformation(0.2F))
            .mirror(false)
            .texOffs(48, 25)
            .mirror()
            .addBox(-1.0F, -10.0F, -0.4F, 16.0F, 24.0F, 0.0F, new CubeDeformation(0.2F))
            .mirror(false),
         PartPose.offsetAndRotation(-1.0F, 0.0F, 0.0F, 0.0F, -0.1745F, 0.0F)
      );
      PartDefinition LeftWing2 = LeftWing.addOrReplaceChild(
         "LeftWing2",
         CubeListBuilder.create()
            .texOffs(36, 65)
            .mirror()
            .addBox(0.0F, -8.0F, 0.0F, 28.0F, 24.0F, 0.0F, new CubeDeformation(0.2F))
            .mirror(false)
            .texOffs(36, 1)
            .mirror()
            .addBox(0.0F, -8.0F, -0.4F, 28.0F, 24.0F, 0.0F, new CubeDeformation(0.2F))
            .mirror(false),
         PartPose.offsetAndRotation(15.1414F, -2.0F, -0.1414F, 0.0F, 0.3491F, 0.0F)
      );
      PartDefinition RightLeg = partdefinition.addOrReplaceChild("RightLeg", CubeListBuilder.create(), PartPose.offset(-2.0F, 12.0F, 0.0F));
      PartDefinition LeftLeg = partdefinition.addOrReplaceChild("LeftLeg", CubeListBuilder.create(), PartPose.offset(2.0F, 12.0F, 0.0F));
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
