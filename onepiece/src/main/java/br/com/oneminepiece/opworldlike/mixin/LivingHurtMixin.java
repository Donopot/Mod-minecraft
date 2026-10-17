package br.com.oneminepiece.opworldlike.mixin;

import br.com.oneminepiece.opworldlike.compat.CombatRecursionGuard;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.mcreator.minepiece.procedures.DevilFruitPowerLogic;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

/**
 * Applique la magnification des dégâts de la couche pouvoir (intangibilité Logia, immunité Gomu).
 * Le hook Forge {@code LivingHurtEvent} n'existe pas en Fabric ; {@code ServerLivingEntityEvents.ALLOW_DAMAGE}
 * ne permet pas de modifier le montant. On modifie donc l'argument « amount » de l'appel à
 * {@code actuallyHurt} à l'intérieur de {@code hurtServer}.
 */
@Mixin(LivingEntity.class)
public abstract class LivingHurtMixin {
   /** Only the base implementation is wrapped: ServerPlayer's super call must not push twice. */
   @WrapMethod(method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z")
   private boolean opworldlike$guardDamageCall(ServerLevel level, DamageSource source, float amount,
                                              Operation<Boolean> original) {
      if(source.getEntity() instanceof LivingEntity attacker && net.mcreator.minepiece.procedures.ParameciaControl.active(attacker,net.mcreator.minepiece.procedures.ParameciaControl.Kind.STONE))return false;
      if((Object)this instanceof net.minecraft.server.level.ServerPlayer player && net.mcreator.minepiece.procedures.ZoanTechniqueEpisodes.dodge(player,source))return false;
      if (!CombatRecursionGuard.enter((LivingEntity) (Object) this, source)) return false;
      try {
         return original.call(level, source, amount);
      } finally {
         CombatRecursionGuard.exit();
      }
   }

   @ModifyArg(
      method = "hurtServer(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)Z",
      at = @At(
         value = "INVOKE",
         target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/damagesource/DamageSource;F)V"
      ),
      index = 2
   )
   private float opworldlike$magnifyDevilFruitDamage(ServerLevel level, DamageSource source, float amount) {
      double mult = DevilFruitPowerLogic.computeDamageMagnification(source, (LivingEntity) (Object) this);
      if((Object)this instanceof net.minecraft.server.level.ServerPlayer player && net.mcreator.minepiece.procedures.MythicBatchTechniques.shielded(player) && source.getEntity()!=null && player.getLookAngle().dot(source.getEntity().position().subtract(player.position()).normalize())>0)mult*=.6;
      return amount * (float) mult;
   }
}
