package br.com.oneminepiece.opworldlike.claim;

import br.com.oneminepiece.opworldlike.integration.MinePieceBridge;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;

public final class IslandDominionBonusEvents {
   private static final IslandDominionBonusEvents INSTANCE = new IslandDominionBonusEvents();
   private final Map<UUID, IslandDominionBonusEvents.DeathDorikiSnapshot> deathDorikiSnapshots = new HashMap<>();

   private IslandDominionBonusEvents() {
   }

   public static void register() {
      ServerLifecycleEvents.SERVER_STARTED.register(INSTANCE::onServerStarted);
      ServerLivingEntityEvents.AFTER_DEATH.register(INSTANCE::onLivingDeath);
      ServerPlayerEvents.COPY_FROM.register(INSTANCE::onCopyFrom);
      ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> INSTANCE.onPlayerLogin(handler.player));
      ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> INSTANCE.onPlayerLogout(handler.player));
   }

   private void onServerStarted(MinecraftServer server) {
      ServerLevel level = server.overworld();
      IslandDominionBonusData.get(level).reconcileClaims(level, IslandClaimsData.get(level).allClaims());
   }

   private void onLivingDeath(LivingEntity entity, DamageSource source) {
      if (entity instanceof ServerPlayer player) {
         IslandDominionBonusData data = IslandDominionBonusData.get(player.level());
         int bonus = data.dorikiLimitBonus(player.getUUID());
         if (bonus <= 0) {
            this.deathDorikiSnapshots.remove(player.getUUID());
         } else {
            this.deathDorikiSnapshots.put(player.getUUID(), new IslandDominionBonusEvents.DeathDorikiSnapshot(MinePieceBridge.currentDoriki(player), bonus));
         }
      }
   }

   private void onCopyFrom(ServerPlayer oldPlayer, ServerPlayer newPlayer, boolean alive) {
      if (!alive) {
         IslandDominionBonusEvents.DeathDorikiSnapshot snapshot = this.deathDorikiSnapshots.remove(newPlayer.getUUID());
         if (snapshot != null && snapshot.bonus() > 0) {
            IslandDominionBonusData data = IslandDominionBonusData.get(newPlayer.level());
            int currentBonus = data.dorikiLimitBonus(newPlayer.getUUID());
            if (currentBonus > 0) {
               double protectedBonus = Math.min(snapshot.bonus(), currentBonus);
               double keptRatio = 1.0;
               double baseBeforeDeath = Math.max(0.0, snapshot.doriki() - snapshot.bonus());
               double targetDoriki = baseBeforeDeath * keptRatio + protectedBonus;
               targetDoriki = Math.min(targetDoriki, MinePieceBridge.effectiveDorikiLimit(newPlayer));
               double currentDoriki = MinePieceBridge.currentDoriki(newPlayer);
               if (currentDoriki + 0.001 < targetDoriki) {
                  MinePieceBridge.applyDorikiDelta(newPlayer, targetDoriki - currentDoriki);
               }
            }
         }
      }
   }

   private void onPlayerLogin(ServerPlayer player) {
      ServerLevel level = player.level();
      IslandDominionBonusData data = IslandDominionBonusData.get(level);
      data.reconcileClaims(level, IslandClaimsData.get(level).allClaims());
      data.applyPending(player);
   }

   private void onPlayerLogout(ServerPlayer player) {
      this.deathDorikiSnapshots.remove(player.getUUID());
   }

   private record DeathDorikiSnapshot(double doriki, int bonus) {
   }
}
