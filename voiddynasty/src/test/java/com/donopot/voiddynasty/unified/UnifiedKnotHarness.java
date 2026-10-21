package com.donopot.voiddynasty.unified;

import java.nio.file.Path;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.launch.knot.Knot;

/** Real client transformer and built-in resource registration, without launching a game window. */
public final class UnifiedKnotHarness {
    public static void main(String[] args) throws Exception {
        var knot=new Knot(EnvType.CLIENT);
        var loader=knot.init(new String[]{"--gameDir",args[0],"--version","26.3"});
        Thread.currentThread().setContextClassLoader(loader);
        var fabric=FabricLoader.getInstance();
        if(fabric.getModContainer("voiddynasty").isEmpty() || fabric.getModContainer("voiddynasty_bridge").isPresent())
            throw new AssertionError("Expected one VOID mod identity, without the standalone bridge");
        var mod=fabric.getModContainer("voiddynasty").orElseThrow();
        if(mod.findPath("resourcepacks/refit/pack.mcmeta").isEmpty())throw new AssertionError("Missing built-in refit pack");
        for(String target:new String[]{"menu.SuperChestMenu","ControlPadScreen","client.SuperChestScreen","client.VoidDynastyModClient","weapon.WeaponService","weapon.BoltProjectile","weapon.LaserBeamEntity"}) {
            var type=Class.forName("com.donopot.voiddynasty."+target,false,loader);type.getDeclaredMethods();
            if(target.equals("ControlPadScreen") && java.util.Arrays.stream(type.getDeclaredMethods()).noneMatch(m->m.getName().contains("vd$list")))
                throw new AssertionError("Missing fleet reply relay");
            System.out.println("UNIFIED TRANSFORM PASS "+target);
        }
        Class.forName("com.donopot.voiddynastybridge.refit.RefitClient",true,loader).getMethod("init").invoke(null);
        System.out.println("UNIFIED REFIT PACK PASS (main mod container)");
        knot.addToClassPath(Path.of(args[1]));
        Class.forName("com.donopot.voiddynastybridge.refit.tests.RefitInventoryHarness",true,loader)
            .getMethod("run",String.class).invoke(null,args[2]);
        System.out.println("PASS UNIFIED CLIENT; no Minecraft window launched");
    }
}
