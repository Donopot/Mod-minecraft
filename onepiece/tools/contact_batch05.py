from pathlib import Path
import argparse,json
from PIL import Image,ImageDraw,ImageFont
ap=argparse.ArgumentParser();ap.add_argument('lab',type=Path);ap.add_argument('--output',default='batch05-review');args=ap.parse_args();P=Path(__file__).resolve().parents[1];out=P/'build'/args.output;out.mkdir(parents=True,exist_ok=True);font=ImageFont.truetype('C:/Windows/Fonts/consola.ttf',14);paths=[]
for who,folder in [('actor','client'),('observer','observer')]:
 groups={}
 for f in (args.lab/folder/'screenshots').glob(who+'-*.png'):
  parts=f.stem.split('-');groups.setdefault(parts[2],{}).setdefault(int(parts[1]),[]).append(f)
 for fruit,rows in groups.items():
  sheet=Image.new('RGB',(1800,230*len(rows)),(22,25,30));draw=ImageDraw.Draw(sheet)
  for y,(_,files) in enumerate(sorted(rows.items())):
   for x,f in enumerate(sorted(files,key=lambda f:int(f.stem.rsplit('-',1)[1]))):
    im=Image.open(f).convert('RGB');im.thumbnail((300,205));sheet.paste(im,(x*300,y*230+23));draw.text((x*300+3,y*230+3),f.stem.replace(who+'-',''),font=font,fill='white')
  target=out/(who+'-'+fruit+'.jpg');sheet.save(target,quality=94);paths.append(str(target));print(target)
(out/'sheets.local.json').write_text(json.dumps({'lab':str(args.lab),'contact_sheets':paths},indent=2),encoding='utf-8')
