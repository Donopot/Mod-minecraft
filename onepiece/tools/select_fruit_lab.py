"""Numbered private lab menu, supporting all 40 fruits without CMD choice limits."""
from pathlib import Path
import argparse,csv,json
from launch_visual_lab import run
P=Path(__file__).resolve().parents[1]
ap=argparse.ArgumentParser();ap.add_argument('--lab',type=Path,required=True);ap.add_argument('--fruits',default='');a=ap.parse_args()
all_fruits=[row['fruit'] for row in csv.DictReader((P/'docs/matrice-fruits-40.csv').open(encoding='utf-8'))]
choices=a.fruits.split(',') if a.fruits else all_fruits
assert choices and set(choices)<=set(all_fruits)
lab=a.lab.resolve();assert lab.parent==(P.parent/'.runtime/onepiece-lab').resolve()
manifest=json.loads((lab/'visual-lab.local.json').read_text(encoding='utf-8'));assert not manifest['capture']
print('One Piece - laboratoire prive. Changer de fruit reconstruit le decor de test.\n')
for i,fruit in enumerate(choices,1):print(f'{i:2}. {fruit.replace("_"," ")}')
while True:
 selected=input('\nNumero du fruit (0 pour quitter) : ').strip()
 if selected=='0':raise SystemExit(0)
 if selected.isascii() and selected.isdecimal() and 1<=int(selected)<=len(choices):break
 print('Choisir un numero de la liste.')
print('R : technique, B : suivante, V : formes, F5 : camera. Terrain modifie definitivement.')
run(lab,choices[int(selected)-1],resume=True)
