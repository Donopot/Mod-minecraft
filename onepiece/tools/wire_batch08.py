from pathlib import Path
import json,re
P=Path(__file__).resolve().parents[1];J=P/'src/main/java';C=J/'net/mcreator/minepiece/client';cat=json.loads((P/'build/b08-moves.local.json').read_text());fruits=['ryu_ryu_brachiosaurus','ryu_ryu_pachycephalosaurus'];ns='net.mcreator.minepiece.client.model.'
def edit(p,old,new):
 s=p.read_text(encoding='utf-8');assert old in s,(p,old[:50]);p.write_text(s.replace(old,new,1),encoding='utf-8')
edit(C/'DevilFruitClientInit.java','ClientVisualLab.register();','ClientVisualLab.register();\n'+f'      ModelLayerRegistry.registerModelLayer({ns}AncientBeastModel.BRACHIO,{ns}ReferenceBeastGeometry::brachiosaurusGuard);\n      ModelLayerRegistry.registerModelLayer({ns}AncientBeastModel.PACHY,{ns}AncientBeastModel::pachy);\n      ModelLayerRegistry.registerModelLayer({ns}AncientBeastModel.DOME,{ns}AncientBeastModel::dome);')
p=C/'layer/PlayerTransformLayer.java';s=p.read_text(encoding='utf-8').replace('private final Map<String,net.mcreator.minepiece.client.model.MythicalBeastModel> mythicalBeasts','private final Map<String,net.mcreator.minepiece.client.model.AncientBeastModel> ancientBeasts=new HashMap<>();\n   private final net.mcreator.minepiece.client.model.AncientBeastModel dome;\n   private final Map<String,net.mcreator.minepiece.client.model.MythicalBeastModel> mythicalBeasts',1)
s=s.replace('EntityModelSet es = Minecraft.getInstance().getEntityModels();','EntityModelSet es = Minecraft.getInstance().getEntityModels();\n'+f'      ancientBeasts.put("{fruits[0]}",new {ns}AncientBeastModel(es.bakeLayer({ns}AncientBeastModel.BRACHIO),0));\n      ancientBeasts.put("{fruits[1]}",new {ns}AncientBeastModel(es.bakeLayer({ns}AncientBeastModel.PACHY),1));\n      dome=new {ns}AncientBeastModel(es.bakeLayer({ns}AncientBeastModel.DOME),2);',1)
anchor='      if(playerForm==2 && mythicalBeasts.containsKey(beastKey)){';s=s.replace(anchor,'''      if(playerForm==2 && ancientBeasts.containsKey(beastKey)){
         data.minepiece$setPoses(net.mcreator.minepiece.client.TransformPoses.capture(getParentModel()));
         renderColoredCutoutModel(ancientBeasts.get(beastKey),beastKey.equals("ryu_ryu_brachiosaurus")?tex("b08_brachiosaurus_guard"):tex("tex_white3"),poseStack,collector,lightCoords,state,beastKey.equals("ryu_ryu_brachiosaurus")?-1:0xFF6EABC9,0);
         if(beastKey.equals("ryu_ryu_pachycephalosaurus"))renderColoredCutoutModel(dome,tex("tex_white3"),poseStack,collector,lightCoords,state,0xFFF0D8E4,1);
         net.mcreator.minepiece.client.Batch08VisualLab.rendered("beast-"+fruitId);return;
      }
'''+anchor,1).replace('net.mcreator.minepiece.client.Batch07VisualLab.rendered("hybrid-"+fruitId);','net.mcreator.minepiece.client.Batch07VisualLab.rendered("hybrid-"+fruitId);net.mcreator.minepiece.client.Batch08VisualLab.rendered("hybrid-"+fruitId);',1);p.write_text(s,encoding='utf-8')
# Native acceptance: same independent wall/terrain/allied/refused scenes with final ten budgets.
p=J/'net/mcreator/minepiece/command/Batch08TestCommands.java';s=(p.with_name('Batch07TestCommands.java')).read_text(encoding='utf-8').replace('Batch07','Batch08').replace('batch07','batch08').replace('B07','B08').replace('b07_','b08_').replace('MythicalZoanTechniques.FRUITS','MythicalZoanTechniques.ANCIENT_FRUITS').replace('MythicalZoanTechniques.NAMES','MythicalZoanTechniques.ANCIENT_NAMES')
s=s.replace('caster,cancelCaster,defenseCaster;','caster,cancelCaster;').replace('Set.of("himorogiri","kosanze","hooin")','Set.of("brachiobober","attackjump","ulmeteor")')
s=s.replace('slam?87.5:83.5','id.equals("ulmeteor")?92.5:slam?87.5:83.5')
s=s.replace('m.damage()+(id.equals("borobreath")?2:0)','m.damage()+(id.equals("brachiolauncher")?13.5:0)',1)
a=s.index(' fruit(FRUITS[4]);caster.getAbilities().mayfly=true;');b=s.index(' cancelCaster=FakePlayer.get(world,',a);s=s[:a]+s[b:]
s=s.replace('v.PlayerDevilFruit=new ItemStack(FRUITS[2]);','v.PlayerDevilFruit=new ItemStack(FRUITS[0]);').replace('v.PlayerTechniqueUsedNumber=2;','v.PlayerTechniqueUsedNumber=3;')
s=s.replace('if(life==220)check("life/defense-expiry",!IceDefense.isActive(defenseCaster)&&defenseCaster.hasEffect(MobEffects.RESISTANCE),"native200ticks; foreign resistance retained");','').replace('DevilFruitPowerLogic.resetDevilFruitPowers(defenseCaster);MinepieceModVariables.clear(defenseCaster);defenseCaster.discard();','');p.write_text(s,encoding='utf-8')
edit(J/'br/com/oneminepiece/opworldlike/OmpWorld.java','net.mcreator.minepiece.command.Batch07TestCommands.register(dispatcher);','net.mcreator.minepiece.command.Batch07TestCommands.register(dispatcher);\n          net.mcreator.minepiece.command.Batch08TestCommands.register(dispatcher);')
# Grouped final pair; the full five-fruit B07 graphical scene is already running on its frozen JAR.
p=C/'Batch08VisualLab.java';s=(p.with_name('Batch07VisualLab.java')).read_text(encoding='utf-8').replace('Batch07','Batch08').replace('batch07','batch08').replace('B07','B08').replace('b07_','b08_')
a=s.index(' public static final String[] FRUITS=');b=s.index(' private static final List',a);s=s[:a]+' public static final String[] FRUITS={'+','.join(json.dumps(f) for f in fruits)+'},NAMES={"brachio","pachy"};\n public static final String[][] MOVES={'+','.join('{'+','.join(json.dumps(m) for m in ms)+'}' for ms in cat.values())+'};\n'+s[b:]
s=s.replace('if(Set.of(FRUITS[2],FRUITS[3],FRUITS[4]).contains(fruit)){if(offset==178||offset==182)c.options.keyJump.setDown(true);if(offset==180||offset==192)c.options.keyJump.setDown(false);}','')
s='\n'.join(l for l in s.splitlines() if 'forms-native-flight' not in l and 'key+"-native-flight"' not in l)+'\n';p.write_text(s,encoding='utf-8')
edit(C/'ClientVisualLab.java','      if("batch07"','      if("batch08".equals(System.getProperty("opworldlike.visualScenario"))) {Batch08VisualLab.register();return;}\n      if("batch07"')
for name in ['EntityGomuArmRenderer','SkillProjectileRenderer']:edit(C/'renderer'/f'{name}.java','net.mcreator.minepiece.client.Batch07VisualLab.rendered(id);','net.mcreator.minepiece.client.Batch07VisualLab.rendered(id);net.mcreator.minepiece.client.Batch08VisualLab.rendered(id);')
p=C/'DevilFruitAnimationLayer.java';s=p.read_text(encoding='utf-8').replace('(payload.animation().startsWith("b07_")','(payload.animation().startsWith("b08_") || payload.animation().startsWith("b07_")',1).replace('check.animation.startsWith("b07_")?"batch07"','check.animation.startsWith("b08_")?"batch08":check.animation.startsWith("b07_")?"batch07"',1);p.write_text(s,encoding='utf-8')
p=J/'net/mcreator/minepiece/command/RepairTestCommands.java';s=p.read_text(encoding='utf-8').replace('"batch06","batch07"','"batch06","batch07","batch08"')
s=s.replace('"ryu_ryu_pteranodon").contains(fruitName)','"ryu_ryu_pteranodon","ryu_ryu_brachiosaurus","ryu_ryu_pachycephalosaurus").contains(fruitName)',1)
anchor='         if("batch07".equals(System.getProperty("opworldlike.visualScenario"))){';s=s.replace(anchor,'''         if("batch08".equals(System.getProperty("opworldlike.visualScenario"))){
            targetZ=technique.equals("forms")?32:technique.equals("ulmeteor")?20:java.util.Set.of("blackcofee","brachiolauncher","sparkingqueen").contains(technique)?22:java.util.Set.of("brachiobober","attackjump").contains(technique)?15:11;
            player.getInventory().clearContent();
            for(var witness:source.getServer().getPlayerList().getPlayers())if(witness.getName().getString().equals("FruitWitness")){witness.connection.teleport(22.5,106,11.5,90,16);witness.getAbilities().mayfly=true;witness.getAbilities().flying=true;witness.onUpdateAbilities();}
         }
'''+anchor,1)
s=s.replace('witness.connection.teleport(20.5,105.5,11.5,90,20);','{witness.connection.teleport(22.5,106,11.5,90,16);witness.getAbilities().mayfly=true;witness.getAbilities().flying=true;witness.onUpdateAbilities();}',1)
p.write_text(s,encoding='utf-8')
p=P/'tools/run_local_tests.py';s=p.read_text(encoding='utf-8').replace("    parser.add_argument('--batch07',","    parser.add_argument('--batch08',action='store_true')\n    parser.add_argument('--batch08-only',default='')\n    parser.add_argument('--batch07',",1)
a=s.index('            if args.batch07:');b=s.index('            if args.batch06:',a);block=s[a:b].replace('batch07','batch08').replace('B07','B08').replace('b07-','b08-').replace("utility={'hyogaheki'}","utility=set()")
block=block.replace("                required|={'form/'+x for x in ['foreign-flight','foreign-flight-retained','owned-flight','owned-flight-release']}\n",'')
block=block.replace("|{'core/'+x for x in ['ice-defense','ice-authority','ice-fire','ice-wrong-fruit']}",'').replace("['projectile-cast','projectile-live','projectile-cancelled','defense-begin','defense-expiry']","['projectile-cast','projectile-live','projectile-cancelled']")
s=s[:a]+block+s[a:];p.write_text(s,encoding='utf-8')
p=P/'tools/launch_visual_lab.py';s=p.read_text(encoding='utf-8');a=s.index("            if frozen.get('capture') and frozen.get('scenario')=='batch07':");b=s.index("            elif frozen.get('capture') and frozen.get('scenario')=='batch06':",a);block=s[a:b].replace('batch07','batch08').replace('BATCH07','BATCH08').replace('B07','B08').replace('b07_','b08_')
a1=block.index('                catalogue=');b1=block.index("                if frozen.get('retake'):",a1);block=block[:a1]+'                catalogue='+repr(dict(zip(fruits,cat.values())))+'\n                short='+repr(dict(zip(fruits,cat)))+'\n'+block[b1:]
block='\n'.join(l for l in block.splitlines() if 'forms-native-flight' not in l)+'\n'
s=s[:a]+block+s[a:].replace("            if frozen.get('capture') and frozen.get('scenario')=='batch07':","            elif frozen.get('capture') and frozen.get('scenario')=='batch07':",1)
s=s.replace("'batch06','batch07'","'batch06','batch07','batch08'")
s=s.replace("'ryu_ryu_pteranodon'],default='hie_hie'","'ryu_ryu_pteranodon','ryu_ryu_brachiosaurus','ryu_ryu_pachycephalosaurus'],default='hie_hie'")
s=s.replace("'batto_batto':'shigan'}","'batto_batto':'shigan','ryu_ryu_brachiosaurus':'attackoverhead','ryu_ryu_pachycephalosaurus':'attackjump'}")
p.write_text(s,encoding='utf-8')
s=(P/'tools/audit_batch07_animations.py').read_text(encoding='utf-8').replace('07','08');(P/'tools/audit_batch08_animations.py').write_text(s,encoding='utf-8')
print('B08 complete forms,175native acceptance,62+62 grouped captures wired')
