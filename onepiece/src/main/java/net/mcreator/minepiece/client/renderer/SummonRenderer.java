package net.mcreator.minepiece.client.renderer;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.GeoRenderState;
import net.mcreator.minepiece.entity.SummonEntity;
import net.mcreator.minepiece.entity.model.SummonModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib de l'invocation (CORE-021). Injecte modèle/texture via les {@link DataTicket} du modèle.
 */
public class SummonRenderer extends GeoEntityRenderer<SummonEntity, EntityRenderState> {
   private static final java.util.Set<String> CAMERA_SEEN=new java.util.HashSet<>();
   private static final com.geckolib.constant.dataticket.DataTicket<String> LOGICAL_MODEL=com.geckolib.constant.dataticket.DataTicket.create("source_summon_logical_model",String.class);
   private static final com.geckolib.constant.dataticket.DataTicket<Boolean> CAMERA_INSIDE=com.geckolib.constant.dataticket.DataTicket.create("source_summon_camera_inside",Boolean.class);
   private static final com.geckolib.constant.dataticket.DataTicket<Float> B04_SCALE=com.geckolib.constant.dataticket.DataTicket.create("b04_summon_scale",Float.class);

   public SummonRenderer(EntityRendererProvider.Context context) {
      super(context, new SummonModel());
   }

   @Override
   public void submit(EntityRenderState state,com.mojang.blaze3d.vertex.PoseStack stack,net.minecraft.client.renderer.SubmitNodeCollector collector,net.minecraft.client.renderer.state.level.CameraRenderState camera){
      if(((GeoRenderState)state).getOrDefaultGeckolibData(CAMERA_INSIDE,false))return;
      net.mcreator.minepiece.client.Batch04VisualLab.rendered("summon-"+((GeoRenderState)state).getOrDefaultGeckolibData(LOGICAL_MODEL,""));
      net.mcreator.minepiece.client.Batch05VisualLab.rendered("summon-"+((GeoRenderState)state).getOrDefaultGeckolibData(LOGICAL_MODEL,""));
      super.submit(state,stack,collector,camera);
   }

   @Override
   public void addRenderData(SummonEntity animatable, Void ctx, EntityRenderState state,float partialTick) {
      ((GeoRenderState)state).addGeckolibData(LOGICAL_MODEL,animatable.getModel());
      var client=net.minecraft.client.Minecraft.getInstance();
      boolean own=client.player!=null&&animatable.getVisualOwnerId().equals(client.player.getUUID().toString());
      boolean hidden=own&&client.options.getCameraType().isFirstPerson()&&animatable.position().add(0,1,0).distanceToSqr(client.gameRenderer.mainCamera().position())<2.5*2.5;
      ((GeoRenderState)state).addGeckolibData(CAMERA_INSIDE,hidden);
      if(Boolean.getBoolean("opworldlike.visualCapture")&&client.player!=null&&!animatable.getVisualOwnerId().isEmpty()&&CAMERA_SEEN.add(animatable.getModel()+":"+hidden))
         org.slf4j.LoggerFactory.getLogger("MinePieceAssets").info("[source-summon-camera] {} owner={} viewer={} hidden={}",animatable.getModel(),animatable.getVisualOwnerId(),client.player.getUUID(),hidden);
      ((GeoRenderState) state).addGeckolibData(SummonModel.MODEL_TICKET, animatable.getModel().equals("gecko_moria")?"biped":animatable.getModel());
      ((GeoRenderState) state).addGeckolibData(SummonModel.TEXTURE_TICKET, ((net.mcreator.minepiece.procedures.ParameciaStoneData)animatable).minepiece$stoneFlag()?"tex_stone":animatable.getModel().equals("gecko_moria")?"tex_black":animatable.getTexture());
      ((GeoRenderState) state).addGeckolibData(B04_SCALE,animatable.getModel().equals("biscuit_warrrior")?.42F:animatable.getModel().equals("kumac")?.5F:animatable.getModel().equals("tot_musica_1")?.3F:animatable.getModel().startsWith("note_warrior")?.5F:animatable.getModel().equals("zeus")?.8F:animatable.getModel().equals("prometheus")?.6F:1F);
   }
   @Override
   public void scaleModelForRender(com.geckolib.renderer.base.RenderPassInfo<EntityRenderState> info,float width,float height){
      float scale=info.getOrDefaultGeckolibData(B04_SCALE,1F);info.poseStack().scale(scale,scale,scale);super.scaleModelForRender(info,width,height);
   }
}
