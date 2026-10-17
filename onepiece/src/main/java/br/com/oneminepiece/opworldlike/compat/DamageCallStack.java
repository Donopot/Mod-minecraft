package br.com.oneminepiece.opworldlike.compat;

import java.util.ArrayDeque;

/** A call stack, never a history of previous damage events. Kept independent of game classes. */
public final class DamageCallStack<K> {
   private final int maxDepth;
   private final int maxSameKey;
   private final ThreadLocal<ArrayDeque<K>> frames = ThreadLocal.withInitial(ArrayDeque::new);

   public DamageCallStack(int maxDepth, int maxSameKey) {
      if (maxDepth < 1 || maxSameKey < 1) throw new IllegalArgumentException("Positive limits required");
      this.maxDepth = maxDepth;
      this.maxSameKey = maxSameKey;
   }

   /** A false result owns no frame and must not be followed by exit(). */
   public boolean enter(K key, boolean familyInvolved) {
      ArrayDeque<K> stack = frames.get();
      if (stack.size() >= maxDepth) return false;
      if (familyInvolved) {
         int matches = 0;
         for (K frame : stack) if (frame.equals(key)) matches++;
         if (matches >= maxSameKey) return false;
      }
      stack.push(key);
      return true;
   }

   /** Must be paired in finally with each successful enter, including refused and lethal hits. */
   public void exit() {
      ArrayDeque<K> stack = frames.get();
      if (stack.isEmpty()) throw new IllegalStateException("Unbalanced damage guard");
      stack.pop();
      if (stack.isEmpty()) frames.remove();
   }

   public int depth() {
      ArrayDeque<K> stack = frames.get();
      int depth = stack.size();
      if (depth == 0) frames.remove();
      return depth;
   }
}
