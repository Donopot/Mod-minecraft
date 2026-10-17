package br.com.oneminepiece.opworldlike.compat;

import br.com.oneminepiece.opworldlike.OmpWorld;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Port Fabric 26.3 de CombatRecursionGuard (Forge 1.20.1).
 *
 * Bloque les dégâts récursifs (un handler de dégât qui en déclenche un autre sur la même cible+source).
 * Le mixin encadre l'appel hurtServer avec try/finally. AFTER_DAMAGE n'est pas un finally :
 * Fabric ne l'émet notamment pas pour les victimes mortes/mourantes. Une pile entretenue par
 * cette paire d'événements finissait donc par bloquer des dégâts successifs non récursifs.
 */
public final class CombatRecursionGuard {
   private static final int MAX_NESTED_DAMAGE_DEPTH = 8;
   private static final int MAX_SAME_DAMAGE_KEY_DEPTH = 3;
   private static final long LOG_COOLDOWN_MS = 5000L;
   private static final DamageCallStack<DamageKey> DAMAGE_STACK =
      new DamageCallStack<>(MAX_NESTED_DAMAGE_DEPTH, MAX_SAME_DAMAGE_KEY_DEPTH);
   private static final CombatRecursionGuard INSTANCE = new CombatRecursionGuard();
   private final Map<CombatRecursionGuard.DamageKey, Long> lastLogByKey = new HashMap<>();

   public static void register() {
      // Kept as an entrypoint for existing callers; LivingHurtMixin owns the one wrapping hook.
   }

   public static boolean enter(LivingEntity entity, DamageSource source) {
      CombatRecursionGuard.DamageKey key = CombatRecursionGuard.DamageKey.from(entity, source);
      // Stack-trace inspection is only relevant near the same-key recursion threshold.
      boolean family = DAMAGE_STACK.depth() >= MAX_SAME_DAMAGE_KEY_DEPTH
         && (INSTANCE.isMinePieceFamilyInvolved(entity, source) || MinePieceAddonCompat.isMinePieceFamilyOnStack());
      if (!DAMAGE_STACK.enter(key, family)) {
         INSTANCE.logSuppressedDamage(key, entity, source);
         return false;
      }
      return true;
   }

   public static void exit() {
      DAMAGE_STACK.exit();
   }

   private boolean isMinePieceFamilyInvolved(LivingEntity target, DamageSource source) {
      String messageId = source.getMsgId();
      return this.isMinePieceFamilyEntity(target)
         || this.isMinePieceFamilyEntity(source.getEntity())
         || this.isMinePieceFamilyEntity(source.getDirectEntity())
         || messageId != null && messageId.toLowerCase(Locale.ROOT).contains("minepiece");
   }

   private boolean isMinePieceFamilyEntity(Entity entity) {
      if (entity == null) {
         return false;
      }

      Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
      return MinePieceAddonCompat.isMinePieceFamily(id);
   }

   private void logSuppressedDamage(CombatRecursionGuard.DamageKey key, LivingEntity target, DamageSource source) {
      long now = System.currentTimeMillis();
      long previous = this.lastLogByKey.getOrDefault(key, 0L);
      if (now - previous >= LOG_COOLDOWN_MS) {
         this.lastLogByKey.put(key, now);
         Identifier targetId = BuiltInRegistries.ENTITY_TYPE.getKey(target.getType());
         String targetName = targetId == null ? target.getType().toString() : targetId.toString();
         OmpWorld.LOGGER
            .warn(
               "Blocked recursive combat damage: target={}, source={}, pos=({}, {}, {})",
               targetName, source.getMsgId(), target.getBlockX(), target.getBlockY(), target.getBlockZ()
            );
      }
   }

   private record DamageKey(UUID target, String source) {
      private static CombatRecursionGuard.DamageKey from(LivingEntity target, DamageSource damageSource) {
         Entity sourceEntity = damageSource.getEntity();
         Entity directEntity = damageSource.getDirectEntity();
         String sourceKey;
         if (sourceEntity != null) {
            sourceKey = "entity:" + sourceEntity.getUUID();
         } else if (directEntity != null) {
            sourceKey = "direct:" + directEntity.getUUID();
         } else {
            sourceKey = "source:" + damageSource.getMsgId();
         }

         return new CombatRecursionGuard.DamageKey(target.getUUID(), sourceKey);
      }
   }
}
