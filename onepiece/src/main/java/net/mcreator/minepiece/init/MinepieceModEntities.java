package net.mcreator.minepiece.init;

import net.mcreator.minepiece.entity.EntityGomuArmEntity;
import net.mcreator.minepiece.entity.SkillProjectileEntity;
import net.mcreator.minepiece.entity.SummonEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

/**
 * Shim Fabric 26.3 des entités Mine Piece référencées par la couche pouvoir.
 * Entités skill (bras Gomu, projectile générique) ; les entités des autres fruits seront ajoutées au fil du portage.
 */
public final class MinepieceModEntities {
   public static final String NAMESPACE = "minepiece";

   public static EntityType<EntityGomuArmEntity> ENTITY_GOMU_ARM;
   public static EntityType<SkillProjectileEntity> SKILL_PROJECTILE;
   public static EntityType<SummonEntity> SUMMON;

   private MinepieceModEntities() {
   }

   public static void register() {
      ENTITY_GOMU_ARM = register("entity_gomu_arm",
         EntityType.Builder.of(EntityGomuArmEntity::new, MobCategory.CREATURE)
            .noSave()
            .sized(0.6F, 1.8F)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(NAMESPACE, "entity_gomu_arm"))));

      SKILL_PROJECTILE = register("skill_projectile",
         EntityType.Builder.<SkillProjectileEntity>of((type, level) -> new SkillProjectileEntity(type, level), MobCategory.MISC)
            .noSave()
            .sized(0.25F, 0.25F)
            .clientTrackingRange(64)
            .updateInterval(1)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(NAMESPACE, "skill_projectile"))));

      SUMMON = register("summon",
         EntityType.Builder.<SummonEntity>of((type, level) -> new SummonEntity(type, level), MobCategory.CREATURE)
            .noSave()
            .sized(0.6F, 1.8F)
            .clientTrackingRange(64)
            .updateInterval(3)
            .build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(NAMESPACE, "summon"))));
   }

   private static <T extends net.minecraft.world.entity.Entity> EntityType<T> register(String name, EntityType<T> type) {
      return Registry.register(BuiltInRegistries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(NAMESPACE, name), type);
   }
}
