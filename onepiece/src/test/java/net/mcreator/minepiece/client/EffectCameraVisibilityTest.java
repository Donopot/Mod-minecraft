package net.mcreator.minepiece.client;

/** Run with javac/java; no game, resources, or third-party test framework required. */
public final class EffectCameraVisibilityTest {
   private static int checks;
   private static void check(boolean ok,String name) {
      if(!ok)throw new AssertionError(name);
      checks++;
   }
   public static void main(String[] args) {
      check(EffectCameraVisibility.flameScale(0,1.6F)==0,"camera within flame");
      check(EffectCameraVisibility.flameScale(2,1.6F)==0,"large sprite diagonal clears lens");
      check(EffectCameraVisibility.flameScale(.4,.4F)==0,"small sprite clears lens");
      check(EffectCameraVisibility.flameScale(8,1.6F)==1,"remote observer retains flame");
      check(EffectCameraVisibility.flameScale(1,.4F)>0,"small remote flame remains visible");
      float previous=0;
      for(int i=0;i<=500;i++) {
         float current=EffectCameraVisibility.flameScale(i*.01,1.6F);
         check(current>=previous&&current>=0&&current<=1,"continuous bounded fade at "+i);
         previous=current;
      }
      check(previous==1,"fade restores full size");
      check(EffectCameraVisibility.meraMeshClearance("hiken",1)>.9,"unscaled fist clears camera");
      check(EffectCameraVisibility.meraMeshClearance("higan",1)>2.13,"bullet and two-block trail clear camera");
      check(EffectCameraVisibility.meraMeshClearance("hiken",1.5F)>EffectCameraVisibility.meraMeshClearance("hiken",1),"charged fist clearance grows");
      check(EffectCameraVisibility.meraMeshClearance("daienkai",1.5F)>3.9,"charged Entei sphere clears camera");
      check(EffectCameraVisibility.meraMeshClearance("mera_charge_ball",1)==EffectCameraVisibility.meraMeshClearance("daienkai",1),"charge and release share clearance");
      check(EffectCameraVisibility.meraMeshClearance("goro_raigo",1)==0,"other fruit policy preserved");
      check(EffectCameraVisibility.pendingMeshClearance(2.5F)>2.5,"pending large factory mesh clears eyes");
      check(EffectCameraVisibility.pendingMeshClearance(0)>0,"pending default mesh has clearance");
      System.out.println("Camera visibility: "+checks+" assertions PASS");
   }
}
