from pathlib import Path
P=Path(__file__).resolve().parents[1];J=P/'src/main/java';B='net/mcreator/minepiece/'
p=J/(B+'command/RepairTestCommands.java');s=p.read_text(encoding='utf-8').replace('"batch01","batch02"','"batch01","batch02","batch03"')
s=s.replace('"suna_suna","yami_yami","gura_gura"','"suna_suna","yami_yami","mochi_mochi","mori_mori","kira_kira","supa_supa","riki_riki","gura_gura"')
needle='         if("batch02".equals(System.getProperty("opworldlike.visualScenario"))){'
pos=s.index(needle);s=s[:pos]+'''         if("batch03".equals(System.getProperty("opworldlike.visualScenario"))){
            targetZ=java.util.Set.of("yakimochi","root","rootgiant","diamondshard","diamondavalanche","projectileslash","stonethrowing","stonethrowinglarge").contains(technique)?26:technique.equals("mochibuild")?16:12;
            player.getInventory().clearContent();
            for(var witness:source.getServer().getPlayerList().getPlayers())if(witness.getName().getString().equals("FruitWitness"))witness.connection.teleport(27,108,20,90,18);
         }
'''+s[pos:];p.write_text(s,encoding='utf-8')
p=J/(B+'procedures/MochiBuild.java');s=p.read_text(encoding='utf-8');s=s.replace('               int avant = PowerBlockService.activeCount();','               if(!level.getEntitiesOfClass(net.minecraft.world.entity.LivingEntity.class,new net.minecraft.world.phys.AABB(p),e->e.isAlive() && !(e instanceof net.mcreator.minepiece.entity.EntityGomuArmEntity)).isEmpty())continue;\n               int avant = PowerBlockService.activeCount();');p.write_text(s,encoding='utf-8')
p=J/(B+'client/renderer/TechniqueEffectGeometry.java');s=p.read_text(encoding='utf-8');start=s.index('            case B03_ROOT ->');end=s.index('            case B03_CAGE ->',start);part=s[start:end];part=part.replace('for(int strand=0;','Vec3 rootAxis=direction.scale(radius),rootU=u.scale(radius),rootV=v.scale(radius);\n               for(int strand=0;').replace('direction.scale(-1.8)','rootAxis.scale(-1.8)').replace('direction.scale(d)','rootAxis.scale(d)').replace('u.scale(Math.cos(step','rootU.scale(Math.cos(step').replace('v.scale(Math.sin(step','rootV.scale(Math.sin(step').replace('direction.scale(.5)','rootAxis.scale(.5)').replace('direction.scale(-.5)','rootAxis.scale(-.5)').replace('direction.scale(-.2)','rootAxis.scale(-.2)').replace('v.scale(.28)','rootV.scale(.28)');s=s[:start]+part+s[end:];p.write_text(s,encoding='utf-8')
print('B03 private scene, occupancy protection and root size differentiation wired')
