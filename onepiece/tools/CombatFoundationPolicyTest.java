import br.com.oneminepiece.opworldlike.compat.DamageCallStack;
import net.mcreator.minepiece.procedures.CombatHitPolicy;

/** Standalone assertions: compile with the two pure production helpers, no Minecraft launch. */
public final class CombatFoundationPolicyTest {
   private static int checks;
   private static void check(boolean condition, String message) {
      if (!condition) throw new AssertionError(message);
      checks++;
   }

   public static void main(String[] args) throws Exception {
      DamageCallStack<String> stack = new DamageCallStack<>(8, 3);
      for (int i = 0; i < 100; i++) {
         check(stack.enter("same victim/source", true), "Sequential damage must not become recursion");
         try { /* normal, lethal and rejected return values all leave through finally */ }
         finally { stack.exit(); }
      }
      check(stack.depth() == 0, "No historical frames");
      check(stack.enter("same", true), "First nested call");
      check(stack.enter("same", true), "Second nested call");
      check(stack.enter("same", true), "Third nested call");
      check(!stack.enter("same", true), "Fourth family call blocked");
      check(stack.depth() == 3, "Rejected entry owns no frame");
      stack.exit(); stack.exit(); stack.exit();
      check(stack.depth() == 0, "Nested frames unwound");

      for (int i = 0; i < 8; i++) check(stack.enter("target " + i, false), "Depth below bound");
      check(!stack.enter("different target", false), "Overall depth is bounded for all sources");
      for (int i = 0; i < 8; i++) stack.exit();

      check(stack.enter("exception", true), "Enter throwing call");
      try {
         try { throw new IllegalStateException("fixture"); }
         finally { stack.exit(); }
      } catch (IllegalStateException expected) { }
      check(stack.depth() == 0, "Exception does not poison next damage call");

      check(stack.enter("main thread", true), "Main thread frame");
      final Throwable[] threadFailure = {null};
      Thread other = new Thread(() -> {
         try {
            if (stack.depth() != 0 || !stack.enter("worker", true)) throw new AssertionError("Thread isolation");
            try { if (stack.depth() != 1) throw new AssertionError("Worker depth"); }
            finally { stack.exit(); }
         } catch (Throwable ex) { threadFailure[0] = ex; }
      });
      other.start(); other.join();
      check(threadFailure[0] == null && stack.depth() == 1, "Worker cannot pop main frame");
      stack.exit();

      check(CombatHitPolicy.confirmed(true, 20, 19), "Health hit confirmed");
      check(CombatHitPolicy.confirmed(true, 28, 20), "Absorption hit confirmed");
      check(!CombatHitPolicy.confirmed(false, 20, 19), "Refused damage has no offensive follow-up");
      check(!CombatHitPolicy.confirmed(true, 20, 20), "Zeroed immunity/shield has no follow-up");
      check(!CombatHitPolicy.confirmed(true, 20, 21), "Healing is not damage");
      check(!CombatHitPolicy.confirmed(true, Double.NaN, 19), "Invalid before rejected");
      check(!CombatHitPolicy.confirmed(true, 20, Double.NaN), "Invalid after rejected");
      check(stack.depth() == 0, "Final stack empty");
      System.out.println("CombatFoundationPolicyTest: " + checks + " assertions passed");
   }
}
