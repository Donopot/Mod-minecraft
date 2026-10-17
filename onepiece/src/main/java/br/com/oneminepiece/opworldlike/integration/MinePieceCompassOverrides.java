package br.com.oneminepiece.opworldlike.integration;

import br.com.oneminepiece.opworldlike.worldgen.IslandDefinition;
import br.com.oneminepiece.opworldlike.worldgen.OnePieceGeography;
import br.com.oneminepiece.opworldlike.worldgen.UniqueIslandCatalog;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class MinePieceCompassOverrides {
   private MinePieceCompassOverrides() {
   }

   public static boolean giveRealMapCompasses(Entity entity) {
      if (entity instanceof ServerPlayer player && player.level().getServer() != null) {
         ServerLevel overworld = player.level().getServer().overworld();

         for (MinePieceCompassOverrides.CompassTarget target : targets(overworld)) {
            ItemStack stack = compass(target);
            if (!player.getInventory().add(stack)) {
               player.spawnAtLocation(player.level(), stack);
            }
         }

         return true;
      } else {
         return false;
      }
   }

   private static List<MinePieceCompassOverrides.CompassTarget> targets(ServerLevel level) {
      List<MinePieceCompassOverrides.CompassTarget> targets = new ArrayList<>();
      addIslandTarget(targets, level, "advancements.battle_grand_line.title", "sabaody");
      addIslandTarget(targets, level, "advancements.battle_red_line.title", "mariejois");
      addIslandTarget(targets, level, "advancements.battle_new_world.title", "punk_hazard");
      addIslandTarget(targets, level, "advancements.battle_east_blue.title", "starter_east_blue");
      addIslandTarget(targets, level, "advancements.battle_west_blue.title", "starter_west_blue");
      addIslandTarget(targets, level, "advancements.battle_north_blue.title", "starter_north_blue");
      addIslandTarget(targets, level, "advancements.battle_south_blue.title", "starter_south_blue");
      return targets;
   }

   private static void addIslandTarget(List<MinePieceCompassOverrides.CompassTarget> targets, ServerLevel level, String titleKey, String islandId) {
      UniqueIslandCatalog.byId(islandId)
         .map(island -> new MinePieceCompassOverrides.CompassTarget(titleKey, safePosition(level, island)))
         .ifPresent(targets::add);
   }

   private static BlockPos safePosition(ServerLevel level, IslandDefinition island) {
      return OnePieceGeography.safeSpawnFor(level.getSeed(), island);
   }

   private static ItemStack compass(MinePieceCompassOverrides.CompassTarget target) {
      ItemStack stack = new ItemStack(Items.COMPASS);
      stack.set(DataComponents.CUSTOM_NAME, Component.translatable(target.titleKey()));
      // TODO: lodestone tracker 26.3 — DataComponents.LODESTONE_TRACKER + GlobalPos (getOrCreateTag() a disparu)
      return stack;
   }

   private record CompassTarget(String titleKey, BlockPos pos) {
   }
}
