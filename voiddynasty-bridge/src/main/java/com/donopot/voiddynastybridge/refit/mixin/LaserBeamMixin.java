package com.donopot.voiddynastybridge.refit.mixin;

import com.donopot.voiddynasty.weapon.LaserBeamEntity;
import com.donopot.voiddynastybridge.refit.BeamData;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(
   value = {LaserBeamEntity.class},
   remap = false
)
public abstract class LaserBeamMixin implements BeamData {
   @Shadow
   @Final
   private static EntityDataAccessor<Float> START_X;
   @Shadow
   @Final
   private static EntityDataAccessor<Float> START_Y;
   @Shadow
   @Final
   private static EntityDataAccessor<Float> START_Z;
   @Shadow
   @Final
   private static EntityDataAccessor<Float> END_X;
   @Shadow
   @Final
   private static EntityDataAccessor<Float> END_Y;
   @Shadow
   @Final
   private static EntityDataAccessor<Float> END_Z;
   @Shadow
   @Final
   private static EntityDataAccessor<Integer> COLOR;

   @Override
   public Vec3 vd$start() {
      SynchedEntityData var1 = (((Entity)(Object)this)).getEntityData();
      return new Vec3(
         (double)((Float)var1.get(START_X)).floatValue(), (double)((Float)var1.get(START_Y)).floatValue(), (double)((Float)var1.get(START_Z)).floatValue()
      );
   }

   @Override
   public Vec3 vd$end() {
      SynchedEntityData var1 = (((Entity)(Object)this)).getEntityData();
      return new Vec3(
         (double)((Float)var1.get(END_X)).floatValue(), (double)((Float)var1.get(END_Y)).floatValue(), (double)((Float)var1.get(END_Z)).floatValue()
      );
   }

   @Override
   public int vd$color() {
      return (Integer)(((Entity)(Object)this)).getEntityData().get(COLOR);
   }
}
