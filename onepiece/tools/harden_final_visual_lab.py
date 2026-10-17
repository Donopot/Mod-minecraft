"""Mutual scene clock avoids capturing stale scenes when one lab client renders more slowly."""
from pathlib import Path
P=Path(__file__).resolve().parents[1];J=P/'src/main/java';C=J/'net/mcreator/minepiece/client'
for name in ['Batch07VisualLab','Batch08VisualLab']:
 p=C/(name+'.java');s=p.read_text(encoding='utf-8').replace('actorClock=-1;','actorClock=-1,witnessClock=-1;',1)
 s=s.replace('if(p.scene().startsWith("batch-clock:"))actorClock=Integer.parseInt(p.scene().substring(12));','if(p.scene().startsWith("batch-clock:"))actorClock=Integer.parseInt(p.scene().substring(12));if(p.scene().startsWith("witness-clock:"))witnessClock=Integer.parseInt(p.scene().substring(14));',1)
 s=s.replace('if(observer&&tick>actorClock)return;','if(observer&&tick>actorClock)return;if(!observer&&tick>witnessClock+10)return;if(observer&&tick%5==0)c.player.connection.sendCommand("dbatchwitness "+tick);',1)
 s=s.replace('if(index>=ids.size()){if(offset>=30)c.stop();','if(index>=ids.size()){if(!observer&&tick%5==0)c.player.connection.sendCommand("dbatchclock "+(tick+100));if(offset>=30)c.stop();',1)
 p.write_text(s,encoding='utf-8')
p=J/'net/mcreator/minepiece/command/RepairTestCommands.java';s=p.read_text(encoding='utf-8');anchor='      dispatcher.register(Commands.literal("dbatchclock")';assert anchor in s
s=s.replace(anchor,'''      dispatcher.register(Commands.literal("dbatchwitness").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).then(Commands.argument("tick",com.mojang.brigadier.arguments.IntegerArgumentType.integer(0)).executes(c->{
         if(!Boolean.getBoolean("opworldlike.visualCapture")||c.getSource().getPlayer()==null||!c.getSource().getPlayer().getName().getString().equals("FruitWitness"))return 0;
         int clock=com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c,"tick");for(var p:c.getSource().getServer().getPlayerList().getPlayers())net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.send(p,new net.mcreator.minepiece.network.DevilFruitNetworking.LabScenePayload("witness-clock:"+clock));return 1;
      })));
'''+anchor,1)
# Both capture roles start without an unsolicited Leopard transformation.
s=s.replace('"batch03","batch04","batch05").contains(System.getProperty("opworldlike.visualScenario"','"batch03","batch04","batch05","batch06","batch07","batch08").contains(System.getProperty("opworldlike.visualScenario"')
p.write_text(s,encoding='utf-8')
p=P/'tools/launch_visual_lab.py';s=p.read_text(encoding='utf-8').replace('(500 if frozen.get(\'scenario\') in (\'batch06\',\'batch07\',\'batch08\')','(650 if frozen.get(\'scenario\') in (\'batch06\',\'batch07\',\'batch08\')');p.write_text(s,encoding='utf-8')
print('B07/B08 mutual client clock, ending grace period and stationary flying observer')
