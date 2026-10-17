package br.com.oneminepiece.opworldlike.mixin;
import org.spongepowered.asm.mixin.*;
import net.mcreator.minepiece.client.ParameciaRenderData;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
@Mixin(LivingEntityRenderState.class)
public class ParameciaRenderStateMixin implements ParameciaRenderData {
 @Unique private boolean minepiece$b04stone;
 public boolean minepiece$stone(){return minepiece$b04stone;}
 public void minepiece$stone(boolean value){minepiece$b04stone=value;}
}
