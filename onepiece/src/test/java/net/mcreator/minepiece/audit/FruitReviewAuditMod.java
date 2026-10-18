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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.*;

/** Separate laboratory entrypoint; never casts attacks or ships in the playing mod. */
public final class FruitReviewAuditMod implements ModInitializer {
   @Override public void onInitialize() {
      new CatalogueAuditMod().onInitialize();
      ServerLifecycleEvents.SERVER_STARTED.register(server -> {
         var player=FakePlayer.get(server.overworld(),new GameProfile(
            UUID.nameUUIDFromBytes("FruitReviewFallback".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"FruitReviewFallback"));
         MinepieceModVariables.get(player).PlayerDevilFruit=ItemStack.EMPTY;
         var report=new JsonObject();var checks=new JsonObject();var names=new JsonArray();
         var move=DevilFruitMove.melee("review_unmapped_melee","Review melee",1,3,0,0,1,null);
         boolean bare=true,sword=true;
         for(int i=0;i<32;i++) {
            player.setItemSlot(EquipmentSlot.MAINHAND,ItemStack.EMPTY);
            String name=TechniqueAnimations.nameFor(move,player);
            bare&=!name.isEmpty()&&TechniqueAnimations.fichierPresent(name);names.add(name);
            player.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.IRON_SWORD));
            name=TechniqueAnimations.nameFor(move,player);
            sword&=!name.isEmpty()&&TechniqueAnimations.fichierPresent(name);names.add(name);
         }
         checks.addProperty("generic-melee-bare-animation-delivered",bare);
         checks.addProperty("generic-melee-sword-animation-delivered",sword);
         String swimming=TechniqueAnimations.traduire(-15,0,player);
         checks.addProperty("legacy-swimming-does-not-request-missing-animation",swimming.isEmpty()||TechniqueAnimations.fichierPresent(swimming));
         report.add("checks",checks);report.add("fallback_names",names);report.addProperty("legacy_swimming",swimming);
         report.addProperty("passed",bare&&sword&&(swimming.isEmpty()||TechniqueAnimations.fichierPresent(swimming)));
         try {Files.writeString(Path.of("fruit-review-fallback.local.json"),new GsonBuilder().setPrettyPrinting().create().toJson(report));}
         catch(java.io.IOException error){throw new RuntimeException(error);}
      });
   }
}
