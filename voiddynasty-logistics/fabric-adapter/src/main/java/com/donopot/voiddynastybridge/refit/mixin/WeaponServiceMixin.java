package com.donopot.voiddynastybridge.refit.mixin;
import com.donopot.voiddynastybridge.refit.*;
import com.donopot.voiddynasty.weapon.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=WeaponService.class,remap=false)
public abstract class WeaponServiceMixin {
    @Inject(method="hitscanShot",at=@At("HEAD"),cancellable=true)
    private static void vd$laser(ServerPlayer player,ServerLevel level,WeaponDefinition def,Vec3 start,Vec3 direction,CallbackInfo ci){
        if(def.family().equals("las")){LaserShot.fire(player,level,def,start,direction);ci.cancel();}
    }
    @Redirect(method="explode",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    private static int vd$impact(ServerLevel level,ParticleOptions particle,double x,double y,double z,int count,double dx,double dy,double dz,double speed,
                                ServerLevel originalLevel,Vec3 center,float radius,float damage,Entity owner,String family){
        return ImpactEffects.send(level,particle,x,y,z,count,dx,dy,dz,speed,family);
    }
}
