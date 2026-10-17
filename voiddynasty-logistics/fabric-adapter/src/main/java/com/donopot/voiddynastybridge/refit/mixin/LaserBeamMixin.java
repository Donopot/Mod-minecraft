package com.donopot.voiddynastybridge.refit.mixin;
import com.donopot.voiddynastybridge.refit.BeamData;
import com.donopot.voiddynasty.weapon.LaserBeamEntity;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.*;

@Mixin(value=LaserBeamEntity.class,remap=false)
public abstract class LaserBeamMixin implements BeamData {
    @Shadow @Final private static EntityDataAccessor<Float> START_X,START_Y,START_Z,END_X,END_Y,END_Z;
    @Shadow @Final private static EntityDataAccessor<Integer> COLOR;
    @Override public Vec3 vd$start(){var d=((Entity)(Object)this).getEntityData();return new Vec3(d.get(START_X),d.get(START_Y),d.get(START_Z));}
    @Override public Vec3 vd$end(){var d=((Entity)(Object)this).getEntityData();return new Vec3(d.get(END_X),d.get(END_Y),d.get(END_Z));}
    @Override public int vd$color(){return ((Entity)(Object)this).getEntityData().get(COLOR);}
}
