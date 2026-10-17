package net.mcreator.minepiece.network;
import java.util.UUID;
import net.fabricmc.fabric.api.networking.v1.*;
import net.mcreator.minepiece.procedures.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
/** Body coats are independent of Zoan form, owned by exact avatar UUID. */
public final class MaterialBodyState {
 public static final CustomPacketPayload.Type<Payload> TYPE=new CustomPacketPayload.Type<>(Identifier.fromNamespaceAndPath("minepiece","material_body"));
 public record Payload(UUID player,int flags) implements CustomPacketPayload {
  public static final StreamCodec<RegistryFriendlyByteBuf,Payload> CODEC=StreamCodec.of((b,p)->{b.writeUUID(p.player);b.writeVarInt(p.flags);},b->new Payload(b.readUUID(),b.readVarInt()));
  public Type<? extends CustomPacketPayload> type(){return TYPE;}
 }
 public static int flags(ServerPlayer p){return (DiamondBody.isActive(p)?1:0)|(SupaBlades.bladesActive(p)?2:0);}
 public static void register(){PayloadTypeRegistry.clientboundPlay().register(TYPE,Payload.CODEC);EntityTrackingEvents.START_TRACKING.register((entity,observer)->{if(entity instanceof ServerPlayer p && ServerPlayNetworking.canSend(observer,TYPE))ServerPlayNetworking.send(observer,new Payload(p.getUUID(),flags(p)));});}
 public static void send(ServerPlayer p){var payload=new Payload(p.getUUID(),flags(p));if(p.connection!=null && ServerPlayNetworking.canSend(p,TYPE))ServerPlayNetworking.send(p,payload);for(var observer:PlayerLookup.tracking(p))if(observer!=p && ServerPlayNetworking.canSend(observer,TYPE))ServerPlayNetworking.send(observer,payload);}
}
