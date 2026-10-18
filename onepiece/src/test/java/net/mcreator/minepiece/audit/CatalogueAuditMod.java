package net.mcreator.minepiece.audit;

import com.google.gson.*;
import com.mojang.authlib.GameProfile;
import java.nio.file.*;
import java.util.*;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.mcreator.minepiece.procedures.*;
import net.minecraft.core.registries.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;

/** Separate test-only mod: reads the loaded catalogue in a disposable server. Never casts a skill. */
public final class CatalogueAuditMod implements ModInitializer {
   @Override public void onInitialize() {
      ServerLifecycleEvents.SERVER_STARTED.register(server -> {
         var report=new JsonObject();var fruits=new JsonArray();var errors=new JsonArray();var policies=new JsonArray();
         var player=FakePlayer.get(server.overworld(),new GameProfile(UUID.nameUUIDFromBytes("CatalogueAudit".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"CatalogueAudit"));
         int count=0;
         for(var item:BuiltInRegistries.ITEM) {
            var moves=DevilFruitRegistry.movesFor(item);if(moves.isEmpty())continue;
            String fruit=TechniqueTerrain.fruitKey(item);var row=new JsonObject();row.addProperty("fruit",fruit);
            var stack=new ItemStack(item);MinepieceModVariables.get(player).PlayerDevilFruit=stack;
            var techniques=new JsonArray();var ids=new HashSet<String>();
            for(var move:moves) {
               count++;var detail=new JsonObject();detail.addProperty("id",move.id());
               if(!ids.add(move.id()))errors.add(fruit+"/"+move.id()+": duplicate");
               if(!Double.isFinite(move.damage())||!Double.isFinite(move.energyCost())||move.energyCost()<0||move.cooldownTicks()<0)errors.add(fruit+"/"+move.id()+": invalid budget");
               player.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
               String bare=TechniqueAnimations.nameFor(move,player);
               player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));
               String sword=TechniqueAnimations.nameFor(move,player);
               detail.addProperty("animation_empty_hand",bare);detail.addProperty("animation_sword",sword);
               if(bare.isEmpty()||sword.isEmpty())errors.add(fruit+"/"+move.id()+": missing selected animation");
               var policy=TechniqueTerrain.policy(item,move.id());
               if(policy==null)errors.add(fruit+"/"+move.id()+": missing terrain policy");
               else {var p=new JsonObject();p.addProperty("fruit",fruit);p.addProperty("technique",move.id());p.addProperty("mode",policy.mode().name());p.addProperty("radius",policy.radius());p.addProperty("blocks",policy.blocks());p.addProperty("reason",policy.reason());policies.add(p);detail.addProperty("terrain",policy.mode().name());}
               techniques.add(detail);
            }
            row.addProperty("complete_zoan_cycle",ZoanForms.handles(stack));
            row.addProperty("transformable",stack.is(TagKey.create(Registries.ITEM,Identifier.fromNamespaceAndPath("minepiece","transformable"))));
            row.add("techniques",techniques);fruits.add(row);
         }
         if(fruits.size()!=40||count!=213)errors.add("Catalogue totals "+fruits.size()+"/"+count);
         report.addProperty("fruit_count",fruits.size());report.addProperty("technique_count",count);
         report.add("fruits",fruits);report.add("terrain_policies",policies);report.add("errors",errors);
         report.addProperty("available_animations",TechniqueAnimations.disponibles().size());
         try {Files.writeString(Path.of("catalogue-audit.local.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}
         catch(java.io.IOException error){throw new RuntimeException(error);}
         org.slf4j.LoggerFactory.getLogger("CatalogueAudit").info("[catalogue-audit] fruits={} techniques={} errors={}",fruits.size(),count,errors.size());
      });
   }
}
