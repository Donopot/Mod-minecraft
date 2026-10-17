from pathlib import Path
import re
J=Path(__file__).resolve().parents[1]/'src/main/java/net/mcreator/minepiece/procedures'
for filename,prefix,states,owners,release in [('StickyState.java','sticky','STICKY','SOURCE','release'),('Roots.java','root','HELD','HELD_BY','release')]:
 p=J/filename;s=p.read_text(encoding='utf-8')
 field='''
   private record Binding(LivingEntity owner,net.minecraft.server.level.ServerLevel level,net.minecraft.world.item.Item fruit){}
   private static final Map<UUID,Binding> BINDINGS=new HashMap<>();
'''
 s=s.replace('   private '+filename.removesuffix('.java')+'() {',field+'\n   private '+filename.removesuffix('.java')+'() {',1)
 # Original references are authoritative; never resolve a removed entity to a UUID clone.
 start=s.index('   private static LivingEntity resolve(UUID id) {');end=s.index('\n   }',start)
 s=s[:start]+'''   private static LivingEntity resolve(UUID id) {return REFS.get(id);'''+s[end:]
 if filename=='StickyState.java':
  s=s.replace('      STICKY.put(cible.getUUID(), DevilFruitMochiConfig.stickyDurationTicks());','''      if(source!=null && (source.level()!=cible.level() || !source.isAlive() || source.isRemoved() || source instanceof ServerPlayer player && !BatchRules.target(player,cible)))return false;
      if(STICKY.containsKey(cible.getUUID()))return REFS.get(cible.getUUID())==cible;
      if(source!=null)BINDINGS.put(cible.getUUID(),new Binding(source,(ServerLevel)source.level(),source instanceof ServerPlayer player?DevilFruitPowerLogic.getDevilFruit(player).getItem():null));
      STICKY.put(cible.getUUID(), DevilFruitMochiConfig.stickyDurationTicks());''',1)
  s=s.replace('return cible != null && STICKY.containsKey(cible.getUUID());','return cible != null && REFS.get(cible.getUUID())==cible && STICKY.containsKey(cible.getUUID());')
 else:
  s=s.replace('      // L\'ORDRE DES OPÉRATIONS','      BINDINGS.put(cible.getUUID(),new Binding(source,(ServerLevel)source.level(),DevilFruitPowerLogic.getDevilFruit(source).getItem()));\n      // L\'ORDRE DES OPÉRATIONS',1)
  s=s.replace('      if (cible == null || !cible.isAlive()) {','      if (source==null || !BatchRules.target(source,cible))return false;\n      if (cible == null || !cible.isAlive()) {',1)
  # Binding only accepted episodes, not an effect refusal.
  s=s.replace('            return false;\n         }\n      }\n      OWNER_REFS.put','            BINDINGS.remove(cible.getUUID());return false;\n         }\n      }\n      OWNER_REFS.put',1)
  s=s.replace('return cible != null && HELD.containsKey(cible.getUUID());','return cible != null && REFS.get(cible.getUUID())==cible && HELD.containsKey(cible.getUUID());')
 s=s.replace('      REFS.remove(cible.getUUID());','      REFS.remove(cible.getUUID());\n      BINDINGS.remove(cible.getUUID());')
 # Tick checks original owner/world/fruit before touching effects.
 needle='         LivingEntity cible = resolve(id);'
 guard='''
         Binding binding=BINDINGS.get(id);
         if(binding!=null && (binding.owner.isRemoved() || !binding.owner.isAlive() || binding.owner.level()!=binding.level || cible==null || cible.isRemoved() || cible.level()!=binding.level || binding.owner instanceof ServerPlayer player && (DevilFruitPowerLogic.isSuppressed(player) || !DevilFruitPowerLogic.getDevilFruit(player).is(binding.fruit)))){
            if(cible!=null)release(cible,"retrait");else {'''+states+'''.remove(id);'''+owners+'''.remove(id);BINDINGS.remove(id);REFS.remove(id);}continue;
         }
'''
 s=s.replace(needle,needle+guard,1)
 # Release removed references too; never scan all entities/worlds.
 s=s.replace('         if (cible == null || !cible.isAlive()) {','         if (cible == null || !cible.isAlive() || cible.isRemoved()) {',1)
 clear='      '+states+'.clear();'
 s=s.replace(clear,clear+'\n      BINDINGS.clear();REFS.clear();')
 register='   public static void register() {'
 s=s.replace(register,register+'\n      net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.SERVER_STOPPING.register(server->releaseEverything("arret"));',1)
 p.write_text(s,encoding='utf-8')
# A legacy helper must never claim a failed hit or stick an invulnerable target.
p=J/'GomuArmStrike.java';s=p.read_text(encoding='utf-8');s=s.replace('      if (isMochi(move.id())) {\n         net.mcreator.minepiece.procedures.StickyState.stick(target, player);\n      }','')
s=s.replace('         target.hurtServer((ServerLevel) player.level(), source, (float) step);\n         target.knockback(direction.x, 0.35, direction.z, source, (float) move.knockback());\n         total += step;','''         if(CombatSkillLogic.hurtAndConfirm(target,(ServerLevel)player.level(),source,(float)step)){
            if(isMochi(move.id()))StickyState.stick(target,player);
            CombatSkillLogic.applySkillKnockback(target,direction,source,move.knockback());total+=step;
         }''');p.write_text(s,encoding='utf-8')
# Legacy target throw is only a push, never a distant pre-emptive terrain edit.
p=J/'RikiForce.java';s=p.read_text(encoding='utf-8');s=s.replace('      casserBlocsManipulables(level, cible.position().add(direction.scale(portee)), BREAK_RADIUS);','      // Terrain changes require an actual collision handled by MaterialBatchTechniques.');s=s.replace('               if (isProtected(etat)) {','               if (!level.hasChunkAt(pos) || !level.getWorldBorder().isWithinBounds(pos) || DevilFruitExplosions.isProtectedPosition(level,pos) || etat.hasBlockEntity() || level.getBlockEntity(pos)!=null || etat.getBlock().getExplosionResistance()>6 || !etat.getFluidState().isEmpty() || PowerBlockService.isTrackedAny(level,pos) || isProtected(etat)) {');s=s.replace('               level.destroyBlock(pos, false);\n               casses++;','               if(pos.distSqr(origine)<=rayon*rayon && casses<96){var cover=level.clip(new net.minecraft.world.level.ClipContext(centre,Vec3.atCenterOf(pos),net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,null));if(cover.getType()==net.minecraft.world.phys.HitResult.Type.MISS || cover.getBlockPos().equals(pos)){level.destroyBlock(pos,false);casses++;}}');p.write_text(s,encoding='utf-8')
print('B03 status references, finite leases and legacy terrain hardened')
