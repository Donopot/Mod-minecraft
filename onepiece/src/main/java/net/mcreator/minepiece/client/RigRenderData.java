package net.mcreator.minepiece.client;

/** Frozen avatar animation input, safe for Minecraft's deferred model evaluation. */
public interface RigRenderData {
   record Snapshot(float formAge,String action,float actionAge,float walking,boolean airborne,float airborneAge) {}
   Snapshot minepiece$rig();
   void minepiece$setRig(Snapshot snapshot);
}
