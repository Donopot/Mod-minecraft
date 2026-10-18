package net.mcreator.minepiece.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.world.phys.Vec3;

/** Bounded emissive geometry, without an opaque substitute for the intentionally clear texture. */
public final class TechniqueEffectGeometry {
   private TechniqueEffectGeometry() {}
   public static void submit(TechniqueVisualProfile profile, Vec3 velocity, float age,
                             PoseStack stack, SubmitNodeCollector collector) {
      Vec3 direction = velocity.lengthSqr() > .00001 ? velocity.normalize() : new Vec3(0, 0, 1);
      Vec3 u = direction.cross(Math.abs(direction.y) > .95 ? new Vec3(1, 0, 0) : new Vec3(0, 1, 0)).normalize();
      Vec3 v = direction.cross(u).normalize();
      double radius = profile.radius();
      boolean flame=profile.kind().ordinal()<=TechniqueVisualProfile.Kind.SAND_SLASH.ordinal() || switch(profile.kind()) {case ELECTRIC_ARC,ELECTRIC_CHARGE,ELECTRIC_WARNING,ELECTRIC_IMPACT,ELECTRIC_CLOUD,ELECTRIC_BALL,MAGMA_FIST_CHARGE,MAGMA_HOUND_CHARGE,MAGMA_WAKE,MAGMA_VOLCANO,MAGMA_SKY,MAGMA_ERUPTION,MAGMA_TRAIL,FIRE_FIST,FIRE_BULLET,FIREFLIES,FIRE_PILLAR,FIRE_BALL,FIRE_DRAGON,FLAME_TRAIL->true;default->false;};
      collector.submitCustomGeometry(stack, flame?RenderTypes.debugQuads():RenderTypes.lightning(), (pose, out) -> {
         switch (profile.kind()) {
            case B07_RUSH -> {for(int i=0;i<3;i++){double a=i*Math.PI*2/3;Vec3 offset=u.scale(Math.cos(a)*.35).add(v.scale(Math.sin(a)*.35));tapered(out,pose,offset.add(direction.scale(-.8)),offset.add(direction.scale(.6)),.025,.003,alpha(profile.color(),.65));}ring(out,pose,direction.scale(.5),u,v,.5,.025,alpha(profile.color(),.55));}
            case B07_FLAME_RAY -> {Vec3 from=direction.scale(-1),to=direction.scale(1);tube(out,pose,from,to,u,v,radius*.7,0x55FF661A);tube(out,pose,from,to,u,v,radius*.18,0xFFFFD267);for(int i=0;i<5;i++){double a=i*1.256+age*.18;Vec3 offset=u.scale(Math.cos(a)*radius*.4).add(v.scale(Math.sin(a)*radius*.4));tapered(out,pose,from.add(offset),to.add(offset.scale(1.4)),.05,.002,0xE6FF7C24);}}

            case B08_MISSILE -> {tapered(out,pose,direction.scale(-radius),direction.scale(radius),radius*.25,radius*.12,0xFFD1D5DD);tapered(out,pose,direction.scale(radius),direction.scale(radius*1.55),radius*.12,.001,0xFFEB4A43);for(int side:new int[]{-1,1}){tapered(out,pose,direction.scale(-radius*.6),direction.scale(-radius).add(u.scale(side*radius*.65)),.05,.01,0xFF665A68);tapered(out,pose,direction.scale(-radius*.6),direction.scale(-radius).add(v.scale(side*radius*.65)),.05,.01,0xFF665A68);}tapered(out,pose,direction.scale(-radius),direction.scale(-radius*(1.7+.2*Math.sin(age))),.09,.001,0xFFFFAD3B);}
            case B08_HEAD -> {sphere(out,pose,Vec3.ZERO,radius*.45,profile.color());sphere(out,pose,direction.scale(radius*.3),radius*.3,0xFFF0DDEB);for(int side:new int[]{-1,1})tapered(out,pose,u.scale(side*radius*.35),u.scale(side*radius*.7).add(v.scale(radius*.55)),.08,.002,0xFFF7EEE3);}
            case B08_COIL -> {Vec3 last=null;for(int i=0;i<=40;i++){double t=i/40.,a=t*Math.PI*8+age*.05;Vec3 at=u.scale(Math.cos(a)*radius*.4).add(v.scale(Math.sin(a)*radius*.4)).add(direction.scale(t*radius*2));if(last!=null)tapered(out,pose,last,at,.055,.04,profile.color());last=at;}}

            case B07_FROST -> {for(int i=0;i<6;i++){double a=i*Math.PI/3+age*.08;Vec3 spoke=u.scale(Math.cos(a)*radius).add(v.scale(Math.sin(a)*radius));tapered(out,pose,Vec3.ZERO,spoke,.025,.005,profile.color());for(int side:new int[]{-1,1})tapered(out,pose,spoke.scale(.55),spoke.scale(.75).add(direction.scale(side*radius*.3)),.015,.002,profile.color());}}
            case B07_TORNADO -> {Vec3 last=null;for(int i=0;i<=48;i++){double y=i/48.*radius*2.5,a=i*.4+age*.16,r=radius*(.2+i/60.);Vec3 at=new Vec3(Math.cos(a)*r,y-radius,Math.sin(a)*r);if(last!=null)tapered(out,pose,last,at,.08,.035,profile.color());last=at;}}
            case B06_WOLF -> {double r=radius;sphere(out,pose,Vec3.ZERO,r*.25,profile.color());sphere(out,pose,direction.scale(r*.5).add(v.scale(r*.18)),r*.22,profile.color());for(int side:new int[]{-1,1}){tapered(out,pose,direction.scale(r*.4).add(v.scale(r*.25)).add(u.scale(side*r*.12)),direction.scale(r*.5).add(v.scale(r*.65)).add(u.scale(side*r*.22)),.12,.001,profile.color());tapered(out,pose,u.scale(side*r*.2),u.scale(side*r*.6).add(direction.scale(r*.7)),.09,.001,profile.color());}tapered(out,pose,direction.scale(-r*.25),direction.scale(-r).add(v.scale(r*.3)),.12,.001,profile.color());}
            case B06_BIRD -> {for(int side:new int[]{-1,1}){Vec3 last=Vec3.ZERO;for(int i=1;i<=7;i++){Vec3 next=u.scale(side*radius*i/7).add(direction.scale(-radius*i*i/49));tapered(out,pose,last,next,.08,.01,profile.color());last=next;}}tapered(out,pose,direction.scale(-radius*.3),direction.scale(radius*.6),.12,.001,profile.color());}
            case B06_NET -> {for(int i=-2;i<=2;i++){double a=i*radius/2;tapered(out,pose,u.scale(-radius).add(v.scale(a)),u.scale(radius).add(v.scale(a)),.018,.018,profile.color());tapered(out,pose,v.scale(-radius).add(u.scale(a)),v.scale(radius).add(u.scale(a)),.018,.018,profile.color());}}
            case B04_ARROW -> {double size=radius;Vec3 tail=direction.scale(-size),tip=direction.scale(size);tapered(out,pose,tail,tip,.045,.04,profile.color());for(int side:new int[]{-1,1})tapered(out,pose,tip,tip.subtract(direction.scale(size*.55)).add(u.scale(side*size*.35)),.05,.012,profile.color());}
            case B04_KICK -> {double size=radius,turn=age*.09;Vec3 last=null;for(int i=0;i<=24;i++){double theta=-1.3+i*2.6/24+turn;Vec3 c=u.scale(Math.cos(theta)*size).add(v.scale(Math.sin(theta)*size*.65));if(last!=null)tapered(out,pose,last,c,.04,.018,alpha(profile.color(),1-smooth(age/24)));last=c;}for(int side:new int[]{-1,1})sphere(out,pose,u.scale(side*size*.7),.09,profile.color());}
            case B04_PRETZEL -> {Vec3 end=direction.scale(radius*2);tapered(out,pose,direction.scale(-.5),end,.08,.015,profile.color());for(int i=0;i<3;i++)ring(out,pose,direction.scale(i*.55),u,v,.18,.04,0xFFAE7B42);}
            case B04_WEIGHT -> {Vec3 x=new Vec3(1,0,0),z=new Vec3(0,0,1);ring(out,pose,Vec3.ZERO,x,z,radius*(.3+.7*smooth(age/20)),.035,alpha(profile.color(),1-smooth(age/24)));for(int i=0;i<5;i++){double a=i*1.256;Vec3 c=new Vec3(Math.cos(a)*radius*.6,.9-(age*.04%1),Math.sin(a)*radius*.6);tapered(out,pose,c,c.add(0,-.4,0),.035,.002,profile.color());}}
            case B04_BISCUIT -> {sphere(out,pose,Vec3.ZERO,radius,profile.color());for(int i=0;i<7;i++){double a=i*2.399;Vec3 c=u.scale(Math.cos(a)*radius*.7).add(v.scale(Math.sin(a)*radius*.7)).add(direction.scale(radius*.65));sphere(out,pose,c,.035,0xFF784D2C);}}
            case B02_HORIZONTAL -> {double advance=age*.18;for(int i=0;i<3;i++)ring(out,pose,direction.scale(advance+i*.4),u,v,Math.abs(radius)*(.3+i*.2),.03,alpha(profile.color(),1-smooth(age/16)));}
            case B02_CRACK -> {
               double fade=1-smooth((age-8)/12),size=Math.abs(radius);
               for(int branch=0;branch<6;branch++){double theta=branch*Math.PI/3;Vec3 previous=Vec3.ZERO;
                  for(int j=1;j<=4;j++){double step=j/4.;Vec3 next=u.scale(Math.cos(theta)*size*step).add(v.scale(Math.sin(theta)*size*step));next=next.add(u.scale((j%2==0?.12:-.12)*size));tapered(out,pose,previous,next,.022*fade,.008*fade,alpha(profile.color(),fade));if(j==2)tapered(out,pose,next,next.add(v.scale(.24*size)),.012,.002,alpha(profile.color(),fade));previous=next;}}
            }
            case B02_SWEEP -> {Vec3 previous=null;for(int i=0;i<=20;i++){double t=-1.4+i*2.8/20+age*.025;Vec3 c=u.scale(Math.cos(t)*Math.abs(radius)).add(v.scale(Math.sin(t)*Math.abs(radius)*.6));if(previous!=null)tapered(out,pose,previous,c,.035,.01,alpha(profile.color(),1-smooth(age/16)));previous=c;}}
            case B02_TILT -> {Vec3 x=new Vec3(1,profile.color()==0xD6D8F6FF?.3:-.3,0).normalize(),z=new Vec3(0,0,1);ring(out,pose,Vec3.ZERO,x,z,Math.abs(radius),.03,profile.color());for(int i=0;i<6;i++){double a=i*Math.PI/3;Vec3 c=x.scale(Math.cos(a)*Math.abs(radius));tapered(out,pose,c,c.add(z.scale(Math.sin(a)*Math.abs(radius))),.02,.003,profile.color());}}
            case B02_BOMB -> {double f=smooth(age/16),size=Math.abs(radius)*(.15+.85*f);Vec3 x=new Vec3(1,0,0),z=new Vec3(0,0,1);ring(out,pose,Vec3.ZERO,x,z,size,.065,alpha(profile.color(),1-f));ring(out,pose,Vec3.ZERO,u,v,size*.8,.045,alpha(0xDFFFF1C2,1-f));for(int i=0;i<6;i++){double a=i*2.399;Vec3 c=new Vec3(Math.cos(a)*size*.5,Math.sin(i)*size*.3,Math.sin(a)*size*.5);sphere(out,pose,c,size*.12,alpha(0x94746C65,1-f));}}
            case B02_PAW -> {if(radius>2){Vec3 x=new Vec3(1,0,0),z=new Vec3(0,0,1);ring(out,pose,Vec3.ZERO,x,z,radius*(.3+.7*smooth(age/16)),.05,alpha(profile.color(),1-smooth(age/16)));}double size=Math.min(2,Math.abs(radius));Vec3 c=v.scale(-size*.15);sphere(out,pose,c,size*.35,profile.color());for(int i=0;i<4;i++){double a=Math.PI*.13+i*Math.PI*.24;Vec3 toe=u.scale(Math.cos(a)*size*.65).add(v.scale(Math.sin(a)*size*.7));sphere(out,pose,toe,size*.14,profile.color());}ring(out,pose,Vec3.ZERO,u,v,size,.018,alpha(profile.color(),.55));}

            case B05_BAT -> {
               sphere(out,pose,Vec3.ZERO,radius*.18,profile.color());double flap=Math.sin(age*1.1)*.25;
               for(int side:new int[]{-1,1}){Vec3 root=u.scale(side*radius*.12),tip=u.scale(side*radius).add(v.scale(flap));tapered(out,pose,root,tip,.12,.012,profile.color());for(int finger=0;finger<3;finger++){Vec3 corner=u.scale(side*radius*(.5+finger*.2)).add(v.scale(-radius*(.2+finger*.15)));tapered(out,pose,tip,corner,.07,.008,profile.color());}}
               for(int side:new int[]{-1,1})tapered(out,pose,v.scale(.1),v.scale(.3).add(u.scale(side*.1)),.055,.001,profile.color());
            }
            case B05_LANCE -> {tapered(out,pose,direction.scale(-radius),direction.scale(radius*1.8),radius*.35,.001,profile.color());for(int side:new int[]{-1,1})tapered(out,pose,direction.scale(-.2),u.scale(side*.5).add(direction.scale(-.6)),.12,.001,profile.color());}
            case B05_SCORE -> {double r=Math.abs(radius);for(int i=0;i<5;i++){double a=i*1.257+age*.05;Vec3 c=u.scale(Math.cos(a)*r).add(v.scale(Math.sin(a)*r));sphere(out,pose,c,.075,profile.color());tapered(out,pose,c,c.add(v.scale(.3)),.025,.025,profile.color());}ring(out,pose,Vec3.ZERO,u,v,r,.012,alpha(profile.color(),.6));}
            case B03_ROOT -> {
               Vec3 rootAxis=direction.scale(radius),rootU=u.scale(radius),rootV=v.scale(radius);
               for(int strand=0;strand<4;strand++){Vec3 last=rootAxis.scale(-1.8);double phase=strand*Math.PI/2;
                  for(int step=1;step<=8;step++){double d=-1.8+step*.52;double offset=.08+strand*.035;Vec3 next=rootAxis.scale(d).add(rootU.scale(Math.cos(step*.7+phase)*offset)).add(rootV.scale(Math.sin(step*.6+phase)*offset));tapered(out,pose,last,next,Math.max(.012,.09-step*.007),Math.max(.004,.075-step*.008),strand%2==0?0xFF6F4828:0xFF9D7140);last=next;}
                  tapered(out,pose,last,last.add(rootAxis.scale(.5)),.1,.001,0xFF7B542B);
               }
               tapered(out,pose,rootAxis.scale(-.5),rootAxis.scale(-.2).add(rootV.scale(.28)),.05,.001,0xFF77AC43);
            }
            case B03_CAGE -> {
               double grow=smooth(age/7);for(int i=0;i<5;i++){double angle=i*2.399;Vec3 base=new Vec3(Math.cos(angle)*.3,0,Math.sin(angle)*.3);Vec3 mid=base.add(Math.cos(angle)*.2,radius*grow*.65,Math.sin(angle)*.2);Vec3 top=base.add(-Math.cos(angle)*.25,radius*grow,-Math.sin(angle)*.25);tapered(out,pose,base,mid,.11,.075,0xFF76512D);tapered(out,pose,mid,top,.075,.001,0xFF9A743E);tapered(out,pose,mid,mid.add(Math.cos(angle)*.4,.18,Math.sin(angle)*.4),.09,.001,0xFF78AB43);}
            }
            case B03_DONUT -> {
               Vec3[] corners={u.scale(-.65).add(v.scale(-.65)),u.scale(.65).add(v.scale(-.65)),u.scale(.65).add(v.scale(.65)),u.scale(-.65).add(v.scale(.65))};for(int i=0;i<4;i++)tapered(out,pose,corners[i],corners[(i+1)%4],.17,.17,profile.color());
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

            case B02_GRAVITY -> {Vec3 x=new Vec3(1,0,0),z=new Vec3(0,0,1);double size=Math.abs(radius);for(int i=0;i<3;i++){double y=(i*.5+age*.035)%1.7;ring(out,pose,new Vec3(0,y,0),x,z,size*(.3+i*.23),.025,profile.color());for(int side:new int[]{-1,1}){Vec3 c=new Vec3(side*size*.45,y,0);tapered(out,pose,c,c.add(0,-.35,0),.035,.002,profile.color());}}}
            case B02_ROOM -> {
               Vec3 x=new Vec3(1,0,0),z=new Vec3(0,0,1),y=new Vec3(0,1,0);double size=Math.abs(radius);
               ring(out,pose,Vec3.ZERO,x,z,size,.045,0xD579E6FF);
               for(int axis=0;axis<8;axis++){double theta=axis*Math.PI/8;Vec3 horizontal=x.scale(Math.cos(theta)).add(z.scale(Math.sin(theta)));Vec3 previous=null;
                  for(int j=0;j<=32;j++){double a=j*Math.PI/32;Vec3 c=horizontal.scale(Math.cos(a)*size).add(y.scale(Math.sin(a)*size));if(previous!=null)tapered(out,pose,previous,c,.025,.025,profile.color());previous=c;}}
               // Moving latitude makes the dome readable from inside; the outer boundary keeps its actual radius.
               double latitude=(age%60)/60*Math.PI/2;ring(out,pose,y.scale(Math.sin(latitude)*size),x,z,Math.cos(latitude)*size,.03,0x9064DFFF);
            }
            case B02_ORBIT -> {for(int i=0;i<3;i++){double a=i*Math.PI*2/3+age*.12;Vec3 c=u.scale(Math.cos(a)*Math.abs(radius)).add(v.scale(Math.sin(a)*Math.abs(radius)));sphere(out,pose,c,.05,profile.color());}ring(out,pose,Vec3.ZERO,u,v,Math.abs(radius),.01,alpha(profile.color(),.5));}
            case B02_METEOR -> {sphere(out,pose,Vec3.ZERO,radius,profile.color());for(int i=0;i<5;i++){double a=i*2.399+age*.05;Vec3 c=u.scale(Math.cos(a)*radius*.85).add(v.scale(Math.sin(a)*radius*.85));sphere(out,pose,c,radius*.23,i%2==0?0xFF8E7A63:0xFF4F423A);tapered(out,pose,c,c.subtract(direction.scale(1.3)),.08,.005,0xDEFFB552);}}

            case B01_KICK -> {
               double fade=1-smooth(age/12);Vec3 previous=null;
               for(int i=0;i<=12;i++){double t=-1.0+i*2.0/12;Vec3 c=u.scale(Math.cos(t)*.45).add(v.scale(Math.sin(t)*.45));if(previous!=null)tapered(out,pose,previous,c,.022,.009,alpha(profile.color(),fade));previous=c;}
            }
            case B01_BEAM -> {tapered(out,pose,Vec3.ZERO,direction.scale(radius),.035,.02,alpha(profile.color(),1-smooth(age/10)));}
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

            case ELECTRIC_ARC -> electricArc(out,pose,Vec3.ZERO,direction.scale(radius),age,.045);
            case ELECTRIC_CHARGE -> {
               for(int side:new int[]{-1,1}) {
                  Vec3 hand=u.scale(side*.45).add(direction.scale(.45));
                  electricArc(out,pose,hand.subtract(v.scale(.18)),hand.add(v.scale(.18)),age+side*2,.025);
                  ring(out,pose,hand,u,v,.17+.025*Math.sin(age*.4),.015,0xCF68CFFF);
               }
            }
            case ELECTRIC_WARNING -> {
               Vec3 x=new Vec3(1,0,0),z=new Vec3(0,0,1),c=new Vec3(0,.035,0);
               double pulse=.5+.5*Math.sin(age*.8);
               ring(out,pose,c,x,z,radius,.025+pulse*.015,0xE954C9FF);
               tube(out,pose,c.add(-.35,0,0),c.add(.35,0,0),z,new Vec3(0,1,0),.018,0xFFB5EEFF);
               tube(out,pose,c.add(0,0,-.35),c.add(0,0,.35),x,new Vec3(0,1,0),.018,0xFFB5EEFF);
            }
            case ELECTRIC_IMPACT -> {
               double f=smooth(age/12),fade=1-f;
               ring(out,pose,new Vec3(0,.07,0),new Vec3(1,0,0),new Vec3(0,0,1),radius*(.2+.8*f),.035,alpha(0xEF6EDBFF,fade));
               for(int i=0;i<7;i++) {
                  double t=i*Math.PI*2/7;
                  Vec3 a=new Vec3(Math.cos(t)*radius*.2,.1,Math.sin(t)*radius*.2);
                  Vec3 b=new Vec3(Math.cos(t)*radius*(.6+.35*f),.1+Math.sin(Math.PI*f)*.6,Math.sin(t)*radius*(.6+.35*f));
                  electricArc(out,pose,a,b,age+i,.025*fade);
               }
            }
            case ELECTRIC_CLOUD -> {
               for(int i=0;i<5;i++) {
                  double t=i*2.39996;
                  Vec3 c=new Vec3(Math.cos(t)*1.6,Math.sin(age*.06+i)*.12,Math.sin(t)*1.1);
                  sphere(out,pose,c,.9,i%2==0?0xA51B263B:0xA5334260);
                  if(i%2==0)electricArc(out,pose,c,c.add(.4,-.4,.25),age+i,.025);
               }
            }
            case ELECTRIC_BALL -> {
               // Electric sphere replaces the source meteor mesh, without an opaque full-screen flash.
               sphere(out,pose,Vec3.ZERO,radius,0xC52C5B82);
               for(int i=0;i<5;i++) {
                  double t=i*Math.PI*2/5+age*.025;
                  Vec3 start=new Vec3(Math.cos(t)*radius,.2*radius,Math.sin(t)*radius);
                  Vec3 end=new Vec3(Math.cos(t+1.1)*radius,-.35*radius,Math.sin(t+1.1)*radius);
                  electricArc(out,pose,start,end,age+i,.035);
               }
               ring(out,pose,Vec3.ZERO,u,v,radius*1.02,.018,0xB66BDCFF);
            }
            case MAGMA_FIST_CHARGE -> {
               double build=smooth(age/10), pulse=1+.05*Math.sin(age*.28);
               Vec3 c=u.scale(.55).add(direction.scale(.25));
               sphere(out,pose,c,(.16+.1*build)*pulse,0xFF722008);
               for(int i=0;i<5;i++) {
                  double t=i*Math.PI*2/5+age*.15;
                  Vec3 chip=c.add(u.scale(Math.cos(t)*(.25+.12*build))).add(v.scale(Math.sin(t)*.32));
                  sphere(out,pose,chip,.06+.025*build,0xFFE4550A);
                  tapered(out,pose,chip,chip.subtract(direction.scale(.16)),.035,.008,0xCFFFAD24);
               }
            }
            case MAGMA_HOUND_CHARGE -> {
               Vec3 c=direction.scale(.7).add(v.scale(Math.sin(age*.22)*.025));
               sphere(out,pose,c,.23+.05*smooth(age/10),0xFF5D1608);
               for(int side:new int[]{-1,1}) {
                  Vec3 ear=c.add(u.scale(side*.27)).add(v.scale(.3));
                  tube(out,pose,ear,ear.add(v.scale(.38)),u,direction,.1,0xFFB82C08);
                  sphere(out,pose,c.add(u.scale(side*.15)).add(v.scale(.05)).add(direction.scale(.26)),.055,0xFFFFC548);
               }
               tube(out,pose,c,c.add(direction.scale(.5)),u,v,.15,0xFFCB350A);
            }
            case MAGMA_WAKE -> {
               // Separated curling wave fronts: low silhouette and bounded by the hit radius.
               double fade=1-smooth((age-30)/10);
               ring(out,pose,new Vec3(0,.045,0),new Vec3(1,0,0),new Vec3(0,0,1),12,.012,alpha(0x55CC4009,fade));
               for(int wave=0;wave<3;wave++) {
                  double phase=smooth((age-(wave==0?15:wave==1?20:30))/8);
                  // The inner wave stays at radius four when the server switches to outer hits.
                  double r=(wave==0?Math.min(4,radius):12)*(.24+.74*phase);
                  for(int arc=0;arc<7;arc++) {
                     Vec3 previous=null;
                     for(int j=0;j<=5;j++) {
                        double t=arc*Math.PI*2/7+j*.105+wave*.33;
                        double rr=r*(1+.018*Math.sin(t*9+age*.12));
                        Vec3 next=new Vec3(Math.cos(t)*rr,.09+Math.sin(j*Math.PI/5)*.23*phase,Math.sin(t)*rr);
                        if(previous!=null) tapered(out,pose,previous,next,.025+phase*.055,.02,alpha(0xECF35A0B,fade*(.25+.75*Math.sin(Math.PI*phase))));
                        previous=next;
                     }
                  }
               }
            }
            case MAGMA_VOLCANO -> {
               double fade=1-smooth((age-32)/8);
               double expansion=smooth((age-19)/4),visualRadius=4+8*expansion;
               ring(out,pose,new Vec3(0,.045,0),new Vec3(1,0,0),new Vec3(0,0,1),12,.012,alpha(0x55CC4009,fade));
               ring(out,pose,new Vec3(0,.06,0),new Vec3(1,0,0),new Vec3(0,0,1),visualRadius,.035,alpha(0xBCE84B09,fade));
               if(age>=14)for(int i=0;i<11;i++) {
                  double t=i*2.39996,r=visualRadius*(i%2==0?.78:.28);
                  double rise=smooth((age-14-i%3*.4)/3);
                  double height=(8-5*expansion)*(.72+.24*Math.sin(i*7.3)) * rise;
                  Vec3 previous=new Vec3(Math.cos(t)*r,.1,Math.sin(t)*r);
                  for(int j=1;j<=6;j++) {
                     double f=j/6.0, lean=.6*f*f, curl=Math.sin(f*3+age*.15+i)*.15*f;
                     Vec3 next=new Vec3(Math.cos(t)*(r+lean)+Math.sin(t)*curl,.1+height*f,Math.sin(t)*(r+lean)-Math.cos(t)*curl);
                     double width=(.22+.07*Math.sin(i*4.7))*(1-f*.85)*rise;
                     tapered(out,pose,previous,next,width+.025,width*.72+.008,alpha(0xDD7F200C,fade));
                     // Thin hot seams inside dark, irregular crust rather than glowing ball tops.
                     tapered(out,pose,previous.add(.035,.015,.025),next.add(.035,.015,.025),width*.32,width*.16,alpha(0xF6FF8512,fade));
                     previous=next;
                  }
                  double life=age-15-i%4;
                  if(life>0 && life<18) {
                     double f=life/18;
                     Vec3 chip=new Vec3(Math.cos(t)*(r+.8*f),.2+height*4*f*(1-f),Math.sin(t)*(r+.8*f));
                     sphere(out,pose,chip,.085*(1-f)+.025,alpha(0xFFF27B0D,fade));
                  }
               }
            }
            case MAGMA_SKY -> {
               for(int i=0;i<7;i++) {
                  double t=i*Math.PI*2/7+age*.035;
                  double lift=.5+.5*Math.sin(age*.15+i*.9);
                  Vec3 a=new Vec3(Math.cos(t)*(1.2+.15*lift),1.9+lift*.8,Math.sin(t)*(1.2+.15*lift));
                  sphere(out,pose,a,.11+.035*lift,0xFF922308);
                  tapered(out,pose,a,a.add(-Math.sin(t)*.12,.35,Math.cos(t)*.12),.04,.006,0xCFFF991E);
               }
            }
            case MAGMA_TRAIL -> {
               Vec3 previous=Vec3.ZERO;
               for(int i=1;i<=7;i++) {
                  double f=i/7.0;
                  Vec3 next=direction.scale(-2.3*f).add(u.scale(Math.sin(age*.28-i*.7)*.1*f)).add(v.scale(Math.cos(age*.2-i*.5)*.08*f));
                  tapered(out,pose,previous,next,radius*(1-f*.78),radius*(1-f*.92),alpha(0xDF9B2E0C,1-f*.6));
                  tapered(out,pose,previous.add(u.scale(.025)),next.add(u.scale(.025)),radius*.35*(1-f),radius*.18*(1-f),alpha(0xEFFF9C13,1-f));
                  previous=next;
               }
            }
            case MAGMA_ERUPTION -> {
               Vec3 x=new Vec3(1,0,0),z=new Vec3(0,0,1);
               ring(out,pose,new Vec3(0,.05,0),x,z,radius,.08,0xFFFF5C0F);
               for(int i=0;i<18;i++) {
                  double theta=i*Math.PI/9+age*.01,r=radius*(i%2==0?.8:.4);
                  double height=age<15?.12:radius<8?6:2.5;
                  Vec3 c=new Vec3(Math.cos(theta)*r,.2,Math.sin(theta)*r);
                  tube(out,pose,c,c.add(Math.sin(theta)*.4,height,Math.cos(theta)*.4),x,z,.17,0xDF8F2008);
                  sphere(out,pose,c.add(0,height,0),.25,0xFFFF6B0F);
               }
            }
            case FIRE_BULLET -> {
               Vec3 back=direction.scale(-Math.min(2,velocity.length()));
               tube(out,pose,back,Vec3.ZERO,u,v,radius,0xBDFF7A14);
               sphere(out,pose,Vec3.ZERO,radius,0xFFFFEC8C);
            }
            case FIRE_FIST -> {
               Vec3 head=direction.scale(.4);
               sphere(out,pose,head,radius,profile.color());
               for(int i=0;i<4;i++) sphere(out,pose,head.add(direction.scale(.55)).add(u.scale((i-1.5)*radius*.43)),radius*.3,0xFFFFC04B);
               tube(out,pose,direction.scale(-3),head,u,v,radius*.55,0xAEFF8C1A);
               for(int i=0;i<5;i++) sphere(out,pose,direction.scale(-i*.55).add(v.scale(Math.sin(age*.6+i)*.13)),radius*(.65-i*.08),0xBFFF6014);
            }
            case FIRE_BALL -> {
               sphere(out,pose,Vec3.ZERO,radius,profile.color());
               sphere(out,pose,new Vec3(-radius*.2,radius*.2,-radius*.1),radius*.78,0xCFFFBB32);
               for(int i=0;i<12;i++) {
                  double t=i*Math.PI/6+age*.06;
                  Vec3 pos=new Vec3(Math.cos(t)*radius*.85,Math.sin(t*2+age*.1)*radius*.6,Math.sin(t)*radius*.85);
                  sphere(out,pose,pos,radius*.25,0xCFFFF09A);
               }
            }
            case FIREFLIES -> {
               for(int i=0;i<42;i++) {
                  double t=i*2.39996+age*.04;
                  double r=radius*Math.sqrt((i+.5)/42);
                  Vec3 pos=new Vec3(Math.cos(t)*r,.4+((i*13)%23)/6.5+Math.sin(age*.2+i)*.15,Math.sin(t)*r);
                  sphere(out,pose,pos,.06+.02*Math.sin(age*.4+i),0xFFFFDB64);
               }
            }
            case FIRE_PILLAR -> {
               Vec3 up=new Vec3(0,1,0),x=new Vec3(1,0,0),z=new Vec3(0,0,1);
               ring(out,pose,new Vec3(0,.04,0),x,z,radius,.07,0xFFFFB632);
               double height=age<10?.15:age<20?8:3;
               for(int column=0;column<20;column++) {
                  double t=column*Math.PI/10+age*.035;
                  double r=radius*(column%2==0?.94:.55);
                  Vec3 a=new Vec3(Math.cos(t)*r,.1,Math.sin(t)*r);
                  for(int j=1;j<=6;j++) {
                     double y=j*height/6;
                     Vec3 b=new Vec3(Math.cos(t+y*.25)*r,y,Math.sin(t+y*.25)*r);
                     tube(out,pose,a,b,x,z,.12*Math.max(.15,1-y/10),j%2==0?0xCFFF9E24:0x9FFF5012);a=b;
                  }
               }
            }
            case FIRE_DRAGON -> {
               double length=Math.max(0,Math.min(12,velocity.length()));
               Vec3 previous=Vec3.ZERO;
               for(int i=1;i<=20;i++) {
                  double f=i/20.0;
                  Vec3 next=direction.scale(length*f).add(u.scale(Math.sin(f*9-age*.3)*.25));
                  tube(out,pose,previous,next,u,v,.18+.35*f,0xBFFF771A);previous=next;
               }
               if(age>=10) {
                  Vec3 ground=new Vec3(0,-1.5,0);
                  ring(out,pose,ground,new Vec3(1,0,0),new Vec3(0,0,1),6,.08,0xFFFF9C26);
                  for(int i=0;i<24;i++) {
                     double t=i*Math.PI/12+age*.03;
                     Vec3 a=ground.add(Math.cos(t)*5.8,0,Math.sin(t)*5.8);
                     tube(out,pose,a,a.add(0,4,0),new Vec3(1,0,0),new Vec3(0,0,1),.1,0xBFFF7B18);
                  }
               }
               sphere(out,pose,previous,.7,0xECFFB53B);
               for(int side:new int[]{-1,1}) {
                  Vec3 horn=previous.add(u.scale(side*.45));
                  tube(out,pose,horn,horn.add(v.scale(.7)).add(direction.scale(-.5)),u,v,.07,0xFFFFEC94);
               }
            }
            case FLAME_TRAIL -> {
               for(int i=0;i<8;i++) {
                  Vec3 center=direction.scale(-i*.3).add(new Vec3(Math.sin(age*.4+i)*.15,.25+(i%3)*.23,0));
                  sphere(out,pose,center,.15+(i%3)*.07,i%2==0?0xDFFFBC38:0xAFFF6514);
               }
            }
            case LIGHT -> {
               double length = Math.min(6, Math.max(1.5, velocity.length() * 1.4));
               Vec3 back = direction.scale(-length);
               tube(out, pose, back, Vec3.ZERO, u, v, radius * 2.5, (profile.color() & 0xFFFFFF) | 0x55000000);
               tube(out, pose, back, Vec3.ZERO, u, v, radius, profile.color());
               tube(out, pose, back, Vec3.ZERO, u, v, radius * .3, 0xFFFFFFFF);
            }
            case PRESSURE -> {
               ring(out, pose, Vec3.ZERO, u, v, radius, .035, profile.color());
               ring(out, pose, direction.scale(-.25), u, v, radius * .72, .02, 0x72FFFFFF);
            }
            case NOTE -> {
               Vec3 a = u.scale(-radius * .3).add(v.scale(-radius * .45));
               Vec3 b = a.add(v.scale(radius * 1.5));
               ring(out, pose, a, u, v, radius * .24, .06, profile.color());
               tube(out, pose, a, b, u, direction, .045, profile.color());
               tube(out, pose, b, b.add(u.scale(radius * .75)).add(v.scale(-radius * .3)), v, direction, .065, 0xFF7DEBFF);
            }
            case HEART -> {
               Vec3 previous = heart(0, radius, u, v);
               for (int i = 1; i <= 28; i++) {
                  Vec3 next = heart(i * Math.PI * 2 / 28, radius, u, v);
                  tube(out, pose, previous, next, direction, next.subtract(previous).cross(direction).normalize(), .045, profile.color());
                  previous = next;
               }
            }
            case GHOST -> {
               ring(out, pose, v.scale(.15), u, v, radius, .06, profile.color());
               ring(out, pose, v.scale(.15), direction, v, radius, .025, 0x65FFFFFF);
               for (int side : new int[]{-1, 1}) {
                  Vec3 eye = u.scale(side * radius * .3).add(v.scale(radius * .3)).add(direction.scale(radius * .1));
                  tube(out, pose, eye, eye.add(v.scale(-radius * .2)), u, direction, .035, 0xFF758A9F);
               }
               Vec3 a = u.scale(-radius).add(v.scale(.15));
               for (int i = 1; i <= 6; i++) {
                  Vec3 b = u.scale(-radius + 2 * radius * i / 6).add(v.scale(-radius * (i % 2 == 0 ? .75 : 1.05)));
                  tube(out, pose, a, b, direction, u, .035, profile.color()); a = b;
               }
            }
            case SMOKE -> {
               for (int i = 0; i < 3; i++) {
                  Vec3 center = direction.scale(-i * .4).add(u.scale(Math.sin(age * .18 + i) * .08));
                  ring(out, pose, center, u, v, radius * (1 + i * .15), .07, profile.color());
               }
            }
            case SPARK -> {
               ring(out, pose, Vec3.ZERO, u, v, radius, .04, profile.color());
               ring(out, pose, Vec3.ZERO, direction, v, radius, .035, 0xFFFFF1C1);
            }
            case FIRE_BREATH -> {
               double length = Math.min(6, Math.max(2, velocity.length() * 2));
               tube(out, pose, direction.scale(-length), Vec3.ZERO, u, v, radius * .4, 0xFFFFF5A5);
               for (int i = 0; i < 7; i++) {
                  double fraction = i / 6.0;
                  Vec3 center = direction.scale(-length * fraction);
                  double r = radius * (1 - fraction * .5) * (1 + .12 * Math.sin(age * .8 + i));
                  ring(out, pose, center, u, v, r, .10, profile.color());
                  ring(out, pose, center, u, v, r * .65, .07, 0xDFFFBD35);
               }
            }
            case ARC -> {
               for (int strip = 0; strip < 3; strip++) {
                  Vec3 previous = null;
                  for (int i = 0; i <= 16; i++) {
                     double t = -.9 * Math.PI + i * 1.8 * Math.PI / 16;
                     Vec3 next = u.scale(Math.cos(t) * radius).add(v.scale(Math.sin(t) * radius))
                        .add(direction.scale(-strip * .12));
                     if (previous != null) tube(out, pose, previous, next, direction,
                        next.subtract(previous).cross(direction).normalize(), .035, profile.color());
                     previous = next;
                  }
               }
            }
            case RUBBLE -> {
               for (int i = 0; i < 4; i++) {
                  double t = age * .12 + i * Math.PI / 2;
                  Vec3 center = u.scale(Math.cos(t) * radius).add(v.scale(Math.sin(t) * radius));
                  double size = radius * (i % 2 == 0 ? .65 : .45);
                  Vec3 a = center.subtract(direction.scale(size)), b = center.add(direction.scale(size));
                  tube(out, pose, a, b, u, v, size, profile.color());
                  quad(out, pose, 0xFFC1BEC7, a.add(u.scale(size)).add(v.scale(size)), a.subtract(u.scale(size)).add(v.scale(size)),
                     a.subtract(u.scale(size)).subtract(v.scale(size)), a.add(u.scale(size)).subtract(v.scale(size)));
                  quad(out, pose, 0xFF625F67, b.add(u.scale(size)).subtract(v.scale(size)), b.subtract(u.scale(size)).subtract(v.scale(size)),
                     b.subtract(u.scale(size)).add(v.scale(size)), b.add(u.scale(size)).add(v.scale(size)));
               }
            }
            default -> { }
         }
      });
   }
   private static double smooth(double value) {
      double t=Math.max(0,Math.min(1,value));return t*t*(3-2*t);
   }
   private static void electricArc(VertexConsumer out,PoseStack.Pose pose,Vec3 start,Vec3 end,float age,double width) {
      if(width<.001)return;
      Vec3 delta=end.subtract(start);if(delta.lengthSqr()<.00001)return;
      Vec3 tangent=delta.normalize(),u=tangent.cross(Math.abs(tangent.y)>.95?new Vec3(1,0,0):new Vec3(0,1,0)).normalize(),v=tangent.cross(u);
      Vec3 previous=start;double amplitude=Math.min(.32,delta.length()*.1);
      for(int i=1;i<=12;i++) {
         double f=i/12.0,envelope=Math.sin(Math.PI*f);
         Vec3 next=start.add(delta.scale(f)).add(u.scale(Math.sin(i*4.9+Math.floor(age/2))*amplitude*envelope))
            .add(v.scale(Math.cos(i*7.1+Math.floor(age/2))*amplitude*.65*envelope));
         tapered(out,pose,previous,next,width,width*.7,0xD45AD5FF);
         tapered(out,pose,previous,next,width*.32,width*.24,0xFFF0FCFF);
         previous=next;
      }
   }
   private static int alpha(int color,double opacity) {
      return (color&0xFFFFFF)|((int)((color>>>24)*Math.max(0,Math.min(1,opacity)))<<24);
   }
   /** A tapered hexagonal segment with a basis perpendicular to its own tangent. */
   private static void tapered(VertexConsumer out,PoseStack.Pose pose,Vec3 a,Vec3 b,double start,double end,int color) {
      Vec3 tangent=b.subtract(a);
      if(tangent.lengthSqr()<.0000001 || (color>>>24)==0)return;
      tangent=tangent.normalize();
      Vec3 u=tangent.cross(Math.abs(tangent.y)>.95?new Vec3(1,0,0):new Vec3(0,1,0)).normalize();
      Vec3 v=tangent.cross(u);
      for(int i=0;i<6;i++) {
         double x=i*Math.PI/3,y=(i+1)*Math.PI/3;
         Vec3 p=u.scale(Math.cos(x)).add(v.scale(Math.sin(x))),q=u.scale(Math.cos(y)).add(v.scale(Math.sin(y)));
         quad(out,pose,color,a.add(p.scale(start)),a.add(q.scale(start)),b.add(q.scale(end)),b.add(p.scale(end)));
      }
   }
   private static void sphere(VertexConsumer out,PoseStack.Pose pose,Vec3 center,double radius,int color) {
      for(int latitude=0;latitude<6;latitude++) for(int longitude=0;longitude<12;longitude++) {
         double a=-Math.PI/2+latitude*Math.PI/6,b=a+Math.PI/6;
         double c=longitude*Math.PI/6,d=c+Math.PI/6;
         quad(out,pose,color,spherical(center,radius,a,c),spherical(center,radius,b,c),
            spherical(center,radius,b,d),spherical(center,radius,a,d));
      }
   }
   private static Vec3 spherical(Vec3 center,double r,double lat,double lon) {
      return center.add(Math.cos(lat)*Math.cos(lon)*r,Math.sin(lat)*r,Math.cos(lat)*Math.sin(lon)*r);
   }
   private static Vec3 heart(double t, double radius, Vec3 u, Vec3 v) {
      double x = Math.pow(Math.sin(t), 3);
      double y = (13 * Math.cos(t) - 5 * Math.cos(2*t) - 2 * Math.cos(3*t) - Math.cos(4*t)) / 16;
      return u.scale(x * radius).add(v.scale(y * radius));
   }
   private static void ring(VertexConsumer out, PoseStack.Pose pose, Vec3 center, Vec3 u, Vec3 v,
                            double radius, double width, int color) {
      for (int i = 0; i < 24; i++) {
         double a = i * Math.PI * 2 / 24, b = (i + 1) * Math.PI * 2 / 24;
         Vec3 innerA = center.add(u.scale(Math.cos(a)*(radius-width))).add(v.scale(Math.sin(a)*(radius-width)));
         Vec3 outerA = center.add(u.scale(Math.cos(a)*(radius+width))).add(v.scale(Math.sin(a)*(radius+width)));
         Vec3 outerB = center.add(u.scale(Math.cos(b)*(radius+width))).add(v.scale(Math.sin(b)*(radius+width)));
         Vec3 innerB = center.add(u.scale(Math.cos(b)*(radius-width))).add(v.scale(Math.sin(b)*(radius-width)));
         quad(out, pose, color, innerA, outerA, outerB, innerB);
      }
   }
   private static void tube(VertexConsumer out, PoseStack.Pose pose, Vec3 a, Vec3 b, Vec3 u, Vec3 v,
                            double radius, int color) {
      Vec3 x = u.scale(radius), y = v.scale(radius);
      Vec3[] p = {a.add(x).add(y), a.subtract(x).add(y), a.subtract(x).subtract(y), a.add(x).subtract(y),
                  b.add(x).add(y), b.subtract(x).add(y), b.subtract(x).subtract(y), b.add(x).subtract(y)};
      for (int i = 0; i < 4; i++) { int j = (i + 1) % 4; quad(out, pose, color, p[i], p[j], p[j+4], p[i+4]); }
   }
   private static void quad(VertexConsumer out, PoseStack.Pose pose, int color, Vec3... points) {
      for (Vec3 p : points) out.addVertex(pose, (float)p.x, (float)p.y, (float)p.z).setColor(color);
   }
}
