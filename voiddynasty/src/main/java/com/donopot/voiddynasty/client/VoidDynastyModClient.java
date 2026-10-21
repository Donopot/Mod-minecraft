package com.donopot.voiddynasty.client;

import com.donopot.voiddynasty.ControlPadScreen;
import com.donopot.voiddynasty.network.FleetListPayload;
import com.donopot.voiddynasty.registry.ModEntities;
import com.donopot.voiddynasty.registry.ModMenuTypes;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
/**
 * Point d'entrée client : keybindings, HUD, renderers et écrans.
 */
@Environment(EnvType.CLIENT)
public final class VoidDynastyModClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BolterKeybindings.init();
        BolterHud.init();
        LaserDrillEffects.init();
        LaserDrillBeamRenderer.init();
        EntityRendererRegistry.register(ModEntities.BOLT_PROJECTILE, BoltProjectileRenderer::new);
        EntityRendererRegistry.register(ModEntities.LASER_BEAM, LaserBeamRenderer::new);
        EntityRendererRegistry.register(ModEntities.CARNIFEX, CarnifexRenderer::new);
        EntityRendererRegistry.register(ModEntities.CARNIFEX_LEVIATHAN, CarnifexLeviathanRenderer::new);
        EntityRendererRegistry.register(ModEntities.LICTOR, LictorRenderer::new);
        EntityRendererRegistry.register(ModEntities.LEMAN_RUSS, LemanRussRenderer::new);
        // Jumeau de test du Leman Russ : meme renderer, donc meme maillage.
        EntityRendererRegistry.register(ModEntities.LEMAN_RUSS_V2, LemanRussRenderer::new);
        EntityRendererRegistry.register(ModEntities.NORN_EMISSARY, NornEmissaryRenderer::new);
        EntityRendererRegistry.register(ModEntities.GREAT_UNCLEAN_ONE, GreatUncleanOneRenderer::new);
        EntityRendererRegistry.register(ModEntities.LORD_OF_CHANGE, LordOfChangeRenderer::new);
        EntityRendererRegistry.register(ModEntities.KEEPER_OF_SECRETS, KeeperOfSecretsRenderer::new);
        EntityRendererRegistry.register(ModEntities.KHORNE_JUGGERNAUT, KhorneJuggernautRenderer::new);
        EntityRendererRegistry.register(ModEntities.LARGE_KHORNE_DAEMON, LargeKhorneDaemonRenderer::new);
        EntityRendererRegistry.register(ModEntities.CHAOS_HELLDRAKE, ChaosHellDrakeRenderer::new);
        EntityRendererRegistry.register(ModEntities.PLAGUETOAD, PlaguetoadRenderer::new);
        EntityRendererRegistry.register(ModEntities.GHAZGHKULL, GhazghkullRenderer::new);
        EntityRendererRegistry.register(ModEntities.TYRANID_WARRIOR, TyranidWarriorRenderer::new);
        EntityRendererRegistry.register(ModEntities.CULTIST_1, Cultist1Renderer::new);
        EntityRendererRegistry.register(ModEntities.CULTIST_2, Cultist2Renderer::new);
        EntityRendererRegistry.register(ModEntities.CULTIST_3, Cultist3Renderer::new);
        EntityRendererRegistry.register(ModEntities.CULTIST_4, Cultist4Renderer::new);
        EntityRendererRegistry.register(ModEntities.CULTIST_BOSS, CultistBossRenderer::new);
        EntityRendererRegistry.register(ModEntities.CHAOS_MARINE_1, ChaosMarine1Renderer::new);
        EntityRendererRegistry.register(ModEntities.CHAOS_MARINE_2, ChaosMarine2Renderer::new);
        EntityRendererRegistry.register(ModEntities.CHAOS_MARINE_3, ChaosMarine3Renderer::new);
        EntityRendererRegistry.register(ModEntities.CHAOS_CAPTAIN, ChaosCaptainRenderer::new);
        EntityRendererRegistry.register(ModEntities.CHAOS_RAPTOR, ChaosRaptorRenderer::new);
        EntityRendererRegistry.register(ModEntities.SERVITOR_1, Servitor1Renderer::new);
        EntityRendererRegistry.register(ModEntities.SERVITOR_2, Servitor2Renderer::new);
        EntityRendererRegistry.register(ModEntities.SERVITOR_3, Servitor3Renderer::new);
        EntityRendererRegistry.register(ModEntities.CHAOS_DREADNOUGHT, ChaosDreadnoughtRenderer::new);
        EntityRendererRegistry.register(ModEntities.CHAOS_HELLBRUTE, ChaosHellbruteRenderer::new);
        EntityRendererRegistry.register(ModEntities.CHAOS_FORGEFIEND, ChaosForgefiendRenderer::new);
        EntityRendererRegistry.register(ModEntities.DAEMON_PRINCE, DaemonPrinceRenderer::new);
        EntityRendererRegistry.register(ModEntities.PROJECTILE_CHAOS, ProjectileChaosRenderer::new);
        EntityRendererRegistry.register(ModEntities.SERVO_SKULL, ServoSkullRenderer::new);
        MenuScreens.register(ModMenuTypes.SUPER_CHEST, SuperChestScreen::new);

        // Réception de la liste de flotte pour le pad
        ClientPlayNetworking.registerGlobalReceiver(FleetListPayload.TYPE, (payload, context) -> {
            ControlPadScreen screen = ControlPadScreen.getInstance();
            if (screen != null) {
                screen.updateShips(payload.ships());
            }
        });
    }
}
