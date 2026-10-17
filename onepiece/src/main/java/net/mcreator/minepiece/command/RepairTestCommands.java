package net.mcreator.minepiece.command;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.CommandDispatcher;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.fabricmc.fabric.api.entity.FakePlayer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.init.MinepieceModItems;
import net.mcreator.minepiece.network.MinepieceModVariables;
import net.mcreator.minepiece.procedures.CombatSkillLogic;
import net.mcreator.minepiece.procedures.DevilFruitRegistry;
import net.mcreator.minepiece.procedures.DevilFruitSkillDispatcher;
import net.mcreator.minepiece.procedures.ElementType;
import net.mcreator.minepiece.procedures.DevilFruitTransformation;
import net.mcreator.minepiece.procedures.GiantForm;
import net.mcreator.minepiece.procedures.CastContext;
import net.mcreator.minepiece.procedures.ZoanClaw;
import net.mcreator.minepiece.procedures.RikiForce;
import net.mcreator.minepiece.procedures.MaterialBatchTechniques;
import net.mcreator.minepiece.procedures.DevilFruitPowerLogic;
import net.mcreator.minepiece.procedures.DevilFruitElement;
import net.mcreator.minepiece.procedures.DiamondBody;
import net.mcreator.minepiece.procedures.IceDefense;
import net.mcreator.minepiece.procedures.ZoanForms;
import net.mcreator.minepiece.procedures.RemoteControl;
import net.mcreator.minepiece.procedures.SupaBlades;
import net.mcreator.minepiece.procedures.UtaDomain;
import net.mcreator.minepiece.procedures.ChainLightning;
import net.mcreator.minepiece.procedures.OpeRoom;
import br.com.oneminepiece.opworldlike.daibutsu.DevilFruitDaibutsuConfig;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Private regression command. FakePlayer is ONLY the caster; ordinary mobs receive all damage. */
public final class RepairTestCommands {
   private static final Logger LOG = LoggerFactory.getLogger("OnePieceRepairTest");
   private static final List<Mob> MOBS = new ArrayList<>();
   private static ServerLevel world;
   private static ServerPlayer caster;
   private static Mob projectileNormal, projectileRefused;
   private static ServerPlayer comboCaster, cancelledCaster;
   private static Mob comboVictim, cancelledVictim;
   private static double comboExpectedHealth, cancelledExpectedHealth;
   private static Mob ghostVictim, beyondRange;
   private static net.mcreator.minepiece.entity.SkillProjectileEntity plainProjectile, ghostProjectile;
   private static long started;
   private static int passed, failed;
   private static boolean active;

   private RepairTestCommands() {}

   public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
      // Never expose destructive arena fixtures in a player's ordinary world.
      if (!Boolean.getBoolean("opworldlike.repairTests")) return;
      ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
         if (!Boolean.getBoolean("opworldlike.visualLab")) return;
         ServerPlayer player = handler.player;
         if(java.util.Set.of("mera","magu","goro","amaru","batch01","batch02","batch03","batch04","batch05","batch06","batch07","batch08").contains(System.getProperty("opworldlike.visualScenario","foundation"))) {
            if(player.getName().getString().equals("FruitWitness")) {
               player.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
               player.connection.teleport(23,106,3,46,12);
            }
            if(player.getName().getString().equals("FruitWitness")) return;
         }
         player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
         player.teleportTo(8.5, 100, 8.5);
         var vars = MinepieceModVariables.get(player);
         vars.PlayerDevilFruit = new ItemStack("mera".equals(System.getProperty("opworldlike.visualScenario")) ? MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA : "magu".equals(System.getProperty("opworldlike.visualScenario")) ? MinepieceModItems.ITEM_DEVIL_FRUIT_MAGU_MAGU : java.util.Set.of("goro","amaru").contains(System.getProperty("opworldlike.visualScenario","")) ? MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO : MinepieceModItems.ITEM_DEVIL_FRUIT_NEKO_NEKO_LEOPARD);
         vars.UseDevilAbilities = true; vars.PlayerEnergy = vars.PlayerEnergyMAX = 2000;
         if(!java.util.Set.of("mera","magu","goro","amaru","batch01","batch02","batch03","batch04","batch05","batch06","batch07","batch08").contains(System.getProperty("opworldlike.visualScenario","foundation"))) DevilFruitTransformation.toggle(player);
         vars.syncPlayerVariables(player);
         if("goro".equals(System.getProperty("opworldlike.visualScenario")) && !Boolean.getBoolean("opworldlike.visualCapture")) {
            visualScene(server.createCommandSourceStack().withEntity(player),"goro_goro","vari");
            player.sendSystemMessage(Component.literal("Test Goro : maintenir/relacher R pour Vari, El Thor, Sango, Kari ; appuyer pour orage, blink, Raigo. B suivante ; Maj = blink arriere ; F5 camera."),false);return;
         }
         if("magu".equals(System.getProperty("opworldlike.visualScenario")) && !Boolean.getBoolean("opworldlike.visualCapture")) {
            visualScene(server.createCommandSourceStack().withEntity(player),"magu_magu","daifunka");
            player.sendSystemMessage(Component.literal("Test Magu : maintenir R puis relacher ; B technique suivante ; F5 camera. Monde neuf."),false);return;
         }
         if("mera".equals(System.getProperty("opworldlike.visualScenario")) && !Boolean.getBoolean("opworldlike.visualCapture")) {
            visualScene(server.createCommandSourceStack().withEntity(player),"mera_mera","hiken");
            player.sendSystemMessage(Component.literal("Test Mera : maintenir R puis relacher ; B technique suivante ; V propulsion ; F5 camera. Monde de test neuf."),false);
            return;
         }
         for (int x : new int[]{8, 12, 16}) {
            var target = EntityTypes.HUSK.spawn(server.overworld(), new BlockPos(x, 100, 16), EntitySpawnReason.COMMAND);
            if (target != null) {
               target.setNoAi(true); target.setBaby(false);
               target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200); target.setHealth(200);
            }
         }
         player.sendSystemMessage(Component.literal("Laboratoire One Piece : R technique, B suivante, V forme, F5 vue. Monde jetable ; progression de test."), false);
      });
      dispatcher.register(Commands.literal("dbatchwitness").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(Commands.argument("tick",com.mojang.brigadier.arguments.IntegerArgumentType.integer(0)).executes(c->{
         if(!Boolean.getBoolean("opworldlike.visualCapture")||c.getSource().getPlayer()==null||!c.getSource().getPlayer().getName().getString().equals("FruitWitness"))return 0;
         int clock=com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c,"tick");for(var p:c.getSource().getServer().getPlayerList().getPlayers())net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(p,new net.mcreator.minepiece.network.DevilFruitNetworking.LabScenePayload("witness-clock:"+clock));return 1;
      })));
      dispatcher.register(Commands.literal("dbatchclock").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(Commands.argument("tick",com.mojang.brigadier.arguments.IntegerArgumentType.integer(0)).executes(c->{
         if(!java.util.Set.of("batch01","batch02","batch03","batch04","batch05","batch06","batch07","batch08").contains(System.getProperty("opworldlike.visualScenario","")))return 0;
         int clock=com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c,"tick");for(var p:c.getSource().getServer().getPlayerList().getPlayers())net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(p,new net.mcreator.minepiece.network.DevilFruitNetworking.LabScenePayload("batch-clock:"+clock));return 1;
      })));
      dispatcher.register(Commands.literal("dmaterialreturn").requires(source->Boolean.getBoolean("opworldlike.visualCapture") && "batch03".equals(System.getProperty("opworldlike.visualScenario")) && source.getPlayer()!=null && source.getPlayer().getName().getString().equals("FruitWitness")).executes(c->{
         var witness=c.getSource().getPlayer();witness.connection.teleport(27,108,20,90,18);
         for(var actor:witness.level().getServer().getPlayerList().getPlayers())if(actor.getName().getString().equals("FruitLab"))net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(actor,new net.mcreator.minepiece.network.DevilFruitNetworking.LabScenePayload("material-returned"));return 1;
      }));
      dispatcher.register(Commands.literal("dmaterialstep").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(Commands.argument("stage",com.mojang.brigadier.arguments.StringArgumentType.word()).executes(c->{
         if(!"batch03".equals(System.getProperty("opworldlike.visualScenario")) || !Boolean.getBoolean("opworldlike.visualCapture"))return 0;
         var p=c.getSource().getPlayer();if(p==null || !p.getName().getString().equals("FruitLab"))return 0;
         String stage=com.mojang.brigadier.arguments.StringArgumentType.getString(c,"stage");
         for(var observer:p.level().getServer().getPlayerList().getPlayers())if(observer.getName().getString().equals("FruitWitness")){
            if(stage.equals("away"))observer.connection.teleport(-350,108,20,90,18);
            if(stage.equals("back"))observer.connection.teleport(27,108,20,90,18);
         }
         if(stage.equals("damage")){
            var level=(ServerLevel)p.level();p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getAbilities().invulnerable=false;p.setPermanentlyInvulnerable(false);p.removeAllEffects();
            for(var slot:net.minecraft.world.entity.EquipmentSlot.values())if(slot.getType()==net.minecraft.world.entity.EquipmentSlot.Type.HUMANOID_ARMOR)p.setItemSlot(slot,ItemStack.EMPTY);
            var armor=p.getAttribute(Attributes.ARMOR);var tough=p.getAttribute(Attributes.ARMOR_TOUGHNESS);
            var armorId=net.minecraft.resources.Identifier.fromNamespaceAndPath("minepiece","diamond_body_armor");var toughId=net.minecraft.resources.Identifier.fromNamespaceAndPath("minepiece","diamond_body_toughness");
            var coatArmor=armor.getModifier(armorId);var coatTough=tough.getModifier(toughId);armor.removeModifier(armorId);tough.removeModifier(toughId);
            var attacker=EntityTypes.HUSK.spawn(level,p.blockPosition().offset(4,0,0),EntitySpawnReason.COMMAND);if(attacker==null)return 0;attacker.setNoAi(true);
            p.setHealth(20);p.damageCooldownTime=0;p.setInvulnerableTime(0);p.hurtServer(level,level.damageSources().mobAttack(attacker),8);float baseline=20-p.getHealth();
            if(coatArmor!=null)armor.addTransientModifier(coatArmor);if(coatTough!=null)tough.addTransientModifier(coatTough);
            p.setHealth(20);p.damageCooldownTime=0;p.setInvulnerableTime(0);p.hurtServer(level,level.damageSources().mobAttack(attacker),8);attacker.discard();float loss=20-p.getHealth();double cap=br.com.oneminepiece.opworldlike.kira.DevilFruitKiraConfig.maxReductionPercent();
            org.slf4j.LoggerFactory.getLogger("B03Live").info("[batch03-native] diamond-reduction {} loss={} baseline={} cap={} armor={}",DiamondBody.isActive(p) && coatArmor!=null && baseline>0 && loss>0 && loss<baseline && (baseline-loss)/baseline*100<=cap+.001?"PASS":"FAIL",loss,baseline,cap,armor.getValue());
            p.setHealth(20);p.damageCooldownTime=0;p.setInvulnerableTime(0);p.hurtServer(level,level.damageSources().drown(),4);float bypass=20-p.getHealth();
            org.slf4j.LoggerFactory.getLogger("B03Live").info("[batch03-native] diamond-armor-bypass {} loss={}",Math.abs(bypass-4)<.001?"PASS":"FAIL",bypass);
            p.setHealth(20);p.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
         }return 1;
      })));
      dispatcher.register(Commands.literal("dmeraready").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(ctx->{
         if(!Boolean.getBoolean("opworldlike.visualLab")) return 0;
         for(ServerPlayer actor:ctx.getSource().getServer().getPlayerList().getPlayers())
            if(java.util.Set.of("FruitLab","FruitWitness").contains(actor.getName().getString())) net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(actor,new net.mcreator.minepiece.network.DevilFruitNetworking.LabScenePayload("ready"));
         return 1;
      }));
      dispatcher.register(Commands.literal("drepair")
         .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
         .executes(ctx -> start(ctx.getSource())));
      dispatcher.register(Commands.literal("dvisual")
         .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
         .then(Commands.argument("fruit", com.mojang.brigadier.arguments.StringArgumentType.word())
            .then(Commands.argument("technique", com.mojang.brigadier.arguments.StringArgumentType.word())
               .executes(ctx -> visualScene(ctx.getSource(),
                  com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "fruit"),
                  com.mojang.brigadier.arguments.StringArgumentType.getString(ctx, "technique"))))));
      ServerTickEvents.END_SERVER_TICK.register(server -> {
         if (!active || world == null || world.getServer() != server) return;
         if (world.getGameTime() - started >= 50) {
            try {
               check("projectile-hurts-normal", projectileNormal.getHealth() < 10.0F,
                  "health=" + projectileNormal.getHealth());
               check("projectile-effect-normal", projectileNormal.hasEffect(MobEffects.SLOWNESS), "accepted target status");
               check("projectile-refusal-no-health", projectileRefused.getHealth() == 10.0F,
                  "health=" + projectileRefused.getHealth());
               check("projectile-refusal-no-effect", !projectileRefused.hasEffect(MobEffects.SLOWNESS), "refused target status");
               check("projectile-refusal-no-displacement", projectileRefused.position().distanceTo(new Vec3(16.5, 100, 12.5)) < .01,
                  "position=" + projectileRefused.position());
               check("real-server-ticks", world.getGameTime() - started >= 50, "elapsed=" + (world.getGameTime() - started));
               check("combo-delayed-total-damage", Math.abs(comboVictim.getHealth() - comboExpectedHealth) < .001,
                  "health=" + comboVictim.getHealth() + " expected=" + comboExpectedHealth);
               check("combo-completes", !net.mcreator.minepiece.procedures.ZoanTechniqueEpisodes.active(comboCaster), "bounded pending hits");
               check("combo-interrupt-fruit-change", cancelledVictim.getHealth() == cancelledExpectedHealth
                  && !net.mcreator.minepiece.procedures.ZoanTechniqueEpisodes.active(cancelledCaster), "fruit change cancels pending damage");
               check("ghost-crosses-solid-wall", ghostVictim.getHealth() < 10, "health behind wall=" + ghostVictim.getHealth());
               check("ray-range-no-overshoot", beyondRange.getHealth() == 10, "health beyond explicit range=" + beyondRange.getHealth());
               check("physical-projectile-no-fireball-flame", !plainProjectile.isOnFire(), "physical skill doesn't burn itself");
               check("ghost-no-fireball-flame", !ghostProjectile.isOnFire(), "ghost doesn't burn itself");
            } catch (Throwable problem) {
               check("async-exception", false, problem.toString());
            } finally {
               cleanup();
               LOG.info("[drepair] END passed={} failed={} total={}", passed, failed, passed + failed);
            }
         }
      });
   }

   private static int visualScene(CommandSourceStack source, String fruitName, String technique) {
      if (!Boolean.getBoolean("opworldlike.visualLab")) return 0;
      var player = source.getPlayer();
      if (player == null || !player.getName().getString().equals("FruitLab")) return 0;
      var id = net.minecraft.resources.Identifier.tryParse("minepiece:item_devil_fruit_" + fruitName);
      if (id == null || !net.minecraft.core.registries.BuiltInRegistries.ITEM.containsKey(id)) return 0;
      ItemStack fruit = new ItemStack(net.minecraft.core.registries.BuiltInRegistries.ITEM.getValue(id));
      var moves = DevilFruitRegistry.movesFor(fruit.getItem());
      int index = -1;
      for (int i = 0; i < moves.size(); i++) if (moves.get(i).id().equals(technique)) index = i;
      if (index < 0 && !technique.equals("propulsion") && !technique.equals("forms")) return 0;
      if(index<0) index=0;
      DevilFruitPowerLogic.resetDevilFruitPowers(player);
      var vars = MinepieceModVariables.get(player);
      vars.PlayerDevilFruit = fruit; vars.PlayerTechniqueUsedNumber = index;
      vars.PlayerEnergy = vars.PlayerEnergyMAX = 2000; vars.UseDevilAbilities = true;
      player.getCooldowns().removeCooldown(player.getCooldowns().getCooldownGroup(fruit));
      player.teleportTo(8.5, 100, 8.5); player.setYRot(0); player.setXRot(0);
      player.connection.teleport(8.5, 100, 8.5, 0, 0);
      if(fruitName.equals("mera_mera") || fruitName.equals("magu_magu") || fruitName.equals("goro_goro") || java.util.Set.of("hie_hie","pika_pika","moku_moku","suna_suna","yami_yami","mochi_mochi","mori_mori","kira_kira","supa_supa","riki_riki","gura_gura","bomu_bomu","zushi_zushi","nikyu_nikyu","ope_ope","aro_aro","horo_horo","mero_mero","kiro_kiro","bisu_bisu","kage_kage","soru_soru","uta_uta","imu","gomu_gomu","inu_inu_wolf","neko_neko_leopard","neko_neko_sabertiger","ushi_ushi_giraffe","batto_batto","inu_inu_okuchi_no_makami","hito_hito_daibutsu","tori_tori_phoenix","uo_uo_seiryu","ryu_ryu_pteranodon","ryu_ryu_brachiosaurus","ryu_ryu_pachycephalosaurus").contains(fruitName)) {
         player.setGameMode(technique.equals("propulsion")?net.minecraft.world.level.GameType.SURVIVAL:net.minecraft.world.level.GameType.CREATIVE);
         player.getAbilities().flying=false;player.onUpdateAbilities();
         player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,new ItemStack(net.minecraft.world.item.Items.IRON_SWORD));
         player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND,new ItemStack(net.minecraft.world.item.Items.SHIELD));
         player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST,new ItemStack(net.minecraft.world.item.Items.IRON_CHESTPLATE));
         for(var entity:player.level().getEntitiesOfClass(net.minecraft.world.entity.Mob.class,new net.minecraft.world.phys.AABB(-8,98,-8,64,116,64))) entity.discard();
         for(int x=-6;x<44;x++) for(int z=-6;z<44;z++) for(int y=94;y<=99;y++) ((ServerLevel)player.level()).setBlockAndUpdate(new BlockPos(x,y,z),Blocks.STONE.defaultBlockState());
         // Explicit laboratory reconstruction, never a power rollback. Permanent blocks from
         // the preceding technique must not obstruct the next independent visual fixture.
         for(int x=-6;x<44;x++)for(int z=-6;z<44;z++)for(int y=100;y<116;y++)((ServerLevel)player.level()).setBlockAndUpdate(new BlockPos(x,y,z),Blocks.AIR.defaultBlockState());
         int targetZ=fruitName.equals("goro_goro") ? (technique.equals("vari")?35:technique.equals("raigo")?30:14) : java.util.Set.of("hiken","higan","daienkai","daifunka","inugamiguren","ryuseikazan").contains(technique)?32:technique.equals("kageromagumagu")?25:technique.equals("hotarubi")?20:14;
         if("batch01".equals(System.getProperty("opworldlike.visualScenario"))){
            targetZ=java.util.Set.of("icepartisan","pheasantbeak","laser1","laser2","magatama","whiteblow","whitevine","desertspada","desertlaspada","darkball","darkmatter").contains(technique)?30:14;
            player.getInventory().clearContent();if(technique.equals("amanomurakumo"))targetZ=10;
            for(var witness:source.getServer().getPlayerList().getPlayers())if(witness.getName().getString().equals("FruitWitness"))witness.connection.teleport(27,108,20,90,18);
         }
         if("batch08".equals(System.getProperty("opworldlike.visualScenario"))){
            targetZ=technique.equals("forms")?32:technique.equals("ulmeteor")?20:java.util.Set.of("blackcofee","brachiolauncher","sparkingqueen").contains(technique)?22:java.util.Set.of("brachiobober","attackjump").contains(technique)?15:11;
            player.getInventory().clearContent();
            for(var witness:source.getServer().getPlayerList().getPlayers())if(witness.getName().getString().equals("FruitWitness")){witness.connection.teleport(22.5,106,11.5,90,16);witness.getAbilities().mayfly=true;witness.getAbilities().flying=true;witness.onUpdateAbilities();}
         }
         if("batch07".equals(System.getProperty("opworldlike.visualScenario"))){
            targetZ=technique.equals("forms")?30:java.util.Set.of("bluebird","narikabura","impactball","kongokabura","kaifu","barizodon").contains(technique)?20:java.util.Set.of("himorogiri","kosanze","hooin").contains(technique)?15:11;
            player.getInventory().clearContent();
            for(var witness:source.getServer().getPlayerList().getPlayers())if(witness.getName().getString().equals("FruitWitness")){witness.connection.teleport(22.5,106,11.5,90,16);witness.getAbilities().mayfly=true;witness.getAbilities().flying=true;witness.onUpdateAbilities();}
         }
         if("batch06".equals(System.getProperty("opworldlike.visualScenario"))){
            targetZ=technique.equals("forms")?26:java.util.Set.of("rankyaku","lupusfall","gaicho","gagan","localareanetwork","rankyakuran","tobushigan").contains(technique)?20:technique.equals("attackjump")?15:11;
            player.getInventory().clearContent();
            for(var witness:source.getServer().getPlayerList().getPlayers())if(witness.getName().getString().equals("FruitWitness"))witness.connection.teleport(16.5,102.5,11.5,90,16);
         }
         if("batch05".equals(System.getProperty("opworldlike.visualScenario"))){
            targetZ=technique.equals("gear_change")?26:java.util.Set.of("brickbat","noteattack","notelaser","ikoku").contains(technique)?20:12;player.getInventory().clearContent();
            for(var witness:source.getServer().getPlayerList().getPlayers())if(witness.getName().getString().equals("FruitWitness"))witness.connection.teleport(18.5,103,14,90,20);
         }
         if("batch04".equals(System.getProperty("opworldlike.visualScenario"))){
            targetZ=java.util.Set.of("arrow","negativehollow","tokuhoro","pistolkiss").contains(technique)?20:12;
            player.getInventory().clearContent();
            for(var witness:source.getServer().getPlayerList().getPlayers())if(witness.getName().getString().equals("FruitWitness"))witness.connection.teleport(18.5,103,14,90,20);
         }
         if("batch03".equals(System.getProperty("opworldlike.visualScenario"))){
            targetZ=java.util.Set.of("yakimochi","root","rootgiant","diamondshard","diamondavalanche","projectileslash","stonethrowing","stonethrowinglarge").contains(technique)?26:technique.equals("mochibuild")?16:12;
            player.getInventory().clearContent();
            for(var witness:source.getServer().getPlayerList().getPlayers())if(witness.getName().getString().equals("FruitWitness"))witness.connection.teleport(27,108,20,90,18);
         }
         if("batch02".equals(System.getProperty("opworldlike.visualScenario"))){
            targetZ=technique.equals("meteor")?26:technique.equals("meteorsmall")?20:technique.equals("bomboverhead")?11:java.util.Set.of("nosefancycannon","padho","ursusshock","rubble","stoneopeope").contains(technique)?26:technique.equals("amputation")?12:14;
            player.getInventory().clearContent();
            if(java.util.Set.of("shambles","amputation").contains(technique))OpeRoom.open(player);
            for(var witness:source.getServer().getPlayerList().getPlayers())if(witness.getName().getString().equals("FruitWitness"))witness.connection.teleport(27,108,20,90,18);
         }
         for(int x:new int[]{8,11,14}) {
            net.minecraft.world.entity.Mob target=(java.util.Set.of("batch04","batch05","batch06","batch07","batch08").contains(System.getProperty("opworldlike.visualScenario"))?net.minecraft.world.entity.EntityTypes.CREEPER:net.minecraft.world.entity.EntityTypes.HUSK).spawn((ServerLevel)player.level(),new BlockPos(x,100,targetZ),net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            if(target!=null) {target.setNoAi(true);target.setNoGravity(true);if(target instanceof net.minecraft.world.entity.monster.zombie.Zombie zombie)zombie.setBaby(false);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);target.setHealth(500);target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);if("batch01".equals(System.getProperty("opworldlike.visualScenario")) && java.util.Set.of("kurouzu","blackhole","whiteout").contains(technique)){target.setNoAi(false);target.removeFreeWill();target.getNavigation().stop();target.setTarget(null);target.setSpeed(0);}}
         }
         if(technique.equals("propulsion")) player.connection.teleport(8.5,102,8.5,0,-10);
         for(ServerPlayer labPlayer:source.getServer().getPlayerList().getPlayers())
            if(Boolean.getBoolean("opworldlike.visualCapture") && java.util.Set.of("FruitLab","FruitWitness").contains(labPlayer.getName().getString()))
               net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(labPlayer,new net.mcreator.minepiece.network.DevilFruitNetworking.LabScenePayload(technique));
      }
      vars.syncPlayerVariables(player);
      LOG.info("[visual-lab] scene fruit={} technique={}", fruitName, technique);
      return 1;
   }

   private static void check(String id, boolean ok, String detail) {
      if (ok) passed++; else failed++;
      LOG.info("[drepair] [{}] {} | {}", ok ? "PASS" : "FAIL", id, detail);
   }

   private static int start(CommandSourceStack source) {
      if (active) { source.sendFailure(Component.literal("A repair test is already running.")); return 0; }
      passed = failed = 0;
      world = source.getServer().overworld();
      try {
         for (int x = 0; x <= 24; x++) for (int z = 0; z <= 24; z++) {
            world.setBlockAndUpdate(new BlockPos(x, 99, z), Blocks.STONE.defaultBlockState());
            for (int y = 100; y <= 104; y++) world.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
         }
         caster = FakePlayer.get(world, new GameProfile(UUID.nameUUIDFromBytes("drepair-caster".getBytes(StandardCharsets.UTF_8)), "repair_caster"));
         MinepieceModVariables.clear(caster);
         var vars = MinepieceModVariables.get(caster);
         vars.PlayerDevilFruit = new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA);
         vars.UseDevilAbilities = true;
         vars.PlayerEnergy = vars.PlayerEnergyMAX = 1000;
         caster.teleportTo(8.5, 100, 8.5);
         caster.setYRot(0); caster.setXRot(0);
         Mob immune = mob(8.5, 11.5, true);
         Vec3 before = immune.getDeltaMovement();
         int hits = CombatSkillLogic.meleeStrike(caster, 3, 5, .8, MobEffects.SLOWNESS, 100, 0);
         check("melee-refusal-hit-count", hits == 0, "hits=" + hits);
         check("melee-refusal-health", immune.getHealth() == 10, "health=" + immune.getHealth());
         check("melee-refusal-status", !immune.hasEffect(MobEffects.SLOWNESS), "no slowness");
         check("melee-refusal-element", !immune.isOnFire(), "no ignition");
         check("melee-refusal-recoil", immune.getDeltaMovement().equals(before), "velocity=" + immune.getDeltaMovement());
         immune.discard();
         // Cardinal directions catch argument/sign mistakes hidden by a single look vector.
         for (int i = 0; i < 4; i++) {
            MinepieceModVariables.get(caster).PlayerDevilFruit = new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_NEKO_NEKO_LEOPARD);
            caster.setYRot(i * 90); caster.setXRot(0);
            Vec3 direction = caster.getLookAngle();
            Mob victim = mob(caster.getX() + direction.x * 3, caster.getZ() + direction.z * 3, false);
            CombatSkillLogic.meleeStrike(caster, 3, 5, .8);
            check("melee-health-" + i, victim.getHealth() < 10, "health=" + victim.getHealth());
            Vec3 movement = victim.getDeltaMovement();
            check("melee-recoil-away-" + i, movement.x * direction.x + movement.z * direction.z > .05,
               "direction=" + direction + " velocity=" + movement);
            victim.discard();
         }
         var fruit = new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_NEKO_NEKO_LEOPARD);
         vars.PlayerDevilFruit = fruit; vars.PlayerTechniqueUsedNumber = 0; vars.PlayerEnergy = 1000;
         caster.getCooldowns().removeCooldown(caster.getCooldowns().getCooldownGroup(fruit));
         var move = DevilFruitRegistry.movesFor(fruit.getItem()).get(0);
         boolean first = DevilFruitSkillDispatcher.castSkill(caster);
         double energy = vars.PlayerEnergy;
         boolean second = DevilFruitSkillDispatcher.castSkill(caster);
         check("cast-once-cost", first && Math.abs(energy - (1000 - move.energyCost())) < .001, "energy=" + energy);
         check("cast-spam-refused", !second && vars.PlayerEnergy == energy, "second=" + second + " energy=" + vars.PlayerEnergy);
         caster.getCooldowns().removeCooldown(caster.getCooldowns().getCooldownGroup(fruit));
         vars.PlayerEnergy = 0;
         check("cast-no-energy-refused", !DevilFruitSkillDispatcher.castSkill(caster) && vars.PlayerEnergy == 0, "zero energy");
         testForms();
         testRefusalAndDamageGuard();
         testRikiAndReset();
         testChainLightning();
         vars.PlayerDevilFruit = fruit;
         projectileNormal = mob(8.5, 12.5, false);
         projectileRefused = mob(16.5, 12.5, true);
         caster.setYRot(0); caster.setXRot(12); // cow's body is below the player's eye
         for (int lane = 0; lane < 2; lane++) {
            caster.teleportTo(8.5 + lane * 8, 100, 8.5);
            var projectile = CombatSkillLogic.fireProjectile(caster, 3, .8, MobEffects.SLOWNESS, 100, 0, 30, 1,
               "aroaro_arrow", "aroaro_arrow", null).element(ElementType.PHYSICAL);
            if (lane == 0) plainProjectile = projectile;
         }
         setupComboFixtures();
         setupFlightFixtures();
         started = world.getGameTime(); active = true;
         source.sendSuccess(() -> Component.literal("Repair fixtures armed; wait 50 real server ticks."), false);
         return 1;
      } catch (Throwable problem) {
         check("setup-exception", false, problem.toString());
         cleanup();
         LOG.info("[drepair] END passed={} failed={} total={}", passed, failed, passed + failed);
         return 0;
      }
   }

   private static void testChainLightning() {
      var vars = MinepieceModVariables.get(caster);
      vars.PlayerDevilFruit = new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_GORO_GORO);
      caster.teleportTo(8.5, 100, 8.5);
      var move = DevilFruitRegistry.movesFor(vars.PlayerDevilFruit.getItem()).stream()
         .filter(value -> value.id().equals("sango")).findFirst().orElseThrow();
      Mob immune = mob(8.5, 10.5, true), normal = mob(12.5, 8.5, false), behind = mob(8.5, 13.5, false);
      for (int x = 4; x <= 16; x++) for (int y = 100; y <= 103; y++)
         world.setBlockAndUpdate(new BlockPos(x, y, 11), Blocks.STONE.defaultBlockState());
      var chain = ChainLightning.cast(caster, move);
      check("chain-lightning-positive-hit", normal.getHealth() < 10 && chain.targets().contains(normal), "normal target receives a hit");
      check("chain-lightning-refusal-not-counted", immune.getHealth() == 10 && !chain.targets().contains(immune), "invulnerable target not reported as hit");
      check("chain-lightning-wall-protects", behind.getHealth() == 10 && !chain.targets().contains(behind), "each link respects line of sight");
      immune.discard(); normal.discard(); behind.discard();
      for (int x = 4; x <= 16; x++) for (int y = 100; y <= 103; y++)
         world.setBlockAndUpdate(new BlockPos(x, y, 11), Blocks.AIR.defaultBlockState());
   }

   private static void testRikiAndReset() {
      var vars = MinepieceModVariables.get(caster);
      caster.teleportTo(8.5, 100, 8.5); caster.setYRot(0); caster.setXRot(0);
      vars.PlayerDevilFruit = new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_RIKI_RIKI);
      vars.PlayerEnergy = 1000;
      var moves = DevilFruitRegistry.movesFor(vars.PlayerDevilFruit.getItem());
      int index = -1;
      for (int i = 0; i < moves.size(); i++) if (moves.get(i).id().equals("hadoelbow")) index = i;
      if (index < 0) throw new IllegalStateException("Missing Riki grab move");
      vars.PlayerTechniqueUsedNumber = index;
      caster.getCooldowns().removeCooldown(caster.getCooldowns().getCooldownGroup(vars.PlayerDevilFruit));
      Mob victim = mob(8.5, 11.5, false);
      victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100); victim.setHealth(100);
      boolean cast = DevilFruitSkillDispatcher.castSkill(caster);
      check("riki-hado-charge-safe", cast && MaterialBatchTechniques.activeCount()>0 && victim.getHealth()==100 && RikiForce.held(caster)==null,
         "accepted windup has neither premature damage nor a speculative grab; B03 verifies the real delayed hit");
      double baseArmor = caster.getAttributeValue(Attributes.ARMOR);
      var foreign = net.minecraft.resources.Identifier.fromNamespaceAndPath("repair_fixture", "external_armor");
      caster.getAttribute(Attributes.ARMOR).addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
         foreign, 2, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_VALUE));
      var iceOwner=net.fabricmc.fabric.api.entity.FakePlayer.get(world,new com.mojang.authlib.GameProfile(java.util.UUID.nameUUIDFromBytes("FoundationIceOwner".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"FoundationIceOwner"));
      MinepieceModVariables.get(iceOwner).PlayerDevilFruit=new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_OKUCHI_NO_MAKAMI);iceOwner.setHealth(20);
      boolean diamond = DiamondBody.begin(caster), ice = IceDefense.begin(iceOwner), remote = RemoteControl.begin(caster);
      SupaBlades.activate(caster, 100); SupaBlades.setStance(caster, SupaBlades.Stance.OFFENSIVE);
      UtaDomain.enter(caster, 12);
      check("reset-fixture-active", diamond && ice && remote && UtaDomain.inDomain(caster)
         && SupaBlades.bladesActive(caster), "four caster states plus a correctly attributed Makami defense and pending Hado");
      DevilFruitPowerLogic.resetDevilFruitPowers(caster);
      for(int pendingTick=0;pendingTick<20;pendingTick++)MaterialBatchTechniques.tick();
      check("riki-hado-reset-cancels",victim.getHealth()==100 && MaterialBatchTechniques.activeCount()==0,"reset prevents the accepted pending strike after its release tick");
      DevilFruitPowerLogic.resetDevilFruitPowers(iceOwner);MinepieceModVariables.clear(iceOwner);iceOwner.discard();
      check("reset-owned-states", !DiamondBody.isActive(caster) && !IceDefense.isActive(iceOwner)
         && !RemoteControl.isControlling(caster) && !UtaDomain.inDomain(caster)
         && !SupaBlades.bladesActive(caster) && RikiForce.held(caster) == null, "owned states released");
      check("reset-external-attribute-preserved", caster.getAttribute(Attributes.ARMOR).getModifier(foreign) != null
         && Math.abs(caster.getAttributeValue(Attributes.ARMOR) - baseArmor - 2) < .001, "foreign armor survives");
      check("reset-fruit-empty", vars.PlayerDevilFruit.isEmpty() && vars.player_form == 0, "no remaining fruit state");
      caster.getAttribute(Attributes.ARMOR).removeModifier(foreign); victim.discard();
      check("element-technique-scope", DevilFruitElement.forTechnique(new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_UO_UO_SEIRYU), "borobreath") == ElementType.FIRE
         && DevilFruitElement.forTechnique(new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_NEKO_NEKO_LEOPARD), "borobreath") == ElementType.PHYSICAL,
         "fire breath belongs only to its fruit");
      check("element-light-versus-kick", DevilFruitElement.forTechnique(new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_PIKA_PIKA), "laser1") == ElementType.LIGHT
         && DevilFruitElement.forTechnique(new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_PIKA_PIKA), "kick") == ElementType.PHYSICAL,
         "laser energy differs from a bodily kick");
   }

   private static void setupFlightFixtures() {
      caster.teleportTo(4.5, 100, 8.5); caster.setYRot(0); caster.setXRot(12);
      ghostVictim = mob(4.5, 12.5, false);
      world.setBlockAndUpdate(new BlockPos(4, 100, 10), Blocks.STONE.defaultBlockState());
      world.setBlockAndUpdate(new BlockPos(4, 101, 10), Blocks.STONE.defaultBlockState());
      ghostProjectile = CombatSkillLogic.fireProjectile(caster, 3, 0, null, 0, 0, 30, 1, "ghost1", "ghost1_1", null)
         .element(ElementType.GHOST).phases(true);
      caster.teleportTo(4.5, 100, 16.5);
      beyondRange = mob(4.5, 20.5, false);
      CombatSkillLogic.fireProjectile(caster, 3, 0, null, 0, 0, 30, 1, "aroaro_arrow", "aroaro_arrow", null)
         .element(ElementType.PHYSICAL).range(3);
   }

   private static void setupComboFixtures() {
      comboCaster = FakePlayer.get(world, new GameProfile(UUID.nameUUIDFromBytes("drepair-combo".getBytes(StandardCharsets.UTF_8)), "repair_combo"));
      cancelledCaster = FakePlayer.get(world, new GameProfile(UUID.nameUUIDFromBytes("drepair-cancel".getBytes(StandardCharsets.UTF_8)), "repair_cancel"));
      int lane = 0;
      for (ServerPlayer owner : new ServerPlayer[]{comboCaster, cancelledCaster}) {
         MinepieceModVariables.clear(owner);
         var vars = MinepieceModVariables.get(owner);
         vars.PlayerDevilFruit = new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_NEKO_NEKO_SABERTIGER);
         vars.PlayerEnergy = vars.PlayerEnergyMAX = 1000;
         vars.UseDevilAbilities = true;
         var moves = DevilFruitRegistry.movesFor(vars.PlayerDevilFruit.getItem());
         int index = -1;
         for (int i = 0; i < moves.size(); i++) if (moves.get(i).id().equals("shiganmadara")) index = i;
         if (index < 0) throw new IllegalStateException("Missing combo fixture move");
         vars.PlayerTechniqueUsedNumber = index;
         owner.getCooldowns().removeCooldown(owner.getCooldowns().getCooldownGroup(vars.PlayerDevilFruit));
         owner.teleportTo(22.5, 100, 4.5 + lane * 8); owner.setYRot(0); owner.setXRot(0);
         Mob victim = mob(22.5, 7.5 + lane * 8, false);
         victim.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
         victim.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1);
         victim.setHealth(100);
         boolean cast = DevilFruitSkillDispatcher.castSkill(owner);
         if (lane == 0) {
            comboVictim = victim;
            comboExpectedHealth = 100 - moves.get(index).damage();
            double energy = vars.PlayerEnergy;
            check("combo-starts-with-windup", cast && victim.getHealth() == 100 && net.mcreator.minepiece.procedures.ZoanTechniqueEpisodes.active(owner), "native preparation before three spaced contacts");
            check("combo-cost-once-and-spam", Math.abs(energy - (1000 - moves.get(index).energyCost())) < .001
               && !DevilFruitSkillDispatcher.castSkill(owner) && vars.PlayerEnergy == energy, "one debit for entire combo");
         } else {
            cancelledVictim = victim;
            cancelledExpectedHealth = victim.getHealth();
            check("combo-cancel-during-windup", cast && cancelledExpectedHealth == 100 && net.mcreator.minepiece.procedures.ZoanTechniqueEpisodes.active(owner), "fruit changes before first contact");
            vars.PlayerDevilFruit = new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_MERA_MERA);
         }
         lane++;
      }
   }

   private static void testForms() {
      var vars = MinepieceModVariables.get(caster);
      caster.teleportTo(8.5, 100, 8.5);
      double baseSpeed = caster.getAttributeValue(Attributes.MOVEMENT_SPEED);
      double baseDamage = caster.getAttributeValue(Attributes.ATTACK_DAMAGE);
      vars.PlayerDevilFruit = new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_NEKO_NEKO_LEOPARD);
      vars.PlayerEnergy = 1000;
      check("form-toggle", DevilFruitTransformation.toggle(caster) && vars.player_form == 1, "leopard hybrid");
      check("form-speed-percentage", Math.abs(caster.getAttributeValue(Attributes.MOVEMENT_SPEED) / baseSpeed - 1.2) < .001,
         "speed ratio=" + caster.getAttributeValue(Attributes.MOVEMENT_SPEED) / baseSpeed);
      double damage = caster.getAttributeValue(Attributes.ATTACK_DAMAGE);
      DevilFruitTransformation.restorePersistedForm(caster);
      DevilFruitTransformation.restorePersistedForm(caster);
      check("form-restore-idempotent", vars.player_form == 0 && caster.getAttributeValue(Attributes.ATTACK_DAMAGE) == baseDamage,
         "transient Zoan forms clear on login; repeated reconciliation stays at base");
      DevilFruitTransformation.revertForm(caster);
      check("form-revert-attributes", caster.getScale() == 1 && caster.getAttributeValue(Attributes.ATTACK_DAMAGE) == baseDamage
         && Math.abs(caster.getAttributeValue(Attributes.MOVEMENT_SPEED) - baseSpeed) < .00001, "base attributes restored");
      vars.PlayerDevilFruit = new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_HITO_HITO_DAIBUTSU);
      check("giant-toggle-budget", DevilFruitTransformation.toggle(caster) && ZoanForms.has(caster)
         && vars.formRemainingTicks > 0, "giant maintained");
      check("giant-volume-absolute-scale", Math.abs(GiantForm.requiredVolume(caster, caster.getScale()).getYsize()
         - caster.getBoundingBox().getYsize()) < .00001, "scaled avatar must not be scaled twice");
      double energy = vars.PlayerEnergy;
      for (int i = 0; i < 20; i++) ZoanForms.tick();
      check("giant-maintenance-one-cost", Math.abs((energy - vars.PlayerEnergy) - DevilFruitDaibutsuConfig.maintenanceCostPerSecond()) < .001,
         "20 service ticks cost=" + (energy - vars.PlayerEnergy));
      int remaining = vars.formRemainingTicks;
      var copy = new MinepieceModVariables.PlayerVariables();
      copy.loadNbt(vars.writeNbt());
      check("giant-budget-nbt", copy.formRemainingTicks == remaining, "timer serialized");
      DevilFruitTransformation.restorePersistedForm(caster);
      check("giant-restore-budget", !ZoanForms.has(caster)&&vars.formRemainingTicks==0&&vars.player_form==0, "transient giant ends on login; timer never refilled");
      vars.formRemainingTicks = 2;
      DevilFruitTransformation.restorePersistedForm(caster);
      ZoanForms.tick(); ZoanForms.tick();
      check("giant-expiry-reverts-body", vars.player_form == 0 && caster.getScale() == 1 && !GiantForm.isGiant(caster),
         "timer expiry removes shared body modifiers");
   }

   private static void testRefusalAndDamageGuard() {
      var vars = MinepieceModVariables.get(caster);
      vars.PlayerDevilFruit = new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_WOLF);
      vars.PlayerEnergy = 1000;
      var moves = DevilFruitRegistry.movesFor(vars.PlayerDevilFruit.getItem());
      for (int i = 0; i < moves.size(); i++) if (moves.get(i).id().equals("poursuite")) vars.PlayerTechniqueUsedNumber = i;
      caster.setYRot(0); caster.setXRot(0);
      caster.getCooldowns().removeCooldown(caster.getCooldowns().getCooldownGroup(vars.PlayerDevilFruit));
      Mob behind = mob(8.5, 5.5, false);
      boolean cast = DevilFruitSkillDispatcher.castSkill(caster);
      check("custom-refusal-no-fallback", !cast && vars.PlayerEnergy == 1000 && behind.getHealth() == 10,
         "target behind cannot trigger pursuit or generic attack");
      check("custom-refusal-no-cooldown", !caster.getCooldowns().isOnCooldown(vars.PlayerDevilFruit), "no cost on refusal");
      check("cast-context-cleaned", CastContext.current() == null, "scoped identity cleared after refusal");
      behind.discard();
      var source = CombatSkillLogic.elementalDamageSource(world, ElementType.PHYSICAL, caster, null);
      int killed = 0;
      for (int i = 0; i < 12; i++) {
         Mob victim = mob(20.5, 20.5, false);
         if (CombatSkillLogic.hurtAndConfirm(victim, world, source, 100) && !victim.isAlive()) killed++;
         victim.discard();
      }
      check("damage-guard-sequential-lethal", killed == 12, "independent lethal hits=" + killed);
      Mob immune = mob(20.5, 20.5, true);
      boolean refused = true;
      for (int i = 0; i < 12; i++) refused &= !CombatSkillLogic.hurtAndConfirm(immune, world, source, 1);
      immune.discard();
      Mob normal = mob(20.5, 20.5, false);
      check("damage-guard-after-refusals", refused && CombatSkillLogic.hurtAndConfirm(normal, world, source, 1),
         "refused calls cannot poison next independent hit");
      boolean attributed = true;
      for (ElementType element : new ElementType[]{ElementType.FIRE, ElementType.MAGMA, ElementType.ICE, ElementType.LIGHTNING, ElementType.PHYSICAL}) {
         var typed = CombatSkillLogic.elementalDamageSource(world, element, caster, normal);
         attributed &= typed.getEntity() == caster && typed.getDirectEntity() == normal;
      }
      check("elemental-owner-and-direct", attributed, "five elements retain owner and direct entity");
      normal.discard();
   }

   private static Mob mob(double x, double z, boolean invulnerable) {
      Mob mob = EntityTypes.COW.spawn(world, new BlockPos((int) x, 100, (int) z), EntitySpawnReason.COMMAND);
      if (mob == null) throw new IllegalStateException("Cow fixture did not spawn");
      mob.setPos(x, 100, z); mob.setNoAi(true); mob.setNoGravity(true);
      mob.setPermanentlyInvulnerable(invulnerable); mob.setHealth(10); mob.setDeltaMovement(Vec3.ZERO);
      MOBS.add(mob);
      return mob;
   }

   private static void cleanup() {
      active = false;
      for (Mob mob : MOBS) mob.discard();
      MOBS.clear();
      if (caster != null) MinepieceModVariables.clear(caster);
      for (ServerPlayer owner : new ServerPlayer[]{comboCaster, cancelledCaster}) if (owner != null) {
         ZoanClaw.cancel(owner); MinepieceModVariables.clear(owner);
      }
   }
}
