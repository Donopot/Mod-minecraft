# Exemple Hermes distant → servitor Minecraft

Ces fichiers sont des **exemples à adapter**, préparés sans accéder à ton serveur Hermes. Aucune adresse ni clé personnelle n'est enregistrée, aucun service SSH n'est installé et aucune configuration réelle n'est modifiée.

Le montage proposé fait lancer le MCP sur l'hôte Minecraft Windows par SSH depuis l'hôte Hermes. SSH transporte l'entrée/sortie du MCP ; RCON reste utilisé localement par le processus Python. Il faut un serveur Minecraft dédié Fabric avec RCON activé, et un accès SSH par clé déjà préparé entre les deux machines.

1. Sur l'hôte Minecraft, placer le dépôt et créer son environnement Python avec `python -m venv .venv`, puis installer `voiddynasty/tools/requirements-mcp.txt`. Installer le pack alpha dans une instance de test.
2. Adapter [le lanceur PowerShell](start-servitor-mcp.example.ps1) avec les chemins absolus du dépôt et du serveur Minecraft. Le placer au chemin choisi dans la configuration SSH, par exemple `C:/VoidDynasty/start-servitor-mcp.ps1`. Le mot de passe RCON reste dans le `server.properties` local.
3. Sur l'hôte Hermes, adapter [ssh_config.example](ssh_config.example) avec l'adresse de **la machine Minecraft**, l'utilisateur SSH et le chemin de clé locale. Vérifier l'empreinte SSH de cet hôte avant d'enregistrer sa clé connue. Le modèle d'exemple utilise la vérification stricte, sans acceptation automatique d'une clé inconnue.
4. Reprendre le couple `command` / `args` de [mcp.remote.example.json](mcp.remote.example.json) dans le gestionnaire MCP d'Hermes. `mcpServers` est une enveloppe d'exemple courante, **pas un fichier de configuration Hermes vérifié** : le format final dépend de la version de ton agent.
5. Vérifier que le client découvre exactement les neuf outils `servitor_*`. Le lanceur utilise `--servitor-only`, qui n'enregistre aucun des outils historiques d'administration. Essayer d'abord `servitor_capabilities`, `servitor_observe` et une estimation ; ceux-ci ne lancent pas de chantier. Associer ensuite [le rôle proposé](ROLE-SERVITOR.md).

L'option SSH `-T` évite un terminal interactif et `BatchMode=yes` empêche une demande de mot de passe de bloquer le protocole. Le lanceur n'écrit aucun texte sur stdout en dehors de JSON-RPC. Les journaux doivent aller sur stderr. Une déconnexion ne supprime pas la mission : elle continue sur Minecraft, puis se consulte avec le même UUID.

Le rôle dispose d'actions de minage et de construction réelles dans les zones indiquées. Exécuter ce raccordement avec un compte dédié et garder l'accès RCON restreint à l'hôte Minecraft. L'exemple ne constitue pas à lui seul une isolation système des privilèges du compte SSH.

Références de format et de configuration : [connexion aux serveurs MCP stdio](https://modelcontextprotocol.io/docs/develop/connect-local-servers), [configuration OpenSSH sous Windows](https://learn.microsoft.com/en-us/windows-server/administration/openssh/openssh-server-configuration), [options du client SSH](https://man.openbsd.org/ssh.1). La connexion à Hermes et le transport SSH entre tes deux machines restent à tester sur ton infrastructure ; le protocole MCP local est testé par le laboratoire.
