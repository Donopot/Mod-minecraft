package br.com.oneminepiece.opworldlike;


import net.mcreator.minepiece.init.MinepieceModParticles;
import br.com.oneminepiece.opworldlike.claim.GriefProtectionConfig;
import br.com.oneminepiece.opworldlike.claim.IslandClaimEvents;
import br.com.oneminepiece.opworldlike.claim.IslandDominionBonusEvents;
import br.com.oneminepiece.opworldlike.command.CrewCommands;
import br.com.oneminepiece.opworldlike.command.IslandCommands;
import br.com.oneminepiece.opworldlike.compat.MinePieceAddonCompat;
import br.com.oneminepiece.opworldlike.compat.MinePieceSingleplayerBlockBreakCompat;
import br.com.oneminepiece.opworldlike.entity.MobBalanceConfig;
import br.com.oneminepiece.opworldlike.entity.ReverseMountainSpawnConfig;
import br.com.oneminepiece.opworldlike.loot.EquipmentLootConfig;
import br.com.oneminepiece.opworldlike.loot.ModMobEquipmentDropEvents;
import br.com.oneminepiece.opworldlike.worldgen.OnePieceBiomeSource;
import br.com.oneminepiece.opworldlike.worldgen.OnePieceChunkGenerator;
import net.fabricmc.api.ModInitializer;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Point d'entrée Fabric 26.3 (port de l'addon Forge 1.20.1 opworldlike-1.5.2-MP).
 */
public final class OmpWorld implements ModInitializer {
   public static final String MOD_ID = "opworldlike";
   public static final int WORLD_RADIUS = 64000;
   public static final Logger LOGGER = LoggerFactory.getLogger("OnePieceWorldLike");
   public static MinecraftServer currentServer;

   @Override
   public void onInitialize() {
      LOGGER.info("[OP World Like] initialisation Fabric 26.3");
      MinePieceAddonCompat.logLoadedAddons();
      OmpWorldGameRules.bootstrap();
      WorldLayoutConfig.loadOrCreate();
      if (OmpFeatureGates.serverCompetition()) {
         GriefProtectionConfig.loadOrCreate();
      }

      MobBalanceConfig.loadOrCreate();
      ReverseMountainSpawnConfig.loadOrCreate();
      EquipmentLootConfig.loadOrCreate();
      br.com.oneminepiece.opworldlike.loot.DevilFruitLootConfig.loadOrCreate();
      br.com.oneminepiece.opworldlike.balance.DevilFruitBalanceConfig.loadOrCreate();
      br.com.oneminepiece.opworldlike.haki.HakiConfig.loadOrCreate();
      br.com.oneminepiece.opworldlike.flight.DevilFruitFlightConfig.loadOrCreate();
      br.com.oneminepiece.opworldlike.explosion.DevilFruitExplosionConfig.loadOrCreate();
      // FRUIT-GURA_GURA-03/05 : réglages des ondes sismiques et fissures.
      br.com.oneminepiece.opworldlike.quake.DevilFruitQuakeConfig.loadOrCreate();
      // FRUIT-MAGU_MAGU-03/04/05 : réglages du magma (brûlure, nappes, destruction du terrain).
      br.com.oneminepiece.opworldlike.magma.DevilFruitMagmaConfig.loadOrCreate();
      // FRUIT-MERA_MERA-03/04/05 : brûlure bornée, anneau de feu, propulsion.
      br.com.oneminepiece.opworldlike.flame.DevilFruitFlameConfig.loadOrCreate();
      // FRUIT-SUNA_SUNA-03/04/05 : tempête, dessiccation, interactions avec l'eau.
      br.com.oneminepiece.opworldlike.sand.DevilFruitSandConfig.loadOrCreate();
      // FRUIT-ZUSHI_ZUSHI-03/04/05 : gravité bornée, lévitation, météores descendants.
      br.com.oneminepiece.opworldlike.gravity.DevilFruitGravityConfig.loadOrCreate();
      // FRUIT-MERO_MERO-03/04 : pétrification (condition, état, libération sans corruption).
      br.com.oneminepiece.opworldlike.love.DevilFruitLoveConfig.loadOrCreate();
      // FRUIT-HITO_HITO_DAIBUTSU-03/04/05 : forme géante bornée, onde de choc voyageuse.
      br.com.oneminepiece.opworldlike.daibutsu.DevilFruitDaibutsuConfig.loadOrCreate();
      // FRUIT-HORO_HORO-03/04/05 : fantômes, état négatif, contrôle à distance.
      br.com.oneminepiece.opworldlike.hollow.DevilFruitHollowConfig.loadOrCreate();
      // FRUIT-IMU-04/05/06 : catalogue étendu (décision de mod) et passif du voile.
      br.com.oneminepiece.opworldlike.imu.DevilFruitImuConfig.loadOrCreate();
      // FRUIT-INU_INU_OKUCHI_NO_MAKAMI-04/05 : souffle glacé et défense de glace.
      br.com.oneminepiece.opworldlike.inu.DevilFruitOkuchiConfig.loadOrCreate();
      // FRUIT-INU_INU_WOLF-04 : bonds, morsures, poursuite avec endurance.
      br.com.oneminepiece.opworldlike.wolf.DevilFruitWolfConfig.loadOrCreate();
      // FRUIT-KAGE_KAGE-03 : le coffre d'ombres (acquisition, restitution garantie).
      br.com.oneminepiece.opworldlike.kage.DevilFruitKageConfig.loadOrCreate();
      // FRUIT-KIRA_KIRA-03/04 : corps de diamant (plafond déclaré, exceptions par la source).
      br.com.oneminepiece.opworldlike.kira.DevilFruitKiraConfig.loadOrCreate();
      // FRUIT-KIRO_KIRO-03/04/05 : poids réglable borné, attaque de chute.
      br.com.oneminepiece.opworldlike.kiro.DevilFruitKiroConfig.loadOrCreate();
      // FRUIT-MOCHI_MOCHI-04/05 : état collant et constructions temporaires.
      br.com.oneminepiece.opworldlike.mochi.DevilFruitMochiConfig.loadOrCreate();
      // FRUIT-MOKU_MOKU-03/04/05 : nuage de fumée, mobilité et capture.
      br.com.oneminepiece.opworldlike.moku.DevilFruitMokuConfig.loadOrCreate();
      // FRUIT-MORI_MORI-03/04/05 : racines, végétation temporaire, drainage.
      br.com.oneminepiece.opworldlike.mori.DevilFruitMoriConfig.loadOrCreate();
      // CORE-025 : applique les budgets d'équilibrage sur le catalogue (idempotent).
      net.mcreator.minepiece.procedures.DevilFruitRegistry.applyBalanceBudgets();
      // CORE-029 : gains d'EXP de Haki en combat + port automatique de l'effet.
      br.com.oneminepiece.opworldlike.haki.HakiEvents.register();
      // FRUIT-GURA_GURA-03 : avancement des fronts d'onde (enregistrement idempotent côté Fabric).
      // FRUIT-NIKYU_NIKYU-03 : charge d'Ursus Shock et annonce de la zone d'impact.
      net.mcreator.minepiece.procedures.NikyuRepulsion.register();
      // FRUIT-RIKI_RIKI-03 : échéance et libération des saisies.
      net.mcreator.minepiece.procedures.RikiForce.register();
      // FRUIT-SORU_SORU-03 : registre des invocations (Zeus et Prometheus), expiration.
      net.mcreator.minepiece.procedures.SoruSummons.register();
      // FRUIT-SUPA_SUPA-03 : rangement des lames.
      net.mcreator.minepiece.procedures.SupaBlades.register();
      // FRUIT-UTA_UTA-03 : mesures du domaine musical et sorties garanties.
      net.mcreator.minepiece.procedures.UtaDomain.register();
      net.mcreator.minepiece.procedures.SeismicWave.register();
      // FRUIT-HIE_HIE-03 : libération à l'échéance du gel progressif.
      net.mcreator.minepiece.procedures.FreezeService.register();
      // FRUIT-MAGU_MAGU-03/04 : entretien de la brûlure de magma et des nappes.
      net.mcreator.minepiece.procedures.MagmaBurn.register();
      net.mcreator.minepiece.procedures.MagmaPools.register();
      // FRUIT-MERA_MERA-03/04/05 : entretien de la brûlure de feu, des anneaux et de la propulsion.
      net.mcreator.minepiece.procedures.FlameBurn.register();
      net.mcreator.minepiece.procedures.FireRing.register();
      net.mcreator.minepiece.procedures.MeraTechniques.register();
      net.mcreator.minepiece.procedures.MaguTechniques.register();
      net.mcreator.minepiece.procedures.GoroTechniques.register();
      net.mcreator.minepiece.procedures.ElementalBatchTechniques.register();
      net.mcreator.minepiece.procedures.ControlledBatchTechniques.register();
      net.mcreator.minepiece.procedures.MaterialBatchTechniques.register();
      net.mcreator.minepiece.procedures.MythicBatchTechniques.register();
      net.mcreator.minepiece.procedures.ZoanTechniqueEpisodes.register();
      net.mcreator.minepiece.procedures.MythicalZoanTechniques.register();
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server -> net.mcreator.minepiece.procedures.ZoanForms.clear());
      net.mcreator.minepiece.procedures.ParameciaBatchTechniques.register();
      net.mcreator.minepiece.procedures.ParameciaControl.register();
      net.mcreator.minepiece.procedures.LightSwords.register();
      net.mcreator.minepiece.procedures.GoroAmaru.register();
      net.mcreator.minepiece.procedures.FirePropulsion.register();
      // FRUIT-SUNA_SUNA-03/04 : entretien de la dessiccation et des tempêtes.
      net.mcreator.minepiece.procedures.Desiccation.register();
      net.mcreator.minepiece.procedures.SandStorm.register();
      // FRUIT-ZUSHI_ZUSHI-03/04/05 : champs de gravité, lévitation, météores.
      net.mcreator.minepiece.procedures.GravityField.register();
      net.mcreator.minepiece.procedures.Levitation.register();
      net.mcreator.minepiece.procedures.MeteorFall.register();
      // FRUIT-MERO_MERO-04 : entretien de la pétrification.
      net.mcreator.minepiece.procedures.Petrification.register();
      // FRUIT-HITO_HITO_DAIBUTSU-03/04 : entretien de la forme géante et des ondes de choc.
      net.mcreator.minepiece.procedures.GiantForm.register();
      net.mcreator.minepiece.procedures.ZoanClaw.register();
      net.mcreator.minepiece.procedures.GiantShockWave.register();
      // FRUIT-HORO_HORO-04/05 : état négatif et contrôle à distance (sortie sur déconnexion).
      net.mcreator.minepiece.procedures.NegativeState.register();
      net.mcreator.minepiece.procedures.RemoteControl.register();
      // FRUIT-IMU-06 : passif « voile souverain » (durée bornée, nettoyage nominatif).
      net.mcreator.minepiece.procedures.SovereignVeil.register();
      // FRUIT-INU_INU_OKUCHI_NO_MAKAMI-05 : défense de glace (expiration, fonte, déconnexion).
      net.mcreator.minepiece.procedures.IceDefense.register();
      // FRUIT-INU_INU_WOLF-04 : poursuite (endurance, énergie, collision, déconnexion).
      net.mcreator.minepiece.procedures.WolfPursuit.register();
      // FRUIT-KAGE_KAGE-03 : restitution des ombres (échéance, mort, suppression, déconnexion).
      net.mcreator.minepiece.procedures.ShadowLedger.register();
      // FRUIT-KIRA_KIRA-03/04 : corps de diamant (échéance, mort, suppression, déconnexion).
      net.mcreator.minepiece.procedures.DiamondBody.register();
      // FRUIT-KIRO_KIRO-03/05 : poids (cooldown) et impacts de chute.
      net.mcreator.minepiece.procedures.WeightState.register();
      // Ope Ope (FRUIT-OPE_OPE-03/04/05) : ROOM et sections temporaires.
      br.com.oneminepiece.opworldlike.ope.DevilFruitOpeConfig.loadOrCreate();
      net.mcreator.minepiece.procedures.OpeRoom.register();
      net.mcreator.minepiece.procedures.Amputation.register();
      // Pika Pika (FRUIT-PIKA_PIKA-03/04/05) : rayons à portée, déplacement lumineux, volée annoncée.
      br.com.oneminepiece.opworldlike.pika.DevilFruitPikaConfig.loadOrCreate();
      net.mcreator.minepiece.procedures.LightCharge.register();
      net.mcreator.minepiece.procedures.LightBuffs.register();
      // Yami Yami (FRUIT-YAMI_YAMI-03/04/05) : attraction progressive, absorption bornée, scellement.
      br.com.oneminepiece.opworldlike.yami.DevilFruitYamiConfig.loadOrCreate();
      net.mcreator.minepiece.procedures.YamiDarkness.register();
      // Fruits Zoan (FRUIT-...-04/05) : bonds bornés (partagés avec le déplacement lumineux) et combos.
      br.com.oneminepiece.opworldlike.zoan.DevilFruitZoanConfig.loadOrCreate();
      net.mcreator.minepiece.procedures.WeightSlam.register();
      // FRUIT-MOCHI_MOCHI-04 : libération de l'état collant (échéance, mort, suppression, déconnexion).
      net.mcreator.minepiece.procedures.StickyState.register();
      // FRUIT-MOKU_MOKU-03/04/05 : nuages bornés, mobilité rendue, captures libérées.
      net.mcreator.minepiece.procedures.SmokeCloud.register();
      // FRUIT-MORI_MORI-03 : racines bornées et libérées.
      net.mcreator.minepiece.procedures.Roots.register();
      br.com.oneminepiece.opworldlike.flight.DevilFruitFlight.register();

      // events déjà portés
      IslandClaimEvents.register();
      OmpWorldEvents.register(IslandClaimEvents.getInstance());
      IslandDominionBonusEvents.register();
      ModMobEquipmentDropEvents.register();
      MinePieceSingleplayerBlockBreakCompat.register();
      br.com.oneminepiece.opworldlike.worldgen.ExtractedIslandRegionInstaller.register();
      br.com.oneminepiece.opworldlike.worldgen.CanonicalOceanChunkNormalizer.register();
      new DimensionGatewayEvents(true).register();
      br.com.oneminepiece.opworldlike.compat.CombatRecursionGuard.register();

      // shim Mine Piece ver15 — items référencés (loot équipement) + effets + attributs + entités skill
      net.mcreator.minepiece.init.MinepieceModItems.init();
      net.mcreator.minepiece.init.MinepieceModMobEffects.register();
      net.mcreator.minepiece.init.MinepieceModAttributes.register();
      net.mcreator.minepiece.init.MinepieceModEntities.register();
      // Particules reprises du JAR source (ténèbres de Yami, lumières de Pika) : le TYPE est
      // enregistré côté serveur, l'apparence côté client (DevilFruitClientInit).
      net.mcreator.minepiece.init.MinepieceModParticles.register();
      net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(
         net.mcreator.minepiece.init.MinepieceModEntities.ENTITY_GOMU_ARM,
         net.mcreator.minepiece.entity.EntityGomuArmEntity.createAttributes()
      );
      net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry.register(
         net.mcreator.minepiece.init.MinepieceModEntities.SUMMON,
         net.mcreator.minepiece.entity.SummonEntity.createAttributes()
      );
      net.mcreator.minepiece.network.DevilFruitNetworking.registerServer();
      net.mcreator.minepiece.network.MaterialBodyState.register();
      net.mcreator.minepiece.network.DevilFruitPersistence.register();
      net.mcreator.minepiece.procedures.PowerBlockService.register();
      net.mcreator.minepiece.procedures.TechniqueTerrain.register();
      LOGGER.info("[MinePiece shim] {}", net.mcreator.minepiece.procedures.DevilFruitRegistry.selfTest());
      br.com.oneminepiece.opworldlike.loot.NaturalLootEvents.register();
      br.com.oneminepiece.opworldlike.loot.DevilFruitLootEvents.register();

      // currentServer (remplace ServerLifecycleHooks.getCurrentServer())
      ServerLifecycleEvents.SERVER_STARTED.register(server -> OmpWorld.currentServer = server);
      ServerLifecycleEvents.SERVER_STOPPED.register(server -> { if (OmpWorld.currentServer == server) OmpWorld.currentServer = null; });

      // commandes
      CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
         IslandCommands.register(dispatcher);
         CrewCommands.register(dispatcher);
         net.mcreator.minepiece.command.DevilFruitCommands.register(dispatcher);
         net.mcreator.minepiece.command.DevilFruitTestCommands.register(dispatcher);
         net.mcreator.minepiece.command.RepairTestCommands.register(dispatcher);
         net.mcreator.minepiece.command.MeraTestCommands.register(dispatcher);
         net.mcreator.minepiece.command.MaguTestCommands.register(dispatcher);
         net.mcreator.minepiece.command.GoroTestCommands.register(dispatcher);
         net.mcreator.minepiece.command.Batch01TestCommands.register(dispatcher);
         net.mcreator.minepiece.command.Batch02TestCommands.register(dispatcher);
          net.mcreator.minepiece.command.Batch03TestCommands.register(dispatcher);
          net.mcreator.minepiece.command.Batch04TestCommands.register(dispatcher);
          net.mcreator.minepiece.command.Batch05TestCommands.register(dispatcher);
          net.mcreator.minepiece.command.Batch06TestCommands.register(dispatcher);
          net.mcreator.minepiece.command.Batch07TestCommands.register(dispatcher);
          net.mcreator.minepiece.command.Batch08TestCommands.register(dispatcher);
          net.mcreator.minepiece.command.TerrainTestCommands.register(dispatcher);
          net.mcreator.minepiece.command.PermanentTerrainTests.register(dispatcher);
         net.mcreator.minepiece.command.AmaruLabCommands.register(dispatcher);
         net.mcreator.minepiece.command.HakiCommands.register(dispatcher);
         LOGGER.info("OPWL commands registered: /island, /crew, /devilfruit.");
      });

      // worldgen custom — enregistrement des codecs (carte One Piece)
      Registry.register(BuiltInRegistries.CHUNK_GENERATOR, id("one_piece"), OnePieceChunkGenerator.CODEC);
      Registry.register(BuiltInRegistries.BIOME_SOURCE, id("one_piece"), OnePieceBiomeSource.CODEC);
      LOGGER.info("OPWL worldgen codecs registered: chunk generator + biome source 'opworldlike:one_piece'.");

      // TODO (non portés) : CombatRecursionGuard, MobBalanceEvents, NaturalLootEvents,
      //   ImportedIslandChestLootEvents, OnePieceFakePlayerCommands, loot condition 'regional_loot_chance'.
   }

   public static Identifier id(String path) {
      return Identifier.fromNamespaceAndPath("opworldlike", path);
   }

   public static int worldRadius() {
      return WorldLayoutConfig.worldRadius();
   }

   public static int worldHalfHeight() {
      return WorldLayoutConfig.worldHalfHeight();
   }

   public static int worldCornerRadius() {
      return WorldLayoutConfig.worldCornerRadius();
   }
}

