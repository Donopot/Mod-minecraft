package net.mcreator.minepiece.client;

import java.util.Set;
import net.minecraft.client.renderer.entity.state.EntityRenderState;

/** Each queued effect keeps its own animation phase and optional visible bone subset. */
public final class SourceEffectState extends EntityRenderState {
   public final String clip;
   public final float seconds;
   public final Set<String> visibleBones;
   public SourceEffectState(String clip,float seconds,Set<String> visibleBones){this.clip=clip;this.seconds=seconds;this.visibleBones=Set.copyOf(visibleBones);ageInTicks=seconds*20;}
}
