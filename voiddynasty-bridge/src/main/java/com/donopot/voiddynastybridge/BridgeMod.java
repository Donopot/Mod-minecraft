package com.donopot.voiddynastybridge;

import java.util.Set;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStarted;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopping;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents.EndTick;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents.Before;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour.Properties;
import net.minecraft.world.level.material.PushReaction;

public final class BridgeMod implements ModInitializer {
   public static CargoBlock CARGO;
   public static BlockEntityType<CargoBlockEntity> CARGO_ENTITY;
   public static volatile DeliveryService service;

   public void onInitialize() {
      Identifier var1 = Identifier.fromNamespaceAndPath("voiddynasty_bridge", "cargo");
      CARGO = (CargoBlock)Registry.register(
         BuiltInRegistries.BLOCK,
         var1,
         new CargoBlock(
            Properties.of().setId(ResourceKey.create(Registries.BLOCK, var1)).strength(-1.0F, 3600000.0F).noLootTable().pushReaction(PushReaction.IMMOVEABLE)
         )
      );
      CARGO_ENTITY = (BlockEntityType<CargoBlockEntity>)Registry.register(
         BuiltInRegistries.BLOCK_ENTITY_TYPE, var1, new BlockEntityType(CargoBlockEntity::new, Set.of(CARGO))
      );
      PlayerBlockBreakEvents.BEFORE.register((Before)(var0, var1x, var2, var3, var4) -> !var3.is(CARGO));
      ServerLifecycleEvents.SERVER_STARTED.register((ServerStarted)var0 -> {
         if (var0.isSingleplayer() && !var0.isPublished()) {
            service = new DeliveryService(var0);
         }
      });
      ServerTickEvents.END_SERVER_TICK.register((EndTick)var0 -> {
         if (service != null && service.server == var0) {
            service.tick();
         }
      });
      ServerLifecycleEvents.SERVER_STOPPING.register((ServerStopping)var0 -> {
         if (service != null && service.server == var0) {
            service.close();
            service = null;
         }
      });
   }
}
