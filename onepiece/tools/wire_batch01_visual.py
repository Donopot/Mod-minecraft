from pathlib import Path
J=Path(__file__).resolve().parents[1]/'src/main/java';C=J/'net/mcreator/minepiece/client';P=J/'net/mcreator/minepiece/procedures'
p=C/'Batch01VisualLab.java';s=p.read_text(encoding='utf-8').replace('e instanceof net.minecraft.world.entity.monster.zombie.Husk h','e instanceof LivingEntity h && h.getType()==net.minecraft.world.entity.EntityTypes.HUSK');p.write_text(s,encoding='utf-8')
p=C/'ClientVisualLab.java';s=p.read_text(encoding='utf-8').replace('      if("amaru".equals','      if("batch01".equals(System.getProperty("opworldlike.visualScenario"))) {Batch01VisualLab.register();return;}\n      if("amaru".equals');p.write_text(s,encoding='utf-8')
p=C/'renderer/SkillProjectileRenderer.java';s=p.read_text(encoding='utf-8').replace('      var profile = TechniqueVisualProfile','      net.mcreator.minepiece.client.Batch01VisualLab.rendered(geo.getOrDefaultGeckolibData(PROFILE,""));\n      var profile = TechniqueVisualProfile');p.write_text(s,encoding='utf-8')
p=C/'renderer/EntityGomuArmRenderer.java';s=p.read_text(encoding='utf-8').replace('      String id = geo.getOrDefaultGeckolibData(PROFILE, "");','      String id = geo.getOrDefaultGeckolibData(PROFILE, "");\n      net.mcreator.minepiece.client.Batch01VisualLab.rendered(id);');p.write_text(s,encoding='utf-8')
p=P/'SandStorm.java';s=p.read_text(encoding='utf-8').replace('ElementalBatchTechniques.fx(owner,"",centre','ElementalBatchTechniques.fx(owner,"sables-native",centre');p.write_text(s,encoding='utf-8')
p=J/'net/mcreator/minepiece/command/RepairTestCommands.java';s=p.read_text(encoding='utf-8').replace('"mera","magu","goro","amaru"','"mera","magu","goro","amaru","batch01"').replace('fruitName.equals("goro_goro")) {','fruitName.equals("goro_goro") || java.util.Set.of("hie_hie","pika_pika","moku_moku","suna_suna","yami_yami").contains(fruitName)) {')
s=s.replace('         for(int x:new int[]{8,11,14}) {','''         if("batch01".equals(System.getProperty("opworldlike.visualScenario"))){
            targetZ=java.util.Set.of("icepartisan","pheasantbeak","laser1","laser2","magatama","whiteblow","whitevine","desertspada","desertlaspada","darkball","darkmatter").contains(technique)?30:14;
            player.getInventory().clearContent();if(technique.equals("amanomurakumo"))targetZ=10;
            for(var witness:source.getServer().getPlayerList().getPlayers())if(witness.getName().getString().equals("FruitWitness"))witness.connection.teleport(27,108,20,90,18);
         }
         for(int x:new int[]{8,11,14}) {''')
prefix='      dispatcher.register(Commands.literal("dmeraready")'
s=s.replace(prefix,'''      dispatcher.register(Commands.literal("dbatchclock").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(Commands.argument("tick",com.mojang.brigadier.arguments.IntegerArgumentType.integer(0)).executes(c->{
         if(!"batch01".equals(System.getProperty("opworldlike.visualScenario")))return 0;
         int clock=com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c,"tick");for(var p:c.getSource().getServer().getPlayerList().getPlayers())net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(p,new net.mcreator.minepiece.network.DevilFruitNetworking.LabScenePayload("batch-clock:"+clock));return 1;
      })));
'''+prefix)
p.write_text(s,encoding='utf-8')
