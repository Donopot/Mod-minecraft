package br.com.oneminepiece.opworldlike.mixin;

import net.mcreator.minepiece.client.TransformPoses;
import net.mcreator.minepiece.client.TransformRenderData;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements TransformRenderData, net.mcreator.minepiece.client.DragonRenderData {
   @Unique private net.mcreator.minepiece.client.DragonSpineMath.Pose minepiece$dragonPose;
   public net.mcreator.minepiece.client.DragonSpineMath.Pose minepiece$dragonPose(){return minepiece$dragonPose;}
   public void minepiece$setDragonPose(net.mcreator.minepiece.client.DragonSpineMath.Pose pose){minepiece$dragonPose=pose;}
   @Unique private boolean minepiece$airborne;
   public boolean minepiece$airborne(){return minepiece$airborne;}
   public void minepiece$setAirborne(boolean value){minepiece$airborne=value;}
   @Unique private int minepiece$material;
   public int minepiece$material(){return minepiece$material;}
   public void minepiece$setMaterial(int flags){minepiece$material=flags;}
   @Unique private double minepiece$form;
   @Unique private String minepiece$fruit = "";
   @Unique private TransformPoses.Snapshot minepiece$poses;
   public double minepiece$form() { return minepiece$form; }
   public String minepiece$fruit() { return minepiece$fruit; }
   public TransformPoses.Snapshot minepiece$poses() { return minepiece$poses; }
   public void minepiece$setForm(double form, String fruit) {
      minepiece$form = form; minepiece$fruit = fruit; minepiece$poses = null;
   }
   public void minepiece$setPoses(TransformPoses.Snapshot poses) { minepiece$poses = poses; }
}
