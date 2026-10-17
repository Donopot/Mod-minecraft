package net.mcreator.minepiece.procedures;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.mochi.DevilFruitMochiConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Blocks;

/** Permanent mochi constructions with bounded active attribution. Population eviction, owner removal and expiry release attribution only. Protected inventories and zones remain intact. */
public final class MochiBuild {

   /** Attributions actives par porteur ; les blocs restent dans le monde. */
   private static final Map<UUID, List<List<BlockPos>>> CONSTRUCTIONS = new HashMap<>();

   /** Raison de la dernière disparition, par porteur. */
   private static final Map<UUID, String> LAST_CLEAR = new HashMap<>();

   private MochiBuild() {
   }

   /** Nombre de constructions actives d'un porteur (observable). */
   public static int activeCount(ServerPlayer porteur) {
      if (porteur == null) {
         return 0;
      }
      List<List<BlockPos>> l = CONSTRUCTIONS.get(porteur.getUUID());
      return l == null ? 0 : l.size();
   }

   /** Raison de la dernière disparition. */
   public static String lastClearReason(ServerPlayer porteur) {
      return porteur == null ? "aucune" : LAST_CLEAR.getOrDefault(porteur.getUUID(), "aucune");
   }

   /** Nombre total de blocs de mochi encore suivis (observable). */
   public static int placedBlocks() {
      int n = 0;
      for (List<List<BlockPos>> l : CONSTRUCTIONS.values()) {
         for (List<BlockPos> c : l) {
            n += c.size();
         }
      }
      return n;
   }

   /**
    * Pose une construction de mochi.
    *
    * <p>Chaque bloc passe par {@link PowerBlockService#place}, le terrain reste définitif. Seule l’attribution
    * au pouvoir a une durée finie.</p>
    *
    * @return nombre de blocs réellement posés
    */
   public static int build(ServerPlayer porteur, BlockPos centre) {
      if (porteur == null || !porteur.isAlive() || !(porteur.level() instanceof ServerLevel level)) {
         return 0;
      }
      if (DevilFruitPowerLogic.isSuppressed(porteur)) {
         return 0;
      }
      // ATTRIBUTION LIMITÉE : au plafond, libérer le bail le plus ancien.
      // Le terrain de toutes les constructions reste définitif.
      // Ce plafond borne le suivi actif, jamais la durée des blocs.
      List<List<BlockPos>> mes = CONSTRUCTIONS.computeIfAbsent(porteur.getUUID(), k -> new ArrayList<>());
      if (mes.size() >= DevilFruitMochiConfig.constructionMaxActive()) {
         List<BlockPos> ancienne = mes.remove(0);
         for (BlockPos p : ancienne) {
            PowerBlockService.releaseNow(level, p, porteur.getUUID());
         }
         LAST_CLEAR.put(porteur.getUUID(), "population");
      }
      List<BlockPos> poses = new ArrayList<>();
      int max = DevilFruitMochiConfig.constructionMaxBlocks();
      int duree = DevilFruitMochiConfig.constructionLifetimeTicks();
      outer:
      for (int y = 0; y < 4; y++) {
         for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
               if (poses.size() >= max) {
                  break outer;
               }
               BlockPos p = centre.offset(x, y, z);
               if(!level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new net.minecraft.world.phys.AABB(p),e->e.isAlive() && !(e instanceof net.mcreator.minepiece.entity.EntityGomuArmEntity)).isEmpty())continue;
               int avant = PowerBlockService.activeCount();
               PowerBlockService.place(level, p, Blocks.HONEY_BLOCK.defaultBlockState(), duree,
                  porteur.getUUID());
               // On ne compte que ce qui a été RÉELLEMENT posé : place() refuse les zones protégées et
               // les blocs non remplaçables. Compter les tentatives ferait croire à une construction
               // plus grande qu'elle n'est.
               if (PowerBlockService.activeCount() > avant) {
                  poses.add(p);
                  level.sendParticles(ParticleTypes.CLOUD, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5,
                     3, 0.2, 0.2, 0.2, 0.01);
               }
            }
         }
      }
      if (poses.isEmpty()) {
         return 0;
      }
      mes.add(poses);
      level.playSound(null, centre, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.PLAYERS, 1.0F, 0.7F);
      return poses.size();
   }

   /** Libère immédiatement l’attribution des constructions d’un porteur (retrait du Fruit, mort, suppression). */
   public static int clearFrom(ServerPlayer porteur, String raison) {
      if (porteur == null || !(porteur.level() instanceof ServerLevel level)) {
         return 0;
      }
      List<List<BlockPos>> mes = CONSTRUCTIONS.remove(porteur.getUUID());
      if (mes == null) {
         return 0;
      }
      int n = 0;
      for (List<BlockPos> c : mes) {
         for (BlockPos p : c) {
            if (PowerBlockService.releaseNow(level, p, porteur.getUUID())) {
               n++;
            }
         }
      }
      LAST_CLEAR.put(porteur.getUUID(), raison);
      return n;
   }

   /** Oublie un porteur sans toucher au monde (les blocs sont définitifs). */
   public static void forget(ServerPlayer porteur) {
      if (porteur != null) {
         CONSTRUCTIONS.remove(porteur.getUUID());
      }
   }

   /** Oublie tout le monde. */
   public static void forgetAll() {
      CONSTRUCTIONS.clear();
      LAST_CLEAR.clear();
   }
}
