from pathlib import Path
P=Path(__file__).resolve().parents[1];J=P/'src/main/java';proc=J/'net/mcreator/minepiece/procedures'
p=proc/'ZoanForms.java';s=p.read_text(encoding='utf-8');s=s.replace('form=n;}}','form=n;if(fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_HITO_HITO_DAIBUTSU)left=br.com.oneminepiece.opworldlike.daibutsu.DevilFruitDaibutsuConfig.formMaxTicks();}}',1)
s=s.replace('return b;return null;','return configured(b);return null;',1)
s=s.replace('private static double hybridScale(String key){return switch(key){case "hito_hito_daibutsu"->3.5;','private static Body configured(Body b){if(!b.fruit.equals("hito_hito_daibutsu"))return b;double scale=Math.min(b.scale,br.com.oneminepiece.opworldlike.daibutsu.DevilFruitDaibutsuConfig.maxScale()),ratio=scale/b.scale;return new Body(b.fruit,b.width*ratio,b.height*ratio,b.eye*ratio,scale);}\n private static double hybridScale(String key){return switch(key){case "hito_hito_daibutsu"->Math.min(3.5,br.com.oneminepiece.opworldlike.daibutsu.DevilFruitDaibutsuConfig.maxScale());',1)
s=s.replace('var b=BODIES.get(key);double scale','var b=configured(BODIES.get(key));double scale',1).replace('vars.player_form=n;modifier','vars.player_form=n;vars.formRemainingTicks=s.left;modifier',1)
s=s.replace('n==2?.3:.2,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL','key.equals("hito_hito_daibutsu")?br.com.oneminepiece.opworldlike.daibutsu.DevilFruitDaibutsuConfig.mobilityPenalty():n==2?.3:.2,AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL',1).replace('BODIES.get(key).scale)-1','configured(BODIES.get(key)).scale)-1',1)
s=s.replace('double cost=.05;','double cost=s.fruit==MinepieceModItems.ITEM_DEVIL_FRUIT_HITO_HITO_DAIBUTSU?br.com.oneminepiece.opworldlike.daibutsu.DevilFruitDaibutsuConfig.maintenanceCostPerSecond()/20:.05;',1)
s=s.replace('new Body("inu_inu_okuchi_no_makami",1.6,1.8,1.4,1.3)','new Body("inu_inu_okuchi_no_makami",1.6,2.1,1.4,1.3)',1);p.write_text(s,encoding='utf-8')
p=J/'net/mcreator/minepiece/command/Batch07TestCommands.java';s=p.read_text(encoding='utf-8').replace('.is(Blocks.ICE)','.is(Blocks.PACKED_ICE)');p.write_text(s,encoding='utf-8')
p=J/'net/mcreator/minepiece/command/RepairTestCommands.java';s=p.read_text(encoding='utf-8')
s=s.replace('boolean diamond = DiamondBody.begin(caster), ice = IceDefense.begin(caster), remote = RemoteControl.begin(caster);','''var iceOwner=net.fabricmc.fabric.api.entity.FakePlayer.get(world,new com.mojang.authlib.GameProfile(java.util.UUID.nameUUIDFromBytes("FoundationIceOwner".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"FoundationIceOwner"));
      MinepieceModVariables.get(iceOwner).PlayerDevilFruit=new ItemStack(MinepieceModItems.ITEM_DEVIL_FRUIT_INU_INU_OKUCHI_NO_MAKAMI);iceOwner.setHealth(20);
      boolean diamond = DiamondBody.begin(caster), ice = IceDefense.begin(iceOwner), remote = RemoteControl.begin(caster);''',1)
s=s.replace('"five active owned states plus a pending Hado"','"four caster states plus a correctly attributed Makami defense and pending Hado"',1)
s=s.replace('      check("reset-owned-states", !DiamondBody.isActive(caster) && !IceDefense.isActive(caster)','      DevilFruitPowerLogic.resetDevilFruitPowers(iceOwner);MinepieceModVariables.clear(iceOwner);iceOwner.discard();\n      check("reset-owned-states", !DiamondBody.isActive(caster) && !IceDefense.isActive(iceOwner)',1)
s=s.replace('DevilFruitTransformation.toggle(caster) && GiantForm.isGiant(caster)','DevilFruitTransformation.toggle(caster) && ZoanForms.has(caster)',1)
s=s.replace('for (int i = 0; i < 20; i++) GiantForm.tick(caster);','for (int i = 0; i < 20; i++) ZoanForms.tick();',1)
s=s.replace('check("giant-restore-budget", GiantForm.remainingTicks(caster) == remaining, "login doesn\'t refill timer");','check("giant-restore-budget", !ZoanForms.has(caster)&&vars.formRemainingTicks==0&&vars.player_form==0, "transient giant ends on login; timer never refilled");',1)
s=s.replace('GiantForm.tick(caster); GiantForm.tick(caster);','ZoanForms.tick(); ZoanForms.tick();',1);p.write_text(s,encoding='utf-8')
print('B07 initial fixture corrections and Daibutsu existing config preserved')
