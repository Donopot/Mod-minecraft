# CORE-028 — langue, HUD, infobulles et retours d'échec

**Constat de départ** : les noms de techniques et les messages étaient largement codés en dur — et
plusieurs messages parlaient de « Curse Power », un terme qui n'apparaissait nulle part ailleurs dans le
projet, alors que les fichiers de langue de ver15 utilisaient « Energy ».

Objectif : noms, descriptions et messages exacts **dans la langue choisie**, interface lisible.

---

## 1. Langue retenue

**Le français (`fr_fr`) est la langue choisie pour ce projet.** Le port fournit donc trois locales :

| Locale | État | Rôle |
|---|---|---|
| `fr_fr.json` | **créé par CORE-028** (2025 clés) | langue de référence du projet |
| `en_us.json` | complété (+162 clés de technique) | langue source / repli |
| `ja_jp.json` | hérité de ver15, inchangé | conservé tel quel |

## 2. Règles de traduction des noms de techniques

Sur les **167 techniques** uniques :

- **93 traduites** — les noms descriptifs anglais, via une table rédigée à la main
  (« Fire Fist » → « Poing de Feu », « Ice Block: Pheasant Beak » → « Bloc de Glace : Bec du Faisan ») ;
- **74 conservées** — les noms propres / romaji (« Hiken », « Raigo », « Soru », « Rankyaku »,
  « Yasakani no Magatama »), exactement comme la localisation officielle française du manga.

> Une première version traduisait mot à mot : elle produisait du français incorrect
> (« Obscur Matière », « Glace Ère », « Atomique Jet »). Elle a été remplacée par une table explicite,
> car l'ordre des mots diffère entre les deux langues. Le générateur ne devine plus : il traduit
> uniquement ce qui est explicitement listé.

Les **noms de Fruits** suivent la nomenclature officielle FR : « Fruit Flamme-Flamme »,
« Fruit Séisme-Séisme », « Fruit Ténèbres-Ténèbres », etc. (+ les modèles : « Modèle : Phénix »,
« Modèle : Giraffe »…).

## 3. Vocabulaire unifié

Le terme « Curse Power » **a été supprimé de tous les textes visibles par le joueur**. La ressource
s'appelle désormais :

- **FR : « Énergie »** · **EN : « Energy »** — via la clé déjà existante `minepiece.overlay.energy`
  (vocabulaire de ver15, donc cohérent avec le reste du mod).

Vérifiable : le contrôle `M4` compte les occurrences de « Curse Power » dans les deux fichiers de
langue — résultat attendu **0**.

## 4. HUD

Nouveau HUD client (`client/hud/DevilFruitHud.java`), affiché **uniquement si le joueur a mangé un Fruit** :

```
┌────────────────────────────────┐
│ Fruit du Démon                 │
│   Fruit Flamme-Flamme          │
│ Énergie   120/200              │
│ ████████████████░░░░░░         │
│ Technique 3/7 Poing de Feu     │
│ Forme     Hybride              │
└────────────────────────────────┘
```

- barre d'énergie colorée (verte → rouge sous 20 %) ;
- nom du Fruit et de la technique **localisés** (clés `item.minepiece.*` et `minepiece.technique.*`) ;
- forme courante (base / hybride).

Implémentation : API 26.3 `HudElementRegistry` + `HudElement`. Pour obtenir le nom localisé de la
technique côté client, `SyncStatePayload` (CORE-004) transporte désormais aussi **l'id de la technique
courante et le nombre de techniques** — le HUD n'a donc aucun texte en dur.

## 5. Retours d'échec et messages

Les **25 littéraux** codés en dur du périmètre Fruits ont tous été câblés sur des clés de traduction
(`minepiece.message.*`), y compris les 6 refus du lanceur de techniques :

| Situation | Clé | FR |
|---|---|---|
| Pas de Fruit | `message.no_fruit` | « Tu n'as pas mangé de Fruit du Démon. » |
| Techniques non portées | `message.not_ported` | « Fruit enregistré, mais ses techniques ne sont pas encore portées. » |
| Suppression par la mer | `message.sea_suppressed` | « Tes pouvoirs sont scellés par la mer ! » |
| Recharge en cours | `message.cooldown` | « Technique en recharge. » |
| Énergie insuffisante | `message.no_energy` | « Pas assez d'Énergie (%s requis, %s disponible). » |
| Limite d'invocations | `message.summon_limit` | « Limite d'invocations atteinte (%s). » |

## 6. Vérification

### Statique

```bash
python3 tools/i18n_gen.py --check     # couverture + littéraux en dur + synchronisation fr_fr
```

Sortie attendue :

```
techniques déclarées         : 167
clés absentes de en_us.json  : 0
clés absentes du fr_fr généré: 0
fr_fr.json à jour            : True
littéraux joueur codés en dur: 0
RÉSULTAT : à jour
```

### En jeu (serveur)

Le scope `/dftest i18n` vérifie, **depuis le jar réellement chargé** :

| id | Contrôle |
|---|---|
| `M1-lang-fr-present` | `fr_fr.json` et `en_us.json` sont lisibles et non vides |
| `M2-techniques-traduites` | chaque technique du registre a sa clé FR |
| `M3-cles-hud-messages` | les 29 clés utilisées par le code (HUD + messages) existent en FR |
| `M4-vocabulaire-energie` | 0 occurrence de « Curse Power » dans les textes joueur |

## 7. Régénérer les traductions

```bash
python3 tools/i18n_gen.py --generate
```

Le générateur (`tools/i18n_gen.py`) contient la table `TECHNIQUES_FR` et les 41 noms de Fruits FR.
Ajouter une technique = ajouter son nom dans le registre puis relancer `--generate` : la clé EN est
créée automatiquement, et la clé FR est traduite si l'id figure dans la table, sinon le nom d'origine
est conservé.

**Maintenance** : les fichiers de langue sont générés — ne pas les éditer à la main, sinon
`--check` signalera une désynchronisation.

## 8. Limite connue

Le masquage du HUD par la touche F1 n'est pas géré par le mod : le champ `hideGui` d'`Options`
n'existe plus en 26.3, et la visibilité est décidée par le pipeline HUD vanilla, dans lequel notre
élément s'insère (`addLast`). Si F1 ne masque pas notre panneau, c'est une correction d'une ligne à
faire dès que l'accès à l'état de visibilité aura été identifié.
