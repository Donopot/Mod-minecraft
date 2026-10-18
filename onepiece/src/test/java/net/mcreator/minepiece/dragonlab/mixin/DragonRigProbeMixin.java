package net.mcreator.minepiece.dragonlab.mixin;

import java.util.Map;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.mcreator.minepiece.client.model.KaidoDragonModel;
import net.mcreator.minepiece.dragonlab.DragonLabClient;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Reads the actual evaluated model, rather than treating a pose packet as rendered evidence. */
@Mixin(KaidoDragonModel.class)
public abstract class DragonRigProbeMixin {
   @Shadow @Final private Map<String,ModelPart> bones;
   @Shadow @Final private ModelPart rig;
   @Inject(method="setupAnim",at=@At("TAIL"))
   private void test$rig(EntityRenderState state,CallbackInfo ci){DragonLabClient.rig(bones,rig);}
}
