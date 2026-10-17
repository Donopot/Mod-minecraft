"""Initial grouped B07 graphical evidence wiring; subsequent fixes edit sources in place."""
from pathlib import Path
import json,re
P=Path(__file__).resolve().parents[1];J=P/'src/main/java';client=J/'net/mcreator/minepiece/client';cat=json.loads((P/'build/b07-moves.local.json').read_text())
fruits=['inu_inu_okuchi_no_makami','hito_hito_daibutsu','tori_tori_phoenix','uo_uo_seiryu','ryu_ryu_pteranodon'];full=dict(zip(fruits,cat.values()));short=dict(zip(fruits,cat))
s=(client/'Batch06VisualLab.java').read_text(encoding='utf-8').replace('Batch06','Batch07').replace('batch06','batch07').replace('b06_','b07_')
a=s.index(' public static final String[] FRUITS=');b=s.index(' private static final List',a)
s=s[:a]+' public static final String[] FRUITS={'+','.join(json.dumps(f) for f in fruits)+'},NAMES={'+','.join(json.dumps(n) for n in cat)+'};\n public static final String[][] MOVES={'+','.join('{'+','.join(json.dumps(m) for m in ms)+'}' for ms in cat.values())+'};\n'+s[b:]
a=s.index(' private static String expected(');b=s.index(' private static void tick(',a);s=s[:a]+''' private static String expected(String f,String id){return "b07_"+NAMES[Arrays.asList(FRUITS).indexOf(f)]+"_"+id;}
'''+s[b:]
s='\n'.join(l for l in s.splitlines() if 'db06counter' not in l)+'\n'
s=s.replace('if(offset==45||offset==145)ClientPlayNetworking.send','if(offset==45||offset==145)ClientPlayNetworking.send')
(client/'Batch07VisualLab.java').write_text(s,encoding='utf-8')
def edit(p,old,new):
 s=p.read_text(encoding='utf-8');assert old in s,(p,old[:70]);p.write_text(s.replace(old,new,1),encoding='utf-8')
edit(client/'ClientVisualLab.java','      if("batch06"','      if("batch07".equals(System.getProperty("opworldlike.visualScenario"))) {Batch07VisualLab.register();return;}\n      if("batch06"')
for name in ['EntityGomuArmRenderer','SkillProjectileRenderer']:
 p=client/'renderer'/f'{name}.java';s=p.read_text(encoding='utf-8');anchor='String id = geo.getOrDefaultGeckolibData(PROFILE, "");' if name=='EntityGomuArmRenderer' else 'String id=geo.getOrDefaultGeckolibData(PROFILE, "");';assert anchor in s;s=s.replace(anchor,anchor+'\n      net.mcreator.minepiece.client.Batch07VisualLab.rendered(id);',1);p.write_text(s,encoding='utf-8')
edit(client/'DevilFruitAnimationLayer.java','(payload.animation().startsWith("b06_")','(payload.animation().startsWith("b07_") || payload.animation().startsWith("b06_")')
edit(client/'DevilFruitAnimationLayer.java','check.animation.startsWith("b06_")?"batch06"','check.animation.startsWith("b07_")?"batch07":check.animation.startsWith("b06_")?"batch06"')
edit(client/'model/MythicalBeastModel.java','  }\n }\n}', '''  }
  if(kind<5){int slot=kind==4?3:kind==3?4:kind;Batch07VisualLab.beastPose(slot,rig.getChild("head").xRot,rig.hasChild("right_front_leg")?rig.getChild("right_front_leg").xRot:rig.getChild("right_arm").xRot,rig.hasChild("left_front_leg")?rig.getChild("left_front_leg").xRot:rig.getChild("left_arm").xRot);}
 }
}''')
p=client/'layer/PlayerTransformLayer.java';s=p.read_text(encoding='utf-8');s=s.replace('if(playerForm==2 && mythicalBeasts.containsKey(beastKey)){','if(playerForm==2 && mythicalBeasts.containsKey(beastKey)){\n         net.mcreator.minepiece.client.Batch07VisualLab.rendered("beast-"+fruitId);');s=s.replace('net.mcreator.minepiece.client.Batch06VisualLab.rendered("hybrid-"+fruitId);','net.mcreator.minepiece.client.Batch06VisualLab.rendered("hybrid-"+fruitId);net.mcreator.minepiece.client.Batch07VisualLab.rendered("hybrid-"+fruitId);');p.write_text(s,encoding='utf-8')
p=J/'net/mcreator/minepiece/command/RepairTestCommands.java';s=p.read_text(encoding='utf-8').replace('"batch05","batch06"','"batch05","batch06","batch07"');s=s.replace('"ushi_ushi_giraffe","batto_batto").contains(fruitName)','"ushi_ushi_giraffe","batto_batto",'+','.join('"'+f+'"' for f in fruits)+').contains(fruitName)')
anchor='         if("batch06".equals(System.getProperty("opworldlike.visualScenario"))){';assert anchor in s
s=s.replace(anchor,'''         if("batch07".equals(System.getProperty("opworldlike.visualScenario"))){
            targetZ=technique.equals("forms")?30:java.util.Set.of("bluebird","narikabura","impactball","kongokabura","kaifu","barizodon").contains(technique)?20:java.util.Set.of("himorogiri","kosanze","hooin").contains(technique)?15:11;
            player.getInventory().clearContent();
            for(var witness:source.getServer().getPlayerList().getPlayers())if(witness.getName().getString().equals("FruitWitness"))witness.connection.teleport(20.5,105.5,11.5,90,20);
         }
'''+anchor);p.write_text(s,encoding='utf-8')
p=P/'tools/launch_visual_lab.py';s=p.read_text(encoding='utf-8');a=s.index("            if frozen.get('capture') and frozen.get('scenario')=='batch06':");b=s.index("            elif frozen.get('capture') and frozen.get('scenario')=='batch05':",a);block=s[a:b].replace('batch06','batch07').replace('B06','B07').replace('b06_','b07_')
a1=block.index('                catalogue=');b1=block.index("                if frozen.get('retake'):",a1);block=block[:a1]+'                catalogue='+repr(full)+'\n                short='+repr(short)+'\n'+block[b1:]
a1=block.index('                for f in catalogue:\n');b1=block.index('                expected_images=',a1);block=block[:a1]+'''                for f,ms in catalogue.items():
                    for suffix in ['', '_release']:expected_pal[f'b07_{short[f]}_{ms[0]}'+suffix]+=2
'''+block[b1:]
a1=block.index("                if 'batto_batto' in catalogue:");b1=block.index("                frozen['rendered']=True",a1);block=block[:a1]+block[b1:]
s=s[:a]+block+s[a:].replace("            if frozen.get('capture') and frozen.get('scenario')=='batch06':","            elif frozen.get('capture') and frozen.get('scenario')=='batch06':",1)
s=s.replace("'batch05','batch06'","'batch05','batch06','batch07'").replace("(500 if frozen.get('scenario')=='batch06'","(500 if frozen.get('scenario') in ('batch06','batch07')")
s=s.replace("'batto_batto'],default='hie_hie'","'batto_batto',"+','.join(repr(f) for f in fruits)+"],default='hie_hie'")
anchor="'batto_batto':'shigan'}";s=s.replace(anchor,"'batto_batto':'shigan',"+','.join(repr(f)+':'+repr(ms[0]) for f,ms in full.items())+'}')
# Current manual mapping had only28 entries; add all remaining handled Zoans explicitly.
anchor="'gomu_gomu':'gomu_pistol'}";s=s.replace(anchor,"'gomu_gomu':'gomu_pistol',"+','.join(repr(f)+':'+repr(ms[0]) for f,ms in full.items())+",'inu_inu_wolf':'shigan','neko_neko_leopard':'shigan','neko_neko_sabertiger':'shigan','ushi_ushi_giraffe':'shigan','batto_batto':'shigan'}")
p.write_text(s,encoding='utf-8')
s=(P/'tools/audit_batch06_animations.py').read_text(encoding='utf-8').replace('06','07');(P/'tools/audit_batch07_animations.py').write_text(s,encoding='utf-8')
print('B07 grouped actor/observer native forms/mesh/58PAL evidence wired')
