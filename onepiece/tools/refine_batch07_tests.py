from pathlib import Path
P=Path(__file__).resolve().parents[1];J=P/'src/main/java'
p=J/'net/mcreator/minepiece/command/DevilFruitTestCommands.java';s=p.read_text(encoding='utf-8')
# Only the R3/R4 giant integration fixture switches to the authoritative form service.
a=s.index('      double base = 6.0;',s.index('boolean ouvert =')-300 if 'boolean ouvert =' in s else 0);b=s.index('      // ============================ R5',a)
part=s[a:b].replace('"giant_speed"','"zoan_speed"').replace('GiantForm.isGiant(caster)','ZoanForms.has(caster)').replace('GiantForm.remainingTicks(caster)','ZoanForms.remaining(caster)').replace('GiantForm.tick(caster)','ZoanForms.tick()');s=s[:a]+part+s[b:]
if 'import net.mcreator.minepiece.procedures.ZoanForms;' not in s:s=s.replace('import net.mcreator.minepiece.procedures.GiantForm;','import net.mcreator.minepiece.procedures.GiantForm;\nimport net.mcreator.minepiece.procedures.ZoanForms;')
p.write_text(s,encoding='utf-8')
p=J/'net/mcreator/minepiece/command/Batch07TestCommands.java';s=p.read_text(encoding='utf-8').replace('ServerPlayer caster,cancelCaster;','ServerPlayer caster,cancelCaster,defenseCaster;')
anchor=' cancelCaster=FakePlayer.get(world,';assert anchor in s
extra=''' defenseCaster=FakePlayer.get(world,new GameProfile(UUID.nameUUIDFromBytes("B07Defense".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"B07Defense"));MinepieceModVariables.get(defenseCaster).PlayerDevilFruit=new ItemStack(FRUITS[0]);defenseCaster.setHealth(20);defenseCaster.teleportTo(730.5,100,110.5);defenseCaster.addEffect(new MobEffectInstance(MobEffects.RESISTANCE,2000,2));check("life/defense-begin",IceDefense.begin(defenseCaster),"native expiry without player-list insertion");
'''
s=s.replace(anchor,extra+anchor,1)
s=s.replace('private static void lifeTick(){++life;','private static void lifeTick(){++life;if(life==220)check("life/defense-expiry",!IceDefense.isActive(defenseCaster)&&defenseCaster.hasEffect(MobEffects.RESISTANCE),"native200ticks; foreign resistance retained");',1)
s=s.replace('forms.clear();DevilFruitPowerLogic.resetDevilFruitPowers(cancelCaster);','forms.clear();DevilFruitPowerLogic.resetDevilFruitPowers(defenseCaster);MinepieceModVariables.clear(defenseCaster);defenseCaster.discard();DevilFruitPowerLogic.resetDevilFruitPowers(cancelCaster);',1)
p.write_text(s,encoding='utf-8')
p=P/'tools/run_local_tests.py';s=p.read_text(encoding='utf-8');a=s.index('            if args.batch07:');b=s.index('            if args.batch06:',a);part=s[a:b].replace("['projectile-cast','projectile-live','projectile-cancelled']","['projectile-cast','projectile-live','projectile-cancelled','defense-begin','defense-expiry']");s=s[:a]+part+s[b:];p.write_text(s,encoding='utf-8')
p=P/'tools/launch_visual_lab.py';s=p.read_text(encoding='utf-8');a=s.index("            if frozen.get('capture') and frozen.get('scenario')=='batch07':");b=s.index("            elif frozen.get('capture') and frozen.get('scenario')=='batch06':",a);part=s[a:b].replace("                expected_pal=Counter", "                required|={f'{f}/forms-native-flight' for f in catalogue if f in {'tori_tori_phoenix','uo_uo_seiryu','ryu_ryu_pteranodon'}}\n                expected_pal=Counter",1);s=s[:a]+part+s[b:];p.write_text(s,encoding='utf-8')
print('B07 exact502 native oracle; grouped graphical47 checks/client')
