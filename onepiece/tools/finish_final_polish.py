"""Close measured visual/ownership defects before the final native and grouped client checks."""
from pathlib import Path
P=Path(__file__).resolve().parents[1];J=P/'src/main/java';C=J/'net/mcreator/minepiece/client'
def edit(p,old,new):
 s=p.read_text(encoding='utf-8');assert old in s,(p,old[:70]);p.write_text(s.replace(old,new,1),encoding='utf-8')
edit(C/'DevilFruitClientInit.java','ClientVisualLab.register();','ClientVisualLab.register();\n      ModelLayerRegistry.registerModelLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.MAKAMI_FACE,net.mcreator.minepiece.client.model.MythicalBeastModel::makamiFace);')
p=C/'layer/PlayerTransformLayer.java';s=p.read_text(encoding='utf-8').replace('MythicalBeastModel makamiMane,dragonTrim,phoenixFlames;','MythicalBeastModel makamiMane,makamiFace,dragonTrim,phoenixFlames;',1)
s=s.replace('EntityModelSet es = Minecraft.getInstance().getEntityModels();','EntityModelSet es = Minecraft.getInstance().getEntityModels();\n      makamiFace=new net.mcreator.minepiece.client.model.MythicalBeastModel(es.bakeLayer(net.mcreator.minepiece.client.model.MythicalBeastModel.MAKAMI_FACE),6);',1)
s=s.replace('         if(beastKey.equals("inu_inu_okuchi_no_makami"))renderColoredCutoutModel(makamiMane','         if(beastKey.equals("inu_inu_okuchi_no_makami"))renderColoredCutoutModel(makamiFace,tex("tex_white3"),poseStack,collector,lightCoords,state,0xFF342F3D,2);\n         if(beastKey.equals("inu_inu_okuchi_no_makami"))renderColoredCutoutModel(makamiMane',1)
s=s.replace('renderColoredCutoutModel(','stoneAware(')
anchor='   @Override\n   public void submit';helper='''   private void stoneAware(EntityModel<EntityRenderState> model,Identifier texture,PoseStack stack,SubmitNodeCollector collector,int light,AvatarRenderState state,int color,int order){
      if(((net.mcreator.minepiece.client.ParameciaRenderData)state).minepiece$stone()){texture=tex("tex_stone");color=0xFFBFC2C8;}
      renderColoredCutoutModel(model,texture,stack,collector,light,state,color,order);
   }

''';assert anchor in s;s=s.replace(anchor,helper+anchor,1)
s=s.replace('         int alpha=state.isInvisible?0x25000000:0xFF000000;','         if(((net.mcreator.minepiece.client.ParameciaRenderData)state).minepiece$stone()){stoneAware(amaru,tex("tex_stone"),poseStack,collector,lightCoords,state,-1,0);stoneAware(amaruDrums,tex("tex_stone"),poseStack,collector,lightCoords,state,-1,1);return;}\n         int alpha=state.isInvisible?0x25000000:0xFF000000;',1);p.write_text(s,encoding='utf-8')
# Neutral stone poses preserve anatomical pivots rather than loading all-zero limb positions.
edit(C/'TransformPoses.java','      return state instanceof TransformRenderData data ? data.minepiece$poses() : null;','''      if(state instanceof ParameciaRenderData stone&&stone.minepiece$stone())return new Snapshot(PartPose.ZERO,PartPose.ZERO,PartPose.offset(-5,2,0),PartPose.offset(5,2,0),PartPose.offset(-1.9F,12,0),PartPose.offset(1.9F,12,0));
      return state instanceof TransformRenderData data ? data.minepiece$poses() : null;''')
for name in ['LandBeastModel','MythicalBeastModel','AncientBeastModel']:
 p=C/'model'/f'{name}.java';s=p.read_text(encoding='utf-8');a=s.index('@Override public void setupAnim');b=s.index('var ',s.index(';',a)+1) if name=='MythicalBeastModel' else -1
 # A reset happens first in all three classes; return before poses or ambient flap/tail motion.
 needle='var poses=TransformPoses.forState(state);' if name=='LandBeastModel' else 'var pose=TransformPoses.forState(state);' if name=='MythicalBeastModel' else 'var p=TransformPoses.forState(state);'
 assert needle in s;s=s.replace(needle,'if(state instanceof ParameciaRenderData stone&&stone.minepiece$stone())return;'+needle,1);p.write_text(s,encoding='utf-8')
edit(C/'renderer/SummonRenderer.java','animatable.getTexture());','((net.mcreator.minepiece.procedures.ParameciaStoneData)animatable).minepiece$stoneFlag()?"tex_stone":animatable.getTexture());')
edit(J/'net/mcreator/minepiece/entity/SummonEntity.java','            String requested = getAnimation();','            if(((net.mcreator.minepiece.procedures.ParameciaStoneData)this).minepiece$stoneFlag())return PlayState.STOP;\n            String requested = getAnimation();')
edit(J/'net/mcreator/minepiece/procedures/MaterialBatchTechniques.java','e.setModel("entity_gomu_arm");e.setTexture("mochi");','e.setModel("b05_elastic_arm");e.setTexture("mochi");')
# Protect the exact landing and charge travel which exposed the old quarter-block rounding bug.
p=J/'net/mcreator/minepiece/command/Batch08TestCommands.java';s=p.read_text(encoding='utf-8');anchor=' check(key+"/ally",';assert anchor in s
s=s.replace(anchor,''' if(Set.of("headbutt","ulzugan","brachiobober","attackjump","ulmeteor").contains(id)){double d=caster.position().distanceTo(start);check(key+"/movement",d>(id.equals("ulzugan")?11:id.equals("ulmeteor")?11:5)&&d<(id.equals("ulzugan")?13:id.equals("ulmeteor")?13:8),"actual complete charge/landing travel="+d);}
'''+anchor,1)
anchor=' cancelCaster=FakePlayer.get(world,';extra=''' fruit(MinepieceModItems.ITEM_DEVIL_FRUIT_BATTO_BATTO);arena();var counterVictim=FakePlayer.get(world,new GameProfile(UUID.nameUUIDFromBytes("B08CounterVictim".getBytes(java.nio.charset.StandardCharsets.UTF_8)),"B08CounterVictim"));counterVictim.setHealth(20);counterVictim.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);counterVictim.getAbilities().invulnerable=false;counterVictim.teleportTo(680.5,100,83.5);ZoanCounterState.apply(caster,counterVictim);check("core/counter-applied",ZoanCounterState.active(counterVictim),"service-only control fixture; FakePlayer received-damage immunity is not a real hit test");DevilFruitPowerLogic.resetDevilFruitPowers(counterVictim);check("core/counter-victim-reset-retained",ZoanCounterState.active(counterVictim),"victim reset cannot cleanse another owner's control");ZoanCounterState.cancel(caster);check("core/counter-owner-release",!ZoanCounterState.active(counterVictim),"own owner cancellation releases");MinepieceModVariables.clear(counterVictim);counterVictim.discard();
''';s=s.replace(anchor,extra+anchor,1);p.write_text(s,encoding='utf-8')
p=P/'tools/run_local_tests.py';s=p.read_text(encoding='utf-8');a=s.index('            if args.batch08:');b=s.index('            if args.batch07:',a);part=s[a:b];anchor='                rows=re.findall';part=part.replace(anchor,"                required|={f'{f}/{m}/movement' for f,ms in catalogue.items() for m in ms if m in {'headbutt','ulzugan','brachiobober','attackjump','ulmeteor'}}\n                required|={'core/'+x for x in ['counter-applied','counter-victim-reset-retained','counter-owner-release']}\n"+anchor,1);s=s[:a]+part+s[b:];p.write_text(s,encoding='utf-8')
print('Final polish: exact movement, Makami face, Mochi pivot, native/Gecko/form stone appearance;183 B08 oracle')
