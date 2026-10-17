"""Author the initial independent B06 native acceptance fixture; do not rerun after later test edits."""
from pathlib import Path
P=Path(__file__).resolve().parents[1];J=P/'src/main/java/net/mcreator/minepiece/command'
old=(J/'Batch05TestCommands.java').read_text(encoding='utf-8')
imports=old[:old.index('/** Real server ticks')]
helpers=old[old.index(' private static void fruit('):old.index(' private static void begin(')]
helpers=helpers.replace('680.5,100,80.5','680.5,100,80.5')
source=imports+'''/** B06 acceptance: inserted native victims, actual server ticks and full beast dimensions. */
public final class Batch06TestCommands {
 private static final Item[] FRUITS=ZoanTechniqueEpisodes.FRUITS;
 private static final String[] NAMES=ZoanTechniqueEpisodes.NAMES;
 private static final Set<String> UTILITY=Set.of("soru","poursuite","kamiezanshin");
 private static final List<Item> fruits=new ArrayList<>();private static final List<DevilFruitMove> moves=new ArrayList<>();private static final List<String> names=new ArrayList<>();private static final List<LivingEntity> fixtures=new ArrayList<>();private static final List<ServerPlayer> forms=new ArrayList<>();
 private static ServerLevel world;private static ServerPlayer caster;private static Mob normal,ally,refused,outside;
 private static int scene,age,passed,failed,terrainBefore,life=-1;private static boolean active,attributed;private static Vec3 start;private static double before;
 private static void check(String id,boolean ok,String detail){if(ok)passed++;else failed++;org.slf4j.LoggerFactory.getLogger("Batch06").info("[dbatch06] [{}] {} | {}",ok?"PASS":"FAIL",id,detail);}
 public static void register(CommandDispatcher<CommandSourceStack> dispatcher){if(!Boolean.getBoolean("opworldlike.repairTests"))return;
  dispatcher.register(Commands.literal("dbatch06").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(c->start(c.getSource())));
  ServerTickEvents.END_SERVER_TICK.register(server->{if(!active)return;try{
   if(life>=0){if(++life>=1210){for(int f=0;f<forms.size();f++){var p=forms.get(f);check("life/expiry-"+NAMES[f],!ZoanForms.has(p)&&MinepieceModVariables.get(p).player_form==0&&Math.abs(p.getScale()-1)<.001,"native1200ticks form expiry");DevilFruitPowerLogic.resetDevilFruitPowers(p);MinepieceModVariables.clear(p);p.discard();}forms.clear();end();}return;}
   age++;caster.getCooldowns().tick();if(normal.getHealth()<500&&normal.getLastHurtByMob()==caster)attributed=true;
   String id=moves.get(scene/5).id(),key=names.get(scene/5)+"/"+id;
   if(age==2&&scene%5==0)check(key+"/telegraph",normal.getHealth()==500,"no hit before preparation");
   if(id.equals("shiganmadara")&&scene%5==0&&(age==11||age==23||age==35))check(key+"/cadence-"+age,Math.abs(500-normal.getHealth()-(age==11?4:age==23?8:12))<.02,"actual HP="+normal.getHealth());
   if(age<120)return;finish();clean();scene++;age=0;if(scene==moves.size()*5){core();life=0;}else begin();
  }catch(Throwable t){check("fixture/exception",false,t.toString());clean();end();}});
 }
 private static int start(CommandSourceStack source){if(active)return 0;fruits.clear();moves.clear();names.clear();for(int f=0;f<FRUITS.length;f++)for(var m:DevilFruitRegistry.movesFor(FRUITS[f])){fruits.add(FRUITS[f]);moves.add(m);names.add(NAMES[f]);}world=source.getServer().overworld();caster=FakePlayer.get(world,new GameProfile(UUID.nameUUIDFromBytes("Batch06Acceptance".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"Batch06Acceptance"));passed=failed=scene=age=0;life=-1;active=true;begin();return 1;}
'''+helpers+'''
 private static void begin(){int i=scene/5;fruit(fruits.get(i));arena();start=caster.position();String id=moves.get(i).id();normal=mob(680.5,id.equals("attackjump")?87.5:83);refused=mob(682.5,83);refused.setPermanentlyInvulnerable(true);outside=mob(746.5,145.5);
  var own=new SummonEntity(MinepieceModEntities.SUMMON,world);own.setOwnerEntity(caster);own.setNoAi(true);own.setNoGravity(true);own.lifetime(500);own.getAttribute(Attributes.MAX_HEALTH).setBaseValue(500);own.setHealth(500);own.setPos(678.5,100,83);world.addFreshEntity(own);ally=own;fixtures.add(own);
  if(scene%5==1||scene%5==2)for(int x=674;x<=689;x++)for(int y=100;y<=110;y++)world.setBlockAndUpdate(new BlockPos(x,y,82),scene%5==1?Blocks.OBSIDIAN.defaultBlockState():Blocks.STONE.defaultBlockState());
  if(scene%5==3){normal.setPos(746.5,100,145.5);ally.setPos(680.5,100,83);}if(scene%5==4){normal.setPos(746.5,100,145.5);refused.setPos(680.5,100,83);}
  attributed=false;terrainBefore=TechniqueTerrain.brokenTotal(TechniqueTerrain.fruitKey(fruits.get(i))+"/"+id);before=MinepieceModVariables.get(caster).PlayerEnergy;boolean accepted=cast(id);String key=names.get(i)+"/"+id;
  if(scene%5==0){check(key+"/cost",accepted&&MinepieceModVariables.get(caster).PlayerEnergy==before-moves.get(i).energyCost(),"debit exactly once");double e=MinepieceModVariables.get(caster).PlayerEnergy;check(key+"/cooldown",!cast(id)&&e==MinepieceModVariables.get(caster).PlayerEnergy,"spam no debit");check(key+"/animation",TechniqueAnimations.nameFor(moves.get(i),caster).equals("b06_"+names.get(i)+"_"+id)&&TechniqueAnimations.fichierPresent("b06_"+names.get(i)+"_"+id),"finite owned gesture delivered");check(key+"/ticking",world.isPositionEntityTicking(normal.blockPosition()),"victim inserted in ticking chunk");}
 }
 private static void finish(){int i=scene/5;var m=moves.get(i);String id=m.id(),key=names.get(i)+"/"+id;
  if(scene%5>=3){var t=scene%5==3?ally:refused;check(key+(scene%5==3?"/ally-on-axis":"/refused-on-axis"),t.getHealth()==500&&!ImuCommand.active(t),"sole axis target survives without control");return;}
  if(scene%5==1){check(key+"/wall",normal.getHealth()==500&&!ImuCommand.active(normal),"obsidian cover; HP="+normal.getHealth());return;}
  if(scene%5==2){var policy=TechniqueTerrain.policy(fruits.get(i),id);int broken=TechniqueTerrain.brokenTotal(TechniqueTerrain.fruitKey(fruits.get(i))+"/"+id)-terrainBefore;check(key+"/terrain",policy!=null&&(policy.mode()==TechniqueTerrain.Mode.DESTRUCTIVE?broken>0&&broken<=policy.blocks():broken==0),"actual changed="+broken+" policy="+policy);return;}
  if(UTILITY.contains(id)){check(key+"/peaceful",normal.getHealth()==500,"utility never directly damages");if(id.equals("soru")||id.equals("poursuite"))check(key+"/movement",caster.position().distanceTo(start)>.3&&caster.position().distanceTo(start)<=6.1,"actual safe displacement="+caster.position().distanceTo(start));}
  else{check(key+"/damage",Math.abs(500-normal.getHealth()-m.damage())<.02,"actual HP="+normal.getHealth()+" budget="+m.damage());check(key+"/attribution",attributed,"native accepted damage credited to owner");}
  check(key+"/ally",ally.getHealth()==500&&!ImuCommand.active(ally),"ally untouched");check(key+"/refused",refused.getHealth()==500&&!ImuCommand.active(refused),"refused hit has no control");check(key+"/outside",outside.getHealth()==500,"out of range");check(key+"/bounded",ZoanTechniqueEpisodes.activeCount()==0&&ZoanTechniqueEpisodes.shotCount()==0&&!ZoanTechniqueEpisodes.guard(caster),"casts/projectiles/guard end on native ticks");
 }
 private static void clean(){ZoanTechniqueEpisodes.clear();ZoanForms.clear();for(var e:fixtures)e.discard();fixtures.clear();}
 private static void core(){
  for(int f=0;f<FRUITS.length;f++){
   fruit(FRUITS[f]);MinepieceModVariables.get(caster).PlayerEnergy=0;check("core/no-energy-"+NAMES[f],!cast("shigan")&&ZoanTechniqueEpisodes.activeCount()==0,"no cost or effect");fruit(FRUITS[f]);boolean accepted=cast("shigan");DevilFruitPowerLogic.resetDevilFruitPowers(caster);check("core/reset-"+NAMES[f],accepted&&ZoanTechniqueEpisodes.activeCount()==0,"cancel before contact");fruit(FRUITS[f]);accepted=cast("shigan");MinepieceModVariables.get(caster).PlayerDevilFruit=new ItemStack(Items.APPLE);ZoanTechniqueEpisodes.tick();check("core/fruit-change-"+NAMES[f],accepted&&ZoanTechniqueEpisodes.activeCount()==0,"exact fruit authority");
   fruit(FRUITS[f]);arena();double energy=MinepieceModVariables.get(caster).PlayerEnergy;world.setBlockAndUpdate(new BlockPos(680,101,80),Blocks.OBSIDIAN.defaultBlockState());check("form/room-"+NAMES[f],!ZoanForms.cycle(caster)&&MinepieceModVariables.get(caster).PlayerEnergy==energy,"refusal before debit");world.setBlockAndUpdate(new BlockPos(680,101,80),Blocks.AIR.defaultBlockState());
   double speed=caster.getAttributeValue(Attributes.MOVEMENT_SPEED);check("form/hybrid-"+NAMES[f],ZoanForms.cycle(caster)&&ZoanForms.form(caster)==1,"hybrid first");check("form/beast-"+NAMES[f],ZoanForms.cycle(caster)&&ZoanForms.form(caster)==2&&ZoanForms.body(caster)!=null,"complete native beast");var body=ZoanForms.body(caster);check("form/dimensions-"+NAMES[f],body!=null&&Math.abs(caster.getBbWidth()-body.width())<.01&&Math.abs(caster.getBbHeight()-body.height())<.01&&Math.abs(caster.getEyeHeight()-body.eye())<.01,"native width/height/eyes="+caster.getBbWidth()+"/"+caster.getBbHeight()+"/"+caster.getEyeHeight());check("form/base-"+NAMES[f],ZoanForms.cycle(caster)&&ZoanForms.form(caster)==0&&Math.abs(caster.getScale()-1)<.001&&caster.getAttributeValue(Attributes.MOVEMENT_SPEED)==speed,"all own modifiers restored");
   var p=FakePlayer.get(world,new GameProfile(UUID.nameUUIDFromBytes(("Batch06Form"+f).getBytes(java.nio.charset.StandardCharsets.UTF_8)),"Batch06Form"+f));var v=MinepieceModVariables.get(p);v.PlayerDevilFruit=new ItemStack(FRUITS[f]);v.UseDevilAbilities=true;v.PlayerEnergy=v.PlayerEnergyMAX=2000;p.setHealth(20);p.teleportTo(680.5+f*6,100,100.5);p.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);p.getAbilities().invulnerable=false;check("life/begin-"+NAMES[f],ZoanForms.cycle(p),"maintained native form");forms.add(p);
  }
  fruit(FRUITS[4]);caster.getAbilities().mayfly=true;check("form/foreign-flight",ZoanForms.cycle(caster)&&!MinepieceModVariables.get(caster).zoanFlightGranted,"existing foreign mayfly untouched");ZoanForms.end(caster);check("form/foreign-flight-retained",caster.getAbilities().mayfly,"foreign grant survives form end");caster.getAbilities().mayfly=false;
  fruit(FRUITS[4]);check("form/owned-flight",ZoanForms.cycle(caster)&&caster.getAbilities().mayfly&&MinepieceModVariables.get(caster).zoanFlightGranted&&!caster.getAbilities().flying,"owned grant, double jump required");ZoanForms.end(caster);check("form/owned-flight-release",!caster.getAbilities().mayfly&&!MinepieceModVariables.get(caster).zoanFlightGranted,"only own grant revoked");
 }
 private static void end(){active=false;if(caster!=null){DevilFruitPowerLogic.resetDevilFruitPowers(caster);MinepieceModVariables.clear(caster);caster.discard();}org.slf4j.LoggerFactory.getLogger("Batch06").info("[dbatch06] END passed={} failed={} total={}",passed,failed,passed+failed);}
}
'''
(J/'Batch06TestCommands.java').write_text(source,encoding='utf-8')
print('B06_NATIVE_FIXTURE_AUTHORED')
