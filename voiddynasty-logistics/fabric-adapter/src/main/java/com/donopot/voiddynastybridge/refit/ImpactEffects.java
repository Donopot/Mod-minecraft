package com.donopot.voiddynastybridge.refit;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.ServerLevel;
public final class ImpactEffects {
    private ImpactEffects(){}
    public static int send(ServerLevel level,ParticleOptions particle,double x,double y,double z,int count,double dx,double dy,double dz,double speed,String family){
        if(family.equals("bolt")){
            if(particle==ParticleTypes.EXPLOSION_EMITTER||particle==ParticleTypes.EXPLOSION)particle=ParticleTypes.CRIT;
            count=Math.min(count,7);dx=Math.min(dx,.12);dy=Math.min(dy,.12);dz=Math.min(dz,.12);speed=Math.min(speed,.03);
        }else if(family.equals("plasma")){
            if(particle==ParticleTypes.EXPLOSION_EMITTER){particle=ParticleTypes.ELECTRIC_SPARK;count=12;}
            else if(particle instanceof DustParticleOptions){particle=new DustParticleOptions(0xFF65EFFF,2.0f);count=Math.min(40,Math.max(16,count));}
        }
        return level.sendParticles(particle,x,y,z,count,dx,dy,dz,speed);
    }
}
