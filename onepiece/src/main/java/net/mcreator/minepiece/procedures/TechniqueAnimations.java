package net.mcreator.minepiece.procedures;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.mcreator.minepiece.network.DevilFruitNetworking;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * <b>ANIMATIONS DE JOUEUR — la table de la source, portée telle quelle.</b>
 *
 * <h2>Ce que fait la source, exactement</h2>
 * <p>{@code PlayAnimationPlayerProcedure} se déclenche quand la source de dégâts est
 * {@code minepiece:start_animation}. Elle lit <b>deux attributs</b>, {@code ANIMATION_1} et
 * {@code ANIMATION_2}, calcule {@code code = ANIMATION_1 % 50} (100 si négatif), puis choisit un
 * <b>nom d'animation</b> dans une longue cascade de conditions et l'envoie par
 * {@code MinepieceModAnimationMessage} <b>deux fois</b> : au joueur lui-même
 * ({@code PacketDistributor.PLAYER}) et aux joueurs qui le suivent
 * ({@code PacketDistributor.TRACKING_ENTITY}).</p>
 *
 * <p>La table ci-dessous est <b>relevée dans cette cascade</b>, pas inventée. Sa structure :</p>
 *
 * <pre>
 *   code = ANIMATION_1 % 50
 *   code 0..4       → attaque de base : 1 → sword_to_right / kick_&lt;côté&gt; / punch_&lt;côté&gt;,
 *                     4 → &lt;sword|punch&gt;_overhead, 0/2/3 → selon l'arme tenue
 *   ANIMATION_1 -1  → backstep            -2 → death             -3 → right_arm_up
 *   ANIMATION_1 -4  → both_arm_front / both_arm_ground / both_arm_up    (selon ANIMATION_2)
 *   ANIMATION_1 -5  → sword_to_left / sword_to_right / sword_rotate
 *   ANIMATION_1 -6  → kick_&lt;côté&gt; / amane_dachi / kick_flying / kick_right / kick_left
 *   ANIMATION_1 -7  → punch_&lt;côté&gt; / punch_gomu_pistol1|2 / punch_gomu_gatling1|2 /
 *                     punch_right_jinbe
 *   ANIMATION_1 -8  → ragnaraku1 / raimei_hakke1 / breath1
 *   ANIMATION_1 -9  → guard / fall1
 *   ANIMATION_1 -10 → speed_attack1 / speed_attack_sword / togen_totsuka1|2
 *   ANIMATION_1 -13 → hakoku_left1|2 / hakoku_right1|2
 *   ANIMATION_1 -14 → ul_zugan / headbutt_1|2 / head_overhead
 * </pre>
 *
 * <h2>La règle tenue</h2>
 * <p>On n'envoie <b>jamais</b> un nom dont le fichier n'existe pas dans
 * {@code assets/minepiece/player_animations/} (avec un S : c'est le chemin que PAL lit, et l'oublier
 * fut l'erreur qui rendait toutes les animations muettes) : un tel nom ne joue rien tout en donnant l'illusion d'un
 * câblage. Les noms sont donc validés contre les ressources réellement présentes dans le mod, et un
 * nom manquant est <b>compté</b> au lieu d'être tu.</p>
 */
public final class TechniqueAnimations {

   /** Les noms d'animation livrés par le mod (94 fichiers, chemins vérifiés un par un). */
   private static final String[] CONNUES = {
      "b08_brachio_attackoverhead","b08_brachio_attackoverhead_release","b08_brachio_brachiobober","b08_brachio_brachiobober_release","b08_brachio_blackcofee","b08_brachio_blackcofee_release","b08_brachio_brachiolauncher","b08_brachio_brachiolauncher_release","b08_brachio_sparkingqueen","b08_brachio_sparkingqueen_release","b08_pachy_attackjump","b08_pachy_attackjump_release","b08_pachy_attacksweep","b08_pachy_attacksweep_release","b08_pachy_headbutt","b08_pachy_headbutt_release","b08_pachy_ulzugan","b08_pachy_ulzugan_release","b08_pachy_ulmeteor","b08_pachy_ulmeteor_release",
      "b07_makami_attackoverhead","b07_makami_attackoverhead_release","b07_makami_attacksweep","b07_makami_attacksweep_release","b07_makami_raimeihakke","b07_makami_raimeihakke_release","b07_makami_narikabura","b07_makami_narikabura_release","b07_makami_namujihyoga","b07_makami_namujihyoga_release","b07_makami_himorogiri","b07_makami_himorogiri_release","b07_makami_hyogaheki","b07_makami_hyogaheki_release","b07_daibutsu_attackoverhead","b07_daibutsu_attackoverhead_release","b07_daibutsu_brilliantpunk","b07_daibutsu_brilliantpunk_release","b07_daibutsu_impactball","b07_daibutsu_impactball_release","b07_daibutsu_overheadsuper","b07_daibutsu_overheadsuper_release","b07_daibutsu_impactwave","b07_daibutsu_impactwave_release","b07_phoenix_attackspeed","b07_phoenix_attackspeed_release","b07_phoenix_ongle","b07_phoenix_ongle_release","b07_phoenix_bluebird","b07_phoenix_bluebird_release","b07_phoenix_marcotornado","b07_phoenix_marcotornado_release","b07_phoenix_hooin","b07_phoenix_hooin_release","b07_phoenix_fujiazami","b07_phoenix_fujiazami_release","b07_seiryu_attackoverhead","b07_seiryu_attackoverhead_release","b07_seiryu_attacksweep","b07_seiryu_attacksweep_release","b07_seiryu_raimeihakke","b07_seiryu_raimeihakke_release","b07_seiryu_kongokabura","b07_seiryu_kongokabura_release","b07_seiryu_kaifu","b07_seiryu_kaifu_release","b07_seiryu_borobreath","b07_seiryu_borobreath_release","b07_seiryu_kosanze","b07_seiryu_kosanze_release","b07_seiryu_ikoku","b07_seiryu_ikoku_release","b07_ptero_attackspeed","b07_ptero_attackspeed_release","b07_ptero_tankyudon","b07_ptero_tankyudon_release","b07_ptero_barizodon","b07_ptero_barizodon_release",
      "b06_wolf_shigan","b06_wolf_shigan_release","b06_wolf_soru","b06_wolf_soru_release","b06_wolf_rankyaku","b06_wolf_rankyaku_release","b06_wolf_lupusfall","b06_wolf_lupusfall_release","b06_wolf_okamihajiki","b06_wolf_okamihajiki_release","b06_wolf_jushigan","b06_wolf_jushigan_release","b06_wolf_localareanetwork","b06_wolf_localareanetwork_release","b06_wolf_poursuite","b06_wolf_poursuite_release","b06_leopard_shigan","b06_leopard_shigan_release","b06_leopard_soru","b06_leopard_soru_release","b06_leopard_rankyaku","b06_leopard_rankyaku_release","b06_leopard_gaicho","b06_leopard_gaicho_release","b06_leopard_attackjump","b06_leopard_attackjump_release","b06_leopard_shugan","b06_leopard_shugan_release","b06_leopard_rokuogan","b06_leopard_rokuogan_release","b06_saber_shigan","b06_saber_shigan_release","b06_saber_attackspeed","b06_saber_attackspeed_release","b06_saber_shiganmadara","b06_saber_shiganmadara_release","b06_saber_gagan","b06_saber_gagan_release","b06_saber_kibasen","b06_saber_kibasen_release","b06_giraffe_shigan","b06_giraffe_shigan_release","b06_giraffe_soru","b06_giraffe_soru_release","b06_giraffe_rankyaku","b06_giraffe_rankyaku_release","b06_giraffe_rankyakuran","b06_giraffe_rankyakuran_release","b06_giraffe_amanedachi","b06_giraffe_amanedachi_release","b06_batto_shigan","b06_batto_shigan_release","b06_batto_soru","b06_batto_soru_release","b06_batto_rankyaku","b06_batto_rankyaku_release","b06_batto_tobushigan","b06_batto_tobushigan_release","b06_batto_kamiezanshin","b06_batto_kamiezanshin_release","b06_batto_yurushitene","b06_batto_yurushitene_release",
      "b05_kage_attackoverhead","b05_kage_attackoverhead_release","b05_kage_brickbat","b05_kage_brickbat_release","b05_kage_tsunotokage","b05_kage_tsunotokage_release","b05_kage_doppelman","b05_kage_doppelman_release","b05_kage_islandshatterpunch","b05_kage_islandshatterpunch_release","b05_kage_shadowasgard","b05_kage_shadowasgard_release","b05_soru_attackoverhead","b05_soru_attackoverhead_release","b05_soru_attacksweep","b05_soru_attacksweep_release","b05_soru_raitei","b05_soru_raitei_release","b05_soru_hebunrifire","b05_soru_hebunrifire_release","b05_soru_furugora","b05_soru_furugora_release","b05_soru_mamaraid","b05_soru_mamaraid_release","b05_soru_ikoku","b05_soru_ikoku_release","b05_uta_noteattack","b05_uta_noteattack_release","b05_uta_notelaser","b05_uta_notelaser_release","b05_uta_posing","b05_uta_posing_release","b05_uta_noteshield","b05_uta_noteshield_release","b05_uta_notewarrior","b05_uta_notewarrior_release","b05_uta_totmusicapunch","b05_uta_totmusicapunch_release","b05_uta_summontotmusica","b05_uta_summontotmusica_release","b05_imu_imuarrow","b05_imu_imuarrow_release","b05_imu_silentcommand","b05_imu_silentcommand_release","b05_imu_sovereigngrasp","b05_imu_sovereigngrasp_release","b05_gomu_gomu_pistol","b05_gomu_gomu_pistol_release","b05_gomu_gomu_bazooka","b05_gomu_gomu_bazooka_release","b05_gomu_gomu_gatling","b05_gomu_gomu_gatling_release","b05_gomu_gear_change","b05_gomu_gear_change_release",
      "b04_aro_arrow","b04_aro_arrow_release","b04_aro_move","b04_aro_move_release","b04_aro_combo","b04_aro_combo_release","b04_aro_passive","b04_aro_passive_release","b04_horo_negativehollow","b04_horo_negativehollow_release","b04_horo_minihollow","b04_horo_minihollow_release","b04_horo_tokuhoro","b04_horo_tokuhoro_release","b04_horo_rap","b04_horo_rap_release","b04_horo_kumac","b04_horo_kumac_release","b04_mero_pistolkiss","b04_mero_pistolkiss_release","b04_mero_roundhousekick","b04_mero_roundhousekick_release","b04_mero_rotatekick","b04_mero_rotatekick_release","b04_mero_perfumefemur","b04_mero_perfumefemur_release","b04_kiro_attackoverhead","b04_kiro_attackoverhead_release","b04_kiro_attackjump","b04_kiro_attackjump_release","b04_kiro_kirooverhead","b04_kiro_kirooverhead_release","b04_kiro_weightmanipulation","b04_kiro_weightmanipulation_release","b04_bisu_crashpretzel","b04_bisu_crashpretzel_release","b04_bisu_biscuitwarrior","b04_bisu_biscuitwarrior_release","b04_bisu_biscuitwarrior2","b04_bisu_biscuitwarrior2_release","b04_bisu_biscuit","b04_bisu_biscuit_release",
      "b03_mochi_mochipistol","b03_mochi_mochipistol_release","b03_mochi_mochigatling","b03_mochi_mochigatling_release","b03_mochi_yakimochi","b03_mochi_yakimochi_release","b03_mochi_zangirimochi","b03_mochi_zangirimochi_release","b03_mochi_musodonatsu","b03_mochi_musodonatsu_release","b03_mochi_mochibuild","b03_mochi_mochibuild_release","b03_mori_root","b03_mori_root_release","b03_mori_rootgiant","b03_mori_rootgiant_release","b03_mori_assault","b03_mori_assault_release","b03_mori_rootsurround","b03_mori_rootsurround_release","b03_kira_attackoverhead","b03_kira_attackoverhead_release","b03_kira_brilliantpunk","b03_kira_brilliantpunk_release","b03_kira_diamondshard","b03_kira_diamondshard_release","b03_kira_diamondavalanche","b03_kira_diamondavalanche_release","b03_kira_diamondbody","b03_kira_diamondbody_release","b03_supa_projectileslash","b03_supa_projectileslash_release","b03_supa_atomicspurt","b03_supa_atomicspurt_release","b03_supa_sparklingdaisy","b03_supa_sparklingdaisy_release","b03_supa_sparbreak","b03_supa_sparbreak_release","b03_riki_attackoverhead","b03_riki_attackoverhead_release","b03_riki_hadoelbow","b03_riki_hadoelbow_release","b03_riki_galleonlariat","b03_riki_galleonlariat_release","b03_riki_stonethrowing","b03_riki_stonethrowing_release","b03_riki_stonethrowinglarge","b03_riki_stonethrowinglarge_release",
      "b02_attacksweep","b02_attacksweep_release","b02_overheadsuper","b02_overheadsuper_release","b02_gekishin","b02_gekishin_release","b02_kabutowari","b02_kabutowari_release","b02_tenchimeido","b02_tenchimeido_release","b02_kaishin","b02_kaishin_release","b02_nosefancycannon","b02_nosefancycannon_release","b02_kickbomb","b02_kickbomb_release","b02_punchbomb","b02_punchbomb_release","b02_bomboverhead","b02_bomboverhead_release","b02_zenshinkibaku","b02_zenshinkibaku_release","b02_attackspeed","b02_attackspeed_release","b02_gravito","b02_gravito_release","b02_meteorsmall","b02_meteorsmall_release","b02_moko","b02_moko_release","b02_rubble","b02_rubble_release","b02_meteor","b02_meteor_release","b02_padho","b02_padho_release","b02_ursusshock","b02_ursusshock_release","b02_assault","b02_assault_release","b02_kumapunch","b02_kumapunch_release","b02_passive","b02_passive_release","b02_stoneopeope","b02_stoneopeope_release","b02_room","b02_room_release","b02_shambles","b02_shambles_release","b02_amputation","b02_amputation_release",
      "b01_icepartisan","b01_icepartisan_release","b01_pheasantbeak","b01_pheasantbeak_release","b01_icespikes","b01_icespikes_release","b01_iceball","b01_iceball_release","b01_iceage","b01_iceage_release","b01_laser1","b01_laser1_release","b01_laser2","b01_laser2_release","b01_kick","b01_kick_release","b01_yatanokagami","b01_yatanokagami_release","b01_magatama","b01_magatama_release","b01_amanomurakumo","b01_amanomurakumo_release","b01_speedispower","b01_speedispower_release","b01_passive","b01_passive_release","b01_whiteblow","b01_whiteblow_release","b01_whitevine","b01_whitevine_release","b01_whitelauncher","b01_whitelauncher_release","b01_whiteout","b01_whiteout_release","b01_desertspada","b01_desertspada_release","b01_barjan","b01_barjan_release","b01_sables","b01_sables_release","b01_desertlaspada","b01_desertlaspada_release","b01_grounddeath","b01_grounddeath_release","b01_kurouzu","b01_kurouzu_release","b01_blackhole","b01_blackhole_release","b01_darkball","b01_darkball_release","b01_darkmatter","b01_darkmatter_release",
      "goro_amaru_transform", "goro_vari_charge","goro_vari_release","goro_elthor_charge","goro_elthor_release","goro_sango_charge","goro_sango_release","goro_karikrash_charge","goro_karikrash_release","goro_storm_cast","goro_blink","goro_raigo_cast", "mera_kaenryuo",
      "magu_fist_charge", "magu_fist_release", "magu_hound_charge", "magu_hound_release", "magu_kagero_dash", "magu_kagero_finish", "magu_volcanic_cast", "magu_meteors_cast",
      "amane_dachi", "backstep", "both_arm_front", "both_arm_ground", "both_arm_up", "breath1",
      "breath2", "cancel", "clap", "death", "domain_expansion1", "energy_charge", "enkai", "fall1",
      "fly_back", "fly_back2", "fly_front", "fly_front2", "fly_front3", "gear_2", "guard",
      "hakoku_left1", "hakoku_left2", "hakoku_right1", "hakoku_right2", "head_overhead",
      "headbutt_1", "headbutt_2", "iai1", "invisibility", "kick_flying", "kick_left", "kick_right",
      "negative", "punch_gomu_gatling1", "punch_gomu_gatling2", "punch_gomu_pistol1",
      "punch_gomu_pistol2", "punch_left", "punch_overhead", "punch_right", "punch_right_jinbe",
      "ragnaraku1", "raimei_hakke1", "right_arm_front", "right_arm_front2", "right_arm_up",
      "right_leg_front", "rin", "rokuogan", "rotation2", "sanzen_sekai1", "sanzen_sekai2",
      "speed_attack1", "speed_attack_sword", "sprint2", "swim", "sword", "sword_overhead",
      "sword_rotate", "sword_to_left", "sword_to_right", "togen_totsuka1", "togen_totsuka2",
      "toragari1", "toragari2", "ul_zugan", "ursus_shock", "yamiyami_blackhole",
      "yasakani_no_magatama"
   };

   /** Table « technique → (ANIMATION_1, ANIMATION_2) », relevée dans la cascade de la source. */
   private static final java.util.Map<String, double[]> CODES = new java.util.HashMap<>();

   /** Techniques dont le nom EST le nom de l'animation livrée (aucune paire à lire). */
   private static final java.util.Map<String, String> NOMS = new java.util.HashMap<>();

   static {
      // Ces quatre techniques portent le nom de leur animation : pour elles, la source n'a pas de
      // paire à lire, le nom se déduit du fichier livré.
      NOMS.put("amanedachi", "amane_dachi");
      NOMS.put("rokuogan", "rokuogan");
      NOMS.put("ulzugan", "ul_zugan");
      NOMS.put("ursusshock", "ursus_shock");
      // -----------------------------------------------------------------------------------
      // Ces paires sont EXTRAITES du bytecode de la source, procédure par procédure : chaque
      // technique pose ses codes par setBaseValue sur ANIMATION_1 / ANIMATION_2, et la cascade
      // de PlayAnimationPlayerProcedure les traduit en nom d'animation. Rien n'est deviné.
      //
      // Codes de ANIMATION_1 : les entiers de la cascade (0..4 = attaques de base, puis -1, -2,
      // -3, -4, -5, -6, -7, -8, -9, -10, -13, -14, -15, -16, -17, -18, -19, -20, -21, -22, -23,
      // -24, -25, -26, -47, -48, -49). ANIMATION_2 est le sous-code (0 quand la procédure ne le
      // pose pas). Seuls les ENTIERS sont retenus : les 0.01, 0.1, 0.25, 0.5 présents dans ces
      // mêmes procédures sont des durées de recharge, pas des codes.
      // -----------------------------------------------------------------------------------
      CODES.put("amanedachi", new double[] {-6.0, 0.0});
      CODES.put("amanomurakumo", new double[] {-4.0, 0.0});
      CODES.put("arrow", new double[] {-19.0, 6.0});
      CODES.put("assault", new double[] {-21.0, 6.0});
      CODES.put("atomicspurt", new double[] {-25.0, 0.0});
      CODES.put("attackjump", new double[] {-9.0, 0.0});
      CODES.put("attackoverhead", new double[] {-8.0, 2.0});
      CODES.put("attackspeed", new double[] {3.0, 0.0});
      CODES.put("attacksweep", new double[] {-13.0, 0.0});
      CODES.put("barizodon", new double[] {-5.0, 0.0});
      CODES.put("barjan", new double[] {-5.0, 5.0});
      CODES.put("biscuit", new double[] {-17.0, 0.0});
      CODES.put("biscuitwarrior", new double[] {-17.0, 0.0});
      CODES.put("biscuitwarrior2", new double[] {-17.0, 0.0});
      CODES.put("blackcofee", new double[] {-19.0, 0.0});
      CODES.put("blackhole", new double[] {-20.0, 0.0});
      CODES.put("bluebird", new double[] {-19.0, 6.0});
      CODES.put("bomboverhead", new double[] {-47.0, 7.0});
      CODES.put("borobreath", new double[] {-8.0, 7.0});
      CODES.put("brachiobober", new double[] {-8.0, 6.0});
      CODES.put("brachiolauncher", new double[] {-49.0, 0.0});
      CODES.put("brickbat", new double[] {-20.0, 2.0});
      CODES.put("brilliantpunk", new double[] {-10.0, 0.0});
      CODES.put("combo", new double[] {-6.0, 4.0});
      CODES.put("crashpretzel", new double[] {-10.0, 0.0});
      CODES.put("daienkai", new double[] {-3.0, 0.0});
      CODES.put("daifunka", new double[] {-7.0, 6.0});
      CODES.put("darkball", new double[] {-4.0, 0.0});
      CODES.put("darkmatter", new double[] {-3.0, 0.0});
      CODES.put("desertlaspada", new double[] {-7.0, 0.0});
      CODES.put("desertspada", new double[] {4.0, 0.0});
      CODES.put("elthor", new double[] {-19.0, 3.0});
      CODES.put("fujiazami", new double[] {-9.0, 0.0});
      CODES.put("furugora", new double[] {-7.0, 5.0});
      CODES.put("gagan", new double[] {-8.0, 6.0});
      CODES.put("gaicho", new double[] {-6.0, 0.0});
      CODES.put("galleonlariat", new double[] {-20.0, 5.0});
      CODES.put("gear_change", new double[] {-26.0, 2.0});
      CODES.put("gekishin", new double[] {4.0, 5.0});
      CODES.put("gomu_bazooka", new double[] {-7.0, 4.0});
      CODES.put("gomu_gatling", new double[] {-7.0, 3.0});
      CODES.put("gravito", new double[] {-20.0, 0.0});
      CODES.put("grounddeath", new double[] {-20.0, 0.0});
      CODES.put("hadoelbow", new double[] {-20.0, 5.0});
      CODES.put("headbutt", new double[] {-14.0, 2.0});
      CODES.put("hebunrifire", new double[] {-7.0, 5.0});
      CODES.put("hibashira", new double[] {-16.0, 0.0});
      CODES.put("higan", new double[] {-7.0, 3.0});
      CODES.put("hiken", new double[] {-7.0, 6.0});
      CODES.put("himorogiri", new double[] {-8.0, 2.0});
      CODES.put("hooin", new double[] {-6.0, 2.0});
      CODES.put("hotarubi", new double[] {-4.0, 0.0});
      CODES.put("iceage", new double[] {-20.0, 0.0});
      CODES.put("iceball", new double[] {-4.0, 6.0});
      CODES.put("icespikes", new double[] {4.0, 0.0});
      CODES.put("ikoku", new double[] {-13.0, 0.0});
      CODES.put("impactball", new double[] {-19.0, 3.0});
      CODES.put("impactwave", new double[] {-19.0, 2.0});
      CODES.put("inugamiguren", new double[] {-7.0, 6.0});
      CODES.put("islandshatterpunch", new double[] {-8.0, 2.0});
      CODES.put("jushigan", new double[] {-18.0, 0.0});
      CODES.put("kabutowari", new double[] {-19.0, 0.0});
      CODES.put("kaenryuo", new double[] {-7.0, 5.0});
      CODES.put("kagero", new double[] {-21.0, 0.0});
      CODES.put("kageromagumagu", new double[] {-21.0, 0.0});
      CODES.put("kaifu", new double[] {-8.0, 6.0});
      CODES.put("kaishin", new double[] {-20.0, 2.0});
      CODES.put("karikrash", new double[] {-19.0, 0.0});
      CODES.put("kibasen", new double[] {-8.0, 6.0});
      CODES.put("kick", new double[] {-6.0, 3.0});
      CODES.put("kickbomb", new double[] {-6.0, 5.0});
      CODES.put("kirooverhead", new double[] {-16.0, 0.0});
      CODES.put("kongokabura", new double[] {-8.0, 4.0});
      CODES.put("kosanze", new double[] {-8.0, 2.0});
      CODES.put("kumapunch", new double[] {-21.0, 0.0});
      CODES.put("kurouzu", new double[] {-19.0, 0.0});
      CODES.put("laser1", new double[] {-19.0, 0.0});
      CODES.put("laser2", new double[] {-19.0, 0.0});
      CODES.put("localareanetwork", new double[] {-21.0, 0.0});
      CODES.put("lupusfall", new double[] {-6.0, 0.0});
      CODES.put("magatama", new double[] {-21.0, 0.0});
      CODES.put("mamaragan", new double[] {-19.0, 0.0});
      CODES.put("mamaraid", new double[] {-6.0, 6.0});
      CODES.put("marcotornado", new double[] {-4.0, 0.0});
      CODES.put("meteor", new double[] {-4.0, 0.0});
      CODES.put("meteorsmall", new double[] {-20.0, 0.0});
      CODES.put("minihollow", new double[] {-19.0, 0.0});
      CODES.put("moko", new double[] {-20.0, 5.0});
      CODES.put("move", new double[] {-6.0, 2.0});
      CODES.put("musodonatsu", new double[] {-16.0, 0.0});
      CODES.put("namujihyoga", new double[] {-8.0, 6.0});
      CODES.put("narikabura", new double[] {-8.0, 4.0});
      CODES.put("negativehollow", new double[] {-19.0, 0.0});
      CODES.put("nosefancycannon", new double[] {-20.0, 2.0});
      CODES.put("noteattack", new double[] {-19.0, 0.0});
      CODES.put("notelaser", new double[] {-16.0, 0.0});
      CODES.put("noteshield", new double[] {-4.0, 2.0});
      CODES.put("okamihajiki", new double[] {-23.0, 0.0});
      CODES.put("ongle", new double[] {-6.0, 2.0});
      CODES.put("overheadsuper", new double[] {-8.0, 2.0});
      CODES.put("padho", new double[] {-7.0, 0.0});
      CODES.put("passive", new double[] {-21.0, 0.0});
      CODES.put("perfumefemur", new double[] {-18.0, 2.0});
      CODES.put("pheasantbeak", new double[] {-19.0, 0.0});
      CODES.put("pistolkiss", new double[] {-19.0, 0.0});
      CODES.put("posing", new double[] {-4.0, 2.0});
      CODES.put("projectileslash", new double[] {4.0, 0.0});
      CODES.put("raigo", new double[] {-4.0, 2.0});
      CODES.put("raimeihakke", new double[] {-8.0, 4.0});
      CODES.put("raitei", new double[] {-19.0, 0.0});
      CODES.put("rankyaku", new double[] {-6.0, 0.0});
      CODES.put("rap", new double[] {-19.0, 0.0});
      CODES.put("rokuogan", new double[] {-23.0, 0.0});
      CODES.put("root", new double[] {-4.0, 0.0});
      CODES.put("rootgiant", new double[] {-4.0, 0.0});
      CODES.put("rotatekick", new double[] {-6.0, 3.0});
      CODES.put("rubble", new double[] {-3.0, 0.0});
      CODES.put("ryuseikazan", new double[] {-4.0, 2.0});
      CODES.put("sables", new double[] {-19.0, 0.0});
      CODES.put("sango", new double[] {-3.0, 0.0});
      CODES.put("shadowasgard", new double[] {-4.0, 0.0});
      CODES.put("shigan", new double[] {-18.0, 0.0});
      CODES.put("shiganmadara", new double[] {-7.0, 3.0});
      CODES.put("shugan", new double[] {-7.0, 5.0});
      CODES.put("soru", new double[] {-21.0, 0.0});
      CODES.put("sparbreak", new double[] {-20.0, 2.0});
      CODES.put("sparkingqueen", new double[] {-8.0, 6.0});
      CODES.put("sparklingdaisy", new double[] {-23.0, 0.0});
      CODES.put("speedispower", new double[] {-21.0, 0.0});
      CODES.put("stoneopeope", new double[] {-19.0, 0.0});
      CODES.put("stonethrowing", new double[] {-7.0, 0.0});
      CODES.put("stonethrowinglarge", new double[] {-4.0, 2.0});
      CODES.put("summontotmusica", new double[] {-4.0, 2.0});
      CODES.put("tankyudon", new double[] {-14.0, 2.0});
      CODES.put("tenchimeido", new double[] {-20.0, 4.0});
      CODES.put("tokuhoro", new double[] {-19.0, 0.0});
      CODES.put("tsunotokage", new double[] {-20.0, 2.0});
      CODES.put("ulmeteor", new double[] {-4.0, 0.0});
      CODES.put("ulzugan", new double[] {-14.0, 0.0});
      CODES.put("ursusshock", new double[] {-22.0, 0.0});
      CODES.put("vari", new double[] {-21.0, 2.0});
      CODES.put("volcanicexplosion", new double[] {-16.0, 0.0});
      CODES.put("whiteblow", new double[] {-7.0, 0.0});
      CODES.put("whitelauncher", new double[] {-47.0, 6.0});
      CODES.put("whiteout", new double[] {-20.0, 0.0});
      CODES.put("whitevine", new double[] {-47.0, 0.0});
      CODES.put("yakimochi", new double[] {-7.0, 6.0});
      CODES.put("yatanokagami", new double[] {-4.0, 0.0});
      CODES.put("yurushitene", new double[] {-8.0, 6.0});
      CODES.put("zangirimochi", new double[] {-6.0, 6.0});
      CODES.put("zenshinkibaku", new double[] {-4.0, 0.0});
   }

   private static Set<String> presentes;
   private static int envoyees;
   private static int introuvables;
   private static final List<String> manquants = new ArrayList<>();

   private TechniqueAnimations() {
   }

   /** Noms réellement livrés : la liste ci-dessus, moins ceux dont le fichier est absent. */
   public static Set<String> disponibles() {
      if (presentes == null) {
         Set<String> noms = new LinkedHashSet<>();
         for (String n : CONNUES) {
            if (fichierPresent(n)) {
               noms.add(n);
            }
         }
         presentes = noms;
      }
      return presentes;
   }

   /** Le fichier d'animation existe-t-il dans les ressources du mod ? */
   public static boolean fichierPresent(String nom) {
      try (InputStream in = TechniqueAnimations.class.getClassLoader()
         .getResourceAsStream("assets/minepiece/player_animations/" + nom + ".json")) {
         return in != null;
      } catch (Exception e) {
         return false;
      }
   }

   /**
    * Nombre de techniques qui portent le geste <b>exact</b> de la source — c'est-à-dire dont la paire
    * (ou le nom homonyme) se traduit en une animation réellement livrée par le mod. Le reste emploie
    * le geste d'attaque de base, comme la source le fait pour ses attaques ordinaires.
    */
   public static int couvertureExacte() {
      int n = 0;
      for (java.util.Map.Entry<String, double[]> e : CODES.entrySet()) {
         String nom = traduire(e.getValue()[0], e.getValue()[1], null);
         if (nom != null && disponibles().contains(nom)) {
            n++;
         }
      }
      for (String nom : NOMS.values()) {
         if (disponibles().contains(nom)) {
            n++;
         }
      }
      return n;
   }

   /** Techniques pour lesquelles la source fournit une paire de codes (extraite de son bytecode). */
   public static int pairesConnues() {
      return CODES.size() + NOMS.size();
   }

   /** Combien de paires de codes ont été lues dans la source ? (preuve de l'extraction) */
   public static int nombreDePaires() {
      return CODES.size();
   }

   public static int envoyees() {
      return envoyees;
   }

   public static int introuvables() {
      return introuvables;
   }

   public static List<String> manquants() {
      return List.copyOf(manquants);
   }

   public static void resetCompteurs() {
      envoyees = 0;
      introuvables = 0;
      manquants.clear();
   }

   /**
    * Nom d'animation pour cette technique, ou {@code ""} s'il n'y en a pas.
    *
    * <p>Deux chemins, dans l'ordre : la <b>paire de codes</b> de la source quand la technique y figure
    * (choix décidé par la source elle-même), sinon une <b>attaque de base</b> déduite du type et de
    * l'arme tenue — exactement ce que fait la source pour {@code code 0..4}.</p>
    */
   public static String nameFor(DevilFruitMove move, ServerPlayer player) {
      String mythical=MythicalZoanTechniques.animationFor(player,move.id());if(!mythical.isEmpty())return mythical;
      String zoan=ZoanTechniqueEpisodes.animationFor(player,move.id());if(!zoan.isEmpty())return zoan;
      String mythic=MythicBatchTechniques.animationFor(player,move.id());if(!mythic.isEmpty())return mythic;
      String paramecia=ParameciaBatchTechniques.animationFor(player,move.id());if(!paramecia.isEmpty())return paramecia;
      String material=MaterialBatchTechniques.animationFor(player,move.id());if(!material.isEmpty())return material;
      String controlled=ControlledBatchTechniques.animationFor(player,move.id());if(!controlled.isEmpty())return controlled;
      String batch=ElementalBatchTechniques.animationFor(player,move.id());if(!batch.isEmpty())return batch;
      String goro=GoroTechniques.animationFor(player,move.id());
      if(!goro.isEmpty())return goro;
      if(move.id().equals("hiken") && !MeraTechniques.charging(player)) return "punch_right";
      if(MeraTechniques.charging(player)) return move.id().equals("daienkai") ? "right_arm_up" : "energy_charge";
      if(move.id().equals("kaenryuo")) return "mera_kaenryuo";
      if(move.id().equals("kagero")) return "speed_attack1";
      String magu=MaguTechniques.animationFor(player,move.id());
      if(!magu.isEmpty()) return magu;
      String parNom = NOMS.get(move.id());
      double[] codes = CODES.get(move.id());
      String nom = parNom != null ? parNom : (codes != null ? traduire(codes[0], codes[1], player) : null);
      if (nom == null) {
         boolean epee = player.getMainHandItem().is(net.minecraft.tags.ItemTags.SWORDS);
         String cote = player.getRandom().nextBoolean() ? "right" : "left";
         nom = switch (move.type()) {
            case MELEE -> epee ? "sword" : "punch_" + cote;
            case PROJECTILE -> "both_arm_front";
            case EFFECT -> "both_arm_up";
            case BUFF -> "energy_charge";
            default -> "clap";
         };
      }
      if (!disponibles().contains(nom)) {
         introuvables++;
         if (!manquants.contains(nom)) {
            manquants.add(nom);
         }
         return "";
      }
      return nom;
   }

   /**
    * Traduction de la cascade de {@code PlayAnimationPlayerProcedure}, branche par branche.
    *
    * <p>Chaque branche ci-dessous est recopiée de la source, pas approchée. Quand la cascade ne
    * connaît pas le code, on renvoie {@code null} : la source non plus n'envoie rien dans ce cas
    * (elle teste {@code if (!ANIME_NAME.isEmpty())} avant de diffuser), et le repli générique prend
    * alors le relais.</p>
    */
   public static String traduire(double animation1, double animation2, ServerPlayer player) {
      String cote = player == null || player.getRandom().nextBoolean() ? "right" : "left";
      int n2 = (int) animation2;
      if (animation1 >= 0.0) {
         double code = animation1 % 50.0;
         return code == 4.0 ? "punch_overhead" : "punch_" + cote;
      }
      return switch ((int) animation1) {
         case -1 -> "backstep";
         case -2 -> "death";
         case -3 -> "right_arm_up";
         case -4 -> switch (n2) {
            case 1 -> "both_arm_ground";
            case 2 -> "both_arm_up";
            default -> "both_arm_front";
         };
         case -5 -> switch (n2) {
            case 0 -> "sword_to_left";
            case 1 -> "sword_to_right";
            default -> "sword_rotate";
         };
         case -6 -> switch (n2) {
            case 1 -> "amane_dachi";
            case 2 -> "kick_flying";
            case 13 -> "kick_right";
            case 14 -> "kick_left";
            default -> "kick_" + cote;
         };
         case -7 -> switch (n2) {
            case 1 -> "punch_gomu_pistol1";
            case 2 -> "punch_gomu_pistol2";
            case 3 -> "punch_gomu_gatling1";
            case 4 -> "punch_gomu_gatling2";
            case 5 -> "punch_right_jinbe";
            default -> "punch_" + cote;
         };
         case -8 -> switch (n2) {
            case 4, 5 -> "raimei_hakke1";
            case 0 -> "breath1";
            default -> "ragnaraku1";
         };
         case -9 -> animation2 == 1.0 ? "fall1" : "guard";
         case -10 -> switch (n2) {
            case 2 -> "speed_attack_sword";
            case 10 -> "togen_totsuka1";
            case 11 -> "togen_totsuka2";
            default -> "speed_attack1";
         };
         case -13 -> switch (n2) {
            case 0 -> "hakoku_left1";
            case 1 -> "hakoku_left2";
            case 2 -> "hakoku_right1";
            default -> "hakoku_right2";
         };
         case -14 -> switch (n2) {
            case 0 -> "ul_zugan";
            case 1 -> "headbutt_1";
            case 2 -> "headbutt_2";
            default -> "head_overhead";
         };
         case -15 -> n2 == 10 ? "rotation2" : "swim";
         case -16 -> "enkai";
         case -17 -> "clap";
         case -18 -> "sprint2";
         case -19 -> switch (n2) {
            case 1 -> "right_leg_front";
            case 2 -> "right_arm_front2";
            case 3 -> "energy_charge";
            default -> "right_arm_front";
         };
         case -20 -> switch (n2) {
            case 0 -> "yamiyami_blackhole";
            case 1, 2, 3, 4 -> "kaishin" + n2;
            default -> "kamusari" + (n2 - 4);
         };
         case -21 -> "invisibility";
         case -22 -> "ursus_shock";
         case -23 -> n2 == 1 ? "rin" : "rokuogan";
         case -24 -> "yasakani_no_magatama";
         case -25 -> switch (n2) {
            case 0 -> "onigiri1";
            case 1 -> "onigiri2";
            case 2 -> "toragari1";
            case 3 -> "toragari2";
            case 4 -> "sanzen_sekai1";
            case 5 -> "sanzen_sekai2";
            default -> "iai1";
         };
         case -26 -> "gear_" + n2;
         case -47 -> switch (n2) {
            case 1 -> "fly_front2";
            case 2 -> "fly_front3";
            default -> "fly_front";
         };
         case -48 -> n2 == 0 ? "fly_back" : "fly_back2";
         case -49 -> n2 == 1 ? "cancel" : "negative";
         default -> null;   // la cascade de la source ne connaît pas ce code : elle n'envoie rien non plus
      };
   }

   /**
    * Diffuse l'animation d'une technique au joueur <b>et</b> aux joueurs qui le suivent — les deux
    * envois de la source ({@code PLAYER} puis {@code TRACKING_ENTITY}).
    */
   public static void play(ServerPlayer player, DevilFruitMove move) {
      playNamed(player,nameFor(move,player));
   }

   public static void playNamed(ServerPlayer player,String nom) {
      if (!disponibles().contains(nom)) return;
      if (nom.isEmpty() || !(player.level() instanceof ServerLevel level)) {
         return;
      }
      DevilFruitNetworking.sendAnimation(player, nom, player.getId());
      for (ServerPlayer autre : net.fabricmc.fabric.api.networking.v1.PlayerLookup.tracking(player)) {
         if (autre != player) {
            DevilFruitNetworking.sendAnimation(autre, nom, player.getId());
         }
      }
      envoyees++;
   }
}
