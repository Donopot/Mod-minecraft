package net.mcreator.minepiece.client;

import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranim.animation.PlayerAnimationController;
import com.zigythebird.playeranim.animation.PlayerRawAnimationBuilder;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.mcreator.minepiece.network.DevilFruitNetworking;
import net.minecraft.resources.Identifier;

/**
 * <b>La couche d'animation de joueur (PAL), isolée dans sa propre classe.</b>
 *
 * <p>Pourquoi isolée : PAL est une bibliothèque <i>externe</i>. Quand une classe qui l'appelle est
 * chargée alors que la bibliothèque manque, la machine virtuelle lève {@code NoClassDefFoundError}
 * et <b>tout ce que la classe portait tombe avec elle</b>. Tant que cette couche vivait dans
 * l'initialiseur client, une PAL absente emportait aussi les particules, les renderers d'entités et
 * le HUD : le serveur continuait de fonctionner, le client n'affichait plus rien, et rien dans le
 * jeu ne disait pourquoi. Séparée, son échec ne coûte que les gestes, et il est nommé dans le
 * journal.</p>
 */
public final class DevilFruitAnimationLayer {

   /** Couche PAL du mod : une seule, où toutes les techniques se posent. */
   private static final Identifier TECHNIQUE_LAYER = Identifier.fromNamespaceAndPath("minepiece", "technique");

   /** Priorité de la couche : la documentation réserve 1500+ aux « animations de jeu importantes ». */
   private static final int PRIORITE = 1500;

   private static final org.slf4j.Logger LOGGER = org.slf4j.LoggerFactory.getLogger("MinePieceAnimation");

   private static final class PoseCheck {
      final int entity;final String animation;int age;double firstTicks,maxPose;boolean matching=true;
      PoseCheck(int entity,String animation){this.entity=entity;this.animation=animation;}
   }
   private static final java.util.List<PoseCheck> POSE_CHECKS=new java.util.ArrayList<>();
   private DevilFruitAnimationLayer() {
   }

   /**
    * Enregistre la fabrique de couche et le récepteur du paquet d'animation.
    *
    * <p>Les corps sont des MÉTHODES NOMMÉES et non des lambdas imbriquées : la première version passait
    * une lambda créant un objet à l'intérieur d'une lambda de réception, et javac 25 plantait
    * (ClassCastException dans com.sun.tools.javac.code.Symtab) — un bug du compilateur, pas du code,
    * mais qui bloquait tout le build. Sortir les corps en méthodes statiques l'évite.</p>
    */
   public static void register() {
      PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(TECHNIQUE_LAYER, PRIORITE,
         DevilFruitAnimationLayer::fabriqueDeCouche);
      ClientPlayNetworking.registerGlobalReceiver(DevilFruitNetworking.PLAY_ANIMATION,
         DevilFruitAnimationLayer::recevoirAnimation);
      if(Boolean.getBoolean("opworldlike.visualCapture")) net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents.END_CLIENT_TICK.register(DevilFruitAnimationLayer::verifyPoses);
      LOGGER.info("couche d'animation PAL prête (priorité {}, couche {})", PRIORITE, TECHNIQUE_LAYER);
   }

   /** Priorité déclarée — reportée par l'auto-contrôle de démarrage. */
   public static int priorite() {
      return PRIORITE;
   }

   /** La couche PAL d'un joueur : une par joueur, comme l'attend l'API. */
   private static com.zigythebird.playeranimcore.animation.layered.IAnimation fabriqueDeCouche(
      net.minecraft.world.entity.Avatar avatar) {
      // Le second argument est la « machine à états » de PAL : elle décide ce qui joue à chaque tick.
      // Les techniques, elles, déclenchent par triggerAnimation() à la réception du paquet — l'état
      // n'a donc rien à poser ici.
      PlayerAnimationController controller = new PlayerAnimationController(avatar, DevilFruitAnimationLayer::etatDeLaCouche);
      controller.setFirstPersonModeHandler(DevilFruitAnimationLayer::firstPersonMode);
      controller.setFirstPersonConfiguration(new com.zigythebird.playeranimcore.api.firstPerson.FirstPersonConfiguration(
         true, true, true, true, true));
      controller.setFirstPersonFollowsCamera(false);
      return controller;
   }

   /** Apply animated arms only during the requested technique; preserve both held items and armor. */
   private static com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode firstPersonMode(
         com.zigythebird.playeranimcore.animation.AnimationController controller) {
      if(controller.isPlayingTriggeredAnimation() && !controller.hasAnimationFinished()){
         var current=controller.getCurrentAnimationInstance();
         var netAnimation=com.zigythebird.playeranim.animation.PlayerAnimResources.getAnimation(
            net.minecraft.resources.Identifier.fromNamespaceAndPath("minepiece","b06_wolf_localareanetwork_release"));
         // Keep Net's complete world gesture without crossing the local camera with both arms.
         if(current!=null&&netAnimation!=null&&current.uuid().equals(netAnimation.uuid()))
            return com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode.VANILLA;
         return com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode.THIRD_PERSON_MODEL;
      }
      return com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode.NONE;
   }

   /** Machine à états de la couche : elle laisse jouer ce qui a été déclenché. */
   private static com.zigythebird.playeranimcore.enums.PlayState etatDeLaCouche(
      com.zigythebird.playeranimcore.animation.AnimationController controleur,
      com.zigythebird.playeranimcore.animation.AnimationData donnees,
      com.zigythebird.playeranimcore.animation.AnimationController.AnimationSetter setter) {
      // La documentation est explicite : « Animations triggered with triggerAnimation override this,
      // so a handler that always returns STOP is perfectly normal for a layer that only plays triggered
      // animations. » Un gestionnaire qui répondrait CONTINUE ferait jouer à chaque image ce qui traîne
      // au lieu de laisser la technique déclenchée s'exprimer.
      return com.zigythebird.playeranimcore.enums.PlayState.STOP;
   }

   /**
    * Reçoit l'animation à jouer et la pose sur la couche du joueur concerné.
    *
    * <p>Le paquet nomme l'animation et l'entité : on retrouve l'entité par son identifiant, puis on
    * déclenche sur sa couche. Si l'animation n'a pas de fichier, PAL ne joue rien — c'est pourquoi le
    * serveur n'en envoie jamais une sans fichier.</p>
    */
   private static void recevoirAnimation(DevilFruitNetworking.AnimationPayload payload,
      net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.Context context) {
      net.minecraft.client.Minecraft client = context.client();
      client.execute(() -> {
         if (client.level == null) {
            return;
         }
         if (!(client.level.getEntity(payload.entityId()) instanceof net.minecraft.world.entity.Avatar avatar)) {
            return;
         }
         RigClientState.action(client.level,avatar,payload.animation());
         if (PlayerAnimationAccess.getPlayerAnimationLayer(avatar, TECHNIQUE_LAYER)
            instanceof PlayerAnimationController controleur) {
            controleur.triggerAnimation(PlayerRawAnimationBuilder.begin()
               .thenPlay(net.minecraft.resources.Identifier.fromNamespaceAndPath("minepiece",
                  payload.animation()))
               .build());
            if(Boolean.getBoolean("opworldlike.visualCapture") && (payload.animation().startsWith("b08_") || payload.animation().startsWith("b07_") || payload.animation().startsWith("b06_") || payload.animation().startsWith("b05_") || payload.animation().startsWith("b04_") || payload.animation().startsWith("b03_") || payload.animation().startsWith("b02_") || payload.animation().startsWith("b01_") || payload.animation().startsWith("magu_") || payload.animation().startsWith("goro_"))) POSE_CHECKS.add(new PoseCheck(payload.entityId(),payload.animation()));
         }
      });
   }
   private static void verifyPoses(net.minecraft.client.Minecraft client) {
      if(client.level==null){POSE_CHECKS.clear();return;}
      for(PoseCheck check:new java.util.ArrayList<>(POSE_CHECKS)) {
         if(++check.age!=2 && check.age!=4)continue;
         boolean matches=false,active=false;double ticks=0;
         if(client.level.getEntity(check.entity) instanceof net.minecraft.world.entity.Avatar avatar
               && PlayerAnimationAccess.getPlayerAnimationLayer(avatar,TECHNIQUE_LAYER) instanceof PlayerAnimationController controller) {
            var expected=com.zigythebird.playeranim.animation.PlayerAnimResources.getAnimation(Identifier.fromNamespaceAndPath("minepiece",check.animation));
            var current=controller.getCurrentAnimationInstance();
            matches=expected!=null && current!=null && expected.uuid().equals(current.uuid());
            active=controller.isActive() && controller.isPlayingTriggeredAnimation() && !controller.hasAnimationFinished();
            ticks=controller.getAnimationTicks();
            for(String name:new String[]{"right_arm","left_arm","body"}) {
               var bone=controller.get3DTransformRaw(new com.zigythebird.playeranimcore.bones.PlayerAnimBone(name));
               check.maxPose=Math.max(check.maxPose,bone.rotation.lengthSquared()+bone.position.lengthSquared());
            }
         }
         check.matching &= matches && active;
         if(check.age==2)check.firstTicks=ticks;
         else {
            boolean advanced=ticks>check.firstTicks;
            LOGGER.info("[{}-pal] {} {} matched={} advanced={} pose={}",check.animation.startsWith("b08_")?"batch08":check.animation.startsWith("b07_")?"batch07":check.animation.startsWith("b06_")?"batch06":check.animation.startsWith("b05_")?"batch05":check.animation.startsWith("b04_")?"batch04":check.animation.startsWith("b03_")?"batch03":check.animation.startsWith("b02_")?"batch02":check.animation.startsWith("b01_")?"batch01":check.animation.startsWith("goro_")?"goro":"magu",check.animation,check.matching && advanced && check.maxPose>.001?"PASS":"FAIL",check.matching,advanced,check.maxPose);
            POSE_CHECKS.remove(check);
         }
      }
   }

}
