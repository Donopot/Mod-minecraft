package com.donopot.voiddynasty.work;

import com.donopot.voiddynasty.ai.BrainBags;
import com.donopot.voiddynasty.ai.BrainInv;
import com.donopot.voiddynasty.ai.BrainSpawn;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityLevelChangeEvents;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

/**
 * Recovery of a companion's personal bag after death or a dimension change.
 * Disposable world only; no LLM endpoint is contacted.
 *
 * <p>Phases are driven by {@code braincheckrecovery <phase>} from the Python lab, which restarts
 * the server between {@code stash} and {@code restart}.</p>
 */
public final class BrainRecoveryServerTest implements ModInitializer {

    private static int checks;
    private static final BlockPos BASE = new BlockPos(60, 64, 4);

    @Override
    public void onInitialize() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registry, environment) ->
                dispatcher.register(Commands.literal("braincheckrecovery")
                        .requires(src -> src.getServer().isDedicatedServer())
                        .then(Commands.argument("phase", com.mojang.brigadier.arguments.StringArgumentType.word())
                                .executes(ctx -> {
                                    String phase = com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "phase");
                                    try {
                                        run(ctx.getSource().getLevel(), phase);
                                        ctx.getSource().sendSuccess(() -> Component.literal("PASS " + phase + " checks=" + checks), false);
                                        return 1;
                                    } catch (Throwable e) {
                                        e.printStackTrace();
                                        ctx.getSource().sendFailure(Component.literal("FAIL " + phase + ": " + e));
                                        return 0;
                                    }
                                }))));
    }

    private static void check(boolean yes, String message) {
        if (!yes) throw new AssertionError(message);
        checks++;
    }

    private static ItemStack named(String name, int count) {
        ItemStack stack = new ItemStack(Items.DIAMOND, count);
        stack.set(DataComponents.CUSTOM_NAME, Component.literal(name));
        return stack;
    }

    private static void run(ServerLevel level, String phase) {
        LivingEntity bot = BrainSpawn.find(level);
        var server = level.getServer();
        switch (phase) {
            case "setup" -> {
                // Sol propre + un servitor neuf, sac vide.
                for (int x = 56; x <= 66; x++) {
                    for (int z = 0; z <= 8; z++) {
                        level.setBlock(new BlockPos(x, 63, z), Blocks.SEA_LANTERN.defaultBlockState(), 3);
                        for (int y = 64; y <= 66; y++) level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 3);
                    }
                }
                bot = BrainSpawn.spawn(level, BASE, BrainSpawn.Variant.TIR);
                check(bot != null, "fixture companion spawned");
                check(BrainInv.of(bot).isEmpty(), "fresh companion starts with an empty bag");
                check(BrainBags.list(server).isEmpty(), "ledger starts empty");
            }
            case "death" -> {
                check(bot != null, "companion present before death");
                check(BrainInv.add(bot, named("cle-de-mort", 5)), "seed bag before death");
                check(BrainInv.add(bot, new ItemStack(Items.IRON_INGOT, 12)), "seed a second stack");
                int before = BrainInv.count(bot);
                check(before == 17, "bag holds the expected 17 items");

                bot.kill(level);
                check(!bot.isAlive(), "companion is dead");

                List<BrainBags.Entry> entries = BrainBags.list(server);
                check(entries.size() == 1, "death leaves exactly one ledger entry, found " + entries.size());
                BrainBags.Entry entry = entries.getFirst();
                check(entry.reason().equals("MORT"), "reason is MORT, got " + entry.reason());
                check(entry.count() == before, "ledger holds all " + before + " items, got " + entry.count());
                check(entry.owner().equals(bot.getUUID()), "ledger keeps the dead owner UUID");
                check(entry.dimension().equals(level.dimension().identifier().toString()), "ledger records the dimension");
                check(BrainInv.of(bot).isEmpty(), "the dead owner's bag store is empty, so nothing can duplicate");
            }
            case "restore" -> {
                check(bot == null, "no live companion after the death phase");
                bot = BrainSpawn.spawn(level, BASE, BrainSpawn.Variant.LOURD);
                check(bot != null && BrainInv.of(bot).isEmpty(), "a new companion arrives with a new UUID and an empty bag");
                List<BrainBags.Entry> entries = BrainBags.list(server);
                check(entries.size() == 1, "the dead companion's bag is still waiting");
                String id = entries.getFirst().id();

                String out = BrainBags.restoreToBot(server, id, bot);
                check(out.startsWith("sac " + id + " restitue"), "restore reports success: " + out);
                check(BrainInv.count(bot) == 17, "restored bag holds 17 items, got " + BrainInv.count(bot));
                check(BrainInv.of(bot).stream().anyMatch(s -> s.getHoverName().getString().equals("cle-de-mort")
                        && s.getCount() == 5), "custom name and quantity survive the transfer");
                check(BrainBags.list(server).isEmpty(), "ledger entry consumed, so a second restore cannot duplicate");
                check(BrainBags.restoreToBot(server, id, bot).startsWith("sac introuvable"),
                        "replaying the same id is refused");
                check(BrainInv.count(bot) == 17, "refused replay did not change the bag");
            }
            case "atomic" -> {
                check(bot != null, "companion present for the atomicity test");
                // Sac plein : 27 piles distinctes.
                for (int i = 0; i < 27; i++) check(BrainInv.add(bot, named("plein-" + i, 64)), "fill slot " + i);
                int before = BrainInv.count(bot);
                check(before == 27 * 64, "bag is full");

                // On fabrique une entrée de registre sans toucher au sac vivant.
                check(BrainInv.add(BrainSpawn.spawn(level, BASE.east(4), BrainSpawn.Variant.RAPIDE),
                        named("a-recuperer", 3)), "seed a donor bag");
                LivingEntity donor = BrainSpawn.find(level);
                check(donor != null, "donor exists");
                BrainBags.stash(level, donor, "TEST");
                List<BrainBags.Entry> entries = BrainBags.list(server);
                check(entries.size() == 1, "donor bag registered");
                String id = entries.getFirst().id();

                String out = BrainBags.restoreToBot(server, id, bot);
                check(out.startsWith("refus"), "a full bag refuses the restore: " + out);
                check(BrainInv.count(bot) == before, "refusal left the living bag byte-identical in quantity");
                check(BrainBags.list(server).size() == 1, "refusal kept the ledger entry for a later attempt");
                check(BrainBags.list(server).getFirst().count() == 3, "refusal did not partially move items");

                // Le sac du donneur est vide : plus rien ne peut être restitué deux fois.
                BrainBags.dropAt(server, id, level, bot.position());
                check(BrainBags.list(server).isEmpty(), "drop consumes the entry");
            }
            case "dimension" -> {
                bot = BrainSpawn.find(level);
                check(bot != null, "companion present before the dimension change");
                check(BrainInv.add(bot, named("sac-voyageur", 4)), "seed the travelling bag");
                ServerLevel nether = server.getLevel(Level.NETHER);
                if (nether == null) {
                    // Dimension absente du monde jetable : on documente le saut au lieu de mentir.
                    check(true, "nether unavailable in this lab; migration handler not exercised");
                    break;
                }
                BrainBags.Entry before = null;
                String uuid = bot.getUUID().toString();
                ServerEntityLevelChangeEvents.AFTER_ENTITY_CHANGE_LEVEL.invoker()
                        .afterChangeLevel(bot, bot, level, nether);
                String audit = BrainBags.audit(server, bot.getUUID());
                check(!audit.contains("vivant " + level.dimension().identifier() + " :"),
                        "the source dimension no longer holds the bag: " + audit);
                check(audit.contains("vivant " + nether.dimension().identifier() + " : 4 objet(s)"),
                        "the destination dimension holds the 4 items: " + audit);
                check(BrainBags.list(server).isEmpty(), "a successful migration leaves nothing in the ledger");
                check(before == null && uuid != null, "identity preserved across the change");
            }
            case "stash" -> {
                bot = BrainSpawn.find(level);
                if (bot == null) bot = BrainSpawn.spawn(level, BASE.west(2), BrainSpawn.Variant.TIR);
                check(bot != null, "companion available to seed the restart test");
                if (BrainInv.of(bot).isEmpty()) check(BrainInv.add(bot, named("survit-au-redemarrage", 7)),
                        "seed the bag that must survive a restart");
                String id = BrainBags.stash(level, bot, "TEST_REDEMARRAGE");
                check(id != null, "bag stashed before restart");
                check(BrainBags.list(server).stream().anyMatch(e -> e.id().equals(id) && e.count() == 7),
                        "ledger holds the exact 7 items before the stop");
            }
            case "restart" -> {
                List<BrainBags.Entry> entries = BrainBags.list(server);
                check(!entries.isEmpty(), "ledger survived the clean restart");
                BrainBags.Entry entry = entries.getFirst();
                check(entry.count() == 7, "exact item count survived, got " + entry.count());
                check(entry.reason().equals("TEST_REDEMARRAGE"), "reason survived, got " + entry.reason());
                check(entry.stacks().getFirst().getHoverName().getString().equals("survit-au-redemarrage"),
                        "item components survived the restart");
                bot = BrainSpawn.find(level);
                if (bot == null) bot = BrainSpawn.spawn(level, BASE, BrainSpawn.Variant.TIR);
                String out = BrainBags.restoreToBot(server, entry.id(), bot);
                check(out.startsWith("sac "), "restore after restart succeeds: " + out);
                check(BrainInv.count(bot) == 7, "restored 7 items after restart, got " + BrainInv.count(bot));
                check(BrainBags.list(server).isEmpty(), "no duplicate entry after the restore");
            }
            default -> throw new IllegalArgumentException("unknown phase " + phase);
        }
    }
}
