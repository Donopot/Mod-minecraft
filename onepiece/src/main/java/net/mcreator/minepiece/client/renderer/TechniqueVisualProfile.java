package net.mcreator.minepiece.client.renderer;

/** Explicit visual identities. Unknown legacy particle-only skills retain their original rendering. */
public record TechniqueVisualProfile(Kind kind, int color, float radius) {
   public enum Kind { B07_RUSH,B07_FLAME_RAY, B08_MISSILE,B08_HEAD,B08_COIL, B07_FROST,B07_TORNADO, B06_WOLF,B06_BIRD,B06_NET, B05_BAT,B05_LANCE,B05_SCORE, B04_ARROW,B04_KICK,B04_PRETZEL,B04_WEIGHT,B04_BISCUIT, B03_ROOT,B03_CAGE,B03_DONUT,B03_SPIKED_FIST,B03_CRYSTAL,B03_FAN,B03_CROSS,B03_PUNCH, B02_HORIZONTAL,B02_CRACK,B02_SWEEP,B02_TILT,B02_BOMB,B02_PAW,B02_GRAVITY,B02_ROOM,B02_ORBIT,B02_METEOR, B01_KICK, B01_BEAM, B01_CHARGE, B01_WAVE, ICE_SPIKE, ICE_BALL, DARK_BALL, DARK_FIELD, SMOKE_FIST, LIGHT_PEARL, LIGHT_SWORD, SAND_SLASH, LEGACY, ELECTRIC_ARC, ELECTRIC_CHARGE, ELECTRIC_WARNING, ELECTRIC_IMPACT, ELECTRIC_CLOUD, ELECTRIC_BALL, MAGMA_FIST_CHARGE, MAGMA_HOUND_CHARGE, MAGMA_WAKE, MAGMA_VOLCANO, MAGMA_SKY, MAGMA_ERUPTION, MAGMA_TRAIL, LIGHT, PRESSURE, NOTE, HEART, GHOST, SMOKE, SPARK, ARC, FIRE_BREATH, RUBBLE, FIRE_FIST, FIRE_BULLET, FIREFLIES, FIRE_PILLAR, FIRE_BALL, FIRE_DRAGON, FLAME_TRAIL }
   public static TechniqueVisualProfile forTechnique(String id) {
      return switch (id) {
         case "b08_brachio_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xFFFFC18A,.2F);
         case "b08_brachio_impact" -> new TechniqueVisualProfile(Kind.PRESSURE,0xFFFFC18A,1);
         case "b08_brachio_attackoverhead" -> new TechniqueVisualProfile(Kind.B03_PUNCH,0xFFFFC18A,1);
         case "b08_brachio_brachiobober" -> new TechniqueVisualProfile(Kind.B08_COIL,0xFFFFC18A,1);
         case "b08_brachio_blackcofee" -> new TechniqueVisualProfile(Kind.LIGHT,0xFFFFF1B3,.09F);
         case "b08_brachio_brachiolauncher" -> new TechniqueVisualProfile(Kind.B08_MISSILE,0xFFFFC18A,.45F);
         case "b08_brachio_sparkingqueen" -> new TechniqueVisualProfile(Kind.LIGHT,0xFFFF403A,.09F);
         case "b08_pachy_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xFFA4EBF5,.2F);
         case "b08_pachy_impact" -> new TechniqueVisualProfile(Kind.PRESSURE,0xFFA4EBF5,1);
         case "b08_pachy_attackjump" -> new TechniqueVisualProfile(Kind.B03_PUNCH,0xFFA4EBF5,1);
         case "b08_pachy_attacksweep" -> new TechniqueVisualProfile(Kind.B02_SWEEP,0xFFA4EBF5,1);
         case "b08_pachy_headbutt" -> new TechniqueVisualProfile(Kind.B08_HEAD,0xFFA4EBF5,1);
         case "b08_pachy_ulzugan" -> new TechniqueVisualProfile(Kind.B08_HEAD,0xFFA4EBF5,1);
         case "b08_pachy_ulmeteor" -> new TechniqueVisualProfile(Kind.B08_HEAD,0xFFA4EBF5,1);

         case "b07_makami_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xFFB9F2FF,.2F);
         case "b07_makami_impact" -> new TechniqueVisualProfile(Kind.PRESSURE,0xFFB9F2FF,.9F);
         case "b07_makami_attackoverhead" -> new TechniqueVisualProfile(Kind.B03_PUNCH,0xFFB9F2FF,1);
         case "b07_makami_attacksweep" -> new TechniqueVisualProfile(Kind.B02_SWEEP,0xFFB9F2FF,1);
         case "b07_makami_raimeihakke" -> new TechniqueVisualProfile(Kind.B07_RUSH,0xFFB9F2FF,1);
         case "b07_makami_narikabura" -> new TechniqueVisualProfile(Kind.PRESSURE,0xFFB9F2FF,1);
         case "b07_makami_namujihyoga" -> new TechniqueVisualProfile(Kind.B07_FROST,0xFFB9F2FF,1);
         case "b07_makami_himorogiri" -> new TechniqueVisualProfile(Kind.B03_PUNCH,0xFFB9F2FF,1);
         case "b07_makami_hyogaheki" -> new TechniqueVisualProfile(Kind.ICE_BALL,0xFFB9F2FF,1);
         case "b07_daibutsu_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xFFFDE08A,.2F);
         case "b07_daibutsu_impact" -> new TechniqueVisualProfile(Kind.PRESSURE,0xFFFDE08A,.9F);
         case "b07_daibutsu_attackoverhead" -> new TechniqueVisualProfile(Kind.B03_PUNCH,0xFFFDE08A,1);
         case "b07_daibutsu_brilliantpunk" -> new TechniqueVisualProfile(Kind.B07_RUSH,0xFFFDE08A,1);
         case "b07_daibutsu_impactball" -> new TechniqueVisualProfile(Kind.PRESSURE,0xFFFDE08A,1);
         case "b07_daibutsu_overheadsuper" -> new TechniqueVisualProfile(Kind.B03_PUNCH,0xFFFDE08A,1);
         case "b07_daibutsu_impactwave" -> new TechniqueVisualProfile(Kind.PRESSURE,0xFFFDE08A,1);
         case "b07_phoenix_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xFF38DBFA,.2F);
         case "b07_phoenix_impact" -> new TechniqueVisualProfile(Kind.PRESSURE,0xFF38DBFA,.9F);
         case "b07_phoenix_attackspeed" -> new TechniqueVisualProfile(Kind.B07_RUSH,0xFF38DBFA,1);
         case "b07_phoenix_ongle" -> new TechniqueVisualProfile(Kind.B04_KICK,0xFF38DBFA,1);
         case "b07_phoenix_bluebird" -> new TechniqueVisualProfile(Kind.B06_BIRD,0xFF38DBFA,1);
         case "b07_phoenix_marcotornado" -> new TechniqueVisualProfile(Kind.B07_TORNADO,0xFF38DBFA,1);
         case "b07_phoenix_hooin" -> new TechniqueVisualProfile(Kind.B04_KICK,0xFF38DBFA,1);
         case "b07_phoenix_fujiazami" -> new TechniqueVisualProfile(Kind.B02_SWEEP,0xFF38DBFA,1);
         case "b07_seiryu_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xFFCCBBFF,.2F);
         case "b07_seiryu_impact" -> new TechniqueVisualProfile(Kind.PRESSURE,0xFFCCBBFF,.9F);
         case "b07_seiryu_attackoverhead" -> new TechniqueVisualProfile(Kind.B03_PUNCH,0xFFCCBBFF,1);
         case "b07_seiryu_attacksweep" -> new TechniqueVisualProfile(Kind.B02_SWEEP,0xFFCCBBFF,1);
         case "b07_seiryu_raimeihakke" -> new TechniqueVisualProfile(Kind.B07_RUSH,0xFFCCBBFF,1);
         case "b07_seiryu_kongokabura" -> new TechniqueVisualProfile(Kind.PRESSURE,0xFFCCBBFF,1);
         case "b07_seiryu_kaifu" -> new TechniqueVisualProfile(Kind.B02_SWEEP,0xFFCCBBFF,1);
         case "b07_seiryu_borobreath" -> new TechniqueVisualProfile(Kind.B07_FLAME_RAY,0xFFFF7129,1);
         case "b07_seiryu_kosanze" -> new TechniqueVisualProfile(Kind.B03_PUNCH,0xFFCCBBFF,1);
         case "b07_seiryu_ikoku" -> new TechniqueVisualProfile(Kind.PRESSURE,0xFFCCBBFF,1);
         case "b07_ptero_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xFFDEDAEF,.2F);
         case "b07_ptero_impact" -> new TechniqueVisualProfile(Kind.PRESSURE,0xFFDEDAEF,.9F);
         case "b07_ptero_attackspeed" -> new TechniqueVisualProfile(Kind.B07_RUSH,0xFFDEDAEF,1);
         case "b07_ptero_tankyudon" -> new TechniqueVisualProfile(Kind.B01_BEAM,0xFFDEDAEF,1);
         case "b07_ptero_barizodon" -> new TechniqueVisualProfile(Kind.B02_SWEEP,0xFFDEDAEF,1);

         case "b06_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xE7B5DFFF,.15F);
         case "b06_thrust","b06_finger" -> new TechniqueVisualProfile(Kind.B01_BEAM,0xFFE9F4FF,id.equals("b06_finger")?.12F:.35F);
         case "b06_slash" -> new TechniqueVisualProfile(Kind.SAND_SLASH,0xF2C9F7FF,.9F);
         case "b06_wolf_slash" -> new TechniqueVisualProfile(Kind.B06_WOLF,0xF0DCEAFF,1.1F);
         case "b06_bird_slash" -> new TechniqueVisualProfile(Kind.B06_BIRD,0xF5CCF4FF,1.5F);
         case "b06_claw","b06_fangs" -> new TechniqueVisualProfile(Kind.B03_FAN,0xFFE9B88B,1.1F);
         case "b06_net" -> new TechniqueVisualProfile(Kind.B06_NET,0xE9B8DEFF,.9F);
         case "b06_rush","b06_evade" -> new TechniqueVisualProfile(Kind.B01_WAVE,0xBEC4D8EC,.8F);
         case "b06_guard" -> new TechniqueVisualProfile(Kind.B02_ORBIT,0xB5BA85E6,.5F);
         case "b06_spin" -> new TechniqueVisualProfile(Kind.B04_KICK,0xF2D3FBFF,3);
         case "b06_pressure","b06_slam","b06_impact" -> new TechniqueVisualProfile(Kind.PRESSURE,0xD9D9F3FF,id.equals("b06_slam")?3:id.equals("b06_pressure")?1.5F:.6F);

         case "b05_kage_charge","b05_imu_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xFF675080,.2F);
         case "b05_soru_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xFFFFDD65,.2F);
         case "b05_uta_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xFFFF86C7,.2F);
         case "b05_gomu_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xFFF0B488,.2F);
         case "b05_kage_brickbat" -> new TechniqueVisualProfile(Kind.B05_BAT,0xFF251930,.55F);
         case "b05_kage_tsunotokage" -> new TechniqueVisualProfile(Kind.B05_LANCE,0xFF24152F,.8F);
         case "b05_kage_shadowasgard","b05_kage_impact","b05_imu_silentcommand" -> new TechniqueVisualProfile(Kind.DARK_FIELD,0xD159366B,2);
         case "b05_kage_attackoverhead","b05_kage_islandshatterpunch","b05_imu_sovereigngrasp" -> new TechniqueVisualProfile(Kind.B03_PUNCH,0xFF332033,2);
         case "b05_soru_attackoverhead","b05_soru_attacksweep","b05_soru_ikoku" -> new TechniqueVisualProfile(Kind.B02_SWEEP,0xFFFFB3DF,2);
         case "b05_soru_raitei","b05_soru_furugora" -> new TechniqueVisualProfile(Kind.ELECTRIC_ARC,0xFFF3C5FF,2);
         case "b05_soru_hebunrifire" -> new TechniqueVisualProfile(Kind.FIRE_FIST,0xFFFF7338,.8F);
         case "b05_soru_impact" -> new TechniqueVisualProfile(Kind.ELECTRIC_IMPACT,0xE5F3B1F9,3);
         case "b05_uta_noteattack","b05_uta_impact" -> new TechniqueVisualProfile(Kind.NOTE,0xFFF18AD3,.45F);
         case "b05_uta_notelaser" -> new TechniqueVisualProfile(Kind.B05_SCORE,0xFFFF79D6,.35F);
         case "b05_uta_posing","b05_uta_noteshield","b05_uta_summontotmusica","b05_uta_guard" -> new TechniqueVisualProfile(Kind.B05_SCORE,0xEAF9A2DC,2);
         case "b05_uta_energy" -> new TechniqueVisualProfile(Kind.B02_BOMB,0xFFFFADDC,.7F);

         case "b04_aro_charge","b04_horo_charge","b04_mero_charge","b04_kiro_charge","b04_bisu_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,id.contains("mero")?0xEDFF80BB:id.contains("aro")?0xE6E82752:id.contains("bisu")?0xE6D8AE76:0xE6CBDBEC,.2F);
         case "b04_aro_arrow","b04_aro_move","b04_aro_combo","b04_aro_passive","b04_aro_impact" -> new TechniqueVisualProfile(Kind.B04_ARROW,0xFFE72B5A,id.equals("b04_aro_arrow")?.6F:1);
         case "b04_horo_burst" -> new TechniqueVisualProfile(Kind.B02_BOMB,0xDDE8FFFF,1.7F);
         case "b04_horo_rap" -> new TechniqueVisualProfile(Kind.B02_ORBIT,0xD5E1FFFF,.8F);
         case "b04_mero_pistolkiss","b04_mero_impact" -> new TechniqueVisualProfile(Kind.HEART,0xFFFF70AB,.35F);
         case "b04_mero_roundhousekick","b04_mero_rotatekick","b04_mero_perfumefemur" -> new TechniqueVisualProfile(Kind.B04_KICK,0xFAFF88BE,id.contains("rotate")?3:1.5F);
         case "b04_kiro_attackoverhead","b04_kiro_impact","b04_kiro_weightmanipulation" -> new TechniqueVisualProfile(Kind.B04_WEIGHT,0xDDD8C0FF,1.5F);
         case "b04_bisu_crashpretzel" -> new TechniqueVisualProfile(Kind.B04_PRETZEL,0xFFE8C28D,2);
         case "b04_bisu_biscuit","b04_bisu_reinforced" -> new TechniqueVisualProfile(Kind.B04_BISCUIT,0xFFD9AE70,.5F);

         case "b03_mochi_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xF4FFF1D8,.2F);
         case "b03_mori_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xED75AC4C,.2F);
         case "b03_kira_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xEC99E9F9,.2F);
         case "b03_supa_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xFFE5EDF5,.2F);
         case "b03_riki_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xFFDBAC88,.2F);
         case "b03_mori_root","b03_mori_rootgiant","b03_mori_root_branch" -> new TechniqueVisualProfile(Kind.B03_ROOT,0xFF76502D,id.equals("b03_mori_rootgiant")?1.7F:.8F);
         case "b03_mori_cage","b03_mori_assault","b03_mori_impact" -> new TechniqueVisualProfile(Kind.B03_CAGE,0xFF805634,1.5F);
         case "b03_mochi_donut" -> new TechniqueVisualProfile(Kind.B03_DONUT,0xFFFFEBD7,1);
         case "b03_mochi_zangirimochi","b03_mochi_impact" -> new TechniqueVisualProfile(Kind.B03_SPIKED_FIST,0xFF292126,.7F);
         case "b03_mochi_yakimochi" -> new TechniqueVisualProfile(Kind.FIRE_FIST,0xFF5B2C24,.55F);
         case "b03_mochi_build" -> new TechniqueVisualProfile(Kind.B03_DONUT,0xFFEED6A8,.7F);
         case "b03_kira_diamondshard","b03_kira_diamondavalanche","b03_kira_impact","b03_kira_body","b03_kira_brilliantpunk","b03_kira_attackoverhead" -> new TechniqueVisualProfile(Kind.B03_CRYSTAL,0xF598E8FF,id.equals("b03_kira_diamondavalanche")?1.4F:.45F);
         case "b03_supa_sparklingdaisy" -> new TechniqueVisualProfile(Kind.B03_FAN,0xFFF4F8FF,1.6F);
         case "b03_supa_sparbreak","b03_supa_atomicspurt" -> new TechniqueVisualProfile(Kind.B03_CROSS,0xFFE7F4FF,1.5F);
         case "b03_supa_impact","b03_riki_sweep" -> new TechniqueVisualProfile(Kind.B02_SWEEP,0xF3E2EFFA,1.2F);
         case "b03_riki_attackoverhead","b03_riki_hadoelbow","b03_riki_galleonlariat","b03_riki_impact" -> new TechniqueVisualProfile(Kind.B03_PUNCH,0xFFE0A679,1);
         case "b03_mochi_dash","b03_kira_dash","b03_supa_dash","b03_riki_dash" -> new TechniqueVisualProfile(Kind.B01_BEAM,0xD7E5EAF0,1.5F);

         case "b02_quake_charge" -> new TechniqueVisualProfile(Kind.B02_CRACK,0xE5CBF3FA,.18F);
         case "b02_air_crack","b02_quake_front","b02_quake_radial" -> new TechniqueVisualProfile(Kind.B02_CRACK,0xECDAFBFF,1.5F);
         case "b02_quake_sweep","b02_quake_overhead","b02_ope_cut" -> new TechniqueVisualProfile(Kind.B02_SWEEP,id.equals("b02_ope_cut")?0xE55BE9FC:0xE5E9F8FF,1);
         case "b02_tilt","b02_tilt_reverse" -> new TechniqueVisualProfile(Kind.B02_TILT,id.equals("b02_tilt")?0xD6D8F6FF:0xD6B7E5FF,3);
         case "b02_bomb_charge","b02_bomb_drop" -> new TechniqueVisualProfile(Kind.B02_ORBIT,0xF7FFB848,.14F);
         case "b02_bomb_blast","b02_meteor_impact" -> new TechniqueVisualProfile(Kind.B02_BOMB,0xCFFF9E3D,3);
         case "b02_nosefancycannon" -> new TechniqueVisualProfile(Kind.SPARK,0xFFB5AB78,.06F);
         case "b02_paw_charge" -> new TechniqueVisualProfile(Kind.B02_PAW,0xCEFFE2EA,.17F);
         case "b02_padho","b02_ursusshock","b02_paw_impact","b02_paw_wave","b02_paw_blast" -> new TechniqueVisualProfile(Kind.B02_PAW,0xDDF9E6EE,id.equals("b02_ursusshock")?.8F:.5F);
         case "b02_paw_warning","b02_meteor_warning" -> new TechniqueVisualProfile(Kind.B01_WAVE,id.equals("b02_paw_warning")?0xD5FFC3D5:0xD5C194F5,1);
         case "b02_gravity_charge","b02_ope_charge" -> new TechniqueVisualProfile(Kind.B02_ORBIT,id.equals("b02_ope_charge")?0xD96DDFFA:0xD4A577EB,.17F);
         case "b02_gravity_horizontal" -> new TechniqueVisualProfile(Kind.B02_HORIZONTAL,0xD6B38BFF,2);
         case "b02_gravity_down","b02_gravity_field" -> new TechniqueVisualProfile(Kind.B02_GRAVITY,0xD9AB83F3,3);
         case "b02_room" -> new TechniqueVisualProfile(Kind.B02_ROOM,0xA573DAFF,8);
         case "b02_ope_swap" -> new TechniqueVisualProfile(Kind.B02_HORIZONTAL,0xE58CEBFF,1);
         case "b02_paw_travel","b02_dash" -> new TechniqueVisualProfile(Kind.B01_BEAM,0xD99ADCFB,1);
         case "b02_meteor","b02_meteorsmall" -> new TechniqueVisualProfile(Kind.B02_METEOR,0xFF746458,id.equals("b02_meteor")?1.4F:.8F);
         case "b02_rock_impact" -> new TechniqueVisualProfile(Kind.B02_ORBIT,0xCDAA9E90,.35F);

         case "b01_ice_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xCCAEF2FF,.15F);
         case "b01_light_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xFFFBE690,.15F);
         case "b01_smoke_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xAEE6F0F6,.15F);
         case "b01_sand_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xDBE6BF7C,.15F);
         case "b01_dark_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xDD6B4487,.15F);
         case "b01_ice_wave","b01_ice_impact" -> new TechniqueVisualProfile(Kind.B01_WAVE,0xD58CDCF4,1);
         case "b01_sand_wave" -> new TechniqueVisualProfile(Kind.B01_WAVE,0xD4DFC082,1);
         case "b01_smoke_wave","b01_impact" -> new TechniqueVisualProfile(Kind.B01_WAVE,0xADD9E2E5,1);
         case "b01_ice_spike" -> new TechniqueVisualProfile(Kind.ICE_SPIKE,0xDD9DDDF0,2.5F);
         case "b01_ice_ball" -> new TechniqueVisualProfile(Kind.ICE_BALL,0xC198E8FF,1);
         case "darkball" -> new TechniqueVisualProfile(Kind.DARK_BALL,0xF1100A1A,1.1F);
         case "b01_dark_field","b01_dark_grab","b01_dark_impact" -> new TechniqueVisualProfile(Kind.DARK_FIELD,0xD8271239,3);
         case "b01_light_sword" -> new TechniqueVisualProfile(Kind.LIGHT_SWORD,0xFFFFDE75,1);
         case "b01_light_kick" -> new TechniqueVisualProfile(Kind.B01_KICK,0xEFFFCC55,.45F);
         case "b01_light_dash" -> new TechniqueVisualProfile(Kind.B01_BEAM,0xEFFFDC67,1);
         case "b01_sand_slash" -> new TechniqueVisualProfile(Kind.SAND_SLASH,0xE8E4C087,1.8F);
         case "b01_sand_dash" -> new TechniqueVisualProfile(Kind.SMOKE,0xAAD9BB8B,.25F);
         case "b01_smoke_dash" -> new TechniqueVisualProfile(Kind.SMOKE,0xAADDDEE5,.3F);
         case "whiteblow","b01_smoke_finish" -> new TechniqueVisualProfile(Kind.SMOKE_FIST,0xE5E6ECF0,.65F);
         case "magatama" -> new TechniqueVisualProfile(Kind.LIGHT_PEARL,0xFFFFE47A,.3F);
         case "goro_arc", "goro_blink_trail" -> new TechniqueVisualProfile(Kind.ELECTRIC_ARC,0xFF7DDFFF,1);
         case "goro_charge" -> new TechniqueVisualProfile(Kind.ELECTRIC_CHARGE,0xFF70DFFF,.45F);
         case "goro_warning" -> new TechniqueVisualProfile(Kind.ELECTRIC_WARNING,0xFF55CEFF,2);
         case "goro_impact" -> new TechniqueVisualProfile(Kind.ELECTRIC_IMPACT,0xFF72E5FF,2);
         case "goro_cloud" -> new TechniqueVisualProfile(Kind.ELECTRIC_CLOUD,0xAA182C49,3);
         case "goro_raigo_charge", "goro_raigo" -> new TechniqueVisualProfile(Kind.ELECTRIC_BALL,0xDD5ACDFF,2);

         case "magu_fist_charge" -> new TechniqueVisualProfile(Kind.MAGMA_FIST_CHARGE,0xFFFC510A,.6F);
         case "magu_hound_charge" -> new TechniqueVisualProfile(Kind.MAGMA_HOUND_CHARGE,0xFFCB2A09,.7F);
         case "magu_wake" -> new TechniqueVisualProfile(Kind.MAGMA_WAKE,0xFFFF6B11,4);
         case "magu_volcanic" -> new TechniqueVisualProfile(Kind.MAGMA_VOLCANO,0xFFDD3208,4);
         case "magu_meteor_charge" -> new TechniqueVisualProfile(Kind.MAGMA_SKY,0xFFFF7C0D,2);
         case "magu_dash" -> new TechniqueVisualProfile(Kind.MAGMA_TRAIL,0xFFB72805,.3F);
         case "magu_eruption" -> new TechniqueVisualProfile(Kind.MAGMA_ERUPTION,0xFFB82C08,4);
         case "magu_charge", "magu_trail" -> new TechniqueVisualProfile(Kind.MAGMA_TRAIL,0xFFEC4B08,.4F);
         case "hiken" -> new TechniqueVisualProfile(Kind.FIRE_FIST,0xEEFF6814,.8F);
         case "higan" -> new TechniqueVisualProfile(Kind.FIRE_BULLET,0xFFFFAD35,.13F);
         case "hibashira" -> new TechniqueVisualProfile(Kind.FIRE_PILLAR,0xDAFF6516,4);
         case "daienkai", "mera_charge_ball" -> new TechniqueVisualProfile(Kind.FIRE_BALL,0xEBFF6012,2);
         case "hotarubi", "mera_fireflies" -> new TechniqueVisualProfile(Kind.FIREFLIES,0xFFFFD657,.4F);
         case "kaenryuo" -> new TechniqueVisualProfile(Kind.FIRE_DRAGON,0xEBFF7118,.7F);
         case "kagero", "mera_dash", "mera_charge" -> new TechniqueVisualProfile(Kind.FLAME_TRAIL,0xCCFF941A,.5F);
         case "laser1", "laser2" -> new TechniqueVisualProfile(Kind.LIGHT, 0xFFFFE47A, .07F);
         case "blackcofee" -> new TechniqueVisualProfile(Kind.LIGHT, 0xFFD99CFF, .12F);
         case "borobreath" -> new TechniqueVisualProfile(Kind.FIRE_BREATH, 0xFFFF651A, .6F);
         case "gagan", "kongokabura" -> new TechniqueVisualProfile(Kind.ARC, 0xDBDAF4FF, .8F);
         case "rubble" -> new TechniqueVisualProfile(Kind.RUBBLE, 0xFF929095, .3F);
         case "padho", "ursusshock", "impactball" -> new TechniqueVisualProfile(Kind.PRESSURE, 0xBCD6F3FF,
            id.equals("ursusshock") ? 1.0F : .55F);
         case "noteattack", "notelaser" -> new TechniqueVisualProfile(Kind.NOTE, 0xFFFF79DA, .45F);
         case "pistolkiss" -> new TechniqueVisualProfile(Kind.HEART, 0xFFFF6BAA, .5F);
         case "minihollow", "tokuhoro" -> new TechniqueVisualProfile(Kind.GHOST, 0xCFF2FFFF,
            id.equals("tokuhoro") ? .65F : .3F);
         case "whitelauncher" -> new TechniqueVisualProfile(Kind.SMOKE, 0x9FEAF1F4, .4F);
         case "nosefancycannon" -> new TechniqueVisualProfile(Kind.SPARK, 0xFFFFB25A, .12F);
         default -> new TechniqueVisualProfile(Kind.LEGACY, 0, 0);
      };
   }
   public boolean replacesLegacyModel() { return kind != Kind.LEGACY; }
}
