package com.donopot.voiddynasty.client;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.Commands;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import com.donopot.voiddynasty.component.ModDataComponents;
import com.donopot.voiddynasty.registry.ModItems;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.mcreator.minepiece.network.DevilFruitNetworking;
import net.mcreator.minepiece.init.MinepieceModItems;

/** Disposable combined-mod input test. Never included in either playable JAR. */
public final class HudServerTest implements ModInitializer {
    private static double previous=200;private static int casts,baseline;private static boolean initial;
    private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger("HUDTEST");
    public void onInitialize() {
        ServerPlayConnectionEvents.JOIN.register((handler,sender,server)->server.execute(()->setup(handler.player)));
        ServerTickEvents.END_SERVER_TICK.register(server->{for(ServerPlayer p:server.getPlayerList().getPlayers()) {
            double energy=MinepieceModVariables.get(p).PlayerEnergy;
            if(energy<previous-.001)casts++;previous=energy;
        }});
        CommandRegistrationCallback.EVENT.register((dispatcher,registry,env)->dispatcher.register(Commands.literal("hudfixture")
                .then(Commands.argument("phase",StringArgumentType.word()).executes(ctx->{
                    var player=ctx.getSource().getPlayerOrException();String phase=StringArgumentType.getString(ctx,"phase");
                    try {run(player,phase);LOG.info("HUD_SERVER PASS {}",phase);return 1;}
                    catch(Throwable error) {LOG.error("HUD_SERVER FAIL {}",phase,error);return 0;}
                }))));
    }
    private static void setup(ServerPlayer player) {
        var vars=MinepieceModVariables.get(player);
        vars.PlayerDevilFruit=new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA);
        vars.UseDevilAbilities=true;vars.PlayerEnergy=vars.PlayerEnergyMAX=vars.PlayerEnergyFormer=200;
        vars.PlayerTechniqueUsedNumber=0;vars.player_form=0;
        ItemStack gun=new ItemStack(ModItems.BOLTGUN);gun.set(ModDataComponents.AMMO,8);
        player.setItemSlot(EquipmentSlot.MAINHAND,gun);
        if(!initial) {player.getInventory().add(new ItemStack(ModItems.BOLT_SHELL,64));initial=true;}
        previous=200;casts=0;DevilFruitNetworking.sendSyncState(player);
    }
    private static void check(boolean value,String message) {if(!value)throw new AssertionError(message);}
    private static void run(ServerPlayer player,String phase) {
        var vars=MinepieceModVariables.get(player);ItemStack gun=player.getMainHandItem();
        switch(phase) {
            case "menu" -> {
                check(casts==0 && vars.PlayerTechniqueUsedNumber==0,"menu must not cast or change technique");
                check(gun.getOrDefault(ModDataComponents.AMMO,0)==8 && gun.getOrDefault(ModDataComponents.RELOADING,0)==0,"menu must not reload");
            }
            case "cast" -> {check(casts==1,"one real cast, observed energy deductions="+casts);}
            case "reload" -> {check(gun.getOrDefault(ModDataComponents.RELOADING,0)>0 || gun.getOrDefault(ModDataComponents.AMMO,0)>8,"real remapped reload reached server");}
            case "prepareLegacy" -> {baseline=casts;vars.PlayerEnergy=200;previous=200;player.getCooldowns().removeCooldown(Identifier.fromNamespaceAndPath("minepiece","item_devil_fruit_mera_mera"));DevilFruitNetworking.sendSyncState(player);}
            case "legacy" -> {check(casts==baseline,"shared legacy R must not cast while holding the tagged firearm");}
            default -> throw new IllegalArgumentException(phase);
        }
    }
}
