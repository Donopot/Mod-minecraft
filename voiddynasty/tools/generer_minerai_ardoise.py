#!/usr/bin/env python3
"""Fabrique la texture du minerai de ceramite EN ARDOISE (deepslate).

DECISION DE DONOPOT 5b : « scinder une variante ardoise ». Le filon genere deja dans les deux
materiaux (`stone_ore_replaceables` et `deepslate_ore_replaceables`) mais posait TOUJOURS le meme
bloc : en profondeur on trouvait donc un minerai a texture de pierre. Le filon avait la meme
apparence partout.

Methode : la texture du minerai est une pierre portant des taches de ceramite. On isole les taches
en comparant notre minerai a la texture de pierre du jeu, puis on les pose sur la texture d'ardoise
du jeu. C'est une composition reproductible (--verifier compte les pixels), pas une retouche a la
main. Le resultat est declare comme GENERE dans docs/DESIGN.md.

    python3 tools/generer_minerai_ardoise.py [--client-jar ...] [--verifier]
"""
from __future__ import annotations

import argparse
import os
import shutil
import subprocess
import sys
import tempfile
import zipfile

from PIL import Image

RACINE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))


def extraire_vanilla(jar: str, noms: list[str], dossier: str) -> dict[str, str]:
    """Extrait des textures de bloc du jar client. Le jar peut etre absent : on le dit."""
    chemins = {}
    with zipfile.ZipFile(jar) as z:
        for nom in noms:
            interne = f"assets/minecraft/textures/block/{nom}.png"
            try:
                with z.open(interne) as f:
                    cible = os.path.join(dossier, f"{nom}.png")
                    with open(cible, "wb") as g:
                        shutil.copyfileobj(f, g)
                    chemins[nom] = cible
            except KeyError:
                pass
    return chemins


def main() -> int:
    p = argparse.ArgumentParser(description="Genere la texture du minerai de ceramite en ardoise.")
    p.add_argument("--client-jar", default="/opt/data/.gradle/caches/fabric-loom/26.3/minecraft-client-only.jar")
    p.add_argument("--verifier", action="store_true", help="ne rien ecrire : dire ce qui serait fait")
    a = p.parse_args()

    base_nom = "ceramite_ore"
    dossier_out = os.path.join(RACINE, "src/main/resources/assets/voiddynasty/textures/block")
    notre_chemin = os.path.join(dossier_out, f"{base_nom}.png")
    sortie_chemin = os.path.join(dossier_out, f"deepslate_{base_nom}.png")

    if not os.path.exists(notre_chemin):
        print(f"ERREUR : {notre_chemin} introuvable")
        return 2
    if not os.path.exists(a.client_jar):
        print(f"ERREUR : jar client introuvable : {a.client_jar}")
        return 2

    tmp = tempfile.mkdtemp(prefix="mine-ardoise-")
    try:
        vanilla = extraire_vanilla(a.client_jar, ["stone", "deepslate"], tmp)
        if "stone" not in vanilla or "deepslate" not in vanilla:
            print(f"ERREUR : textures vanilla manquantes ({sorted(vanilla)})")
            return 2

        notre = Image.open(notre_chemin).convert("RGBA")
        pierre = Image.open(vanilla["stone"]).convert("RGBA")
        ardoise = Image.open(vanilla["deepslate"]).convert("RGBA")
        if notre.size != pierre.size or notre.size != ardoise.size:
            print(f"ERREUR : tailles differentes — notre {notre.size}, pierre {pierre.size}, ardoise {ardoise.size}")
            return 2

        pn, pp, pa = notre.load(), pierre.load(), ardoise.load()
        sortie = Image.new("RGBA", notre.size)
        ps = sortie.load()
        taches = 0
        for y in range(notre.height):
            for x in range(notre.width):
                r, v, b, alpha = pn[x, y]
                if alpha <= 40:
                    ps[x, y] = (0, 0, 0, 0)
                    continue
                # pixel de tache si notre minerai s'ecarte nettement de la pierre du jeu
                ecart = max(abs(r - pp[x, y][0]), abs(v - pp[x, y][1]), abs(b - pp[x, y][2]))
                if ecart > 18:
                    ps[x, y] = (r, v, b, alpha)          # la ceramite est reprise telle quelle
                    taches += 1
                else:
                    ps[x, y] = pa[x, y]                   # le fond devient l'ardoise du jeu

        total = notre.width * notre.height
        part = 100.0 * taches / total
        print(f"fond : ardoise vanilla · taches de ceramite : {taches} px sur {total} ({part:.1f} %)")
        if part < 5.0:
            print("ATTENTION : moins de 5 % de taches — la texture du minerai n'est peut-etre pas une")
            print("            pierre + taches, et cette composition ne veut alors rien dire.")
            return 1
        if a.verifier:
            print(f"rien ecrit (--verifier) : {sortie_chemin} serait ecrit")
            return 0
        sortie.save(sortie_chemin)
        print(f"ecrit : {sortie_chemin}")
        return 0
    finally:
        shutil.rmtree(tmp, ignore_errors=True)


if __name__ == "__main__":
    sys.exit(main())
