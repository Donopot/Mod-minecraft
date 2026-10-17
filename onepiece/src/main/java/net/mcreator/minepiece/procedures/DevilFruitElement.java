package net.mcreator.minepiece.procedures;

import java.util.HashMap;
import java.util.Map;
import net.mcreator.minepiece.init.MinepieceModItems;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Mapping fruit → élément (CORE-012). Un fruit logia/élémentaire applique son élément à ses
 * techniques ; tout le reste (Zoans, Paramecia physiques) reste {@link ElementType#PHYSICAL}.
 */
public final class DevilFruitElement {

   private static final Map<Item, ElementType> BY_ITEM = new HashMap<>();

   static {
      // Feu
      BY_ITEM.put(MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA, ElementType.FIRE);
      BY_ITEM.put(MinepieceModItems.ITEM_DEVIL_FRUIT_TORI_TORI_PHOENIX, ElementType.FIRE);
      // Magma
      BY_ITEM.put(MinepieceModItems.ITEM_DEVIL_FRUIT_MAGU_MAGU, ElementType.MAGMA);
      // Glace
      BY_ITEM.put(MinepieceModItems.ITEM_DEVIL_FRUIT_HIE_HIE, ElementType.ICE);
      BY_ITEM.put(MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_OKUCHI_NO_MAKAMI, ElementType.ICE);
      // Foudre
      BY_ITEM.put(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO, ElementType.LIGHTNING);
      // Explosion
      BY_ITEM.put(MinepieceModItems.ITEM_DEVIL_FRUIT_BOMU_BOMU, ElementType.EXPLOSIVE);
      BY_ITEM.put(MinepieceModItems.ITEM_DEVIL_FRUIT_GURA_GURA, ElementType.EXPLOSIVE);
      // Charme
      BY_ITEM.put(MinepieceModItems.ITEM_DEVIL_FRUIT_MERO_MERO, ElementType.CHARM);
      // Fantôme
      BY_ITEM.put(MinepieceModItems.ITEM_DEVIL_FRUIT_HORO_HORO, ElementType.GHOST);
   }

   private DevilFruitElement() {
   }

   /** Résout l'élément dominant du fruit possédé (PHYSICAL par défaut). */
   public static ElementType of(ItemStack fruit) {
      if (fruit.isEmpty()) {
         return ElementType.PHYSICAL;
      }
      return BY_ITEM.getOrDefault(fruit.getItem(), ElementType.PHYSICAL);
   }

   /** A fruit can contain both bodily strikes and elemental techniques. */
   public static ElementType forTechnique(ItemStack fruit, String technique) {
      if(fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_TORI_TORI_PHOENIX))return ElementType.PHYSICAL;
      if(fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_OKUCHI_NO_MAKAMI))return java.util.Set.of("narikabura","namujihyoga","himorogiri").contains(technique)?ElementType.ICE:ElementType.PHYSICAL;
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_UO_UO_SEIRYU) && "borobreath".equals(technique)) return ElementType.FIRE;
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_RYU_RYU_BRACHIOSAURUS) && "blackcofee".equals(technique)) return ElementType.LIGHT;
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_PIKA_PIKA)
            && java.util.Set.of("laser1", "laser2", "magatama").contains(technique)) return ElementType.LIGHT;
      return of(fruit);
   }

   /**
    * Visuel de repli intentionnel (modèle + texture) d'un projectile sans mapping explicite,
    * choisi selon l'élément (CORE-017) — évite le « météore par défaut » hors-sujet.
    */
   public static String[] projectileVisualFallback(ElementType element) {
      return switch (element) {
         case FIRE, MAGMA -> new String[] { "fist_small", "flame_tiger" };
         case ICE -> new String[] { "partisan", "tex_ice" };
         case LIGHTNING -> new String[] { "ball_maximum", "meteor" };
         case EXPLOSIVE -> new String[] { "ball_maximum", "clear" };
         case CHARM -> new String[] { "fist_small", "clear" };
         case GHOST -> new String[] { "ball_maximum", "tex_black" };
         default -> new String[] { "fist_small", "clear" };
      };
   }
}
