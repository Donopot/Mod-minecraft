package net.mcreator.minepiece.procedures;

import java.util.Map;
import java.util.Set;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

/** Explicit fruit-scoped ownership for handlers whose boolean can represent a refusal. */
public final class CustomTechniqueRoutes {
   private CustomTechniqueRoutes() { }
   private static final Map<String,Set<String>> ROUTES = Map.ofEntries(
      entry("gura_gura","attacksweep","overheadsuper","gekishin","kabutowari","tenchimeido","kaishin"),
      entry("aro_aro","arrow","move","combo","passive"),entry("bisu_bisu","crashpretzel","biscuitwarrior","biscuitwarrior2","biscuit"),
      entry("riki_riki","galleonlariat","attackoverhead","hadoelbow","stonethrowing","stonethrowinglarge"),
      entry("soru_soru","raitei","hebunrifire"),
      entry("supa_supa","atomicspurt","sparbreak","projectileslash","sparklingdaisy"),
      entry("uta_uta","posing","summontotmusica"),
      entry("nikyu_nikyu","padho","ursusshock","assault","kumapunch","passive"),
      entry("yami_yami","kurouzu","blackhole","darkball","darkmatter"),
      entry("pika_pika","laser1","magatama","laser2","yatanokagami","amanomurakumo","passive","kick","speedispower"),
      entry("ope_ope","stoneopeope","room","shambles","amputation"),
      entry("mori_mori","rootsurround","rootgiant","assault"),
      entry("moku_moku","whiteblow","whitevine","whitelauncher","whiteout"),entry("mochi_mochi","mochibuild"),
      entry("kiro_kiro","weightmanipulation","attackjump","attackoverhead","kirooverhead"),entry("kira_kira","diamondbody"),entry("kage_kage","shadowasgard"),
      entry("inu_inu_wolf","soru","okamihajiki","poursuite"),
      entry("inu_inu_okuchi_no_makami","namujihyoga","hyogaheki"),
      entry("imu","imuarrow"),entry("horo_horo","negativehollow","minihollow","tokuhoro","rap","kumac"),entry("hito_hito_daibutsu","impactwave"),
      entry("mero_mero","pistolkiss","roundhousekick","rotatekick","perfumefemur"),entry("zushi_zushi","attackspeed","gravito","moko","meteor","meteorsmall","rubble"),
      entry("suna_suna","desertspada","barjan","sables","desertlaspada","grounddeath"),entry("mera_mera","hiken","higan","hibashira","kagero","hotarubi","kaenryuo","daienkai"),
      entry("magu_magu","daifunka","inugamiguren","kageromagumagu","volcanicexplosion","ryuseikazan"),entry("hie_hie","icepartisan","pheasantbeak","iceball","iceage","icespikes"),
      entry("goro_goro","vari","elthor","sango","mamaragan","karikrash","passive","raigo"),entry("bomu_bomu","punchbomb","kickbomb","bomboverhead","zenshinkibaku","nosefancycannon")
   );
   private static Map.Entry<String,Set<String>> entry(String fruit,String... ids) { return Map.entry(fruit,Set.of(ids)); }
   public static boolean handles(ItemStack fruit,DevilFruitMove move) {
      if(MythicalZoanTechniques.handles(fruit))return true;
      if(ZoanTechniqueEpisodes.handles(fruit))return true;
      if(MythicBatchTechniques.handles(fruit))return true;
      String full=BuiltInRegistries.ITEM.getKey(fruit.getItem()).toString();
      String key=full.startsWith("minepiece:item_devil_fruit_")?full.substring("minepiece:item_devil_fruit_".length()):full;
      if(ROUTES.getOrDefault(key,Set.of()).contains(move.id()))return true;
      if((key.equals("gomu_gomu")||key.equals("mochi_mochi")) && GomuArmStrike.handles(move.id()))return true;
      if(key.equals("gura_gura") && SeismicWave.isWave(move.id()))return true;
      // These fruits return from their own handlers before the shared Zoan fallback.
      if(Set.of("inu_inu_wolf","inu_inu_okuchi_no_makami","hito_hito_daibutsu").contains(key))return false;
      return DevilFruitPowerLogic.family(fruit.getItem())==DevilFruitPowerLogic.Family.ZOAN
         && (ZoanMovement.isMovementMove(move.id())||(move.type()==MoveType.MELEE && ZoanClaw.isComboMove(move.id())));
   }
}
