package br.com.oneminepiece.opworldlike.mixin;

import br.com.oneminepiece.opworldlike.claim.ProtectedSkillEffectGuard;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Bloque la mutation de blocs dans les territoires protégés (claims).
 * Adapté à 26.3 : setBlock/destroyBlock n'existent plus qu'en variante 4-args (recursionLeft) ;
 * setBlockAndUpdate (3 args) et destroyBlock (2 args) ont disparu.
 */
@Mixin(Level.class)
public abstract class LevelMutationMixin {
   @Inject(
      method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
      at = @At("HEAD"),
      cancellable = true
   )
   private void opworldlike$blockAbilitySetBlock(BlockPos pos, BlockState state, int flags, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
      if (ProtectedSkillEffectGuard.shouldBlockStackMutation((Level)(Object)this, pos)) {
         cir.setReturnValue(false);
      }
   }

   @Inject(
      method = "destroyBlock(Lnet/minecraft/core/BlockPos;ZLnet/minecraft/world/entity/Entity;I)Z",
      at = @At("HEAD"),
      cancellable = true
   )
   private void opworldlike$blockAbilityDestroyBlock(BlockPos pos, boolean dropBlock, Entity entity, int recursionLeft, CallbackInfoReturnable<Boolean> cir) {
      if (ProtectedSkillEffectGuard.shouldBlockStackMutation((Level)(Object)this, pos)) {
         cir.setReturnValue(false);
      }
   }

   @Inject(method = "removeBlock(Lnet/minecraft/core/BlockPos;Z)Z", at = @At("HEAD"), cancellable = true)
   private void opworldlike$blockAbilityRemoveBlock(BlockPos pos, boolean moving, CallbackInfoReturnable<Boolean> cir) {
      if (ProtectedSkillEffectGuard.shouldBlockStackMutation((Level)(Object)this, pos)) {
         cir.setReturnValue(false);
      }
   }
}
