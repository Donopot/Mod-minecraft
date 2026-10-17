import net.fabricmc.api.EnvType;
import net.fabricmc.loader.impl.launch.knot.Knot;
import java.util.UUID;

/** Loads transformed client bytecode without running entrypoints, opening a window or a world. */
public final class ClientTransformProbe {
    public static void main(String[] args) throws Exception {
        var knot = new Knot(EnvType.CLIENT);
        var loader = knot.init(new String[]{"--gameDir", args[0], "--version", "26.3"});
        Thread.currentThread().setContextClassLoader(loader);
        String[] targets = {
            "net.minecraft.world.entity.LivingEntity",
            "net.minecraft.server.level.ServerPlayer",
            "net.minecraft.world.level.Level",
            "net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile",
            "net.minecraft.client.renderer.entity.player.AvatarRenderer",
            "net.minecraft.client.renderer.entity.state.AvatarRenderState",
            "net.minecraft.client.renderer.entity.LivingEntityRenderer",
            "net.mcreator.minepiece.client.layer.PlayerTransformLayer",
            "net.mcreator.minepiece.client.renderer.SkillProjectileRenderer",
            "net.mcreator.minepiece.client.renderer.EntityGomuArmRenderer",
            "net.mcreator.minepiece.entity.SkillProjectileEntity",
            "net.mcreator.minepiece.entity.EntityGomuArmEntity",
            "net.mcreator.minepiece.entity.SummonEntity",
            "net.mcreator.minepiece.client.DevilFruitAnimationLayer",
            "com.donopot.voiddynasty.ControlPadScreen",
            "com.donopot.voiddynasty.weapon.WeaponService",
            "com.donopot.voiddynasty.menu.SuperChestMenu"
        };
        for (String name : targets) {
            Class.forName(name, false, loader).getDeclaredMethods();
            System.out.println("CLIENT_TRANSFORM_PASS " + name);
        }
        var state = Class.forName("net.minecraft.client.renderer.entity.state.AvatarRenderState", false, loader);
        var data = Class.forName("net.mcreator.minepiece.client.TransformRenderData", false, loader);
        if (!data.isAssignableFrom(state)) throw new AssertionError("Avatar state Mixin was not applied");
        Object first = state.getDeclaredConstructor().newInstance(), second = state.getDeclaredConstructor().newInstance();
        data.getMethod("minepiece$setForm", double.class, String.class).invoke(first, 1d, "wolf");
        data.getMethod("minepiece$setForm", double.class, String.class).invoke(second, 0d, "leopard");
        if (!data.getMethod("minepiece$fruit").invoke(first).equals("wolf")
              || !data.getMethod("minepiece$form").invoke(second).equals(0d)) throw new AssertionError("Render state contamination");
        var forms = Class.forName("net.mcreator.minepiece.client.TransformClientState", true, loader);
        forms.getMethod("clear").invoke(null);
        UUID a = UUID.randomUUID(), b = UUID.randomUUID();
        forms.getMethod("put", UUID.class, double.class, String.class).invoke(null, a, 1d, "wolf");
        forms.getMethod("put", UUID.class, double.class, String.class).invoke(null, b, 1d, "phoenix");
        Object own = forms.getMethod("get", UUID.class).invoke(null, a);
        if (!own.getClass().getMethod("fruit").invoke(own).equals("wolf")) throw new AssertionError("UUID ownership lost");
        forms.getMethod("clear").invoke(null);
        own = forms.getMethod("get", UUID.class).invoke(null, a);
        if (!own.getClass().getMethod("value").invoke(own).equals(0d)) throw new AssertionError("Disconnect left a form");
        System.out.println("CLIENT_STATE_PASS two independent avatar states, UUID ownership and explicit disconnect clear");
        var profiles = Class.forName("net.mcreator.minepiece.client.renderer.TechniqueVisualProfile", true, loader);
        String[] visible = {"laser1", "laser2", "magatama", "blackcofee", "borobreath", "gagan", "kongokabura", "rubble",
            "padho", "ursusshock", "impactball", "noteattack", "notelaser", "pistolkiss", "minihollow", "tokuhoro",
            "whiteblow", "whitelauncher", "nosefancycannon"};
        for (String id : visible) {
            Object profile = profiles.getMethod("forTechnique", String.class).invoke(null, id);
            if (!profiles.getMethod("replacesLegacyModel").invoke(profile).equals(true)) throw new AssertionError("Missing visible profile: " + id);
        }
        Object legacy = profiles.getMethod("forTechnique", String.class).invoke(null, "unmapped-test-id");
        if (!profiles.getMethod("replacesLegacyModel").invoke(legacy).equals(false)) throw new AssertionError("Unexpected global replacement");
        System.out.println("CLIENT_PROFILE_PASS 19 explicit profiles; geometry and actual visibility require graphical acceptance");
        System.out.println("CLIENT_AUDIT_PASS " + targets.length + " targets and per-avatar state interface; rendering unverified");
    }
}
