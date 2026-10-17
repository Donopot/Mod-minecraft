package net.mcreator.minepiece.procedures;

import java.util.*;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.AABB;
import net.mcreator.minepiece.network.*;
import net.mcreator.minepiece.init.MinepieceModItems;
import br.com.oneminepiece.opworldlike.flight.DevilFruitFlightConfig;

/** Hybrid -> complete beast -> base. Complete beasts are an explicit extension of the ver15 hybrids. */
public final class ZoanForms {
 public record Body(String fruit,double width,double height,double eye,double scale){}
 private static final Identifier ATTACK=id("attack"),ARMOR=id("armor"),SPEED=id("speed");
 private static final class State {final ServerPlayer owner;final ServerLevel world;final Item fruit;final int form;int left=1200;State(ServerPlayer p,int n){owner=p;world=(ServerLevel)p.level();fruit=DevilFruitPowerLogic.getDevilFruit(p).getItem();form=n;if(fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_HITO_HITO_DAIBUTSU)left=br.com.oneminepiece.opworldlike.daibutsu.DevilFruitDaibutsuConfig.formMaxTicks();}}
 private static final Map<UUID,State> ACTIVE=new HashMap<>();
 private static final Map<String,Body> BODIES=Map.ofEntries(Map.entry("inu_inu_wolf",new Body("inu_inu_wolf",1.2,1.2,0.95,1)),Map.entry("neko_neko_leopard",new Body("neko_neko_leopard",1.3,1.35,1.1,1)),Map.entry("neko_neko_sabertiger",new Body("neko_neko_sabertiger",1.6,1.5,1.2,1.2)),Map.entry("ushi_ushi_giraffe",new Body("ushi_ushi_giraffe",1.5,3.4,3.05,1.5)),Map.entry("batto_batto",new Body("batto_batto",1.2,1.2,1,1)),Map.entry("inu_inu_okuchi_no_makami",new Body("inu_inu_okuchi_no_makami",1.6,2.1,1.4,1.3)),Map.entry("hito_hito_daibutsu",new Body("hito_hito_daibutsu",4.5,6.4,5.5,2.5)),Map.entry("tori_tori_phoenix",new Body("tori_tori_phoenix",2.4,1.5,1.05,1.5)),Map.entry("uo_uo_seiryu",new Body("uo_uo_seiryu",6,4.5,3.8,2)),Map.entry("ryu_ryu_pteranodon",new Body("ryu_ryu_pteranodon",2.4,1.8,1.35,1.5)),Map.entry("ryu_ryu_brachiosaurus",new Body("ryu_ryu_brachiosaurus",2.6,5.8,5.1,2)),Map.entry("ryu_ryu_pachycephalosaurus",new Body("ryu_ryu_pachycephalosaurus",2,2.9,2.3,1.8)));
 private ZoanForms(){}
 private static Identifier id(String key){return Identifier.fromNamespaceAndPath("minepiece","zoan_"+key);}
 private static Identifier marker(String fruit,int form){return id("body_"+fruit+"_"+form);}
 public static boolean handles(ItemStack fruit){return BODIES.containsKey(TechniqueTerrain.fruitKey(fruit.getItem()));}
 public static boolean has(ServerPlayer p){var s=ACTIVE.get(p.getUUID());return s!=null&&s.owner==p;}
 public static Body body(LivingEntity entity){var attr=entity.getAttribute(Attributes.SCALE);if(attr==null)return null;for(var b:BODIES.values())if(attr.getModifier(marker(b.fruit,2))!=null)return configured(b);return null;}
 public static int form(ServerPlayer p){var s=ACTIVE.get(p.getUUID());return s!=null&&s.owner==p?s.form:0;}
 public static int remaining(ServerPlayer p){var s=ACTIVE.get(p.getUUID());return s==null||s.owner!=p?0:s.left;}
 private static Body configured(Body b){if(!b.fruit.equals("hito_hito_daibutsu"))return b;double scale=Math.min(b.scale,br.com.oneminepiece.opworldlike.daibutsu.DevilFruitDaibutsuConfig.maxScale()),ratio=scale/b.scale;return new Body(b.fruit,b.width*ratio,b.height*ratio,b.eye*ratio,scale);}
 private static double hybridScale(String key){return switch(key){case "hito_hito_daibutsu"->Math.min(3.5,br.com.oneminepiece.opworldlike.daibutsu.DevilFruitDaibutsuConfig.maxScale());case "ryu_ryu_brachiosaurus","ryu_ryu_pachycephalosaurus","ushi_ushi_giraffe","uo_uo_seiryu"->1.75;case "inu_inu_wolf","neko_neko_leopard","inu_inu_okuchi_no_makami"->1.5;default->1;};}
 private static boolean winged(String key,int form){return Set.of("batto_batto","tori_tori_phoenix","ryu_ryu_pteranodon").contains(key)||key.equals("uo_uo_seiryu")&&form==2;}
 private static boolean heavy(String key){return Set.of("ryu_ryu_brachiosaurus","ryu_ryu_pachycephalosaurus","hito_hito_daibutsu","uo_uo_seiryu","inu_inu_okuchi_no_makami").contains(key);}

 public static boolean canCycle(ServerPlayer p){var f=DevilFruitPowerLogic.getDevilFruit(p);if(!handles(f)||!BatchRules.usable(p,(ServerLevel)p.level(),f.getItem()))return false;int n=form(p)==2?0:form(p)+1;if(n==0)return true;String key=TechniqueTerrain.fruitKey(f.getItem());var b=configured(BODIES.get(key));double scale=n==1?hybridScale(key):b.scale,w=n==1?.6*scale:b.width,h=n==1?1.8*scale:b.height;var box=new AABB(p.getX()-w/2,p.getY(),p.getZ()-w/2,p.getX()+w/2,p.getY()+h,p.getZ()+w/2);return GoroAmaru.safeVolume((ServerLevel)p.level(),box)&&p.level().noCollision(p,box.deflate(.001));}
 public static boolean cycle(ServerPlayer p){if(!canCycle(p))return false;var vars=MinepieceModVariables.get(p);int n=form(p)==2?0:form(p)+1;if(n!=0&&vars.PlayerEnergy<40)return false;if(n!=0)vars.PlayerEnergy-=40;end(p);if(n==0)return true;String key=TechniqueTerrain.fruitKey(vars.PlayerDevilFruit.getItem());var s=new State(p,n);ACTIVE.put(p.getUUID(),s);vars.player_form=n;vars.formRemainingTicks=s.left;modifier(p,Attributes.ATTACK_DAMAGE,ATTACK,heavy(key)?9:6,AttributeModifier.Operation.ADD_VALUE);modifier(p,Attributes.ARMOR,ARMOR,key.equals("uo_uo_seiryu")?8:4,AttributeModifier.Operation.ADD_VALUE);modifier(p,Attributes.MOVEMENT_SPEED,SPEED,key.equals("hito_hito_daibutsu")?br.com.oneminepiece.opworldlike.daibutsu.DevilFruitDaibutsuConfig.mobilityPenalty():n==2?.3:.2,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);modifier(p,Attributes.SCALE,marker(key,n),(n==1?hybridScale(key):configured(BODIES.get(key)).scale)-1,AttributeModifier.Operation.ADD_VALUE);
  if(winged(key,n)&&DevilFruitFlightConfig.enabled()&&!p.getAbilities().mayfly&&!p.getAbilities().instabuild&&!p.isSpectator()){vars.zoanFlightGranted=true;MinepieceModVariables.saveAll();p.getAbilities().mayfly=true;p.onUpdateAbilities();}
  p.refreshDimensions();sync(p);p.sendSystemMessage(net.minecraft.network.chat.Component.literal((n==1?"Forme hybride":"Forme animale complète")+" — V : forme suivante ; durée60s, maintien d'énergie."),true);return true;
 }
 private static void modifier(ServerPlayer p,net.minecraft.core.Holder<Attribute> type,Identifier key,double value,AttributeModifier.Operation op){var a=p.getAttribute(type);if(a!=null){a.removeModifier(key);a.addTransientModifier(new AttributeModifier(key,value,op));}}
 public static void end(ServerPlayer p){var s=ACTIVE.get(p.getUUID());if(s!=null&&s.owner!=p)return;ACTIVE.remove(p.getUUID(),s);var v=MinepieceModVariables.get(p);boolean owned=s!=null||v.zoanFlightGranted||handles(v.PlayerDevilFruit);if(!owned)return;
  for(var type:List.of(Attributes.ATTACK_DAMAGE,Attributes.ARMOR,Attributes.MOVEMENT_SPEED,Attributes.SCALE)){var a=p.getAttribute(type);if(a==null)continue;for(var key:List.of(ATTACK,ARMOR,SPEED))a.removeModifier(key);for(var b:BODIES.values())for(int n=1;n<=2;n++)a.removeModifier(marker(b.fruit,n));}
  revokeFlight(p);v.player_form=0;v.formRemainingTicks=0;p.refreshDimensions();sync(p);
 }
 public static void revokeFlight(ServerPlayer p){var v=MinepieceModVariables.get(p);if(!v.zoanFlightGranted)return;v.zoanFlightGranted=false;MinepieceModVariables.saveAll();if(!p.getAbilities().instabuild&&!p.isSpectator()){p.getAbilities().mayfly=false;p.getAbilities().flying=false;p.onUpdateAbilities();}}
 public static void clear(){for(var s:new ArrayList<>(ACTIVE.values()))end(s.owner);}
 public static void tick(){for(var s:new ArrayList<>(ACTIVE.values())){var p=s.owner;var v=MinepieceModVariables.get(p);if(!BatchRules.usable(p,s.world,s.fruit)||v.player_form!=s.form||--s.left<=0){end(p);continue;}double cost=s.fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_HITO_HITO_DAIBUTSU?br.com.oneminepiece.opworldlike.daibutsu.DevilFruitDaibutsuConfig.maintenanceCostPerSecond()/20:.05;if(v.zoanFlightGranted){if(!DevilFruitFlightConfig.enabled()){revokeFlight(p);}else if(p.getAbilities().flying){cost+=DevilFruitFlightConfig.costPerSecond()/20;if(v.PlayerEnergy<DevilFruitFlightConfig.minimumEnergy()){v.PlayerEnergy=0;end(p);continue;}}}if(v.PlayerEnergy<cost){v.PlayerEnergy=0;end(p);continue;}v.PlayerEnergy-=cost;v.formRemainingTicks=s.left;if(s.left%20==0)sync(p);}}
 private static void sync(ServerPlayer p){var v=MinepieceModVariables.get(p);DevilFruitNetworking.sendTransformState(p,v.player_form,net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(v.PlayerDevilFruit.getItem()).toString());DevilFruitNetworking.sendSyncState(p);}
}
