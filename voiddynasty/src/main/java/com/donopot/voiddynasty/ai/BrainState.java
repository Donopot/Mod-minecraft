package com.donopot.voiddynasty.ai;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.donopot.voiddynasty.work.WorkState;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Photo de l'état du monde au moment de la décision.
 *
 * <p>C'est la seule chose que le modèle voit du monde réel : pas de devinette, pas de
 * mémoire implicite. Chaque cycle repart de cette photo, ce qui évite qu'un modèle se
 * raconte une histoire sur un état qui n'existe plus.</p>
 */
public final class BrainState {

    private BrainState() {}

    public static JsonObject snapshot(ServerLevel level, LivingEntity bot) {
        JsonObject root = new JsonObject();
        root.addProperty("tick", level.getGameTime());
        root.addProperty("dimension", level.dimension().identifier().getPath());
        root.addProperty("heure", level.getDefaultClockTime() % 24000L);
        root.addProperty("meteo", weather(level));

        if (bot != null) {
            JsonObject s = new JsonObject();
            s.addProperty("x", bot.getBlockX());
            s.addProperty("y", bot.getBlockY());
            s.addProperty("z", bot.getBlockZ());
            s.addProperty("pv", (int) bot.getHealth());
            s.addProperty("pv_max", (int) bot.getMaxHealth());
            s.addProperty("en_vie", bot.isAlive());
            s.addProperty("sac", BrainInv.summary(bot));
            s.addProperty("tache", TaskRunner.describe(bot));
            JsonArray sack = new JsonArray();
            int shown = 0;
            java.util.List<ItemStack> bag;
            try {bag=BrainInv.of(bot);}
            catch(IllegalStateException ex) {
                s.addProperty("sac_erreur",ex.getMessage());bag=java.util.List.of();
            }
            for (ItemStack stack : bag) {
                if (stack.isEmpty() || shown++ >= 10) continue;
                JsonObject it = new JsonObject();
                it.addProperty("item", BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath());
                it.addProperty("n", stack.getCount());
                sack.add(it);
            }
            if(!s.has("sac_erreur")) s.add("sac_detail", sack);
            root.add("servitor", s);
        } else {
            root.addProperty("servitor", "aucun servitor en jeu (l'operateur doit faire /servitor spawn)");
        }

        WorkState work = WorkState.get(level);
        root.add("mission",com.donopot.voiddynasty.work.MissionService.status(level,"current"));
        if (work != null && work.jobName!=null) {
            JsonObject job = new JsonObject();
            job.addProperty("nom", work.jobName);
            job.addProperty("avancement_pct", work.total <= 0 ? 0
                    : (int) (((long) work.total - Math.max(0, work.total - work.done - work.skipped)) * 100 / work.total));
            job.addProperty("blocs_casses", work.done);
            job.addProperty("outil", work.toolId);
            root.add("chantier_automatique", job);
        }

        JsonArray players = new JsonArray();
        if (bot != null) {
            for (ServerPlayer p : level.getServer().getPlayerList().getPlayers()) {
                if(p.level()!=level) continue;
                JsonObject o = new JsonObject();
                o.addProperty("nom", p.getGameProfile().name());
                o.addProperty("x", p.getBlockX());
                o.addProperty("y", p.getBlockY());
                o.addProperty("z", p.getBlockZ());
                o.addProperty("distance", (int) Math.sqrt(p.distanceToSqr(bot)));
                players.add(o);
            }
        }
        root.add("joueurs", players);
        return root;
    }

    private static String weather(ServerLevel level) {
        if (level.isThundering()) return "orage";
        if (level.isRaining()) return "pluie";
        return level.dimension() != Level.OVERWORLD ? "interieur" : "clair";
    }
}
