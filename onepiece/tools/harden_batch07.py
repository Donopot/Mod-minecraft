"""Initial owned-state hardening and native acceptance fixture authoring."""
from pathlib import Path
P=Path(__file__).resolve().parents[1];J=P/'src/main/java';proc=J/'net/mcreator/minepiece/procedures'
p=proc/'IceDefense.java';s=p.read_text(encoding='utf-8')
s=s.replace('private static final Map<UUID, Integer> ACTIVE = new HashMap<>();','''private static final class Defense { final ServerPlayer owner;final ServerLevel world;final MobEffectInstance marker;int left;Defense(ServerPlayer p,int duration,MobEffectInstance m){owner=p;world=(ServerLevel)p.level();left=duration;marker=m;} }
   private static final Map<UUID, Defense> ACTIVE = new HashMap<>();''')
s=s.replace('for (ServerPlayer player : new ArrayList<>(server.getPlayerList().getPlayers())) {\n            tick(player);','for (Defense state : new ArrayList<>(ACTIVE.values())) {\n            tick(state.owner);')
s=s.replace('      ServerPlayConnectionEvents.DISCONNECT.register','      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(s -> endAll("arret"));\n      ServerPlayConnectionEvents.DISCONNECT.register',1)
s=s.replace('return player != null && ACTIVE.containsKey(player.getUUID());','return player != null && ACTIVE.get(player.getUUID())!=null && ACTIVE.get(player.getUUID()).owner==player;')
s=s.replace('return player == null ? 0 : ACTIVE.getOrDefault(player.getUUID(), 0);','return !isActive(player) ? 0 : ACTIVE.get(player.getUUID()).left;')
s=s.replace('if (player == null || !player.isAlive() || DevilFruitPowerLogic.isSuppressed(player))','if (player == null || !BatchRules.usable(player,(ServerLevel)player.level(),net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_OKUCHI_NO_MAKAMI) || isActive(player) || player.hasEffect(MinepieceModMobEffects.ICE_DEFENSE))')
s=s.replace('      ACTIVE.put(player.getUUID(), duration);\n','')
s=s.replace('         false, false, true));','         false, false, true));\n      ACTIVE.put(player.getUUID(),new Defense(player,duration,player.getEffect(MinepieceModMobEffects.ICE_DEFENSE)));',1)
s=s.replace('if (player == null || ACTIVE.remove(player.getUUID()) == null) {','Defense state=player==null?null:ACTIVE.get(player.getUUID());\n      if (state==null || state.owner!=player || !ACTIVE.remove(player.getUUID(),state)) {')
s=s.replace('if (MinepieceModMobEffects.ICE_DEFENSE != null) {','if (MinepieceModMobEffects.ICE_DEFENSE != null && player.getEffect(MinepieceModMobEffects.ICE_DEFENSE)==state.marker) {')
start=s.index('      var server =',s.index('public static void endAll'));end=s.index('\n   }',start)
s=s[:start]+'''      for(Defense state:new ArrayList<>(ACTIVE.values()))end(state.owner,reason);'''+s[end:]
s=s.replace('Integer remaining = ACTIVE.get(player.getUUID());','Defense state = ACTIVE.get(player.getUUID());\n      Integer remaining=state==null||state.owner!=player?null:state.left;')
s=s.replace('      if (!player.isAlive()) {','      if(player.isRemoved() || player.level()!=state.world || !DevilFruitPowerLogic.getDevilFruit(player).is(net.mcreator.minepiece.init.MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_OKUCHI_NO_MAKAMI)){end(player,"autorite");return;}\n      if (!player.isAlive()) {')
s=s.replace('ACTIVE.put(player.getUUID(), remaining - 1);','state.left=remaining-1;');p.write_text(s,encoding='utf-8')
# Counter is owned by its attacker, not by a victim resetting their own fruit.
p=proc/'ZoanCounterState.java';s=p.read_text(encoding='utf-8').replace('if(s.owner==p||s.target==p)end(s);','if(s.owner==p)end(s);');p.write_text(s,encoding='utf-8')
# Element routing remains technique-specific even in shared damage/logia checks.
p=proc/'DevilFruitElement.java';s=p.read_text(encoding='utf-8').replace('      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_UO_UO_SEIRYU)', '''      if(fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_TORI_TORI_PHOENIX))return ElementType.PHYSICAL;
      if(fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_OKUCHI_NO_MAKAMI))return java.util.Set.of("narikabura","namujihyoga","himorogiri").contains(technique)?ElementType.ICE:ElementType.PHYSICAL;
      if (fruit.is(MinepieceModItems.ITEM_DEVIL_FRUIT_UO_UO_SEIRYU)''',1);p.write_text(s,encoding='utf-8')
print('B07 owned ice defense and physical Phoenix contacts hardened')
