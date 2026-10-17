package net.mcreator.minepiece.client;

/** Immutable per-avatar data carried through Minecraft's deferred render queue. */
public interface TransformRenderData {
   boolean minepiece$airborne();
   void minepiece$setAirborne(boolean airborne);
   int minepiece$material();
   void minepiece$setMaterial(int flags);
   double minepiece$form();
   String minepiece$fruit();
   TransformPoses.Snapshot minepiece$poses();
   void minepiece$setForm(double form, String fruit);
   void minepiece$setPoses(TransformPoses.Snapshot poses);
}
