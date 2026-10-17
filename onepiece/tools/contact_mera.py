"""Contact sheets from owned Mera lab screenshots; originals stay untouched."""
from pathlib import Path
import argparse,json
from PIL import Image,ImageDraw,ImageFont
ap=argparse.ArgumentParser();ap.add_argument('lab',type=Path);ap.add_argument('--output',default='mera-review');a=ap.parse_args();P=Path(__file__).resolve().parents[1];out=P/'build'/a.output;out.mkdir(parents=True,exist_ok=True)
font=ImageFont.truetype('C:/Windows/Fonts/consola.ttf',13);scenes=['hiken','higan','hibashira','kagero','hotarubi','kaenryuo','daienkai','propulsion'];paths=[]
def order(p):
 name=p.stem
 if 'emission-' in name:return int(name.rsplit('-',1)[1])
 return 22 if name.endswith('-fps') else 42 if name.endswith('-charge') else 47 if name.endswith('-active') else 54 if name.endswith('-impact') else 70 if name.endswith('-after') else int(name.rsplit('-',1)[1])
for who,d in [('actor','client'),('observer','observer')]:
 cols=8 if who=='actor' else 3;sheet=Image.new('RGB',(cols*225,8*170),(22,25,30));draw=ImageDraw.Draw(sheet)
 for y,scene in enumerate(scenes):
  files=sorted((a.lab/d/'screenshots').glob(who+'-'+scene+'-*.png'),key=order)
  for x,p in enumerate(files):
   im=Image.open(p).convert('RGB');im.thumbnail((225,145));sheet.paste(im,(x*225,y*170+22));draw.text((x*225+2,y*170+3),p.stem.replace(who+'-',''),font=font,fill='white')
 target=out/(who+'-mera.jpg');sheet.save(target,quality=94);paths.append(str(target.resolve()));print(target)
(out/'sheets.local.json').write_text(json.dumps({'lab':str(a.lab.resolve()),'sheets':paths},indent=2),encoding='utf-8')
