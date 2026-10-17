package net.mcreator.minepiece.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import java.util.*;
import net.mcreator.minepiece.client.model.SourcePrimitiveModels;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.phys.Vec3;

/** Source Java meshes, kept outside generic Gecko metadata and all server combat code. */
public final class SourcePrimitiveVisuals {
 private static final Set<String> SEEN=new HashSet<>();
 private static final class StaticMesh extends EntityModel<EntityRenderState>{StaticMesh(ModelPart root){super(root);}}
 private final StaticMesh roots=new StaticMesh(SourcePrimitiveModels.roots().bakeRoot());
 private final StaticMesh missile=new StaticMesh(SourcePrimitiveModels.missile().bakeRoot());
 public boolean submit(String id,float factor,Vec3 direction,EntityRenderState state,PoseStack pose,SubmitNodeCollector collector){
  boolean wood=id.equals("b03_mori_rootgiant")||id.equals("b03_mori_root_branch"),ice=id.equals("b01_ice_spike"),rocket=id.equals("b08_brachio_brachiolauncher");
  if(!wood&&!ice&&!rocket)return false;
  String texture=rocket?"missile":ice?"tex_ice":"tex_wood";var model=rocket?missile:roots;
  double yaw=Math.toDegrees(Math.atan2(-direction.x,direction.z)),pitch=-Math.toDegrees(Math.atan2(direction.y,Math.hypot(direction.x,direction.z)));
  pose.pushPose();pose.rotateDegrees(Axis.YP,(float)yaw+180);
  if(rocket)pose.rotateDegrees(Axis.XP,90+(float)pitch);
  else pose.rotateDegrees(Axis.XP,ice?90:(float)pitch);
  float size=rocket?factor:ice?factor*.65F:factor*1.5F;pose.scale(size,size,size);
  pose.translate(0,1.5,0);pose.scale(-1,-1,1);
  collector.order(0).submitModel(model,state,pose,RenderTypes.entityCutout(Identifier.fromNamespaceAndPath("minepiece","textures/entities/"+texture+".png")),15728880,OverlayTexture.NO_OVERLAY,-1,null,state.outlineColor);
  pose.popPose();
  if(Boolean.getBoolean("opworldlike.visualCapture")&&SEEN.add(id))org.slf4j.LoggerFactory.getLogger("MinePieceAssets").info("[source-asset-render] {} native-java-ver15",id);
  return true;
 }
}
