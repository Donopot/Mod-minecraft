package com.donopot.voiddynasty.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Une tâche du Servitor : un but qui dure plusieurs ticks, piloté par le serveur.
 *
 * <p>Le modèle ne demande pas « casse ce bloc » mais « mine cette zone » : c'est la tâche qui
 * découpe le travail en pas concrets, tick par tick. Une tâche peut échouer ou expirer ; le
 * compte rendu remonte au modèle au cycle suivant, qui décide de la suite.</p>
 */
public interface BrainTask {

    enum Status { RUNNING, DONE, FAILED }

    /** Contexte d'exécution, reconstruit à chaque tick (aucune référence gardée). */
    record Ctx(ServerLevel level, LivingEntity bot, BrainConfig cfg) {

        public boolean hasBot() {
            return bot != null && bot.isAlive();
        }

        public Mob mob() {
            return bot instanceof Mob m ? m : null;
        }

        public double distTo(double x, double y, double z) {
            return bot == null ? Double.MAX_VALUE
                    : Math.sqrt(bot.distanceToSqr(x, y, z));
        }

        public long gameTime() {
            return level.getGameTime();
        }
    }

    /** Phrase courte lisible par l'opérateur et par le modèle. */
    String describe();

    /** Appelée une fois, sur le fil du serveur, quand la tâche démarre. */
    default void start(Ctx ctx) {}

    /** Appelée à chaque tick serveur. Ne doit jamais bloquer. */
    Status tick(Ctx ctx);

    /** Appelée si une autre tâche prend la place ou si l'opérateur annule. */
    default void cancel(Ctx ctx) {
        Mob mob = ctx.mob();
        if (mob != null) mob.getNavigation().stop();
    }

    /** Une tâche longue n'expire pas toute seule (suivre un joueur, patrouiller). */
    default boolean longRunning() {
        return false;
    }

    /** Progression en pourcentage, -1 si non mesurable. */
    default int progress() {
        return -1;
    }

    /** Position visée, pour l'affichage ; {@code null} si aucune. */
    default BlockPos target() {
        return null;
    }
}
