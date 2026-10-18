package net.mcreator.minepiece.client.model;
import net.mcreator.minepiece.client.TransformPoses;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.*;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
/** Only the reserved cloudring2 collar and its alpha atlas, following the captured torso pose. */
public final class GearCloudCollar extends EntityModel<EntityRenderState> {
 private final ModelPart body,collar;
 public GearCloudCollar(){super(layer().bakeRoot());body=root().getChild("body");collar=body.getChild("collar");}
 private static LayerDefinition layer(){var mesh=new MeshDefinition();var body=mesh.getRoot().addOrReplaceChild("body",CubeListBuilder.create(),PartPose.ZERO);body.addOrReplaceChild("collar",CubeListBuilder.create().texOffs(34,73).addBox(-8,-6,-8,16,10,20),PartPose.offsetAndRotation(0,2,4,.48F,0,0));return LayerDefinition.create(mesh,128,128);}
 @Override public void setupAnim(EntityRenderState state){root().getAllParts().forEach(ModelPart::resetPose);var p=TransformPoses.forState(state);if(p==null)return;body.loadPose(p.body());float breathe=1+(float)Math.sin(state.ageInTicks*.09)*.035F;collar.xScale=collar.zScale=breathe;collar.y+=(float)Math.sin(state.ageInTicks*.12)*.25F;}
}
