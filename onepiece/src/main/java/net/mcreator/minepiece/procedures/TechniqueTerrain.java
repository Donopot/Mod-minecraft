package net.mcreator.minepiece.procedures;

import java.util.*;
import java.nio.file.*;
import com.google.gson.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.*;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;

/** Reviewed, fruit-scoped terrain rules. All quotas are per cast, including sibling projectiles. */
public final class TechniqueTerrain {
 public enum Mode { NONE, TRANSFORM, DESTRUCTIVE }
 public record Policy(Mode mode,double radius,int blocks,boolean transforms,String reason) {}
 private static final Map<String,Policy> RULES=new LinkedHashMap<>();
 private static final Map<String,Integer> BROKEN=new HashMap<>();
 public static int brokenTotal(String key){return BROKEN.getOrDefault(key,0);}
 private static final Set<String> DISABLED=new HashSet<>();
 private static final Path CONFIG=Path.of("config","opworldlike","technique-terrain.json");
 static {
  destructive("ryu_ryu_brachiosaurus","attackoverhead",2,48,false,"AttackOverhead actual contact");destructive("ryu_ryu_brachiosaurus","brachiobober",3.5,96,false,"BrachioBober explicit BlockDestroy at landing");destructive("ryu_ryu_brachiosaurus","blackcofee",2.5,80,false,"BlackCofee beam explicit BlockDestroy");destructive("ryu_ryu_brachiosaurus","brachiolauncher",2.5,64,false,"MissileProjectileHitsBlock -> BlockDestroy;13.5 blast plus direct2.2");destructive("ryu_ryu_brachiosaurus","sparkingqueen",2,48,false,"ProjectileLaserRed uses ProjectileLaserProjectileHitsBlock");
  destructive("ryu_ryu_pachycephalosaurus","attackjump",3,64,false,"AttackJump -> AttackOverhead at actual landing");destructive("ryu_ryu_pachycephalosaurus","attacksweep",1.5,32,false,"AttackSweep");destructive("ryu_ryu_pachycephalosaurus","headbutt",2,48,false,"Headbutt -> StrongPunch contact");none("ryu_ryu_pachycephalosaurus","ulzugan","UlZugan RangeAttack moving contact; no BlockDestroy");destructive("ryu_ryu_pachycephalosaurus","ulmeteor",4,128,false,"UlMeteor actual landing explicit BlockDestroy");

  // B07 source helper traces and bounded permanent adaptations.
  for(String f:List.of("inu_inu_okuchi_no_makami","hito_hito_daibutsu","uo_uo_seiryu"))destructive(f,"attackoverhead",2,48,false,"AttackOverhead actual contact");
  for(String f:List.of("inu_inu_okuchi_no_makami","uo_uo_seiryu")){destructive(f,"attacksweep",1.5,32,false,"AttackSweep contact");destructive(f,"raimeihakke",2,48,false,"RaimeiHakke strong sword finishing contact");}
  destructive("inu_inu_okuchi_no_makami","narikabura",2,48,false,"AIEnergy actual collision");transform("inu_inu_okuchi_no_makami","namujihyoga","NamujiHyoga -> BlockToIce permanent path");destructive("inu_inu_okuchi_no_makami","himorogiri",3,80,false,"Himorogiri explicit BlockDestroy on landing");none("inu_inu_okuchi_no_makami","hyogaheki","Port-added bounded personal armor, no terrain");
  destructive("hito_hito_daibutsu","brilliantpunk",2,48,false,"Strong punch finishing contact");destructive("hito_hito_daibutsu","impactball",2.5,64,false,"AI energy collision");destructive("hito_hito_daibutsu","overheadsuper",3,96,false,"AttackOverheadSuper contact");destructive("hito_hito_daibutsu","impactwave",3,96,false,"ImpactWave -> AIImpactWave -> BlockDestroy");
  destructive("tori_tori_phoenix","attackspeed",1.5,32,false,"AttackSpeed/AttackStrongPunch ending contact");destructive("tori_tori_phoenix","ongle",1.5,32,false,"Ongle/AttackStrongPunch ending contact");destructive("tori_tori_phoenix","bluebird",2.5,64,false,"BlueBird DaiFunka -> AIFist actual impact");destructive("tori_tori_phoenix","marcotornado",2,80,false,"MarcoTornado explicit BlockDestroy; shared quota");destructive("tori_tori_phoenix","hooin",3,80,false,"HooIn explicit landing BlockDestroy");destructive("tori_tori_phoenix","fujiazami",2,48,false,"Fujiazami actual sweep BlockDestroy");
  destructive("uo_uo_seiryu","kongokabura",2.5,64,false,"AIEnergy collision");destructive("uo_uo_seiryu","kaifu",2,96,false,"Three SkillProjectileSlash siblings share quota");destructive("uo_uo_seiryu","borobreath",3,128,false,"BoroBreath explicit BlockDestroy along active ray");destructive("uo_uo_seiryu","kosanze",4,128,false,"Kosanze explicit landing BlockDestroy");destructive("uo_uo_seiryu","ikoku",3,128,false,"Ikoku traveling pressure wave");
  destructive("ryu_ryu_pteranodon","attackspeed",1.5,32,false,"AttackSpeed strong punch finishing contact");none("ryu_ryu_pteranodon","tankyudon","StepBlock + RangeAttack; beak lunge never tunnels");destructive("ryu_ryu_pteranodon","barizodon",2,80,false,"SkillProjectileSlash -> AIProjectileSlash actual collision");

  // B06: permanent bounded contacts; source trace and playability adaptations in BATCH-06.md.
  for(String f:List.of("inu_inu_wolf","neko_neko_leopard","neko_neko_sabertiger","ushi_ushi_giraffe","batto_batto")){
   destructive(f,"shigan",1.2,24,false,"Shigan RangeAttack + BlockDestroy; contact only");
   if(!f.equals("neko_neko_sabertiger")){none(f,"soru","StepBlock movement never tunnels");destructive(f,"rankyaku",1.5,40,false,"Finite physical slash impact; projectile helper adaptation");}
  }
  destructive("inu_inu_wolf","lupusfall",2,48,false,"Wolf projectile impact, bounded permanent adaptation");destructive("inu_inu_wolf","okamihajiki",1.5,32,false,"AttackStrongPunch contact");destructive("inu_inu_wolf","jushigan",1.5,32,false,"Strong/weak punch helpers");destructive("inu_inu_wolf","localareanetwork",1.5,40,false,"Wolf projectiles share one permanent quota");none("inu_inu_wolf","poursuite","Mod-designed chase never tunnels");
  destructive("neko_neko_leopard","gaicho",2,64,false,"AIProjectileSlash actual collision");destructive("neko_neko_leopard","attackjump",3,64,false,"AttackJump delegates AttackOverhead at landing");none("neko_neko_leopard","shugan","Source range/dash has no block destruction");destructive("neko_neko_leopard","rokuogan",3,80,false,"Rokuogan moving pressure wave");
  destructive("neko_neko_sabertiger","attackspeed",1.5,32,false,"Source AttackStrongPunch/Sword finishing contact");destructive("neko_neko_sabertiger","shiganmadara",1,32,false,"Timed Shigan contacts share one quota");destructive("neko_neko_sabertiger","gagan",2,48,false,"Energy/strong punch physical impact");destructive("neko_neko_sabertiger","kibasen",1.5,32,false,"Strong punch finishing contact");
  destructive("ushi_ushi_giraffe","rankyakuran",1.5,56,false,"Three emitted slashes share a quota");destructive("ushi_ushi_giraffe","amanedachi",3,80,false,"Source RangeAttack + explicit BlockDestroy");none("batto_batto","tobushigan,kamiezanshin,yurushitene","Finger projectile, evasion/counter stance and range dash without source block destruction");

  destructive("kage_kage","attackoverhead",2,48,false,"AttackOverhead active contact");
  destructive("kage_kage","islandshatterpunch",5,192,false,"IslandShatterPunch destruction; bounded port44budget");
  none("kage_kage","brickbat,tsunotokage,doppelman,shadowasgard","Shadow swarm, lance, summon or transformation; no source block destruction");
  destructive("soru_soru","attackoverhead",2,48,false,"AttackOverhead");destructive("soru_soru","attacksweep",1.5,32,false,"AttackSweep");
  destructive("soru_soru","raitei",3,64,false,"Zeus commanded electric strike");destructive("soru_soru","hebunrifire",3,64,false,"Prometheus SkillTackle contact");
  destructive("soru_soru","furugora",3,72,false,"Electric moving wave");destructive("soru_soru","mamaraid",4,96,false,"Actual landing only");destructive("soru_soru","ikoku",4,128,false,"Moving pressure wave, shared per cast");
  destructive("uta_uta","noteattack",1.5,24,false,"AINote actual impact, siblings share quota");destructive("uta_uta","notelaser",1.5,32,false,"Three native source angles, siblings share quota");
  destructive("uta_uta","posing",2,48,false,"AIImpactWave active arrival");destructive("uta_uta","noteshield",2,48,false,"One bounded release wave");destructive("uta_uta","totmusicapunch",3,64,false,"Two arm contacts share quota");
  none("uta_uta","notewarrior","Soldier invocation itself preserves terrain");destructive("uta_uta","summontotmusica",3,96,false,"Owned Tot Musica energy impacts share lifetime quota");
  destructive("imu","imuarrow",2,48,false,"Source AttackStrongPunch delegates block destruction; port25budget");none("imu","silentcommand","Mod-defined control preserves terrain");destructive("imu","sovereigngrasp",2,48,false,"Mod-defined physical heavy impact");
  destructive("gomu_gomu","gomu_pistol",1.5,32,false,"Source strong punch real arm contact");destructive("gomu_gomu","gomu_bazooka",2,48,false,"Two-hand impact once");destructive("gomu_gomu","gomu_gatling",1,48,false,"Six timed contacts share quota");none("gomu_gomu","gear_change","Transformation preserves terrain");

  destructive("aro_aro","arrow",2,48,false,"AIArrow actual contact; bounded adaptation");
  none("aro_aro","move,combo,passive","Source movement/contacts have no block destruction");
  none("horo_horo","negativehollow,rap,kumac","Control, command and summon preserve blocks");
  destructive("horo_horo","minihollow",2,48,false,"AIMiniHollow commanded explosions share48blocks");
  destructive("horo_horo","tokuhoro",3.5,96,false,"Large AIMiniHollow explosion");
  none("mero_mero","pistolkiss,perfumefemur","Heart projectile and AttackSpeed dash preserve terrain");
  destructive("mero_mero","roundhousekick",1.5,32,false,"Karate roundhouse active contact");
  destructive("mero_mero","rotatekick",2,48,false,"RotateKick active phase");
  destructive("kiro_kiro","attackoverhead",3,64,false,"AttackOverhead contact");
  destructive("kiro_kiro","attackjump",3,64,false,"AttackJump delegates overhead on landing");
  destructive("kiro_kiro","kirooverhead",4,96,false,"Kiro overhead landing");
  destructive("kiro_kiro","weightmanipulation",3,64,false,"Source grounded erosion adapted to one heavy landing, never idle ticks");
  none("bisu_bisu","crashpretzel","CrashPretzel RangeAttack and Knockback; no BlockDestroy call");
  none("bisu_bisu","biscuitwarrior,biscuitwarrior2,biscuit","Summons and food creation preserve terrain");
  destructive("mera_mera","hiken",3,48,false,"AIFist; charged radius capped at3");
  destructive("mera_mera","higan",.9,8,false,"ProjectileLaser block impact; four shots share8 blocks");
  destructive("mera_mera","hibashira",4,96,false,"Hibashira BlockRange16; bounded adaptation");
  destructive("mera_mera","kagero",1,12,false,"Kagero active trail only");
  destructive("mera_mera","hotarubi",2.5,32,false,"Hotarubi expanding active field");
  destructive("mera_mera","kaenryuo",4,64,false,"KaenRyuo active stages");
  destructive("mera_mera","daienkai",6,160,false,"AIFlameBall actual impact");
  destructive("magu_magu","daifunka",2.5,48,true,"AIFist impact; permanent magma blocks, finite attributed field");
  destructive("magu_magu","inugamiguren",2,32,true,"AIFist impact");
  destructive("magu_magu","kageromagumagu",1.5,24,true,"KageroMagu active path and eruption");
  destructive("magu_magu","volcanicexplosion",4,96,true,"VolcanicExplosion active eruption");
  destructive("magu_magu","ryuseikazan",2,160,true,"AIFist2; twenty meteor impacts share160 blocks");
  destructive("hie_hie","icepartisan",2.5,64,false,"AIEntityBird collision");
  destructive("hie_hie","pheasantbeak",3,96,false,"AIEntityBird collision");
  transform("hie_hie","icespikes,iceball,iceage","BlockToIce; permanent ice");
  destructive("pika_pika","laser1",2.5,64,false,"Laser BlockRange4 -> radius2.5");
  destructive("pika_pika","laser2",4.5,160,false,"Laser2 BlockRange8; shared volley budget");
  destructive("pika_pika","kick",1.5,32,false,"Light kick active impact");
  destructive("pika_pika","magatama",1.5,96,false,"ProjectileLaser block impact; shared volley budget");
  destructive("pika_pika","speedispower",2,48,false,"SpeedIsPower active strike");
  none("pika_pika","yatanokagami,amanomurakumo,passive","Travel or weapon grant, no terrain impact");
  destructive("moku_moku","whiteblow",2.5,64,false,"AIFist collision");
  destructive("moku_moku","whitevine",3,96,false,"AITornado collision");
  destructive("moku_moku","whiteout",8,256,false,"WhiteOut active field; source radius16.5 reduced");
  none("moku_moku","whitelauncher","StepBlock is movement, not destruction");
  destructive("suna_suna","desertspada",1.5,64,false,"AIProjectileSlash collision");
  destructive("suna_suna","desertlaspada",2,96,false,"AIProjectileSlash collision");
  destructive("suna_suna","sables",3.5,160,false,"AITornado; bounded field");
  none("suna_suna","barjan","StepBlock and slash visual, no destruction path");
  transform("suna_suna","grounddeath","BlockToSand; permanent stable sandstone surface");
  destructive("goro_goro","vari",3,48,false,"Vari arrival only; blink never tunnels");
  destructive("goro_goro","elthor",3,48,false,"ElThor active strike");
  destructive("goro_goro","sango",1,16,false,"Sango accepted contacts only, four blocks per contact");
  none("goro_goro","mamaragan,passive","Storm has no explicit source BlockDestroy; blink has none");
  destructive("goro_goro","karikrash",2.5,32,false,"KariKrash front contact");
  destructive("goro_goro","raigo",4,128,false,"AIMeteorRaigo true collision");
  none("goro_goro","amaru","Transformation itself preserves terrain; technique budgets unchanged");
  destructive("gura_gura","attacksweep",1.5,24,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("gura_gura","overheadsuper",3,64,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("gura_gura","gekishin",2.5,96,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("gura_gura","kabutowari",1.5,16,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("gura_gura","tenchimeido",3,256,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("gura_gura","kaishin",3,96,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("bomu_bomu","nosefancycannon",2.5,48,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("bomu_bomu","kickbomb",3,64,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("bomu_bomu","punchbomb",3,64,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("bomu_bomu","bomboverhead",4,96,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("bomu_bomu","zenshinkibaku",5,160,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("zushi_zushi","attackspeed",1.5,32,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("zushi_zushi","gravito",3,96,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("zushi_zushi","meteorsmall",3,96,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("zushi_zushi","moko",6,160,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("zushi_zushi","rubble",2,64,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("zushi_zushi","meteor",5,192,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("nikyu_nikyu","padho",2,48,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("nikyu_nikyu","ursusshock",6,192,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("nikyu_nikyu","assault",2.5,64,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("nikyu_nikyu","kumapunch",2,64,false,"B02 source audit; active collision only, bounded adaptation");
  destructive("ope_ope","stoneopeope",2,96,false,"B02 source audit; active collision only, bounded adaptation");
  none("nikyu_nikyu","passive","Travel never tunnels");
  none("ope_ope","room,shambles,amputation","Surgery does not destroy terrain");
  none("yami_yami","kurouzu","Attraction, no destruction path");

  destructive("mochi_mochi","mochipistol",1.25,24,false,"AttackStrongPunch true contact, bounded adaptation");
  destructive("mochi_mochi","mochigatling",1,48,false,"AttackWeakPunch six native pulses share quota");
  none("mochi_mochi","yakimochi","AIFist mode -2 has no mode1 block destruction");
  destructive("mochi_mochi","zangirimochi",1.5,48,false,"AttackSpeed finishing contact");
  destructive("mochi_mochi","musodonatsu",1.25,64,false,"Two reactive rings share a lifetime terrain quota");
  transform("mochi_mochi","mochibuild","Plan-added honey construction, permanent24blocks");
  transform("mori_mori","root,rootgiant,assault,rootsurround","TreeRoot and BlockToGrass, permanent selective grass conversion");
  destructive("kira_kira","attackoverhead",4.5,128,false,"AttackOverhead actual active contact");
  destructive("kira_kira","brilliantpunk",1.5,48,false,"AttackStrongPunch after shoulder rush");
  destructive("kira_kira","diamondshard",1.25,48,false,"RockFragment handler; diamond identity adaptation");
  destructive("kira_kira","diamondavalanche",4.5,192,false,"Large RockFragment handler, bounded adaptation");
  none("kira_kira","diamondbody","Body coat never changes terrain");
  destructive("supa_supa","projectileslash",1.5,64,false,"AIProjectileSlash true collision");
  none("supa_supa","atomicspurt","Source StepBlock dash, charge explosion interaction NONE");
  destructive("supa_supa","sparklingdaisy",1,96,false,"Six source fan phases share quota, port bounded contact");
  destructive("supa_supa","sparbreak",1,64,false,"Bilateral slash active contact");
  destructive("riki_riki","attackoverhead",4.5,128,false,"AttackOverhead active impact");
  destructive("riki_riki","hadoelbow",3.5,128,false,"HadoElbow active impact");
  destructive("riki_riki","galleonlariat",3,96,false,"AIProjectileSweep actual collision");
  destructive("riki_riki","stonethrowing",1.25,48,false,"RockFragment actual collision; no held target required");
  destructive("riki_riki","stonethrowinglarge",4.5,192,false,"Large RockFragment actual collision");
  destructive("yami_yami","blackhole",8,384,false,"BlackHole active field; source radius24.5 reduced");
  destructive("yami_yami","darkball",3.5,128,false,"AIBall collision");
  destructive("yami_yami","darkmatter",4.5,192,false,"AIFlameBall collision");
 }
 private TechniqueTerrain(){}
 private static void destructive(String fruit,String id,double radius,int blocks,boolean transforms,String reason){RULES.put(fruit+"/"+id,new Policy(Mode.DESTRUCTIVE,radius,blocks,transforms,reason));}
 private static void none(String fruit,String ids,String reason){for(String id:ids.split(","))RULES.put(fruit+"/"+id,new Policy(Mode.NONE,0,0,false,reason));}
 private static void transform(String fruit,String ids,String reason){for(String id:ids.split(","))RULES.put(fruit+"/"+id,new Policy(Mode.TRANSFORM,0,0,true,reason));}
 public static String fruitKey(Item fruit){return BuiltInRegistries.ITEM.getKey(fruit).getPath().replace("item_devil_fruit_","");}
 public static Policy policy(Item fruit,String id){return RULES.get(fruitKey(fruit)+"/"+id);}
 public static Map<String,Policy> reviewed(){return Collections.unmodifiableMap(RULES);}
 public static void register(){ServerLifecycleEvents.SERVER_STARTED.register(s->load());ServerLifecycleEvents.SERVER_STOPPED.register(s->{DISABLED.clear();BROKEN.clear();});}
 public static void reload(){load();}
 private static void load(){DISABLED.clear();try{
  Files.createDirectories(CONFIG.getParent());
  if(!Files.exists(CONFIG)){var root=new JsonObject();root.addProperty("description","Disable selected reviewed permanent impacts; all placed terrain is permanent; attribution field durations remain in fruit configs.");var enabled=new JsonObject();RULES.forEach((k,v)->{if(v.mode==Mode.DESTRUCTIVE)enabled.addProperty(k,true);});root.add("enabled",enabled);Files.writeString(CONFIG,new GsonBuilder().setPrettyPrinting().create().toJson(root),java.nio.charset.StandardCharsets.UTF_8);}
  var root=JsonParser.parseString(Files.readString(CONFIG,java.nio.charset.StandardCharsets.UTF_8)).getAsJsonObject();
  if(root.has("enabled"))for(var e:root.getAsJsonObject("enabled").entrySet())if(e.getValue().isJsonPrimitive() && !e.getValue().getAsBoolean())DISABLED.add(e.getKey());
 }catch(Exception e){ // A malformed terrain config fails closed, never enables unexpected destruction.
  DISABLED.addAll(RULES.keySet());org.slf4j.LoggerFactory.getLogger("TechniqueTerrain").warn("Permanent terrain changes disabled: invalid config",e);
 }}
 public static Budget begin(ServerPlayer p,DevilFruitMove move){return begin(p,move.id());}
 public static Budget begin(ServerPlayer p,String id){return new Budget(p,DevilFruitPowerLogic.getDevilFruit(p).getItem(),id);}
 public static final class Budget {
  final ServerPlayer owner;final ServerLevel level;final Item fruit;final String key;final Policy policy;
  private final Set<BlockPos> visited=new HashSet<>();private int broken;private int transformedBlocks;
  private Budget(ServerPlayer p,Item f,String id){owner=p;level=(ServerLevel)p.level();fruit=f;key=fruitKey(f)+"/"+id;policy=RULES.get(key);}
  public int broken(){return broken;}
  public int transformedBlocks(){return transformedBlocks;}
  public int freezePath(Vec3 at,Vec3 direction,double distance){int before=transformedBlocks;Vec3 flat=new Vec3(direction.x,0,direction.z).normalize();for(double d=1;d<=Math.min(16,distance);d++){if(LightBeams.blocked(level,at.add(0,1,0),flat,d))break;replaceSurface(at.add(flat.scale(d)),0,Blocks.PACKED_ICE.defaultBlockState());}return transformedBlocks-before;}
  public int freezeWater(Vec3 at,double radius){
   if(policy==null || policy.mode!=Mode.TRANSFORM || !BatchRules.usable(owner,level,fruit))return 0;int before=transformedBlocks;int r=(int)Math.ceil(Math.min(8,radius));BlockPos c=BlockPos.containing(at);
   for(var column:BlockPos.betweenClosed(c.offset(-r,0,-r),c.offset(r,0,r))){if(column.distSqr(c)>r*r || transformedBlocks>=128)continue;
    for(int dy=0;dy<=2;dy++){BlockPos pos=column.below(dy);if(!level.hasChunkAt(pos) || visited.contains(pos) || PowerBlockService.isTrackedAny(level,pos))continue;
     if(!level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER))continue;
     Vec3 top=Vec3.atCenterOf(pos).add(0,.51,0);var ray=level.clip(new ClipContext(at.add(0,1.62,0),top,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,owner));if(ray.getType()!=HitResult.Type.MISS && !ray.getBlockPos().equals(pos))break;
     int count=PowerBlockService.activeCountFor(owner.getUUID());PowerBlockService.place(level,pos,Blocks.PACKED_ICE.defaultBlockState(),200,owner.getUUID());if(PowerBlockService.activeCountFor(owner.getUUID())>count){visited.add(pos);transformedBlocks++;}break;
    }
   }return transformedBlocks-before;
  }
  public int replaceSurface(Vec3 at,double radius,BlockState replacement){
   if(policy==null || policy.mode!=Mode.TRANSFORM || !BatchRules.usable(owner,level,fruit) || transformedBlocks>=128)return 0;int before=transformedBlocks;
   int r=(int)Math.ceil(Math.min(16,radius));BlockPos c=BlockPos.containing(at);
   var cells=new ArrayList<BlockPos>();for(var pos:BlockPos.betweenClosed(c.offset(-r,-1,-r),c.offset(r,-1,r)))if(pos.distSqr(c.below())<=r*r)cells.add(pos.immutable());
   cells.sort(Comparator.comparingDouble(pos->pos.distSqr(c)));
   for(var pos:cells){if(transformedBlocks>=128)break;if(!level.hasChunkAt(pos) || visited.contains(pos) || PowerBlockService.isTrackedAny(level,pos))continue;var state=level.getBlockState(pos);
    if(!(state.is(net.minecraft.tags.BlockTags.DIRT) || state.is(net.minecraft.tags.BlockTags.BASE_STONE_OVERWORLD) || state.is(net.minecraft.tags.BlockTags.SAND)))continue;
    if(LightBeams.blocked(level,at.add(0,.8,0),Vec3.atCenterOf(pos).subtract(at.add(0,.8,0)).normalize(),Math.max(0,Vec3.atCenterOf(pos).distanceTo(at.add(0,.8,0))-1)))continue;
    int count=PowerBlockService.activeCountFor(owner.getUUID());PowerBlockService.place(level,pos,replacement,200,owner.getUUID());if(PowerBlockService.activeCountFor(owner.getUUID())>count){visited.add(pos);transformedBlocks++;}
   }return transformedBlocks-before;
  }
  public boolean valid(){return policy!=null && policy.mode==Mode.DESTRUCTIVE && !DISABLED.contains(key) && BatchRules.usable(owner,level,fruit) && !DevilFruitExplosions.isProtectedPosition(level,owner.blockPosition());}
  /** Call AFTER damage/cover resolution, at the real collision or active wave front. */
  public int impact(Vec3 at){return impact(at,policy==null?0:policy.radius,policy==null?0:policy.blocks);}
  public int impact(Vec3 at,double requestedRadius,int localLimit){
   if(!valid() || !level.hasChunkAt(BlockPos.containing(at)) || !Double.isFinite(at.x+at.y+at.z) || broken>=policy.blocks || DevilFruitExplosions.isProtectedPosition(level,BlockPos.containing(at)))return 0;
   double r=Math.min(policy.radius,Math.max(0,requestedRadius));int span=(int)Math.ceil(r);BlockPos c=BlockPos.containing(at);int before=broken;
   var candidates=new ArrayList<BlockPos>();
   for(var pos:BlockPos.betweenClosed(c.offset(-span,-span,-span),c.offset(span,span,span)))if(Vec3.atCenterOf(pos).distanceToSqr(at)<=r*r)candidates.add(pos.immutable());
   candidates.sort(Comparator.comparingDouble(p->Vec3.atCenterOf(p).distanceToSqr(at)));
   for(var pos:candidates){if(broken>=policy.blocks || broken-before>=Math.max(0,localLimit))break;
    if(!level.hasChunkAt(pos) || !level.getWorldBorder().isWithinBounds(pos) || pos.getY()<level.getMinY() || pos.getY()>=level.getMaxY() || visited.contains(pos))continue;
    if(DevilFruitExplosions.isProtectedPosition(level,pos) || PowerBlockService.isTrackedAny(level,pos))continue;
    var state=level.getBlockState(pos);
    if(state.isAir() || !state.getFluidState().isEmpty() || state.hasBlockEntity() || level.getBlockEntity(pos)!=null || state.getDestroySpeed(level,pos)<0 || state.getBlock().getExplosionResistance()>6)continue;
    // A chest, a claim or resistant wall must not be tunnelled through by a spherical selection.
    var obstruction=level.clip(new ClipContext(at,Vec3.atCenterOf(pos),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,owner));
    if(obstruction.getType()==HitResult.Type.BLOCK && !obstruction.getBlockPos().equals(pos))continue;
    visited.add(pos);if(level.destroyBlock(pos,false)){broken++;BROKEN.merge(key,1,Integer::sum);}
   }
   return broken-before;
  }
 }
}
