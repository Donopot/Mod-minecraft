#!/usr/bin/env python3
"""Preuve de bout en bout : le minerai de ceramite se GENERE-t-il, dans les DEUX materiaux ?

Pourquoi cet outil existe
-------------------------
Le harnais tourne dans un monde PLAT : il peut prouver qu'un bloc est enregistre, qu'un tag est poses,
qu'un filon est declare dans le biome — il ne peut PAS prouver qu'un bloc apparait vraiment dans le sol.
La decision 5b (variante « ardoise ») ne vaut que si l'ardoise est reellement generate en profondeur.

Ce que fait l'outil : monte un serveur Fabric JETABLE (monde neuf, generateur normal), le laisse
generer les chunks du spawn, l'arrete, puis COMPTE les blocs dans les fichiers de region avec
tools/region_blocks.py. Le dossier temporaire est supprime a la fin — rien n'est touche ailleurs.

    python3 tools/verifier_minerai_genere.py --jar build/libs/voiddynasty-1.0.0.jar
"""
from __future__ import annotations

import argparse
import os
import re
import shutil
import subprocess
import sys
import tempfile
import time

RACINE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
SERVEUR_MODELE = "/opt/data/workspace/mc-server"


def preparer(dossier: str, jar: str) -> None:
    """Monte un serveur jetable par LIENS : recopier 336 Mo n'apporterait rien."""
    for nom in ("libraries", "config"):
        source = os.path.join(SERVEUR_MODELE, nom)
        if os.path.exists(source):
            os.symlink(source, os.path.join(dossier, nom))
    # ATTENTION : mods/ doit etre un VRAI dossier contenant des LIENS vers chaque fichier. Lier le
    # dossier entier ferait de tout ajout ou retrait dans le dossier temporaire une modification du
    # serveur modele — c'est exactement l'erreur que j'ai commise une fois : le jar du serveur s'est
    # retrouve remplace par un lien vers le build.
    os.makedirs(os.path.join(dossier, "mods"), exist_ok=True)
    for f in os.listdir(os.path.join(SERVEUR_MODELE, "mods")):
        os.symlink(os.path.join(SERVEUR_MODELE, "mods", f), os.path.join(dossier, "mods", f))
    # le jar sous test : celui du build courant, qui porte le meme nom que celui du serveur modele
    lien_jar = os.path.join(dossier, "mods", os.path.basename(jar))
    if os.path.lexists(lien_jar):
        os.remove(lien_jar)
    os.symlink(os.path.abspath(jar), lien_jar)
    # tout ce que le lanceur Fabric attend a cote de lui : le lanceur, le jar serveur vanilla,
    # sa configuration, et le dossier des versions
    for nom in ("fabric-server-launch.jar", "server.jar", "fabric-server-launcher.properties", "versions"):
        source = os.path.join(SERVEUR_MODELE, nom)
        cible = os.path.join(dossier, nom)
        if os.path.exists(source) and not os.path.lexists(cible):
            os.symlink(source, cible)
    open(os.path.join(dossier, "eula.txt"), "w", encoding="utf-8").write("eula=true\n")
    open(os.path.join(dossier, "server.properties"), "w", encoding="utf-8").write(
        "level-name=monde-verif\n"
        "level-type=minecraft:normal\n"          # le monde du harnais est plat : on veut du vrai sol
        "online-mode=false\n"
        "spawn-protection=0\n"
        "max-players=1\n"
        "view-distance=6\n"
        "simulation-distance=4\n"
        "enable-rcon=false\n"
        "server-port=25599\n"
    )


def lancer(dossier: str, java: str, limite_s: int = 300) -> str:
    """Demarre, attend « Done ( » puis arrete proprement. Renvoie le journal."""
    journal = os.path.join(dossier, "journal.txt")
    with open(journal, "w", encoding="utf-8") as j:
        proc = subprocess.Popen(
            [java, "-Xmx2G", "-jar", "fabric-server-launch.jar", "nogui"],
            cwd=dossier, stdout=j, stderr=subprocess.STDOUT, stdin=subprocess.PIPE, text=True)
        debut = time.time()
        pret = False
        while time.time() - debut < limite_s:
            time.sleep(3)
            texte = open(journal, encoding="utf-8", errors="ignore").read()
            if "Done (" in texte:
                pret = True
                break
            if proc.poll() is not None:
                break
        if pret:
            time.sleep(25)          # laisse generer les chunks du spawn
        # arret propre : la console du serveur lit « stop » sur l'entree standard
        try:
            if proc.stdin is not None:
                proc.stdin.write("stop\n")
                proc.stdin.flush()
                proc.wait(timeout=90)
        except Exception:  # noqa: BLE001
            pass
        if proc.poll() is None:
            proc.terminate()
        try:
            proc.wait(timeout=60)
        except subprocess.TimeoutExpired:
            proc.kill()
    return journal


def main() -> int:
    p = argparse.ArgumentParser(description="Prouve que le minerai se genere dans les deux materiaux.")
    p.add_argument("--jar", default=os.path.join(RACINE, "build/libs/voiddynasty-1.0.0.jar"))
    p.add_argument("--java", default="/opt/data/jdk/jdk-25.0.4.1+1/bin/java")
    p.add_argument("--garder", action="store_true", help="ne pas supprimer le dossier temporaire")
    a = p.parse_args()
    if not os.path.exists(a.jar):
        print(f"ERREUR : {a.jar} introuvable")
        return 2

    dossier = tempfile.mkdtemp(prefix="verif-minerai-")
    try:
        preparer(dossier, a.jar)
        journal = lancer(dossier, a.java)
        texte = open(journal, encoding="utf-8", errors="ignore").read()
        if "Done (" not in texte:
            print("ERREUR : le serveur n'a pas demarre. Dernieres lignes :")
            print("\n".join(texte.splitlines()[-12:]))
            return 2

        ereurs = [l for l in texte.splitlines() if "ERROR" in l or "Exception" in l]
        monde = os.path.join(dossier, "monde-verif")
        # En 26.3 le monde vit dans <monde>/dimensions/minecraft/overworld/region, pas <monde>/region.
        regions = os.path.join(monde, "region")
        if not os.path.isdir(regions):
            for racine, dossiers, _ in os.walk(monde):
                if "region" in dossiers:
                    regions = os.path.join(racine, "region")
                    break
        if not os.path.isdir(regions):
            # un echec doit dire OU le monde est alle et ce que le serveur a raconte
            print(f"ERREUR : aucune region generee sous {monde}")
            trouves = []
            for racine, dossiers, _ in os.walk(dossier):
                for d in list(dossiers):
                    if d == "region":
                        trouves.append(os.path.join(racine, d))
            print("   dossiers « region » trouves :", trouves or "aucun")
            print("   contenu de", dossier, ":", sorted(os.listdir(dossier))[:20])
            print("   dernieres lignes du journal serveur :")
            print("\n".join(texte.splitlines()[-15:]))
            return 2
        nb_fichiers = len([f for f in os.listdir(regions) if f.endswith(".mca")])

        # comptage reel, bloc par bloc : region_blocks.py sait deja decompresser un .mca, on
        # reutilise sa fonction plutot que de relire le format une deuxieme fois
        sys.path.insert(0, os.path.join(RACINE, "tools"))
        import collections
        import glob as globmod
        import region_blocks

        total: collections.Counter = collections.Counter()
        chunks = 0
        for f in globmod.glob(os.path.join(regions, "*.mca")):
            trouve, lus = region_blocks.blocs_du_fichier(f)
            total.update(trouve)
            chunks += lus
        nb_fichiers = len(globmod.glob(os.path.join(regions, "*.mca")))

        # ATTENTION A CE QUE CES NOMBRES SONT : region_blocks.py cherche les identifiants dans le NBT
        # decompresse. Un identifiant y apparait une fois par SECTION (16x16x16) de palette qui le
        # contient. Ce sont donc des PRESENCES, pas des quantites de blocs — les temoins vanilla le
        # montrent (192 « pierre » pour 529 chunks). Pour la question posee — « l'ardoise se genere-t-elle
        # vraiment ? » — la presence suffit, et c'est meme la bonne mesure.
        resultats = {nom: total.get(f"voiddynasty:{nom}", 0)
                     for nom in ("ceramite_ore", "deepslate_ceramite_ore")}
        temoins = {nom: total.get(f"minecraft:{nom}", 0)
                   for nom in ("iron_ore", "deepslate_iron_ore")}
        autres = {n: v for n, v in total.items() if n.startswith("voiddynasty:") and v}

        print(f"monde neuf · {nb_fichiers} fichier(s) de region · {chunks} chunks lus "
              f"· erreurs serveur : {len(ereurs)}")
        print(f"   voiddynasty:ceramite_ore            {resultats['ceramite_ore']:5d} sections de palette")
        print(f"   voiddynasty:deepslate_ceramite_ore  {resultats['deepslate_ceramite_ore']:5d} sections de palette")
        print(f"   temoins vanilla — iron_ore {temoins['iron_ore']} · deepslate_iron_ore "
              f"{temoins['deepslate_iron_ore']} (pour situer la frequence)")
        ok = resultats["ceramite_ore"] > 0 and resultats["deepslate_ceramite_ore"] > 0
        if not ok:
            print("\nECHEC : un des deux minerais n'apparait pas dans un monde neuf.")
            print("        La variante ardoise ne se genere donc pas, ou le filon ne genere pas du tout.")
            print("        (Inventaire complet cherche dans la sortie de region_blocks.py.)")
            print("   blocs voiddynasty trouves :", dict(sorted(autres.items())))
        else:
            print("\nOK : les DEUX variantes apparaissent dans un monde neuf — la 5b est prouvee en jeu.")
        return 0 if ok else 1
    finally:
        if a.garder:
            print(f"dossier conserve : {dossier}")
        else:
            shutil.rmtree(dossier, ignore_errors=True)
            print("dossier temporaire supprime (seul ce que cet outil a cree)")


if __name__ == "__main__":
    sys.exit(main())
