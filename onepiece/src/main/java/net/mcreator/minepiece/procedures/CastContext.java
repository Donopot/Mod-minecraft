package net.mcreator.minepiece.procedures;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** Scoped identity shared by server execution and the visuals it spawns. */
public final class CastContext implements AutoCloseable {
   public record TechniqueKey(String fruit, String technique) { }
   private static final ThreadLocal<TechniqueKey> CURRENT = new ThreadLocal<>();
   private final TechniqueKey previous;
   private CastContext(ItemStack fruit, DevilFruitMove move) {
      previous = CURRENT.get();
      CURRENT.set(new TechniqueKey(BuiltInRegistries.ITEM.getKey(fruit.getItem()).toString(), move.id()));
   }
   public static CastContext enter(ItemStack fruit, DevilFruitMove move) { return new CastContext(fruit,move); }
   public static TechniqueKey current() { return CURRENT.get(); }
   public static String currentId() { var key=CURRENT.get(); return key==null?"":key.technique(); }
   @Override public void close() { if(previous==null)CURRENT.remove(); else CURRENT.set(previous); }
}
