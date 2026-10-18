package net.mcreator.minepiece.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;

/** Uses vanilla flight permission and collision, replacing only complete Seiryu steering. */
public final class DragonFlightControl {
   public static final String FRUIT="minepiece:item_devil_fruit_uo_uo_seiryu";
   private DragonFlightControl(){}
   public static boolean travel(LocalPlayer player) {
      var form=TransformClientState.get(player.getUUID());
      if(form.value()!=2 || !FRUIT.equals(form.fruit()) || !player.isAlive() || player.isSpectator()
            || !player.getAbilities().mayfly || !player.getAbilities().flying
            || player.isPassenger() || player.isInWater() || player.isInLava())return false;
      var client=Minecraft.getInstance();var input=player.input.getMoveVector();
      boolean blocked=client.gui.screen()!=null;
      double vertical=blocked?0:(player.input.keyPresses.jump()?1:0)-(player.input.keyPresses.shift()?1:0);
      Vec3 current=player.getDeltaMovement();
      var next=DragonFlightMath.steer(new DragonFlightMath.Vector(current.x,current.y,current.z),
         player.getYRot(),player.getXRot(),blocked?0:input.y,blocked?0:input.x,vertical,player.isSprinting());
      Vec3 movement=new Vec3(next.x(),next.y(),next.z()),before=player.position();
      player.setDeltaMovement(movement);
      player.move(MoverType.SELF,movement);
      Vec3 actual=player.position().subtract(before);
      player.setDeltaMovement(new Vec3(Math.abs(actual.x-movement.x)>.001?0:movement.x,
         Math.abs(actual.y-movement.y)>.001?0:movement.y,Math.abs(actual.z-movement.z)>.001?0:movement.z));
      player.fallDistance=0;
      return true;
   }
}
