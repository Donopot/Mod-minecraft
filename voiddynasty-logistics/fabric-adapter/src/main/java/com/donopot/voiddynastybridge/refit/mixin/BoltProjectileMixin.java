package com.donopot.voiddynastybridge.refit.mixin;
import com.donopot.voiddynastybridge.refit.ImpactEffects;
import com.donopot.voiddynasty.weapon.*;
import net.minecraft.core.particles.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value=BoltProjectile.class,remap=false)
public abstract class BoltProjectileMixin {
    @Shadow private WeaponDefinition def;
    @Redirect(method="impact",at=@At(value="INVOKE",target="Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    private int vd$impact(ServerLevel level,ParticleOptions particle,double x,double y,double z,int count,double dx,double dy,double dz,double speed){
        return ImpactEffects.send(level,particle,x,y,z,count,dx,dy,dz,speed,def==null?"":def.family());
    }
    @Inject(method="tick",at=@At("RETURN"))
    private void vd$plasma(CallbackInfo ci){
        var e=(Entity)(Object)this;
        if(!e.level().isClientSide()||e.isRemoved()||def==null||!def.family().equals("plasma"))return;
        var p=e.position();var v=e.getDeltaMovement();int steps=Math.clamp((int)Math.ceil(v.length()/.18),1,24);
        var glow=new DustParticleOptions(0xFF43CFFF,2.2f);var core=new DustParticleOptions(0xFFDEFFFF,1.25f);
        for(int i=0;i<steps;i++){var q=p.subtract(v.scale((double)i/steps));e.level().addParticle(glow,q.x,q.y,q.z,0,0,0);if(i%2==0)e.level().addParticle(core,q.x,q.y,q.z,0,0,0);}
    }
}
