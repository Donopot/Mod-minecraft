package com.donopot.voiddynastybridge.refit;

import com.donopot.voiddynasty.weapon.LaserBeamEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.PoseStack.Pose;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider.Context;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class FixedLaserRenderer extends EntityRenderer<LaserBeamEntity, FixedLaserRenderer.State> {
   public FixedLaserRenderer(Context var1) {
      super(var1);
   }

   public FixedLaserRenderer.State createRenderState() {
      return new FixedLaserRenderer.State();
   }

   public void extractRenderState(LaserBeamEntity var1, FixedLaserRenderer.State var2, float var3) {
      super.extractRenderState(var1, var2, var3);
      BeamData var4 = (BeamData)var1;
      var2.start = var4.vd$start().subtract(var1.position());
      var2.end = var4.vd$end().subtract(var1.position());
      var2.color = var4.vd$color();
   }

   protected AABB getBoundingBoxForCulling(LaserBeamEntity var1, float var2) {
      BeamData var3 = (BeamData)var1;
      return new AABB(var3.vd$start(), var3.vd$end()).inflate(0.2);
   }

   public void submit(FixedLaserRenderer.State var1, PoseStack var2, SubmitNodeCollector var3, CameraRenderState var4) {
      Vec3 var5 = var1.start;
      Vec3 var6 = var1.end;
      Vec3 var7 = var6.subtract(var5);
      double var8 = var7.length();
      if (!(var8 < 0.001) && !(var8 > 513.0)) {
         Vec3 var10 = var7.scale(1.0 / var8);
         Vec3 var11 = var5.add(var10.scale(Math.min(0.25, var8 * 0.15)));
         Vec3 var12 = Math.abs(var10.y) > 0.95 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
         Vec3 var13 = var10.cross(var12).normalize();
         Vec3 var14 = var10.cross(var13).normalize();
         int var15 = var1.color;
         var3.submitCustomGeometry(var2, RenderTypes.lightning(), (var5x, var6x) -> {
            tube(var6x, var5x, var11, var6, var13.scale(0.055), var14.scale(0.055), var15 & 16777215 | -2013265920);
            tube(var6x, var5x, var11, var6, var13.scale(0.018), var14.scale(0.018), -3620);
         });
      }
   }

   private static void tube(VertexConsumer var0, Pose var1, Vec3 var2, Vec3 var3, Vec3 var4, Vec3 var5, int var6) {
      Vec3[] var7 = new Vec3[]{
         var2.add(var4).add(var5),
         var2.subtract(var4).add(var5),
         var2.subtract(var4).subtract(var5),
         var2.add(var4).subtract(var5),
         var3.add(var4).add(var5),
         var3.subtract(var4).add(var5),
         var3.subtract(var4).subtract(var5),
         var3.add(var4).subtract(var5)
      };

      for (int var8 = 0; var8 < 4; var8++) {
         int var9 = (var8 + 1) % 4;

         for (int var13 : new int[]{var8, var9, var9 + 4, var8 + 4}) {
            Vec3 var14 = var7[var13];
            var0.addVertex(var1, (float)var14.x, (float)var14.y, (float)var14.z).setColor(var6);
         }
      }
   }

   public static final class State extends EntityRenderState {
      Vec3 start = Vec3.ZERO;
      Vec3 end = Vec3.ZERO;
      int color;
   }
}
