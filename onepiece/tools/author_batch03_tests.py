from pathlib import Path
import re
P=Path(__file__).resolve().parents[1];folder=P/'src/main/java/net/mcreator/minepiece/command'
s=(folder/'Batch02TestCommands.java').read_text(encoding='utf-8').replace('Batch02','Batch03').replace('dbatch02','dbatch03').replace('ControlledBatchTechniques','MaterialBatchTechniques')
s=s.replace('ITEM_DEVIL_FRUIT_GURA_GURA','ITEM_DEVIL_FRUIT_MOCHI_MOCHI').replace('ITEM_DEVIL_FRUIT_BOMU_BOMU','ITEM_DEVIL_FRUIT_MORI_MORI').replace('ITEM_DEVIL_FRUIT_ZUSHI_ZUSHI','ITEM_DEVIL_FRUIT_KIRA_KIRA').replace('ITEM_DEVIL_FRUIT_NIKYU_NIKYU','ITEM_DEVIL_FRUIT_SUPA_SUPA').replace('ITEM_DEVIL_FRUIT_OPE_OPE','ITEM_DEVIL_FRUIT_RIKI_RIKI')
s=s.replace('{"gura","bomu","zushi","nikyu","ope"}','{"mochi","mori","kira","supa","riki"}').replace('Set.of("room","shambles","passive")','Set.of("mochibuild","diamondbody")').replace('Set.of("kickbomb","attackspeed","assault","kumapunch","passive")','Set.of("zangirimochi","brilliantpunk","atomicspurt","galleonlariat")')
for n in sorted([352,405,376,375,378,390,442,373,380],reverse=True):s=re.sub(r'\b'+str(n)+r'\b',str(n+304),s)
s=s.replace('   if(scene>=moves.size()*3){extrasTick();return;}','')
s=s.replace('   if(age==22 && scene%3==0 && m.id().equals("stoneopeope"))check(prefix+"/staggered",MaterialBatchTechniques.shotCount()<4,"rubble releases across native ticks");','''   if(age==15 && scene%3==0 && m.id().equals("mochigatling"))check(prefix+"/cadence-early",normal.getHealth()==496,"first4HP hit only, later pulses pending");
   if(age==120 && scene%3==0 && m.id().equals("musodonatsu"))check(prefix+"/no-autonomous-damage",normal.getHealth()==500 && MaterialBatchTechniques.ringCount(caster)==2,"two rings wait for a new owner gesture");
   if(age==150 && m.id().equals("musodonatsu")){boolean next=cast("mochipistol");if(scene%3==0)check(prefix+"/gesture",next,"normal recharge elapsed; follow-up caster gesture accepted");}''')
s=s.replace('if(age<90)return;finish();clean();scene++;age=0;if(scene==moves.size()*3){core();setupRoof();}else begin();','if(age<(m.id().equals("musodonatsu")?250:140))return;finish();clean();scene++;age=0;if(scene==moves.size()*3){core();end();}else begin();')
s=s.replace('double distance=id.equals("meteor")?18:id.equals("meteorsmall")?12:id.equals("amputation")?4:id.equals("bomboverhead")?(scene%3==0?3:5):5;','double distance=4;')
s=s.replace('  if(Set.of("shambles","amputation").contains(id))OpeRoom.open(caster);','')
s=s.replace('  if(scene%3==2 && id.equals("kabutowari"))normal.setPos(680.5,100,82);','')
s=s.replace('TechniqueAnimations.nameFor(moves.get(i),caster).equals("b02_"+id) && TechniqueAnimations.fichierPresent("b02_"+id)','TechniqueAnimations.nameFor(moves.get(i),caster).equals("b03_"+names.get(i)+"_"+id) && TechniqueAnimations.fichierPresent("b03_"+names.get(i)+"_"+id)')
start=s.index('  if(scene%3==2){var policy=');end=s.index('\n  if(scene%3==1)',start)
s=s[:start]+'''  if(scene%3==2){var policy=TechniqueTerrain.policy(fruits.get(i),id);int changed=TechniqueTerrain.brokenTotal(TechniqueTerrain.fruitKey(fruits.get(i))+"/"+id)-terrainBefore;int grass=0,honey=0;
   for(int x=656;x<=709;x++)for(int z=76;z<=140;z++){if(world.getBlockState(new BlockPos(x,99,z)).is(Blocks.GRASS_BLOCK))grass++;for(int y=100;y<=104;y++)if(world.getBlockState(new BlockPos(x,y,z)).is(Blocks.HONEY_BLOCK))honey++;}
   check(prefix+"/terrain",policy!=null && (policy.mode()==TechniqueTerrain.Mode.DESTRUCTIVE?changed>0 && changed<=policy.blocks():policy.mode()==TechniqueTerrain.Mode.TRANSFORM?(id.equals("mochibuild")?honey>0 && honey<=24:grass>0 && grass<=128):changed==0 && grass==0 && honey==0),"physical broken="+changed+", grass="+grass+", honey="+honey+", policy="+policy);return;}
'''+s[end:]
s=s.replace('normal.getHealth()==500 && !Amputation.isCut(normal)','normal.getHealth()==500 && !Roots.isHeld(normal) && !StickyState.isSticky(normal)')
start=s.index('  if(id.equals("shambles"))check(');end=s.index('  check(prefix+"/ally"',start)
s=s[:start]+'''  if(id.equals("musodonatsu"))check(prefix+"/rings-follow",MaterialBatchTechniques.ringCount(caster)==2,"rings remain active until source TTL or owner cancellation");
  if(id.equals("diamondbody"))check(prefix+"/body-active",DiamondBody.isActive(caster) && caster.getAttributeValue(Attributes.ARMOR)>0,"scoped coat active without player_form");
  if(id.equals("mochigatling"))check(prefix+"/six-hit-budget",Math.abs(normal.getHealth()-476)<.01,"six accepted4HP pulses on native ticks, HP="+normal.getHealth());
'''+s[end:]
s=s.replace('!Amputation.isCut(refused)','!Roots.isHeld(refused) && !StickyState.isSticky(refused)')
s=s.replace('MaterialBatchTechniques.activeCount()==0 && MaterialBatchTechniques.shotCount()==0 && SeismicWave.activeWaves()==0','MaterialBatchTechniques.activeCount()==0 && MaterialBatchTechniques.shotCount()==0')
start=s.index(' private static void clean()');end=s.index(' private static void end()',start)
core=''' private static void clean(){MaterialBatchTechniques.clearAll();StickyState.releaseEverything("test");Roots.releaseEverything("test");for(var e:fixtures)e.discard();fixtures.clear();}
 private static void core(){arena();
  for(int f=0;f<FRUITS.length;f++){
   fruit(FRUITS[f]);String id=DevilFruitRegistry.movesFor(FRUITS[f]).getFirst().id();MinepieceModVariables.get(caster).PlayerEnergy=0;
   check("core/no-energy-"+NAMES[f],!cast(id) && MaterialBatchTechniques.activeCount()==0,"refusal has no new episode");
   fruit(FRUITS[f]);boolean ok=cast(id);DevilFruitPowerLogic.resetDevilFruitPowers(caster);
   check("core/reset-"+NAMES[f],ok && MaterialBatchTechniques.activeCount()==0 && MaterialBatchTechniques.shotCount()==0 && MaterialBatchTechniques.ringCount(caster)==0 && !SupaBlades.bladesActive(caster) && !DiamondBody.isActive(caster),"immediate owner cleanup");
   fruit(FRUITS[f]);ok=cast(id);MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);MaterialBatchTechniques.tick();
   check("core/fruit-change-"+NAMES[f],ok && MaterialBatchTechniques.activeCount()==0,"old fruit episode stopped");
   fruit(FRUITS[f]);ok=cast(id);caster.addEffect(new MobEffectInstance(MinepieceModMobEffects.SEA_POWER,80,0));MaterialBatchTechniques.tick();
   check("core/suppression-"+NAMES[f],ok && MaterialBatchTechniques.activeCount()==0,"suppression stops charge before damage");caster.removeAllEffects();
  }
  fruit(FRUITS[3]);caster.addEffect(new MobEffectInstance(MobEffects.STRENGTH,600,2));caster.addEffect(new MobEffectInstance(MobEffects.RESISTANCE,600,1));
  SupaBlades.activate(caster,8);for(int j=0;j<6;j++)SupaBlades.setStance(caster,j%2==0?SupaBlades.Stance.OFFENSIVE:SupaBlades.Stance.DEFENSIVE);
  check("core/stance-exclusive",SupaBlades.stanceEffectsOn(caster)==1,"only one owned modifier despite two external potions");
  for(int j=0;j<10;j++)SupaBlades.tickBlades();
  check("core/stance-expiry",!SupaBlades.bladesActive(caster) && SupaBlades.stanceEffectsOn(caster)==0 && caster.hasEffect(MobEffects.STRENGTH) && caster.getEffect(MobEffects.STRENGTH).getAmplifier()==2 && caster.hasEffect(MobEffects.RESISTANCE),"expiry removes owned posture, preserves both potions");caster.removeAllEffects();
  fruit(FRUITS[0]);normal=mob(680.5,84.5);StickyState.stick(normal,caster);for(int j=0;j<20;j++){StickyState.tickAll();StickyState.stick(normal,caster);}check("core/sticky-bounded-refresh",StickyState.remaining(normal)<=80,"refresh cannot reset100tick maximum");
  MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);StickyState.tickAll();check("core/sticky-fruit-release",!StickyState.isSticky(normal),"source fruit identity revalidated on native service tick");normal.discard();
  fruit(FRUITS[1]);normal=mob(680.5,84.5);Roots.entangle(normal,caster);MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);Roots.tickHeld();check("core/root-fruit-release",!Roots.isHeld(normal),"source fruit identity revalidated");normal.discard();
  fruit(FRUITS[2]);caster.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION,600,0));double armor=caster.getAttributeValue(Attributes.ARMOR);DiamondBody.toggle(caster);double bonus=caster.getAttributeValue(Attributes.ARMOR)-armor;
  check("core/diamond-cap",bonus<=br.com.oneminepiece.opworldlike.kira.DevilFruitKiraConfig.maxReductionPercent()/4.0+.001,"bonus bounded against native armor/25 upper bound");
  MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);DiamondBody.tick(caster);check("core/diamond-fruit-release",!DiamondBody.isActive(caster) && caster.getAttributeValue(Attributes.ARMOR)==armor && caster.hasEffect(MobEffects.NIGHT_VISION),"only own armor removed on change");
  fruit(FRUITS[2]);normal=mob(680.5,84.5);normal.setPermanentlyInvulnerable(true);Vec3 velocity=normal.getDeltaMovement();check("core/refused-no-control",!CombatSkillLogic.hurtAndConfirm(normal,world,world.damageSources().playerAttack(caster),20) && normal.getDeltaMovement().equals(velocity) && !Roots.isHeld(normal) && !StickyState.isSticky(normal),"native refusal remains side-effect free");normal.discard();
  for(int i=0;i<FRUITS.length;i++){fruit(FRUITS[i]);BlockPos at=new BlockPos(680,101,83);world.setBlockAndUpdate(at,Blocks.CHEST.defaultBlockState());var chest=(net.minecraft.world.level.block.entity.ChestBlockEntity)world.getBlockEntity(at);chest.setItem(0,new ItemStack(Items.DIAMOND,7));for(var move:DevilFruitRegistry.movesFor(FRUITS[i]))TechniqueTerrain.begin(caster,move).impact(new Vec3(680.5,101.5,82.99));check("core/chest-"+NAMES[i],world.getBlockEntity(at)==chest && chest.getItem(0).getCount()==7,"all terrain policies preserve the exact inventory");world.setBlockAndUpdate(at,Blocks.AIR.defaultBlockState());}
  clean();
 }
'''
s=s[:start]+core+s[end:]
(folder/'Batch03TestCommands.java').write_text(s,encoding='utf-8')
p=P/'src/main/java/br/com/oneminepiece/opworldlike/OmpWorld.java';s=p.read_text(encoding='utf-8');needle='net.mcreator.minepiece.command.Batch02TestCommands.register(dispatcher);';assert needle in s;s=s.replace(needle,needle+'\n          net.mcreator.minepiece.command.Batch03TestCommands.register(dispatcher);');p.write_text(s,encoding='utf-8')
print('B03 native tests authored')
