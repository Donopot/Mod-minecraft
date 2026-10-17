package com.donopot.voiddynasty.entity;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import java.util.UUID;

/**
 * Base des boss de la phase E : ajoute une barre de vie.
 *
 * La barre suit la visibilité de l'entité (startSeenByPlayer/stopSeenByPlayer) plutôt qu'une liste de
 * joueurs tenue à la main : c'est le suivi du jeu qui décide qui la voit, donc elle ne peut pas se
 * désynchroniser quand un joueur s'éloigne, meurt ou se déconnecte.
 */
public abstract class ChaosBossEntity extends ChaosCultistEntity {

    private final ServerBossEvent bossEvent;

    protected ChaosBossEntity(EntityType<? extends Monster> type, Level level,
                              String cleNom, BossEvent.BossBarColor couleur) {
        super(type, level);
        this.bossEvent = new ServerBossEvent(UUID.randomUUID(), Component.translatable(cleNom),
                couleur, BossEvent.BossBarOverlay.PROGRESS);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    @Override
    protected void customServerAiStep(ServerLevel level) {
        super.customServerAiStep(level);
        this.bossEvent.setProgress(this.getMaxHealth() <= 0f ? 0f : this.getHealth() / this.getMaxHealth());
    }
}
