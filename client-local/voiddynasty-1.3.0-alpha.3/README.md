# VOID DYNASTY 1.3.0-alpha.3 — correction des zones vides

Pour **Minecraft Java 26.3, Fabric Loader 0.19.5 et Java 25**. Rogue Trader est facultatif. Cette version corrige le servitor qui court sur place au repos, la préparation d'un dépôt sans bloc à miner et les servitors oubliés lors d'une fin de chantier immédiate.

## Mise à jour du client

1. Fermer Minecraft et sauvegarder les mondes avant de modifier une instance existante.
2. Dans `mods`, retirer les anciennes versions du mod principal. Garder **un seul** `voiddynasty-<version>.jar`.
3. Copier les quatre JAR de [mods](mods). Le bridge 0.7.1, Fabric API et GeckoLib sont identiques au pack alpha 2 ; il n'est pas nécessaire de les remplacer s'ils sont déjà identiques.

| Fichier | Rôle |
|---|---|
| `voiddynasty-1.3.0-alpha.3.jar` | Mod principal avec le correctif du servitor. |
| `voiddynasty-bridge-0.7.1.jar` | Pad, coffres, armes et logistique existants. |
| `fabric-api-0.162.0+26.3.jar` | Fabric API. |
| `geckolib-fabric-26.3-5.5.7.jar` | Animations GeckoLib. |

Pour un ancien servitor bloqué, `/work stop` le retire tout en conservant les ressources et les reçus. `/work status` indique ensuite le sac et le journal. Le retour du sac utilise `/work deposit` ; `/work commit` abandonne explicitement les reçus une fois le chantier arrêté et le sac vide.

Une nouvelle zone vide se termine sans coffre inutile et sans servitor restant dans le monde. Le message de `/work status` indique `Zone vide: aucun bloc a miner; chantier termine.` S'il reste un sac à rapporter, le servitor le dépose avant de terminer. Les observations des conteneurs et les reçus existants restent conservés.

La collecte MCP, les accès horizontaux et les deux plans de construction restent ceux de l'[alpha 2](../../voiddynasty/docs/SERVITOR-POLYVALENT-ALPHA2.md). Les commandes `/work` fonctionnent en solo avec les commandes de triche activées ; le raccordement MCP nécessite toujours la préparation décrite dans le guide. Le format de sauvegarde reste 5.

[Correctif détaillé](../../voiddynasty/docs/SERVITOR-1.3.0-alpha.3.md) · [Validation](VALIDATION.md) · [Empreintes et sources](manifest.json) · [SHA256.txt](SHA256.txt). Les licences d'origine restent dans [licences](licences), avec les mentions MIT/CC0 historiques de la base. Ce pack ne contient aucun monde, secret, bibliothèque du jeu ou asset One Piece. Il ne modifie pas l'ancien installeur inter-jeux.
