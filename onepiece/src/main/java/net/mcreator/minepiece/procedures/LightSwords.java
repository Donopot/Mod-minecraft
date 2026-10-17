package net.mcreator.minepiece.procedures;
import java.util.*;
import net.fabricmc.fabric.api.event.lifecycle.v1.*;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.mcreator.minepiece.init.MinepieceModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;

/** Native source sword with a finite private lease. Only its tagged grant can be removed. */
public final class LightSwords {
 public static final int DURATION=600;
 private static final String KEY="opwl_light_sword";
 private static final class Lease {final ServerPlayer owner;final ServerLevel world;final String token=UUID.randomUUID().toString();int ttl=DURATION;Lease(ServerPlayer p){owner=p;world=(ServerLevel)p.level();}}
 private static final Map<UUID,Lease> ACTIVE=new HashMap<>();
 private LightSwords(){}
 private static String token(ItemStack stack){return stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getStringOr(KEY,"");}
 public static boolean grant(ServerPlayer p){
  if(!(p.level() instanceof ServerLevel l) || !BatchRules.usable(p,l,MinepieceModItems.ITEM_DEVIL_FRUIT_PIKA_PIKA) || ACTIVE.containsKey(p.getUUID()))return false;
  for(int i=0;i<p.getInventory().getContainerSize();i++)if(p.getInventory().getItem(i).is(MinepieceModItems.AMANO_MURAKUMO))return false;
  Lease lease=new Lease(p);ItemStack stack=new ItemStack(MinepieceModItems.AMANO_MURAKUMO);CompoundTag tag=new CompoundTag();tag.putString(KEY,lease.token);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(tag));
  if(p.getMainHandItem().isEmpty())p.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,stack);else if(!p.getInventory().add(stack))return false;
  ACTIVE.put(p.getUUID(),lease);return true;
 }
 public static void inventoryTick(ItemStack stack,Entity holder){String token=token(stack);if(token.isEmpty())return;var l=holder instanceof ServerPlayer p?ACTIVE.get(p.getUUID()):null;if(l==null || l.owner!=holder || !l.token.equals(token) || !BatchRules.usable(l.owner,l.world,MinepieceModItems.ITEM_DEVIL_FRUIT_PIKA_PIKA))stack.setCount(0);}
 public static void clearFor(ServerPlayer p){var l=ACTIVE.remove(p.getUUID());if(l==null)return;for(int i=0;i<p.getInventory().getContainerSize();i++){var stack=p.getInventory().getItem(i);if(l.token.equals(token(stack)))stack.setCount(0);}}
 public static int activeCount(){return ACTIVE.size();}
 public static void register(){
  ServerTickEvents.END_SERVER_TICK.register(s->{for(var l:new ArrayList<>(ACTIVE.values()))if(--l.ttl<=0 || !BatchRules.usable(l.owner,l.world,MinepieceModItems.ITEM_DEVIL_FRUIT_PIKA_PIKA))clearFor(l.owner);});
  ServerEntityEvents.ENTITY_LOAD.register((e,l)->{if(e instanceof ItemEntity item && !token(item.getItem()).isEmpty())item.discard();});
  ServerPlayConnectionEvents.DISCONNECT.register((h,s)->clearFor(h.player));
  ServerLifecycleEvents.SERVER_STOPPING.register(s->{for(var l:new ArrayList<>(ACTIVE.values()))clearFor(l.owner);});
 }
}
