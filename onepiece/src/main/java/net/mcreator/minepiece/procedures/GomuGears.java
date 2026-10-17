package net.mcreator.minepiece.procedures;
import java.util.*;
import net.mcreator.minepiece.init.*;
import net.mcreator.minepiece.network.*;
import net.minecraft.server.level.*;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.AABB;
/** Gears2/3 from ver15; Fourth/Fifth explicitly authored extensions requested by the user. */
public final class GomuGears {
 public static final String FRUIT="minepiece:item_devil_fruit_gomu_gomu";
 public static final Identifier SPEED=Identifier.fromNamespaceAndPath("minepiece","gomu_gear_speed"),ATTACK=Identifier.fromNamespaceAndPath("minepiece","gomu_gear_attack"),SCALE=Identifier.fromNamespaceAndPath("minepiece","gomu_gear_scale");
 private static final class Form {final ServerPlayer owner;final ServerLevel world;final int gear;int left=1200;Form(ServerPlayer p,int g){owner=p;world=(ServerLevel)p.level();gear=g;}}
 private static final Map<UUID,Form> FORMS=new HashMap<>();
 public static int gear(ServerPlayer p){var f=FORMS.get(p.getUUID());return f!=null && f.owner==p?f.gear:0;}
 public static boolean fourth(net.minecraft.world.entity.LivingEntity p){var a=p.getAttribute(Attributes.SCALE);var m=a==null?null:a.getModifier(SCALE);return m!=null&&Math.abs(m.amount()-.5)<.001;}
 public static int remaining(ServerPlayer p){var f=FORMS.get(p.getUUID());return f==null?0:f.left;}
 public static int next(ServerPlayer p){int g=gear(p);return g==0?2:g==5?0:g+1;}
 public static boolean canCycle(ServerPlayer p){int n=next(p);if(!BatchRules.usable(p,(ServerLevel)p.level(),MinepieceModItems.ITEM_DEVIL_FRUIT_GOMU_GOMU))return false;if(n<4)return true;double w=n==4?1.8:.8,h=n==4?2.85:2.16;var box=new AABB(p.getX()-w/2,p.getY(),p.getZ()-w/2,p.getX()+w/2,p.getY()+h,p.getZ()+w/2);return GoroAmaru.safeVolume((ServerLevel)p.level(),box) && p.level().noCollision(p,box.deflate(.001));}
 public static boolean cycle(ServerPlayer p,boolean paid){if(!canCycle(p))return false;var vars=MinepieceModVariables.get(p);if(!paid && next(p)!=0 && vars.PlayerEnergy<40)return false;int n=next(p);if(!paid && n!=0)vars.PlayerEnergy-=40;end(p);if(n==0)return true;
  FORMS.put(p.getUUID(),new Form(p,n));vars.player_form=n;modifier(p,Attributes.MOVEMENT_SPEED,SPEED,n==2?.2:n==4?.3:n==5?.4:0,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);modifier(p,Attributes.ATTACK_DAMAGE,ATTACK,.2,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);modifier(p,Attributes.SCALE,SCALE,n==4?.5:n==5?.2:0,AttributeModifier.Operation.ADD_VALUE);if(n==4&&!p.getAbilities().mayfly&&!p.getAbilities().instabuild&&!p.isSpectator()){vars.gomuFlightGranted=true;MinepieceModVariables.saveAll();p.getAbilities().mayfly=true;p.onUpdateAbilities();}p.refreshDimensions();sync(p);p.sendSystemMessage(net.minecraft.network.chat.Component.literal("Gear "+n+" — V : forme suivante, durée60s ; maintien d'énergie."),true);return true;
 }
 private static void modifier(ServerPlayer p,net.minecraft.core.Holder<Attribute> type,Identifier id,double value,AttributeModifier.Operation operation){var a=p.getAttribute(type);if(a!=null){a.removeModifier(id);if(value!=0)a.addTransientModifier(new AttributeModifier(id,value,operation));}}
 public static void tick(){for(var f:new ArrayList<>(FORMS.values())){var p=f.owner;if(!BatchRules.usable(p,f.world,MinepieceModItems.ITEM_DEVIL_FRUIT_GOMU_GOMU)||--f.left<=0){end(p);continue;}var v=MinepieceModVariables.get(p);v.PlayerEnergy=Math.max(0,v.PlayerEnergy-(f.gear>=4?.2:.1));if(v.PlayerEnergy==0){end(p);continue;}if(f.left%5==0 && (f.gear==2||f.gear==5))f.world.sendParticles(f.gear==5?ParticleTypes.CLOUD:ParticleTypes.WHITE_SMOKE,p.getX(),p.getY()+1,p.getZ(),3,.35,.6,.35,.01);if(f.left%20==0)sync(p);}}
 public static void end(ServerPlayer p){var f=FORMS.get(p.getUUID());if(f!=null && f.owner!=p)return;FORMS.remove(p.getUUID());var vars=MinepieceModVariables.get(p);if(vars.gomuFlightGranted){vars.gomuFlightGranted=false;MinepieceModVariables.saveAll();if(!p.getAbilities().instabuild&&!p.isSpectator()){p.getAbilities().mayfly=false;p.getAbilities().flying=false;p.onUpdateAbilities();}}for(var type:List.of(Attributes.MOVEMENT_SPEED,Attributes.ATTACK_DAMAGE,Attributes.SCALE)){var a=p.getAttribute(type);if(a!=null)for(var id:List.of(SPEED,ATTACK,SCALE))a.removeModifier(id);}if(f!=null || DevilFruitPowerLogic.getDevilFruit(p).is(MinepieceModItems.ITEM_DEVIL_FRUIT_GOMU_GOMU)){MinepieceModVariables.get(p).player_form=0;p.refreshDimensions();sync(p);}}
 public static void clear(){for(var f:new ArrayList<>(FORMS.values()))end(f.owner);}
 private static void sync(ServerPlayer p){DevilFruitNetworking.sendTransformState(p,MinepieceModVariables.get(p).player_form,FRUIT);DevilFruitNetworking.sendSyncState(p);}
}
