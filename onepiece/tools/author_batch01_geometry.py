from pathlib import Path
R=Path(__file__).resolve().parents[1]/'src/main/java/net/mcreator/minepiece/client/renderer'
p=R/'TechniqueVisualProfile.java';s=p.read_text(encoding='utf-8').replace('enum Kind { LEGACY,','enum Kind { B01_CHARGE, B01_WAVE, ICE_SPIKE, ICE_BALL, DARK_BALL, DARK_FIELD, SMOKE_FIST, LIGHT_PEARL, LIGHT_SWORD, SAND_SLASH, LEGACY,')
s=s.replace('      return switch (id) {','''      return switch (id) {
         case "b01_ice_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xCCAEF2FF,.35F);
         case "b01_light_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xFFFBE690,.22F);
         case "b01_smoke_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xAEE6F0F6,.4F);
         case "b01_sand_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xDBE6BF7C,.32F);
         case "b01_dark_charge" -> new TechniqueVisualProfile(Kind.B01_CHARGE,0xDD6B4487,.5F);
         case "b01_ice_wave","b01_ice_impact" -> new TechniqueVisualProfile(Kind.B01_WAVE,0xD58CDCF4,1);
         case "b01_sand_wave" -> new TechniqueVisualProfile(Kind.B01_WAVE,0xD4DFC082,1);
         case "b01_smoke_wave","b01_impact" -> new TechniqueVisualProfile(Kind.B01_WAVE,0xADD9E2E5,1);
         case "b01_ice_spike" -> new TechniqueVisualProfile(Kind.ICE_SPIKE,0xDD9DDDF0,2.5F);
         case "b01_ice_ball" -> new TechniqueVisualProfile(Kind.ICE_BALL,0xC198E8FF,1);
         case "darkball" -> new TechniqueVisualProfile(Kind.DARK_BALL,0xF1100A1A,1.1F);
         case "b01_dark_field","b01_dark_grab","b01_dark_impact" -> new TechniqueVisualProfile(Kind.DARK_FIELD,0xD8271239,3);
         case "b01_light_sword" -> new TechniqueVisualProfile(Kind.LIGHT_SWORD,0xFFFFDE75,1);
         case "b01_light_kick" -> new TechniqueVisualProfile(Kind.ARC,0xFFFFD875,.8F);
         case "b01_light_dash" -> new TechniqueVisualProfile(Kind.LIGHT,0xEFFFDC67,.08F);
         case "b01_sand_slash" -> new TechniqueVisualProfile(Kind.SAND_SLASH,0xE8E4C087,1.8F);
         case "b01_sand_dash" -> new TechniqueVisualProfile(Kind.SMOKE,0xAAD9BB8B,.25F);
         case "b01_smoke_dash" -> new TechniqueVisualProfile(Kind.SMOKE,0xAADDDEE5,.3F);
         case "whiteblow" -> new TechniqueVisualProfile(Kind.SMOKE_FIST,0xE5E6ECF0,.65F);
         case "magatama" -> new TechniqueVisualProfile(Kind.LIGHT_PEARL,0xFFFFE47A,.3F);''')
s=s.replace('case "laser1", "laser2", "magatama"','case "laser1", "laser2"').replace('case "whiteblow", "whitelauncher"','case "whitelauncher"');p.write_text(s,encoding='utf-8')
p=R/'TechniqueEffectGeometry.java';s=p.read_text(encoding='utf-8').replace('boolean flame=switch','boolean flame=profile.kind().ordinal()<=TechniqueVisualProfile.Kind.SAND_SLASH.ordinal() || switch')
s=s.replace('         switch (profile.kind()) {','''         switch (profile.kind()) {
            case B01_CHARGE -> {
               double build=smooth(age/10),r=radius*(.4+.6*build);
               for(int i=0;i<4;i++){double angle=i*Math.PI/2+age*.18;Vec3 c=u.scale(Math.cos(angle)*r).add(v.scale(Math.sin(angle)*r));sphere(out,pose,c,.04+.03*build,profile.color());}
               ring(out,pose,Vec3.ZERO,u,v,r,.016,profile.color());
            }
            case B01_WAVE -> {
               double fade=1-smooth(age/12);Vec3 x=new Vec3(1,0,0),z=new Vec3(0,0,1);
               ring(out,pose,new Vec3(0,.08,0),x,z,radius,.045,alpha(profile.color(),fade));
               for(int i=0;i<12;i++){double t=i*2.399+age*.03;Vec3 c=new Vec3(Math.cos(t)*radius,.12,Math.sin(t)*radius);tapered(out,pose,c,c.add(0,.18+.12*Math.sin(i+age*.3),0),.04,.006,alpha(profile.color(),fade));}
            }
            case ICE_SPIKE -> {
               double rise=smooth(age/6),fade=1-smooth((age-24)/8);Vec3 c=new Vec3(0,.03,0);tapered(out,pose,c,c.add(.12,radius*rise,.08),.34,.004,alpha(profile.color(),fade));
               tapered(out,pose,c.add(.3,0,.1),c.add(.15,radius*.65*rise,.18),.18,.002,alpha(0xFFA7EEFF,fade));
               tapered(out,pose,c.add(-.25,0,-.1),c.add(-.4,radius*.45*rise,-.2),.18,.002,alpha(0xE576B6DB,fade));
            }
            case ICE_BALL -> {sphere(out,pose,Vec3.ZERO,radius*(.4+.6*smooth(age/5)),profile.color());for(int i=0;i<6;i++){double t=i*Math.PI/3+age*.1;Vec3 c=u.scale(Math.cos(t)*radius).add(v.scale(Math.sin(t)*radius));tapered(out,pose,c,c.scale(1.25),.05,.001,0xE5E8FAFF);}}
            case DARK_BALL -> {sphere(out,pose,Vec3.ZERO,radius,profile.color());for(int i=0;i<3;i++)ring(out,pose,Vec3.ZERO,u,v,radius*(1.03+i*.06),.02,alpha(0xCE9976C4,.6+.3*Math.sin(age*.3+i)));}
            case DARK_FIELD -> {Vec3 x=new Vec3(1,0,0),z=new Vec3(0,0,1);for(int i=0;i<5;i++){double r=radius*(.2+i*.17);ring(out,pose,new Vec3(0,.025+i*.008,0),x,z,r,.06,profile.color());for(int j=0;j<3;j++){double angle=j*Math.PI*2/3+age*.06+i*.4;Vec3 c=new Vec3(Math.cos(angle)*r,.03,Math.sin(angle)*r);sphere(out,pose,c,.1,0xC98765AD);}}}
            case SMOKE_FIST -> {Vec3 head=direction.scale(.4);sphere(out,pose,head,radius,profile.color());for(int i=0;i<4;i++)sphere(out,pose,head.add(direction.scale(.45)).add(u.scale((i-1.5)*radius*.44)),radius*.3,0xF4F1F5F8);tube(out,pose,direction.scale(-2),head,u,v,radius*.45,0xA6C7D1DB);for(int i=0;i<3;i++)ring(out,pose,direction.scale(-i*.5),u,v,radius*(.8-i*.14),.045,0x9AE3ECF5);}
            case LIGHT_PEARL -> {sphere(out,pose,Vec3.ZERO,radius,profile.color());ring(out,pose,Vec3.ZERO,u,v,radius*1.45,.025,0xD8FFF4C1);tube(out,pose,direction.scale(-1),Vec3.ZERO,u,v,.045,0xA8F5D67A);}
            case LIGHT_SWORD -> {tapered(out,pose,new Vec3(0,-.1,0),new Vec3(0,1.7,0),.07,.008,profile.color());tube(out,pose,new Vec3(-.25,0,0),new Vec3(.25,0,0),new Vec3(0,1,0),new Vec3(0,0,1),.035,0xFFFFF0C1);}
            case SAND_SLASH -> {Vec3 previous=null;for(int i=0;i<=16;i++){double t=-Math.PI*.7+i*Math.PI*1.4/16;Vec3 c=u.scale(Math.cos(t)*radius).add(v.scale(Math.sin(t)*radius));if(previous!=null)tapered(out,pose,previous,c,.06,.035,profile.color());previous=c;}}
''');p.write_text(s,encoding='utf-8')
p=R/'EntityGomuArmRenderer.java';s=p.read_text(encoding='utf-8').replace('if ("whitelauncher".equals(id) ||','if (id.startsWith("b01_") || "whitelauncher".equals(id) ||').replace('         if(id.equals("kaenryuo"))','         if(id.startsWith("b01_") && !id.endsWith("charge") && !id.endsWith("dash"))profile=new TechniqueVisualProfile(profile.kind(),profile.color(),stretch);\n         if(id.equals("kaenryuo"))').replace('return (net.mcreator','return (entity.getVisualProfile().startsWith("b01_") || net.mcreator');p.write_text(s,encoding='utf-8')
print('B01 procedural silhouettes and native visual adapters written')
