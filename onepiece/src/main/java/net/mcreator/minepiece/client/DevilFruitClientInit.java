package net.mcreator.minepiece.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.mcreator.minepiece.client.layer.PlayerTransformLayer;
import net.mcreator.minepiece.client.model.ModelhitoHitoDaibutsuHumanbeast;
import net.mcreator.minepiece.client.model.ModelhitoHitoDaibutsuHumanbeast2;
import net.mcreator.minepiece.client.model.ModelinuInuOkuchinomakamiHumanbeast;
import net.mcreator.minepiece.client.model.ModelinuInuWolfHumanbeast;
import net.mcreator.minepiece.client.model.ModelnekonekoLeopardHumanbeast;
import net.mcreator.minepiece.client.model.ModelnekonekoLeopardHumanbeast2;
import net.mcreator.minepiece.client.model.ModelnekonekoSabertigerHumanbeast;
import net.mcreator.minepiece.client.model.Modeloverlay;
import net.mcreator.minepiece.client.model.ModelryuRyuBrachiosaurusHumanbeast;
import net.mcreator.minepiece.client.model.ModelryuRyuBrachiosaurusHumanbeast2;
import net.mcreator.minepiece.client.model.ModelryuRyuPachycephalosaurusHumanbeast;
import net.mcreator.minepiece.client.model.ModelryuRyuPachycephalosaurusHumanbeast2;
import net.mcreator.minepiece.client.model.ModelryuRyuPteranodonHumanbeast;
import net.mcreator.minepiece.client.model.ModeltoritoriPhoenixHumanbeast;
import net.mcreator.minepiece.client.model.ModeltoritoriPhoenixHumanbeastFly;
import net.mcreator.minepiece.client.model.ModeluouoSeiryuHumanBeast;
import net.mcreator.minepiece.client.model.ModelushiUshiGiraffeHumanbeast;
import net.mcreator.minepiece.client.model.ModelushiUshiGiraffeHumanbeast2;
import net.mcreator.minepiece.client.model.ModelwingsBatBat;
import net.mcreator.minepiece.client.model.ModelmoriMoriFlower;
import net.mcreator.minepiece.client.model.ModelsupasupaForm2;
import net.mcreator.minepiece.client.model.ModelsupasupaForm22;
import net.mcreator.minepiece.client.model.Modelpaws;
import net.mcreator.minepiece.client.model.ModelpawsSlim;
import net.mcreator.minepiece.client.model.ModelhornsYamato;
import net.mcreator.minepiece.client.model.ModelhornsKaido;
import net.mcreator.minepiece.client.model.ModelenelDrum;
import net.mcreator.minepiece.client.model.ModelfeatherRobe;
import net.mcreator.minepiece.client.model.ModelinuInuWolfHumanbeastSlim;
import net.mcreator.minepiece.client.model.ModeloverlaySlim;
import net.mcreator.minepiece.client.model.ModelwingsBatBatFly;
import net.mcreator.minepiece.client.renderer.EntityGomuArmRenderer;
import net.mcreator.minepiece.client.renderer.SkillProjectileRenderer;
import net.mcreator.minepiece.client.renderer.SummonRenderer;
import net.mcreator.minepiece.init.MinepieceModEntities;
import net.mcreator.minepiece.network.DevilFruitNetworking;
import net.minecraft.client.KeyMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Côté client de la couche pouvoir : keybind « Skill 1 » (R par défaut) qui envoie le paquet C2S,
 * + enregistrement du renderer de l'entité skill (bras de Gomu Gomu).
 * Isolé derrière {@code ClientModInitializer} (l'entrypoint client n'est pas chargé par le serveur).
 */
public final class DevilFruitClientInit implements ClientModInitializer {

   private static final Logger LOGGER = LoggerFactory.getLogger("MinePieceDevilFruit");

   private static KeyMapping SKILL_KEY;
   private static boolean skillHeld;
   private static KeyMapping TRANSFORM_KEY;
   /** Lab uses the game key queue; no desktop input injection. */
   public static void clickTransformForLab() { if(Boolean.getBoolean("opworldlike.visualCapture")) KeyMapping.click(com.mojang.blaze3d.platform.InputConstants.Type.KEYBOARD.getOrCreate(25)); }
   private static KeyMapping CYCLE_KEY;

   @Override
   public void onInitializeClient() {
      MaterialClientState.register();
      ClientVisualLab.register();
      MeraFlameVisuals.register();
      SourceAssetParticles.register();
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.PHOENIX_WINGS,net.mcreator.minepiece.client.model.MythicalBeastModel::phoenixWings);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.MAKAMI_FACE,net.mcreator.minepiece.client.model.MythicalBeastModel::makamiFace);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.AncientBeastModel.BRACHIO,net.mcreator.minepiece.client.model.ReferenceBeastGeometry::brachiosaurusGuard);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.AncientBeastModel.PACHY,net.mcreator.minepiece.client.model.AncientBeastModel::pachy);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.AncientBeastModel.DOME,net.mcreator.minepiece.client.model.AncientBeastModel::dome);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.MAKAMI,net.mcreator.minepiece.client.model.MythicalBeastModel::makami);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.MAKAMI_MANE,net.mcreator.minepiece.client.model.MythicalBeastModel::makamiMane);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.DAIBUTSU,net.mcreator.minepiece.client.model.ReferenceBeastGeometry::daibutsu);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.PHOENIX,net.mcreator.minepiece.client.model.ReferenceBeastGeometry::phoenixFly);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.PTERO,net.mcreator.minepiece.client.model.ReferenceBeastGeometry::pteranodonFly);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.DRAGON,net.mcreator.minepiece.client.model.MythicalBeastModel::dragon);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.DRAGON_TRIM,net.mcreator.minepiece.client.model.MythicalBeastModel::dragonTrim);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.LandBeastModel.WOLF,net.mcreator.minepiece.client.model.LandBeastModel::wolf);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.LandBeastModel.WOLF_FACE,net.mcreator.minepiece.client.model.LandBeastModel::wolfFace);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.LandBeastModel.SABER_FANGS,net.mcreator.minepiece.client.model.LandBeastModel::fangs);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.LandBeastModel.SABER_MARKS,net.mcreator.minepiece.client.model.LandBeastModel::saberMarks);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.LandBeastModel.LEOPARD,net.mcreator.minepiece.client.model.ReferenceBeastGeometry::leopardWalk);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.LandBeastModel.GIRAFFE,net.mcreator.minepiece.client.model.ReferenceBeastGeometry::giraffeWalk);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.LandBeastModel.BAT,net.mcreator.minepiece.client.model.LandBeastModel::bat);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.GomuGearModel.BODY,net.mcreator.minepiece.client.model.GomuGearModel::bodyLayer);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.GomuGearModel.CLOUD,net.mcreator.minepiece.client.model.GomuGearModel::cloudLayer);
      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.AmaruModel.LAYER_LOCATION, net.mcreator.minepiece.client.model.AmaruModel::createBodyLayer);
      // renderer de l'entité skill (sans lui, le client NPE au rendu)
      // Apparence des particules extraites du jar source (FRUIT-YAMI_YAMI-06).
      MinepieceParticlesClient.register();
      EntityRendererRegistry.register(MinepieceModEntities.ENTITY_GOMU_ARM, EntityGomuArmRenderer::new);
      EntityRendererRegistry.register(MinepieceModEntities.SKILL_PROJECTILE, SkillProjectileRenderer::new);
      EntityRendererRegistry.register(MinepieceModEntities.SUMMON, SummonRenderer::new);

      // LayerDefinitions des modèles de transformation Zoan (humanbeast) — CORE-019
      ModelLayerRegistry.registerModelLayer(ModelinuInuWolfHumanbeast.LAYER_LOCATION, ModelinuInuWolfHumanbeast::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelnekonekoLeopardHumanbeast.LAYER_LOCATION, ModelnekonekoLeopardHumanbeast::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelnekonekoSabertigerHumanbeast.LAYER_LOCATION, ModelnekonekoSabertigerHumanbeast::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelushiUshiGiraffeHumanbeast.LAYER_LOCATION, ModelushiUshiGiraffeHumanbeast::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModeltoritoriPhoenixHumanbeast.LAYER_LOCATION, ModeltoritoriPhoenixHumanbeast::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelhitoHitoDaibutsuHumanbeast.LAYER_LOCATION, ModelhitoHitoDaibutsuHumanbeast::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModeluouoSeiryuHumanBeast.LAYER_LOCATION, ModeluouoSeiryuHumanBeast::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelryuRyuBrachiosaurusHumanbeast.LAYER_LOCATION, ModelryuRyuBrachiosaurusHumanbeast::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelryuRyuPachycephalosaurusHumanbeast.LAYER_LOCATION, ModelryuRyuPachycephalosaurusHumanbeast::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelryuRyuPteranodonHumanbeast.LAYER_LOCATION, ModelryuRyuPteranodonHumanbeast::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelinuInuOkuchinomakamiHumanbeast.LAYER_LOCATION, ModelinuInuOkuchinomakamiHumanbeast::createBodyLayer);
      // CORE-019 : corps « full beast » (humanbeast2, peau du joueur) + overlay + ailes + forme vol
      ModelLayerRegistry.registerModelLayer(ModelhitoHitoDaibutsuHumanbeast2.LAYER_LOCATION, ModelhitoHitoDaibutsuHumanbeast2::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelnekonekoLeopardHumanbeast2.LAYER_LOCATION, ModelnekonekoLeopardHumanbeast2::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelryuRyuBrachiosaurusHumanbeast2.LAYER_LOCATION, ModelryuRyuBrachiosaurusHumanbeast2::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelryuRyuPachycephalosaurusHumanbeast2.LAYER_LOCATION, ModelryuRyuPachycephalosaurusHumanbeast2::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelushiUshiGiraffeHumanbeast2.LAYER_LOCATION, ModelushiUshiGiraffeHumanbeast2::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(Modeloverlay.LAYER_LOCATION, Modeloverlay::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelwingsBatBat.LAYER_LOCATION, ModelwingsBatBat::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelwingsBatBatFly.LAYER_LOCATION, ModelwingsBatBatFly::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModeltoritoriPhoenixHumanbeastFly.LAYER_LOCATION, ModeltoritoriPhoenixHumanbeastFly::createBodyLayer);
      // FRUIT-*-TRANSFORM : modèles extraits du jar source ver15 (KleidersRenderEventProcedure).
      ModelLayerRegistry.registerModelLayer(ModelmoriMoriFlower.LAYER_LOCATION, ModelmoriMoriFlower::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelsupasupaForm2.LAYER_LOCATION, ModelsupasupaForm2::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelsupasupaForm22.LAYER_LOCATION, ModelsupasupaForm22::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(Modelpaws.LAYER_LOCATION, Modelpaws::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelpawsSlim.LAYER_LOCATION, ModelpawsSlim::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelhornsYamato.LAYER_LOCATION, ModelhornsYamato::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelhornsKaido.LAYER_LOCATION, ModelhornsKaido::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelenelDrum.LAYER_LOCATION, ModelenelDrum::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelfeatherRobe.LAYER_LOCATION, ModelfeatherRobe::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModelinuInuWolfHumanbeastSlim.LAYER_LOCATION, ModelinuInuWolfHumanbeastSlim::createBodyLayer);
      ModelLayerRegistry.registerModelLayer(ModeloverlaySlim.LAYER_LOCATION, ModeloverlaySlim::createBodyLayer);

      SKILL_KEY = KeyMappingHelper.registerKeyMapping(
         new KeyMapping(
            "key.minepiece.skill",
            InputConstants.Type.KEYBOARD,
            21, // SDL_SCANCODE_R (26.3 = SDL, PAS GLFW : R=21)
            KeyMapping.Category.GAMEPLAY
         )
      );

      TRANSFORM_KEY = KeyMappingHelper.registerKeyMapping(
         new KeyMapping(
            "key.minepiece.transform",
            InputConstants.Type.KEYBOARD,
            25, // SDL_SCANCODE_V (26.3 = SDL : V=25)
            KeyMapping.Category.GAMEPLAY
         )
      );

      CYCLE_KEY = KeyMappingHelper.registerKeyMapping(
         new KeyMapping(
            "key.minepiece.cycle",
            InputConstants.Type.KEYBOARD,
            5, // SDL_SCANCODE_B (26.3 = SDL : B=5) — cycle de technique
            KeyMapping.Category.GAMEPLAY
         )
      );

      // AUTO-CONTRÔLE CLIENT — une ligne, au démarrage, qui dit si la moitié client est chargée.
      // Sans elle, un mod dont le client n'a pas pris se comporte exactement comme un mod qui
      // fonctionne côté serveur mais ne dessine rien : aucune erreur, aucun repère. Cette ligne
      // rend le journal lisible (« est-ce que mon client a bien chargé cette moitié ? »).
      LOGGER.info("MinePiece — moitié CLIENT chargée : {} types de particules déclarés, renderers (bras"
         + " Gomu, projectile, invoc), couche d'animation PAL prio {} ; touches : R = technique,"
         + " B = cycle, V = transformation",
         MinepieceParticlesClient.nombreEnregistre(), DevilFruitAnimationLayer.priorite());
      net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
         skillHeld=false;
         TransformClientState.clear();
         DevilFruitClientState.reset();
      });
      ClientTickEvents.START_CLIENT_TICK.register(client -> {
         if(client.player!=null && client.player.isAlive() && DevilFruitClientState.flameThrust) {
            // Vanilla ends flying on the ground before applying a server impulse.
            // Mera thrust owns this short window; the server closes it on every stop path.
            client.player.setOnGround(false);
            if(!client.player.getAbilities().flying) {
               client.player.getAbilities().flying=true;client.player.onUpdateAbilities();
            }
         }
      });
      ClientTickEvents.END_CLIENT_TICK.register(client -> {
         TransformClientState.useLevel(client.level);
         // CORE-014 : n'envoyer que connecté (joueur présent) et canal supporté par le serveur.
         if (client.player == null) {
            return;
         }
         if(skillHeld && (!SKILL_KEY.isDown() || client.gui.screen()!=null)
               && ClientPlayNetworking.canSend(DevilFruitNetworking.SKILL_RELEASE)) {
            ClientPlayNetworking.send(new DevilFruitNetworking.SkillReleasePayload());skillHeld=false;
         }
         while (SKILL_KEY.consumeClick()) {
            boolean possible = ClientPlayNetworking.canSend(DevilFruitNetworking.SKILL_CAST);
            LOGGER.info("touche R (technique) pressée — canal ouvert : {}", possible);
            if (possible) {
               ClientPlayNetworking.send(new DevilFruitNetworking.SkillCastPayload());
               skillHeld=true;
            }
         }
         while (TRANSFORM_KEY.consumeClick()) {
            if (ClientPlayNetworking.canSend(DevilFruitNetworking.TRANSFORM)) {
               ClientPlayNetworking.send(new DevilFruitNetworking.TransformPayload());
            }
         }
         while (CYCLE_KEY.consumeClick()) {
            if (ClientPlayNetworking.canSend(DevilFruitNetworking.CYCLE_TECHNIQUE)) {
               ClientPlayNetworking.send(new DevilFruitNetworking.CycleTechniquePayload());
            }
         }
      });

      // Réception de l'état de transformation (S2C) → alimente la couche de rendu du modèle.
      // ANIMATIONS DE JOUEUR — la couche PAL et son déclenchement.
      //
      // Les corps sont des MÉTHODES NOMMÉES et non des lambdas imbriquées : la première version passait
      // une lambda créant un objet à l'intérieur d'une lambda de réception, et javac 25 plantait
      // (ClassCastException dans com.sun.tools.javac.code.Symtab) — un bug du compilateur, pas du code,
      // mais qui bloquait tout le build. Sortir les corps en méthodes statiques l'évite.
      // La couche d'animation vit dans sa propre classe : une PAL absente ne doit pas emporter les
      // particules, les renderers et le HUD avec elle (voir DevilFruitAnimationLayer).
      try {
         DevilFruitAnimationLayer.register();
      } catch (Throwable erreur) {
         LOGGER.error("Couche d'animation PAL INDISPONIBLE — les gestes de technique ne joueront pas,"
            + " mais le reste du client (particules, renderers, HUD) reste actif. Vérifie que"
            + " playeranimlib-26.3-1.2.8.jar est bien dans le dossier mods.", erreur);
      }

      ClientPlayNetworking.registerGlobalReceiver(DevilFruitNetworking.FLAME_THRUST,(payload,context)->context.client().execute(()->DevilFruitClientState.flameThrust=payload.active()));
      ClientPlayNetworking.registerGlobalReceiver(DevilFruitNetworking.TRANSFORM_STATE, (payload, context) -> {
         context.client().execute(() -> {
            TransformClientState.useLevel(context.client().level);
            TransformClientState.put(payload.playerId(), payload.playerForm(), payload.fruitId());
         });
      });

      // Réception de l'état complet (S2C) → HUD/état client (CORE-004).
      ClientPlayNetworking.registerGlobalReceiver(DevilFruitNetworking.SYNC_STATE, (payload, context) -> {
         TransformClientState.useLevel(context.client().level);
         if (context.client().player != null) TransformClientState.put(context.client().player.getUUID(), payload.form(), payload.fruitId());
         DevilFruitClientState.fruitId = payload.fruitId();
         DevilFruitClientState.energy = payload.energy();
         DevilFruitClientState.energyMax = payload.energyMax();
         DevilFruitClientState.form = payload.form();
         DevilFruitClientState.technique = payload.technique();
         DevilFruitClientState.techniqueId = payload.techniqueId();
         DevilFruitClientState.techniqueCount = payload.techniqueCount();
         DevilFruitClientState.weight = payload.weight();
      });

      // CORE-028 : HUD (fruit, énergie, technique, forme) — affiché seulement si le joueur a un Fruit.
      net.mcreator.minepiece.client.hud.DevilFruitHud.register();

      LOGGER.info("Devil Fruit client init OK — keybinds R (skill), V (transform), B (cycle) enregistrés.");
   }
}
