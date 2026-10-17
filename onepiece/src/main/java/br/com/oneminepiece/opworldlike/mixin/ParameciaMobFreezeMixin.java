package br.com.oneminepiece.opworldlike.mixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.entity.Mob;
import net.mcreator.minepiece.procedures.ParameciaControl;
@Mixin(Mob.class)
public class ParameciaMobFreezeMixin {
 @Inject(method="serverAiStep",at=@At("HEAD"),cancellable=true)
 private void minepiece$stonePause(CallbackInfo ci){if(ParameciaControl.active((Mob)(Object)this,ParameciaControl.Kind.STONE))ci.cancel();}
}
