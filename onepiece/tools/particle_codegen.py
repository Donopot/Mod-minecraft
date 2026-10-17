#!/usr/bin/env python3
"""Extrait les paramètres RÉELS des 42 particules source et génère le code du port.

Ce que la source met dans le bytecode, et qu'il faut lire pour ne pas approximer :
  * `lifetime` — parfois une CONSTANTE (15), parfois une EXPRESSION randomisée
    (`Math.max(1, 10 + rand.nextInt(6) - 3)`) ; un parseur qui ne cherche qu'une constante annonce
    « durée 0 » sur douze particules et se trompe ;
  * `gravity`, `friction`, `hasPhysics` ;
  * la TAILLE : `setSize(w, h)` puis `quadSize *= k` ;
  * le TYPE DE RENDU (`getRenderType`) — opaque ou translucide, ce qui décide si les bords fondus
    des textures sont respectés ;
  * l'ANIMATION de la sprite, dans `tick()` : `setSprite(spriteSet.get(age / pas % images + 1, images))`.

Usage : python3 tools/particle_codegen.py [--ecrire]
"""
import os
import re
import subprocess
import sys
import zipfile

JAR = "/opt/data/workspace/minepiece/MinePiece-ver15-forge-1.20.1.jar"
TMP = "/tmp/mp-particles"
ROOT = "/opt/data/workspace/opworldlike-fabric"
JAVA = ROOT + "/src/main/java/net/mcreator/minepiece"
JAVAP = "/opt/data/jdk/jdk-25.0.4.1+1/bin/javap"
PKG = "net.mcreator.minepiece.client.particle."


def javap(classe):
    return subprocess.run([JAVAP, "-c", "-p", "-classpath", TMP, classe],
                          capture_output=True, text=True).stdout


def entier(ligne):
    m = re.search(r"(?:bipush|sipush)\s+(-?\d+)", ligne)
    if m:
        return int(m.group(1))
    m = re.search(r"iconst_(-?\d)", ligne)
    if m:
        return int(m.group(1))
    m = re.search(r"ldc\s+#\d+\s+// int (-?\d+)", ligne)
    if m:
        return int(m.group(1))
    return None


def flottant(ligne):
    m = re.search(r"// float ([\d.]+)f", ligne)
    if m:
        return float(m.group(1))
    if re.search(r"fconst_0\b", ligne):
        return 0.0
    if re.search(r"fconst_1\b", ligne):
        return 1.0
    if re.search(r"fconst_2\b", ligne):
        return 2.0
    return None


def extraire(classe):
    out = javap(PKG + classe)
    if not out:
        return None
    lignes = out.splitlines()
    res = {"animee": False, "opaque": False, "images": 0, "pas": 1}

    # ---- constructeur : champs numériques
    for i, l in enumerate(lignes):
        if "putfield" not in l:
            continue
        if "f_107225_:I" in l:          # lifetime
            fenetre = lignes[max(0, i - 16):i]
            txt = "\n".join(fenetre)
            m = re.search(r"Math\.max:\(II\)I", txt)
            ints = [entier(x) for x in fenetre if entier(x) is not None]
            rand = re.search(r"m_188503_:\(I\)I", txt)
            if rand and ints:
                # Math.max(a, b + rand(k) [- c])
                base, borne = ints[0], ints[1] if len(ints) > 1 else 0
                k = ints[2] if len(ints) > 2 else 0
                off = ints[3] if len(ints) > 3 else 0
                res["lifetime_expr"] = (base, borne, k, off)
            elif ints:
                res["lifetime"] = ints[-1]
        elif "f_107226_:F" in l:        # gravity
            v = next((flottant(lignes[j]) for j in range(i - 1, max(0, i - 4), -1)
                      if flottant(lignes[j]) is not None), None)
            if v is not None:
                res["gravity"] = v
        elif "f_107228_:F" in l:        # friction
            v = next((flottant(lignes[j]) for j in range(i - 1, max(0, i - 4), -1)
                      if flottant(lignes[j]) is not None), None)
            if v is not None:
                res["friction"] = v
        elif "f_107219_:Z" in l:        # hasPhysics
            res["hasPhysics"] = "iconst_0" not in lignes[max(0, i - 2)]

    # ---- taille : setSize + quadSize *= k
    for i, l in enumerate(lignes):
        if "m_107250_" in l:
            vals = [flottant(lignes[j]) for j in range(i - 1, max(0, i - 5), -1)]
            vals = [v for v in vals if v is not None]
            if vals:
                res["setSize"] = max(vals)
        if "putfield" in l and "f_107663_:F" in l:
            vals = [flottant(lignes[j]) for j in range(i - 1, max(0, i - 5), -1)]
            vals = [v for v in vals if v is not None]
            if vals:
                res["facteur"] = vals[0]

    # ---- type de rendu
    m = re.search(r"getstatic\s+#\d+\s+// Field net/minecraft/client/particle/ParticleRenderType\.(f_\d+)", out)
    if m:
        res["render_field"] = m.group(1)

    # ---- animation de la sprite, dans tick() : get(pas, images)
    m = re.search(r"m_5819_:\(II\)", out)
    if m:
        i = out.find(m.group(0))
        fenetre = out[max(0, i - 700):i]
        ints = [int(x) for x in re.findall(r"(?:bipush|sipush)\s+(\d+)|iconst_(\d)", fenetre)
                for x in [x[0] or x[1]] if x]
        if len(ints) >= 2:
            res["pas"] = ints[-2]
            res["images"] = ints[-1]
            res["animee"] = True
    return res


TYPES = open(os.path.join(os.path.dirname(__file__), "particle_types.tpl"), encoding="utf-8").read()
CLIENT = open(os.path.join(os.path.dirname(__file__), "particle_client.tpl"), encoding="utf-8").read()


def classe_de(d):
    return "".join(x.capitalize() for x in d.split("_")) + "Particle"


def champ_de(d):
    return d.replace("particle_", "").upper()


def duree_java(p):
    if "lifetime_expr" in p:
        a, b, k, o = p["lifetime_expr"]
        # `level.random` est PROTÉGÉ en 26.3 : l'accesseur public est getRandom().
        return "Math.max(%d, %d + level.getRandom().nextInt(%d)%s)" % (a, b, k, (" - %d" % o) if o else "")
    return str(int(p.get("lifetime", 20) or 20))


def main():
    ecrire = "--ecrire" in sys.argv
    z = zipfile.ZipFile(JAR)
    os.makedirs(TMP, exist_ok=True)
    for n in z.namelist():
        if "/client/particle/" in n and n.endswith(".class"):
            z.extract(n, TMP)
    classes = sorted(os.path.basename(n)[:-6] for n in z.namelist()
                     if "/client/particle/" in n and n.endswith(".class") and "$" not in n)
    par = {}
    for c in classes:
        r = extraire(c)
        if r:
            par[c] = r
    print("%-40s %-22s %6s %6s %7s %6s %5s %s" %
          ("classe", "durée", "grav", "frot", "taille", "type", "img", "animée"))
    for c, p in sorted(par.items()):
        d = duree_java(p)
        print("%-40s %-22s %6.2f %6.2f %7.2f %6s %5d %s"
              % (c, d[:22], p.get("gravity", 0), p.get("friction", 0),
                 0.1 * p.get("facteur", 1), p.get("render_field", "?")[-6:],
                 p.get("images", 0), p["animee"]))
    definitions = sorted(os.path.basename(x)[:-5] for x in z.namelist()
                         if x.startswith("assets/minepiece/particles/") and x.endswith(".json"))
    if not ecrire:
        print("\n(relancer avec --ecrire pour générer le code)")
        return
    champs, switch, enr = [], [], []
    for d in definitions:
        p = par.get(classe_de(d), {})
        champs.append('   /** %s — durée %s, gravité %.2f, taille %.2f. */\n'
                      '   public static final SimpleParticleType %s = register("%s");'
                      % (d, duree_java(p), p.get("gravity", 0.0),
                         0.1 * p.get("facteur", 1), champ_de(d), d))
        switch.append('         case "%s" -> %s;' % (d, champ_de(d)))
        # Le source fait `quadSize *= k` sur la valeur PAR DÉFAUT de TextureSheetParticle (0,1) :
        # la taille vaut donc 0,1 × k. Un premier calcul en « setSize × k » annonçait des particules
        # de 64 blocs, et un `/8 » supposait que setSize était la taille — il ne l'est pas.
        taille = 0.1 * p.get("facteur", 1.0)
        opaque = "true" if p.get("render_field", "").endswith(("f_107430_", "f_107428_")) else "false"
        enr.append('      registry.register(MinepieceModParticles.%s,\n'
                   '         sprites -> (type, level, x, y, z, vx, vy, vz, aleatoire) ->\n'
                   '            new SpriteParticle(sprites, level, x, y, z, vx, vy, vz, %.3fF, %s, '
                   '%.3fF, %.3fF, %d, %d, %s));'
                   % (champ_de(d), taille, duree_java(p), p.get("gravity", 0.0),
                      p.get("friction", 0.98), p.get("images", 0), p.get("pas", 1), opaque))
    open(JAVA + "/init/MinepieceModParticles.java", "w", encoding="utf-8").write(
        TYPES.replace("§N§", str(len(definitions)))
             .replace("§CHAMPS§", "\n\n".join(champs))
             .replace("§SWITCH§", "\n".join(switch)))
    open(JAVA + "/client/MinepieceParticlesClient.java", "w", encoding="utf-8").write(
        CLIENT.replace("§N§", str(len(definitions)))
              .replace("§ENREGISTREMENTS§", "\n".join(enr)))
    print("\nécrits : %d types + %d rendus" % (len(definitions), len(enr)))


if __name__ == "__main__":
    main()
