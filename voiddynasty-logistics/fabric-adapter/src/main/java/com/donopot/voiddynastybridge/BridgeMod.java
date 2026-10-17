package com.donopot.voiddynastybridge;

import java.util.Set;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.core.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.*;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;

public final class BridgeMod implements ModInitializer {
    public static CargoBlock CARGO;
    public static BlockEntityType<CargoBlockEntity> CARGO_ENTITY;
    public static volatile DeliveryService service;
    @Override public void onInitialize(){
        Identifier id=Identifier.fromNamespaceAndPath("voiddynasty_bridge","cargo");
        CARGO=Registry.register(BuiltInRegistries.BLOCK,id,new CargoBlock(BlockBehaviour.Properties.of().setId(ResourceKey.create(Registries.BLOCK,id)).strength(-1,3600000).noLootTable().pushReaction(PushReaction.IMMOVEABLE)));
        CARGO_ENTITY=Registry.register(BuiltInRegistries.BLOCK_ENTITY_TYPE,id,new BlockEntityType<>(CargoBlockEntity::new,Set.of(CARGO)));
        PlayerBlockBreakEvents.BEFORE.register((level,player,pos,state,entity)->!state.is(CARGO));
        ServerLifecycleEvents.SERVER_STARTED.register(server->{if(server.isSingleplayer()&&!server.isPublished())service=new DeliveryService(server);});
        ServerTickEvents.END_SERVER_TICK.register(server->{if(service!=null&&service.server==server)service.tick();});
        ServerLifecycleEvents.SERVER_STOPPING.register(server->{if(service!=null&&service.server==server){service.close();service=null;}});
    }
}
