from pathlib import Path
import zipfile,json
R=Path(__file__).resolve().parents[1];J=R/'src/main/java';P=J/'net/mcreator/minepiece/procedures'
def edit(f,a,b):
 s=f.read_text(encoding='utf-8');assert a in s,(f,a);f.write_text(s.replace(a,b),encoding='utf-8')
edit(R/'gradle.properties','version=1.0.1-alpha.7','version=1.0.1-alpha.8')
edit(P/'DevilFruitBehaviours.java','   public static boolean customCast(ServerPlayer player, ItemStack fruit, DevilFruitMove move) {','   public static boolean customCast(ServerPlayer player, ItemStack fruit, DevilFruitMove move) {\n      if(ElementalBatchTechniques.handles(fruit))return ElementalBatchTechniques.cast(player,fruit,move);')
for a,b in [('"yami_yami","kurouzu","blackhole"','"yami_yami","kurouzu","blackhole","darkball","darkmatter"'),('"moku_moku","whiteout"','"moku_moku","whiteblow","whitevine","whitelauncher","whiteout"'),('"suna_suna","barjan","grounddeath"','"suna_suna","desertspada","barjan","sables","desertlaspada","grounddeath"'),('"hie_hie","iceball","iceage","icespikes"','"hie_hie","icepartisan","pheasantbeak","iceball","iceage","icespikes"')]: edit(P/'CustomTechniqueRoutes.java',a,b)
edit(J/'br/com/oneminepiece/opworldlike/OmpWorld.java','      net.mcreator.minepiece.procedures.GoroTechniques.register();','      net.mcreator.minepiece.procedures.GoroTechniques.register();\n      net.mcreator.minepiece.procedures.ElementalBatchTechniques.register();')
edit(P/'ElementalBatchTechniques.java','DevilFruitPikaConfig.travelMaxBlocks()','DevilFruitPikaConfig.travelMaxDistanceBlocks()')
edit(P/'ElementalBatchTechniques.java','case "sables"->{SandStorm.create(p);','case "sables"->{area(a,p.position(),6,false,true);SandStorm.create(p);')
edit(P/'ElementalBatchTechniques.java','radius=Math.min(24,Math.max(1,radius))','radius=Math.min(32,Math.max(1,radius))')
edit(J/'net/mcreator/minepiece/init/MinepieceModItems.java','   public static final Item ACE =','   public static final Item AMANO_MURAKUMO = registerSword("amano_murakumo", 7.0f, -2.4f);\n   public static final Item ACE =')
edit(P/'LightBuffs.java','double increment=Math.max(0,b.amplifier+1-ext)*(b.effect==MobEffects.SPEED?.2:3);','double increment=b.effect==MobEffects.SPEED?Math.max(0,(1+.2*(b.amplifier+1))/(1+.2*ext)-1):Math.max(0,b.amplifier+1-ext)*3;')
edit(P/'LightBuffs.java','b.effect==MobEffects.SPEED?AttributeModifier.Operation.ADD_MULTIPLIED_BASE','b.effect==MobEffects.SPEED?AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL')
edit(P/'TechniqueAnimations.java','      String goro=GoroTechniques.animationFor','      String batch=ElementalBatchTechniques.animationFor(player,move.id());if(!batch.isEmpty())return batch;\n      String goro=GoroTechniques.animationFor')
# Reset pending episodes synchronously, in addition to tick revalidation.
f=P/'DevilFruitPowerLogic.java';s=f.read_text(encoding='utf-8');needle='GoroTechniques.cancel(player);'
if needle in s:s=s.replace(needle,needle+'ElementalBatchTechniques.cancel(player);')
else:print('CHECK reset hook', 'GoroTechniques' in s)
f.write_text(s,encoding='utf-8')
f=J/'net/mcreator/minepiece/entity/SkillProjectileEntity.java';s=f.read_text(encoding='utf-8')
s=s.replace('   private ServerPlayer goroOrigin;', '''   private ServerPlayer batchOrigin;private net.minecraft.world.item.Item batchFruit;
   public SkillProjectileEntity batchOrigin(ServerPlayer p){batchOrigin=p;batchFruit=net.mcreator.minepiece.procedures.DevilFruitPowerLogic.getDevilFruit(p).getItem();return this;}
   private ServerPlayer goroOrigin;''')
s=s.replace('      if (net.mcreator.minepiece.procedures.MeraTechniques.ownsVisual(getVisualProfile())) {','      if(batchOrigin!=null && !net.mcreator.minepiece.procedures.BatchRules.usable(batchOrigin,(ServerLevel)level(),batchFruit)){discard();return;}\n      if (net.mcreator.minepiece.procedures.MeraTechniques.ownsVisual(getVisualProfile())) {')
s=s.replace('      Entity target = result.getEntity();','      Entity target = result.getEntity();\n      if(batchOrigin!=null && target instanceof LivingEntity t && (!(getOwner() instanceof ServerPlayer p) || !net.mcreator.minepiece.procedures.BatchRules.target(p,t))){discard();return;}')
s=s.replace('            CombatSkillLogic.applyElementalEffect(living, element,','            if(batchOrigin!=null && getOwner() instanceof ServerPlayer p)net.mcreator.minepiece.procedures.ElementalBatchTechniques.onConfirmed(p,living,batchFruit);\n            else CombatSkillLogic.applyElementalEffect(living, element,')
s=s.replace('      ServerLevel serverLevel = (ServerLevel) level();\n      serverLevel.sendParticles', '      ServerLevel serverLevel = (ServerLevel) level();\n      if(batchOrigin!=null){net.mcreator.minepiece.procedures.ElementalBatchTechniques.fx(batchOrigin,getVisualProfile().startsWith("dark")?"b01_dark_impact":batchFruit==net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_HIE_HIE?"b01_ice_impact":"b01_impact",position(),10);}\n      serverLevel.sendParticles')
f.write_text(s,encoding='utf-8')
for a,b in [('v("barjan", "entity_slash", "slash_blue")','v("barjan", "entity_slash", "slash_sand")'),('v("desertspada", "projectile_slash", "projectile_slash")','v("desertspada", "projectile_slash", "slash_sand")'),('v("desertlaspada", "projectile_slash", "projectile_slash")','v("desertlaspada", "projectile_slash", "slash_sand")'),('v("sables", "tornado", "slash_blue")','v("sables", "tornado", "slash_sand")'),('v("whitevine", "tornado", "slash_blue")','v("whitevine", "tornado", "slash_white")')]:
 f=P/'DevilFruitRegistry.java';s=f.read_text(encoding='utf-8');s=s.replace(a,b);f.write_text(s,encoding='utf-8')
# Preserve full baseline language files; only new item definition is additive.
resources=R/'src/main/resources';source=R.parent/'.runtime/onepiece/reference-jars/MinePiece-ver15-forge-1.20.1.jar'
with zipfile.ZipFile(source) as z:
 for texture in ['slash_white','slash_sand']:
  n='assets/minepiece/textures/entities/'+texture+'.png';candidates=[n for n in z.namelist() if n.endswith('/'+texture+'.png')];assert candidates,texture
  n=candidates[0];dest=resources/n;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(z.read(n));print('TEXTURE',n)
 sword=[n for n in z.namelist() if 'amano_murakumo' in n and n.endswith('.png')];print('SWORD REFERENCES',sword)
 if sword:
  n=sword[0];dest=resources/'assets/minepiece/textures/item/amano_murakumo.png';dest.parent.mkdir(parents=True,exist_ok=True);dest.write_bytes(z.read(n))
  for name,obj in [('items/amano_murakumo.json',{'model':{'type':'minecraft:model','model':'minepiece:item/amano_murakumo'}}),('models/item/amano_murakumo.json',{'parent':'minecraft:item/handheld','textures':{'layer0':'minepiece:item/amano_murakumo'}})]:
   dest=resources/'assets/minepiece'/name;dest.parent.mkdir(parents=True,exist_ok=True);dest.write_text(json.dumps(obj),encoding='utf-8')
print('B01 wiring complete')
