package com.donopot.voiddynastybridge.refit;
import com.donopot.voiddynasty.weapon.*;
import com.donopot.voiddynasty.terrain.TerrainDamageManager;
import net.minecraft.server.level.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

public final class LaserShot {
    private LaserShot(){}
    public static void fire(ServerPlayer player,ServerLevel level,WeaponDefinition definition,Vec3 start,Vec3 direction){
        Vec3 end=start.add(direction.normalize().scale(Math.clamp(definition.maxRange(),1,512)));
        var wall=level.clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
        var entities=level.getEntities(player,new AABB(start,wall.getLocation()).inflate(.3),e->e instanceof LivingEntity&&e.isAlive()&&e.isPickable()&&!e.isSpectator());
        var choice=LaserRay.closest(start,wall.getLocation(),entities.stream().map(e->e.getBoundingBox().inflate(.05)).toList());
        Vec3 hit=choice.point();LivingEntity target=choice.index()<0?null:(LivingEntity)entities.get(choice.index());
        // The trace follows the current server look direction; a nearer wall always wins.
        level.addFreshEntity(new LaserBeamEntity(level,start,hit,0xFFFF3C45));
        if(target!=null)DamageCalculator.applyDirect(player,target,definition,hit);
        else if(wall.getType()==HitResult.Type.BLOCK)TerrainDamageManager.onImpact(level,wall.getBlockPos(),start,direction,definition.terrain());
        if(target!=null||wall.getType()==HitResult.Type.BLOCK)level.sendParticles(ParticleTypes.ELECTRIC_SPARK,hit.x,hit.y,hit.z,5,.06,.06,.06,.025);
    }
}
