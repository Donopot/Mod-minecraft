package net.mcreator.minepiece.network;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.EntityTrackingEvents;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.mcreator.minepiece.procedures.DevilFruitSkillDispatcher;
import net.mcreator.minepiece.procedures.DevilFruitTransformation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;

/**
 * Réseau de la couche pouvoir : paquets C2S « skill », « transform » et « cycle » déclenchés par les
 * keybinds client. Fabric 26.3 : {@code CustomPacketPayload} + {@code StreamCodec} ; registre C2S via
 * {@code PayloadTypeRegistry.serverboundPlay()} (l'ancien {@code playC2S()} a été renommé).
 *
 * <p>CORE-027 : les handlers valident l'état du joueur (vivant, non spectateur) avant d'agir ; le reste
 * (fruit possédé, suppression, recharge, énergie) est validé dans le dispatcher lui-même.</p>
 */
public final class DevilFruitNetworking {

   public static final CustomPacketPayload.Type<SkillCastPayload> SKILL_CAST =
      new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("minepiece", "skill_cast"));

   public static final CustomPacketPayload.Type<FlameThrustPayload> FLAME_THRUST =
      new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("minepiece","flame_thrust"));

   public static final CustomPacketPayload.Type<LabScenePayload> LAB_SCENE =
      new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("minepiece","lab_scene"));

   public static final CustomPacketPayload.Type<SkillReleasePayload> SKILL_RELEASE =
      new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("minepiece", "skill_release"));

   public static final CustomPacketPayload.Type<TransformPayload> TRANSFORM =
      new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("minepiece", "transform"));

   public static final CustomPacketPayload.Type<CycleTechniquePayload> CYCLE_TECHNIQUE =
      new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("minepiece", "cycle_technique"));

   public static final CustomPacketPayload.Type<TransformStatePayload> TRANSFORM_STATE =
      new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("minepiece", "transform_state"));

   /** S2C — l'animation d'une technique (la source : MinepieceModAnimationMessage). */
   public static final CustomPacketPayload.Type<AnimationPayload> PLAY_ANIMATION =
      new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("minepiece", "play_animation"));

   public static final CustomPacketPayload.Type<SyncStatePayload> SYNC_STATE =
      new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("minepiece", "sync_state"));

   private record FormKey(double form, String fruit) {}
   private static final java.util.Map<UUID, FormKey> LAST_FORMS = new java.util.HashMap<>();

   private DevilFruitNetworking() {
   }

   /** À appeler depuis l'entrypoint principal (serveur). */
   public static void registerServer() {
      PayloadTypeRegistry.clientboundPlay().register(LAB_SCENE,LabScenePayload.STREAM_CODEC);
      PayloadTypeRegistry.clientboundPlay().register(FLAME_THRUST,FlameThrustPayload.STREAM_CODEC);
      PayloadTypeRegistry.serverboundPlay().register(SKILL_RELEASE, SkillReleasePayload.STREAM_CODEC);
      ServerPlayNetworking.registerGlobalReceiver(SKILL_RELEASE,(payload,context)->{net.mcreator.minepiece.procedures.MeraTechniques.release(context.player());net.mcreator.minepiece.procedures.MaguTechniques.release(context.player());net.mcreator.minepiece.procedures.GoroTechniques.release(context.player());});
      PayloadTypeRegistry.serverboundPlay().register(SKILL_CAST, SkillCastPayload.STREAM_CODEC);
      ServerPlayNetworking.registerGlobalReceiver(SKILL_CAST, (payload, context) -> {
         ServerPlayer player = context.player();
         if (player.isAlive() && !player.isSpectator()) {
            DevilFruitSkillDispatcher.castSkill(player);
         }
      });

      PayloadTypeRegistry.serverboundPlay().register(TRANSFORM, TransformPayload.STREAM_CODEC);
      ServerPlayNetworking.registerGlobalReceiver(TRANSFORM, (payload, context) -> {
         ServerPlayer player = context.player();
         if (player.isAlive() && !player.isSpectator()) {
            DevilFruitTransformation.toggle(player);
         }
      });

      PayloadTypeRegistry.serverboundPlay().register(CYCLE_TECHNIQUE, CycleTechniquePayload.STREAM_CODEC);
      ServerPlayNetworking.registerGlobalReceiver(CYCLE_TECHNIQUE, (payload, context) -> {
         ServerPlayer player = context.player();
         if (player.isAlive() && !player.isSpectator()) {
            DevilFruitSkillDispatcher.cycleTechnique(player);
         }
      });

      PayloadTypeRegistry.clientboundPlay().register(TRANSFORM_STATE, TransformStatePayload.STREAM_CODEC);
      PayloadTypeRegistry.clientboundPlay().register(SYNC_STATE, SyncStatePayload.STREAM_CODEC);
      PayloadTypeRegistry.clientboundPlay().register(PLAY_ANIMATION, AnimationPayload.STREAM_CODEC);
      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPED.register(server -> LAST_FORMS.clear());
      net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents.DISCONNECT.register((handler, server) ->
         LAST_FORMS.remove(handler.player.getUUID()));
      EntityTrackingEvents.START_TRACKING.register((entity, observer) -> {
         if (entity instanceof ServerPlayer subject) {
            var vars = MinepieceModVariables.get(subject);
            String fruit = vars.PlayerDevilFruit.isEmpty() ? ""
               : BuiltInRegistries.ITEM.getKey(vars.PlayerDevilFruit.getItem()).toString();
            ServerPlayNetworking.send(observer, new TransformStatePayload(subject.getUUID(), vars.player_form, fruit));
         }
      });
   }

   /** Envoie l'état de transformation (forme + fruit) au client. */
   public static void sendTransformState(ServerPlayer player, double form, String fruitId) {
      LAST_FORMS.put(player.getUUID(), new FormKey(form, fruitId));
      TransformStatePayload payload = new TransformStatePayload(player.getUUID(), form, fruitId);
      ServerPlayNetworking.send(player, payload);
      for (ServerPlayer observer : PlayerLookup.tracking(player)) {
         if (observer != player) ServerPlayNetworking.send(observer, payload);
      }
   }

   /** Synchronise l'état complet (fruit, énergie, forme, sélection) vers le client (CORE-004). */
   public static void sendSyncState(ServerPlayer player) {
      MinepieceModVariables.PlayerVariables vars = MinepieceModVariables.get(player);
      String fruitId = vars.PlayerDevilFruit.isEmpty()
         ? ""
         : BuiltInRegistries.ITEM.getKey(vars.PlayerDevilFruit.getItem()).toString();
      FormKey form = new FormKey(vars.player_form, fruitId);
      if (!form.equals(LAST_FORMS.get(player.getUUID()))) sendTransformState(player, vars.player_form, fruitId);
      int technique = (int) vars.PlayerTechniqueUsedNumber;
      // CORE-028 : le HUD affiche le nom localisé de la technique et « n/total » — le client a donc
      // besoin de l'id de la technique courante et du nombre de techniques du Fruit.
      java.util.List<net.mcreator.minepiece.procedures.DevilFruitMove> moves = vars.PlayerDevilFruit.isEmpty()
         ? java.util.List.of()
         : net.mcreator.minepiece.procedures.DevilFruitRegistry.movesFor(vars.PlayerDevilFruit.getItem());
      String techniqueId = moves.isEmpty() || technique < 0 || technique >= moves.size()
         ? ""
         : moves.get(technique).id();
      ServerPlayNetworking.send(player, new SyncStatePayload(
         fruitId, vars.PlayerEnergy, vars.PlayerEnergyMAX, vars.player_form, technique,
         techniqueId, moves.size(),
         net.mcreator.minepiece.procedures.WeightState.confirmedForClient(player)));
   }

   public record SkillCastPayload() implements CustomPacketPayload {
      public static final StreamCodec<RegistryFriendlyByteBuf, SkillCastPayload> STREAM_CODEC =
         StreamCodec.unit(new SkillCastPayload());

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return SKILL_CAST;
      }
   }

   public record FlameThrustPayload(boolean active) implements CustomPacketPayload {
      public static final StreamCodec<RegistryFriendlyByteBuf,FlameThrustPayload> STREAM_CODEC=StreamCodec.of((buf,payload)->buf.writeBoolean(payload.active()),buf->new FlameThrustPayload(buf.readBoolean()));
      @Override public Type<? extends CustomPacketPayload> type() {return FLAME_THRUST;}
   }

   public record LabScenePayload(String scene) implements CustomPacketPayload {
      public static final StreamCodec<RegistryFriendlyByteBuf,LabScenePayload> STREAM_CODEC=StreamCodec.of((buf,payload)->buf.writeUtf(payload.scene()),buf->new LabScenePayload(buf.readUtf()));
      @Override public Type<? extends CustomPacketPayload> type() {return LAB_SCENE;}
   }

   public record SkillReleasePayload() implements CustomPacketPayload {
      public static final StreamCodec<RegistryFriendlyByteBuf,SkillReleasePayload> STREAM_CODEC=StreamCodec.unit(new SkillReleasePayload());
      @Override public Type<? extends CustomPacketPayload> type() {return SKILL_RELEASE;}
   }

   public record TransformPayload() implements CustomPacketPayload {
      public static final StreamCodec<RegistryFriendlyByteBuf, TransformPayload> STREAM_CODEC =
         StreamCodec.unit(new TransformPayload());

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return TRANSFORM;
      }
   }

   public record CycleTechniquePayload() implements CustomPacketPayload {
      public static final StreamCodec<RegistryFriendlyByteBuf, CycleTechniquePayload> STREAM_CODEC =
         StreamCodec.unit(new CycleTechniquePayload());

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return CYCLE_TECHNIQUE;
      }
   }

   /**
    * L'animation à jouer et l'entité concernée.
    *
    * <p>La source envoyait le nom et l'identifiant de l'entité ({@code MinepieceModAnimationMessage})
    * au joueur <b>puis</b> aux joueurs qui le suivent : le port reprend les deux envois, sans quoi
    * l'animation ne serait visible que par celui qui la lance.</p>
    */
   public record AnimationPayload(String animation, int entityId) implements CustomPacketPayload {
      public static final StreamCodec<RegistryFriendlyByteBuf, AnimationPayload> STREAM_CODEC =
         StreamCodec.of(
            (RegistryFriendlyByteBuf buf, AnimationPayload p) -> {
               buf.writeUtf(p.animation);
               buf.writeInt(p.entityId);
            },
            (RegistryFriendlyByteBuf buf) -> new AnimationPayload(buf.readUtf(), buf.readInt())
         );

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return PLAY_ANIMATION;
      }
   }

   /** Demande au client de jouer une animation de joueur. */
   public static void sendAnimation(ServerPlayer to, String animation, int entityId) {
      ServerPlayNetworking.send(to, new AnimationPayload(animation, entityId));
   }

   public record TransformStatePayload(UUID playerId, double playerForm, String fruitId) implements CustomPacketPayload {
      public static final StreamCodec<RegistryFriendlyByteBuf, TransformStatePayload> STREAM_CODEC =
         StreamCodec.of(
            (RegistryFriendlyByteBuf buf, TransformStatePayload p) -> {
               buf.writeUUID(p.playerId);
               buf.writeDouble(p.playerForm);
               buf.writeUtf(p.fruitId);
            },
            (RegistryFriendlyByteBuf buf) -> new TransformStatePayload(buf.readUUID(), buf.readDouble(), buf.readUtf())
         );

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return TRANSFORM_STATE;
      }
   }

   public record SyncStatePayload(String fruitId, double energy, double energyMax, double form, int technique,
                                  String techniqueId, int techniqueCount, double weight)
         implements CustomPacketPayload {
      public static final StreamCodec<RegistryFriendlyByteBuf, SyncStatePayload> STREAM_CODEC =
         StreamCodec.of(
            (RegistryFriendlyByteBuf buf, SyncStatePayload p) -> {
               buf.writeUtf(p.fruitId);
               buf.writeDouble(p.energy);
               buf.writeDouble(p.energyMax);
               buf.writeDouble(p.form);
               buf.writeInt(p.technique);
               buf.writeUtf(p.techniqueId);
               buf.writeInt(p.techniqueCount);
               // FRUIT-KIRO_KIRO-03 : la valeur CONFIRMEE du poids (jamais la demande) — « l'interface
               // suit l'état confirmé ». L'ordre écriture/lecture est identique : c'est le seul contrat
               // du codec, et une inversion ne se verrait que sur un vrai client.
               buf.writeDouble(p.weight);
            },
            (RegistryFriendlyByteBuf buf) -> new SyncStatePayload(
               buf.readUtf(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readInt(),
               buf.readUtf(), buf.readInt(), buf.readDouble())
         );

      @Override
      public Type<? extends CustomPacketPayload> type() {
         return SYNC_STATE;
      }
   }
}
