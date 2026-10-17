"""Contact sheets for private B02 graphical QA; originals remain untouched."""
from pathlib import Path
import argparse,json
from PIL import Image,ImageDraw,ImageFont
p=argparse.ArgumentParser();p.add_argument('lab',type=Path);p.add_argument('--output',default='batch02-review');args=p.parse_args()
R=Path(__file__).resolve().parents[1];dest=R/'build'/args.output;dest.mkdir(parents=True,exist_ok=True)
font=ImageFont.truetype('C:/Windows/Fonts/consola.ttf',14)
meta=json.loads((args.lab/'visual-lab.local.json').read_text(encoding='utf-8'))
groups={};cursor=0
for name,count in [('gura',6),('bomu',5),('zushi',6),('nikyu',5),('ope',4)]:
 if meta.get('retake') and name+'_'+name not in meta['retake'].split(','):continue
 groups[name]=range(cursor,cursor+count);cursor+=count
outputs=[]
for who,folder in [('actor','client'),('observer','observer')]:
 for fruit,indexes in groups.items():
  files=[]
  for i in indexes:
   found=list((args.lab/folder/'screenshots').glob(f'{who}-{i:02d}-*.png'))
   files.extend(sorted(found,key=lambda f:int(f.stem.rsplit('-',1)[1])))
  if len(files)!=5*len(indexes):continue
  canvas=Image.new('RGB',(1500,230*len(indexes)),(22,25,30));draw=ImageDraw.Draw(canvas)
  for k,f in enumerate(files):
   image=Image.open(f).convert('RGB');image.thumbnail((300,205));x=(k%5)*300;y=(k//5)*230
   canvas.paste(image,(x,y+23));draw.text((x+4,y+3),f.stem.replace(who+'-',''),font=font,fill='white')
  out=dest/(who+'-'+fruit+'.jpg');canvas.save(out,quality=94);outputs.append(str(out));print(out)
(dest/'sheets.local.json').write_text(json.dumps({'lab':str(args.lab),'contact_sheets':outputs},indent=2),encoding='utf-8')
