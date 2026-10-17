package net.mcreator.minepiece.client;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Captures poses now, rather than consulting a mutable parent during deferred submission. */
public final class TransformPoses {
   public record Snapshot(PartPose head, PartPose body, PartPose rightArm, PartPose leftArm,
                          PartPose rightLeg, PartPose leftLeg) {}
   private TransformPoses() {}
   public static Snapshot capture(PlayerModel parent) {
      return new Snapshot(parent.head.storePose(), parent.body.storePose(), parent.rightArm.storePose(),
         parent.leftArm.storePose(), parent.rightLeg.storePose(), parent.leftLeg.storePose());
   }
   public static Snapshot forState(EntityRenderState state) {
      if(state instanceof ParameciaRenderData stone&&stone.minepiece$stone())return new Snapshot(PartPose.ZERO,PartPose.ZERO,PartPose.offset(-5,2,0),PartPose.offset(5,2,0),PartPose.offset(-1.9F,12,0),PartPose.offset(1.9F,12,0));
      return state instanceof TransformRenderData data ? data.minepiece$poses() : null;
   }
}
