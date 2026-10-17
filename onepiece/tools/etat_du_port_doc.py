#!/usr/bin/env python3
"""Rend le document de référence (docs/ETAT-DU-PORT.md) en PDF lisible.

Usage :  python3 tools/etat_du_port_doc.py

Le document est la source unique : ce script ne fait que le mettre en page. HTML intermédiaire
dans /tmp/doc, PDF copié à la racine du dépôt pour être envoyé tel quel.
"""
import html
import os
import re
import subprocess
import sys

RACINE = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DOC = os.path.join(RACINE, "docs/ETAT-DU-PORT.md")
TRAVAIL = "/tmp/doc"
HTML = os.path.join(TRAVAIL, "etat-du-port.html")
PDF = os.path.join(TRAVAIL, "OP-World-Like-etat-du-portage.pdf")
COPIE = os.path.abspath(os.path.join(RACINE, "..", "OP-World-Like-etat-du-portage.pdf"))
CHROME = "/opt/data/local/chrome/opt/google/chrome/chrome"

CSS = """
@page { size: A4; margin: 18mm 16mm 20mm; }
body { font-family: "DejaVu Serif", Georgia, serif; color: #1d1d1f; font-size: 10.4pt;
       line-height: 1.5; max-width: 780px; margin: 0 auto; }
h1 { font-size: 24pt; line-height: 1.15; margin: 0 0 4pt; letter-spacing: -0.4pt; }
h2 { font-size: 14.5pt; margin: 22pt 0 6pt; padding-bottom: 4pt;
     border-bottom: 1.5pt solid #C89B67; page-break-after: avoid; }
h3 { font-size: 11.8pt; margin: 14pt 0 4pt; page-break-after: avoid; }
p { margin: 0 0 7pt; text-align: justify; }
hr { border: 0; border-top: 1pt solid #e3ddd2; margin: 16pt 0; }
code { font-family: "DejaVu Sans Mono", monospace; font-size: 8.8pt;
       background: #f6f2ea; padding: 0.5pt 2.5pt; border-radius: 2pt; }
pre { background: #f8f5ef; border-left: 2.5pt solid #C89B67; padding: 8pt 10pt;
      white-space: pre-wrap; page-break-inside: avoid; }
pre code { background: none; font-size: 8.6pt; }
table { border-collapse: collapse; width: 100%; margin: 8pt 0 12pt; font-size: 9.1pt;
        page-break-inside: avoid; }
th { background: #f1ebe0; text-align: left; }
th, td { border: 0.6pt solid #ded6c8; padding: 3.4pt 5pt; vertical-align: top; }
tbody tr:nth-child(even) { background: #fbf9f5; }
blockquote { margin: 6pt 0; padding: 5pt 9pt; background: #f6f2ea;
             border-left: 2.5pt solid #C89B67; color: #4a443c; }
ul, ol { margin: 4pt 0 8pt; padding-left: 16pt; }
li { margin-bottom: 2.5pt; }
.case { font-weight: bold; color: #8a7f6d; }
.case.ok { color: #2f7d4f; }
"""


def enligne(txt):
    txt = html.escape(txt)
    txt = re.sub(r"`([^`]+)`", r"<code>\1</code>", txt)
    txt = re.sub(r"\*\*([^*]+)\*\*", r"<strong>\1</strong>", txt)
    return re.sub(r"(?<!\*)\*([^*]+)\*(?!\*)", r"<em>\1</em>", txt)


def convertir(md):
    lignes = md.split("\n")
    sortie, i, n = [], 0, len(lignes)
    while i < n:
        ligne = lignes[i]
        if ligne.startswith("```"):
            bloc = []
            i += 1
            while i < n and not lignes[i].startswith("```"):
                bloc.append(html.escape(lignes[i]))
                i += 1
            i += 1
            sortie.append("<pre><code>" + "\n".join(bloc) + "</code></pre>")
            continue
        if re.match(r"^\s*\|", ligne) and i + 1 < n and re.match(r"^\s*\|[\s:\-|]+\|\s*$", lignes[i + 1]):
            entetes = [c.strip() for c in ligne.strip().strip("|").split("|")]
            i += 2
            corps = []
            while i < n and re.match(r"^\s*\|", lignes[i]):
                corps.append([c.strip() for c in lignes[i].strip().strip("|").split("|")])
                i += 1
            t = ["<table><thead><tr>" + "".join(f"<th>{enligne(c)}</th>" for c in entetes) + "</tr></thead><tbody>"]
            for r in corps:
                t.append("<tr>" + "".join(f"<td>{enligne(c)}</td>" for c in r) + "</tr>")
            sortie.append("".join(t) + "</tbody></table>")
            continue
        m = re.match(r"^(#{1,4})\s+(.*)$", ligne)
        if m:
            niv = len(m.group(1))
            sortie.append(f"<h{niv}>{enligne(m.group(2))}</h{niv}>")
            i += 1
            continue
        if re.match(r"^\s*---\s*$", ligne):
            sortie.append("<hr>")
            i += 1
            continue
        if re.match(r"^\s*\d+\.\s+", ligne):
            items = []
            while i < n and re.match(r"^\s*\d+\.\s+", lignes[i]):
                items.append("<li>" + enligne(re.sub(r"^\s*\d+\.\s+", "", lignes[i])) + "</li>")
                i += 1
            sortie.append("<ol>" + "".join(items) + "</ol>")
            continue
        if re.match(r"^\s*[-*]\s+", ligne):
            items = []
            while i < n and re.match(r"^\s*[-*]\s+", lignes[i]):
                txt = re.sub(r"^\s*[-*]\s+", "", lignes[i])
                case = ""
                mc = re.match(r"^\[([ xX])\]\s+(.*)$", txt)
                if mc:
                    case = '<span class="case ok">&#9745;</span> ' if mc.group(1) != " " else '<span class="case">&#9744;</span> '
                    txt = mc.group(2)
                items.append(f"<li>{case}{enligne(txt)}</li>")
                i += 1
            sortie.append("<ul>" + "".join(items) + "</ul>")
            continue
        if ligne.startswith(">"):
            sortie.append("<blockquote>" + enligne(ligne.lstrip("> ")) + "</blockquote>")
            i += 1
            continue
        if not ligne.strip():
            i += 1
            continue
        para = [ligne]
        i += 1
        while i < n and lignes[i].strip() and not re.match(
                r"^(#{1,4}\s|\s*[-*]\s|\s*\d+\.\s|\s*\||```|>|\s*---\s*$)", lignes[i]):
            para.append(lignes[i])
            i += 1
        sortie.append("<p>" + enligne(" ".join(para)) + "</p>")
    return "".join(sortie)


def main():
    os.makedirs(TRAVAIL, exist_ok=True)
    corps = convertir(open(DOC, encoding="utf-8").read())
    page = ('<!doctype html><html lang="fr"><head><meta charset="utf-8">'
            "<title>OP World Like — état du portage</title>"
            f"<style>{CSS}</style></head><body>{corps}</body></html>")
    open(HTML, "w", encoding="utf-8").write(page)

    incoherentes = 0
    for t in re.findall(r"<table>.*?</table>", page, re.S):
        attendu = len(re.findall(r"<th>", t))
        for r in re.findall(r"<tr>(.*?)</tr>", t, re.S)[1:]:
            if len(re.findall(r"<td>", r)) != attendu:
                incoherentes += 1
    print(f"tableaux : {page.count('<table>')} · lignes incohérentes : {incoherentes}")
    if incoherentes:
        print("ATTENTION : une cellule contient une barre verticale non échappée dans un tableau.")

    subprocess.run([CHROME, "--headless=new", "--disable-gpu", "--no-sandbox",
                    "--disable-dev-shm-usage", "--no-pdf-header-footer",
                    f"--print-to-pdf={PDF}", f"file://{HTML}"], capture_output=True)
    if not os.path.exists(PDF):
        print("ÉCHEC : le PDF n'a pas été produit.")
        return 1
    subprocess.run(["cp", PDF, COPIE], check=False)
    data = open(PDF, "rb").read()
    print(f"PDF : {len(data)} o · {len(re.findall(rb'/Type\s*/Page[^s]', data))} pages")
    print(f"copie : {COPIE}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
