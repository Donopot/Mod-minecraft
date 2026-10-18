package net.mcreator.minepiece.client.renderer;

import com.geckolib.constant.DataTickets;
import com.geckolib.renderer.base.GeoRenderState;
import com.geckolib.renderer.base.RenderPassInfo;
import com.geckolib.renderer.specialty.DirectionalProjectileRenderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.mcreator.minepiece.entity.SkillProjectileEntity;
import net.mcreator.minepiece.entity.model.SkillProjectileModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/**
 * Renderer GeckoLib du projectile skill, basé sur {@link DirectionalProjectileRenderer}. Le modèle
 * (poing, slash…) pointe vers +Z dans son espace local, alors que le renderer GeckoLib applique un
 * décalage +180° (convention -Z) et un pitch non inversé : on corrige les deux pour que le projectile
 * soit bien orienté le long de son vecteur de déplacement. Injecte aussi modèle + texture + échelle.
 */
public class SkillProjectileRenderer extends DirectionalProjectileRenderer<SkillProjectileEntity, EntityRenderState> {
   private final SourcePrimitiveVisuals sourcePrimitives=new SourcePrimitiveVisuals();

   private static final java.util.Set<String> SEEN=new java.util.HashSet<>();
   private static final com.geckolib.constant.dataticket.DataTicket<Boolean> VISUAL_READY =
      com.geckolib.constant.dataticket.DataTicket.create("opwl_projectile_ready",Boolean.class);
   private static final com.geckolib.constant.dataticket.DataTicket<Boolean> HIDE_NEAR_CAMERA =
      com.geckolib.constant.dataticket.DataTicket.create("opwl_magma_near_camera",Boolean.class);
   private static final com.geckolib.constant.dataticket.DataTicket<String> PROFILE =
      com.geckolib.constant.dataticket.DataTicket.create("opwl_visual_profile", String.class);
   private static final com.geckolib.constant.dataticket.DataTicket<net.minecraft.world.phys.Vec3> VELOCITY =
      com.geckolib.constant.dataticket.DataTicket.create("opwl_visual_velocity", net.minecraft.world.phys.Vec3.class);

   public SkillProjectileRenderer(EntityRendererProvider.Context context) {
      super(context, new SkillProjectileModel());
   }

   @Override
   public void addRenderData(SkillProjectileEntity animatable, Void ctx, EntityRenderState state, float partialTick) {
      ((GeoRenderState) state).addGeckolibData(SkillProjectileModel.MODEL_TICKET, OriginalAssetVisuals.model(animatable.getVisualProfile(),animatable.getModel()));
      ((GeoRenderState) state).addGeckolibData(SkillProjectileModel.TEXTURE_TICKET, OriginalAssetVisuals.texture(animatable.getVisualProfile(),animatable.getTexture(),animatable.getId()));
      ((GeoRenderState) state).addGeckolibData(SkillProjectileModel.SCALE_TICKET, animatable.getScale());
      ((GeoRenderState) state).addGeckolibData(PROFILE, animatable.getVisualProfile());
      ((GeoRenderState) state).addGeckolibData(VELOCITY, animatable.getDeltaMovement());
      ((GeoRenderState) state).addGeckolibData(VISUAL_READY,animatable.getEntityData().get(SkillProjectileEntity.VISUAL_READY));
      var client=net.minecraft.client.Minecraft.getInstance();
      double meraClearance=net.mcreator.minepiece.client.EffectCameraVisibility.meraMeshClearance(animatable.getVisualProfile(),animatable.getScale());
      boolean pendingProfile=animatable.getVisualProfile().isEmpty();
      if(pendingProfile)meraClearance=net.mcreator.minepiece.client.EffectCameraVisibility.pendingMeshClearance(animatable.getScale());
      boolean nativeCameraInside=meraClearance>0 && animatable.getPosition(partialTick)
         .distanceToSqr(client.gameRenderer.mainCamera().position())<meraClearance*meraClearance;
      double elementalClearance=net.mcreator.minepiece.client.EffectCameraVisibility.elementalMeshClearance(animatable.getVisualProfile(),animatable.getScale());
      double lensDistance=animatable.getPosition(partialTick).distanceTo(client.gameRenderer.mainCamera().position());
      nativeCameraInside|=elementalClearance>0&&lensDistance<elementalClearance;
      // Use the actual camera origin; feet distance lets near eye-level meshes fill the view.
      if(client.options.getCameraType().isFirstPerson()){
         double clearance=switch(animatable.getVisualProfile()){
            case "b04_horo_negativehollow"->2.1;
            case "b02_rubble"->3.2*animatable.getScale();
            default->0;
         };
         nativeCameraInside|=clearance>0&&animatable.getPosition(partialTick).distanceToSqr(client.gameRenderer.mainCamera().position())<clearance*clearance;
      }
      if(animatable.getVisualProfile().equals("daienkai")||animatable.getVisualProfile().equals("goro_raigo")){
          double radius=(animatable.getVisualProfile().equals("daienkai")?2.6:2.1)*animatable.getScale();
         nativeCameraInside|=animatable.position().add(0,animatable.getBbHeight()*.5,0).distanceToSqr(client.gameRenderer.mainCamera().position())<radius*radius;
      }
      boolean inside=(animatable.getVisualProfile().equals("goro_raigo") || net.mcreator.minepiece.procedures.MaguTechniques.ownsVisual(animatable.getVisualProfile()))
         && client.player!=null && client.options.getCameraType().isFirstPerson()
         && animatable.distanceTo(client.player)<(animatable.getVisualProfile().equals("inugamiguren")?8:3);
      if(client.player!=null && client.options.getCameraType().isFirstPerson()){
         double clearance=switch(animatable.getVisualProfile()){case "b04_aro_arrow"->1.8;case "b04_horo_negativehollow"->2;case "b04_horo_minihollow"->1.5;case "b04_horo_tokuhoro"->4;case "b03_riki_stonethrowinglarge","b03_kira_diamondavalanche"->8;case "b03_riki_stonethrowing"->4;case "b03_mochi_yakimochi"->3.5;case "b03_mochi_ringfist"->1.8;case "b02_rubble"->3;case "b02_stoneopeope"->2.5;case "darkball","whiteblow","magatama"->2;case "darkmatter"->6;case "pheasantbeak"->4;case "icepartisan","whitevine"->2.5;default->0;};
         inside |= clearance>0 && animatable.distanceTo(client.player)<clearance;
      }
      ((GeoRenderState) state).addGeckolibData(HIDE_NEAR_CAMERA,inside||nativeCameraInside);
      net.mcreator.minepiece.client.Batch01VisualLab.cameraMesh(animatable.getVisualProfile(),inside||nativeCameraInside,lensDistance);
      net.mcreator.minepiece.client.MeraVisualLab.cameraMesh(pendingProfile?"pending-profile":animatable.getVisualProfile(),inside||nativeCameraInside);
   }

   @Override
   protected net.minecraft.world.phys.AABB getBoundingBoxForCulling(SkillProjectileEntity entity, float partialTick) {
      return (net.mcreator.minepiece.procedures.MaguTechniques.ownsVisual(entity.getVisualProfile()) || TechniqueVisualProfile.forTechnique(entity.getVisualProfile()).replacesLegacyModel())
         ? entity.getBoundingBox().inflate(7) : super.getBoundingBoxForCulling(entity, partialTick);
   }

   @Override
   public void submit(EntityRenderState state, PoseStack stack, net.minecraft.client.renderer.SubmitNodeCollector collector,
                      net.minecraft.client.renderer.state.level.CameraRenderState camera) {
      GeoRenderState geo = (GeoRenderState) state;
      // A spawn packet can render before model/scale metadata: never flash the default giant meteor.
      if(!geo.getOrDefaultGeckolibData(VISUAL_READY,false) || geo.getOrDefaultGeckolibData(HIDE_NEAR_CAMERA,false)) return;
      net.mcreator.minepiece.client.Batch05VisualLab.rendered(geo.getOrDefaultGeckolibData(PROFILE,""));
      net.mcreator.minepiece.client.Batch06VisualLab.rendered(geo.getOrDefaultGeckolibData(PROFILE,""));
      net.mcreator.minepiece.client.Batch04VisualLab.rendered(geo.getOrDefaultGeckolibData(PROFILE,""));
      net.mcreator.minepiece.client.Batch03VisualLab.rendered(geo.getOrDefaultGeckolibData(PROFILE,""));
      net.mcreator.minepiece.client.Batch02VisualLab.rendered(geo.getOrDefaultGeckolibData(PROFILE,""));
      net.mcreator.minepiece.client.Batch01VisualLab.rendered(geo.getOrDefaultGeckolibData(PROFILE,""));
      String primitiveId=geo.getOrDefaultGeckolibData(PROFILE,"");
      if(sourcePrimitives.submit(primitiveId,geo.getOrDefaultGeckolibData(SkillProjectileModel.SCALE_TICKET,1F),geo.getOrDefaultGeckolibData(VELOCITY,net.minecraft.world.phys.Vec3.ZERO),state,stack,collector)){
         net.mcreator.minepiece.client.Batch07VisualLab.rendered(primitiveId);net.mcreator.minepiece.client.Batch08VisualLab.rendered(primitiveId);return;
      }
      var profile = TechniqueVisualProfile.forTechnique(geo.getOrDefaultGeckolibData(PROFILE, ""));
      String sourceProfile=geo.getOrDefaultGeckolibData(PROFILE, "");
      if(OriginalAssetVisuals.nativeMesh(sourceProfile)){
         super.submit(state,stack,collector,camera);
         net.mcreator.minepiece.client.Batch07VisualLab.rendered(sourceProfile);
         net.mcreator.minepiece.client.Batch08VisualLab.rendered(sourceProfile);
         if(Boolean.getBoolean("opworldlike.visualCapture")&&SEEN.add("source:"+sourceProfile))org.slf4j.LoggerFactory.getLogger("MinePieceAssets").info("[source-asset-render] {} native-ver15",sourceProfile);
      } else if (profile.replacesLegacyModel()) {
         String id=geo.getOrDefaultGeckolibData(PROFILE, "");
      net.mcreator.minepiece.client.Batch07VisualLab.rendered(id);net.mcreator.minepiece.client.Batch08VisualLab.rendered(id);
         if(Boolean.getBoolean("opworldlike.visualCapture") && SEEN.add(id)) org.slf4j.LoggerFactory.getLogger("MeraRenderer").info("[mera-render] {} {}",id,profile.kind());
         if(id.equals("hiken") || id.equals("daienkai")) profile=new TechniqueVisualProfile(profile.kind(),profile.color(),profile.radius()*geo.getOrDefaultGeckolibData(SkillProjectileModel.SCALE_TICKET,1F));
         TechniqueEffectGeometry.submit(profile, geo.getOrDefaultGeckolibData(VELOCITY, net.minecraft.world.phys.Vec3.ZERO),
            state.ageInTicks, stack, collector);
      } else {
         // Omit a mesh containing the first-person camera, including before its owner packet arrives.
         if(!geo.getOrDefaultGeckolibData(HIDE_NEAR_CAMERA,false)) super.submit(state, stack, collector, camera);
         String id=geo.getOrDefaultGeckolibData(PROFILE, "");
         if(net.mcreator.minepiece.procedures.MaguTechniques.ownsVisual(id) && !geo.getOrDefaultGeckolibData(HIDE_NEAR_CAMERA,false)) {
            if(Boolean.getBoolean("opworldlike.visualCapture") && SEEN.add(id)) org.slf4j.LoggerFactory.getLogger("MaguRenderer").info("[magu-render] {} native-model",id);
            TechniqueEffectGeometry.submit(new TechniqueVisualProfile(TechniqueVisualProfile.Kind.MAGMA_TRAIL,0xFFBA2708,.16F),geo.getOrDefaultGeckolibData(VELOCITY,net.minecraft.world.phys.Vec3.ZERO),state.ageInTicks,stack,collector);
         }
      }
   }

   /** Retire le +180° du renderer GeckoLib (le modèle pointe +Z, pas -Z). */
   @Override
   protected float calculateYRot(SkillProjectileEntity entity, float ignored, float partialTick) {
      return entity.getViewYRot(partialTick)+(entity.getVisualProfile().equals("inugamiguren")?180:OriginalAssetVisuals.yawOffset(entity.getVisualProfile()));
   }

   /** Applique yaw sans offset + pitch inversé (le xRot de {@code shoot} est inversé vs convention). */
   @Override
   protected void applyRotations(RenderPassInfo<EntityRenderState> passInfo, PoseStack poseStack, float partialTick) {
      float yaw = passInfo.getOrDefaultGeckolibData(DataTickets.ENTITY_YAW, 0.0F);
      float pitch = passInfo.getOrDefaultGeckolibData(DataTickets.ENTITY_PITCH, 0.0F);
      poseStack.rotateDegrees(Axis.YP, yaw);
      poseStack.rotateDegrees(Axis.XP, -pitch);
   }

   @Override
   public void scaleModelForRender(RenderPassInfo<EntityRenderState> passInfo, float partialTick, float ageInTicks) {
      float scale = passInfo.getOrDefaultGeckolibData(SkillProjectileModel.SCALE_TICKET, 1.0F);
      if (scale != 1.0F) {
         passInfo.poseStack().scale(scale, scale, scale);
      }
      String id=passInfo.getOrDefaultGeckolibData(PROFILE, "");
      OriginalAssetVisuals.transform(passInfo.poseStack(),id,false,1);
      super.scaleModelForRender(passInfo, partialTick, ageInTicks);
   }
}
