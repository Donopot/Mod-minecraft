package net.mcreator.minepiece.client;

import net.minecraft.client.Camera;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

/** Keeps the rear view above the original mane, clipping the whole offset against terrain. */
public final class DragonCamera {
   private DragonCamera(){}

   public static Vec3 rearPosition(Camera camera,float partialTick) {
      var client=Minecraft.getInstance();
      if(client.player==null || client.level==null || camera.entity()!=client.player
            || client.options.getCameraType()!=CameraType.THIRD_PERSON_BACK || client.player.isPassenger())return null;
      var form=TransformClientState.get(client.player.getUUID());
      if(form.value()!=2 || !DragonFlightControl.FRUIT.equals(form.fruit()))return null;

      Vec3 eye=client.player.getEyePosition(partialTick);
      var local=new Vector3f(-6,7,12).rotate(camera.rotation());
      Vec3 offset=new Vec3(local.x,local.y,local.z);
      double length=offset.length(),fraction=1;
      // An eight-corner camera volume keeps the shoulder offset out of walls and ceilings.
      for(int corner=0;corner<8;corner++) {
         Vec3 start=eye.add((corner&1)==0?-.15:.15,(corner&2)==0?-.15:.15,(corner&4)==0?-.15:.15);
         var hit=client.level.clip(new ClipContext(start,start.add(offset),ClipContext.Block.VISUAL,
            ClipContext.Fluid.NONE,client.player));
         if(hit.getType()!=HitResult.Type.MISS)
            fraction=Math.min(fraction,Math.max(0,(start.distanceTo(hit.getLocation())-.2)/length));
      }
      return eye.add(offset.scale(fraction));
   }
}
