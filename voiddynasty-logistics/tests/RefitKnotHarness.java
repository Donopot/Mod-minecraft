import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.launch.knot.Knot;

/** Initialize Fabric's real transformer, but never invoke Minecraft.main or open a game window. */
public final class RefitKnotHarness {
    public static void main(String[] args) throws Exception {
        var knot=new Knot(EnvType.CLIENT);
        var loader=knot.init(new String[]{"--gameDir",args[0],"--version","26.3"});
        Thread.currentThread().setContextClassLoader(loader);
        for(String target:new String[]{"menu.SuperChestMenu","ControlPadScreen","client.SuperChestScreen","client.VoidDynastyModClient","weapon.WeaponService","weapon.BoltProjectile","weapon.LaserBeamEntity"}){
            var type=Class.forName("com.donopot.voiddynasty."+target,false,loader);type.getDeclaredMethods();
            if (target.equals("ControlPadScreen") && java.util.Arrays.stream(type.getDeclaredMethods())
                    .noneMatch(method -> method.getName().contains("vd$list"))) {
                throw new AssertionError("Fleet response relay is missing from the transformed pad");
            }
            System.out.println("TRANSFORM PASS "+target);
        }
        knot.addToClassPath(Path.of(args[1]));
        Class.forName("com.donopot.voiddynastybridge.refit.tests.RefitInventoryHarness",true,loader).getMethod("run",String.class).invoke(null,args[2]);
        System.out.println("PASS FABRIC TRANSFORMS AND REAL MINECRAFT INVENTORIES; no game launch");
    }
}
