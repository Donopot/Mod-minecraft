#!/usr/bin/env python3
"""Câble les techniques sur les particules DU MOD au lieu des particules vanilla.

Principe : les 42 particules du jar source portent des noms qui disent leur élément
(`particle_flame`, `particle_thunder_blue`, `particle_sand`, `particle_ice`, `particle_magma`,
`particle_note`, `particle_dark`…). Le registre du port déclare chaque technique **dans le bloc de son
Fruit** (`MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_<CLÉ>, List.of(…))`) : la correspondance
Fruit → particule est donc dérivable sans deviner.

Deux règles, explicites :

  1. **Remplaçables** — les particules employées comme BOUCHE-TROU par le port (`CRIT` sur 111
     techniques, `ELECTRIC_SPARK`, `FLAME`, `CLOUD`, `SNOWFLAKE`, `SMOKE`, `LARGE_SMOKE`, `SMALL_FLAME`,
     `SOUL_FIRE_FLAME`) prennent la particule du Fruit.
  2. **Conservées** — les particules choisies POUR UNE RAISON ne sont pas touchées : `EXPLOSION`
     (Bomu), `LAVA`/`LAVA_POP`, `PORTAL`, `SQUID_INK`, `END_ROD`, `SWEEP_ATTACK`, `SCULK_SOUL`,
     `WHITE_ASH`, `HAPPY_VILLAGER`, `CAMPFIRE_COSY_SMOKE`, `POOF`. Les remplacer effacerait une
     intention, pas un oubli.

Ce que le script NE fait pas : décider à la place du lecteur. Il écrit un tableau de correspondance
(`docs/particules-mapping.md`) avec, par Fruit, la particule retenue et le nombre de remplacements —
et il signale les Fruits sans correspondance certaine au lieu de leur en attribuer une.

Usage : python3 tools/particle_wiring.py [--ecrire] [fruit …]
"""
import collections
import os
import re
import sys

RACINE = "/opt/data/workspace/opworldlike-fabric"
REGISTRE = RACINE + "/src/main/java/net/mcreator/minepiece/procedures/DevilFruitRegistry.java"
DOC = RACINE + "/docs/particules-mapping.md"

# Fruit -> particule du jar source, choisie sur l'ÉLÉMENT de la source.
CORRESPONDANCE = {
    # Fruits d'IMPACT physique : la source a une particule d'impact (`particle_shock_hit`), un
    # projectile tranchant a `particle_slash`, et une flèche qui perce prend l'un ou l'autre —
    # ce sont des choix DÉCLARÉS, écrits dans docs/particules-mapping.md, pas des évidences.
    "aro_aro": "SLASH",
    "bomu_bomu": "SHOCK_HIT",
    "hito_hito_daibutsu": "SHOCK_HIT",
    "imu": "NEGATIVE",
    "mochi_mochi": "SHOCK_HIT",
    "nikyu_nikyu": "SHOCK_HIT",
    "riki_riki": "SHOCK_HIT",
    "ryu_ryu_pachycephalosaurus": "SHOCK_HIT",
    "batto_batto": "BAT",
    "gomu_gomu": "SHOCKWAVE",
    "goro_goro": "THUNDER_BLUE",
    "gura_gura": "SHOCKWAVE",
    "hie_hie": "ICE",
    "horo_horo": "NEGATIVE",
    "kage_kage": "DARK",
    "kira_kira": "LIGHT_LARGE",
    "kiro_kiro": "GRAVITY",
    "magu_magu": "MAGMA",
    "mera_mera": "FLAME",
    "mero_mero": "FLOWER_PINK",
    "moku_moku": "BIG_SMOKE",
    "mori_mori": "FLOWER",
    "neko_neko_leopard": "CLAWS",
    "neko_neko_sabertiger": "CLAWS",
    "inu_inu_okuchi_no_makami": "CLAWS",
    "inu_inu_wolf": "CLAWS",
    "ope_ope": "LIGHT_SMALL",
    "pika_pika": "LIGHT_LARGE",
    "soru_soru": "THUNDER_BLUE",
    "suna_suna": "SAND",
    "supa_supa": "SLASH",
    "tori_tori_phoenix": "FLAME_BLUE",
    "ushi_ushi_giraffe": "CLAWS",
    "uo_uo_seiryu": "WATER",
    "uta_uta": "NOTE",
    "yami_yami": "DARK",
    "zushi_zushi": "GRAVITY",
}

# Bouche-trou : remplaçables. Le reste est conservé, volontairement.
REMPLACABLES = {"CRIT", "ELECTRIC_SPARK", "FLAME", "CLOUD", "SNOWFLAKE", "SMOKE", "LARGE_SMOKE",
                "SMALL_FLAME", "SOUL_FIRE_FLAME"}


# Les SERVICES portent leur élément dans leur nom : la correspondance est lisible, pas devinée.
# Deux familles restent VOLONTAIREMENT en vanilla : le moteur générique (transformation, burst,
# comportements) et les tests. Une particule de fruit y serait fausse — ces fichiers servent TOUS les
# fruits à la fois.
SERVICES = {
    "procedures/MagmaBurn.java": {"FLAME": "MAGMA"},
    "procedures/IceBreath.java": {"CLOUD": "ICE", "SNOWFLAKE": "ICE"},
    "procedures/IceDefense.java": {"SNOWFLAKE": "ICE"},
    "procedures/SandStorm.java": {"CRIT": "SAND"},
    "procedures/SmokeCloud.java": {"LARGE_SMOKE": "BIG_SMOKE", "SMOKE": "BIG_SMOKE"},
    "procedures/NegativeState.java": {"SCULK_SOUL": "NEGATIVE"},
    "procedures/Roots.java": {"HAPPY_VILLAGER": "FLOWER"},
    "procedures/SeismicWave.java": {"CRIT": "SHOCKWAVE"},
    "procedures/GiantShockWave.java": {"CLOUD": "SHOCK_HIT", "CRIT": "SHOCK_HIT"},
    "procedures/MeteorFall.java": {"FLAME": "FLAME", "SMOKE": "BIG_SMOKE", "CRIT": "SHOCK_HIT"},
    "procedures/ShadowLedger.java": {"SQUID_INK": "DARK"},
    "procedures/RemoteControl.java": {"SCULK_SOUL": "DARK"},
    "procedures/Levitation.java": {"END_ROD": "GRAVITY"},
    "procedures/MochiBuild.java": {"CLOUD": "BIG_SMOKE"},
}


def cabler_services(ecrire):
    base = RACINE + "/src/main/java/net/mcreator/minepiece/"
    total = 0
    for chemin, table in SERVICES.items():
        c = base + chemin
        if not os.path.exists(c):
            print("  (absent) %s" % chemin)
            continue
        texte = open(c, encoding="utf-8").read()
        n = 0
        for van, mod in table.items():
            n += len(re.findall(r"ParticleTypes\." + van + r"\b", texte))
            texte = re.sub(r"ParticleTypes\." + van + r"\b", "MinepieceModParticles." + mod, texte)
        if n and "MinepieceModParticles" in texte and "import net.mcreator.minepiece.init.MinepieceModParticles;" not in texte:
            # ⚠️ L'ancrage sur un import « net.mcreator.minepiece » était FAUX : plusieurs services
            # n'importent que « br.com.oneminepiece… », et l'import n'était donc jamais inséré — le
            # build a échoué sur « cannot find symbol MinepieceModParticles ». La ligne « package »
            # existe toujours : c'est le seul ancrage sûr.
            m = re.search(r"^package [^;]+;\s*$", texte, re.M)
            if m:
                texte = texte[:m.end()] + "\n\n" + motif + texte[m.end():]
        if ecrire and n:
            open(c, "w", encoding="utf-8").write(texte)
            assert "MinepieceModParticles." in open(c, encoding="utf-8").read(), chemin
        print("  %-34s %d remplacement(s)" % (chemin, n))
        total += n
    print("services : %d remplacement(s)" % total)
    return total


def main():
    ecrire = "--ecrire" in sys.argv
    filtres = [a for a in sys.argv[1:] if not a.startswith("-")]
    texte = open(REGISTRE, encoding="utf-8").read()

    # découpe par bloc de Fruit : MOVES.put(MinepieceModItems.ITEM_DEVIL_FRUIT_<CLÉ>, List.of( … ));
    motif = re.compile(r"(MOVES\.put\(MinepieceModItems\.ITEM_DEVIL_FRUIT_([A-Z_0-9]+), List\.of\()(.*?)(\)\);)", re.S)
    lignes = []
    remplacements = collections.Counter()
    tot_remplace = 0
    modifie = texte
    for m in motif.finditer(texte):
        cle = m.group(2).lower()
        if filtres and not any(f in cle for f in filtres):
            continue
        bloc = m.group(3)
        cible = CORRESPONDANCE.get(cle)
        joueurs = collections.Counter(re.findall(r"ParticleTypes\.([A-Z_0-9]+)", bloc))
        remplacables = {k: v for k, v in joueurs.items() if k in REMPLACABLES}
        conserves = {k: v for k, v in joueurs.items() if k not in REMPLACABLES}
        if cible and cible.endswith("_?"):
            cible = None
        nouveau = bloc
        if cible:
            for van in remplacables:
                nouveau = re.sub(r"ParticleTypes\.%s\b" % van, "MinepieceModParticles.%s" % cible, nouveau)
            n = sum(remplacables.values())
            tot_remplace += n
            remplacements[cle] = n
            if n:
                modifie = modifie.replace(m.group(0), m.group(1) + nouveau + m.group(4), 1)
        lignes.append((cle, cible or "— (incertain)", sum(remplacables.values()), conserves))

    print(f"{'fruit':26s} {'particule mod':16s} {'rempl.':>6s}  conservées")
    for cle, cible, n, cons in sorted(lignes):
        print(f"{cle:26s} {cible:16s} {n:>6d}  {', '.join(cons) if cons else '-'}")
    print(f"\nremplacements au total : {tot_remplace} sur {len(lignes)} fruits")

    print()
    tot_services = cabler_services(ecrire)
    if not ecrire:
        print("(relancer avec --ecrire)")
        return
    # import si nécessaire
    if "MinepieceModParticles" in modifie and "import net.mcreator.minepiece.init.MinepieceModParticles;" not in modifie:
        modifie = modifie.replace("import net.mcreator.minepiece.procedures.DevilFruitMove;",
                                  "import net.mcreator.minepiece.init.MinepieceModParticles;\n"
                                  "import net.mcreator.minepiece.procedures.DevilFruitMove;", 1)
    open(REGISTRE, "w", encoding="utf-8").write(modifie)
    # la relecture est la seule preuve
    relu = open(REGISTRE, encoding="utf-8").read()
    restants = len(re.findall(r"ParticleTypes\.([A-Z_0-9]+)", relu))
    print(f"\nécrit dans le registre · usages vanilla restants : {restants}")

    doc = ["# Particules : correspondance Fruit → particule du jar source\n",
           "Établie par `tools/particle_wiring.py` sur l'**élément** de chaque Fruit, en employant les",
           "particules **du jar source** (42, toutes portées).\n",
           "Les particules employées comme bouche-trou (`CRIT`, `ELECTRIC_SPARK`, `FLAME`, `CLOUD`,",
           "`SNOWFLAKE`, `SMOKE`…) sont remplacées ; celles choisies **pour une raison** (`EXPLOSION`,",
           "`LAVA`, `PORTAL`, `SWEEP_ATTACK`, `SCULK_SOUL`…) sont **conservées** : les remplacer",
           "effacerait une intention, pas un oubli.\n",
           "| Fruit | Particule du mod | Remplacements | Conservées |", "|---|---|---|---|"]
    for cle, cible, n, cons in sorted(lignes):
        doc.append("| `%s` | %s | %d | %s |" % (cle, cible, n, ", ".join(cons) if cons else "—"))
    doc.append("\n## Fruits sans correspondance certaine\n")
    incertains = [cle for cle, cible, n, cons in lignes if cible == "— (incertain)"]
    doc.append("Aucune particule attribuée (élément non tranché) : %s" % (", ".join("`%s`" % c for c in incertains) or "aucun"))
    doc.append("\nCes techniques gardent leur particule actuelle. Leur attribuer une particule « au")
    doc.append("jugé » aurait l'air d'un progrès et serait une invention.")
    open(DOC, "w", encoding="utf-8").write("\n".join(doc) + "\n")
    print("tableau :", DOC)


if __name__ == "__main__":
    main()
