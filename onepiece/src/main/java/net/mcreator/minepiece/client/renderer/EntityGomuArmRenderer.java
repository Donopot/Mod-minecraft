package net.mcreator.minepiece.client.renderer;

import com.geckolib.renderer.GeoEntityRenderer;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.base.GeoRenderState;
import net.mcreator.minepiece.entity.EntityGomuArmEntity;
import net.mcreator.minepiece.entity.model.EntityGomuArmModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib du bras de Gomu Gomu (côté client). Injecte la texture (luffy / armament_haki / clear)
 * dans le {@link GeoRenderState} via le {@link com.geckolib.constant.dataticket.DataTicket} du modèle.
 * NB : en 26.3, {@code EntityRenderState} implémente {@link GeoRenderState} via le mixin GeckoLib.
 */
public class EntityGomuArmRenderer extends GeoEntityRenderer<EntityGomuArmEntity, EntityRenderState> {
   private final SourcePrimitiveVisuals sourcePrimitives=new SourcePrimitiveVisuals();
   private static final com.geckolib.constant.dataticket.DataTicket<Float> THICKNESS=com.geckolib.constant.dataticket.DataTicket.create("elastic_thickness",Float.class);
   private static final java.util.Set<String> SEEN=new java.util.HashSet<>();
   private static final com.geckolib.constant.dataticket.DataTicket<Boolean> HIDE_NATIVE_CORE =
      com.geckolib.constant.dataticket.DataTicket.create("opwl_native_core_camera_inside",Boolean.class);
   private static final com.geckolib.constant.dataticket.DataTicket<Boolean> HIDE_MAGMA_CHARGE =
      com.geckolib.constant.dataticket.DataTicket.create("opwl_magma_charge_inside",Boolean.class);
   private static final com.geckolib.constant.dataticket.DataTicket<String> PROFILE =
      com.geckolib.constant.dataticket.DataTicket.create("opwl_arm_profile", String.class);
   private static final com.geckolib.constant.dataticket.DataTicket<net.minecraft.world.phys.Vec3> DIRECTION =
      com.geckolib.constant.dataticket.DataTicket.create("opwl_arm_direction", net.minecraft.world.phys.Vec3.class);

   public EntityGomuArmRenderer(EntityRendererProvider.Context context) {
      super(context, new EntityGomuArmModel());
   }

   @Override
   public void addRenderData(EntityGomuArmEntity animatable, Void ctx, EntityRenderState state, float partialTick) {
      ((GeoRenderState) state).addGeckolibData(EntityGomuArmModel.MODEL_TICKET, OriginalAssetVisuals.model(animatable.getVisualProfile(),animatable.getModel()));
      ((GeoRenderState) state).addGeckolibData(EntityGomuArmModel.TEXTURE_TICKET, OriginalAssetVisuals.texture(animatable.getVisualProfile(),animatable.getTexture()));
      ((GeoRenderState) state).addGeckolibData(EntityGomuArmModel.STRETCH_TICKET, animatable.getStretch());
      ((GeoRenderState)state).addGeckolibData(THICKNESS,animatable.getThickness());
      ((GeoRenderState) state).addGeckolibData(PROFILE, animatable.getVisualProfile());
      ((GeoRenderState) state).addGeckolibData(DIRECTION, animatable.getLookAngle());
      var client=net.minecraft.client.Minecraft.getInstance();
      String visualId=animatable.getVisualProfile();
      double coreRadius=1.35*OriginalAssetVisuals.modelScale(visualId)*animatable.getStretch();
      ((GeoRenderState)state).addGeckolibData(HIDE_NATIVE_CORE,
         OriginalAssetVisuals.additive(visualId)
         && animatable.position().distanceToSqr(client.gameRenderer.mainCamera().position())<(coreRadius+.2)*(coreRadius+.2));
      ((GeoRenderState) state).addGeckolibData(HIDE_MAGMA_CHARGE,
         ((animatable.getVisualProfile().startsWith("b07_")||animatable.getVisualProfile().startsWith("b08_"))&&client.player!=null&&animatable.position().distanceTo(client.player.getEyePosition())<.8 || java.util.Set.of("b05_soru_hebunrifire","b03_mochi_zangirimochi","b03_mochi_impact","b01_smoke_finish","sables-native","goro_charge","goro_raigo_charge","magu_charge","magu_trail","magu_fist_charge","magu_hound_charge","magu_dash","magu_meteor_charge").contains(animatable.getVisualProfile()))
         && client.player!=null && client.options.getCameraType().isFirstPerson() && animatable.distanceTo(client.player)<4);
   }

   @Override
   public net.minecraft.client.renderer.rendertype.RenderType getRenderType(EntityRenderState state,net.minecraft.resources.Identifier texture){
      String id=((GeoRenderState)state).getOrDefaultGeckolibData(PROFILE,"");
      // Source EntityImpactWaveLayer uses eyes: additive light, not opaque cutout faces.
      return OriginalAssetVisuals.additive(id)?net.minecraft.client.renderer.rendertype.RenderTypes.eyes(texture):super.getRenderType(state,texture);
   }

   @Override
   public void submit(EntityRenderState state, com.mojang.blaze3d.vertex.PoseStack stack,
                      net.minecraft.client.renderer.SubmitNodeCollector collector,
                      net.minecraft.client.renderer.state.level.CameraRenderState camera) {
      GeoRenderState geo = (GeoRenderState) state;
      String id = geo.getOrDefaultGeckolibData(PROFILE, "");
      net.mcreator.minepiece.client.Batch01VisualLab.rendered(id);
      if(geo.getOrDefaultGeckolibData(HIDE_MAGMA_CHARGE,false)) return;
      net.mcreator.minepiece.client.Batch07VisualLab.rendered(id);net.mcreator.minepiece.client.Batch08VisualLab.rendered(id);
      net.mcreator.minepiece.client.Batch02VisualLab.rendered(id);
      net.mcreator.minepiece.client.Batch05VisualLab.rendered(id);
      net.mcreator.minepiece.client.Batch06VisualLab.rendered(id);
      net.mcreator.minepiece.client.Batch04VisualLab.rendered(id);
      net.mcreator.minepiece.client.Batch03VisualLab.rendered(id);
      if(sourcePrimitives.submit(id,geo.getOrDefaultGeckolibData(EntityGomuArmModel.STRETCH_TICKET,1F),geo.getOrDefaultGeckolibData(DIRECTION,net.minecraft.world.phys.Vec3.ZERO),state,stack,collector))return;
      if(OriginalAssetVisuals.nativeMesh(id)){
         if(!geo.getOrDefaultGeckolibData(HIDE_NATIVE_CORE,false)){
            super.submit(state,stack,collector,camera);
            if(Boolean.getBoolean("opworldlike.visualCapture")&&SEEN.add("source:"+id))org.slf4j.LoggerFactory.getLogger("MinePieceAssets").info("[source-asset-render] {} native-ver15",id);
         }
         if(!OriginalAssetVisuals.additive(id))return;
      }
      // Only the transparent White Launcher melee visual uses procedural smoke.
      if ((id.startsWith("b08_") || id.startsWith("b07_") || id.startsWith("b06_") || id.startsWith("b05_") || id.startsWith("b04_") || id.startsWith("b03_")) && TechniqueVisualProfile.forTechnique(id).replacesLegacyModel() || id.startsWith("b02_") || id.startsWith("b01_") || "whitelauncher".equals(id) || (net.mcreator.minepiece.procedures.MeraTechniques.ownsVisual(id) || net.mcreator.minepiece.procedures.MaguTechniques.ownsVisual(id) || net.mcreator.minepiece.procedures.GoroTechniques.ownsVisual(id))) {
         var profile=TechniqueVisualProfile.forTechnique(id);
         if(Boolean.getBoolean("opworldlike.visualCapture") && SEEN.add(id)) org.slf4j.LoggerFactory.getLogger("MeraRenderer").info("[mera-render] {} {}",id,profile.kind());
         float stretch=geo.getOrDefaultGeckolibData(EntityGomuArmModel.STRETCH_TICKET,1F);
         if((id.startsWith("b08_") || id.startsWith("b07_"))&&!id.endsWith("charge"))profile=new TechniqueVisualProfile(profile.kind(),profile.color(),stretch);
         if(id.equals("hibashira") || id.equals("mera_fireflies") || id.equals("magu_eruption") || id.equals("magu_wake") || id.equals("magu_volcanic")) profile=new TechniqueVisualProfile(profile.kind(),profile.color(),stretch);
         if(id.equals("mera_charge_ball")) profile=new TechniqueVisualProfile(profile.kind(),profile.color(),.2F+stretch*2);
         var direction=geo.getOrDefaultGeckolibData(DIRECTION,net.minecraft.world.phys.Vec3.ZERO);
         if(net.mcreator.minepiece.procedures.GoroTechniques.ownsVisual(id) && !id.equals("goro_charge") && !id.equals("goro_cloud")) profile=new TechniqueVisualProfile(profile.kind(),profile.color(),stretch);
         if(id.startsWith("b01_") && !id.endsWith("charge") && (!id.endsWith("dash") || id.equals("b01_light_dash")))profile=new TechniqueVisualProfile(profile.kind(),profile.color(),stretch);
         if(id.startsWith("b03_") && !id.endsWith("charge"))profile=new TechniqueVisualProfile(profile.kind(),profile.color(),id.startsWith("b03_kira_")?profile.radius()*stretch:stretch);
         if(id.startsWith("b05_") && !id.endsWith("charge"))profile=new TechniqueVisualProfile(profile.kind(),profile.color(),stretch);
         if(id.startsWith("b04_") && !id.endsWith("charge"))profile=new TechniqueVisualProfile(profile.kind(),profile.color(),stretch);
         if(id.startsWith("b02_") && !id.endsWith("charge"))profile=new TechniqueVisualProfile(profile.kind(),profile.color(),stretch);
         if(id.equals("kaenryuo")) direction=direction.scale(stretch);
         TechniqueEffectGeometry.submit(profile,direction,state.ageInTicks,stack,collector);
      } else super.submit(state, stack, collector, camera);
   }

   @Override
   protected net.minecraft.world.phys.AABB getBoundingBoxForCulling(EntityGomuArmEntity entity,float partialTick) {
      return (entity.getVisualProfile().startsWith("b08_") || entity.getVisualProfile().startsWith("b07_") || entity.getVisualProfile().startsWith("b06_") || entity.getVisualProfile().startsWith("b05_") || entity.getVisualProfile().startsWith("b04_") || entity.getVisualProfile().startsWith("b03_") || entity.getVisualProfile().startsWith("b02_") || entity.getVisualProfile().startsWith("b01_") || net.mcreator.minepiece.procedures.MeraTechniques.ownsVisual(entity.getVisualProfile()) || net.mcreator.minepiece.procedures.GoroTechniques.ownsVisual(entity.getVisualProfile()) || net.mcreator.minepiece.procedures.MaguTechniques.ownsVisual(entity.getVisualProfile()))
         ? entity.getBoundingBox().inflate(32) : super.getBoundingBoxForCulling(entity,partialTick);
   }

   /**
    * Étire le bras jusqu'à son point d'impact (FRUIT-GOMU_GOMU-03).
    *
    * <p>Le modèle porte le poing à 2 blocs par défaut (débattement de l'animation). Le facteur
    * d'étirement, calculé côté serveur à partir de la distance réelle, allonge le bras sur son axe
    * d'avancée : le poing se trouve donc exactement là où les dégâts sont appliqués, quelle que soit
    * la portée — et l'arrêt sur un mur se voit, puisque la distance transmise est plus courte.</p>
    */
   @Override
   public void scaleModelForRender(RenderPassInfo<EntityRenderState> info, float widthScale, float heightScale) {
      float thickness=info.getOrDefaultGeckolibData(THICKNESS,1F);info.poseStack().scale(thickness,thickness,1);
      Float stretch = ((GeoRenderState) info.renderState()).getGeckolibData(EntityGomuArmModel.STRETCH_TICKET);
      String profile=info.getOrDefaultGeckolibData(PROFILE,"");
      if(OriginalAssetVisuals.nativeMesh(profile)){
         OriginalAssetVisuals.transform(info.poseStack(),profile,true,stretch==null?1:stretch);
         super.scaleModelForRender(info,widthScale,heightScale);return;
      }
      if (stretch != null && stretch > 0.0F && !((profile.startsWith("b08_") || profile.startsWith("b07_") || profile.startsWith("b06_") || profile.startsWith("b05_") || profile.startsWith("b04_") || profile.startsWith("b03_")) && TechniqueVisualProfile.forTechnique(profile).replacesLegacyModel())) {
         info.poseStack().scale(1.0F, 1.0F, stretch);
      }
      super.scaleModelForRender(info, widthScale, heightScale);
   }
}
