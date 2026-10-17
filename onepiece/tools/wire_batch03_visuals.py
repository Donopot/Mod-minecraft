from pathlib import Path
P=Path(__file__).resolve().parents[1];J=P/'src/main/java'
def edit(path,old,new):
 p=J/path;s=p.read_text(encoding='utf-8');assert old in s,(path,old);p.write_text(s.replace(old,new),encoding='utf-8')
B='net/mcreator/minepiece/'
edit(B+'client/renderer/TechniqueVisualProfile.java','public enum Kind {','public enum Kind { B03_ROOT,B03_CAGE,B03_DONUT,B03_SPIKED_FIST,B03_CRYSTAL,B03_FAN,B03_CROSS,B03_PUNCH,')
profiles='''
         case "b03_mochi_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xF4FFF1D8,.2F);
         case "b03_mori_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xED75AC4C,.2F);
         case "b03_kira_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xEC99E9F9,.2F);
         case "b03_supa_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xFFE5EDF5,.2F);
         case "b03_riki_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xFFDBAC88,.2F);
         case "b03_mori_root","b03_mori_rootgiant","b03_mori_root_branch" -> new TechniqueVisualProfile(Kind.B03_ROOT,0xFF76502D,id.equals("b03_mori_rootgiant")?1.7F:.8F);
         case "b03_mori_cage","b03_mori_assault","b03_mori_impact" -> new TechniqueVisualProfile(Kind.B03_CAGE,0xFF805634,1.5F);
         case "b03_mochi_donut" -> new TechniqueVisualProfile(Kind.B03_DONUT,0xFFFFEBD7,1);
         case "b03_mochi_zangirimochi","b03_mochi_impact" -> new TechniqueVisualProfile(Kind.B03_SPIKED_FIST,0xFF292126,.7F);
         case "b03_mochi_yakimochi" -> new TechniqueVisualProfile(Kind.FIRE_FIST,0xFF5B2C24,.55F);
         case "b03_mochi_build" -> new TechniqueVisualProfile(Kind.B03_DONUT,0xFFEED6A8,.7F);
         case "b03_kira_diamondshard","b03_kira_diamondavalanche","b03_kira_impact","b03_kira_body","b03_kira_brilliantpunk","b03_kira_attackoverhead" -> new TechniqueVisualProfile(Kind.B03_CRYSTAL,0xF598E8FF,id.equals("b03_kira_diamondavalanche")?1.4F:.45F);
         case "b03_supa_sparklingdaisy" -> new TechniqueVisualProfile(Kind.B03_FAN,0xFFF4F8FF,1.6F);
         case "b03_supa_sparbreak","b03_supa_atomicspurt" -> new TechniqueVisualProfile(Kind.B03_CROSS,0xFFE7F4FF,1.5F);
         case "b03_supa_impact","b03_riki_sweep" -> new TechniqueVisualProfile(Kind.B02_SWEEP,0xF3E2EFFA,1.2F);
         case "b03_riki_attackoverhead","b03_riki_hadoelbow","b03_riki_galleonlariat","b03_riki_impact" -> new TechniqueVisualProfile(Kind.B03_PUNCH,0xFFE0A679,1);
         case "b03_mochi_dash","b03_kira_dash","b03_supa_dash","b03_riki_dash" -> new TechniqueVisualProfile(Kind.B01_BEAM,0xD7E5EAF0,1.5F);
'''
edit(B+'client/renderer/TechniqueVisualProfile.java','      return switch (id) {','      return switch (id) {'+profiles)
geometry='''
            case B03_ROOT -> {
               for(int strand=0;strand<4;strand++){Vec3 last=direction.scale(-1.8);double phase=strand*Math.PI/2;
                  for(int step=1;step<=8;step++){double d=-1.8+step*.52;double offset=.08+strand*.035;Vec3 next=direction.scale(d).add(u.scale(Math.cos(step*.7+phase)*offset)).add(v.scale(Math.sin(step*.6+phase)*offset));tapered(out,pose,last,next,Math.max(.012,.09-step*.007),Math.max(.004,.075-step*.008),strand%2==0?0xFF6F4828:0xFF9D7140);last=next;}
                  tapered(out,pose,last,last.add(direction.scale(.5)),.1,.001,0xFF7B542B);
               }
               tapered(out,pose,direction.scale(-.5),direction.scale(-.2).add(v.scale(.28)),.05,.001,0xFF77AC43);
            }
            case B03_CAGE -> {
               double grow=smooth(age/7);for(int i=0;i<5;i++){double angle=i*2.399;Vec3 base=new Vec3(Math.cos(angle)*.3,0,Math.sin(angle)*.3);Vec3 mid=base.add(Math.cos(angle)*.2,radius*grow*.65,Math.sin(angle)*.2);Vec3 top=base.add(-Math.cos(angle)*.25,radius*grow,-Math.sin(angle)*.25);tapered(out,pose,base,mid,.11,.075,0xFF76512D);tapered(out,pose,mid,top,.075,.001,0xFF9A743E);tapered(out,pose,mid,mid.add(Math.cos(angle)*.4,.18,Math.sin(angle)*.4),.09,.001,0xFF78AB43);}
            }
            case B03_DONUT -> {
               Vec3[] corners={u.scale(-.65).add(v.scale(-.65)),u.scale(.65).add(v.scale(-.65)),u.scale(.65).add(v.scale(.65)),u.scale(-.65).add(v.scale(.65))};for(int i=0;i<4;i++)tube(out,pose,corners[i],corners[(i+1)%4],direction,u,.17,profile.color());
            }
            case B03_SPIKED_FIST -> {
               sphere(out,pose,Vec3.ZERO,radius,profile.color());tube(out,pose,direction.scale(-1.2),Vec3.ZERO,u,v,radius*.55,0xFFF1E4CE);for(int i=0;i<9;i++){double a=i*2.399;Vec3 end=u.scale(Math.cos(a)*radius).add(v.scale(Math.sin(a)*radius));tapered(out,pose,end.scale(.65),end.scale(1.4),.08,.001,0xFF2A2026);}
            }
            case B03_CRYSTAL -> {
               tapered(out,pose,direction.scale(-radius),Vec3.ZERO,.001,radius*.45,0xFF5BAECF);tapered(out,pose,Vec3.ZERO,direction.scale(radius*1.6),radius*.45,.001,profile.color());for(int i=0;i<3;i++){Vec3 offset=u.scale(Math.cos(i*2.1)*radius*.55).add(v.scale(Math.sin(i*2.1)*radius*.55));tapered(out,pose,offset.subtract(direction.scale(.2)),offset.add(direction.scale(.35)),.06,.001,0xFFD7F8FF);}
            }
            case B03_FAN -> {
               for(int blade=0;blade<5;blade++){double a=(blade-2)*.24;Vec3 end=direction.scale(Math.cos(a)*radius).add(u.scale(Math.sin(a)*radius));tapered(out,pose,u.scale((blade-2)*.08),end,.05,.003,profile.color());}ring(out,pose,direction.scale(.2),u,v,.22,.016,0xFFE2EBF5);
            }
            case B03_CROSS -> {for(int side:new int[]{-1,1}){Vec3 last=null;for(int i=0;i<=12;i++){double t=i/12.0;Vec3 next=u.scale(side*(.9-t*1.8)).add(v.scale(.8-t*1.6)).add(direction.scale(Math.sin(t*Math.PI)*radius));if(last!=null)tapered(out,pose,last,next,.035,.014,profile.color());last=next;}}}
            case B03_PUNCH -> {sphere(out,pose,direction.scale(.3),.42,profile.color());for(int i=0;i<4;i++)sphere(out,pose,direction.scale(.6).add(u.scale((i-1.5)*.18)),.14,0xFFF1C197);ring(out,pose,direction.scale(.8),u,v,radius*(.2+smooth(age/12)),.035,0xC7F4E4C7);}
'''
edit(B+'client/renderer/TechniqueEffectGeometry.java','            case B02_GRAVITY ->',geometry+'\n            case B02_GRAVITY ->')
# Native arm and rock profiles keep Gecko meshes. Only the reviewed geometry overrides are procedural.
edit(B+'client/renderer/EntityGomuArmRenderer.java','if (id.startsWith("b02_")','if (id.startsWith("b03_") && TechniqueVisualProfile.forTechnique(id).replacesLegacyModel() || id.startsWith("b02_")')
edit(B+'client/renderer/EntityGomuArmRenderer.java','      net.mcreator.minepiece.client.Batch02VisualLab.rendered(id);','      net.mcreator.minepiece.client.Batch02VisualLab.rendered(id);\n      net.mcreator.minepiece.client.Batch03VisualLab.rendered(id);')
edit(B+'client/renderer/EntityGomuArmRenderer.java','         if(id.startsWith("b02_") && !id.endsWith("charge"))','         if(id.startsWith("b03_") && !id.endsWith("charge"))profile=new TechniqueVisualProfile(profile.kind(),profile.color(),stretch);\n         if(id.startsWith("b02_") && !id.endsWith("charge"))')
edit(B+'client/renderer/EntityGomuArmRenderer.java','return (entity.getVisualProfile().startsWith("b02_")','return (entity.getVisualProfile().startsWith("b03_") || entity.getVisualProfile().startsWith("b02_")')
edit(B+'client/renderer/SkillProjectileRenderer.java','      net.mcreator.minepiece.client.Batch02VisualLab.rendered','      net.mcreator.minepiece.client.Batch03VisualLab.rendered(geo.getOrDefaultGeckolibData(PROFILE,""));\n      net.mcreator.minepiece.client.Batch02VisualLab.rendered')
edit(B+'client/renderer/SkillProjectileRenderer.java','case "b02_rubble"->3;','case "b03_riki_stonethrowinglarge","b03_kira_diamondavalanche"->3;case "b03_riki_stonethrowing","b03_mochi_ringfist"->1.8;case "b02_rubble"->3;')
edit(B+'client/DevilFruitAnimationLayer.java','(payload.animation().startsWith("b02_")','(payload.animation().startsWith("b03_") || payload.animation().startsWith("b02_")')
edit(B+'client/DevilFruitAnimationLayer.java','check.animation.startsWith("b02_")?"batch02"','check.animation.startsWith("b03_")?"batch03":check.animation.startsWith("b02_")?"batch02"')
print('B03 geometry, body overlays and native arm/rock render paths wired')
